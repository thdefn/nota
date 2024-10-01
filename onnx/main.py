from fastapi import FastAPI
from io import BytesIO
from PIL import Image
import asyncio
from service import run_inference
from confluent_kafka import Producer, Consumer, KafkaError
from contextlib import asynccontextmanager
from config import KAFKA_BOOTSTRAP_SERVERS, KAFKA_GROUP_ID, KAFKA_TOPICS
import json
import base64

app = FastAPI()

producer_conf = {
    'bootstrap.servers': KAFKA_BOOTSTRAP_SERVERS
}
producer = Producer(producer_conf)

consumer_conf = {
    'bootstrap.servers': KAFKA_BOOTSTRAP_SERVERS,
    'group.id': KAFKA_GROUP_ID
}
consumer = Consumer(consumer_conf)
consumer.subscribe(KAFKA_TOPICS)


@asynccontextmanager
async def lifespan(app: FastAPI):
    task = asyncio.create_task(consume_messages())
    yield
    task.cancel()

app = FastAPI(lifespan=lifespan)

async def consume_messages():
    while True:
        msg = consumer.poll(1.0)
        if msg is None:
            await asyncio.sleep(1)
            continue
        if msg.error():
            if msg.error().code() == KafkaError._PARTITION_EOF:
                continue
            else:
                print(f"Consumer error: {msg.error()}")
                break
        message_data = json.loads(msg.value().decode('utf-8'))
        inference_id = message_data['id']
        try:
            file_content = base64.b64decode(message_data['fileContent'])
            await predict(file_content, inference_id)
        except Exception as e:
            print(f"Error in processing message: {e}")

            publishing_message = {"id": inference_id}
            message_str = json.dumps(publishing_message)

            topic = "inference_fail"
            producer.produce(topic, message_str.encode('utf-8'))
            producer.poll(0)
            producer.flush()

        await asyncio.sleep(0.1)


async def predict(file_content, inference_id):
        image = Image.open(BytesIO(file_content))
        result = run_inference(image)
        print(f"Prediction result: {result}")
        publishing_message = {"id" : inference_id, "result": result}
        message_str = json.dumps(publishing_message)

        topic = "inference_success"
        producer.produce(topic, message_str.encode('utf-8'))
        producer.poll(0)
        producer.flush()
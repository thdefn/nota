from fastapi import FastAPI, File, UploadFile
from io import BytesIO
from PIL import Image
import asyncio
from service import run_inference
from confluent_kafka import Producer, Consumer, KafkaError
from contextlib import asynccontextmanager
from config import KAFKA_BOOTSTRAP_SERVERS, KAFKA_GROUP_ID, KAFKA_TOPICS
import json

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
        try:
            message_data = json.loads(msg.value().decode('utf-8'))
            print(f"Received message: {message_data}")

            await predict(message_data)
        except Exception as e:
            print(f"Error in processing message: {e}")

        await asyncio.sleep(0.1)


async def predict(message):
        image = Image.open(BytesIO(message))
        result = run_inference(image)
        print(f"Prediction result: {result}")
        publishing_message = {"result": result}
        message_str = json.dumps(publishing_message)

        topic = "prediction_result_topic"
        producer.produce(topic, message_str.encode('utf-8'))
        producer.flush()
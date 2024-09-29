from fastapi import FastAPI, File, UploadFile
from io import BytesIO
from PIL import Image
from service import run_inference

app = FastAPI()

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    contents = await file.read()
    image = Image.open(BytesIO(contents))

    result = run_inference(image)

    return {"result": result}

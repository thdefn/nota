## Setup
1. 해당 저장소를 클론합니다.
   ```
   git clone https://github.com/thdefn/nota.git
   ```
2. 루트 디렉토리로 이동합니다.
3. 루트 디렉토리에서 Docker Compose를 실행합니다.
   ```
   docker compose up -d
   ```
4. 아래 링크에서 api document 를 살펴보시고, 실행하실 수 있습니다. <br/>
   api document 링크 : http://localhost:8080/swagger-ui/index.html#/
5. request 형식 등 자세한 설명은 [아래 api 명세](#api-명세) 에서 확인하실 수 있습니다.



## API 명세
### 1. Single Inference 요청 api
#### request
##### request syntax

`POST /inferences`

```curl
curl -X 'POST' \
  'http://localhost:8080/inferences?runtime=onnx' \
  -H 'accept: application/json' \
  -H 'userId: mock' \
  -H 'Content-Type: multipart/form-data' \
  -F 'image=@green-apply.jpg;type=image/jpeg'
```
##### request elements
| 필드       | 타입     | 필수 여부 | 설명                        |
|----------|--------|----|---------------------------|
| runtime    | `String` | 필수 | `onnx` / `tflite`         |
| image | `image/jpeg` | 필수 | jpeg / jpg / png 확장자의 이미지 |

#### response
##### response syntax
```http
HTTP/1.1 202 

{
  "id": 0
}
```

### 2. Single Inference 결과 확인 api
[1. Single Inference 요청 api](#1-single-inference-요청-api) 에 대한 결과를 조회하는 api 입니다.
#### request
##### request syntax

`GET /inferences/{inferenceId}`

```curl
curl -X 'GET' \
  'http://localhost:8080/inferences/4' \
  -H 'accept: */*'
```
##### request elements
| 필드       | 타입          | 필수 여부 | 설명                                       |
|----------|-------------|----|------------------------------------------|
| inferenceId    | `number`    | 필수 | [#1](#1-single-inference-요청-api) 응답값의 `id` |

#### response
##### response syntax
**처리중 상태**
```http
HTTP/1.1 202 

{
  "id": 0,
  "isProcessing": true
}
```

**완료 상태**
```http
HTTP/1.1 200

{
  "id": 0,
  "isProcessing": true,
  "result": "string"
}
```


### 3. Single Inference 기록 삭제 api
#### request
##### request syntax

`DELETE /inferences/{inferenceId}`

```curl
curl -X 'DELETE' \
  'http://localhost:8080/inferences/5' \
  -H 'accept: */*' \
  -H 'userId: mock'
```

##### request elements
| 필드       | 타입          | 필수 여부 | 설명                                       |
|----------|-------------|----|------------------------------------------|
| inferenceId    | `number`    | 필수 | [#1](#1-single-inference-요청-api) 응답값의 `id` |

#### response
##### response syntax

```http
HTTP/1.1 200
```

### 4. Single Inference 기록 조회 api
#### request
##### request syntax

`GET /inferences`

```curl
curl -X 'GET' \
  'http://localhost:8080/inferences?page=0&size=10&runtime=onnx&userId=mock&createdAt=2024-10-03T17%3A00%3A00' \
  -H 'accept: */*'
```

##### request elements
| 필드     | 타입          | 필수 여부 | 설명                                       |
|--------|-------------|------|------------------------------------------|
| page   | `number`    | 필수아님 | 페이지 정보                                   |
| size   | `number`    | 필수아님 | 하나의 페이지에 아이템 수                           |
| runtime | `String` | 필수아님 | `onnx` / `tflite`                        |
| userId | `String` | 필수아님 | 유저 아이디                                   |
| createdAt | `String` | 필수아님 | 'yyyy-MM-ddTHH:00:00' 형식의 조회하고자 하는 기록 시간 |

#### response
##### response syntax

```http
HTTP/1.1 200

{
  "content": [
    {
      "id": 3,
      "fileName": "green-apply.jpg",
      "runtime": "ONNX",
      "status": "COMPLETE",
      "result": "apple",
      "userId": "mock",
      "createdAt": "2024-10-03T17:22:55.584802"
    },
    {
      "id": 4,
      "fileName": "green-apply.jpg",
      "runtime": "ONNX",
      "status": "COMPLETE",
      "result": "apple",
      "userId": "mock",
      "createdAt": "2024-10-03T17:23:20.741174"
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 2,
    "totalPages": 1
  }
}
```


### 5. Inference 기록 삭제 주기 변경 api
#### request
##### request syntax

`PUT /schedule/inference-history`

```curl
curl -X 'PUT' \
  'http://localhost:8080/schedule/inference-history?cronExpression=0%200%2012%20%2A%20%2A%20%2A' \
  -H 'accept: */*'
```

##### request elements
| 필드     | 타입       | 필수 여부 | 설명                                                  |
|--------|----------|----|-----------------------------------------------------|
| cronExpression   | `String` | 필수 | `* * * * * *` (초 분 시 일 월 요일) 형식의 다섯자리 cron expression |
#### response
##### response syntax

```http
HTTP/1.1 200
```





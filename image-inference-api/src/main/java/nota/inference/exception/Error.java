package nota.inference.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum Error {
    NOT_ALLOWED_FILE(HttpStatus.BAD_REQUEST, "하용되지 않는 파일 확장자 입니다."),
    INFERENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 추론 기록이 없습니다."),
    INFERENCE_EXECUTION_FAILED(HttpStatus.BAD_GATEWAY, "추론 실행 중 문제가 발생했습니다."),
    REQUEST_ARGUMENT_NOT_VALID(HttpStatus.BAD_REQUEST, "요청 데이터의 형식을 확인해주세요."),
    REQUEST_ARGUMENT_MISSING(HttpStatus.BAD_REQUEST, "요청 데이터가 모두 있는지 확인해주세요.")
    ;
    public final HttpStatus httpStatus;
    public final String message;
}

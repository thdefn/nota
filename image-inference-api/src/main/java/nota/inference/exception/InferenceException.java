package nota.inference.exception;

import lombok.Getter;


@Getter
public class InferenceException extends RuntimeException {
    private final Error error;

    public InferenceException(Error error) {
        this.error = error;
    }
}

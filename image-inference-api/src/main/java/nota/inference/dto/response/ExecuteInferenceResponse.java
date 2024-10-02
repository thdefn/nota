package nota.inference.dto.response;

public record ExecuteInferenceResponse(
        Long id
) {
    public static ExecuteInferenceResponse of(Long id) {
        return new ExecuteInferenceResponse(id);
    }
}

package nota.inference.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import nota.inference.domain.model.Inference;

@Builder
public record InferenceResultResponse(
        Long id,
        boolean isProcessing,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String result
) {
    public static InferenceResultResponse from(Inference inference) {
        return InferenceResultResponse.builder()
                .id(inference.getId())
                .result(inference.getResult())
                .isProcessing(inference.isProcessing())
                .build();
    }
}

package nota.inference.dto.response;

import lombok.Builder;
import nota.inference.domain.model.Inference;

import java.time.LocalDateTime;

@Builder
public record InferenceHistoryItem(
        Long id,
        String fileName,
        String runtime,
        String status,
        String result,
        String userId,
        LocalDateTime createdAt
) {
    public static InferenceHistoryItem from(Inference inference) {
        return InferenceHistoryItem.builder()
                .id(inference.getId())
                .fileName(inference.getFileName())
                .status(inference.getStatus().name())
                .result(inference.getResult())
                .userId(inference.getUserId())
                .runtime(inference.getRuntime().name())
                .createdAt(inference.getCreatedAt())
                .build();
    }
}

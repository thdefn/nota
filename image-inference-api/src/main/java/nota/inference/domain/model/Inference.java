package nota.inference.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inferences", indexes = {
        @Index(name = "idx_inference_userId", columnList = "userId"),
        @Index(name = "idx_inference_runtime", columnList = "runtime"),
        @Index(name = "idx_inference_id_userId_runtime", columnList = "id, userId, runtime")
})
public class Inference extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Runtime runtime;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private InferenceStatus status;

    @Column(nullable = false)
    private String userId;

    public static Inference of(Runtime runtime, String fileName, String userId) {
        return Inference.builder()
                .runtime(runtime)
                .fileName(fileName)
                .userId(userId)
                .status(InferenceStatus.PROCESSING)
                .build();
    }
}

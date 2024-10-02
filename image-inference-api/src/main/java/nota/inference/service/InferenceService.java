package nota.inference.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nota.inference.domain.model.Inference;
import nota.inference.domain.model.Runtime;
import nota.inference.domain.repository.InferenceRepository;
import nota.inference.dto.message.InferenceRequestMessage;
import nota.inference.dto.response.ExecuteInferenceResponse;
import nota.inference.dto.response.InferenceHistoryItem;
import nota.inference.dto.response.InferenceResultResponse;
import nota.inference.exception.Error;
import nota.inference.exception.InferenceException;
import nota.inference.message.KafkaPublisher;
import nota.inference.util.FileUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InferenceService {
    private static final Set<String> VALID_FILE_EXTENSIONS = Set.of("jpg", "png");
    private final KafkaPublisher kafkaPublisher;
    private final InferenceRepository inferenceRepository;

    public ExecuteInferenceResponse executeInference(MultipartFile file, String runtime, String userId) throws IOException {
        FileUtil.getFileExtension(file)
                .map(String::toLowerCase)
                .filter(VALID_FILE_EXTENSIONS::contains)
                .orElseThrow(() -> new InferenceException(Error.NOT_ALLOWED_FILE));

        Inference saved = inferenceRepository.save(
                Inference.of(Runtime.valueOf(runtime.toUpperCase()), file.getOriginalFilename(), userId));

        String topic = getKafkaTopicFromRuntime(runtime);
        kafkaPublisher.sendMessage(topic, InferenceRequestMessage.of(saved.getId(), file));

        return ExecuteInferenceResponse.of(saved.getId());
    }

    private String getKafkaTopicFromRuntime(String runtime) {
        return runtime.toLowerCase() + "_inference_request";
    }

    @Transactional
    public void markInferenceAsComplete(Long id, String result) {
        Inference inference = inferenceRepository.findById(id)
                .filter(Inference::isProcessing)
                .orElseThrow(() -> new InferenceException(Error.INFERENCE_NOT_FOUND));

        inference.complete(result);
    }

    @Transactional
    public void markInferenceAsFail(Long id) {
        Inference inference = inferenceRepository.findById(id)
                .filter(Inference::isProcessing)
                .orElseThrow(() -> new InferenceException(Error.INFERENCE_NOT_FOUND));

        inference.fail();
    }


    public InferenceResultResponse getInferenceResultById(Long id) {
        Inference inference = inferenceRepository.findById(id)
                .orElseThrow(() -> new InferenceException(Error.INFERENCE_NOT_FOUND));
        if (inference.isFail())
            throw new InferenceException(Error.INFERENCE_EXECUTION_FAILED);

        return InferenceResultResponse.from(inference);
    }

    public void deleteInference(Long id, String userId) {
        Inference inference = inferenceRepository.findById(id)
                .orElseThrow(() -> new InferenceException(Error.INFERENCE_NOT_FOUND));

        if (!inference.getUserId().equals(userId))
            throw new InferenceException(Error.NOT_INFERENCE_EXECUTOR);

        inferenceRepository.delete(inference);
    }

    public Slice<InferenceHistoryItem> getInferenceHistory(int page, int size, Optional<String> maybeUserId, Optional<String> maybeCreatedAt, Optional<String> maybeRuntime) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Inference> specification = getQueryFilter(maybeUserId, maybeCreatedAt, maybeRuntime);
        return inferenceRepository.findAll(specification, pageable)
                .map(InferenceHistoryItem::from);
    }

    public Specification<Inference> getQueryFilter(Optional<String> maybeUserId, Optional<String> maybeCreatedAt, Optional<String> maybeRuntime) {
        return (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            maybeUserId.ifPresent(userId -> predicates.add(criteriaBuilder.equal(root.get("userId"), userId)));
            maybeCreatedAt.ifPresent(createdAt -> {
                LocalDateTime startAt = LocalDateTime.parse(createdAt).withMinute(0).withSecond(0).withNano(0);
                LocalDateTime endAt = startAt.plusHours(1);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startAt));
                predicates.add(criteriaBuilder.lessThan(root.get("createdAt"), endAt));
            });
            maybeRuntime.ifPresent(runtime -> predicates.add(criteriaBuilder.equal(root.get("runtime"), runtime)));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}

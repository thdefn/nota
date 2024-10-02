package nota.inference.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nota.inference.domain.model.Inference;
import nota.inference.domain.model.Runtime;
import nota.inference.domain.repository.InferenceRepository;
import nota.inference.dto.message.InferenceRequestMessage;
import nota.inference.dto.response.ExecuteInferenceResponse;
import nota.inference.dto.response.InferenceResultResponse;
import nota.inference.exception.Error;
import nota.inference.exception.InferenceException;
import nota.inference.message.KafkaPublisher;
import nota.inference.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InferenceService {
    private static final Set<String> VALID_FILE_EXTENSIONS = Set.of("jpg", "png");
    private final KafkaPublisher kafkaPublisher;
    private final InferenceRepository inferenceRepository;

    public ExecuteInferenceResponse executeInference(MultipartFile file, String runtime) throws IOException {
        FileUtil.getFileExtension(file)
                .map(String::toLowerCase)
                .filter(VALID_FILE_EXTENSIONS::contains)
                .orElseThrow(() -> new InferenceException(Error.NOT_ALLOWED_FILE));

        Inference saved = inferenceRepository.save(
                Inference.of(Runtime.valueOf(runtime.toUpperCase()), file.getOriginalFilename(), "mock"));

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
        if(inference.isFail())
            throw new InferenceException(Error.INFERENCE_EXECUTION_FAILED);

        return InferenceResultResponse.from(inference);
    }
}

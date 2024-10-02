package nota.inference.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nota.inference.dto.message.InferenceFailMessage;
import nota.inference.dto.message.InferenceSuccessMessage;
import nota.inference.service.InferenceService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final InferenceService inferenceService;

    @KafkaListener(topics = "inference_success")
    public void handleInferenceSuccess(InferenceSuccessMessage message) {
        log.debug("Received message: " + message);
        inferenceService.markInferenceAsComplete(message.id(), message.result());
    }

    @KafkaListener(topics = "inference_fail")
    public void handleInferenceFail(InferenceFailMessage message) {
        log.debug("Received message: " + message);
        inferenceService.markInferenceAsFail(message.id());
    }
}

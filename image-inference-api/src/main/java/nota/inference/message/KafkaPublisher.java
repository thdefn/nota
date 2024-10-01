package nota.inference.message;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(String topic, Object message) {
        kafkaTemplate.send(topic, message);
    }
}

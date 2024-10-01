package nota.inference.dto.message;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

public record InferenceRequestMessage(
        Long id,
        String fileContent
) {
    public static InferenceRequestMessage of(Long id, MultipartFile file) throws IOException {
        String fileContent = Base64.getEncoder().encodeToString(file.getBytes());
        return new InferenceRequestMessage(id, fileContent);
    }
}

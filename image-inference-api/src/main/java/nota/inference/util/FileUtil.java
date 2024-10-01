package nota.inference.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

public class FileUtil {

    public static Optional<String> getFileExtension(MultipartFile multipartFile) {
        String fileName = Objects.requireNonNull(multipartFile.getOriginalFilename());
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex < 0)
            return Optional.empty();
        return Optional.of(fileName.substring(dotIndex + 1));
    }

    private FileUtil() {
    }
}

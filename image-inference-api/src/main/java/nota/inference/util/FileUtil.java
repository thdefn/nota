package nota.inference.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public class FileUtil {

    public static Optional<String> getFileExtension(MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        if (fileName == null)
            return Optional.empty();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex < 0 || dotIndex == fileName.length() - 1)
            return Optional.empty();
        return Optional.of(fileName.substring(dotIndex + 1));
    }

    private FileUtil() {
    }
}

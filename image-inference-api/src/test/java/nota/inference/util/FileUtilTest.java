package nota.inference.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUtilTest {
    @Test
    void getFileExtension_validFileNameProvided() {
        //given
        MultipartFile file = new MockMultipartFile("image", "apple.jpg",
                MediaType.MULTIPART_FORM_DATA_VALUE, "abcde".getBytes());
        //when
        Optional<String> maybeFileExtension = FileUtil.getFileExtension(file);
        //then
        assertTrue(maybeFileExtension.isPresent());
        assertEquals("jpg", maybeFileExtension.get());
    }

    @Test
    void getFileExtension_fileNameWithoutExtension() {
        //given
        MultipartFile file = new MockMultipartFile("image", "apple.",
                MediaType.MULTIPART_FORM_DATA_VALUE, "abcde".getBytes());
        //when
        Optional<String> maybeFileExtension = FileUtil.getFileExtension(file);
        //then
        assertTrue(maybeFileExtension.isEmpty());
    }

    @Test
    void getFileExtension_fileNameIsNull() {
        //given
        MultipartFile file = new MockMultipartFile("image", null,
                MediaType.MULTIPART_FORM_DATA_VALUE, "abcde".getBytes());
        //when
        Optional<String> maybeFileExtension = FileUtil.getFileExtension(file);
        //then
        assertTrue(maybeFileExtension.isEmpty());
    }

    @Test
    void getFileExtension_fileNameIsEmpty() {
        //given
        MultipartFile file = new MockMultipartFile("image", "",
                MediaType.MULTIPART_FORM_DATA_VALUE, "abcde".getBytes());
        //when
        Optional<String> maybeFileExtension = FileUtil.getFileExtension(file);
        //then
        assertTrue(maybeFileExtension.isEmpty());
    }

}
package nota.inference.service;

import nota.inference.domain.model.Inference;
import nota.inference.domain.model.InferenceStatus;
import nota.inference.domain.model.Runtime;
import nota.inference.domain.repository.InferenceRepository;
import nota.inference.dto.response.ExecuteInferenceResponse;
import nota.inference.dto.response.InferenceResultResponse;
import nota.inference.exception.InferenceException;
import nota.inference.message.KafkaPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static nota.inference.exception.Error.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InferenceServiceTest {
    @Mock
    private InferenceRepository inferenceRepository;
    @Mock
    private KafkaPublisher kafkaPublisher;
    @InjectMocks
    private InferenceService inferenceService;

    @Test
    void executeInference_success() throws IOException {
        //given
        MultipartFile file = new MockMultipartFile("image", "apple.JPG",
                MediaType.MULTIPART_FORM_DATA_VALUE, "abcde".getBytes());
        String runtime = "oNNx";
        given(inferenceRepository.save(any())).willReturn(
                Inference.builder()
                        .id(1L)
                        .runtime(Runtime.ONNX)
                        .userId("mock")
                        .fileName("apple.jpg")
                        .status(InferenceStatus.PROCESSING)
                        .build());
        //when
        ExecuteInferenceResponse response = inferenceService.executeInference(file, runtime);
        //then
        ArgumentCaptor<Inference> inferenceArgumentCaptor = ArgumentCaptor.forClass(Inference.class);
        verify(inferenceRepository, times(1)).save(inferenceArgumentCaptor.capture());
        Inference saved = inferenceArgumentCaptor.getValue();
        assertEquals(InferenceStatus.PROCESSING, saved.getStatus());
        assertEquals(Runtime.ONNX, saved.getRuntime());
        assertEquals("apple.JPG", saved.getFileName());

        verify(kafkaPublisher, times(1)).sendMessage(eq("onnx_inference_request"), any());

        assertEquals(1L, response.id());
    }


    @Test
    void executeInference_fail_NOT_ALLOWED_FILE() throws IOException {
        //given
        MultipartFile file = new MockMultipartFile("image", "apple.webp",
                MediaType.MULTIPART_FORM_DATA_VALUE, "abcde".getBytes());
        String runtime = "oNNx";
        //when
        InferenceException e = assertThrows(InferenceException.class, () -> inferenceService.executeInference(file, runtime));
        //then
        assertEquals(NOT_ALLOWED_FILE, e.getError());
    }


    @Test
    void markInferenceAsComplete_success() {
        //given
        Inference inference = Inference.builder()
                .id(1L)
                .runtime(Runtime.ONNX)
                .userId("mock")
                .fileName("apple.jpg")
                .status(InferenceStatus.PROCESSING)
                .build();
        given(inferenceRepository.findById(any())).willReturn(Optional.of(inference));
        //when
        inferenceService.markInferenceAsComplete(1L, "apple");
        //then
        assertEquals("apple", inference.getResult());
        assertEquals(InferenceStatus.COMPLETE, inference.getStatus());
    }

    @Test
    void markInferenceAsComplete_fail_INFERENCE_NOT_FOUND() {
        //given
        Inference inference = Inference.builder()
                .id(1L)
                .runtime(Runtime.ONNX)
                .userId("mock")
                .fileName("apple.jpg")
                .result("apple")
                .status(InferenceStatus.COMPLETE)
                .build();
        given(inferenceRepository.findById(any())).willReturn(Optional.of(inference));
        //when
        InferenceException e = assertThrows(InferenceException.class, () -> inferenceService.markInferenceAsComplete(1L, "apple"));
        //then
        assertEquals(INFERENCE_NOT_FOUND, e.getError());
    }

    @Test
    void markInferenceAsFail_success() {
        //given
        Inference inference = Inference.builder()
                .id(1L)
                .runtime(Runtime.ONNX)
                .userId("mock")
                .fileName("apple.jpg")
                .status(InferenceStatus.PROCESSING)
                .build();
        given(inferenceRepository.findById(any())).willReturn(Optional.of(inference));
        //when
        inferenceService.markInferenceAsFail(1L);
        //then
        assertEquals(InferenceStatus.FAIL, inference.getStatus());
    }

    @Test
    void markInferenceAsFail_fail_INFERENCE_NOT_FOUND() {
        //given
        given(inferenceRepository.findById(any())).willReturn(Optional.empty());
        //when
        InferenceException e = assertThrows(InferenceException.class, () -> inferenceService.markInferenceAsFail(1L));
        //then
        assertEquals(INFERENCE_NOT_FOUND, e.getError());
    }


    @Test
    void getInferenceResultById_success_WhenInferenceIsComplete() {
        //given
        Inference inference = Inference.builder()
                .id(1L)
                .runtime(Runtime.ONNX)
                .userId("mock")
                .fileName("apple.jpg")
                .result("apple")
                .status(InferenceStatus.COMPLETE)
                .build();
        given(inferenceRepository.findById(any())).willReturn(Optional.of(inference));
        //when
        InferenceResultResponse response = inferenceService.getInferenceResultById(1L);
        //then
        assertEquals(1L, response.id());
        assertEquals("apple", response.result());
        assertFalse(response.isProcessing());
    }

    @Test
    void getInferenceResultById_success_WhenInferenceIsProcessing() {
        //given
        Inference inference = Inference.builder()
                .id(1L)
                .runtime(Runtime.ONNX)
                .userId("mock")
                .fileName("apple.jpg")
                .status(InferenceStatus.PROCESSING)
                .build();
        given(inferenceRepository.findById(any())).willReturn(Optional.of(inference));
        //when
        InferenceResultResponse response = inferenceService.getInferenceResultById(1L);
        //then
        assertEquals(1L, response.id());
        assertNull(response.result());
        assertTrue(response.isProcessing());
    }

    @Test
    void getInferenceResultById_fail_INFERENCE_EXECUTION_FAILED() {
        //given
        Inference inference = Inference.builder()
                .id(1L)
                .runtime(Runtime.ONNX)
                .userId("mock")
                .fileName("apple.jpg")
                .status(InferenceStatus.FAIL)
                .build();
        given(inferenceRepository.findById(any())).willReturn(Optional.of(inference));
        //when
        InferenceException e = assertThrows(InferenceException.class, () -> inferenceService.getInferenceResultById(1L));
        //then
        assertEquals(INFERENCE_EXECUTION_FAILED, e.getError());
    }

}
package nota.inference.service;

import jakarta.persistence.criteria.*;
import nota.inference.domain.model.Inference;
import nota.inference.domain.model.InferenceStatus;
import nota.inference.domain.model.Runtime;
import nota.inference.domain.repository.InferenceRepository;
import nota.inference.dto.response.ExecuteInferenceResponse;
import nota.inference.dto.response.InferenceHistoryItem;
import nota.inference.dto.response.InferenceResultResponse;
import nota.inference.exception.InferenceException;
import nota.inference.message.KafkaPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static nota.inference.exception.Error.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
        String userId = "mock";
        given(inferenceRepository.save(any())).willReturn(
                Inference.builder()
                        .id(1L)
                        .runtime(Runtime.ONNX)
                        .userId("mock")
                        .fileName("apple.jpg")
                        .status(InferenceStatus.PROCESSING)
                        .build());
        //when
        ExecuteInferenceResponse response = inferenceService.executeInference(file, runtime, userId);
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
        String userId = "mock";
        //when
        InferenceException e = assertThrows(InferenceException.class, () -> inferenceService.executeInference(file, runtime, userId));
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


    @Test
    void deleteInference_success() {
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
        inferenceService.deleteInference(1L, "mock");
        //then
        verify(inferenceRepository, times(1)).delete((Inference) any());
    }

    @Test
    void deleteInference_fail_INFERENCE_NOT_FOUND() {
        //given
        given(inferenceRepository.findById(any())).willReturn(Optional.empty());
        //when
        InferenceException e = assertThrows(InferenceException.class, () -> inferenceService.deleteInference(1L, "mock"));
        //then
        assertEquals(INFERENCE_NOT_FOUND, e.getError());
    }

    @Test
    void deleteInference_fail_NOT_INFERENCE_EXECUTOR() {
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
        InferenceException e = assertThrows(InferenceException.class, () -> inferenceService.deleteInference(1L, "abcde"));
        //then
        assertEquals(NOT_INFERENCE_EXECUTOR, e.getError());
    }


    @Test
    void getInferenceHistory_success() {
        //given
        int page = 0;
        int size = 10;
        String userId = "mock";
        String createdAt = "2024-10-02T14:23:00";
        String runtime = "onnx";
        Pageable pageable = PageRequest.of(page, size);
        given(inferenceRepository.findAll(any(Specification.class), eq(pageable)))
                .willReturn(new PageImpl<>(
                        List.of(
                                Inference.builder()
                                        .id(1L)
                                        .runtime(Runtime.ONNX)
                                        .userId("mock")
                                        .fileName("apple.jpg")
                                        .result("apple")
                                        .status(InferenceStatus.COMPLETE)
                                        .build(),
                                Inference.builder()
                                        .id(2L)
                                        .runtime(Runtime.ONNX)
                                        .userId("mock")
                                        .fileName("dog.jpg")
                                        .result("dog")
                                        .status(InferenceStatus.PROCESSING)
                                        .build()
                        )
                ));
        //when
        Slice<InferenceHistoryItem> result = inferenceService.getInferenceHistory(
                page, size, Optional.of(userId), Optional.of(createdAt), Optional.of(runtime));
        //then
        List<InferenceHistoryItem> items = result.getContent();
        assertEquals(1L, items.getFirst().id());
        assertEquals("ONNX", items.getFirst().runtime());
        assertEquals("apple.jpg", items.getFirst().fileName());
        assertEquals("mock", items.getFirst().userId());
        assertEquals("COMPLETE", items.getFirst().status());

        assertEquals(2L, items.getLast().id());
        assertEquals("ONNX", items.getLast().runtime());
        assertEquals("dog.jpg", items.getLast().fileName());
        assertEquals("mock", items.getLast().userId());
        assertEquals("PROCESSING", items.getLast().status());
    }




}
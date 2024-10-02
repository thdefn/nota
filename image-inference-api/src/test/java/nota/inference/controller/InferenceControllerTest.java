package nota.inference.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nota.inference.domain.model.Inference;
import nota.inference.domain.model.InferenceStatus;
import nota.inference.domain.model.Runtime;
import nota.inference.dto.response.InferenceHistoryItem;
import nota.inference.dto.response.InferenceResultResponse;
import nota.inference.service.InferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InferenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class InferenceControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private InferenceService inferenceService;

    @Test
    void executeInference_success() throws Exception {
        //given
        //when
        ResultActions actions = mockMvc.perform(multipart("/inferences", HttpMethod.POST)
                .file(new MockMultipartFile("image", "apple.jpg",
                        MediaType.MULTIPART_FORM_DATA_VALUE, "abcde".getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("runtime", "ONNX")
        );
        //then
        actions.andDo(print())
                .andExpect(status().isAccepted());
    }


    @Test
    void executeInference_success_caseInsensitive() throws Exception {
        //given
        //when
        ResultActions actions = mockMvc.perform(multipart("/inferences", HttpMethod.POST)
                .file(new MockMultipartFile("image", "apple.jpg",
                        MediaType.MULTIPART_FORM_DATA_VALUE, "abcde".getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("runtime", "Tflite")
        );
        //then
        actions.andDo(print())
                .andExpect(status().isAccepted());
    }


    @Test
    void executeInference_fail_whenRuntimeIsNotProvided() throws Exception {
        //given
        //when
        ResultActions actions = mockMvc.perform(multipart("/inferences", HttpMethod.POST)
                .file(new MockMultipartFile("image", "apple.jpg",
                        MediaType.MULTIPART_FORM_DATA_VALUE, "abcde".getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );
        //then
        actions.andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void executeInference_fail_whenInvalidRuntimeIsProvided() throws Exception {
        //given
        //when
        ResultActions actions = mockMvc.perform(multipart("/inferences", HttpMethod.POST)
                .file(new MockMultipartFile("image", "apple.jpg",
                        MediaType.MULTIPART_FORM_DATA_VALUE, "abcde".getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("runtime", "abcdefg")
        );
        //then
        actions.andDo(print())
                .andExpectAll(status().isBadRequest(),
                        jsonPath("$.message").value("허용되지 않는 runtime 입니다."));
    }

    @Test
    void getInferenceResultById_success_WhenInferenceIsProcessing() throws Exception {
        //given
        given(inferenceService.getInferenceResultById(anyLong()))
                .willReturn(InferenceResultResponse.builder()
                        .isProcessing(true)
                        .id(1L).build());
        //when
        ResultActions actions = mockMvc.perform(get("/inferences/{inferenceId}", 1L)
                .contentType(MediaType.APPLICATION_JSON));
        //then
        actions.andDo(print())
                .andExpectAll(status().isAccepted(),
                        jsonPath("$.isProcessing").value(true),
                        jsonPath("$.id").value(1L),
                        jsonPath("$.result").doesNotExist());
    }


    @Test
    void getInferenceResultById_success_WhenInferenceIsComplete() throws Exception {
        //given
        given(inferenceService.getInferenceResultById(anyLong()))
                .willReturn(InferenceResultResponse.builder()
                        .isProcessing(false)
                        .id(1L)
                        .result("apple")
                        .build());
        //when
        ResultActions actions = mockMvc.perform(get("/inferences/{inferenceId}", 1L)
                .contentType(MediaType.APPLICATION_JSON));
        //then
        actions.andDo(print())
                .andExpectAll(status().isOk(),
                        jsonPath("$.isProcessing").value(false),
                        jsonPath("$.id").value(1L),
                        jsonPath("$.result").value("apple"));
    }

    @Test
    void deleteInferenceById_success() throws Exception {
        //given
        //when
        ResultActions actions = mockMvc.perform(delete("/inferences/{inferenceId}", 1L)
                .contentType(MediaType.APPLICATION_JSON));
        //then
        actions.andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getInferenceHistory_success() throws Exception {
        //given
        given(inferenceService.getInferenceHistory(anyInt(), anyInt(), any(), any(), any()))
                .willReturn(new PageImpl<>(
                        List.of(
                                InferenceHistoryItem.builder()
                                        .id(1L)
                                        .fileName("apple.png")
                                        .runtime("ONNX")
                                        .status("COMPLETE")
                                        .result("apple")
                                        .userId("mock")
                                        .createdAt(LocalDateTime.parse("2024-10-02T14:02:00"))
                                        .build(),
                                InferenceHistoryItem.builder()
                                        .id(2L)
                                        .fileName("dog.png")
                                        .runtime("ONNX")
                                        .status("PROCESSING")
                                        .userId("mock")
                                        .createdAt(LocalDateTime.parse("2024-10-02T14:21:00"))
                                        .build()
                        )
                ));
        //when
        ResultActions actions = mockMvc.perform(get("/inferences")
                .param("page", "0")
                .param("size", "20")
                .param("runtime", "onnx")
                .param("createdAt", "2024-10-02T14:00:00")
                .param("userId", "mock")
                .contentType(MediaType.APPLICATION_JSON));
        //then
        actions.andDo(print())
                .andExpectAll(status().isOk(),
                        jsonPath("$.content.[0].id").value(1L),
                        jsonPath("$.content.[0].fileName").value("apple.png"),
                        jsonPath("$.content.[0].runtime").value("ONNX"),
                        jsonPath("$.content.[0].status").value("COMPLETE"),
                        jsonPath("$.content.[0].result").value("apple"),
                        jsonPath("$.content.[0].userId").value("mock"),
                        jsonPath("$.content.[0].createdAt").value("2024-10-02T14:02:00"),

                        jsonPath("$.content.[1].id").value(2L),
                        jsonPath("$.content.[1].fileName").value("dog.png"),
                        jsonPath("$.content.[1].runtime").value("ONNX"),
                        jsonPath("$.content.[1].status").value("PROCESSING"),
                        jsonPath("$.content.[1].result").isEmpty(),
                        jsonPath("$.content.[1].userId").value("mock"),
                        jsonPath("$.content.[1].createdAt").value("2024-10-02T14:21:00")
                );
    }

    @Test
    void getInferenceHistory_success_WhenHaveNoParam() throws Exception {
        //given
        given(inferenceService.getInferenceHistory(anyInt(), anyInt(), any(), any(), any()))
                .willReturn(new PageImpl<>(
                        List.of(
                                InferenceHistoryItem.builder()
                                        .id(1L)
                                        .fileName("apple.png")
                                        .runtime("ONNX")
                                        .status("COMPLETE")
                                        .result("apple")
                                        .userId("mock")
                                        .createdAt(LocalDateTime.parse("2024-10-02T14:02:00"))
                                        .build(),
                                InferenceHistoryItem.builder()
                                        .id(2L)
                                        .fileName("dog.png")
                                        .runtime("ONNX")
                                        .status("PROCESSING")
                                        .userId("mock")
                                        .createdAt(LocalDateTime.parse("2024-10-02T14:21:00"))
                                        .build()
                        )
                ));
        //when
        ResultActions actions = mockMvc.perform(get("/inferences")
                .contentType(MediaType.APPLICATION_JSON));
        //then
        actions.andDo(print())
                .andExpectAll(status().isOk(),
                        jsonPath("$.content.[0].id").value(1L),
                        jsonPath("$.content.[0].fileName").value("apple.png"),
                        jsonPath("$.content.[0].runtime").value("ONNX"),
                        jsonPath("$.content.[0].status").value("COMPLETE"),
                        jsonPath("$.content.[0].result").value("apple"),
                        jsonPath("$.content.[0].userId").value("mock"),
                        jsonPath("$.content.[0].createdAt").value("2024-10-02T14:02:00"),

                        jsonPath("$.content.[1].id").value(2L),
                        jsonPath("$.content.[1].fileName").value("dog.png"),
                        jsonPath("$.content.[1].runtime").value("ONNX"),
                        jsonPath("$.content.[1].status").value("PROCESSING"),
                        jsonPath("$.content.[1].result").isEmpty(),
                        jsonPath("$.content.[1].userId").value("mock"),
                        jsonPath("$.content.[1].createdAt").value("2024-10-02T14:21:00")
                );
    }

    @Test
    void getInferenceHistory_fail_whenInvalidRuntimeIsProvided() throws Exception {
        //given
        //when
        ResultActions actions = mockMvc.perform(get("/inferences")
                .param("runtime", "abcdefg")
                .contentType(MediaType.APPLICATION_JSON));
        //then
        actions.andDo(print())
                .andExpectAll(status().isBadRequest(),
                        jsonPath("$.message").value("허용되지 않는 runtime 입니다."));
    }

    @Test
    void getInferenceHistory_fail_whenInvalidCreatedAtIsProvided() throws Exception {
        //given
        //when
        ResultActions actions = mockMvc.perform(get("/inferences")
                .param("createdAt", "2024-10-02 14:00:00")
                .contentType(MediaType.APPLICATION_JSON));
        //then
        actions.andDo(print())
                .andExpectAll(status().isBadRequest(),
                        jsonPath("$.message").value("yyyy-MM-ddTHH:00:00 형식으로 입력해주세요."));
    }


}
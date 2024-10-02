package nota.inference.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nota.inference.dto.response.InferenceResultResponse;
import nota.inference.service.InferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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


}
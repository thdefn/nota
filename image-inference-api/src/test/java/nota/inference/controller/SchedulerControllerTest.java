package nota.inference.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nota.inference.service.SchedulerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SchedulerController.class)
@AutoConfigureMockMvc(addFilters = false)
class SchedulerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private SchedulerService schedulerService;

    @Test
    void updateInferenceHistoryDeleteSchedule_success() throws Exception {
        //given
        String cronExpression = "0 15 21 * * *";
        //when
        ResultActions actions = mockMvc.perform(put("/schedule/inference-history")
                .param("cronExpression", cronExpression)
                .contentType(MediaType.APPLICATION_JSON));
        //then
        actions.andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void updateInferenceHistoryDeleteSchedule_fail_WhenInvalidCronJobIsProvided() throws Exception {
        //given
        String cronExpression = "0 15 21 * *";
        //when
        ResultActions actions = mockMvc.perform(put("/schedule/inference-history")
                .param("cronExpression", cronExpression)
                .contentType(MediaType.APPLICATION_JSON));
        //then
        actions.andDo(print())
                .andExpectAll(status().isBadRequest(),
                        jsonPath("$.message").value( "cron 표현식은 '초 분 시 일 월 요일' 형식이어야 합니다. 올바른 형식인지 확인해주세요."));
    }
}
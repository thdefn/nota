package nota.inference.controller;

import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nota.inference.service.SchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/schedule")
public class SchedulerController {
    private final SchedulerService schedulerService;
    private static final String CRON_REGEX = "^([0-5]?[0-9])\\s([0-5]?[0-9])\\s([0-1]?[0-9]|2[0-3])\\s(\\*|[1-9]|[12][0-9]|3[01])\\s(\\*|[1-9]|1[0-2])\\s(\\*|[0-6])$";

    @PutMapping("/inference-history")
    public ResponseEntity<Void> updateInferenceHistoryDeleteSchedule(@RequestParam @Pattern(regexp = CRON_REGEX, message = "cron 표현식은 '초 분 시 일 월 요일' 형식이어야 합니다. 올바른 형식인지 확인해주세요.") String cronExpression) {
        schedulerService.updateInferenceHistoryDeleteSchedule(cronExpression);
        return ResponseEntity.ok().build();
    }
}

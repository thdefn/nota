package nota.inference.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class SchedulerService {
    private final TaskScheduler taskScheduler;
    private final InferenceService inferenceService;

    private ScheduledFuture<?> futureTask;

    @PostConstruct
    private void initializeDefaultSchedule() {
        updateInferenceHistoryDeleteSchedule("0 0 12 * * *");
    }

    public void updateInferenceHistoryDeleteSchedule(String cronExpression) {
        if (futureTask != null && !futureTask.isCancelled()) {
            futureTask.cancel(false);
        }

        futureTask = taskScheduler.schedule(this::deleteInferenceHistory, new CronTrigger(cronExpression));
    }

    private void deleteInferenceHistory() {
        inferenceService.deleteAllInferenceHistory();
    }
}

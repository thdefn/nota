package nota.inference.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class SchedulerServiceTest {
    @Mock
    private TaskScheduler taskScheduler;
    @Mock
    private InferenceService inferenceService;
    @InjectMocks
    private SchedulerService schedulerService;

    private ScheduledFuture<?> futureTask;
    @BeforeEach
    void setUp() throws IllegalAccessException, NoSuchFieldException {
        MockitoAnnotations.openMocks(this);
        futureTask = Mockito.mock(ScheduledFuture.class);

        Field futureTaskField = SchedulerService.class.getDeclaredField("futureTask");
        futureTaskField.setAccessible(true);
        futureTaskField.set(schedulerService, futureTask);
    }

    @Test
    void updateInferenceHistoryDeleteSchedule_WhenFutureTaskExists() {
        // given
        String cronExpression = "0 0 12 * * *";
        given(futureTask.isCancelled()).willReturn(false);
        // when
        schedulerService.updateInferenceHistoryDeleteSchedule(cronExpression);
        // then
        verify(futureTask, times(1)).cancel(false);
        verify(taskScheduler, times(1)).schedule(any(Runnable.class), eq(new CronTrigger(cronExpression)));
    }
}
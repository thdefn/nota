package nota.inference.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nota.inference.domain.model.Runtime;
import nota.inference.dto.response.ExecuteInferenceResponse;
import nota.inference.dto.response.InferenceResultResponse;
import nota.inference.service.InferenceService;
import nota.inference.util.validator.EnumValue;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/inferences")
public class InferenceController {
    private final InferenceService inferenceService;

    @PostMapping
    public ResponseEntity<ExecuteInferenceResponse> executeInference(
            @RequestParam(value = "runtime") @EnumValue(enumClass = Runtime.class, message = "허용되지 않는 runtime 입니다.") String runtime,
            @RequestPart(value = "image") MultipartFile file,
            @RequestHeader(defaultValue = "mock") String userId) throws IOException {
        return ResponseEntity.accepted().body(inferenceService.executeInference(file, runtime, userId));
    }

    @GetMapping("/{inferenceId}")
    public ResponseEntity<InferenceResultResponse> getInferenceResultById(@PathVariable Long inferenceId) {
        InferenceResultResponse response = inferenceService.getInferenceResultById(inferenceId);
        if (response.isProcessing())
            return ResponseEntity.accepted().body(response);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{inferenceId}")
    public ResponseEntity<Void> deleteInference(@PathVariable Long inferenceId,
                                                @RequestHeader(defaultValue = "mock") String userId) {
        inferenceService.deleteInference(inferenceId, userId);
        return ResponseEntity.ok().build();
    }

}

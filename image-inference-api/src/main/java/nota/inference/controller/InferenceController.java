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
            @RequestParam(value = "runtime") @EnumValue(enumClass = Runtime.class) String runtime,
            @RequestPart(value = "image") MultipartFile file) throws IOException {
        return ResponseEntity.accepted().body(inferenceService.executeInference(file, runtime));
    }

    @GetMapping("/{inferenceId}")
    public ResponseEntity<InferenceResultResponse> getInferenceResultById(@PathVariable Long inferenceId) {
        InferenceResultResponse response = inferenceService.getInferenceResultById(inferenceId);
        if (response.isProcessing())
            return ResponseEntity.accepted().body(response);
        return ResponseEntity.ok(response);
    }


}

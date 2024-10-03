package nota.inference.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nota.inference.domain.model.Runtime;
import nota.inference.dto.response.ExecuteInferenceResponse;
import nota.inference.dto.response.InferenceHistoryItem;
import nota.inference.dto.response.InferenceResultResponse;
import nota.inference.exception.ExceptionResponse;
import nota.inference.service.InferenceService;
import nota.inference.util.validator.DateTime;
import nota.inference.util.validator.EnumValue;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Inference", description = "APIs for inference and retrieving history")
@RequestMapping("/inferences")
public class InferenceController {
    private final InferenceService inferenceService;

    @Operation(summary = "execute inference")
    @ApiResponses({
            @ApiResponse(responseCode = "202", content = {@Content(schema = @Schema(implementation = ExecuteInferenceResponse.class))}),
            @ApiResponse(responseCode = "400", description = "request field error", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExecuteInferenceResponse> executeInference(
            @Parameter(name = "runtime", description = "Parameter must be 'onnx' or 'tflite'", example = "onnx")
            @RequestParam @EnumValue(enumClass = Runtime.class, message = "허용되지 않는 runtime 입니다.") String runtime,
            @RequestPart(value = "image") MultipartFile file,
            @RequestHeader(defaultValue = "mock") String userId) throws IOException {
        return ResponseEntity.accepted().body(inferenceService.executeInference(file, runtime, userId));
    }

    @Operation(summary = "retrieve inference result by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "inference is complete", content = {@Content(schema = @Schema(implementation = InferenceResultResponse.class))}),
            @ApiResponse(responseCode = "202", description = "inference is processing", content = {@Content(schema = @Schema(implementation = InferenceResultResponse.class))}),
            @ApiResponse(responseCode = "400", description = "request field error", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "404", description = "invalid inferenceId", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "502", description = "error occurred while executing inference", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @GetMapping("/{inferenceId}")
    public ResponseEntity<InferenceResultResponse> getInferenceResultById(@PathVariable Long inferenceId) {
        InferenceResultResponse response = inferenceService.getInferenceResultById(inferenceId);
        if (response.isProcessing())
            return ResponseEntity.accepted().body(response);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "delete inference history")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "request field error", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "404", description = "invalid inferenceId", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "403", description = "no permission to delete", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @DeleteMapping("/{inferenceId}")
    public ResponseEntity<Void> deleteInference(@PathVariable Long inferenceId,
                                                @RequestHeader(defaultValue = "mock") String userId) {
        inferenceService.deleteInference(inferenceId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "retrieve inference histories")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = InferenceHistoryItem.class)))}),
            @ApiResponse(responseCode = "400", description = "request field error", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @GetMapping
    public ResponseEntity<Slice<InferenceHistoryItem>> getInferenceHistory(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(name = "runtime", description = "Parameter must be 'onnx' or 'tflite'", example = "onnx")
            @RequestParam(required = false) @EnumValue(enumClass = Runtime.class, message = "허용되지 않는 runtime 입니다.", nullable = true) String runtime,
            @RequestParam(required = false) String userId,
            @Parameter(name = "createdAt", description = "Parameter must be 'yyyy-MM-ddTHH:00:00'", example = "2024-10-03T23:00:00")
            @RequestParam(required = false) @DateTime(nullable = true, message = "yyyy-MM-ddTHH:00:00 형식으로 입력해주세요.") String createdAt) {
        Slice<InferenceHistoryItem> response = inferenceService.getInferenceHistory(page, size,
                Optional.ofNullable(userId),
                Optional.ofNullable(createdAt),
                Optional.ofNullable(runtime));
        return ResponseEntity.ok(response);
    }

}

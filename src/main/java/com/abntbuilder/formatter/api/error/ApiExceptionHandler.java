package com.abntbuilder.formatter.api.error;

import com.abntbuilder.formatter.output.docx.api.DocxWriterException;
import com.abntbuilder.formatter.shared.exception.MissingStyleRuleException;
import com.abntbuilder.formatter.shared.exception.SinglePageLayoutOverflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.abntbuilder.formatter.shared.exception.ComponentRuleTypeMismatchException;
import com.abntbuilder.formatter.shared.exception.MissingComponentRuleException;
import com.abntbuilder.formatter.shared.exception.MissingGeneratedDocxExportException;
import com.abntbuilder.formatter.shared.exception.MissingProfileException;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        List<ApiErrorResponse.ValidationError> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ApiErrorResponse.ValidationError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of("Validation failed.", errors));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MissingStyleRuleException.class,
            MissingComponentRuleException.class,
            ComponentRuleTypeMismatchException.class,
            SinglePageLayoutOverflowException.class,
            MissingProfileException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(exception.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of("Invalid request body."));
    }

    @ExceptionHandler(DocxWriterException.class)
    public ResponseEntity<ApiErrorResponse> handleDocxWriterException(DocxWriterException exception) {
        LOGGER.error("DOCX generation failed.", exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("Failed to generate DOCX document."));
    }

    @ExceptionHandler(MissingGeneratedDocxExportException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(exception.getMessage()));
    }
}

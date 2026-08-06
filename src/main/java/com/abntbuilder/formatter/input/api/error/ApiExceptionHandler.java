package com.abntbuilder.formatter.input.api.error;

import com.abntbuilder.formatter.engine.contract.DocxWriterException;
import com.abntbuilder.formatter.shared.exception.MissingStyleRuleException;
import com.abntbuilder.formatter.shared.exception.SinglePageLayoutOverflowException;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;
import com.abntbuilder.formatter.shared.exception.UnsupportedLayoutPolicyException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.abntbuilder.formatter.shared.exception.ComponentRuleTypeMismatchException;
import com.abntbuilder.formatter.shared.exception.MissingComponentRendererException;
import com.abntbuilder.formatter.shared.exception.MissingComponentRuleException;
import com.abntbuilder.formatter.shared.exception.MissingGeneratedDocxExportException;
import com.abntbuilder.formatter.shared.exception.MissingProfileException;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
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
                .body(ApiErrorResponse.of("VALIDATION_FAILED", "Um ou mais campos estão inválidos.", traceId(request), request.getRequestURI(), errors));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MissingStyleRuleException.class,
            MissingComponentRendererException.class,
            MissingComponentRuleException.class,
            ComponentRuleTypeMismatchException.class,
            SinglePageLayoutOverflowException.class,
            InvalidProfileStructureException.class,
            UnsupportedLayoutPolicyException.class,
            MissingProfileException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException exception, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of("FORMATTER_REQUEST_INVALID", exception.getMessage(), traceId(request), request.getRequestURI()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of("REQUEST_BODY_INVALID", "Corpo da requisição inválido.", traceId(request), request.getRequestURI()));
    }

    @ExceptionHandler(DocxWriterException.class)
    public ResponseEntity<ApiErrorResponse> handleDocxWriterException(DocxWriterException exception, HttpServletRequest request) {
        LOGGER.error("DOCX generation failed. path={}, traceId={}", request.getRequestURI(), traceId(request), exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("DOCX_GENERATION_FAILED", "Não foi possível gerar o documento DOCX.", traceId(request), request.getRequestURI()));
    }

    @ExceptionHandler(MissingGeneratedDocxExportException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException exception, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of("RESOURCE_NOT_FOUND", exception.getMessage(), traceId(request), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        LOGGER.error("Unexpected formatter error. path={}, traceId={}", request.getRequestURI(), traceId(request), exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        "INTERNAL_ERROR",
                        "Ocorreu um erro inesperado. Informe o código de rastreio ao suporte.",
                        traceId(request),
                        request.getRequestURI()
                ));
    }

    private String traceId(HttpServletRequest request) {
        Object traceIdAttribute = request.getAttribute(CORRELATION_ID_HEADER);
        if (traceIdAttribute instanceof String traceId && !traceId.isBlank()) {
            return traceId;
        }

        String traceId = request.getHeader(CORRELATION_ID_HEADER);
        return traceId == null || traceId.isBlank() ? null : traceId;
    }
}

package com.gymplatform.controller.error;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.beans.TypeMismatchException;
import com.gymplatform.service.BusinessConflictException;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private ResponseEntity<Object> error(HttpStatus status, String code, String title, String detail, WebRequest request) {
        String path = request.getDescription(false).replaceFirst("^uri=", "");
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(ProblemFactory.create(status, code, title, detail, path));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                   HttpStatusCode status, WebRequest request) {
        var response = error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Datos inválidos", "Revisá los campos enviados.", request);
        var problem = (org.springframework.http.ProblemDetail) response.getBody();
        problem.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(field -> Map.of("field", field.getField(), "message", field.getDefaultMessage() == null ? "Inválido" : field.getDefaultMessage()))
                .toList());
        return response;
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
                                                                    HttpStatusCode status, WebRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Solicitud inválida", "El cuerpo de la solicitud no es válido.", request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers,
                                                                           HttpStatusCode status, WebRequest request) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Método no admitido", "El método no está permitido para este recurso.", request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers,
                                                                      HttpStatusCode status, WebRequest request) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "Formato no admitido", "El formato enviado no está admitido.", request);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, HttpHeaders headers,
                                                                    HttpStatusCode status, WebRequest request) {
        return error(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Recurso no encontrado", "No se encontró el recurso solicitado.", request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> badCredentials(BadCredentialsException ex, WebRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Autenticación requerida", "No se pudo autenticar la solicitud.", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> invalidArgument(IllegalArgumentException ex, WebRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Solicitud inválida", "Revisá los datos enviados.", request);
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<Object> notFound(java.util.NoSuchElementException ex, WebRequest request) {
        return error(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Recurso no encontrado", "No se encontró el recurso solicitado.", request);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class, BusinessConflictException.class})
    public ResponseEntity<Object> conflict(RuntimeException ex, WebRequest request) {
        return error(HttpStatus.CONFLICT, "CONFLICT", "Conflicto", "La operación entra en conflicto con datos existentes.", request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                         HttpStatusCode status, WebRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Solicitud inválida", "Revisá los datos enviados.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> unexpected(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(ProblemFactory.create(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Error interno",
                        "No se pudo completar la solicitud.", request.getRequestURI()));
    }
}

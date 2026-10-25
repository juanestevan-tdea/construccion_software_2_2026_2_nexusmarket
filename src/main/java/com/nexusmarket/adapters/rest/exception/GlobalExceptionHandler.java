package com.nexusmarket.adapters.rest.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexusmarket.adapters.rest.dtos.responses.ErrorResponse;
import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.CategoryHasProductsException;
import com.nexusmarket.domain.exceptions.DamagedInventoryException;
import com.nexusmarket.domain.exceptions.DomainException;
import com.nexusmarket.domain.exceptions.DuplicateResourceException;
import com.nexusmarket.domain.exceptions.InsufficientStockException;
import com.nexusmarket.domain.exceptions.InvalidQuantityException;
import com.nexusmarket.domain.exceptions.InvalidRefundAmountException;
import com.nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import com.nexusmarket.domain.exceptions.InvoiceAlreadyExistsException;
import com.nexusmarket.domain.exceptions.InvoiceNotPayableException;
import com.nexusmarket.domain.exceptions.InvoiceNotRefundableException;
import com.nexusmarket.domain.exceptions.PaymentGatewayException;
import com.nexusmarket.domain.exceptions.ProductNotAvailableException;
import com.nexusmarket.domain.exceptions.RefundAmountExceededException;
import com.nexusmarket.domain.exceptions.RefundNotAllowedException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.exceptions.ReturnAlreadyProcessedException;
import com.nexusmarket.domain.exceptions.ReturnNotAllowedException;
import com.nexusmarket.domain.exceptions.ReturnWindowExpiredException;
import com.nexusmarket.domain.exceptions.ShipmentAlreadyExistsException;
import com.nexusmarket.domain.exceptions.TrackingNotFoundException;
import com.nexusmarket.domain.exceptions.UserAlreadyExistsException;
import com.nexusmarket.domain.exceptions.WarehouseCapacityExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- Custom business exceptions ----------
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(
            BusinessRuleException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<ErrorResponse> handleInvalidQuantity(
            InvalidQuantityException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
            InsufficientStockException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(
            InvalidStatusTransitionException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request,
                List.of("Current status: " + ex.getCurrentStatus(),
                        "Target status: " + ex.getTargetStatus()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation at {} ({})", request.getRequestURI(),
                ex.getClass().getSimpleName());
        return build(HttpStatus.CONFLICT, "Database integrity constraint violation occurred", request, null);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLocking(
            OptimisticLockingFailureException ex, HttpServletRequest request) {
        log.warn("Optimistic locking failure at {} ({})", request.getRequestURI(),
                ex.getClass().getSimpleName());
        return build(HttpStatus.CONFLICT, "Resource was updated concurrently by another transaction", request, null);
    }

    @ExceptionHandler(ProductNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleProductNotAvailable(
            ProductNotAvailableException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(CategoryHasProductsException.class)
    public ResponseEntity<ErrorResponse> handleCategoryHasProducts(
            CategoryHasProductsException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DamagedInventoryException.class)
    public ResponseEntity<ErrorResponse> handleDamagedInventory(
            DamagedInventoryException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(WarehouseCapacityExceededException.class)
    public ResponseEntity<ErrorResponse> handleWarehouseCapacityExceeded(
            WarehouseCapacityExceededException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(InvoiceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceAlreadyExists(
            InvoiceAlreadyExistsException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(InvoiceNotPayableException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceNotPayable(
            InvoiceNotPayableException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(InvoiceNotRefundableException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceNotRefundable(
            InvoiceNotRefundableException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(InvalidRefundAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefundAmount(
            InvalidRefundAmountException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(RefundAmountExceededException.class)
    public ResponseEntity<ErrorResponse> handleRefundAmountExceeded(
            RefundAmountExceededException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(RefundNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleRefundNotAllowed(
            RefundNotAllowedException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(PaymentGatewayException.class)
    public ResponseEntity<ErrorResponse> handlePaymentGateway(
            PaymentGatewayException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ShipmentAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleShipmentAlreadyExists(
            ShipmentAlreadyExistsException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(TrackingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTrackingNotFound(
            TrackingNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ReturnWindowExpiredException.class)
    public ResponseEntity<ErrorResponse> handleReturnWindowExpired(
            ReturnWindowExpiredException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ReturnNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleReturnNotAllowed(
            ReturnNotAllowedException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ReturnAlreadyProcessedException.class)
    public ResponseEntity<ErrorResponse> handleReturnAlreadyProcessed(
            ReturnAlreadyProcessedException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    /**
     * Red de seguridad para excepciones de dominio sin mapeo explícito.
     *
     * <p>Spring elige el handler más específico por tipo, por lo que este método
     * solo se ejecuta cuando no existe un {@code @ExceptionHandler} dedicado.
     * El dominio nunca conoce HTTP; la traducción a 400 se decide aquí.</p>
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleUnmappedDomainException(
            DomainException ex, HttpServletRequest request) {
        log.warn("Unmapped domain exception at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    // ---------- Spring / framework exceptions ----------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request body", request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        return build(HttpStatus.BAD_REQUEST, message, request, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("Required parameter '%s' of type '%s' is missing", ex.getParameterName(), ex.getParameterType());
        return build(HttpStatus.BAD_REQUEST, message, request, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type", request, null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return build(HttpStatus.CONTENT_TOO_LARGE, "Maximum upload size exceeded", request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Resource not found", request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not supported", request, null);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "No handler found", request, null);
    }

    // ---------- Security exceptions ----------
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failure at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "You do not have permission to access this resource", request, null);
    }

    // ---------- Fallback ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred", request, null);
    }

    // ---------- Helper ----------
    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest request, List<String> details) {
        ErrorResponse body = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                details);
        return ResponseEntity.status(status).body(body);
    }
}

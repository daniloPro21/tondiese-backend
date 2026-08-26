package com.tondise.utils.config;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.tondise.utils.exception.*;
import com.tondise.utils.exception.ApiErrorResponse;
import com.tondise.utils.exception.BadRequestException;
import com.tondise.utils.exception.ConflictException;
import com.tondise.utils.exception.DatabaseException;
import com.tondise.utils.exception.ForbiddenException;
import com.tondise.utils.exception.ForeignKeyConstraintException;
import com.tondise.utils.exception.MissingApiKeyException;
import com.tondise.utils.exception.ResourceNotFoundException;
import com.tondise.utils.exception.TransactionAlreadyExecuteException;
import com.tondise.utils.exception.UnAuthorizeException;
import com.tondise.utils.exception.UniqueConstraintException;
import com.tondise.utils.security.keycloak_manager.exception.UsernameOrPasswordIsBadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Ajoute le détail technique au message renvoyé au client.
     * À laisser à {@code false} en production : le message d'une exception peut
     * contenir une requête SQL, un chemin de fichier ou un identifiant interne.
     */
    @Value("${tondise.errors.expose-details:false}")
    private boolean exposeDetails;

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<Object> handleNotFound(RuntimeException ex) {
        return buildResponseEntity(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({UnAuthorizeException.class, UsernameOrPasswordIsBadException.class})
    protected ResponseEntity<Object> handleUnAuthorize(RuntimeException ex) {
        return buildResponseEntity(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    protected ResponseEntity<Object> handleForbidden(RuntimeException ex) {
        return buildResponseEntity(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ConflictException.class)
    protected ResponseEntity<Object> handleConflict(RuntimeException ex) {
        return buildResponseEntity(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<Object> handleBad(RuntimeException ex) {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingApiKeyException.class)
    public ResponseEntity<Object> handleMissingApiKeyException(MissingApiKeyException ex) {
        return buildResponseEntity(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        return buildResponseEntity(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<Object> handleDatabaseException(DatabaseException ex) {
        return buildResponseEntity(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ForeignKeyConstraintException.class)
    public ResponseEntity<Object> handleForeignKeyConstraintException(ForeignKeyConstraintException ex) {
        return buildResponseEntity(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UniqueConstraintException.class)
    public ResponseEntity<Object> handleUniqueConstraintException(UniqueConstraintException ex) {
        return buildResponseEntity(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidBearerTokenException.class)
    public ResponseEntity<Object> handleInvalidBearerTokenException(InvalidBearerTokenException ex) {
        return buildResponseEntity(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TransactionAlreadyExecuteException.class)
    protected ResponseEntity<Object> handleTransactionAlreadyExecute(RuntimeException ex) {
        return buildResponseEntity(ex, HttpStatus.OK);
    }

    @ExceptionHandler(JwtValidationException.class)
    protected ResponseEntity<Object> handleJwtExpired(RuntimeException ex) {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }


    private ResponseEntity<Object> buildResponseEntity(Exception ex, HttpStatus status) {
        return buildResponseEntity(status, ex.getMessage());
    }

    private ResponseEntity<Object> buildResponseEntity(HttpStatusCode status, String message) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        ApiErrorResponse body = new ApiErrorResponse(
                status.value(),
                resolved != null ? resolved.getReasonPhrase() : "Error",
                message,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(body, status);
    }

    /**
     * Erreurs standard de Spring MVC : route inexistante, verbe non autorisé,
     * type de contenu refusé, {@code ResponseStatusException} levée par le code…
     *
     * <p>Sans ce gestionnaire, elles seraient interceptées par le filet de
     * sécurité ci-dessous et transformées en 500. Le statut d'origine est donc
     * conservé, et le motif ({@code getReason()}) est enfin transmis au client :
     * jusqu'ici il était perdu, {@code server.error.include-message} valant
     * {@code never} par défaut depuis Spring Boot 2.3.</p>
     */
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<Object> handleErrorResponse(ErrorResponseException ex) {
        String message = ex.getBody() != null && ex.getBody().getDetail() != null
                ? ex.getBody().getDetail()
                : ex.getMessage();
        return buildResponseEntity(ex.getStatusCode(), message);
    }

    /**
     * Filet de sécurité : toute exception non prévue ailleurs.
     *
     * <h2>Le problème qu'il corrige</h2>
     * Sans gestionnaire {@code Exception.class}, une exception non déclarée
     * (typiquement {@code RuntimeException}, {@code IllegalArgumentException},
     * {@code NoSuchElementException}) tombait dans la page d'erreur par défaut de
     * Spring Boot, qui renvoie :
     *
     * <pre>{"timestamp":"…","status":500,"error":"Internal Server Error","path":"…"}</pre>
     *
     * <b>sans aucun message</b> — le champ est masqué par défaut. Le front-end
     * n'avait donc rien à afficher à l'utilisateur.
     *
     * <h2>Ce qu'il renvoie</h2>
     * Un message stable accompagné d'une <b>référence</b>, journalisée avec la
     * pile complète. L'utilisateur voit un message exploitable, le support
     * retrouve l'incident exact par sa référence, et aucun détail technique
     * (requête SQL, chemin de fichier, nom de classe) ne fuit vers le client.
     *
     * <p>Le détail technique peut être renvoyé en plus dans les environnements de
     * développement, via {@code tondise.errors.expose-details=true}.</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex) {
        // Les exceptions Spring MVC portant déjà un statut gardent le leur :
        // sans ce test, un 404 ou un 405 deviendrait un 500.
        if (ex instanceof ErrorResponse errorResponse) {
            String detail = errorResponse.getBody() != null ? errorResponse.getBody().getDetail() : null;
            return buildResponseEntity(errorResponse.getStatusCode(),
                    detail != null ? detail : ex.getMessage());
        }

        String reference = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.error("💥 Erreur inattendue [réf. {}] : {}", reference, ex.getMessage(), ex);

        String message = "Une erreur interne est survenue. Merci de réessayer ; "
                + "si le problème persiste, communiquez la référence " + reference + " au support.";
        if (exposeDetails) {
            message += " [" + ex.getClass().getSimpleName() + " : " + ex.getMessage() + "]";
        }
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> Map.of(
                        "field", err.getField(),
                        "message", err.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "ValidationException");
        body.put("message", "Validation failed");
        body.put("details", errors);
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String field = "unknown";
        String message;

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            if (!ife.getPath().isEmpty()) {
                field = ife.getPath().get(ife.getPath().size() - 1).getFieldName();
            }
            String targetType = ife.getTargetType() != null ? ife.getTargetType().getSimpleName() : "unknown";
            message = "Invalid value '" + ife.getValue() + "' for field '" + field + "': expected a valid " + targetType;
        } else {
            message = "Malformed JSON request: " + ex.getMessage();
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "InvalidRequestBody");
        body.put("message", message);
        body.put("field", field);
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        String field = ex.getName(); // ← "method" directement
        String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";

        // Si le type attendu est un enum, on liste les valeurs valides
        String message;
        Class<?> requiredType = ex.getRequiredType();
        if (requiredType != null && requiredType.isEnum()) {
            String validValues = Arrays.stream(requiredType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            message = "Invalid value '" + invalidValue + "' for field '" + field
                    + "': expected one of [" + validValues + "]";
        } else {
            String typeName = requiredType != null ? requiredType.getSimpleName() : "unknown";
            message = "Invalid value '" + invalidValue + "' for field '" + field
                    + "': expected a valid " + typeName;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "TypeMismatchException");
        body.put("message", message);
        body.put("field", field);
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}

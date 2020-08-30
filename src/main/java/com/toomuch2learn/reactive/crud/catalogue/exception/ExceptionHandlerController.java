package com.toomuch2learn.reactive.crud.catalogue.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

@ControllerAdvice
@Slf4j
public class ExceptionHandlerController {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public Mono<ErrorResponse> onResourceFound(ResourceNotFoundException exception) {
        log.error("No resource found exception occurred: {} ", exception.getMessage());

        ErrorResponse response = new ErrorResponse();
        response.getErrors().add(
            new Error(
                ErrorCodes.ERR_RESOURCE_NOT_FOUND,
                "Resource not found",
                exception.getMessage()));

        return Mono.just(response);
    }

    /**
     * Handle request Validation failures
     * @param e
     * @return errorResponse
     */
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<ErrorResponse> onValidationException(WebExchangeBindException e) {
        log.error("Validation exception occurred", e);

        ErrorResponse error = new ErrorResponse();
        for (ObjectError objectError : e.getAllErrors()) {
            error.getErrors().add(
                new Error(
                    ErrorCodes.ERR_CONSTRAINT_CHECK_FAILED,
                    "Invalid Request",
                    objectError.getDefaultMessage()));
        }
        return Mono.just(error);
    }
}

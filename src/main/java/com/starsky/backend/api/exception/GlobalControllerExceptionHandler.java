package com.starsky.backend.api.exception;

import com.starsky.backend.api.user.InviteInvalidResponse;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Hidden
class GlobalControllerExceptionHandler {
    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "Data integrity violation - resource already exists.")
    @ExceptionHandler(DataIntegrityViolationException.class)
    public void handleConflict() {
        // Nothing to do
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInviteTokenException.class)
    @ResponseBody
    public InviteInvalidResponse handleInvalidToken(InvalidInviteTokenException ex) {
        return new InviteInvalidResponse(ex.getInviteToken().toString(), ex.getMessage());
    }

}


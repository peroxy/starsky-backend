package com.starsky.backend.api.exception;

import com.starsky.backend.api.schedule.InvalidDateRangeResponse;
import com.starsky.backend.api.schedule.ScheduleUnsolvableResponse;
import com.starsky.backend.api.user.InviteInvalidResponse;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;

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

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(DateRangeException.class)
    @ResponseBody
    public InvalidDateRangeResponse handleInvalidToken(DateRangeException ex) {
        return new InvalidDateRangeResponse(ex.getMessage());
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(ScheduleUnsolvableException.class)
    @ResponseBody
    public ScheduleUnsolvableResponse handleInvalidToken(ScheduleUnsolvableException ex) {
        return new ScheduleUnsolvableResponse(ex.getMessage());
    }


    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    @ResponseBody
    public ForbiddenResponse handleForbiddenAccess(ForbiddenException ex) {
        return new ForbiddenResponse(ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleBadListOfObjects() {
    }

}


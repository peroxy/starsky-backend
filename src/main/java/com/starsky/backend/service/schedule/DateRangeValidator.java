package com.starsky.backend.service.schedule;

import com.starsky.backend.api.exception.DateRangeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DateRangeValidator {

    private final Logger logger = LoggerFactory.getLogger(DateRangeValidator.class);

    public void validateDateInterval(Instant start, Instant end) throws DateRangeException {
        String error = null;
        if (start.isAfter(end)) {
            error = "Start timestamp (%s) occurs after end timestamp (%s). Start date has to occur before end date."
                    .formatted(start, end);
        } else if (start.equals(end)) {
            error = "Start timestamp (%s) equals end timestamp (%s). Start and end date cannot be equal."
                    .formatted(start, end);
        }

        if (error != null) {
            this.logger.warn(error);
            throw new DateRangeException(error);
        }
    }

}

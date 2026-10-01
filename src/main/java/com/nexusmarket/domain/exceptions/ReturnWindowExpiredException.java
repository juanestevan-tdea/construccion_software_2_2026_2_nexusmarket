package com.nexusmarket.domain.exceptions;

import java.time.LocalDateTime;

public class ReturnWindowExpiredException extends RuntimeException {

    public ReturnWindowExpiredException(String resourceName, String fieldName, LocalDateTime orderDate) {
        super(String.format("Return window has expired for %s with %s '%s'. Returns are only allowed within 30 days of the order date.",
                resourceName, fieldName, orderDate));
    }
}

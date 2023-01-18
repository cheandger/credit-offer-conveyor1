package com.shrek.exceptions;

public class ParametersValidationException extends CustomException{
    public ParametersValidationException(String message) {
        super(MessageCode.VALIDATION_EXCEPTION, "The params can't pass the validation :"+message);
    }
}

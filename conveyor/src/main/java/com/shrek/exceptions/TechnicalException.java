package com.shrek.exceptions;



public class TechnicalException extends CustomException {
   protected TechnicalException(int code, String message) {
            super(code, message);
    }
}

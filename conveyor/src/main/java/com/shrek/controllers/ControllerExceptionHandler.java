package com.shrek.controllers;
import com.shrek.exceptions.BusinessException;
import com.shrek.exceptions.TechnicalException;
import org.apache.http.impl.execchain.TunnelRefusedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ControllerExceptionHandler.class);//поподробнее распросить как работает

    //400 (Ошибка 400 bad request переводится как «плохой запрос». Она возникает тогда, когда браузер
    // пользователя отправляет некорректный запрос серверу, на котором находится сайт.)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request,
            HttpServletResponse response) {
        log.warn("Validation exception", ex);
        return ResponseEntity.badRequest().body("Неверный запрос " + ex.getMessage());
    }

    //500 ( это внутренняя проблема сервера. Она возникает, когда браузер
    // или другой клиент отправляет серверу запрос, а тот не может его обработать.)
    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<String> handleTechnicalException(
            TechnicalException ex,
            HttpServletRequest request,
            HttpServletResponse response) {
        log.error("Technical exception", ex);
        return ResponseEntity.internalServerError().body(ex.toString());
    }

    //422 (сервер понимает тип содержимого в теле запроса и
    // синтаксис запроса является правильным, но серверу не удалось обработать инструкции содержимого)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request,
            HttpServletResponse response) {
        log.error("Business exception", ex);
        return ResponseEntity.unprocessableEntity().body(ex.toString());
    }


    @Override
    @NotNull
    protected  ResponseEntity<Object> handleExceptionInternal(
           @NotNull Exception ex,
           @NotNull  Object body,
           @NotNull HttpHeaders headers,
           @NotNull  HttpStatus status,
           @NotNull WebRequest request) {
        log.error("Unhandled REST exception", ex);
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }
}

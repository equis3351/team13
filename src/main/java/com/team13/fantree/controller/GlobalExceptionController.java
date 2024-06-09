package com.team13.fantree.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.team13.fantree.exception.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionController extends ResponseEntityExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleAllException(Exception ex) {
		log.warn("handleAllException", ex);
		ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
		return handleExceptionInternal(errorCode, ex.getMessage());
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<Object> handleCustomException(NotFoundException e) {
		ErrorCode errorCode = e.getErrorCode();
		return handleExceptionInternal(errorCode);
	}

	@ExceptionHandler(MismatchException.class)
	public ResponseEntity<Object> handleCustomException(MismatchException e) {
		ErrorCode errorCode = e.getErrorCode();
		return handleExceptionInternal(errorCode);
	}

	@ExceptionHandler(DuplicatedException.class)
	public ResponseEntity<Object> handleCustomException(DuplicatedException e) {
		ErrorCode errorCode = e.getErrorCode();
		return handleExceptionInternal(errorCode);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException
																			  e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		log.warn("handleIllegalArgument", e);
		ErrorCode errorCode = CommonErrorCode.INVALID_PARAMETER;
		return handleExceptionInternal(e, errorCode);
	}


	private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode) {
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(makeErrorResponse(errorCode));
	}

	private ErrorResponse makeErrorResponse(ErrorCode errorCode) {
		return ErrorResponse.builder()
			.code(errorCode.name())
			.message(errorCode.getMessage())
			.httpStatus(errorCode.getHttpStatus())
			.build();
	}

	private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode, String message) {
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(makeErrorResponse(errorCode, message));
	}

	private ErrorResponse makeErrorResponse(ErrorCode errorCode, String message) {
		return ErrorResponse.builder()
			.code(errorCode.name())
			.message(message)
			.httpStatus(errorCode.getHttpStatus())
			.build();
	}

	private ResponseEntity<Object> handleExceptionInternal(BindException e, ErrorCode errorCode) {
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(makeErrorResponse(e, errorCode));
	}

	private ErrorResponse makeErrorResponse(BindException e, ErrorCode errorCode) {
		List<ErrorResponse.ValidationError> validationErrorList = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(ErrorResponse.ValidationError::of)
			.collect(Collectors.toList());

		return ErrorResponse.builder()
			.code(errorCode.name())
			.message(errorCode.getMessage())
			.httpStatus(errorCode.getHttpStatus())
			.errors(validationErrorList)
			.build();
	}
}

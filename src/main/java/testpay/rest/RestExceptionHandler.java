package testpay.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

  private static final Logger logger = LogManager.getLogger(RestExceptionHandler.class);

  @ResponseStatus(value=HttpStatus.BAD_REQUEST)
  @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class,
                     MethodArgumentNotValidException.class})
  public Map<String, String> handleHttpMessageNotReadableException(HttpServletRequest request, Exception ex) {
    return getErrorMessage(request, ex, "INVALID_REQUEST",
      "Request is not well-formatted, syntactically incorrect or violates schema");
  }

  @ResponseStatus(value = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public Map<String, String> handleHttpMediaTypeNotSupportedException(HttpServletRequest request, Exception ex) {
    return getErrorMessage(request, ex, "UNSUPPORTED_MEDIA_TYPE",
      "The server does not support the request payload media type");
  }

  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public Map<String, String> exceptionHandler(HttpServletRequest request, Exception ex) {
    return getErrorMessage(request, ex, "INTERNAL_SERVER_ERROR",
      "An internal server error has occurred");
  }

  private Map<String, String> getErrorMessage(HttpServletRequest request, Exception ex, String errorCode,
                                              String errorDescription) {
    logger.info("Exception {} on Request {}", ex.getMessage(), request.getRequestURL());
    Map<String, String> error = new HashMap<>(2);
    error.put("error", errorCode);
    error.put("error_description", errorDescription);
    return error;
  }

}


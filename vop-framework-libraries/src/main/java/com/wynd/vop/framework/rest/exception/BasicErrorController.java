package com.wynd.vop.framework.rest.exception;

import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.rest.provider.ProviderResponse;
import com.wynd.vop.framework.util.HttpHeadersUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Spring Boot automatically registers a BasicErrorController bean if you don’t
 * specify any custom implementation in the configuration. However, this default
 * controller needs to be configured for VOP platform. By default Spring Boot
 * maps /error to BasicErrorController which populates model with error
 * attributes and then returns 'error' as the view name to map application
 * defined error pages. To replace BasicErrorController with our own custom
 * controller which can map to '/error', we need to implement ErrorController
 * interface. Also @ApiIgnore added to ignored controller method parameter types
 * so that the framework does not generate swagger model or parameter
 * information for these specific types
 *

 *
 * @see org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController
 * @see org.springframework.boot.autoconfigure.web.ErrorProperties
 */
@RestController
public class BasicErrorController implements ErrorController {

	/** Constant for the logger for this class */
	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(BasicErrorController.class);

	/** The error attributes. */
	@Autowired(required = false)
	private ErrorAttributes errorAttributes;


	/**
	 * Handle error for @PatchMapping.
	 *
	 * @param webRequest
	 *            the web request
	 * @param response
	 *            the response
	 * @return the response entity
	 */
	@PatchMapping(value = "/error")
	public ResponseEntity<ProviderResponse> handleErrorPatch(WebRequest webRequest, HttpServletResponse response) {
		return handleError(webRequest, response);
	}

	/**
	 * Handle error for @DeleteMapping.
	 *
	 * @param webRequest
	 *            the web request
	 * @param response
	 *            the response
	 * @return the response entity
	 */
	@DeleteMapping(value = "/error")
	public ResponseEntity<ProviderResponse> handleErrorDelete(WebRequest webRequest, HttpServletResponse response) {
		return handleError(webRequest, response);
	}

	/**
	 * Handle error for @PutMapping.
	 *
	 * @param webRequest
	 *            the web request
	 * @param response
	 *            the response
	 * @return the response entity
	 */
	@PutMapping(value = "/error")
	public ResponseEntity<ProviderResponse> handleErrorPut(WebRequest webRequest, HttpServletResponse response) {
		return handleError(webRequest, response);
	}

	/**
	 * Handle error for @PostMapping.
	 *
	 * @param webRequest
	 *            the web request
	 * @param response
	 *            the response
	 * @return the response entity
	 */
	@PostMapping(value = "/error")
	public ResponseEntity<ProviderResponse> handleErrorPost(WebRequest webRequest, HttpServletResponse response) {
		return handleError(webRequest, response);
	}

	/**
	 * Handle error for @GetMapping.
	 *
	 * @param webRequest
	 *            the web request
	 * @param response
	 *            the response
	 * @return the response entity
	 */
	@GetMapping(value = "/error")
	public ResponseEntity<ProviderResponse> handleError(WebRequest webRequest, HttpServletResponse response) {
		HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		String message = MessageKeys.VOP_GLOBAL_GENERAL_EXCEPTION.getMessage();

		if (response != null) {
			try {
				httpStatus = HttpStatus.valueOf(response.getStatus());
			} catch (IllegalArgumentException e) {
				LOGGER.warn("IllegalArgumentException raised for the specified numeric value. Setting as Internal Error {}",e);
				// for invalid status code, set it to internal error
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			}
		}
		final Map<String, Object> error = getErrorAttributes(webRequest, false);
		if (error != null) {
			message = (String) error.getOrDefault("message", MessageKeys.VOP_GLOBAL_GENERAL_EXCEPTION.getMessage());
		}

		ProviderResponse providerResponse = new ProviderResponse();
		providerResponse.addMessage(MessageSeverity.ERROR, httpStatus.name(), message, httpStatus);

		return new ResponseEntity<>(providerResponse, HttpHeadersUtil.buildHttpHeadersForError(), httpStatus);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.boot.web.servlet.error.ErrorController#getErrorPath()
	 */

	/**
	 * Gets the error attributes.
	 *
	 * @param webRequest
	 *            the web request
	 * @param includeStackTrace
	 *            the include stack trace
	 * @return the error attributes
	 */
	private Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {


		if (errorAttributes != null) {
			return errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());

		} else {
			return null;
		}
	}


	public String getErrorPath() {
		return "/error";
	}


}
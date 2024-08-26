package com.wynd.vop.framework.rest.log;

import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.rest.log.model.ClientLogEntry;
import com.wynd.vop.framework.swagger.SwaggerResponseMessages;
import javax.validation.Valid;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.wynd.vop.framework.rest.log.model.ClientLogEntryList;


/**
 * Exposes an endpoint to allow client UI applications, specifically those
 * based on the vop-archetype-ui-react project, to persist client-side log messages
 * 
 * @since 4.0.5
 */
@RestController
@RequestMapping("/log")
public class LoggingController implements SwaggerResponseMessages {
	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(LoggingController.class);
	private static final String LOG_FORMAT = "{ timestamp: %s, origin: %s, logger: %s, trace_id: %s, span_id: %s, message: %s }";
	
	private static final String API_OPERATION_VALUE = "Persist an entry in the application logs";
	private static final String API_OPERATION_NOTES = "Allows a client UI application to persist log messages on the server.";
	
	
	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.TEXT_PLAIN_VALUE })
	@ApiOperation(value = API_OPERATION_VALUE,  notes = API_OPERATION_NOTES)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = MESSAGE_200),
			@ApiResponse(code = 400, message = MESSAGE_400),
			@ApiResponse(code = 500, message = MESSAGE_500) })
	public ResponseEntity<String> recordLogMessages(
			@RequestHeader(name = "Referer") String origin,
			@ApiParam(value = "logs") @Valid @RequestBody final ClientLogEntryList logs) {
		for (ClientLogEntry log : logs.getLogs()) {
			Level logLevel = Level.valueOf(StringUtils.upperCase(log.getLevel()));
			String formattedMsg = String.format(LOG_FORMAT, log.getTimestamp(), origin, log.getLogger(),
					log.getTraceId(), log.getSpanId(), log.getMessage(), log.getStacktrace());

			LOGGER.log(logLevel, formattedMsg);
		}
		
		return ResponseEntity.status(HttpStatus.OK).body("Log message recorded.");
	}
}

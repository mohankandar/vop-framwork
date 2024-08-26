package com.wynd.vop.framework.rest.log.model;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Format of log messages coming from a client UI application.
 * Specifically those utilizing vop-archetype-ui-react
 * 
 * @since 4.0.5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientLogEntry {
	@NotNull(message = "Log message required")
	private String message;
	
	@NotNull(message = "Log severity level required [trace, debug, info, warn, error]")
	private String level;
	
	@NotNull(message = "Timestamp of log message required")
	private String timestamp;
	
	private String logger;
	
	private String stacktrace;
	
	private String traceId;
	
	private String spanId;
}

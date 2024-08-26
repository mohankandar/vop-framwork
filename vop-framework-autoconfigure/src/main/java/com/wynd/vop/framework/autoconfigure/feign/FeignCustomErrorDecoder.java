package com.wynd.vop.framework.autoconfigure.feign;

import feign.Response;
import feign.codec.ErrorDecoder;
import com.wynd.vop.framework.exception.VopFeignRuntimeException;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import org.springframework.http.HttpStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * The Class FeignCustomErrorDecoder.
 */
public class FeignCustomErrorDecoder implements ErrorDecoder {

	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(FeignCustomErrorDecoder.class);

	private final ErrorDecoder defaultErrorDecoder = new Default();

	/*
	 * (non-Javadoc)
	 *
	 * @see feign.codec.ErrorDecoder#decode(java.lang.String, feign.Response)
	 */
	@Override
	public Exception decode(final String methodKey, final Response response) {
		if ((response.status() >= 400) && (response.status() <= 499)) {

			StringBuilder strBuffer = new StringBuilder();
			try {

				if (response.body() != null) {
					Reader inputReader = response.body().asReader(StandardCharsets.UTF_8);
					int data = inputReader.read();
					while (data != -1) {
						strBuffer.append((char) data);
						data = inputReader.read();
					}
				}

			} catch (IOException e) {
				LOGGER.debug("Could not read response body, trying alternate methods of error decoding as implemented "
						+ "in decode() method of feign.codec.ErrorDecoder.Default.Default()", e);
				return defaultErrorDecoder.decode(methodKey, response);
			}

			try {
				if (strBuffer.length() > 0) {
					JsonObject messageObjects = JsonParser.parseString(strBuffer.toString()).getAsJsonObject();
					JsonArray jsonarray = messageObjects.getAsJsonArray("messages");
					JsonObject messageObject = jsonarray.get(0).getAsJsonObject();
					MessageKeys key = MessageKeys.VOP_FEIGN_MESSAGE_RECEIVED;
					String[] params = new String[] { messageObject.get("key").getAsString(), messageObject.get("text").getAsString() };
					return new VopFeignRuntimeException(key,
							MessageSeverity.fromValue(messageObject.get("severity").getAsString()),
							HttpStatus.resolve(Integer.valueOf(messageObject.get("status").getAsString())), params);
				}
			} catch (Exception e) {
				LOGGER.debug(
						"Could not interpret response body, trying alternate methods of error decoding as implemented in decode() method of "
								+ "feign.codec.ErrorDecoder.Default.Default()",
						e);
				return defaultErrorDecoder.decode(methodKey, response);
			}

		}
		return defaultErrorDecoder.decode(methodKey, response);
	}

}
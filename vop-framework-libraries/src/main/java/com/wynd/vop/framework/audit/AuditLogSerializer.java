package com.wynd.vop.framework.audit;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wynd.vop.framework.audit.model.ResponseAuditData;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.audit.model.RequestAuditData;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.wynd.vop.framework.audit.BaseAsyncAudit.NUMBER_OF_BYTES_TO_LIMIT_AUDIT_LOGGED_OBJECT;

/**
 * The purpose of this class is to asynchronuously serialize an object to JSON
 * and then write it to the audit logs.
 *
 */
@Component
public class AuditLogSerializer implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(AuditLogSerializer.class);
	private static final VopLogger LOGGER_AUDIT_FILTER = VopLoggerFactory.getLogger(AuditSimpleBeanObjectFilter.class);

	private static final String[] EXCLUDE_FIELDS = new String[] { "logger", "LOGGER" };

	/** Jackson object mapper */
	ObjectMapper mapper = new ObjectMapper();

	/** Format for Java to XML conversions */
	@Value("${spring.jackson.date-format:yyyy-MM-dd'T'HH:mm:ss.SSSZ}")
	private String dateFormat = "GyMdkHmsSEDFwWahKzZ";

	public AuditLogSerializer() {
		super();
		setupObjectMapper();
	}

	/**
	 * Asynchronuously converts an object to JSON and then writes it to the
	 * audit logger.
	 * <p>
	 * <b>"Around" Advised by:</b>
	 * org.springframework.cloud.sleuth.instrument.async.TraceAsyncAspect.traceBackgroundThread(org.aspectj.lang.ProceedingJoinPoint)
	 *
	 * @param auditEventData
	 *            Data specific to the audit event
	 * @param auditData
	 *            The request and response audit data
	 */
	@Async
	public void asyncAuditRequestResponseData(final AuditEventData auditEventData, final AuditableData auditData,
			final Class<?> auditDataClass, final MessageSeverity messageSeverity, final Throwable t) {

		String auditDetails = null;
		if (auditData != null) {
			try {
				auditDetails = mapper.writeValueAsString(auditData);
			} catch (JsonProcessingException ex) {
				LOGGER.trace("Error occurred on ClassCast or JSON processing, calling custom toString()", ex);
				try {
					auditDetails = auditDataClass.cast(auditData).toString();
				} catch (Exception e) {
					LOGGER.trace(
							"Error occurred on ClassCast or Custom toString() processing, calling ReflectionToStringBuilder",
							e);
					auditDetails = ReflectionToStringBuilder.toString(auditData, ToStringStyle.JSON_STYLE, false, false,
							Object.class);
				}
			}
		}
		if (messageSeverity.equals(MessageSeverity.ERROR) || messageSeverity.equals(MessageSeverity.FATAL)) {
			AuditLogger.error(auditEventData, auditDetails, t);
		} else if (messageSeverity.equals(MessageSeverity.WARN)) {
			AuditLogger.warn(auditEventData, auditDetails);
		} else {
			AuditLogger.info(auditEventData, auditDetails);
		}
	}

	private void setupObjectMapper() {
		mapper.registerModule(new JavaTimeModule());
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(OffsetDateTime.class, new JsonSerializer<OffsetDateTime>() {
			@Override
			public void serialize(final OffsetDateTime offsetDateTime, final JsonGenerator jsonGenerator,
					final SerializerProvider serializerProvider) throws IOException {
				jsonGenerator.writeString(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(offsetDateTime));
			}
		});
		mapper.registerModule(simpleModule);
		mapper.registerModule(new JodaModule());
		final FilterProvider filters = new SimpleFilterProvider().addFilter("beanObjectFilter",
				new AuditSimpleBeanObjectFilter());
		mapper.setFilterProvider(filters);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.setDateFormat(new SimpleDateFormat(dateFormat, Locale.US));
		mapper.addMixIn(Object.class, AuditSimpleBeanObjectFilter.class);

		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		mapper.disable(SerializationFeature.FAIL_ON_SELF_REFERENCES);
		mapper.disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);

		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.disable(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
		mapper.disable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES);
		mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
	}

	/**
	 * The Class AuditSimpleBeanObjectFilter.
	 */
	@JsonFilter("beanObjectFilter")
	private static class AuditSimpleBeanObjectFilter extends SimpleBeanPropertyFilter {

		@Override
		public void serializeAsField(final Object pojo, final JsonGenerator jgen, final SerializerProvider provider,
				final PropertyWriter writer) throws Exception {

			if (!canSerializeField(pojo, writer)) {
				return;
			}

			super.serializeAsField(pojo, jgen, provider, writer);
		}

		/**
		 * Can serialize field?
		 *
		 * @param pojo the pojo
		 * @param writer the writer
		 * @return the boolean
		 * @throws IOException
		 */
		private static boolean canSerializeField(final Object pojo, final PropertyWriter writer) {

			boolean foundField = true;
			final String fieldName = writer.getFullName().getSimpleName();
			for (int i = 0; foundField && (i < EXCLUDE_FIELDS.length); i++) {
				foundField = !fieldName.equalsIgnoreCase(EXCLUDE_FIELDS[i]);
			}

			verifyRequestResponse(pojo, writer, fieldName);

			if (!foundField) {
				LOGGER_AUDIT_FILTER.trace("Field [{}] is excluded", fieldName);
				return false;
			}
			return true;
		}

		/**
		 * Verify request response.
		 *
		 * @param pojo the pojo
		 * @param writer the writer
		 * @param fieldName the field name
		 */
		private static void verifyRequestResponse(final Object pojo, final PropertyWriter writer, final String fieldName) {
			if (fieldName.equals("request") || fieldName.equals("response")) {
				LOGGER_AUDIT_FILTER.trace("==============================");
				LOGGER_AUDIT_FILTER.trace("Field Name: {}", fieldName);
				LOGGER_AUDIT_FILTER.trace("Type: {}", writer.getType());
				LOGGER_AUDIT_FILTER.trace("Type Class: {}", writer.getType().getClass());
				LOGGER_AUDIT_FILTER.trace("Type Raw Class: {}", writer.getType().getRawClass());
				LOGGER_AUDIT_FILTER.trace("==============================");
				List<Object> objectList = null;
				if (pojo instanceof RequestAuditData) {
					List<Object> requestObjectList = ((RequestAuditData) pojo).getRequest();
					if ((requestObjectList != null) && !requestObjectList.isEmpty()) {
						objectList = restrictObjectsToSetByteLimit(requestObjectList);
						((RequestAuditData) pojo).setRequest(objectList);
					}
				}
				if (pojo instanceof ResponseAuditData) {
					Object responseObject = ((ResponseAuditData) pojo).getResponse();
					if (responseObject != null) {
						objectList = restrictObjectsToSetByteLimit(Arrays.asList(responseObject));
						((ResponseAuditData) pojo).setResponse(objectList.get(0));
					}
				}
			}
		}

		/**
		 * Restrict objects to set byte limit.
		 *
		 * @param objectList the object list
		 * @return the list
		 * @throws IOException
		 */
		private static List<Object> restrictObjectsToSetByteLimit(final List<Object> objectList) {
			List<Object> newObjectList = new LinkedList<>();
			for (Object object : objectList) {
				if (object instanceof byte[]) {
					byte[] bytesAfterLimiting = null;
					try {
						ByteArrayOutputStream bo = new ByteArrayOutputStream();
						ObjectOutputStream so = new ObjectOutputStream(bo);
						so.writeObject(object);
						if (bo.size() > NUMBER_OF_BYTES_TO_LIMIT_AUDIT_LOGGED_OBJECT) {
							bytesAfterLimiting = new byte[NUMBER_OF_BYTES_TO_LIMIT_AUDIT_LOGGED_OBJECT];
							bo.write(bytesAfterLimiting);
							newObjectList.add(bytesAfterLimiting);
						}
						so.flush();
					} catch (IOException e) {
						LOGGER_AUDIT_FILTER
								.error("IOException thrown from AuditSimpleBeanObjectFilter for restrictObjectsToSetByteLimit", e);
						return objectList;
					}
				} else {
					newObjectList.add(object);
				}
			}
			return newObjectList;
		}
	}

	/**
	 * Asynchronuously writes to the audit logger.
	 * <p>
	 * <b>"Around" Advised by:</b>
	 * org.springframework.cloud.sleuth.instrument.async.TraceAsyncAspect.traceBackgroundThread(org.aspectj.lang.ProceedingJoinPoint)
	 *
	 * @param auditEventData
	 *            Data specific to the audit event
	 * @param messageSeverity
	 *            the message severity
	 * @param activityDetail
	 *            the activity detail
	 */
	@Async
	public void asyncAuditMessageData(final AuditEventData auditEventData, final String activityDetail,
			final MessageSeverity messageSeverity, final Throwable t) {

		if (messageSeverity.equals(MessageSeverity.ERROR) || messageSeverity.equals(MessageSeverity.FATAL)) {
			AuditLogger.error(auditEventData, activityDetail, t);
		} else if (messageSeverity.equals(MessageSeverity.WARN)) {
			AuditLogger.warn(auditEventData, activityDetail);
		} else {
			AuditLogger.info(auditEventData, activityDetail);
		}
	}

}

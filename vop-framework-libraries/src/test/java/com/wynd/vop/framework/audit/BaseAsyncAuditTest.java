package com.wynd.vop.framework.audit;

import com.wynd.vop.framework.audit.model.MessageAuditData;
import com.wynd.vop.framework.messages.MessageSeverity;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BaseAsyncAuditTest {

	@Test
	public void postConstructTest() {
		BaseAsyncAudit baseAsyncAudit = new BaseAsyncAudit();
		AuditLogSerializer auditLogSerializer = new AuditLogSerializer();
		ReflectionTestUtils.setField(baseAsyncAudit, "auditLogSerializer", auditLogSerializer);
		baseAsyncAudit.postConstruct();
	}

	@Test
	public void closeInputStreamIfRequiredTest() {
		InputStream mockInputstream = mock(InputStream.class);
		BaseAsyncAudit.closeInputStreamIfRequired(mockInputstream);
		try {
			verify(mockInputstream, times(1)).close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("Problem testing input stream closing");
		}
	}
	
	@Test
	public void closeInputStreamIfRequiredFailureTest() throws IOException {
		InputStream mockInputstream = mock(InputStream.class);
		Mockito.doThrow(new IOException()).when(mockInputstream).close();
		
		BaseAsyncAudit.closeInputStreamIfRequired(mockInputstream);
	}

	@Test
	public void convertBytesOfSetSizeToStringTest() {
		try {
			InputStream stubInputStream = 
					IOUtils.toInputStream("some test data for my input stream", "UTF-8");
			String convertedString = BaseAsyncAudit.convertBytesOfSetSizeToString(stubInputStream);
			assertEquals("some test data for my input stream", convertedString);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Problem testing convert bytes of set size to string");
		}
	}
	
	@Test
	public void convertBytesOfSetSizeToStringLargeTextTest() {
		try {
			InputStream stubInputStream = 
					IOUtils.toInputStream("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec vel nisl placerat, "
							+ "sodales odio eget, commodo nulla. In tempus faucibus leo non dapibus. Proin id fringilla elit, eu gravida metus. Nulla porttitor eros id sem egestas efficitur. "
							+ "Curabitur nibh magna, fermentum ut eros ac, sagittis vestibulum felis. Vestibulum laoreet non enim eu faucibus. Fusce laoreet est non turpis hendrerit, mollis porta quam iaculis. "
							+ "Etiam ut bibendum quam. Cras ac facilisis erat, eu sodales est. Cras diam erat, egestas non dignissim ac, eleifend ac neque. Donec imperdiet tristique turpis eget tristique. "
							+ "Fusce mauris mauris, ultricies eu justo quis, efficitur mattis massa. Mauris urna orci, eleifend et dui ac, blandit maximus quam. Sed eget gravida orci, ut vulputate enim. "
							+ "Curabitur nec sapien ultricies, congue sapien id, tincidunt leo. Integer imperdiet arcu eu lectus bibendum, in consectetur lacus fermentum. Curabitur tempus lobortis mattis. "
							+ "Aliquam sodales posuere elementum. Curabitur auctor, ipsum at gravida ultrices amet. Aliquam sodales posuere elementum", "UTF-8");
			String convertedString = BaseAsyncAudit.convertBytesOfSetSizeToString(stubInputStream);
			assertNotNull(convertedString);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Problem testing convert bytes of set size to string");
		}
	}
	
	@Test
	public void convertBytesOfSetSizeToStringExceptionTest() {
		try {
			InputStream mockInputstream = mock(InputStream.class);
			byte[] data = new byte[BaseAsyncAudit.NUMBER_OF_BYTES_TO_LIMIT_AUDIT_LOGGED_OBJECT];
			Mockito.when(mockInputstream.read(data, 0, data.length)).thenThrow(IOException.class);
			String convertedString = BaseAsyncAudit.convertBytesOfSetSizeToString(mockInputstream);
			assertEquals("", convertedString);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Problem testing convert bytes of set size to string");
		}
	}
	
	@Test
	public void convertBytesOfSetSizeToStringTestNull() {
		String convertedString = BaseAsyncAudit.convertBytesOfSetSizeToString(null);
		assertEquals("", convertedString);

	}
	
	
	@Test
	public void writeMessageAuditLogTest() {
		BaseAsyncAudit baseAsyncAudit = new BaseAsyncAudit();
		AuditLogSerializer auditLogSerializer = new AuditLogSerializer();
		ReflectionTestUtils.setField(auditLogSerializer, "dateFormat", "yyyy-MM-dd'T'HH:mm:ss");
		ReflectionTestUtils.setField(baseAsyncAudit, "auditLogSerializer", auditLogSerializer);
		baseAsyncAudit.postConstruct();
		AuditEventData auditEventData =
				new AuditEventData(AuditEvents.SERVICE_AUDIT, "INVOKE", this.getClass().getCanonicalName());
		
		final MessageAuditData messageAuditData = new 	MessageAuditData();
		messageAuditData.setMessage(Arrays.asList("personByPid call was successful"));
		baseAsyncAudit.writeMessageAuditLog(messageAuditData, auditEventData, MessageSeverity.INFO, null, this.getClass());
	}
}

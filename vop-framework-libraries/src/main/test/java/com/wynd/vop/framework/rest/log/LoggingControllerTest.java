package com.wynd.vop.framework.rest.log;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.wynd.vop.framework.rest.log.model.ClientLogEntry;
import com.wynd.vop.framework.rest.log.model.ClientLogEntryList;

@RunWith(MockitoJUnitRunner.class)
@AutoConfigureMockMvc
@WebAppConfiguration
public class LoggingControllerTest {
	private MockMvc mockMvc;

	private LoggingController loggingController = new LoggingController();
	private static ClientLogEntryList entryList;
	
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.standaloneSetup(loggingController).build();
		
		ClientLogEntry entry = ClientLogEntry.builder()
				.message("Log message")
				.level("warn")
				.timestamp("2021-10-20T00:00:00Z")
				.build();
		
		List<ClientLogEntry> entries = new ArrayList<>();
		entries.add(entry);
		entryList = new ClientLogEntryList(entries);
	}
	
	@Test
	public void testLoggingControllerPost() throws Exception {
		String jsonValue = new ObjectMapper().writeValueAsString(entryList);
		
		this.mockMvc.perform(
				post("/log")
				.header("Referer", "localhost")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(jsonValue))
		.andExpect(status().isOk())
		.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
	}
}

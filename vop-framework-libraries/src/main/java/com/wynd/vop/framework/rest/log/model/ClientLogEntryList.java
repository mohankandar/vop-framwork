package com.wynd.vop.framework.rest.log.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientLogEntryList {
	private List<ClientLogEntry> logs;
}

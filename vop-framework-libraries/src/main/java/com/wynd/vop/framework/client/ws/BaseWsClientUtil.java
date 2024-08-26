package com.wynd.vop.framework.client.ws;

import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

/**
 * Base WebService Client Utility
 *
 */
@Component("baseWsClientUtil")
public final class BaseWsClientUtil {

	/** The logger for this class */
	public static final VopLogger LOGGER = VopLoggerFactory.getLogger(BaseWsClientUtil.class);
	/**
	 * hide constructor.
	 */
	private BaseWsClientUtil() {
	}

	/**
	 * Verify add file prefix.
	 *
	 * @param fileLocation the file location
	 * @return the string
	 */
	public static String verifyAddFilePrefix(final String fileLocation) {
		StringBuilder finalFileLocation = new StringBuilder();
		if (StringUtils.isNotBlank(fileLocation)) {
			finalFileLocation.append(fileLocation);
			if (!fileLocation.startsWith(ResourceUtils.FILE_URL_PREFIX)
					&& !fileLocation.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
				LOGGER.warn("No prefix for the file location path, adding prefix file:");
				finalFileLocation.insert(0, ResourceUtils.FILE_URL_PREFIX);
			}
		}
		LOGGER.debug("File Location Returned {}", finalFileLocation.toString());
		return finalFileLocation.toString();
	}
}
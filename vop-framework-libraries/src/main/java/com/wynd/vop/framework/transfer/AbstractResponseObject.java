package com.wynd.vop.framework.transfer;

import com.wynd.vop.framework.messages.MessageSeverity;

/**
 * An abstract base class for provider and domain response objects.
 * <p>
 * Implementers of this class should implement the appropriate
 * {@code com.wynd.vop.framework.transfer.*TransferObjectMarker} interface.
 */
public abstract class AbstractResponseObject {

	public AbstractResponseObject() {
		super();
	}

	/**
	 * Determine whether any of the messages in the response are of the specified
	 * severity.
	 *
	 * @param severity - the {@link MessageSeverity} to look for
	 * @return boolean - {@code true} if the specified severity was found in a message
	 */
	protected abstract boolean hasMessagesOfType(final MessageSeverity severity);

	/**
	 * Checks the messages in the response for fatals.
	 *
	 * @return true, if successful
	 */
	public final boolean hasFatals() {
		return hasMessagesOfType(MessageSeverity.FATAL);
	}

	/**
	 * Checks the messages in the response for errors.
	 *
	 * @return true, if successful
	 */
	public final boolean hasErrors() {
		return hasMessagesOfType(MessageSeverity.ERROR);
	}

	/**
	 * Checks the messages in the response for warnings.
	 *
	 * @return true, if successful
	 */
	public final boolean hasWarnings() {
		return hasMessagesOfType(MessageSeverity.WARN);
	}

	/**
	 * Checks the messages in the response for infos.
	 *
	 * @return true, if successful
	 */
	public final boolean hasInfos() {
		return hasMessagesOfType(MessageSeverity.INFO);
	}

}
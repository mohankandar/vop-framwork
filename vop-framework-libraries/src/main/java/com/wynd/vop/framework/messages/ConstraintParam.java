package com.wynd.vop.framework.messages;

/**
 * A class for Replaceable Parameter tuples for Constraint messages, to simplify passing
 * replaceable parameter names and values in subclasses of {@link AbstractMessage}.
 *

 */
public class ConstraintParam {

	/** The name of the replaceable parameter in the message */
	private String name;
	/** The value to replace the parameter name in the message */
	private String value;

	/**
	 * Construct a Replaceable Parameter tuple for one Constraint message parameter.
	 * <p>
	 * Typically useful with passing message that need to include violation constraint parameters and the like.
	 *
	 * @param name - the name of the replaceable parameter in the message, as found between the curly braces in a constraint message
	 * @param value - the value to replace the parameter name in the message
	 */
	public ConstraintParam(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * The name of the replaceable parameter in the message,
	 * as found between the curly braces of replaceable parameters in a constraint message.
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * The name of the replaceable parameter in the message,
	 * as found between the curly braces of replaceable parameters in a constraint message.
	 *
	 * @param name - the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * The value to replace the parameter name in the message.
	 *
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * The value to replace the parameter name in the message.
	 *
	 * @param value - the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
}

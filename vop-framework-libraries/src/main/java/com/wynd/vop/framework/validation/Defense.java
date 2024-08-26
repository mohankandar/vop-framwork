package com.wynd.vop.framework.validation;

import com.wynd.vop.framework.exception.VopValidationRuntimeException;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * Make assertions as runtime validations of service layer method arguments.
 * Failed validations will always return {@link VopValidationRuntimeException},
 * with the underlying assertion error as the cause.
 *
 */
public final class Defense {

	/**
	 * Defense is a static class. Do not instantiate it.
	 */
	private Defense() {
		throw new IllegalAccessError("Defense is a static class. Do not instantiate it.");
	}

	/**
	 * Assert that the provided object is an instance of the provided class.
	 *
	 * @param clazz the clazz
	 * @param obj the obj
	 * @see Assert#isInstanceOf(Class, Object)
	 */
	public static void isInstanceOf(final Class<?> clazz, final Object obj) {
		try {
			Assert.isInstanceOf(clazz, obj);
		} catch (Exception e) {
			throw new VopValidationRuntimeException(MessageKeys.VOP_VALIDATOR_ASSERTION,
					MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, e, e.getMessage());
		}
	}

	/**
	 * Assert a boolean expression, throwing an IllegalStateException with the specified message
	 * if the expression evaluates to false.
	 *
	 * @param expression the expression
	 * @param message the message
	 * @see Assert#state(boolean, String)
	 */
	public static void state(final boolean expression, final String message) {
		try {
			Assert.state(expression, message);
		} catch (Exception e) {
			throw new VopValidationRuntimeException(MessageKeys.VOP_VALIDATOR_ASSERTION,
					MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, e, e.getMessage());
		}
	}

	/**
	 * Assert a boolean expression, throwing an IllegalStateException with default message
	 * if the expression evaluates to false.
	 *
	 * @param expression the expression
	 * @see Assert#state(boolean, String)
	 */
	public static void state(final boolean expression) {
		try {
			Assert.state(expression, "[Assertion failed] - this state invariant must be true");
		} catch (Exception e) {
			throw new VopValidationRuntimeException(MessageKeys.VOP_VALIDATOR_ASSERTION,
					MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, e, e.getMessage());
		}
	}

	/**
	 * Assert that an object is null, using a default message if the object is not null.
	 *
	 * @param ref the ref
	 * @see Assert#isNull(Object, String)
	 */
	public static void isNull(final Object ref) {
		try {
			Assert.isNull(ref, "[Assertion failed] - the object argument must be null");
		} catch (Exception e) {
			throw new VopValidationRuntimeException(MessageKeys.VOP_VALIDATOR_ASSERTION,
					MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, e, e.getMessage());
		}
	}

	/**
	 * Assert that an object is null, using the provided message if the object is not null.
	 *
	 * @param ref the ref
	 * @param message the message
	 * @see Assert#isNull(Object, String)
	 */
	public static void isNull(final Object ref, final String message) {
		try {
			Assert.isNull(ref, message);
		} catch (Exception e) {
			throw new VopValidationRuntimeException(MessageKeys.VOP_VALIDATOR_ASSERTION,
					MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, e, e.getMessage());
		}
	}

	/**
	 * Assert that an object is not null, using a default message if the object is null.
	 *
	 * @param ref the ref
	 * @see Assert#isNull(Object, String)
	 */
	public static void notNull(final Object ref) {
		try {
			Assert.notNull(ref, "[Assertion failed] - this argument is required; it must not be null");
		} catch (Exception e) {
			throw new VopValidationRuntimeException(MessageKeys.VOP_VALIDATOR_ASSERTION,
					MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, e, e.getMessage());
		}
	}

	/**
	 * Assert that an object is not null, using the provided message if the object is null.
	 *
	 * @param ref the ref
	 * @param message the message
	 * @see Assert#isNull(Object, String)
	 */
	public static void notNull(final Object ref, final String message) {
		try {
			Assert.notNull(ref, message);
		} catch (Exception e) {
			throw new VopValidationRuntimeException(MessageKeys.VOP_VALIDATOR_ASSERTION,
					MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, e, e.getMessage());
		}
	}

	/**
	 * Assert that the given String contains valid text content;
	 * that is, it must not be null and must contain at least one non-whitespace character.
	 * Uses a default message if the text does not pass the assertion.
	 *
	 * @param text the text
	 * @see Assert#hasText(String, String)
	 */
	public static void hasText(final String text) {
		try {
			Assert.hasText(text, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
		} catch (Exception e) {
			throw new VopValidationRuntimeException(MessageKeys.VOP_VALIDATOR_ASSERTION,
					MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, e, e.getMessage());
		}
	}

	/**
	 * Assert that the given String contains valid text content;
	 * that is, it must not be null and must contain at least one non-whitespace character.
	 * Uses a default message if the text does not pass the assertion.
	 *
	 * @param text the text
	 * @param message the message
	 * @see Assert#hasText(String, String)
	 */
	public static void hasText(final String text, final String message) {
		try {
			Assert.hasText(text, message);
		} catch (Exception e) {
			throw new VopValidationRuntimeException(MessageKeys.VOP_VALIDATOR_ASSERTION,
					MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, e, e.getMessage());
		}
	}

	/**
	 * Assert that a collection contains elements;
	 * that is, it must not be null and must contain at least one element.
	 * Uses a default message if the ref does not pass the assertion.
	 *
	 * @param ref the ref
	 * @see Assert#notEmpty(Collection, String)
	 */
	public static void notEmpty(final Collection<?> ref) {
		try {
			Assert.notEmpty(ref, "[Assertion failed] - this collection must not be empty: it must contain at least 1 element");
		} catch (Exception e) {
			throw new VopValidationRuntimeException(MessageKeys.VOP_VALIDATOR_ASSERTION,
					MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, e, e.getMessage());
		}
	}

	/**
	 * Assert that a collection contains elements;
	 * that is, it must not be null and must contain at least one element.
	 * Uses the provided message if the ref does not pass the assertion.
	 *
	 * @param ref the ref
	 * @param message the message
	 * @see Assert#notEmpty(Collection, String)
	 */
	public static void notEmpty(final Collection<?> ref, final String message) {
		try {
			Assert.notEmpty(ref, message);
		} catch (Exception e) {
			throw new VopValidationRuntimeException(MessageKeys.VOP_VALIDATOR_ASSERTION,
					MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, e, e.getMessage());
		}
	}

	/**
	 * Assert that an array contains elements;
	 * that is, it must not be null and must contain at least one element.
	 * Uses the provided message if the ref does not pass the assertion.
	 *
	 * @param ref the ref
	 * @param message the message
	 * @see Assert#notEmpty(Object[], String)
	 */
	public static void notEmpty(final String[] ref, final String message) {
		try {
			Assert.notEmpty(ref, message);
		} catch (Exception e) {
			throw new VopValidationRuntimeException(MessageKeys.VOP_VALIDATOR_ASSERTION,
					MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, e, e.getMessage());
		}
	}

	/**
	 * Assert a non-null boolean expression,
	 * throwing an IllegalArgumentException if the expression is null or evaluates to false.
	 * Uses a default message if the expression does not pass the assertion.
	 *
	 * @param expression the expression
	 * @see Assert#notNull(Object, String)
	 * @see Assert#isTrue(boolean, String)
	 */
	public static void isTrue(final Boolean expression) {
		try {
			Assert.notNull(expression, "[Assertion failed] - this argument is required; it must not be null");
			Assert.isTrue(expression, "[Assertion failed] - this expression must be true");
		} catch (Exception e) {
			throw new VopValidationRuntimeException(MessageKeys.VOP_VALIDATOR_ASSERTION,
					MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, e, e.getMessage());
		}
	}

	/**
	 * Assert a non-null boolean expression,
	 * throwing an IllegalArgumentException if the expression is null or evaluates to false.
	 * Uses the provided message if the expression does not pass the assertion.
	 *
	 * @param expression the expression
	 * @param message the message
	 * @see Assert#notNull(Object, String)
	 * @see Assert#isTrue(boolean, String)
	 */
	public static void isTrue(final Boolean expression, final String message) {
		try {
			Assert.notNull(expression, message);
			Assert.isTrue(expression, message);
		} catch (Exception e) {
			throw new VopValidationRuntimeException(MessageKeys.VOP_VALIDATOR_ASSERTION,
					MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, e, e.getMessage());
		}
	}

}

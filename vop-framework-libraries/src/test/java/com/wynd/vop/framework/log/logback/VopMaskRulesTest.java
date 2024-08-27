package com.wynd.vop.framework.log.logback;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VopMaskRulesTest {

	@Test
	public void shouldValidateAllRules() throws Exception {
		VopMaskRules rules = new VopMaskRules();
		rules.addRule(new VopMaskRule.Definition("Credit Card", "\\d{13,18}"));
		rules.addRule(new VopMaskRule.Definition("SSN", "\\d{3}-?\\d{2}-?\\d{4}"));

		String output =
				rules.apply("My credit card number is 4111111111111111 and my social security number is 123-12-1234");
		assertThat(output).isEqualTo("My credit card number is **************** and my social security number is ***********");
	}

	@Test
	public final void testHashCodeAndEqualsAndEtters()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		VopMaskRule.Definition testDef = new VopMaskRule.Definition();
		VopMaskRule.Definition otherDef = new VopMaskRule.Definition();

		assertTrue(testDef.hashCode() != 0);
		assertTrue(testDef.equals(testDef));
		assertFalse(testDef.equals(null));
		assertFalse(testDef.equals("A different type"));
		assertTrue(testDef.equals(otherDef));

		testDef.setName("TEST");
		assertTrue(testDef.hashCode() != 0);
		assertFalse(testDef.equals(otherDef));
		otherDef.setName("TEST");
		assertTrue(testDef.hashCode() != 0);
		assertTrue(testDef.equals(otherDef));
		testDef.setName(null);
		assertTrue(testDef.hashCode() != 0);
		assertFalse(testDef.equals(otherDef));
		testDef.setName("TEST"); // for next test

		testDef.setPattern("TEST");
		assertTrue(testDef.hashCode() != 0);
		assertFalse(testDef.equals(otherDef));
		otherDef.setPattern("TEST");
		assertTrue(testDef.hashCode() != 0);
		assertTrue(testDef.equals(otherDef));
		testDef.setPattern(null);
		assertTrue(testDef.hashCode() != 0);
		assertFalse(testDef.equals(otherDef));
		testDef.setPattern("TEST"); // for next test

		testDef.setPrefix("TEST");
		assertTrue(testDef.hashCode() != 0);
		assertFalse(testDef.equals(otherDef));
		otherDef.setPrefix("TEST");
		assertTrue(testDef.hashCode() != 0);
		assertTrue(testDef.equals(otherDef));
		testDef.setPrefix(null);
		assertTrue(testDef.hashCode() != 0);
		assertFalse(testDef.equals(otherDef));
		testDef.setPrefix("TEST"); // for next test

		testDef.setSuffix("TEST");
		assertTrue(testDef.hashCode() != 0);
		assertFalse(testDef.equals(otherDef));
		otherDef.setSuffix("TEST");
		assertTrue(testDef.hashCode() != 0);
		assertTrue(testDef.equals(otherDef));
		testDef.setSuffix(null);
		assertTrue(testDef.hashCode() != 0);
		assertFalse(testDef.equals(otherDef));
		testDef.setSuffix("TEST"); // for next test

		testDef.setUnmasked(4);
		assertTrue(testDef.hashCode() != 0);
		assertFalse(testDef.equals(otherDef));
		otherDef.setUnmasked(4);
		assertTrue(testDef.hashCode() != 0);
		assertTrue(testDef.equals(otherDef));
		testDef.setUnmasked(0);
		assertTrue(testDef.hashCode() != 0);
		assertFalse(testDef.equals(otherDef));
	}

	@Test
	public final void testToString() {
		VopMaskRule.Definition testDef = new VopMaskRule.Definition();
		assertTrue(testDef.toString().length() > 0);
	}
}
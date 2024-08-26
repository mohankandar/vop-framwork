package com.wynd.vop.framework.log.logback;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class VopMaskRuleTest {

	@RunWith(Parameterized.class)
	public static class InvalidMasks {

		@Rule
		public ExpectedException thrown = ExpectedException.none();

		private final String invalid;

		public InvalidMasks(String invalid) {
			this.invalid = invalid;
		}

		@Parameters
		public static Object[][] data() {
			return new Object[][] {
					{ null },
					{ "" },
					{ "   " },
					{ "\t   \t" },
					{ "\t   \t\n\n" },
			};
		}

		@Test
		public void shouldNotCreateWithAnInvalidPattern() throws Exception {
			thrown.expect(IllegalArgumentException.class);
			new VopMaskRule.Definition("Test", invalid).rule();
		}

		@Test
		public void shouldNotCreateWithAnInvalidName() throws Exception {
			thrown.expect(IllegalArgumentException.class);
			new VopMaskRule.Definition(invalid, "\\d{13,18}").rule();
		}
	}

	@RunWith(Parameterized.class)
	public static class ValidMasks {
		private final String pattern;
		private final String input;
		private final String output;
		private final int unmasked;
		private final String prefix;
		private final String suffix;

		public ValidMasks(int unmasked, String prefix, String pattern, String suffix, String input, String output) {
			this.unmasked = unmasked;
			this.prefix = prefix;
			this.pattern = pattern;
			this.suffix = suffix;
			this.input = input;
			this.output = output;
		}

		@Parameters(name = "Pattern {2} - leave {0} unmasked character(s)")
		public static Object[][] data() {
			return new Object[][] {
					// @formatter:off
					{ 0, "<test>", "(\\S+)", "</test>", "<other>\n  <test>\nhello\n</test>\n  <more>bye</more>\n</other>",
							"<other>\n  <test>\n*****\n</test>\n  <more>bye</more>\n</other>" },
					{ 0, "<test>", "(\\S+)", "</test>", "<other>\n  <test>hello</test>\n  <more>bye</more>\n</other>",
							"<other>\n  <test>*****</test>\n  <more>bye</more>\n</other>" },
					{ 0, "<test>", "\\S+", "</test>", "<other>\n  <test>\n\nhello</test>\n  <more>bye</more>\n</other>",
							"<other>\n  <test>\n\n*****</test>\n  <more>bye</more>\n</other>" },
					{ 2, "<test>", "\\S+", "</test>", "<other>\n  <test>\n\nhello</test>\n  <more>bye</more>\n</other>",
							"<other>\n  <test>\n\n***lo</test>\n  <more>bye</more>\n</other>" },
					{ 5, "<test>", "\\S+", "</test>", "<other>\n  <test>\n\nhello</test>\n  <more>bye</more>\n</other>",
							"<other>\n  <test>\n\nhello</test>\n  <more>bye</more>\n</other>" },
					{ 5, "<test>", "\\S+", "</test>", "<other>\n  <test>\n\nhello123</test>\n  <more>bye</more>\n</other>",
							"<other>\n  <test>\n\n***lo123</test>\n  <more>bye</more>\n</other>" },
					{ 0, "<test>", "\\S+", "</test>", "<other>\n  <test>\n\nhello123</test>\n  <more>bye</more>\n</other>",
							"<other>\n  <test>\n\n********</test>\n  <more>bye</more>\n</other>" },
					{ 0, "", "\\d{3}-?\\d{2}-?\\d{4}", "", "123-12-1234", "***********" },
					{ 4, "", "\\d{3}-?\\d{2}-?\\d{4}", "", "123121234", "*****1234" },
					{ 4, "", "\\d{3}-?\\d{2}-?\\d{4}", "", "123-12-1234", "*******1234" },
					{ 12, "", "\\d{3}-?\\d{2}-?\\d{4}", "", "123-12-1234", "123-12-1234" },
					{ 4, "", "\\d{13,18}", "", "4111111111111111", "************1111" }
					// @formatter:on
			};
		}

		@Test
		public void shouldMask() throws Exception {
			VopMaskRule rule = new VopMaskRule.Definition("Test", prefix, suffix, pattern, unmasked).rule();
			assertThat(rule.apply(input)).isEqualTo(output);
		}

	}

	public static class GeneralTests {
		@Test
		public void testDefinition() {
			VopMaskRule.Definition def = null;
			try {
				new VopMaskRule(def);
			} catch (Exception e) {
				assertTrue(IllegalArgumentException.class.equals(e.getClass()));
			}

			def = new VopMaskRule.Definition();
			try {
				new VopMaskRule(def);
			} catch (Exception e) {
				assertTrue(IllegalArgumentException.class.equals(e.getClass()));
			}

			def.setName("Some Name");
			try {
				new VopMaskRule(def);
			} catch (Exception e) {
				assertTrue(IllegalArgumentException.class.equals(e.getClass()));
			}

			def.setPattern("some[ -]?Pattern");
			def.setUnmasked(-1);
			try {
				new VopMaskRule(def);
			} catch (Exception e) {
				assertTrue(IllegalArgumentException.class.equals(e.getClass()));
			}
		}

		@Test
		public void testDefinitionGetters() {
			VopMaskRule.Definition def = new VopMaskRule.Definition("Name", "Prefix", "Suffix", "Pattern", 1);
			assertThat(def.getName()).isEqualTo("Name");
			assertThat(def.getPrefix()).isEqualTo("Prefix");
			assertThat(def.getSuffix()).isEqualTo("Suffix");
			assertThat(def.getPattern()).isEqualTo("Pattern");
			assertThat(def.getUnmasked()).isEqualTo(1);
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
			VopMaskRule.Definition testFilter = new VopMaskRule.Definition();
			assertTrue(testFilter.toString().length() > 0);
		}
	}
}
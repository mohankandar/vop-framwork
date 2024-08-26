package com.wynd.vop.framework.config;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

public class VopCommonSpringProfilesTest {

	/**
	 * Spring default profile
	 */
	public static final String TEST_PROFILE_DEFAULT = "default";

	/**
	 * Spring profile for local dev environment
	 */
	public static final String TEST_PROFILE_ENV_LOCAL_INT = "local-int";

	/**
	 * Spring profile for local dev environment
	 */
	public static final String TEST_PROFILE_ENV_DOCKER_DEMO = "docker-demo";

	/**
	 * Spring profile for AWS CI environment
	 */
	public static final String TEST_PROFILE_ENV_CI = "ci";

	/**
	 * Spring profile for AWS DEV environment
	 */
	public static final String TEST_PROFILE_ENV_DEV = "dev";

	/**
	 * Spring profile for AWS STAGE environment
	 */
	public static final String TEST_PROFILE_ENV_STAGE = "stage";

	/**
	 * Spring profile for AWS PROD environment
	 */
	public static final String TEST_PROFILE_ENV_PROD = "prod";

	/**
	 * Spring profile for remote client real implementations
	 */
	public static final String TEST_PROFILE_REMOTE_CLIENT_IMPLS = "remote_client_impls";

	/**
	 * Spring profile for remote client simulator implementations
	 */
	public static final String TEST_PROFILE_REMOTE_CLIENT_SIMULATORS = "remote_client_sims";

	/**
	 * Spring profile for unit test specific impls
	 */
	public static final String TEST_PROFILE_UNIT_TEST = "unit_test_sims";

	/**
	 * Spring profile for remote audit simulator implementations
	 */
	public static final String TEST_PROFILE_REMOTE_AUDIT_SIMULATORS = "remote_audit_client_sims";

	/**
	 * Spring profile for remote audit impl implementations
	 */
	public static final String TEST_PROFILE_REMOTE_AUDIT_IMPLS = "remote_audit_client_impl";

	/**
	 * Spring Profile to signify that the application will run embedded redis
	 */
	public static final String TEST_PROFILE_EMBEDDED_REDIS = "embedded-redis";

	/**
	 * Spring Profile to signify that the application will run embedded AWS
	 */
	public static final String TEST_PROFILE_EMBEDDED_AWS = "embedded-aws";

	/**
	 * Spring Profile to signify that the configuration will not be loaded in embedded aws
	 */
	public static final String TEST_NOT_PROFILE_EMBEDDED_AWS = "!embedded-aws";

	public static String getTestProfileDefault() {
		return TEST_PROFILE_DEFAULT;
	}

	public static String getTestProfileEnvLocalInt() {
		return TEST_PROFILE_ENV_LOCAL_INT;
	}

	public static String getTestProfileEnvDockerDemo() {
		return TEST_PROFILE_ENV_DOCKER_DEMO;
	}

	public static String getTestProfileEnvCi() {
		return TEST_PROFILE_ENV_CI;
	}

	public static String getTestProfileEnvDev() {
		return TEST_PROFILE_ENV_DEV;
	}

	public static String getTestProfileEnvStage() {
		return TEST_PROFILE_ENV_STAGE;
	}

	public static String getTestProfileEnvProd() {
		return TEST_PROFILE_ENV_PROD;
	}

	public static String getTestProfileRemoteClientImpls() {
		return TEST_PROFILE_REMOTE_CLIENT_IMPLS;
	}

	public static String getTestProfileRemoteClientSimulators() {
		return TEST_PROFILE_REMOTE_CLIENT_SIMULATORS;
	}

	public static String getTestProfileUnitTest() {
		return TEST_PROFILE_UNIT_TEST;
	}

	public static String getTestProfileRemoteAuditSimulators() {
		return TEST_PROFILE_REMOTE_AUDIT_SIMULATORS;
	}

	public static String getTestProfileRemoteAuditImpls() {
		return TEST_PROFILE_REMOTE_AUDIT_IMPLS;
	}

	public static String getTestProfileEmbeddedRedis() {
		return TEST_PROFILE_EMBEDDED_REDIS;
	}

	public static String getTestProfileEmbeddedAws() {
		return TEST_PROFILE_EMBEDDED_AWS;
	}

	public static String getTestNotProfileEmbeddedAws() {
		return TEST_NOT_PROFILE_EMBEDDED_AWS;
	}

	@Test
	public void profileDefaultTest() throws Exception {
		assertEquals(CommonSpringProfiles.PROFILE_DEFAULT, getTestProfileDefault());
	}

	@Test
	public void profileLocalIntTest() throws Exception {
		assertEquals(CommonSpringProfiles.PROFILE_ENV_LOCAL_INT, getTestProfileEnvLocalInt());
	}

	@Test
	public void profileDockerDemoTest() throws Exception {
		assertEquals(CommonSpringProfiles.PROFILE_ENV_DOCKER_DEMO, getTestProfileEnvDockerDemo());
	}

	@Test
	public void profileAwsCITest() throws Exception {
		assertEquals(CommonSpringProfiles.PROFILE_ENV_CI, getTestProfileEnvCi());
	}

	@Test
	public void profileAwsDevTest() throws Exception {
		assertEquals(CommonSpringProfiles.PROFILE_ENV_DEV, getTestProfileEnvDev());
	}

	@Test
	public void profileAwsStageTest() throws Exception {
		assertEquals(CommonSpringProfiles.PROFILE_ENV_STAGE, getTestProfileEnvStage());
	}

	@Test
	public void profileAwsProdTest() throws Exception {
		assertEquals(CommonSpringProfiles.PROFILE_ENV_PROD, getTestProfileEnvProd());
	}

	@Test
	public void profileRemoteClientSimulatorsTest() throws Exception {
		assertEquals(CommonSpringProfiles.PROFILE_REMOTE_CLIENT_SIMULATORS, getTestProfileRemoteClientSimulators());
	}

	@Test
	public void profileRemoteClientImplsTest() throws Exception {
		assertEquals(CommonSpringProfiles.PROFILE_REMOTE_CLIENT_IMPLS, getTestProfileRemoteClientImpls());
	}

	@Test
	public void profileRemoteAuditSimulatorsTest() throws Exception {
		assertEquals(CommonSpringProfiles.PROFILE_REMOTE_AUDIT_SIMULATORS, getTestProfileRemoteAuditSimulators());
	}

	@Test
	public void profileRemoteAuditImplsTest() throws Exception {
		assertEquals(CommonSpringProfiles.PROFILE_REMOTE_AUDIT_IMPLS, getTestProfileRemoteAuditImpls());
	}

	@Test
	public void profileUnitTestingTest() throws Exception {
		assertEquals(CommonSpringProfiles.PROFILE_UNIT_TEST, getTestProfileUnitTest());
	}

	@Test
	public void profileEmbeddedRedisTest() throws Exception {
		assertEquals(CommonSpringProfiles.PROFILE_EMBEDDED_REDIS, getTestProfileEmbeddedRedis());
	}

	@Test
	public void profileEmbeddedAwsTest() throws Exception {
		assertEquals(CommonSpringProfiles.PROFILE_EMBEDDED_AWS, getTestProfileEmbeddedAws());
	}

	@Test
	public void notProfileEmbeddedAwsTest() throws Exception {
		assertEquals(CommonSpringProfiles.NOT_PROFILE_EMBEDDED_AWS, getTestNotProfileEmbeddedAws());
	}

	@Test
	public void referenceCommonSpringProfilesConstructor() throws Exception {
		Constructor<CommonSpringProfiles> c = CommonSpringProfiles.class.getDeclaredConstructor((Class<?>[]) null);
		c.setAccessible(true);
		try {
			c.newInstance();
			fail("Should have thrown exception");
		} catch (Exception e) {
			assertTrue(InvocationTargetException.class.equals(e.getClass()));
			assertTrue(IllegalStateException.class.equals(e.getCause().getClass()));
		}
	}
}

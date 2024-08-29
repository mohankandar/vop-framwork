package com.wynd.vop.framework.config;

import com.wynd.vop.framework.constants.VopConstants;

/**
 * VopConstants to store the profiles commonly used in the applications.
 *
 */
public final class CommonSpringProfiles {

	/**
	 * Spring default profile
	 */
	public static final String PROFILE_DEFAULT = "default";

	/**
	 * Spring profile for local dev environment
	 */
	public static final String PROFILE_ENV_LOCAL_INT = "local-int";

	/**
	 * Spring profile for local dev environment
	 */
	public static final String PROFILE_ENV_DOCKER_DEMO = "docker-demo";

	/**
	 * Spring profile for AWS CI environment
	 */
	public static final String PROFILE_ENV_CI = "ci";

	/**
	 * Spring profile for AWS DEV environment
	 */
	public static final String PROFILE_ENV_DEV = "dev";

	/**
	 * Spring profile for AWS STAGE environment
	 */
	public static final String PROFILE_ENV_STAGE = "stage";

	/**
	 * Spring profile for AWS PROD environment
	 */
	public static final String PROFILE_ENV_PROD = "prod";

	/**
	 * Spring profile for remote client real implementations
	 */
	public static final String PROFILE_REMOTE_CLIENT_IMPLS = "remote_client_impls";

	/**
	 * Spring profile for remote client simulator implementations
	 */
	public static final String PROFILE_REMOTE_CLIENT_SIMULATORS = "remote_client_sims";

	/**
	 * Spring profile for unit test specific impls
	 */
	public static final String PROFILE_UNIT_TEST = "unit_test_sims";

	/**
	 * Spring profile for remote audit simulator implementations
	 */
	public static final String PROFILE_REMOTE_AUDIT_SIMULATORS = "remote_audit_client_sims";

	/**
	 * Spring profile for remote audit impl implementations
	 */
	public static final String PROFILE_REMOTE_AUDIT_IMPLS = "remote_audit_client_impl";

	/**
	 * Spring Profile to signify that the application will run embedded redis
	 */
	public static final String PROFILE_EMBEDDED_REDIS = "embedded-redis";

	/**
	 * Spring Profile to signify that the application will run embedded aws
	 */
	public static final String PROFILE_EMBEDDED_AWS = "embedded-aws";

	/**
	 * Spring Profile to signify that the configuration will not be loaded in embedded aws
	 */
	public static final String NOT_PROFILE_EMBEDDED_AWS = "!embedded-aws";

	/**
	 * Do not instantiate.
	 */
	private CommonSpringProfiles() {
		throw new IllegalStateException(CommonSpringProfiles.class.getSimpleName() + VopConstants.ILLEGALSTATE_STATICS);
	}
}

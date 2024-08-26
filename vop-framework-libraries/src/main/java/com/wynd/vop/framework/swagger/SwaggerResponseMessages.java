package com.wynd.vop.framework.swagger;

/**
 * The Interface SwaggerResponseMessages contains response messages for documenting into
 * the swagger documentation. This is to ensure consistent messaging (and ideally behavior)
 * for services.
 *
 
 */
public interface SwaggerResponseMessages { // NOSONAR constants must be in this class
	/** The Constant MESSAGE_200 SUCCESS */
	static final String MESSAGE_200 =
			"A Response which indicates a successful Request.  Response may contain \"messages\" that could describe warnings or further information.";
	
	/** The Constant MESSAGE_400 BAD REQUEST (some issue with the request) */
	static final String MESSAGE_400 =
			"There was an error encountered processing the Request.  Response will contain \"messages\" element with additional information on the error.  This request shouldn't be retried until corrected.";

	/** The Constant MESSAGE_401 Unauthorized */
	static final String MESSAGE_401 = "The request is not authorized.  Please verify credentials in the request. Response will contain \"messages\" element with additional information on the error.";

	/** The Constant MESSAGE_403 FORBIDDEN */
	static final String MESSAGE_403 = "Access to the resource is Forbidden.  Please verify if you have permission to access this resource. Response will contain \"messages\" element with additional information on the error.";

	/** The Constant MESSAGE_500 INTERNAL SERVER ERROR (some issue in the server-side code) */
	static final String MESSAGE_500 =
			"There was an error encountered processing the Request. Response will contain \"messages\" element with additional information on the error. Please retry. If problem persists, please contact support with a copy of the Response.";

}
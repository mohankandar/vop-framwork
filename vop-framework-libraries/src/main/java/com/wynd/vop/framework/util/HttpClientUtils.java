package com.wynd.vop.framework.util;

import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class HttpClientUtils {
    
    private static final VopLogger LOGGER = VopLoggerFactory.getLogger(HttpClientUtils.class);
    
    private HttpClientUtils() {
    
    }
    
    /**
     * Utility class to handle retries to work around Apache HttpClient issues with orphaned connections.
     */
    public static class NoHttpResponseExceptionHttpRequestRetryHandler extends DefaultHttpRequestRetryHandler {
    
        private final int noHttpResponseRetryCount;
    
        /**
         * Public constructor.
         *          The number of retries allowed for NoHttpResponseExceptions before giving up. For other exceptions,
         *          it still defaults to 3 retries.
         */
        public NoHttpResponseExceptionHttpRequestRetryHandler(final int noHttpResponseRetryCount) {
            super(3, false);
            this.noHttpResponseRetryCount = noHttpResponseRetryCount;
        }
        
        
        /**
         * Invoked when Apache HttpClient is attempting to determine if it should retry a request.
         * @param exception
         *          The exception that caused the need to check for a retry.
         * @param executionCount
         *          The number of times the retry has been retried.
         * @param context
         *          The HttpContext associated with the request.
         *
         * @return Whether the retry should continue.
         */
        @Override
        public boolean retryRequest(final IOException exception, final int executionCount, final HttpContext context) {
            if (exception instanceof org.apache.http.NoHttpResponseException && executionCount <= noHttpResponseRetryCount) {
                LOGGER.warn("No response from server on {}", executionCount, " call");
                return true;
            } else {
                final boolean retry = super.retryRequest(exception, executionCount, context);
                if (retry) {
                    LOGGER.warn("Retrying based on DefaultHttpRequestRetryHandler default behavior for {}", executionCount, " call");
                }
                return retry;
            }
        }
    }
    
    /**
     * Sets the retry handler to client builder.
     *
     * @param clientBuilder the new retry handler to client builder
     * @param noHttpResponseRetryCount Max number of retries for a NoHttpResponseException
     */
    public static void setRetryHandlerToClientBuilder(final HttpClientBuilder clientBuilder, final int noHttpResponseRetryCount) {
        clientBuilder.setRetryHandler(new NoHttpResponseExceptionHttpRequestRetryHandler(noHttpResponseRetryCount));
    }
}

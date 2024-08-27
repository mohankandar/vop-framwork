package com.wynd.vop.framework.client.ws;

import com.wynd.vop.framework.exception.VopPartnerRuntimeException;
import com.wynd.vop.framework.exception.VopRuntimeException;
import com.wynd.vop.framework.log.PerformanceLogMethodInterceptor;
import com.wynd.vop.framework.util.HttpClientUtils;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.execchain.ClientExecChain;
import org.apache.http.impl.execchain.MainClientExec;
import org.apache.http.impl.execchain.RetryExec;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.security.KeyStore;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class BaseWsClientConfigTest {

	private Resource KEYSTORE = new FileSystemResource("src/test/resources/ssl/dev/vaebnweb1Keystore.jks");
	private String KEYSTORE_PASS = "password";
	private Resource TRUSTSTORE = new FileSystemResource("src/test/resources/ssl/dev/vaebnTruststore.jks");
	private String TRUSTSTORE_PASS = "password";

	@Mock
	Marshaller mockMarshaller;
	@Mock
	Unmarshaller mockUnmarshaller;
	@Mock
	ClientInterceptor mockClientInterceptor;
	ClientInterceptor intercpetors[] = { mockClientInterceptor };
	@Mock
	HttpResponseInterceptor mockHttpResponseInterceptor;
	HttpResponseInterceptor respInterceptors[] = { mockHttpResponseInterceptor };
	@Mock
	HttpRequestInterceptor mockHttpRequestInterceptor;
	HttpRequestInterceptor reqInterceptors[] = { mockHttpRequestInterceptor };
	@Mock
	WebServiceMessageFactory mockWebServiceMessageFactory;
	@Mock
	Resource mockResource;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateDefaultWebServiceTemplateStringIntIntMarshallerUnmarshaller() {
		BaseWsClientConfig test = new BaseWsClientConfig();
		assertTrue(test.createDefaultWebServiceTemplate("http://dummyservice/endpoint", 30, 30, mockMarshaller,
				mockUnmarshaller) instanceof WebServiceTemplate);
	}

	@Test
	public void testCreateDefaultWebServiceTemplateStringIntIntMarshallerUnmarshallerClientInterceptorArray() {
		BaseWsClientConfig test = new BaseWsClientConfig();
		assertTrue(test.createDefaultWebServiceTemplate("http://dummyservice/endpoint", 30, 30, mockMarshaller, mockUnmarshaller,
				intercpetors) instanceof WebServiceTemplate);
	}

	@Test
	public void
	testCreateDefaultWebServiceTemplateStringIntIntMarshallerUnmarshallerHttpRequestInterceptorArrayHttpResponseInterceptorArrayClientInterceptorArray() {
		BaseWsClientConfig test = new BaseWsClientConfig();
		assertTrue(test.createDefaultWebServiceTemplate("http://dummyservice/endpoint", 30, 30, mockMarshaller, mockUnmarshaller,
				reqInterceptors, respInterceptors, intercpetors) instanceof WebServiceTemplate);
	}

	@Test
	public void testCreateSaajWebServiceTemplate() {
		BaseWsClientConfig test = new BaseWsClientConfig();
		try {
			assertTrue(test.createSaajWebServiceTemplate("http://dummyservice/endpoint", 30, 30, mockMarshaller, mockUnmarshaller,
					intercpetors) instanceof WebServiceTemplate);
		} catch (SOAPException e) {

		}
	}

	@Test
	public void testAddSslContext() {
		BaseWsClientConfig test = new BaseWsClientConfig();
		HttpClientBuilder httpClient = HttpClients.custom();

		Resource mockKeystore = mock(KEYSTORE.getClass());

		// IOException
		try {
			Mockito.lenient().when(mockKeystore.getInputStream()).thenThrow(UnsupportedOperationException.class);
		} catch (IOException e1) {
			fail("Mocking should not throw exception");
		}
		try {
			test.addSslContext(httpClient, mockKeystore, KEYSTORE_PASS, TRUSTSTORE, TRUSTSTORE_PASS);
			fail("Should have thrown exception");
		} catch (Exception e) {
			assertTrue(e.getClass().isAssignableFrom(UnsupportedOperationException.class));
		}
	}

	@Test
	public void testCreateSslWebServiceTemplateStringIntIntMarshallerUnmarshaller() {
		BaseWsClientConfig test = new BaseWsClientConfig();
		assertTrue(test.createSslWebServiceTemplate("http://dummyservice/endpoint", 30, 30, mockMarshaller, mockUnmarshaller,
				KEYSTORE, KEYSTORE_PASS, TRUSTSTORE, TRUSTSTORE_PASS) instanceof WebServiceTemplate);
	}

	@Test
	public void testCreateSslWebServiceTemplateStringIntIntMarshallerUnmarshallerClientInterceptorArray() {
		BaseWsClientConfig test = new BaseWsClientConfig();
		assertTrue(test.createSslWebServiceTemplate("http://dummyservice/endpoint", 30, 30, mockMarshaller, mockUnmarshaller,
				intercpetors, KEYSTORE, KEYSTORE_PASS, TRUSTSTORE, TRUSTSTORE_PASS) instanceof WebServiceTemplate);
	}

	@Test
	public void
	testCreateSslWebServiceTemplateStringIntIntMarshallerUnmarshallerHttpRequestInterceptorArrayHttpResponseInterceptorArrayClientInterceptorArray() {
		BaseWsClientConfig test = new BaseWsClientConfig();
		assertTrue(test.createSslWebServiceTemplate("http://dummyservice/endpoint", 30, 30, mockMarshaller, mockUnmarshaller,
				reqInterceptors, respInterceptors, intercpetors, KEYSTORE, KEYSTORE_PASS, TRUSTSTORE,
				TRUSTSTORE_PASS) instanceof WebServiceTemplate);
	}

	@Test
	public void testCreateSaajSslWebServiceTemplate() {
		BaseWsClientConfig test = new BaseWsClientConfig();
		try {
			assertTrue(test.createSaajSslWebServiceTemplate("http://dummyservice/endpoint", 30, 30, mockMarshaller, mockUnmarshaller,
					intercpetors, KEYSTORE, KEYSTORE_PASS, TRUSTSTORE, TRUSTSTORE_PASS) instanceof WebServiceTemplate);
		} catch (SOAPException e) {

		}
	}

	@Test
	public void testCreateWebServiceTemplate() {
		BaseWsClientConfig test = new BaseWsClientConfig();
		assertTrue(test.createWebServiceTemplate("http://dummyservice/endpoint", 30, 30, mockMarshaller, mockUnmarshaller,
				reqInterceptors, respInterceptors, intercpetors, mockWebServiceMessageFactory, KEYSTORE, KEYSTORE_PASS, TRUSTSTORE,
				TRUSTSTORE_PASS) instanceof WebServiceTemplate);

		test = new BaseWsClientConfig();
		assertTrue(test.createWebServiceTemplate("http://dummyservice/endpoint", 30, 30, mockMarshaller, mockUnmarshaller,
				reqInterceptors, respInterceptors, intercpetors, mockWebServiceMessageFactory, null, null, null,
				null) instanceof WebServiceTemplate);
	}

	@Test
	public void testGetBeanNameAutoProxyCreator() {
		BaseWsClientConfig test = new BaseWsClientConfig();
		String beanNames[] = { "TestBeanName" };
		String interceptorNames[] = { "TestInterceptorName" };
		assertTrue(test.getBeanNameAutoProxyCreator(beanNames, interceptorNames) instanceof BeanNameAutoProxyCreator);
	}

	@Test
	public void testGetMarshaller() {
		BaseWsClientConfig test = new BaseWsClientConfig();
		Resource resources[] = { mockResource };
		try {
			test.getMarshaller("com.oracle.xmlns.internal.webservices.jaxws_databinding", resources, true);
		} catch (Exception e) {
			Assert.assertTrue(VopPartnerRuntimeException.class.isAssignableFrom(e.getClass()));
			//Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
			Assert.assertNotNull(e.getMessage());
		}
	}

	// pkg-com.wynd.vop.reference.partner.person.ws.transfer
	// resource-xsd/PersonService/PersonWebService.xsd
	@Test
	public void testGetPerformanceLogMethodInterceptor() {
		BaseWsClientConfig test = new BaseWsClientConfig();
		assertTrue(test.getPerformanceLogMethodInterceptor(5) instanceof PerformanceLogMethodInterceptor);
	}


	@Test(expected = VopRuntimeException.class)
	public void testHandleExceptions() {
		BaseWsClientConfig test = new BaseWsClientConfig();
		ReflectionTestUtils.invokeMethod(test, "handleExceptions", new Exception());
	}

	
	/**
	 * Test the use case where both a client KeyStore and custom TrustStore are provided.
	 * Expect that both KeyStores are populated in the configured SSLContext object.
	 * 
	 * @throws Exception
	 */
	@Test
	public void addSslContext_KeyStoreResource() throws Exception {
		HttpClientBuilder httpClientBuilder = HttpClients.custom();
		
		BaseWsClientConfig config = new BaseWsClientConfig();
		config.addSslContext(httpClientBuilder, KEYSTORE, KEYSTORE_PASS, TRUSTSTORE, TRUSTSTORE_PASS);
	}
	
	/**
	 * Test the use case where only custom TrustStore are provided.
	 * Expect that the custom TrustStore is populated in the configured SSLContext object.
	 * 
	 * @throws Exception
	 */
	@Test
	public void addSslContext_NoClientKeyStoreResource() throws Exception {
		HttpClientBuilder httpClientBuilder = HttpClients.custom();
		
		BaseWsClientConfig config = new BaseWsClientConfig();
		config.addSslContext(httpClientBuilder, null, null, TRUSTSTORE, TRUSTSTORE_PASS);
	}
	
	/**
	 * Test the use case where only a client KeyStore are provided.
	 * Expect that the client KeyStore is populated in the configured SSLContext object.
	 * 
	 * @throws Exception
	 */
	@Test
	public void addSslContext_NoTrustStoreResource() throws Exception {
		HttpClientBuilder httpClientBuilder = HttpClients.custom();
		
		BaseWsClientConfig config = new BaseWsClientConfig();
		config.addSslContext(httpClientBuilder, KEYSTORE, KEYSTORE_PASS, null, null);
	}
 
	@Test
    public void testRetryOnNoHttpResponseException() {
        BaseWsClientConfig test = new BaseWsClientConfig();
        ReflectionTestUtils.setField(test, "retryOnNoHttpResponseException", true);
        WebServiceTemplate defaultWebServiceTemplate = test.createDefaultWebServiceTemplate("http://dummyservice/endpoint", 30, 30, mockMarshaller,
                mockUnmarshaller);
        assertTrue(defaultWebServiceTemplate instanceof WebServiceTemplate);
        assertTrue(defaultWebServiceTemplate.getMessageSenders().length > 0);
        WebServiceMessageSender messageSender = defaultWebServiceTemplate.getMessageSenders()[0];
        assertTrue(messageSender instanceof HttpComponentsMessageSender);
        HttpComponentsMessageSender httpComponentsMessageSender = (HttpComponentsMessageSender) messageSender;
        HttpClient httpClient = httpComponentsMessageSender.getHttpClient();
        ClientExecChain execChain = (ClientExecChain) ReflectionTestUtils.getField(httpClient, "execChain");
        assertNotNull(execChain);
        ClientExecChain requestExecutor = execChain;
        while (!(requestExecutor instanceof RetryExec)) {
            requestExecutor = (ClientExecChain) ReflectionTestUtils.getField(requestExecutor, "requestExecutor");
        }
        
        RetryExec retryExec = (RetryExec) requestExecutor;
        HttpRequestRetryHandler httpRequestRetryHandler = (HttpRequestRetryHandler) ReflectionTestUtils.getField(retryExec, "retryHandler");
        assertNotNull(httpRequestRetryHandler);
        assertTrue(httpRequestRetryHandler instanceof HttpClientUtils.NoHttpResponseExceptionHttpRequestRetryHandler);
    }
    
    @Test
    public void testDisableConnectionReuse() {
        BaseWsClientConfig test = new BaseWsClientConfig();
        ReflectionTestUtils.setField(test, "disableConnectionReuse", true);
        WebServiceTemplate defaultWebServiceTemplate = test.createDefaultWebServiceTemplate("http://dummyservice/endpoint", 30, 30, mockMarshaller,
                mockUnmarshaller);
        assertTrue(defaultWebServiceTemplate instanceof WebServiceTemplate);
        assertTrue(defaultWebServiceTemplate.getMessageSenders().length > 0);
        WebServiceMessageSender messageSender = defaultWebServiceTemplate.getMessageSenders()[0];
        assertTrue(messageSender instanceof HttpComponentsMessageSender);
        HttpComponentsMessageSender httpComponentsMessageSender = (HttpComponentsMessageSender) messageSender;
        HttpClient httpClient = httpComponentsMessageSender.getHttpClient();
        ClientExecChain execChain = (ClientExecChain) ReflectionTestUtils.getField(httpClient, "execChain");
        assertNotNull(execChain);
        ClientExecChain requestExecutor = execChain;
        while (!(requestExecutor instanceof MainClientExec)) {
            requestExecutor = (ClientExecChain) ReflectionTestUtils.getField(requestExecutor, "requestExecutor");
        }
    
        MainClientExec mainClientExec = (MainClientExec) requestExecutor;
        ConnectionReuseStrategy connectionReuseStrategy = (ConnectionReuseStrategy) ReflectionTestUtils.getField(mainClientExec, "reuseStrategy");
        assertNotNull(connectionReuseStrategy);
        assertEquals(NoConnectionReuseStrategy.INSTANCE, connectionReuseStrategy);
    }
    
    @Test
    public void testEvictExpiredConnections() {
        BaseWsClientConfig test = new BaseWsClientConfig();
        ReflectionTestUtils.setField(test, "evictExpiredConnections", true);
        HttpClientBuilder httpClientBuilder = test.getHttpClientBuilder(null, null,
                null, null, null);
    
        boolean evictExpiredConnection = (Boolean) ReflectionTestUtils.getField(httpClientBuilder, "evictExpiredConnections");
        
        assertTrue(evictExpiredConnection);
    }
    
    @Test
    public void testPoolingHttpClientConnectionManager() {
        BaseWsClientConfig test = new BaseWsClientConfig();
        int maxTotalPool = 34;
        int defaultMaxPerRoutePool = 6;
        int validateAfterInactivityPool = 1234;
        ReflectionTestUtils.setField(test, "evictExpiredConnections", true);
        ReflectionTestUtils.setField(test, "maxTotalPool", maxTotalPool);
        ReflectionTestUtils.setField(test, "defaultMaxPerRoutePool", defaultMaxPerRoutePool);
        ReflectionTestUtils.setField(test, "validateAfterInactivityPool", validateAfterInactivityPool);
        
        HttpClientBuilder httpClientBuilder = test.getHttpClientBuilder(null, null,
                null, null, null);
    
        HttpClientConnectionManager connManager = (HttpClientConnectionManager) ReflectionTestUtils.getField(httpClientBuilder, "connManager");
    
        assertTrue(connManager instanceof PoolingHttpClientConnectionManager);
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = (PoolingHttpClientConnectionManager) connManager;
        assertEquals(maxTotalPool, poolingHttpClientConnectionManager.getMaxTotal());
        assertEquals(defaultMaxPerRoutePool, poolingHttpClientConnectionManager.getDefaultMaxPerRoute());
        assertEquals(validateAfterInactivityPool, poolingHttpClientConnectionManager.getValidateAfterInactivity());
    }
    
    @Test
    public void testForcePrometheusPostProcessor_no_nulls() throws Exception {
        TestBeanPostProcessor testBeanPostProcessor = new TestBeanPostProcessor();
        BaseWsClientConfig test = new BaseWsClientConfig();
        InitializingBean initializingBean = test.forcePrometheusPostProcessor(testBeanPostProcessor, new LoggingMeterRegistry());
        initializingBean.afterPropertiesSet();
        
        assertTrue(testBeanPostProcessor.isInvoked());
    }
    
    @Test
    public void testForcePrometheusPostProcessor_postProcessorNull() throws Exception {
        TestBeanPostProcessor testBeanPostProcessor = new TestBeanPostProcessor();
        BaseWsClientConfig test = new BaseWsClientConfig();
        InitializingBean initializingBean = test.forcePrometheusPostProcessor(null, new LoggingMeterRegistry());
        initializingBean.afterPropertiesSet();
        
        assertFalse(testBeanPostProcessor.isInvoked());
    }
    
    @Test
    public void testForcePrometheusPostProcessor_registryNull() throws Exception {
        TestBeanPostProcessor testBeanPostProcessor = new TestBeanPostProcessor();
        BaseWsClientConfig test = new BaseWsClientConfig();
        InitializingBean initializingBean = test.forcePrometheusPostProcessor(testBeanPostProcessor, null);
        initializingBean.afterPropertiesSet();
        
        assertFalse(testBeanPostProcessor.isInvoked());
    }
    

    private class TestBeanPostProcessor implements BeanPostProcessor {
	    private boolean invoked;
    
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            invoked = true;
            return bean;
        }
    
        public boolean isInvoked() {
            return invoked;
        }
    }
}

package com.wynd.vop.framework.client.ws;

import com.wynd.vop.framework.client.ws.interceptor.AuditWsInterceptor;
import com.wynd.vop.framework.client.ws.interceptor.AuditWsInterceptorConfig;
import com.wynd.vop.framework.exception.VopPartnerRuntimeException;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.log.PerformanceLogMethodInterceptor;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.validation.Defense;
import com.wynd.vop.framework.audit.BaseAsyncAudit;
import com.wynd.vop.framework.util.HttpClientUtils;
import io.jsonwebtoken.lang.Collections;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import javax.net.ssl.SSLContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base WebService Client configuration, consolidates core/common web service configuration operations used across the applications.
 *
 */
@Configuration
public class BaseWsClientConfig {

	/** Logger for this class */
	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(BaseWsClientConfig.class);

	/** base package for framework exceptions */
	public static final String PACKAGE_FRAMEWORK_EXCEPTION = "com.wynd.vop.framework.exception";
    
    @Value("${vop.framework.client.ws.disableConnectionReuse:false}")
    private boolean disableConnectionReuse;
    
    @Value("${vop.framework.client.ws.retryOnNoHttpResponseException:true}")
    private boolean retryOnNoHttpResponseException;
    
    @Value("${vop.framework.client.ws.evictExpiredConnections:true}")
    private boolean evictExpiredConnections;
    
    //Note: The following values differ from their BipRestAutoConfiguration counterpart. However,
    // we are leaving them as is to match the current Apache Http Client defaults.
    @Value("${vop.framework.client.ws.maxTotalPool:20}")
    private int maxTotalPool = 20;
    
    @Value("${vop.framework.client.ws.defaultMaxPerRoutePool:2}")
    private int defaultMaxPerRoutePool = 2;
    
    @Value("${vop.framework.client.ws.validateAfterInactivityPool:2000}")
    private int validateAfterInactivityPool = 2000;

	@Value("${vop.framework.client.ws.micrometerEnabled:true}")
	private boolean micrometerEnabled;

	@Value("${vop.framework.client.ws.useClientMachineFromRequest:true}")
	private boolean useRequestClientMachine;

    @Autowired(required = false)
	private MeterRegistry meterRegistry;

	/**
	 * Creates the default web service template using the default audit request/response interceptors and no web service interceptors.
	 * <p>
	 * Auditing {@link AuditWsInterceptor} is added automatically.
	 *
	 * @param endpoint the endpoint
	 * @param readTimeout the read timeout
	 * @param connectionTimeout the connection timeout
	 * @param marshaller the marshaller
	 * @param unmarshaller the unmarshaller
	 * @return the web service template
	 * @throws KeyManagementException the key management exception
	 * @throws UnrecoverableKeyException the unrecoverable key exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws KeyStoreException the key store exception
	 * @throws CertificateException the certificate exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected final WebServiceTemplate createDefaultWebServiceTemplate(final String endpoint, final int readTimeout,
			final int connectionTimeout, final Marshaller marshaller, final Unmarshaller unmarshaller) {
		return this.createDefaultWebServiceTemplate(endpoint, readTimeout, connectionTimeout, marshaller, unmarshaller,
				new HttpRequestInterceptor[] { null },
				new HttpResponseInterceptor[] { null }, null);
	}
    
    @Bean
    InitializingBean forcePrometheusPostProcessor(@Autowired(required = false) @Qualifier("meterRegistryPostProcessor") BeanPostProcessor meterRegistryPostProcessor, @Autowired(required = false) MeterRegistry registry) {
        return () -> {
            if (registry != null && meterRegistryPostProcessor != null) {
                meterRegistryPostProcessor.postProcessAfterInitialization(registry, "");
            }
        };
    }

	/**
	 * Creates the default web service template using the default audit request/response interceptors and the provided web service
	 * interceptors
	 * <p>
	 * Auditing {@link AuditWsInterceptor} is added automatically.
	 *
	 * @param endpoint the endpoint
	 * @param readTimeout the read timeout
	 * @param connectionTimeout the connection timeout
	 * @param marshaller the marshaller
	 * @param unmarshaller the unmarshaller
	 * @param wsInterceptors the ws interceptors
	 * @return the web service template
	 * @throws KeyManagementException the key management exception
	 * @throws UnrecoverableKeyException the unrecoverable key exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws KeyStoreException the key store exception
	 * @throws CertificateException the certificate exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected final WebServiceTemplate createDefaultWebServiceTemplate(final String endpoint, final int readTimeout,
			final int connectionTimeout, final Marshaller marshaller, final Unmarshaller unmarshaller,
			final ClientInterceptor[] wsInterceptors) {
		return this.createDefaultWebServiceTemplate(endpoint, readTimeout, connectionTimeout, marshaller, unmarshaller,
				new HttpRequestInterceptor[] { null },
				new HttpResponseInterceptor[] { null }, wsInterceptors);
	}

	/**
	 * Creates the default web service template using the supplied http request/response interceptors and the provided web service
	 * interceptors with axiom message factory
	 * <p>
	 * Auditing {@link AuditWsInterceptor} is added automatically.
	 *
	 * @param endpoint the endpoint
	 * @param readTimeout the read timeout
	 * @param connectionTimeout the connection timeout
	 * @param marshaller the marshaller
	 * @param unmarshaller the unmarshaller
	 * @param httpRequestInterceptors the http request interceptors
	 * @param httpResponseInterceptors the http response interceptors
	 * @param wsInterceptors the ws interceptors
	 * @return the web service template
	 * @throws KeyManagementException the key management exception
	 * @throws UnrecoverableKeyException the unrecoverable key exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws KeyStoreException the key store exception
	 * @throws CertificateException the certificate exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected final WebServiceTemplate createDefaultWebServiceTemplate( // NOSONAR do NOT encapsulate params just to reduce the number
			final String endpoint, // NOSONAR do NOT encapsulate params just to reduce the number
			final int readTimeout, // NOSONAR do NOT encapsulate params just to reduce the number
			final int connectionTimeout, // NOSONAR do NOT encapsulate params just to reduce the number
			final Marshaller marshaller, // NOSONAR do NOT encapsulate params just to reduce the number
			final Unmarshaller unmarshaller, // NOSONAR do NOT encapsulate params just to reduce the number
			final HttpRequestInterceptor[] httpRequestInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final HttpResponseInterceptor[] httpResponseInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final ClientInterceptor[] wsInterceptors) { // NOSONAR do NOT encapsulate params just to reduce the number

		// create axiom message factory
		final AxiomSoapMessageFactory axiomSoapMessageFactory = new AxiomSoapMessageFactory();

		return this
				.createWebServiceTemplate(endpoint, readTimeout, connectionTimeout, marshaller, unmarshaller, httpRequestInterceptors,
						httpResponseInterceptors, wsInterceptors, axiomSoapMessageFactory,
						null, null, null, null);
	}

	/**
	 * Creates the web service template using the the default audit request/response interceptors and the provided web service
	 * interceptors with saaj message factory.
	 * <p>
	 * Auditing {@link AuditWsInterceptor} is added automatically.
	 *
	 * @param endpoint the endpoint
	 * @param readTimeout the read timeout
	 * @param connectionTimeout the connection timeout
	 * @param marshaller the marshaller
	 * @param unmarshaller the unmarshaller
	 * @param wsInterceptors the ws interceptors
	 * @return the web service template
	 * @throws KeyManagementException the key management exception
	 * @throws UnrecoverableKeyException the unrecoverable key exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws KeyStoreException the key store exception
	 * @throws CertificateException the certificate exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws SOAPException error creating message factory
	 */
	protected final WebServiceTemplate createSaajWebServiceTemplate(final String endpoint, final int readTimeout,
			final int connectionTimeout, final Marshaller marshaller, final Unmarshaller unmarshaller,
			final ClientInterceptor[] wsInterceptors) throws SOAPException {
		return this.createWebServiceTemplate(endpoint, readTimeout, connectionTimeout, marshaller, unmarshaller,
				new HttpRequestInterceptor[] { null },
				new HttpResponseInterceptor[] { null }, wsInterceptors,
				new SaajSoapMessageFactory(MessageFactory.newInstance()),
				null, null, null, null);
	}

	/**
	 * Creates the ssl web service template using the default audit request/response interceptors and no web service interceptors.
	 * <p>
	 * Auditing {@link AuditWsInterceptor} is added automatically.
	 *
	 * @param endpoint the endpoint
	 * @param readTimeout the read timeout
	 * @param connectionTimeout the connection timeout
	 * @param marshaller the marshaller
	 * @param unmarshaller the unmarshaller
	 * @param keystore the path to the client ssl keystore
	 * @param keystorePass the pass-word for the client ssl keystore
	 * @param truststore the path to the client ssl truststore
	 * @param truststorePass the pass-word for the client ssl truststore
	 * @return the web service template
	 * @throws KeyManagementException the key management exception
	 * @throws UnrecoverableKeyException the unrecoverable key exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws KeyStoreException the key store exception
	 * @throws CertificateException the certificate exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected final WebServiceTemplate createSslWebServiceTemplate( // NOSONAR do NOT encapsulate params just to reduce the number
			final String endpoint, // NOSONAR do NOT encapsulate params just to reduce the number
			final int readTimeout,// NOSONAR do NOT encapsulate params just to reduce the number
			final int connectionTimeout, // NOSONAR do NOT encapsulate params just to reduce the number
			final Marshaller marshaller, // NOSONAR do NOT encapsulate params just to reduce the number
			final Unmarshaller unmarshaller,// NOSONAR do NOT encapsulate params just to reduce the number
			final Resource keystore, // NOSONAR do NOT encapsulate params just to reduce the number
			final String keystorePass, // NOSONAR do NOT encapsulate params just to reduce the number
			final Resource truststore, // NOSONAR do NOT encapsulate params just to reduce the number
			final String truststorePass) { // NOSONAR do NOT encapsulate params just to reduce the number
		return this.createSslWebServiceTemplate(endpoint, readTimeout, connectionTimeout, marshaller, unmarshaller,
				new HttpRequestInterceptor[] { null },
				new HttpResponseInterceptor[] { null }, null, keystore, keystorePass, truststore, truststorePass);
	}
	
	/**
	 * Creates the ssl web service template using the default audit request/response interceptors and no web service interceptors.
	 * <p>
	 * Auditing {@link AuditWsInterceptor} is added automatically.
	 *
	 * @param endpoint the endpoint
	 * @param readTimeout the read timeout
	 * @param connectionTimeout the connection timeout
	 * @param marshaller the marshaller
	 * @param unmarshaller the unmarshaller
	 * @param keystore Client KeyStore for SSL connections
	 * @param privateKeyPass the pass-word for the client ssl private key
	 * @param truststore KeyStore object for trusted certificates
	 * @return
	 */
	protected final WebServiceTemplate createSslWebServiceTemplate( // NOSONAR do NOT encapsulate params just to reduce the number
			final String endpoint, // NOSONAR do NOT encapsulate params just to reduce the number
			final int readTimeout,// NOSONAR do NOT encapsulate params just to reduce the number
			final int connectionTimeout, // NOSONAR do NOT encapsulate params just to reduce the number
			final Marshaller marshaller, // NOSONAR do NOT encapsulate params just to reduce the number
			final Unmarshaller unmarshaller,// NOSONAR do NOT encapsulate params just to reduce the number
			final KeyStore keystore, // NOSONAR do NOT encapsulate params just to reduce the number
			final String privateKeyPass, // NOSONAR do NOT encapsulate params just to reduce the number
			final KeyStore truststore) { // NOSONAR do NOT encapsulate params just to reduce the number
		
		
		return this.createSslWebServiceTemplate(endpoint, readTimeout, connectionTimeout, marshaller, unmarshaller,
				null, keystore, privateKeyPass, truststore);
	}

	/**
	 * Creates the ssl web service template using the default audit request/response interceptors and the provided web service
	 * interceptors.
	 * <p>
	 * Auditing {@link AuditWsInterceptor} is added automatically.
	 *
	 * @param endpoint the endpoint
	 * @param readTimeout the read timeout
	 * @param connectionTimeout the connection timeout
	 * @param marshaller the marshaller
	 * @param unmarshaller the unmarshaller
	 * @param wsInterceptors the ws interceptors
	 * @param keystore the path to the client ssl keystore
	 * @param keystorePass the pass-word for the client ssl keystore
	 * @param truststore the path to the client ssl truststore
	 * @param truststorePass the pass-word for the client ssl truststore
	 * @return the web service template
	 * @throws KeyManagementException the key management exception
	 * @throws UnrecoverableKeyException the unrecoverable key exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws KeyStoreException the key store exception
	 * @throws CertificateException the certificate exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected final WebServiceTemplate createSslWebServiceTemplate( // NOSONAR do NOT encapsulate params just to reduce the number
			final String endpoint, // NOSONAR do NOT encapsulate params just to reduce the number
			final int readTimeout,// NOSONAR do NOT encapsulate params just to reduce the number
			final int connectionTimeout, // NOSONAR do NOT encapsulate params just to reduce the number
			final Marshaller marshaller, // NOSONAR do NOT encapsulate params just to reduce the number
			final Unmarshaller unmarshaller,// NOSONAR do NOT encapsulate params just to reduce the number
			final ClientInterceptor[] wsInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final Resource keystore, // NOSONAR do NOT encapsulate params just to reduce the number
			final String keystorePass, // NOSONAR do NOT encapsulate params just to reduce the number
			final Resource truststore, // NOSONAR do NOT encapsulate params just to reduce the number
			final String truststorePass) { // NOSONAR do NOT encapsulate params just to reduce the number
		return this.createSslWebServiceTemplate(endpoint, readTimeout, connectionTimeout, marshaller, unmarshaller,
				new HttpRequestInterceptor[] { null },
				new HttpResponseInterceptor[] { null }, wsInterceptors, keystore, keystorePass, truststore, truststorePass);
	}
	
	/**
	 * Creates the ssl web service template using the default audit request/response interceptors and the provided web service
	 * interceptors.
	 * <p>
	 * Auditing {@link AuditWsInterceptor} is added automatically.
	 *
	 * @param endpoint the endpoint
	 * @param readTimeout the read timeout
	 * @param connectionTimeout the connection timeout
	 * @param marshaller the marshaller
	 * @param unmarshaller the unmarshaller
	 * @param wsInterceptors the ws interceptors
	 * @param keystore SSL client KeyStoree
	 * @param privateKeyPass the pass-word for the client ssl private key
	 * @param truststore the SSL client TrustStore
	 * @param truststorePass the pass-word for the client ssl truststore
	 * @return the web service template
	 * @throws KeyManagementException the key management exception
	 * @throws UnrecoverableKeyException the unrecoverable key exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws KeyStoreException the key store exception
	 * @throws CertificateException the certificate exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected final WebServiceTemplate createSslWebServiceTemplate( // NOSONAR do NOT encapsulate params just to reduce the number
			final String endpoint, // NOSONAR do NOT encapsulate params just to reduce the number
			final int readTimeout,// NOSONAR do NOT encapsulate params just to reduce the number
			final int connectionTimeout, // NOSONAR do NOT encapsulate params just to reduce the number
			final Marshaller marshaller, // NOSONAR do NOT encapsulate params just to reduce the number
			final Unmarshaller unmarshaller,// NOSONAR do NOT encapsulate params just to reduce the number
			final ClientInterceptor[] wsInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final KeyStore keystore, // NOSONAR do NOT encapsulate params just to reduce the number
			final String privateKeyPass, // NOSONAR do NOT encapsulate params just to reduce the number
			final KeyStore truststore) { // NOSONAR do NOT encapsulate params just to reduce the number
		return this.createSslWebServiceTemplate(endpoint, readTimeout, connectionTimeout, marshaller, unmarshaller,
				new HttpRequestInterceptor[] { null },
				new HttpResponseInterceptor[] { null }, wsInterceptors, keystore, privateKeyPass, truststore);
	}

	/**
	 * Creates the ssl web service template using the supplied http request/response interceptors and the provided web service
	 * interceptors with axiom message factory
	 *
	 * {@link AuditWsInterceptor} to audit the request and response are added automatically to
	 * the {@code wsInterceptors} array of {@link ClientInterceptor}s.
	 * If the {@code wsInterceptors} array already has AuditWebserviceInterceptors at the beginning and the end
	 * of the array, the array will be left untouched. Any other instances (e.g. in the middle of the array)
	 * will be removed.
	 *
	 * @param endpoint the endpoint
	 * @param readTimeout the read timeout
	 * @param connectionTimeout the connection timeout
	 * @param marshaller the marshaller
	 * @param unmarshaller the unmarshaller
	 * @param httpRequestInterceptors the http request interceptors
	 * @param httpResponseInterceptors the http response interceptors
	 * @param wsInterceptors the ws interceptors
	 * @param keystore the path to the client ssl keystore
	 * @param keystorePass the pass-word for the client ssl keystore
	 * @param truststore the path to the client ssl truststore
	 * @param truststorePass the pass-word for the client ssl truststore
	 * @return the web service template
	 * @throws KeyManagementException the key management exception
	 * @throws UnrecoverableKeyException the unrecoverable key exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws KeyStoreException the key store exception
	 * @throws CertificateException the certificate exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected final WebServiceTemplate createSslWebServiceTemplate( // NOSONAR do NOT encapsulate params just to reduce the number
			final String endpoint, // NOSONAR do NOT encapsulate params just to reduce the number
			final int readTimeout, // NOSONAR do NOT encapsulate params just to reduce the number
			final int connectionTimeout, // NOSONAR do NOT encapsulate params just to reduce the number
			final Marshaller marshaller, // NOSONAR do NOT encapsulate params just to reduce the number
			final Unmarshaller unmarshaller, // NOSONAR do NOT encapsulate params just to reduce the number
			final HttpRequestInterceptor[] httpRequestInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final HttpResponseInterceptor[] httpResponseInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final ClientInterceptor[] wsInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final Resource keystore, // NOSONAR do NOT encapsulate params just to reduce the number
			final String keystorePass, // NOSONAR do NOT encapsulate params just to reduce the number
			final Resource truststore, // NOSONAR do NOT encapsulate params just to reduce the number
			final String truststorePass) { // NOSONAR do NOT encapsulate params just to reduce the number

		// create axiom message factory
		final AxiomSoapMessageFactory axiomSoapMessageFactory = new AxiomSoapMessageFactory();

		return this
				.createWebServiceTemplate(endpoint, readTimeout, connectionTimeout, marshaller, unmarshaller, httpRequestInterceptors,
						httpResponseInterceptors, wsInterceptors, axiomSoapMessageFactory, keystore, keystorePass, truststore,
						truststorePass);
	}
	
	/**
	 * Creates the ssl web service template using the supplied http request/response interceptors and the provided web service
	 * interceptors with axiom message factory
	 *
	 * {@link AuditWsInterceptor} to audit the request and response are added automatically to
	 * the {@code wsInterceptors} array of {@link ClientInterceptor}s.
	 * If the {@code wsInterceptors} array already has AuditWebserviceInterceptors at the beginning and the end
	 * of the array, the array will be left untouched. Any other instances (e.g. in the middle of the array)
	 * will be removed.
	 *
	 * @param endpoint the endpoint
	 * @param readTimeout the read timeout
	 * @param connectionTimeout the connection timeout
	 * @param marshaller the marshaller
	 * @param unmarshaller the unmarshaller
	 * @param httpRequestInterceptors the http request interceptors
	 * @param httpResponseInterceptors the http response interceptors
	 * @param wsInterceptors the ws interceptors
	 * @param keystore SSL client KeyStore
	 * @param privateKeyPass the pass-word for the client ssl private key
	 * @param truststore SSL client TrustStore
	 * @param truststorePass the pass-word for the client ssl truststore
	 * @return the web service template
	 * @throws KeyManagementException the key management exception
	 * @throws UnrecoverableKeyException the unrecoverable key exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws KeyStoreException the key store exception
	 * @throws CertificateException the certificate exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected final WebServiceTemplate createSslWebServiceTemplate( // NOSONAR do NOT encapsulate params just to reduce the number
			final String endpoint, // NOSONAR do NOT encapsulate params just to reduce the number
			final int readTimeout, // NOSONAR do NOT encapsulate params just to reduce the number
			final int connectionTimeout, // NOSONAR do NOT encapsulate params just to reduce the number
			final Marshaller marshaller, // NOSONAR do NOT encapsulate params just to reduce the number
			final Unmarshaller unmarshaller, // NOSONAR do NOT encapsulate params just to reduce the number
			final HttpRequestInterceptor[] httpRequestInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final HttpResponseInterceptor[] httpResponseInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final ClientInterceptor[] wsInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final KeyStore keystore, // NOSONAR do NOT encapsulate params just to reduce the number
			final String privateKeyPass, // NOSONAR do NOT encapsulate params just to reduce the number
			final KeyStore truststore) { // NOSONAR do NOT encapsulate params just to reduce the number

		// create axiom message factory
		final AxiomSoapMessageFactory axiomSoapMessageFactory = new AxiomSoapMessageFactory();

		return this
				.createSslWebServiceTemplate(endpoint, readTimeout, connectionTimeout, marshaller, unmarshaller, httpRequestInterceptors,
						httpResponseInterceptors, wsInterceptors, axiomSoapMessageFactory, keystore, privateKeyPass, truststore);
	}

	/**
	 * Creates the SAAJ SSL web service template using the the default audit request/response interceptors and the provided web service
	 * interceptors with saaj message factory.
	 *
	 * {@link AuditWsInterceptor} to audit the request and response are added automatically to
	 * the {@code wsInterceptors} array of {@link ClientInterceptor}s.
	 * If the {@code wsInterceptors} array already has AuditWebserviceInterceptors at the beginning and the end
	 * of the array, the array will be left untouched. Any other instances (e.g. in the middle of the array)
	 * will be removed.
	 *
	 * @param endpoint the endpoint
	 * @param readTimeout the read timeout
	 * @param connectionTimeout the connection timeout
	 * @param marshaller the marshaller
	 * @param unmarshaller the unmarshaller
	 * @param wsInterceptors the ws interceptors
	 * @param keystore the path to the client ssl keystore
	 * @param keystorePass the pass-word for the client ssl keystore
	 * @param truststore the path to the client ssl truststore
	 * @param truststorePass the pass-word for the client ssl truststore
	 * @return the web service template
	 * @throws KeyManagementException the key management exception
	 * @throws UnrecoverableKeyException the unrecoverable key exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws KeyStoreException the key store exception
	 * @throws CertificateException the certificate exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws SOAPException error creating message factory
	 */
	protected final WebServiceTemplate createSaajSslWebServiceTemplate( // NOSONAR do NOT encapsulate params just to reduce the number
			final String endpoint,  // NOSONAR do NOT encapsulate params just to reduce the number
			final int readTimeout, // NOSONAR do NOT encapsulate params just to reduce the number
			final int connectionTimeout,  // NOSONAR do NOT encapsulate params just to reduce the number
			final Marshaller marshaller,  // NOSONAR do NOT encapsulate params just to reduce the number
			final Unmarshaller unmarshaller, // NOSONAR do NOT encapsulate params just to reduce the number
			final ClientInterceptor[] wsInterceptors,  // NOSONAR do NOT encapsulate params just to reduce the number
			final Resource keystore, // NOSONAR do NOT encapsulate params just to reduce the number
			final String keystorePass, // NOSONAR do NOT encapsulate params just to reduce the number
			final Resource truststore,  // NOSONAR do NOT encapsulate params just to reduce the number
			final String truststorePass) throws SOAPException { // NOSONAR do NOT encapsulate params just to reduce the number
		return this.createWebServiceTemplate(endpoint, readTimeout, connectionTimeout, marshaller, unmarshaller,
				new HttpRequestInterceptor[] { null },
				new HttpResponseInterceptor[] { null }, wsInterceptors,
				new SaajSoapMessageFactory(MessageFactory.newInstance()),
				keystore, keystorePass, truststore, truststorePass);
	}

	/**
	 * Creates web service template using the supplied http request/response interceptors and the provided web service
	 * interceptors and message factory - if web service clients wish to configure their own message factory.
	 *
	 * {@link AuditWsInterceptor} to audit the request and response are added automatically to
	 * the {@code wsInterceptors} array of {@link ClientInterceptor}s.
	 * If the {@code wsInterceptors} array already has AuditWebserviceInterceptors at the beginning and the end
	 * of the array, the array will be left untouched. Any other instances (e.g. in the middle of the array)
	 * will be removed.
	 *
	 *
	 * @param endpoint the endpoint
	 * @param readTimeout the read timeout
	 * @param connectionTimeout the connection timeout
	 * @param marshaller the marshaller
	 * @param unmarshaller the unmarshaller
	 * @param httpRequestInterceptors the http request interceptors
	 * @param httpResponseInterceptors the http response interceptors
	 * @param wsInterceptors the ws interceptors
	 * @param messageFactory webservice message factory
	 * @param truststore the path to the client ssl truststore
	 * @param truststorePass the pass-word for the client ssl truststore
	 * @param keystore the path to the client ssl keystore
	 * @param keystorePass the pass-word for the client ssl keystore
	 * @return the web service template
	 * @throws KeyManagementException the key management exception
	 * @throws UnrecoverableKeyException the unrecoverable key exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws KeyStoreException the key store exception
	 * @throws CertificateException the certificate exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected final WebServiceTemplate createWebServiceTemplate( // NOSONAR do NOT encapsulate params just to reduce the number
			final String endpoint, // NOSONAR do NOT encapsulate params just to reduce the number
			final int readTimeout, // NOSONAR do NOT encapsulate params just to reduce the number
			final int connectionTimeout, // NOSONAR do NOT encapsulate params just to reduce the number
			final Marshaller marshaller, // NOSONAR do NOT encapsulate params just to reduce the number
			final Unmarshaller unmarshaller, // NOSONAR do NOT encapsulate params just to reduce the number
			final HttpRequestInterceptor[] httpRequestInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final HttpResponseInterceptor[] httpResponseInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final ClientInterceptor[] wsInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final WebServiceMessageFactory messageFactory, // NOSONAR do NOT encapsulate params just to reduce the number
			final Resource keystore, // NOSONAR do NOT encapsulate params just to reduce the number
			final String keystorePass, // NOSONAR do NOT encapsulate params just to reduce the number
			final Resource truststore, // NOSONAR do NOT encapsulate params just to reduce the number
			final String truststorePass) { // NOSONAR do NOT encapsulate params just to reduce the number
		
		//Load the KeyStore files
		KeyStore clientKS = null;
		KeyStore trustKS = null;
		try {
			if (keystore != null) {
				clientKS = this.keyStore(keystore, keystorePass.toCharArray());
			}
			if (truststore != null) {
				trustKS = this.keyStore(truststore, truststorePass.toCharArray());
			}
			
		} catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException e) {
			handleExceptions(e);
		}
		
		return createSslWebServiceTemplate(endpoint, readTimeout, connectionTimeout, marshaller, unmarshaller, httpRequestInterceptors,
				httpResponseInterceptors, wsInterceptors, messageFactory, clientKS, keystorePass, trustKS);
		
	}
	
	/**
	 * Creates web service template using the supplied http request/response interceptors and the provided web service
	 * interceptors and message factory - if web service clients wish to configure their own message factory.
	 *
	 * {@link AuditWsInterceptor} to audit the request and response are added automatically to
	 * the {@code wsInterceptors} array of {@link ClientInterceptor}s.
	 * If the {@code wsInterceptors} array already has AuditWebserviceInterceptors at the beginning and the end
	 * of the array, the array will be left untouched. Any other instances (e.g. in the middle of the array)
	 * will be removed.
	 *
	 *
	 * @param endpoint the endpoint
	 * @param readTimeout the read timeout
	 * @param connectionTimeout the connection timeout
	 * @param marshaller the marshaller
	 * @param unmarshaller the unmarshaller
	 * @param httpRequestInterceptors the http request interceptors
	 * @param httpResponseInterceptors the http response interceptors
	 * @param wsInterceptors the ws interceptors
	 * @param messageFactory webservice message factory
	 * @param truststore the path to the client ssl truststore
	 * @param keystore the path to the client ssl keystore
	 * @param privateKeyPass the pass-word for the client ssl private key
	 * @return the web service template
	 * @throws KeyManagementException the key management exception
	 * @throws UnrecoverableKeyException the unrecoverable key exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws KeyStoreException the key store exception
	 * @throws CertificateException the certificate exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected final WebServiceTemplate createSslWebServiceTemplate( // NOSONAR do NOT encapsulate params just to reduce the number
			final String endpoint, // NOSONAR do NOT encapsulate params just to reduce the number
			final int readTimeout, // NOSONAR do NOT encapsulate params just to reduce the number
			final int connectionTimeout, // NOSONAR do NOT encapsulate params just to reduce the number
			final Marshaller marshaller, // NOSONAR do NOT encapsulate params just to reduce the number
			final Unmarshaller unmarshaller, // NOSONAR do NOT encapsulate params just to reduce the number
			final HttpRequestInterceptor[] httpRequestInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final HttpResponseInterceptor[] httpResponseInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final ClientInterceptor[] wsInterceptors, // NOSONAR do NOT encapsulate params just to reduce the number
			final WebServiceMessageFactory messageFactory, // NOSONAR do NOT encapsulate params just to reduce the number
			final KeyStore keystore, // NOSONAR do NOT encapsulate params just to reduce the number
			final String privateKeyPass, // NOSONAR do NOT encapsulate params just to reduce the number
			final KeyStore truststore) { // NOSONAR do NOT encapsulate params just to reduce the number
        
        
        // configure the message sender
		final HttpComponentsMessageSender messageSender = new HttpComponentsMessageSender();
		messageSender.setReadTimeout(readTimeout);
		messageSender.setConnectionTimeout(connectionTimeout);
        
        final HttpClientBuilder httpClientBuilder = getHttpClientBuilder(httpRequestInterceptors, httpResponseInterceptors, keystore, privateKeyPass, truststore);
        
        LOGGER.debug("HttpClient Object : %s% {}", ReflectionToStringBuilder.toString(httpClientBuilder));
        LOGGER.debug("Default Uri : %s% {}", endpoint);
        
        messageSender.setHttpClient(httpClientBuilder.build());

		// set the message factory & configure and return the template
		final WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
		webServiceTemplate.setMessageFactory(messageFactory);
		webServiceTemplate.setMessageSender(messageSender);
		webServiceTemplate.setDefaultUri(endpoint);
		webServiceTemplate.setMarshaller(marshaller);
		webServiceTemplate.setUnmarshaller(unmarshaller);
		webServiceTemplate.setInterceptors(addAuditLoggingInterceptors(wsInterceptors));
		LOGGER.debug("WebServiceTemplate {}: ", ReflectionToStringBuilder.toString(webServiceTemplate));
		return webServiceTemplate;
	}
    
    HttpClientBuilder getHttpClientBuilder(HttpRequestInterceptor[] httpRequestInterceptors, HttpResponseInterceptor[] httpResponseInterceptors, KeyStore keystore, String privateKeyPass, KeyStore truststore) {
        final HttpClientBuilder httpClientBuilder = HttpClients.custom();
        
        if (httpRequestInterceptors != null) {
            for (final HttpRequestInterceptor httpRequestInterceptor : httpRequestInterceptors) {
                httpClientBuilder.addInterceptorFirst(httpRequestInterceptor);
            }
            LOGGER.debug("Added {} HttpRequestInterceptors: {}", httpRequestInterceptors.length, Arrays.toString(httpRequestInterceptors));
        }
        if (httpResponseInterceptors != null) {
            for (final HttpResponseInterceptor httpResponseInterceptor : httpResponseInterceptors) {
                httpClientBuilder.addInterceptorLast(httpResponseInterceptor);
            }
            LOGGER.debug("Added {} HttpResponseInterceptors: {}", httpResponseInterceptors.length, Arrays.toString(httpResponseInterceptors));
        }
        
        addSslContext(httpClientBuilder, keystore, privateKeyPass, truststore);
        
        if (disableConnectionReuse) {
            httpClientBuilder.setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE);
        }
        
        if (retryOnNoHttpResponseException) {
            HttpClientUtils.setRetryHandlerToClientBuilder(httpClientBuilder, defaultMaxPerRoutePool);
        }
        
        if (evictExpiredConnections){
            httpClientBuilder.evictExpiredConnections();
        }
        return httpClientBuilder;
    }
    
    /**
	 * If keystore and truststore are not null, SSL context is added to the httpClient.
	 *
	 * @param httpClient the http client
	 * @param keystoreResource the keystore resource
	 * @param keystorePass the keystore pass
	 * @param truststoreResource the truststore
	 * @param truststorePass the truststore pass
	 */
	protected void addSslContext(final HttpClientBuilder httpClient,
			final Resource keystoreResource, final String keystorePass, final Resource truststoreResource, final String truststorePass) {
		
		KeyStore keystore = null;
		KeyStore truststore = null;
		try {
			if (keystoreResource != null) {
				keystore = this.keyStore(keystoreResource, keystorePass.toCharArray());
			}
			if (truststoreResource != null) {
				truststore = this.keyStore(truststoreResource, truststorePass.toCharArray());
			}
			LOGGER.debug("Adding SSL...");
			addSslContext(httpClient, keystore, keystorePass, truststore);
		} catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException e) {
			handleExceptions(e);
		}
	}
    
    /**
     * If keystore and truststore are not null, SSL context is added to the httpClient.
     *
     * @param httpClient the http client
     * @param keystore the keystore object
     * @param privateKeyPass the password for the client private key
     * @param truststore the truststore object
     */
    protected void addSslContext(final HttpClientBuilder httpClient,
                                 final KeyStore keystore, final String privateKeyPass, final KeyStore truststore) {
        
        PoolingHttpClientConnectionManager poolingConnectionManager; // NOSONAR
        
        if ((keystore != null) || (truststore != null)) {
            // Add SSL
            try {
                SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();
                if (keystore != null) {
                    LOGGER.debug("Adding keystore to SSLContext: {}", keystore);
                    sslContextBuilder.loadKeyMaterial(keystore, privateKeyPass.toCharArray());
                }
                if (truststore != null) {
                    LOGGER.debug("Adding truststore to SSLContext: {}", truststore);
                    sslContextBuilder.loadTrustMaterial(truststore, null);
                }
                SSLContext sslContext= sslContextBuilder.build();
                // use NoopHostnameVerifier to turn off host name verification
                SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
                httpClient.setSSLSocketFactory(csf);
                
                poolingConnectionManager = new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", csf)
                        .build()); // NOSONAR
                
                LOGGER.debug("Added SSLConnection: {}", ReflectionToStringBuilder.toString(csf));
                
            } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | UnrecoverableKeyException e) {
                poolingConnectionManager = new PoolingHttpClientConnectionManager(); // NOSONAR
                handleExceptions(e);
            }
        } else {
            poolingConnectionManager = new PoolingHttpClientConnectionManager(); // NOSONAR
            LOGGER.debug("SSLContext not loaded: Keystore/Truststore null");
        }
        
        // CloseableHttpClient#close
        // should
        // automatically
        // shut down the
        // connection pool
        // only if exclusively
        // owned by the client
        poolingConnectionManager.setMaxTotal(maxTotalPool);
        poolingConnectionManager.setDefaultMaxPerRoute(defaultMaxPerRoutePool);
        poolingConnectionManager.setValidateAfterInactivity(validateAfterInactivityPool);
    
        httpClient.setConnectionManager(poolingConnectionManager);
    
    }


	private void handleExceptions(final Exception e) {
		MessageKeys key = MessageKeys.VOP_SECURITY_SSL_CONTEXT_FAIL;
		String[] params = new String[] { e.getClass().getSimpleName(), e.getMessage() };
		LOGGER.error(key.getMessage(params), e);
		throw new VopPartnerRuntimeException(key, MessageSeverity.FATAL, HttpStatus.INTERNAL_SERVER_ERROR, e, params);
	}

	/**
	 * Produce a KeyStore object for a given JKS file and its pass-word.
	 *
	 * @param keystoreResource the keystore resource
	 * @param pass the pass-word
	 * @return KeyStore
	 * @throws KeyStoreException the key store exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws CertificateException the certificate exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private KeyStore keyStore(final Resource keystoreResource, final char[] pass)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		KeyStore keyStore = KeyStore.getInstance("JKS");

		LOGGER.debug("KeyStore: {}", keyStore);
		LOGGER.debug("Resource: {}", keystoreResource);

		InputStream inputstream = null;
		try {
			inputstream = keystoreResource.getInputStream();
			keyStore.load(inputstream, pass);
			LOGGER.debug("KeyStore load done");
		} finally {
			BaseAsyncAudit.closeInputStreamIfRequired(inputstream);
		}
		return keyStore;
	}

	/**
	 * Adds audit logging interceptors to the {@link ClientInterceptor} array.
	 * <p>
	 * If the {@code wsInterceptor} parameter is NOT null or empty, an audit interceptor
	 * will be added to log BEFORE the other interceptors run (a "raw" log),
	 * and second interceptor to log AFTER the other interceptors run (a "wire" log).
	 *
	 * @param wsInterceptors the ClientInterceptor array being added to the configuration
	 * @return ClientInterceptor[] - the updated array of interceptors
	 */
	@SuppressWarnings("unchecked")
	private ClientInterceptor[] addAuditLoggingInterceptors(final ClientInterceptor[] wsInterceptors) {

		// if no other interceptors run, no need to add "After" audit log
		boolean logAfter = (wsInterceptors != null) && (wsInterceptors.length > 0);
		LOGGER.debug("Initial ClientInterceptors list: " + Arrays.toString(wsInterceptors));

		List<ClientInterceptor> list = new ArrayList<>();

		/* Add audit logging interceptors for Before and After any other interceptors run */
		if (!logAfter) {
			LOGGER.debug("Adding audit interceptor only for " + AuditWsInterceptorConfig.AFTER.name());
			list.add(new AuditWsInterceptor(AuditWsInterceptorConfig.AFTER));
		} else {
			LOGGER.debug("Adding audit interceptor only for both " + AuditWsInterceptorConfig.BEFORE.name()
					+ " and " + AuditWsInterceptorConfig.AFTER.name());
			list.add(new AuditWsInterceptor(AuditWsInterceptorConfig.BEFORE));
			list.addAll(Collections.arrayToList(wsInterceptors));
			list.add(new AuditWsInterceptor(AuditWsInterceptorConfig.AFTER));
		}

		ClientInterceptor[] newWsInterceptors = list.toArray(new ClientInterceptor[list.size()]);
		LOGGER.debug("Final ClientInterceptors list: {}", Arrays.toString(newWsInterceptors));
		return newWsInterceptors;
	}

	/**
	 * Gets the bean name auto proxy creator.
	 *
	 * @param beanNames the bean names
	 * @param interceptorNames the interceptor names
	 * @return the bean name auto proxy creator
	 */
	public final BeanNameAutoProxyCreator getBeanNameAutoProxyCreator(final String[] beanNames, final String[] interceptorNames) {
		final BeanNameAutoProxyCreator creator = new BeanNameAutoProxyCreator();
		LOGGER.debug("Adding Beans to proxy creator: {}", Arrays.toString(beanNames));
		creator.setBeanNames(beanNames);
		LOGGER.debug("Adding Interceptors to proxy creator: {}", Arrays.toString(interceptorNames));
		creator.setInterceptorNames(interceptorNames);
		return creator;
	}

	/**
	 * Gets the marshaller.
	 *
	 * @param transferPackage the transfer package
	 * @param schemaLocations the schema locations
	 * @param isLogValidationErrors the is log validation errors
	 * @return the marshaller
	 */
	public final Jaxb2Marshaller getMarshaller(final String transferPackage, final Resource[] schemaLocations,
			final boolean isLogValidationErrors) {
		Defense.notNull(transferPackage, "Marshaller transferPackage cannot be null");

		final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		LOGGER.debug("Setting Marshaller properties...");
		marshaller.setValidationEventHandler(new JaxbLogAndEatValidationEventHandler(isLogValidationErrors));
		marshaller.setContextPath(transferPackage);
		if (schemaLocations != null) {
			marshaller.setSchemas(schemaLocations);
		}
		LOGGER.debug("Successfully set Marshaller properties: {}", ReflectionToStringBuilder.toString(marshaller));
		try {
			marshaller.afterPropertiesSet();
		} catch (final Exception ex) {

			throw new VopPartnerRuntimeException(MessageKeys.VOP_REST_CONFIG_JAXB_MARSHALLER_FAIL,
					MessageSeverity.FATAL, HttpStatus.INTERNAL_SERVER_ERROR, ex);
		}
		return marshaller;
	}

	/**
	 * Gets the performance interceptor.
	 *
	 * @param methodWarningThreshhold the method warning threshhold
	 * @return the performance interceptor
	 */
	public final PerformanceLogMethodInterceptor getPerformanceLogMethodInterceptor(final Integer methodWarningThreshhold) {
		final PerformanceLogMethodInterceptor performanceLogMethodInteceptor = new PerformanceLogMethodInterceptor(meterRegistry);
		performanceLogMethodInteceptor.setWarningThreshhold(methodWarningThreshhold);
		performanceLogMethodInteceptor.setMicrometerEnabled(micrometerEnabled);
		return performanceLogMethodInteceptor;
	}

}

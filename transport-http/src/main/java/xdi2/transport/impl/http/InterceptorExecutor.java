package xdi2.transport.impl.http;

import java.io.IOException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.messaging.container.interceptor.InterceptorList;
import xdi2.transport.Transport;
import xdi2.transport.exceptions.Xdi2TransportException;
import xdi2.transport.impl.http.interceptor.HttpTransportInterceptor;
import xdi2.transport.registry.impl.uri.UriMessagingContainerMount;

public class InterceptorExecutor {

	private static final Logger log = LoggerFactory.getLogger(InterceptorExecutor.class);

	private InterceptorExecutor() {

	}

	/*
	 * Methods for executing interceptors
	 */

	public static boolean executeHttpTransportInterceptorsGet(InterceptorList<? extends Transport<?, ?>> interceptorList, HttpTransport httpTransport, HttpTransportRequest request, HttpTransportResponse response, UriMessagingContainerMount messagingContainerMount) throws Xdi2TransportException, IOException {

		for (Iterator<HttpTransportInterceptor> httpTransportInterceptors = findHttpTransportInterceptors(interceptorList); httpTransportInterceptors.hasNext(); ) {

			HttpTransportInterceptor httpTransportInterceptor = httpTransportInterceptors.next();

			if (httpTransportInterceptor.skip(null)) {

				if (log.isDebugEnabled()) log.debug("Skipping HTTP transport interceptor " + httpTransportInterceptor.getClass().getSimpleName() + " (GET).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing HTTP transport interceptor " + httpTransportInterceptor.getClass().getSimpleName() + " (GET).");

			if (httpTransportInterceptor.processGetRequest(httpTransport, request, response, messagingContainerMount)) {

				if (log.isDebugEnabled()) log.debug("GET request has been fully handled by interceptor " + httpTransportInterceptor.getClass().getSimpleName() + ".");
				return true;
			}
		}

		return false;
	}

	public static boolean executeHttpTransportInterceptorsPut(InterceptorList<? extends Transport<?, ?>> interceptorList, HttpTransport httpTransport, HttpTransportRequest request, HttpTransportResponse response, UriMessagingContainerMount messagingContainerMount) throws Xdi2TransportException, IOException {

		for (Iterator<HttpTransportInterceptor> httpTransportInterceptors = findHttpTransportInterceptors(interceptorList); httpTransportInterceptors.hasNext(); ) {

			HttpTransportInterceptor httpTransportInterceptor = httpTransportInterceptors.next();

			if (httpTransportInterceptor.skip(null)) {

				if (log.isDebugEnabled()) log.debug("Skipping HTTP transport interceptor " + httpTransportInterceptor.getClass().getSimpleName() + " (PUT).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing HTTP transport interceptor " + httpTransportInterceptor.getClass().getSimpleName() + " (PUT).");

			if (httpTransportInterceptor.processPutRequest(httpTransport, request, response, messagingContainerMount)) {

				if (log.isDebugEnabled()) log.debug("PUT request has been fully handled by interceptor " + httpTransportInterceptor.getClass().getSimpleName() + ".");
				return true;
			}
		}

		return false;
	}

	public static boolean executeHttpTransportInterceptorsPost(InterceptorList<? extends Transport<?, ?>> interceptorList, HttpTransport httpTransport, HttpTransportRequest request, HttpTransportResponse response, UriMessagingContainerMount messagingContainerMount) throws Xdi2TransportException, IOException {

		for (Iterator<HttpTransportInterceptor> httpTransportInterceptors = findHttpTransportInterceptors(interceptorList); httpTransportInterceptors.hasNext(); ) {

			HttpTransportInterceptor httpTransportInterceptor = httpTransportInterceptors.next();

			if (httpTransportInterceptor.skip(null)) {

				if (log.isDebugEnabled()) log.debug("Skipping HTTP transport interceptor " + httpTransportInterceptor.getClass().getSimpleName() + " (POST).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing HTTP transport interceptor " + httpTransportInterceptor.getClass().getSimpleName() + " (POST).");

			if (httpTransportInterceptor.processPostRequest(httpTransport, request, response, messagingContainerMount)) {

				if (log.isDebugEnabled()) log.debug("POST request has been fully handled by interceptor " + httpTransportInterceptor.getClass().getSimpleName() + ".");
				return true;
			}
		}

		return false;
	}

	public static boolean executeHttpTransportInterceptorsDelete(InterceptorList<? extends Transport<?, ?>> interceptorList, HttpTransport httpTransport, HttpTransportRequest request, HttpTransportResponse response, UriMessagingContainerMount messagingContainerMount) throws Xdi2TransportException, IOException {

		for (Iterator<HttpTransportInterceptor> httpTransportInterceptors = findHttpTransportInterceptors(interceptorList); httpTransportInterceptors.hasNext(); ) {

			HttpTransportInterceptor httpTransportInterceptor = httpTransportInterceptors.next();

			if (httpTransportInterceptor.skip(null)) {

				if (log.isDebugEnabled()) log.debug("Skipping HTTP transport interceptor " + httpTransportInterceptor.getClass().getSimpleName() + " (DELETE).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing HTTP transport interceptor " + httpTransportInterceptor.getClass().getSimpleName() + " (DELETE).");

			if (httpTransportInterceptor.processDeleteRequest(httpTransport, request, response, messagingContainerMount)) {

				if (log.isDebugEnabled()) log.debug("DELETE request has been fully handled by interceptor " + httpTransportInterceptor.getClass().getSimpleName() + ".");
				return true;
			}
		}

		return false;
	}

	/*
	 * Methods for finding interceptors
	 */

	public static Iterator<HttpTransportInterceptor> findHttpTransportInterceptors(InterceptorList<? extends Transport<?, ?>> interceptorList) {

		return interceptorList.findInterceptors(HttpTransportInterceptor.class);
	}
}

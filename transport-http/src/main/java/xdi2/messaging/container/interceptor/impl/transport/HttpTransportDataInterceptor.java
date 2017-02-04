package xdi2.messaging.container.interceptor.impl.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.LiteralNode;
import xdi2.core.features.nodetypes.XdiAttribute;
import xdi2.core.features.nodetypes.XdiAttributeSingleton;
import xdi2.core.syntax.XDIAddress;
import xdi2.messaging.Message;
import xdi2.messaging.container.MessagingContainer;
import xdi2.messaging.container.Prototype;
import xdi2.messaging.container.exceptions.Xdi2MessagingException;
import xdi2.messaging.container.execution.ExecutionContext;
import xdi2.messaging.container.execution.ExecutionResult;
import xdi2.messaging.container.interceptor.InterceptorResult;
import xdi2.messaging.container.interceptor.MessageInterceptor;
import xdi2.messaging.container.interceptor.impl.AbstractInterceptor;
import xdi2.transport.TransportRequest;
import xdi2.transport.impl.AbstractTransport;
import xdi2.transport.impl.http.HttpTransportRequest;

/**
 * This interceptor looks for certain features associated with the HTTP transport,
 * e.g. IP address.
 */
public class HttpTransportDataInterceptor extends AbstractInterceptor<MessagingContainer> implements MessageInterceptor, Prototype<HttpTransportDataInterceptor> {

	private static Logger log = LoggerFactory.getLogger(HttpTransportDataInterceptor.class.getName());

	public static final XDIAddress XDI_ADD_IP = XDIAddress.create("<$ip>");

	/*
	 * Prototype
	 */

	@Override
	public HttpTransportDataInterceptor instanceFor(PrototypingContext prototypingContext) throws Xdi2MessagingException {

		// done

		return this;
	}

	/*
	 * MessageInterceptor
	 */

	@Override
	public InterceptorResult before(Message message, ExecutionContext executionContext, ExecutionResult executionResult) throws Xdi2MessagingException {

		// look for HttpTransport, HttpRequest, HttpResponse

		TransportRequest request = AbstractTransport.getRequest(executionContext);
		if (! (request instanceof HttpTransportRequest)) return InterceptorResult.DEFAULT;

		HttpTransportRequest httpRequest = (HttpTransportRequest) request;

		// add <$ip>

		String remoteAddr = httpRequest.getRemoteAddr();

		XdiAttribute ipXdiAttribute = XdiAttributeSingleton.fromContextNode(message.getContextNode().setDeepContextNode(XDI_ADD_IP));
		LiteralNode ipLiteral = ipXdiAttribute.setLiteralString(remoteAddr);

		if (log.isDebugEnabled()) log.debug("IP: " + ipLiteral.getStatement());

		// done

		return InterceptorResult.DEFAULT;
	}

	@Override
	public InterceptorResult after(Message message, ExecutionContext executionContext, ExecutionResult executionResult) throws Xdi2MessagingException {

		return InterceptorResult.DEFAULT;
	}
}

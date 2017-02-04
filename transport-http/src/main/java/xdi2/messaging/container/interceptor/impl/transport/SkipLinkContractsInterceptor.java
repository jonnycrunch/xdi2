package xdi2.messaging.container.interceptor.impl.transport;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.container.Prototype;
import xdi2.messaging.container.exceptions.Xdi2MessagingException;
import xdi2.messaging.container.execution.ExecutionContext;
import xdi2.messaging.container.execution.ExecutionResult;
import xdi2.messaging.container.impl.AbstractMessagingContainer;
import xdi2.messaging.container.interceptor.InterceptorResult;
import xdi2.messaging.container.interceptor.MessageEnvelopeInterceptor;
import xdi2.messaging.container.interceptor.impl.AbstractMessageEnvelopeInterceptor;
import xdi2.messaging.container.interceptor.impl.linkcontract.LinkContractInterceptor;
import xdi2.transport.TransportRequest;
import xdi2.transport.impl.AbstractTransport;
import xdi2.transport.impl.http.HttpTransportRequest;

/**
 * This interceptor will skip the link contract interceptor,
 * if a request comes from a whitelisted IP address.
 */
public class SkipLinkContractsInterceptor extends AbstractMessageEnvelopeInterceptor implements MessageEnvelopeInterceptor, Prototype<SkipLinkContractsInterceptor> {

	private static final Logger log = LoggerFactory.getLogger(SkipLinkContractsInterceptor.class);

	private Set<String> remoteAddrWhiteList;

	/*
	 * Prototype
	 */

	@Override
	public SkipLinkContractsInterceptor instanceFor(PrototypingContext prototypingContext) throws Xdi2MessagingException {

		// done

		return this;
	}

	/*
	 * MessageEnvelopeInterceptor
	 */

	@Override
	public InterceptorResult before(MessageEnvelope messageEnvelope, ExecutionContext executionContext, ExecutionResult executionResult) throws Xdi2MessagingException {

		// look for HttpTransport, HttpRequest, HttpResponse

		TransportRequest request = AbstractTransport.getRequest(executionContext);
		if (! (request instanceof HttpTransportRequest)) return InterceptorResult.DEFAULT;

		HttpTransportRequest httpRequest = (HttpTransportRequest) request;

		String remoteAddr = httpRequest.getHeader("X-Forwarded-Remote-Addr"); 
		if (remoteAddr == null) remoteAddr = httpRequest.getRemoteAddr();

		// whitelist ?

		if (this.getRemoteAddrWhiteList().contains(remoteAddr)) {

			if (log.isDebugEnabled()) log.debug("Whitelisting remote address " + remoteAddr);

			AbstractMessagingContainer messagingContainer = (AbstractMessagingContainer) executionContext.getCurrentMessagingContainer();

			LinkContractInterceptor linkContractInterceptor = messagingContainer.getInterceptors().getInterceptor(LinkContractInterceptor.class);
			if (linkContractInterceptor != null) linkContractInterceptor.setDisabledForMessageEnvelope(messageEnvelope);
		}

		// done

		return InterceptorResult.DEFAULT;
	}

	/*
	 * Getters and setters
	 */

	public Set<String> getRemoteAddrWhiteList() {

		return this.remoteAddrWhiteList;
	}

	public void setRemoteAddrWhiteList(Set<String> remoteAddrWhiteList) {

		this.remoteAddrWhiteList = remoteAddrWhiteList;
	}
}

package xdi2.messaging.target.contributor.impl.proxy;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.XDIClient;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.http.XDIHttpClient;
import xdi2.core.Graph;
import xdi2.core.features.nodetypes.XdiPeerRoot;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.CopyUtil;
import xdi2.core.util.XDIAddressUtil;
import xdi2.core.util.XDIStatementUtil;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.messaging.context.ExecutionContext;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.operations.Operation;
import xdi2.messaging.request.RequestMessage;
import xdi2.messaging.request.RequestMessageEnvelope;
import xdi2.messaging.response.MessagingResponse;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.Prototype;
import xdi2.messaging.target.contributor.AbstractContributor;
import xdi2.messaging.target.contributor.ContributorMount;
import xdi2.messaging.target.contributor.ContributorResult;
import xdi2.messaging.target.contributor.impl.proxy.manipulator.ProxyManipulator;
import xdi2.messaging.target.interceptor.InterceptorResult;
import xdi2.messaging.target.interceptor.MessageInterceptor;
import xdi2.messaging.util.MessagingCloneUtil;

/**
 * This contributor can answer requests by forwarding them to another XDI endpoint.
 */
@ContributorMount(
		contributorAddresses={""}
		)
public class ProxyContributor extends AbstractContributor implements MessageInterceptor, Prototype<ProxyContributor> {

	private static final Logger log = LoggerFactory.getLogger(ProxyContributor.class);

	private XDIArc toPeerRootXDIArc;
	private XDIClient xdiClient;
	private XDIAddress linkContractAddress;

	private XDIDiscoveryClient xdiDiscoveryClient;

	private boolean skipParentContributors;
	private boolean skipSiblingContributors;
	private boolean skipMessagingTarget;
	private List<ProxyManipulator> proxyManipulators;

	public ProxyContributor() {

		this.skipParentContributors = false;
		this.skipSiblingContributors = false;
		this.skipMessagingTarget = true;
		this.proxyManipulators = new ArrayList<ProxyManipulator> ();
	}

	/*
	 * Prototype
	 */

	@Override
	public ProxyContributor instanceFor(xdi2.messaging.target.Prototype.PrototypingContext prototypingContext) throws Xdi2MessagingException {

		// done

		return this;
	}

	/*
	 * Init and shutdown
	 */

	@Override
	public void init(MessagingTarget messagingTarget) throws Exception {

		super.init(messagingTarget);

		// if we don't have an XDI discovery client, use the default one

		if (this.xdiDiscoveryClient == null) {

			this.xdiDiscoveryClient = new XDIDiscoveryClient();
		}

		// if we have a static forwarding target, but no XDI client, use XDI discovery to create one

		if (this.toPeerRootXDIArc != null && this.xdiClient == null) {

			XDIDiscoveryResult xdiDiscoveryResult = this.getXdiDiscoveryClient().discoverFromRegistry(XdiPeerRoot.getXDIAddressOfPeerRootXDIArc(this.toPeerRootXDIArc), null);

			if (xdiDiscoveryResult.getXdiEndpointUrl() == null) throw new RuntimeException("Could not discover XDI endpoint URI for " + this.toPeerRootXDIArc);

			this.xdiClient = new XDIHttpClient(xdiDiscoveryResult.getXdiEndpointUrl());
		}
	}

	/*
	 * MessageInterceptor
	 */

	@Override
	public InterceptorResult before(RequestMessage message, Graph resultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		// if there is a static forwarding target, we use it

		if (this.getToPeerRootXDIArc() != null && this.getXdiClient() != null) {

			XDIArc staticForwardingTargetToPeerRootXDIArc = this.getToPeerRootXDIArc();
			XDIClient staticForwardingTargetXdiClient = this.getXdiClient();
			XDIAddress staticLinkContractAddress = this.getLinkContractAddress();

			if (log.isDebugEnabled()) log.debug("Setting static forwarding target: " + staticForwardingTargetToPeerRootXDIArc + " (" + staticForwardingTargetXdiClient + ") with link contract address " + staticLinkContractAddress);

			putToPeerRootXDIArc(executionContext, staticForwardingTargetToPeerRootXDIArc, this);
			putXdiClient(executionContext, staticForwardingTargetXdiClient, this);
			putLinkContractAddress(executionContext, staticLinkContractAddress, this);

			return InterceptorResult.DEFAULT;
		}

		// no static forwarding target, so we check if the target is self

		MessagingTarget messagingTarget = executionContext.getCurrentMessagingTarget();
		XDIArc ownerPeerRootXDIArc = messagingTarget.getOwnerPeerRootXDIArc();
		XDIArc toPeerRootXDIArc = message.getToPeerRootXDIArc();

		if (toPeerRootXDIArc == null || toPeerRootXDIArc.equals(ownerPeerRootXDIArc)) {

			if (log.isDebugEnabled()) log.debug("Not setting any forwarding target for self request to " + ownerPeerRootXDIArc);

			return InterceptorResult.DEFAULT;
		}

		// no static forwarding target, and target is not self, so we check if the target is local

		/* SKIP THIS FOR NOW, TREAT LOCAL JUST LIKE REMOTE (i.e. use discovery and messaging)

		Transport<?, ?> transport = executionContext.getTransport();

		if (transport instanceof HttpTransport) {

			HttpTransport httpTransport = (HttpTransport) transport;

			MessagingTargetMount messagingTargetMount;

			try {

				messagingTargetMount = httpTransport.getHttpMessagingTargetRegistry().lookup(toPeerRootXDIArc);
			} catch (Xdi2TransportException ex) {

				throw new Xdi2MessagingException("Unable to locally look up messaging target for peer root arc " + toPeerRootXDIArc, ex, executionContext);
			}

			if (messagingTargetMount != null) {

				XDIArc dynamicForwardingTargetToPeerRootXDIArc = toPeerRootXDIArc;
				XDIClient dynamicForwardingTargetXdiClient = new XDILocalClient(messagingTargetMount.getMessagingTarget());
				XDIAddress dynamicLinkContractAddress = message.getLinkContractAddress();

				if (log.isDebugEnabled()) log.debug("Setting dynamic local forwarding target: " + dynamicForwardingTargetToPeerRootXDIArc + " (" + dynamicForwardingTargetXdiClient + ") with link contract address " + dynamicLinkContractAddress);

				putToPeerRootXDIArc(executionContext, dynamicForwardingTargetToPeerRootXDIArc, this);
				putXdiClient(executionContext, dynamicForwardingTargetXdiClient, this);
				putLinkContractAddress(executionContext, dynamicLinkContractAddress, this);

				return InterceptorResult.DEFAULT;
			}
		}

		 */

		// no static forwarding target, and target is not self, and target is not local, so we discover the remote forwarding target dynamically

		XDIDiscoveryResult xdiDiscoveryResult;

		try {

			xdiDiscoveryResult = this.getXdiDiscoveryClient().discoverFromRegistry(XdiPeerRoot.getXDIAddressOfPeerRootXDIArc(toPeerRootXDIArc), null);
		} catch (Xdi2ClientException ex) {

			throw new Xdi2MessagingException("XDI Discovery failed on " + toPeerRootXDIArc + ": " + ex.getMessage(), ex, executionContext);
		}

		if (xdiDiscoveryResult.getCloudNumber() == null) throw new Xdi2MessagingException("Could not discover Cloud Number for forwarding target at " + toPeerRootXDIArc, null, executionContext);
		if (xdiDiscoveryResult.getXdiEndpointUrl() == null) throw new Xdi2MessagingException("Could not discover XDI endpoint URI for forwarding target at " + toPeerRootXDIArc, null, executionContext);

		XDIArc dynamicForwardingTargetToPeerRootXDIArc = toPeerRootXDIArc;
		XDIClient dynamicForwardingTargetXdiClient = new XDIHttpClient(xdiDiscoveryResult.getXdiEndpointUrl());
		XDIAddress dynamicLinkContractAddress = message.getLinkContractXDIAddress();

		if (log.isDebugEnabled()) log.debug("Setting dynamic remote forwarding target: " + dynamicForwardingTargetToPeerRootXDIArc + " (" + dynamicForwardingTargetXdiClient + ") with link contract address " + dynamicLinkContractAddress);

		putToPeerRootXDIArc(executionContext, dynamicForwardingTargetToPeerRootXDIArc, this);
		putXdiClient(executionContext, dynamicForwardingTargetXdiClient, this);
		putLinkContractAddress(executionContext, dynamicLinkContractAddress, this);

		return InterceptorResult.DEFAULT;
	}

	@Override
	public InterceptorResult after(RequestMessage message, Graph resultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		return InterceptorResult.DEFAULT;
	}

	/*
	 * Contributor methods
	 */

	@Override
	public ContributorResult executeOnAddress(XDIAddress[] contributorAddresses, XDIAddress contributorsAddress, XDIAddress relativeTargetAddress, Operation operation, Graph resultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		// check forwarding target

		XDIArc toPeerRootXDIArc = getToPeerRootXDIArc(executionContext, this);
		XDIClient xdiClient = getXdiClient(executionContext, this);
		XDIAddress linkContractAddress = getLinkContractAddress(executionContext, this);

		if (toPeerRootXDIArc == null || xdiClient == null || linkContractAddress == null) return ContributorResult.DEFAULT;

		// prepare the forwarding message envelope

		RequestMessage message = (RequestMessage) operation.getMessage();
		if (log.isDebugEnabled()) log.debug("Preparing message for forwarding: " + message);

		XDIAddress targetAddress = XDIAddressUtil.concatXDIAddresses(contributorsAddress, relativeTargetAddress);

		RequestMessage forwardingMessage = MessagingCloneUtil.cloneMessage(message);

		forwardingMessage.setToPeerRootXDIArc(toPeerRootXDIArc);
		forwardingMessage.setLinkContractXDIAddress(linkContractAddress);
		forwardingMessage.deleteOperations();
		forwardingMessage.createOperation(operation.getOperationXDIAddress(), targetAddress);

		if (log.isDebugEnabled()) log.debug("Prepared message for forwarding: " + forwardingMessage);

		RequestMessageEnvelope forwardingMessageEnvelope = forwardingMessage.getMessageEnvelope();

		// manipulate the forwarding message envelope

		for (ProxyManipulator proxyManipulator : this.proxyManipulators) {

			if (log.isDebugEnabled()) log.debug("Executing proxy manipulator " + proxyManipulator.getClass().getSimpleName() + " with operation " + operation.getOperationXDIAddress() + " on address " + targetAddress + " (message envelope).");

			proxyManipulator.manipulate(forwardingMessageEnvelope, executionContext);
		}

		// send the forwarding message envelope

		MessagingResponse forwardingMessagingResponse;
		Graph forwardingResultGraph;

		try {

			if (log.isDebugEnabled() && xdiClient instanceof XDIHttpClient) log.debug("Forwarding operation " + operation.getOperationXDIAddress() + " on target address " + targetAddress + " to " + ((XDIHttpClient) this.getXdiClient()).getXdiEndpointUrl() + ".");

			forwardingMessagingResponse = xdiClient.send(forwardingMessageEnvelope);
			forwardingResultGraph = forwardingMessagingResponse.getResultGraph();
		} catch (Xdi2ClientException ex) {

			throw new Xdi2MessagingException("Problem while forwarding XDI request: " + ex.getMessage(), ex, executionContext);
		}

		// manipulate the forwarding result graph

		for (ProxyManipulator proxyManipulator : this.proxyManipulators) {

			if (log.isDebugEnabled()) log.debug("Executing proxy manipulator " + proxyManipulator.getClass().getSimpleName() + " with operation " + operation.getOperationXDIAddress() + " on address " + targetAddress + " (message result).");

			proxyManipulator.manipulate(forwardingResultGraph, executionContext);
		}

		if (log.isDebugEnabled()) log.debug("Manipulated result graph from forwarding: " + forwardingResultGraph);

		// done

		CopyUtil.copyGraph(forwardingResultGraph, resultGraph, null);

		return new ContributorResult(this.isSkipParentContributors(), this.isSkipSiblingContributors(), this.isSkipMessagingTarget());
	}

	@Override
	public ContributorResult executeOnStatement(XDIAddress[] contributorAddresses, XDIAddress contributorsAddress, XDIStatement relativeTargetStatement, Operation operation, Graph resultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		// check forwarding target

		XDIArc toPeerRootXDIArc = getToPeerRootXDIArc(executionContext, this);
		XDIClient xdiClient = getXdiClient(executionContext, this);
		XDIAddress linkContractAddress = getLinkContractAddress(executionContext, this);

		if (toPeerRootXDIArc == null || xdiClient == null || linkContractAddress == null) return ContributorResult.DEFAULT;

		// prepare the forwarding message envelope

		RequestMessage message = (RequestMessage) operation.getMessage();
		if (log.isDebugEnabled()) log.debug("Message as a basis for forwarding: " + message);

		XDIStatement targetStatement = XDIStatementUtil.concatXDIStatement(contributorsAddress, relativeTargetStatement);

		RequestMessage forwardingMessage = new RequestMessageEnvelope().createMessage(message.getSenderXDIAddress());

		forwardingMessage.setToPeerRootXDIArc(toPeerRootXDIArc);
		forwardingMessage.setLinkContractXDIAddress(linkContractAddress);
		forwardingMessage.deleteOperations();
		forwardingMessage.createOperation(operation.getOperationXDIAddress(), targetStatement);

		RequestMessageEnvelope forwardingMessageEnvelope = forwardingMessage.getMessageEnvelope();

		if (log.isDebugEnabled()) log.debug("Message envelope for forwarding: " + forwardingMessageEnvelope);

		// manipulate the forwarding message envelope

		for (ProxyManipulator proxyManipulator : this.proxyManipulators) {

			if (log.isDebugEnabled()) log.debug("Executing manipulator " + proxyManipulator.getClass().getSimpleName() + " with operation " + operation.getOperationXDIAddress() + " on statement " + targetStatement + " (message envelope).");

			proxyManipulator.manipulate(forwardingMessageEnvelope, executionContext);
		}

		if (log.isDebugEnabled()) log.debug("Manipulated message envelope for forwarding: " + forwardingMessageEnvelope);

		// send the forwarding message envelope

		MessagingResponse forwardingMessagingResponse;
		Graph forwardingResultGraph;

		try {

			if (log.isDebugEnabled() && this.getXdiClient() instanceof XDIHttpClient) log.debug("Forwarding operation " + operation.getOperationXDIAddress() + " on target statement " + targetStatement + " to " + ((XDIHttpClient) this.getXdiClient()).getXdiEndpointUrl() + ".");

			forwardingMessagingResponse = xdiClient.send(forwardingMessageEnvelope);
			forwardingResultGraph = forwardingMessagingResponse.getResultGraph();
		} catch (Xdi2ClientException ex) {

			throw new Xdi2MessagingException("Problem while forwarding XDI request: " + ex.getMessage(), ex, executionContext);
		}

		// manipulate the forwarding result graph

		for (ProxyManipulator proxyManipulator : this.proxyManipulators) {

			if (log.isDebugEnabled()) log.debug("Executing manipulator " + proxyManipulator.getClass().getSimpleName() + " with operation " + operation.getOperationXDIAddress() + " on statement " + targetStatement + " (message result).");

			proxyManipulator.manipulate(forwardingResultGraph, executionContext);
		}

		if (log.isDebugEnabled()) log.debug("Manipulated result graph from forwarding: " + forwardingResultGraph);

		// done

		CopyUtil.copyGraph(forwardingResultGraph, resultGraph, null);

		return new ContributorResult(this.isSkipParentContributors(), this.isSkipSiblingContributors(), this.isSkipMessagingTarget());
	}

	/*
	 * Getters and setters
	 */

	public XDIArc getToPeerRootXDIArc() {

		return this.toPeerRootXDIArc;
	}

	public void setToPeerRootXDIArc(XDIArc toPeerRootXDIArc) {

		this.toPeerRootXDIArc = toPeerRootXDIArc;
	}

	public XDIClient getXdiClient() {

		return this.xdiClient;
	}

	public void setXdiClient(XDIClient xdiClient) {

		this.xdiClient = xdiClient;
	}

	public XDIAddress getLinkContractAddress() {

		return this.linkContractAddress;
	}

	public void setLinkContractAddress(XDIAddress linkContractAddress) {

		this.linkContractAddress = linkContractAddress;
	}

	public XDIDiscoveryClient getXdiDiscoveryClient() {

		return this.xdiDiscoveryClient;
	}

	public void setXdiDiscoveryClient(XDIDiscoveryClient xdiDiscoveryClient) {

		this.xdiDiscoveryClient = xdiDiscoveryClient;
	}

	public boolean isSkipParentContributors() {

		return this.skipParentContributors;
	}

	public void setSkipParentContributors(boolean skipParentContributors) {

		this.skipParentContributors = skipParentContributors;
	}

	public boolean isSkipSiblingContributors() {

		return this.skipSiblingContributors;
	}

	public void setSkipSiblingContributors(boolean skipSiblingContributors) {

		this.skipSiblingContributors = skipSiblingContributors;
	}

	public boolean isSkipMessagingTarget() {

		return this.skipMessagingTarget;
	}

	public void setSkipMessagingTarget(boolean skipMessagingTarget) {

		this.skipMessagingTarget = skipMessagingTarget;
	}

	public List<ProxyManipulator> getProxyManipulators() {

		return this.proxyManipulators;
	}

	public void setProxyManipulators(List<ProxyManipulator> proxyManipulators) {

		this.proxyManipulators = proxyManipulators;
	}

	/*
	 * ExecutionContext helper methods
	 */

	private static final String EXECUTIONCONTEXT_KEY_TO_PEER_ROOT_ARC_PER_MESSAGE = ProxyContributor.class.getCanonicalName() + "#topeerrootarcpermessage";
	private static final String EXECUTIONCONTEXT_KEY_XDI_CLIENT_PER_MESSAGE = ProxyContributor.class.getCanonicalName() + "#xdiclientpermessage";
	private static final String EXECUTIONCONTEXT_KEY_LINK_CONTRACT_ADDRESS_PER_MESSAGE = ProxyContributor.class.getCanonicalName() + "#linkcontractaddress";

	public static XDIArc getToPeerRootXDIArc(ExecutionContext executionContext, ProxyContributor proxyContributor) {

		return (XDIArc) executionContext.getMessageAttribute(EXECUTIONCONTEXT_KEY_TO_PEER_ROOT_ARC_PER_MESSAGE + Integer.toString(System.identityHashCode(proxyContributor)));
	}

	public static void putToPeerRootXDIArc(ExecutionContext executionContext, XDIArc toPeerRootXDIArc, ProxyContributor proxyContributor) {

		executionContext.putMessageAttribute(EXECUTIONCONTEXT_KEY_TO_PEER_ROOT_ARC_PER_MESSAGE + Integer.toString(System.identityHashCode(proxyContributor)), toPeerRootXDIArc);
	}

	public static XDIClient getXdiClient(ExecutionContext executionContext, ProxyContributor proxyContributor) {

		return (XDIClient) executionContext.getMessageAttribute(EXECUTIONCONTEXT_KEY_XDI_CLIENT_PER_MESSAGE + Integer.toString(System.identityHashCode(proxyContributor)));
	}

	public static void putXdiClient(ExecutionContext executionContext, XDIClient xdiClient, ProxyContributor proxyContributor) {

		executionContext.putMessageAttribute(EXECUTIONCONTEXT_KEY_XDI_CLIENT_PER_MESSAGE + Integer.toString(System.identityHashCode(proxyContributor)), xdiClient);
	}

	public static XDIAddress getLinkContractAddress(ExecutionContext executionContext, ProxyContributor proxyContributor) {

		return (XDIAddress) executionContext.getMessageAttribute(EXECUTIONCONTEXT_KEY_LINK_CONTRACT_ADDRESS_PER_MESSAGE + Integer.toString(System.identityHashCode(proxyContributor)));
	}

	public static void putLinkContractAddress(ExecutionContext executionContext, XDIAddress linkContractAddress, ProxyContributor proxyContributor) {

		executionContext.putMessageAttribute(EXECUTIONCONTEXT_KEY_LINK_CONTRACT_ADDRESS_PER_MESSAGE + Integer.toString(System.identityHashCode(proxyContributor)), linkContractAddress);
	}
}

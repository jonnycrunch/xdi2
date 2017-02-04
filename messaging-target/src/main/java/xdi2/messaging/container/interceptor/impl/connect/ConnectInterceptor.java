package xdi2.messaging.container.interceptor.impl.connect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.agent.XDIAgent;
import xdi2.agent.impl.XDIBasicAgent;
import xdi2.client.exceptions.Xdi2AgentException;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.manipulator.Manipulator;
import xdi2.client.manipulator.impl.SetLinkContractMessageManipulator;
import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.constants.XDILinkContractConstants;
import xdi2.core.features.index.Index;
import xdi2.core.features.linkcontracts.LinkContractTemplates;
import xdi2.core.features.linkcontracts.instance.LinkContract;
import xdi2.core.features.linkcontracts.instance.PublicLinkContract;
import xdi2.core.features.linkcontracts.instantiation.LinkContractInstantiation;
import xdi2.core.features.linkcontracts.template.LinkContractTemplate;
import xdi2.core.features.nodetypes.XdiEntityCollection;
import xdi2.core.features.nodetypes.XdiEntitySingleton;
import xdi2.core.features.nodetypes.XdiInnerRoot;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.util.CopyUtil;
import xdi2.core.util.iterators.IteratorListMaker;
import xdi2.messaging.container.MessagingContainer;
import xdi2.messaging.container.Prototype;
import xdi2.messaging.container.exceptions.Xdi2MessagingException;
import xdi2.messaging.container.execution.ExecutionContext;
import xdi2.messaging.container.interceptor.InterceptorResult;
import xdi2.messaging.container.interceptor.OperationInterceptor;
import xdi2.messaging.container.interceptor.impl.AbstractInterceptor;
import xdi2.messaging.container.interceptor.impl.defer.DeferResultInterceptor;
import xdi2.messaging.operations.ConnectOperation;
import xdi2.messaging.operations.Operation;

/**
 * This interceptor can process $connect operations.
 */
public class ConnectInterceptor extends AbstractInterceptor<MessagingContainer> implements OperationInterceptor, Prototype<ConnectInterceptor> {

	private static final Logger log = LoggerFactory.getLogger(ConnectInterceptor.class);

	private Graph targetGraph;
	private XDIAgent xdiAgent;
	private Collection<Manipulator> manipulators;

	public ConnectInterceptor(Graph targetGraph, XDIAgent xdiAgent, Collection<Manipulator> manipulators) {

		this.targetGraph = targetGraph;
		this.xdiAgent = xdiAgent;
		this.manipulators = manipulators;
	}

	public ConnectInterceptor() {

		this(null, new XDIBasicAgent(), new ArrayList<Manipulator> ());
	}

	/*
	 * Prototype
	 */

	@Override
	public ConnectInterceptor instanceFor(PrototypingContext prototypingContext) throws Xdi2MessagingException {

		// done

		return this;
	}

	/*
	 * OperationInterceptor
	 */

	@Override
	public InterceptorResult before(Operation operation, Graph operationResultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		// check operation

		if (! (operation instanceof ConnectOperation)) return InterceptorResult.DEFAULT;
		if (DeferResultInterceptor.hasDeferResult(executionContext, operation.getMessage())) return InterceptorResult.DEFAULT;

		// get link contract template(s)

		List<LinkContractTemplate> linkContractTemplates = this.getLinkContractTemplates(operation, executionContext);
		if (log.isDebugEnabled()) log.debug("Trying to instantiate " + linkContractTemplates.size() + " link contract templates.");

		// connect

		for (LinkContractTemplate linkContractTemplate : linkContractTemplates) {

			this.processConnect(linkContractTemplate, (ConnectOperation) operation, operationResultGraph, executionContext);
		}

		// done

		return InterceptorResult.SKIP_MESSAGING_TARGET;
	}

	@Override
	public InterceptorResult after(Operation operation, Graph operationResultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		// done

		return InterceptorResult.DEFAULT;
	}

	/*
	 * Helper methods
	 */

	public List<LinkContractTemplate> getLinkContractTemplates(Operation operation, ExecutionContext executionContext) throws Xdi2MessagingException {

		List<LinkContractTemplate> linkContractTemplates = ConnectInterceptor.getLinkContractTemplates(executionContext);
		if (linkContractTemplates != null) return linkContractTemplates;

		linkContractTemplates = new ArrayList<LinkContractTemplate> ();

		if (operation.getTargetXDIAddress() != null) linkContractTemplates.add(this.linkContractTemplateFromTargetXDIAddress(operation.getTargetXDIAddress(), executionContext));
		if (operation.getTargetXdiInnerRoot() != null) linkContractTemplates.addAll(this.linkContractTemplatesFromTargetXdiInnerRoot(operation.getTargetXdiInnerRoot(), executionContext));
		if (linkContractTemplates.isEmpty()) throw new Xdi2MessagingException("No link contract template(s) in operation " + operation, null, executionContext);

		ConnectInterceptor.putLinkContractTemplates(executionContext, linkContractTemplates);

		return linkContractTemplates;
	}

	private LinkContractTemplate linkContractTemplateFromTargetXDIAddress(XDIAddress targetXDIAddress, ExecutionContext executionContext) throws Xdi2MessagingException {

		// use agent to obtain link contract template

		XDIAddress linkContractTemplateXDIaddress = targetXDIAddress;

		ContextNode linkContractTemplateContextNode;

		try {

			// add manipulators

			Collection<Manipulator> manipulators = new ArrayList<Manipulator> ();
			// TODO: is it okay that we set the public link contract here, or are we supposed to rely on the XDIAgent's configuration for that?
			manipulators.add(new SetLinkContractMessageManipulator(PublicLinkContract.class));
			if (this.getManipulators() != null) manipulators.addAll(this.getManipulators());

			// get

			linkContractTemplateContextNode = this.getXdiAgent().get(linkContractTemplateXDIaddress, manipulators);
		} catch (Xdi2AgentException ex) {

			throw new Xdi2MessagingException("Agent problem while getting link contract template at address " + targetXDIAddress + ": " + ex.getMessage(), ex, executionContext);
		} catch (Xdi2ClientException ex) {

			throw new Xdi2MessagingException("Client problem while getting link contract template at address " + targetXDIAddress + ": " + ex.getMessage(), ex, executionContext);
		}

		// read link contract template

		if (linkContractTemplateContextNode == null) throw new Xdi2MessagingException("Cannot get link contract template at address " + targetXDIAddress, null, executionContext);

		XdiEntitySingleton.Variable linkContractTemplateXdiVariable = XdiEntitySingleton.Variable.fromContextNode(linkContractTemplateContextNode);
		if (linkContractTemplateXdiVariable == null) throw new Xdi2MessagingException("Invalid link contract template context node at address " + targetXDIAddress, null, executionContext);

		LinkContractTemplate linkContractTemplate = LinkContractTemplate.fromXdiEntitySingletonVariable(linkContractTemplateXdiVariable);
		if (linkContractTemplate == null) throw new Xdi2MessagingException("Invalid link contract template at address " + targetXDIAddress, null, executionContext);

		// done

		return linkContractTemplate;
	}

	private List<LinkContractTemplate> linkContractTemplatesFromTargetXdiInnerRoot(XdiInnerRoot targetXdiInnerRoot, ExecutionContext executionContext) throws Xdi2MessagingException {

		// get the inner graph

		Graph innerGraph = targetXdiInnerRoot.getInnerGraph();

		// return link contract templates

		return new IteratorListMaker<LinkContractTemplate> (LinkContractTemplates.getAllLinkContractTemplates(innerGraph)).list();
	}

	private void processConnect(LinkContractTemplate linkContractTemplate, ConnectOperation operation, Graph operationResultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		if (log.isDebugEnabled()) log.debug("Preparing to instantiate link contract template " + linkContractTemplate);

		// set variable values

		Map<XDIArc, Object> variableValues = operation.getVariableValues();

		variableValues.put(XDILinkContractConstants.XDI_ARC_V_FROM, operation.getMessage().getSenderXDIAddress());
		variableValues.put(XDILinkContractConstants.XDI_ARC_V_TO, operation.getMessage().getToXDIAddress());

		// instantiate link contract

		LinkContractInstantiation linkContractInstantiation = new LinkContractInstantiation(linkContractTemplate);
		linkContractInstantiation.setVariableValues(variableValues);

		LinkContract linkContract;

		try {

			linkContract = linkContractInstantiation.execute();
		} catch (Exception ex) {

			throw new Xdi2MessagingException("Cannot instantiate link contract: " + ex.getMessage(), ex, executionContext);
		}

		// write link contract and index into operation result graph

		CopyUtil.copyGraph(linkContract.getContextNode().getGraph(), operationResultGraph, null);

		// first delete link contract in target graph if it exists already

		ContextNode linkContractContextNode = this.getTargetGraph(executionContext).getDeepContextNode(linkContract.getContextNode().getXDIAddress());
		if (linkContractContextNode != null) linkContractContextNode.delete();

		// write link contract and index into target graph

		if (this.getTargetGraph(executionContext) != null) {

			CopyUtil.copyGraph(linkContract.getContextNode().getGraph(), this.getTargetGraph(executionContext), null);
			XdiEntityCollection xdiLinkContractIndex = Index.getEntityIndex(this.getTargetGraph(executionContext), XDILinkContractConstants.XDI_ARC_CONTRACT, true);
			Index.setEntityIndexAggregation(xdiLinkContractIndex, linkContract.getXdiEntity());
		}
	}

	/*
	 * Getters and setters
	 */

	public Graph getTargetGraph(ExecutionContext executionContext) {

		Graph targetGraph = this.getTargetGraph();
		if (targetGraph == null) targetGraph = executionContext.getCurrentGraph();
		if (targetGraph == null) throw new NullPointerException("No target graph.");

		return targetGraph;
	}

	public Graph getTargetGraph() {

		return this.targetGraph;
	}

	public void setTargetGraph(Graph targetGraph) {

		this.targetGraph = targetGraph;
	}

	public XDIAgent getXdiAgent() {

		return this.xdiAgent;
	}

	public void setXdiAgent(XDIAgent xdiAgent) {

		this.xdiAgent = xdiAgent;
	}

	public Collection<Manipulator> getManipulators() {

		return this.manipulators;
	}

	public void setManipulators(Collection<Manipulator> manipulators) {

		this.manipulators = manipulators;
	}

	/*
	 * ExecutionContext helper methods
	 */

	private static final String EXECUTIONCONTEXT_KEY_LINKCONTRACTTEMPLATES_PER_OPERATION = ConnectInterceptor.class.getCanonicalName() + "#linkcontracttemplatesperoperation";

	@SuppressWarnings("unchecked")
	public static List<LinkContractTemplate> getLinkContractTemplates(ExecutionContext executionContext) {

		return (List<LinkContractTemplate>) executionContext.getOperationAttribute(EXECUTIONCONTEXT_KEY_LINKCONTRACTTEMPLATES_PER_OPERATION);
	}

	public static void putLinkContractTemplates(ExecutionContext executionContext, List<LinkContractTemplate> linkContractTemplates) {

		executionContext.putOperationAttribute(EXECUTIONCONTEXT_KEY_LINKCONTRACTTEMPLATES_PER_OPERATION, linkContractTemplates);
	}
}

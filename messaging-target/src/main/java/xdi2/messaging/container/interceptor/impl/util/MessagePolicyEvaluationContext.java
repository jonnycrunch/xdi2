package xdi2.messaging.container.interceptor.impl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.Statement;
import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.core.features.nodetypes.XdiInnerRoot;
import xdi2.core.features.nodetypes.XdiPeerRoot;
import xdi2.core.features.policy.evaluation.PolicyEvaluationContext;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.XDIAddressUtil;
import xdi2.messaging.Message;

public class MessagePolicyEvaluationContext implements PolicyEvaluationContext {

	private static final Logger log = LoggerFactory.getLogger(MessagePolicyEvaluationContext.class);

	public static final XDIArc XDI_ARC_FROM_VARIABLE = XDIArc.create("{$from}");
	public static final XDIArc XDI_ARC_FROM_PEER_VARIABLE = XDIArc.create("{($from)}");
	public static final XDIArc XDI_ARC_MSG_VARIABLE = XDIArc.create("{$msg}");

	public static final XDIAddress XDI_ADD_FROM_VARIABLE = XDIAddress.fromComponent(XDI_ARC_FROM_VARIABLE);
	public static final XDIAddress XDI_ADD_FROM_PEER_VARIABLE = XDIAddress.fromComponent(XDI_ARC_FROM_PEER_VARIABLE);
	public static final XDIAddress XDI_ADD_MSG_VARIABLE = XDIAddress.fromComponent(XDI_ARC_MSG_VARIABLE);

	private Message message;
	private Graph targetGraph;

	public MessagePolicyEvaluationContext(Message message, Graph targetGraph) {

		this.message = message;
		this.targetGraph = targetGraph;
	}

	@Override
	public XDIAddress resolveXDIAddress(XDIAddress contextNodeXDIAddress) {

		XDIAddress resolvedContextNodeXDIAddress = contextNodeXDIAddress;

		resolvedContextNodeXDIAddress = XDIAddressUtil.replaceXDIAddress(resolvedContextNodeXDIAddress, XDI_ARC_FROM_VARIABLE, this.getMessage().getSenderXDIAddress());
		resolvedContextNodeXDIAddress = XDIAddressUtil.replaceXDIAddress(resolvedContextNodeXDIAddress, XDI_ARC_FROM_PEER_VARIABLE, this.getMessage().getFromPeerRootXDIArc());
		resolvedContextNodeXDIAddress = XDIAddressUtil.replaceXDIAddress(resolvedContextNodeXDIAddress, XDI_ARC_MSG_VARIABLE, this.getMessage().getContextNode().getXDIAddress());

		if (log.isTraceEnabled()) log.trace("resolveXDIAddress(" + contextNodeXDIAddress + ") --> " + resolvedContextNodeXDIAddress);

		return resolvedContextNodeXDIAddress;
	}

	@Override
	public ContextNode getContextNode(XDIAddress nodeXDIAddress) {

		Graph resolvedGraph = this.resolveGraph(nodeXDIAddress);
		XDIAddress resolvedcontextNodeXDIAddress = this.resolveXDIAddress(nodeXDIAddress);
		ContextNode resolvedContextNode = resolvedGraph.getDeepContextNode(resolvedcontextNodeXDIAddress, false);

		if (log.isTraceEnabled()) log.trace("getContextNode(" + nodeXDIAddress + ") --> " + resolvedcontextNodeXDIAddress + " --> " + resolvedContextNode);

		return resolvedContextNode;
	}

	@Override
	public Statement getStatement(XDIStatement XDIstatement) {

		XDIAddress contextNodeXDIAddress = XDIstatement.getContextNodeXDIAddress();
		Graph resolvedGraph = this.resolveGraph(contextNodeXDIAddress);
		XDIAddress resolvedcontextNodeXDIAddress = this.resolveXDIAddress(contextNodeXDIAddress);

		XDIStatement resolvedStatementAddress;

		if (XDIstatement.isContextNodeStatement()) {

			XDIArc contextNodeXDIArc = XDIstatement.getContextNodeXDIArc();

			resolvedStatementAddress = XDIStatement.fromContextNodeComponents(
					resolvedcontextNodeXDIAddress, 
					contextNodeXDIArc);
		} else if (XDIstatement.isRelationStatement()) {

			XDIAddress relationAddress = XDIstatement.getRelationXDIAddress();
			XDIAddress targetXDIAddress = XDIstatement.getTargetXDIAddress();

			XDIAddress resolvedtargetXDIAddress = this.resolveXDIAddress(targetXDIAddress);

			resolvedStatementAddress = XDIStatement.fromRelationComponents(
					resolvedcontextNodeXDIAddress, 
					relationAddress, 
					resolvedtargetXDIAddress);
		} else if (XDIstatement.isLiteralStatement()) {

			Object literalData = XDIstatement.getLiteralData();

			resolvedStatementAddress = XDIStatement.fromLiteralComponents(
					resolvedcontextNodeXDIAddress, 
					literalData);
		} else {

			throw new Xdi2RuntimeException("Unexpected statement: " + XDIstatement);
		}

		Statement resolvedStatement = resolvedGraph.getStatement(resolvedStatementAddress);

		if (log.isTraceEnabled()) log.trace("getStatement(" + XDIstatement + ") --> " + resolvedStatementAddress + " --> " + resolvedStatement);

		return resolvedStatement;
	}

	/*
	 * Helper methods
	 */

	private Graph resolveGraph(XDIAddress contextNodeXDIAddress) {

		XDIArc firstArc = contextNodeXDIAddress.getFirstXDIArc();

		if (XdiPeerRoot.isValidXDIArc(firstArc)) {

			firstArc = XdiPeerRoot.getXDIAddressOfPeerRootXDIArc(firstArc).getFirstXDIArc();
		} else if (XdiInnerRoot.isValidXDIArc(firstArc)) {

			firstArc = XdiInnerRoot.getSubjectOfInnerRootXDIArc(firstArc).getFirstXDIArc();
		}

		Graph resolvedGraph = null;

		if (XDI_ARC_MSG_VARIABLE.equals(firstArc)) {

			resolvedGraph = this.getMessage().getContextNode().getGraph();
		} else if (XDI_ARC_FROM_VARIABLE.equals(firstArc)) {

			resolvedGraph = this.getTargetGraph();
		} else if (XDI_ARC_FROM_PEER_VARIABLE.equals(firstArc)) {

			resolvedGraph = this.getTargetGraph();
		} else {

			resolvedGraph = this.getTargetGraph();
		}

		if (log.isTraceEnabled()) log.trace("getGraph(" + contextNodeXDIAddress + ") --> " + resolvedGraph);

		return resolvedGraph;
	}

	/*
	 * Getters and setters
	 */

	public Message getMessage() {

		return this.message;
	}

	public Graph getTargetGraph() {

		return this.targetGraph;
	}
}

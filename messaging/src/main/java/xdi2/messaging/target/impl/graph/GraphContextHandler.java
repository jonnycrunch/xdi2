package xdi2.messaging.target.impl.graph;

import java.util.Iterator;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.LiteralNode;
import xdi2.core.Node;
import xdi2.core.Relation;
import xdi2.core.constants.XDIConstants;
import xdi2.core.features.nodetypes.XdiCommonRoot;
import xdi2.core.features.nodetypes.XdiInnerRoot;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.CopyUtil;
import xdi2.core.util.VariableUtil;
import xdi2.core.util.XDIAddressUtil;
import xdi2.messaging.DelOperation;
import xdi2.messaging.GetOperation;
import xdi2.messaging.MessageResult;
import xdi2.messaging.SetOperation;
import xdi2.messaging.context.ExecutionContext;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.impl.AbstractContextHandler;

public class GraphContextHandler extends AbstractContextHandler {

	private Graph graph;

	public GraphContextHandler(Graph graph) {

		super();

		this.graph = graph;
	}

	public Graph getGraph() {

		return this.graph;
	}

	/*
	 * Operations on statements
	 */

	@Override
	public void executeSetOnStatement(XDIStatement targetStatement, SetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		this.getGraph().setStatement(targetStatement);
	}

	/*
	 * Operations on addresses
	 */

	@Override
	public void executeGetOnAddress(XDIAddress targetXDIAddress, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		Node node = this.getGraph().getDeepNode(targetXDIAddress, true);
		if (node == null) return;

		if (node instanceof ContextNode) {

			CopyUtil.copyContextNode((ContextNode) node, messageResult.getGraph(), null);

			for (XdiInnerRoot xdiInnerRoot : XdiCommonRoot.findCommonRoot(messageResult.getGraph()).getInnerRoots()) {

				node = this.getGraph().getDeepContextNode(xdiInnerRoot.getContextNode().getXDIAddress(), true);

				CopyUtil.copyContextNode((ContextNode) node, messageResult.getGraph(), null);
			}
		} else if (node instanceof LiteralNode) {

			CopyUtil.copyLiteralNode((LiteralNode) node, messageResult.getGraph(), null);
		}
	}

	@Override
	public void executeSetOnAddress(XDIAddress targetXDIAddress, SetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		this.getGraph().setDeepNode(targetXDIAddress);
	}

	@Override
	public void executeDelOnAddress(XDIAddress targetXDIAddress, DelOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		if (XDIConstants.XDI_ADD_ROOT.equals(targetXDIAddress)) {

			this.getGraph().clear();
		} else if (targetXDIAddress.getNumXDIArcs() == 1) {

			this.getGraph().getRootContextNode(false).delContextNode(targetXDIAddress.getFirstXDIArc());
		} else {

			XDIAddress parentContextNodeXDIAddress = XDIAddressUtil.parentXDIAddress(targetXDIAddress, -1);
			XDIArc XDIarc = targetXDIAddress.getLastXDIArc();

			ContextNode parentContextNode = this.getGraph().getDeepContextNode(parentContextNodeXDIAddress, false);
			if (parentContextNode == null) return;

			if (XDIConstants.XDI_ARC_LITERAL.equals(XDIarc)) {

				parentContextNode.delLiteralNode();
			} else {

				parentContextNode.delContextNode(XDIarc);
			}
		}
	}

	/*
	 * Operations on context node statements
	 */

	@Override
	public void executeGetOnContextNodeStatement(XDIStatement contextNodeStatement, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		XDIAddress targetXDIAddress = contextNodeStatement.getTargetXDIAddress();

		ContextNode contextNode = this.getGraph().getDeepContextNode(targetXDIAddress, false);
		if (contextNode == null) return;

		CopyUtil.copyStatement(contextNode.getStatement(), messageResult.getGraph(), null);
	}

	/*
	 * Operations on relation statements
	 */

	@Override
	public void executeGetOnRelationStatement(XDIStatement relationStatement, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		XDIAddress contextNodeXDIAddress = relationStatement.getContextNodeXDIAddress();
		XDIAddress relationXDIaddress = relationStatement.getRelationXDIAddress();
		XDIAddress targetXDIAddress = relationStatement.getTargetXDIAddress();

		ContextNode contextNode = this.getGraph().getDeepContextNode(contextNodeXDIAddress);
		if (contextNode == null) return;

		if (VariableUtil.isVariable(targetXDIAddress)) {

			Iterator<Relation> relations;

			if (VariableUtil.isVariable(relationXDIaddress)) {

				relations = contextNode.getRelations();
			} else {

				relations = contextNode.getRelations(relationXDIaddress);
			}

			while (relations.hasNext()) CopyUtil.copyStatement(relations.next().getStatement(), messageResult.getGraph(), null);
		} else {

			Relation relation = contextNode.getRelation(relationXDIaddress, targetXDIAddress);
			if (relation == null) return;

			CopyUtil.copyStatement(relation.getStatement(), messageResult.getGraph(), null);
		}
	}

	@Override
	public void executeSetOnRelationStatement(XDIStatement relationStatement, SetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		XDIAddress contextNodeXDIAddress = relationStatement.getContextNodeXDIAddress();
		XDIAddress relationXDIaddress = relationStatement.getRelationXDIAddress();
		XDIAddress targetXDIAddress = relationStatement.getTargetXDIAddress();

		this.getGraph().setDeepContextNode(contextNodeXDIAddress).setRelation(relationXDIaddress, targetXDIAddress);
	}

	@Override
	public void executeDelOnRelationStatement(XDIStatement relationStatement, DelOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		XDIAddress contextNodeXDIAddress = relationStatement.getContextNodeXDIAddress();
		XDIAddress relationXDIaddress = relationStatement.getRelationXDIAddress();
		XDIAddress targetXDIAddress = relationStatement.getTargetXDIAddress();

		ContextNode contextNode = this.getGraph().getDeepContextNode(contextNodeXDIAddress, false);
		if (contextNode == null) return;

		if (VariableUtil.isVariable(targetXDIAddress)) {

			if (VariableUtil.isVariable(relationXDIaddress)) {

				contextNode.delRelations();
			} else {

				contextNode.delRelations(relationXDIaddress);
			}
		} else {

			contextNode.delRelation(relationXDIaddress, targetXDIAddress);
		}
	}

	/*
	 * Operations on literal statements
	 */

	@Override
	public void executeGetOnLiteralStatement(XDIStatement literalStatement, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		XDIAddress contextNodeXDIAddress = literalStatement.getContextNodeXDIAddress();
		Object literalData = literalStatement.getLiteralData();

		ContextNode contextNode = this.getGraph().getDeepContextNode(contextNodeXDIAddress);
		if (contextNode == null) return;

		LiteralNode literalNode = contextNode.getLiteralNode(literalData);
		if (literalNode == null) return;

		CopyUtil.copyStatement(literalNode.getStatement(), messageResult.getGraph(), null);
	}

	@Override
	public void executeSetOnLiteralStatement(XDIStatement literalStatement, SetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		XDIAddress contextNodeXDIAddress = literalStatement.getContextNodeXDIAddress();
		Object literalData = literalStatement.getLiteralData();

		ContextNode contextNode = this.getGraph().getDeepContextNode(contextNodeXDIAddress);
		if (contextNode == null) return;

		contextNode.setLiteralNode(literalData);
	}

	@Override
	public void executeDelOnLiteralStatement(XDIStatement literalStatement, DelOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		XDIAddress contextNodeXDIAddress = literalStatement.getContextNodeXDIAddress();
		Object literalData = literalStatement.getLiteralData();

		ContextNode contextNode = this.getGraph().getDeepContextNode(contextNodeXDIAddress);
		if (contextNode == null) return;

		LiteralNode literalNode = contextNode.getLiteralNode(literalData);
		if (literalNode == null) return;

		literalNode.delete();
	}
}

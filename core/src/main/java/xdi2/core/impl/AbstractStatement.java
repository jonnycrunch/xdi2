package xdi2.core.impl;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.LiteralNode;
import xdi2.core.Relation;
import xdi2.core.Statement;
import xdi2.core.constants.XDIConstants;
import xdi2.core.features.nodetypes.XdiInnerRoot;
import xdi2.core.syntax.XDIArc;
import xdi2.core.syntax.XDIStatement;

public abstract class AbstractStatement implements Statement {

	private static final long serialVersionUID = -8879896347494275688L;

	@Override
	public Graph getGraph() {

		return null;
	}

	@Override
	public boolean isImplied() {

		if (this instanceof ContextNodeStatement) {

			ContextNode contextNode = ((ContextNodeStatement) this).getContextNode();
			if (contextNode == null) return false;

			if (! contextNode.isEmpty()) return true;
			if (contextNode.containsIncomingRelations()) return true;
		}

		if (this instanceof RelationStatement) {

			Relation relation = ((RelationStatement) this).getRelation();
			if (relation == null) return false;

			ContextNode targetContextNode = relation.followContextNode();
			if (targetContextNode == null) return false;

			XdiInnerRoot innerRoot = XdiInnerRoot.fromContextNode(targetContextNode);
			if (innerRoot != null && relation.equals(innerRoot.getPredicateRelation()) && ! innerRoot.getContextNode().isEmpty()) return true;
		}

		return false;
	}

	@Override
	public void delete() {

	}

	@Override
	public XDIStatement getXDIStatement() {

		if (this instanceof ContextNodeStatement) {

			return XDIStatement.fromContextNodeComponents(((ContextNodeStatement) this).getSubject(), ((ContextNodeStatement) this).getObject());
		} else if (this instanceof RelationStatement) {

			return XDIStatement.fromRelationComponents(((RelationStatement) this).getSubject(), ((RelationStatement) this).getPredicate(), ((RelationStatement) this).getObject());
		} else if (this instanceof LiteralStatement) {

			return XDIStatement.fromLiteralComponents(((LiteralStatement) this).getSubject(), ((LiteralStatement) this).getObject());
		}

		throw new IllegalStateException("Invalid statement: " + this.getClass().getSimpleName());
	}

	/*
	 * Object methods
	 */

	@Override
	public String toString() {

		return this.getXDIStatement().toString();
	}

	@Override
	public boolean equals(Object object) {

		if (object == null || ! (object instanceof Statement)) return false;
		if (object == this) return true;

		Statement other = (Statement) object;

		// two statements are equal if their components are equals

		if (! this.getSubject().equals(other.getSubject())) return false;
		if (! this.getPredicate().equals(other.getPredicate())) return false;
		if (! this.getObject().equals(other.getObject())) return false;

		return true;
	}

	@Override
	public int hashCode() {

		int hashCode = 1;

		hashCode = (hashCode * 31) + (this.getSubject() == null ? 0 : this.getSubject().hashCode());
		hashCode = (hashCode * 31) + (this.getPredicate() == null ? 0 : this.getPredicate().hashCode());
		hashCode = (hashCode * 31) + (this.getObject() == null ? 0 : this.getObject().hashCode());

		return hashCode;
	}

	@Override
	public int compareTo(Statement other) {

		if (other == null || other == this) return(0);

		int c;

		// compare subject

		c = this.getSubject().compareTo(other.getSubject());
		if (c != 0) return c;

		// compare predicate

		c = this.getPredicate().toString().compareTo(other.getPredicate().toString());
		if (c != 0) return c;

		// compare objects

		c = this.getObject().toString().compareTo(other.getObject().toString());
		if (c != 0) return c;

		return 0;
	}

	/*
	 * Sub-classes
	 */

	public static abstract class AbstractContextNodeStatement extends AbstractStatement implements ContextNodeStatement {

		private static final long serialVersionUID = -7006808512493295364L;

		@Override
		public final String getPredicate() {

			return XDIConstants.STRING_CONTEXT;
		}

		@Override
		public ContextNode getContextNode() {

			return null;
		}
	}

	public static abstract class AbstractRelationStatement extends AbstractStatement implements RelationStatement {

		private static final long serialVersionUID = -2393268622327844933L;

		@Override
		public Relation getRelation() {

			return null;
		}
	}

	public static abstract class AbstractLiteralStatement extends AbstractStatement implements LiteralStatement {

		private static final long serialVersionUID = -7876412291137305476L;

		@Override
		public final XDIArc getPredicate() {

			return XDIConstants.XDI_ARC_LITERAL;
		}

		@Override
		public LiteralNode getLiteralNode() {

			return null;
		}
	}
}

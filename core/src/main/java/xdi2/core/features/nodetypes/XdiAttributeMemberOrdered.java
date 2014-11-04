package xdi2.core.features.nodetypes;

import java.util.Iterator;

import xdi2.core.ContextNode;
import xdi2.core.syntax.XDIArc;
import xdi2.core.util.iterators.MappingIterator;
import xdi2.core.util.iterators.NotNullIterator;

/**
 * An XDI ordered attribute member, represented as a context node.
 * 
 * @author markus
 */
public final class XdiAttributeMemberOrdered extends XdiAbstractAttribute implements XdiMemberOrdered<XdiAttributeCollection, XdiAttribute, XdiAttributeCollection, XdiAttributeMemberUnordered, XdiAttributeMemberOrdered, XdiAttributeMember>, XdiAttributeMember {

	private static final long serialVersionUID = 3562576098019686485L;

	protected XdiAttributeMemberOrdered(ContextNode contextNode) {

		super(contextNode);
	}

	/*
	 * Static methods
	 */

	/**
	 * Checks if a context node is a valid XDI ordered attribute member.
	 * @param contextNode The context node to check.
	 * @return True if the context node is a valid XDI ordered attribute member.
	 */
	public static boolean isValid(ContextNode contextNode) {

		if (contextNode == null) throw new NullPointerException();

		if (contextNode.getXDIArc() == null || ! isValidXDIArc(contextNode.getXDIArc())) return false;
		if (contextNode.getContextNode() == null || ! XdiAttributeCollection.isValid(contextNode.getContextNode())) return false;

		return true;
	}

	/**
	 * Factory method that creates an XDI ordered attribute member bound to a given context node.
	 * @param contextNode The context node that is an XDI ordered attribute member.
	 * @return The XDI ordered attribute member.
	 */
	public static XdiAttributeMemberOrdered fromContextNode(ContextNode contextNode) {

		if (contextNode == null) throw new NullPointerException();

		if (! isValid(contextNode)) return null;

		return new XdiAttributeMemberOrdered(contextNode);
	}

	/*
	 * Methods for arcs
	 */

	public static boolean isValidXDIArc(XDIArc XDIarc) {

		if (XDIarc == null) throw new NullPointerException();

		if (! XdiAbstractMemberOrdered.isValidXDIArc(XDIarc, XdiAttributeCollection.class)) return false;

		return true;
	}

	/*
	 * Instance methods
	 */

	/**
	 * Returns the parent XDI collection of this XDI ordered attribute member.
	 * @return The parent XDI collection.
	 */
	@Override
	public XdiAttributeCollection getXdiCollection() {

		return new XdiAttributeCollection(this.getContextNode().getContextNode());
	}

	/*
	 * Helper classes
	 */

	public static class MappingContextNodeXdiAttributeMemberOrderedIterator extends NotNullIterator<XdiAttributeMemberOrdered> {

		public MappingContextNodeXdiAttributeMemberOrderedIterator(Iterator<ContextNode> contextNodes) {

			super(new MappingIterator<ContextNode, XdiAttributeMemberOrdered> (contextNodes) {

				@Override
				public XdiAttributeMemberOrdered map(ContextNode contextNode) {

					return XdiAttributeMemberOrdered.fromContextNode(contextNode);
				}
			});
		}
	}
}

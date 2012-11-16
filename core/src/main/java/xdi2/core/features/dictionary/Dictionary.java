package xdi2.core.features.dictionary;

import java.util.Iterator;

import xdi2.core.ContextNode;
import xdi2.core.Relation;
import xdi2.core.constants.XDIDictionaryConstants;
import xdi2.core.util.iterators.MappingContextNodeXriIterator;
import xdi2.core.util.iterators.MappingRelationContextNodeIterator;
import xdi2.core.util.iterators.MappingRelationTargetContextNodeIterator;
import xdi2.core.xri3.impl.XRI3Constants;
import xdi2.core.xri3.impl.XDI3Segment;
import xdi2.core.xri3.impl.XDI3SubSegment;

public class Dictionary {

	private Dictionary() { }

	/*
	 * Methods for dictionary XRIs
	 */

	public static XDI3SubSegment instanceXriToDictionaryXri(XDI3SubSegment instanceXri) {

		return new XDI3SubSegment("" + XRI3Constants.GCS_PLUS + "(" + instanceXri + ")");
	}

	public static XDI3SubSegment dictionaryXriToInstanceXri(XDI3SubSegment dictionaryXri) {

		if (! XRI3Constants.GCS_PLUS.equals(dictionaryXri.getGCS())) return null;
		if (dictionaryXri.hasLCS()) return null;
		if (! dictionaryXri.hasXRef()) return null;
		if (! dictionaryXri.getXRef().hasXRIReference()) return null;

		return new XDI3SubSegment("" + dictionaryXri.getXRef().getXRIReference());
	}

	public static XDI3SubSegment nativeIdentifierToInstanceXri(String nativeIdentifier) {

		return new XDI3SubSegment("" + XRI3Constants.GCS_PLUS + "(" + nativeIdentifier + ")");
	}

	public static String instanceXriToNativeIdentifier(XDI3SubSegment instanceXri) {

		if (! instanceXri.hasXRef()) return null;
		if (! instanceXri.getXRef().hasXRIReference()) return null;

		return instanceXri.getXRef().getXRIReference().toString();
	}

	/*
	 * Methods for canonical context nodes.
	 * This is the target of a $is / $xis relation.
	 */

	public static ContextNode getCanonicalContextNode(ContextNode contextNode) {

		Relation relation = contextNode.getRelation(XDIDictionaryConstants.XRI_S_IS);
		if (relation == null) return null;

		return relation.follow();
	}

	public static void setCanonicalContextNode(ContextNode contextNode, ContextNode canonicalContextNode) {

		contextNode.createRelation(XDIDictionaryConstants.XRI_S_IS, canonicalContextNode);
	}

	public static ContextNode getPrivateCanonicalContextNode(ContextNode contextNode) {

		Relation relation = contextNode.getRelation(XDIDictionaryConstants.XRI_S_IS_BANG);
		if (relation == null) return null;

		return relation.follow();
	}

	public static void setPrivateCanonicalContextNode(ContextNode contextNode, ContextNode canonicalContextNode) {

		contextNode.createRelation(XDIDictionaryConstants.XRI_S_IS_BANG, canonicalContextNode);
	}

	/*
	 * Methods for synonym context nodes.
	 * These are the sources of incoming $is relations.
	 */

	public static Iterator<ContextNode> getSynonymContextNodes(ContextNode contextNode) {

		Iterator<Relation> relations = contextNode.getIncomingRelations(XDIDictionaryConstants.XRI_S_IS);

		return new MappingRelationContextNodeIterator(relations);
	}

	/*
	 * Methods for types of context nodes.
	 */

	public static Iterator<XDI3Segment> getContextNodeTypes(ContextNode contextNode) {

		return new MappingContextNodeXriIterator(new MappingRelationTargetContextNodeIterator(contextNode.getRelations(XDIDictionaryConstants.XRI_S_IS_TYPE)));
	}

	public static XDI3Segment getContextNodeType(ContextNode contextNode) {

		return contextNode.getRelation(XDIDictionaryConstants.XRI_S_IS_TYPE).getTargetContextNodeXri();
	}

	public static boolean isContextNodeType(ContextNode contextNode, XDI3Segment type) {

		return contextNode.containsRelation(XDIDictionaryConstants.XRI_S_IS_TYPE, type);
	}

	public static void addContextNodeType(ContextNode contextNode, XDI3Segment type) {

		contextNode.createRelation(XDIDictionaryConstants.XRI_S_IS_TYPE, type);
	}

	public static void removeContextNodeType(ContextNode contextNode, XDI3Segment type) {

		contextNode.deleteRelation(XDIDictionaryConstants.XRI_S_IS_TYPE, type);
	}

	public static void removeContextNodeTypes(ContextNode contextNode) {

		contextNode.deleteRelations(XDIDictionaryConstants.XRI_S_IS_TYPE);
	}

	public static void setContextNodeType(ContextNode contextNode, XDI3Segment type) {

		removeContextNodeTypes(contextNode);
		addContextNodeType(contextNode, type);
	}
}

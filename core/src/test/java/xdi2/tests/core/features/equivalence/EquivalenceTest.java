package xdi2.tests.core.features.equivalence;

import junit.framework.TestCase;
import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.constants.XDIDictionaryConstants;
import xdi2.core.features.equivalence.Equivalence;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;

public class EquivalenceTest extends TestCase {

	public void testIdentity() throws Exception {

		Graph graph = MemoryGraphFactory.getInstance().openGraph();
		ContextNode contextNode = graph.getRootContextNode().setContextNode(XDIArc.create("=markus"));
		ContextNode identityContextNode = graph.getRootContextNode().setDeepContextNode(XDIAddress.create("=pseudo"));

		// test $is

		Equivalence.setIdentityContextNode(contextNode, identityContextNode);

		assertEquals(Equivalence.getIdentityContextNodes(contextNode).next(), identityContextNode);
		assertEquals(Equivalence.getIncomingIdentityContextNodes(identityContextNode).next(), contextNode);

		assertEquals(Equivalence.getIdentityRelations(contextNode).next(), contextNode.getRelation(XDIDictionaryConstants.XDI_ADD_IS));
		assertEquals(Equivalence.getIncomingIdentityRelations(identityContextNode).next(), contextNode.getRelation(XDIDictionaryConstants.XDI_ADD_IS));

		Equivalence.getIdentityContextNodes(contextNode).next().delete();

		// done

		assertTrue(contextNode.isEmpty());
		assertTrue(identityContextNode.isEmpty());
		contextNode.delete();
		assertTrue(graph.isEmpty());
		
		graph.close();
	}

	public void testReference() throws Exception {

		Graph graph = MemoryGraphFactory.getInstance().openGraph();
		ContextNode contextNode = graph.getRootContextNode().setContextNode(XDIArc.create("=markus"));
		ContextNode referenceContextNode = graph.getRootContextNode().setDeepContextNode(XDIAddress.create("=pseudo1"));
		ContextNode replacementContextNode = graph.getRootContextNode().setDeepContextNode(XDIAddress.create("=pseudo2"));

		// test $ref

		Equivalence.setReferenceContextNode(contextNode, referenceContextNode);

		assertEquals(Equivalence.getReferenceContextNode(contextNode), referenceContextNode);
		assertEquals(Equivalence.getIncomingReferenceContextNodes(referenceContextNode).next(), contextNode);
		assertNull(Equivalence.getReplacementContextNode(contextNode));

		assertEquals(Equivalence.getReferenceRelation(contextNode), contextNode.getRelation(XDIDictionaryConstants.XDI_ADD_REF));
		assertEquals(Equivalence.getIncomingReferenceRelations(referenceContextNode).next(), contextNode.getRelation(XDIDictionaryConstants.XDI_ADD_REF));

		Equivalence.getReferenceContextNode(contextNode).delete();

		// test $rep

		Equivalence.setReplacementContextNode(contextNode, replacementContextNode);

		assertEquals(Equivalence.getReplacementContextNode(contextNode), replacementContextNode);
		assertEquals(Equivalence.getIncomingReplacementContextNodes(replacementContextNode).next(), contextNode);
		assertNull(Equivalence.getReferenceContextNode(contextNode));

		assertEquals(Equivalence.getReplacementRelation(contextNode), contextNode.getRelation(XDIDictionaryConstants.XDI_ADD_REP));
		assertEquals(Equivalence.getIncomingReplacementRelations(replacementContextNode).next(), contextNode.getRelation(XDIDictionaryConstants.XDI_ADD_REP));

		Equivalence.getReplacementContextNode(contextNode).delete();

		// done

		assertTrue(contextNode.isEmpty());
		assertTrue(referenceContextNode.isEmpty());
		assertTrue(replacementContextNode.isEmpty());
		contextNode.delete();
		assertTrue(graph.isEmpty());
		
		graph.close();
	}
}

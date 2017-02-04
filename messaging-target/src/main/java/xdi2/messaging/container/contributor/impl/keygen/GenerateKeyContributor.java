package xdi2.messaging.container.contributor.impl.keygen;

import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.features.datatypes.DataTypes;
import xdi2.core.features.keys.Keys;
import xdi2.core.features.nodetypes.XdiAbstractAttribute;
import xdi2.core.features.nodetypes.XdiAbstractEntity;
import xdi2.core.features.nodetypes.XdiAttribute;
import xdi2.core.features.nodetypes.XdiAttributeSingleton;
import xdi2.core.features.nodetypes.XdiEntity;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.syntax.XDIStatement;
import xdi2.messaging.container.MessagingContainer;
import xdi2.messaging.container.Prototype;
import xdi2.messaging.container.contributor.ContributorMount;
import xdi2.messaging.container.contributor.ContributorResult;
import xdi2.messaging.container.contributor.impl.AbstractContributor;
import xdi2.messaging.container.exceptions.Xdi2MessagingException;
import xdi2.messaging.container.execution.ExecutionContext;
import xdi2.messaging.container.impl.graph.GraphMessagingContainer;
import xdi2.messaging.operations.DoOperation;

/**
 * This contributor can generate key pairs and symmetric keys in a target graph.
 */
//TODO: fix variable syntax
@ContributorMount(
		contributorXDIAddresses={"{}$keypair", "$keypair", "{}<$key>", "<$key>"},
		operationXDIAddresses={"$do$keypair", "$do<$key>"},
		relationXDIAddresses={"$is#"}
		)
public class GenerateKeyContributor extends AbstractContributor implements Prototype<GenerateKeyContributor> {

	private static final Logger log = LoggerFactory.getLogger(GenerateKeyContributor.class);

	public static final XDIAddress XDI_ADD_DO_KEYPAIR = XDIAddress.create("$do$keypair");
	public static final XDIAddress XDI_ADD_DO_KEY = XDIAddress.create("$do<$key>");

	private Graph targetGraph;

	public GenerateKeyContributor(Graph targetGraph) {

		this.targetGraph = targetGraph;
	}

	public GenerateKeyContributor() {

		this(null);
	}

	/*
	 * Prototype
	 */

	@Override
	public GenerateKeyContributor instanceFor(xdi2.messaging.container.Prototype.PrototypingContext prototypingContext) throws Xdi2MessagingException {

		// create new contributor

		GenerateKeyContributor contributor = new GenerateKeyContributor();

		// set the graph

		contributor.setTargetGraph(this.getTargetGraph());

		// done

		return contributor;
	}

	/*
	 * Init and shutdown
	 */

	@Override
	public void init(MessagingContainer messagingContainer) throws Exception {

		super.init(messagingContainer);

		if (this.getTargetGraph() == null && messagingContainer instanceof GraphMessagingContainer) this.setTargetGraph(((GraphMessagingContainer) messagingContainer).getGraph()); 
		if (this.getTargetGraph() == null) throw new Xdi2MessagingException("No target graph.", null, null);
	}

	/*
	 * Contributor methods
	 */

	@Override
	public ContributorResult executeDoOnRelationStatement(XDIAddress[] contributorAddresses, XDIAddress contributorsAddress, XDIStatement relativeTargetStatement, DoOperation operation, Graph operationResultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		XDIAddress targetXDIAddress = relativeTargetStatement.getTargetXDIAddress();

		// check parameters

		XDIAddress dataTypeXDIAddress = targetXDIAddress;

		String keyAlgorithm;
		Integer keyLength;

		keyAlgorithm = Keys.getKeyAlgorithm(dataTypeXDIAddress);
		if (keyAlgorithm == null) throw new Xdi2MessagingException("Invalid key algorithm: " + dataTypeXDIAddress, null, executionContext);

		keyLength = Keys.getKeyLength(dataTypeXDIAddress);
		if (keyLength == null) throw new Xdi2MessagingException("Invalid key length: " + dataTypeXDIAddress, null, executionContext);

		if (log.isDebugEnabled()) log.debug("keyAlgorithm: " + keyAlgorithm + ", keyLength: " + keyLength);

		// key pair or symmetric key?

		if (XDI_ADD_DO_KEYPAIR.equals(operation.getOperationXDIAddress())) {

			// generate key pair

			KeyPair keyPair;

			try {

				KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(keyAlgorithm);
				keyPairGen.initialize(keyLength.intValue());
				keyPair = keyPairGen.generateKeyPair();
			} catch (Exception ex) {

				throw new Xdi2MessagingException("Problem while creating key pair: " + ex.getMessage(), ex, executionContext);
			}

			if (log.isDebugEnabled()) log.debug("Created key pair: " + keyPair.getClass().getSimpleName());

			// add it to the graph

			ContextNode contextNode = this.getTargetGraph().setDeepContextNode(contributorsAddress);
			if (! XdiAbstractEntity.isValid(contextNode)) throw new Xdi2MessagingException("Can only create a key pair on an entity.", null, executionContext);
			XdiEntity keyPairXdiEntity = XdiAbstractEntity.fromContextNode(contextNode);
			XdiAttributeSingleton publicKeyXdiAttribute = keyPairXdiEntity.getXdiAttributeSingleton(XDIArc.create("<$public>"), true).getXdiAttributeSingleton(XDIArc.create("<$key>"), true);
			XdiAttributeSingleton privateKeyXdiAttribute = keyPairXdiEntity.getXdiAttributeSingleton(XDIArc.create("<$private>"), true).getXdiAttributeSingleton(XDIArc.create("<$key>"), true);
			publicKeyXdiAttribute.setLiteralString(new String(Base64.encodeBase64(keyPair.getPublic().getEncoded()), Charset.forName("UTF-8")));
			privateKeyXdiAttribute.setLiteralString(new String(Base64.encodeBase64(keyPair.getPrivate().getEncoded()), Charset.forName("UTF-8")));
			DataTypes.setDataType(contextNode, dataTypeXDIAddress);
		} else if (XDI_ADD_DO_KEY.equals(operation.getOperationXDIAddress())) {

			// generate symmetric key

			SecretKey secretKey;

			try {

				KeyGenerator keyGen = KeyGenerator.getInstance(keyAlgorithm);
				keyGen.init(keyLength.intValue());
				secretKey = keyGen.generateKey(); 
			} catch (Exception ex) {

				throw new Xdi2MessagingException("Problem while creating symmetric key: " + ex.getMessage(), ex, executionContext);
			}

			if (log.isDebugEnabled()) log.debug("Created symmetric key: " + secretKey.getClass().getSimpleName());

			// add it to the graph

			ContextNode contextNode = this.getTargetGraph().setDeepContextNode(contributorsAddress);
			if (! XdiAbstractAttribute.isValid(contextNode)) throw new Xdi2MessagingException("Can only create a symmetric key on an attribute.", null, executionContext);
			XdiAttribute symmetricKeyXdiAttribute = XdiAbstractAttribute.fromContextNode(contextNode);
			symmetricKeyXdiAttribute.setLiteralString(new String(Base64.encodeBase64(secretKey.getEncoded()), Charset.forName("UTF-8")));
			DataTypes.setDataType(contextNode, dataTypeXDIAddress);
		}

		// done

		return ContributorResult.SKIP_MESSAGING_TARGET;
	}

	/*
	 * Getters and setters
	 */

	public Graph getTargetGraph() {

		return this.targetGraph;
	}

	public void setTargetGraph(Graph targetGraph) {

		this.targetGraph = targetGraph;
	}
}

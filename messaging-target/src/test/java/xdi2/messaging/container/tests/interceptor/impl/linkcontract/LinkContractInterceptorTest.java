package xdi2.messaging.container.tests.interceptor.impl.linkcontract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;
import xdi2.core.Graph;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.io.XDIReader;
import xdi2.core.io.readers.XDIDisplayReader;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.container.exceptions.Xdi2NotAuthorizedException;
import xdi2.messaging.container.execution.ExecutionContext;
import xdi2.messaging.container.execution.ExecutionResult;
import xdi2.messaging.container.impl.graph.GraphMessagingContainer;
import xdi2.messaging.container.interceptor.impl.linkcontract.LinkContractInterceptor;

public class LinkContractInterceptorTest extends TestCase {

	private static final Logger log = LoggerFactory.getLogger(LinkContractInterceptorTest.class);

	private static final XDIReader autoReader = new XDIDisplayReader(null);

	private static MemoryGraphFactory graphFactory = new MemoryGraphFactory();

	public void testDummy() {

	}

	public void testLinkContracts() throws Exception {

		int i=1, ii;

		while (true) {

			if (this.getClass().getResourceAsStream("graph" + i + ".xdi") == null) break;

			log.info("Graph " + i);

			Graph graph = graphFactory.openGraph(); 
			autoReader.read(graph, this.getClass().getResourceAsStream("graph" + i + ".xdi")).close();

			// check authorized

			ii = 1;

			while (true) {

				if (this.getClass().getResourceAsStream("authorized" + i + "." + ii + ".xdi") == null) break;

				log.info("Authorized " + i + "." + ii);

				Graph authorized = graphFactory.openGraph(); 
				autoReader.read(authorized, this.getClass().getResourceAsStream("authorized" + i + "." + ii + ".xdi")).close();

				LinkContractInterceptor linkContractsInterceptor = new LinkContractInterceptor();
				linkContractsInterceptor.setLinkContractsGraph(graph);

				GraphMessagingContainer graphMessagingContainer = new GraphMessagingContainer();
				graphMessagingContainer.setGraph(graph);
				graphMessagingContainer.getInterceptors().addInterceptor(linkContractsInterceptor);

				MessageEnvelope messageEnvelope = MessageEnvelope.fromGraph(authorized);
				ExecutionContext executionContext = ExecutionContext.createExecutionContext();
				ExecutionResult executionResult = ExecutionResult.createExecutionResult(messageEnvelope);

				try {

					graphMessagingContainer.execute(messageEnvelope, executionContext, executionResult);
					continue;
				} catch (Xdi2NotAuthorizedException ex) {

					log.error(ex.getMessage(), ex);
					fail();
				} finally {
					ii++;
				}
			}

			// check not authorized

			ii = 1;

			while (true) {

				if (this.getClass().getResourceAsStream("notauthorized" + i + "." + ii + ".xdi") == null) break;

				log.info("Not Authorized " + i + "." + ii);

				Graph notauthorized = graphFactory.openGraph(); 
				autoReader.read(notauthorized, this.getClass().getResourceAsStream("notauthorized" + i + "." + ii + ".xdi")).close();

				LinkContractInterceptor linkContractsInterceptor = new LinkContractInterceptor();
				linkContractsInterceptor.setLinkContractsGraph(graph);

				GraphMessagingContainer graphMessagingContainer = new GraphMessagingContainer();
				graphMessagingContainer.setGraph(graph);
				graphMessagingContainer.getInterceptors().addInterceptor(linkContractsInterceptor);

				MessageEnvelope messageEnvelope = MessageEnvelope.fromGraph(notauthorized);
				ExecutionContext executionContext = ExecutionContext.createExecutionContext();
				ExecutionResult executionResult = ExecutionResult.createExecutionResult(messageEnvelope);

				try {

					graphMessagingContainer.execute(messageEnvelope, executionContext, executionResult);
					fail();
				} catch (Xdi2NotAuthorizedException ex) {

					continue;
				} finally {

					ii++;
				}
			}

			assertTrue(ii > 1);

			i++;
		}

		log.info("Done.");

		assertTrue(i > 1);
	}
}

package xdi2.webtools.localmessenger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.impl.local.XDILocalClient;
import xdi2.core.Graph;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.io.XDIReader;
import xdi2.core.io.XDIReaderRegistry;
import xdi2.core.io.XDIWriter;
import xdi2.core.io.XDIWriterRegistry;
import xdi2.core.io.readers.AutoReader;
import xdi2.core.syntax.XDIAddress;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.response.MessagingResponse;
import xdi2.messaging.container.impl.graph.GraphMessagingContainer;
import xdi2.messaging.container.interceptor.impl.FromInterceptor;
import xdi2.messaging.container.interceptor.impl.MessagePolicyInterceptor;
import xdi2.messaging.container.interceptor.impl.ReadOnlyInterceptor;
import xdi2.messaging.container.interceptor.impl.RefInterceptor;
import xdi2.messaging.container.interceptor.impl.ToInterceptor;
import xdi2.messaging.container.interceptor.impl.VariablesInterceptor;
import xdi2.messaging.container.interceptor.impl.linkcontract.LinkContractInterceptor;
import xdi2.webtools.util.OutputCache;

/**
 * Servlet implementation class for Servlet: XDILocalMessenger
 *
 */
public class XDILocalMessenger extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

	private static final long serialVersionUID = -3840753270326755062L;

	private static Logger log = LoggerFactory.getLogger(XDILocalMessenger.class);

	private static MemoryGraphFactory graphFactory;
	private static List<String> sampleCategories;
	private static List<List<String>> sampleInputs;
	private static List<List<String>> sampleMessages;
	private static List<List<String>> sampleTooltips;

	static {

		graphFactory = MemoryGraphFactory.getInstance();
		graphFactory.setSortmode(MemoryGraphFactory.SORTMODE_ORDER);

		sampleCategories = new ArrayList<String> ();
		sampleInputs = new ArrayList<List<String>> ();
		sampleMessages = new ArrayList<List<String>> ();
		sampleTooltips = new ArrayList<List<String>> ();

		int i;

		while (true) {

			InputStream inputStream1 = XDILocalMessenger.class.getResourceAsStream("category" + (sampleCategories.size() + 1) + ".txt");
			ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();

			try {

				while ((i = inputStream1.read()) != -1) outputStream1.write(i);
				sampleCategories.add(new String(outputStream1.toByteArray()));
				sampleInputs.add(new ArrayList<String> ());
				sampleMessages.add(new ArrayList<String> ());
				sampleTooltips.add(new ArrayList<String> ());
			} catch (Exception ex) {

				break;
			} finally {

				try { inputStream1.close(); } catch (Exception ex) { }
				try { outputStream1.close(); } catch (Exception ex) { }
			}

			for (int c=0; c<sampleCategories.size(); c++) {

				while (true) {

					InputStream inputStream2 = XDILocalMessenger.class.getResourceAsStream("graph" + (c + 1) + "_" + (sampleInputs.get(c).size() + 1) + ".xdi");
					InputStream inputStream3 = XDILocalMessenger.class.getResourceAsStream("message" + (c + 1) + "_" + (sampleMessages.get(c).size() + 1) + ".xdi");
					InputStream inputStream4 = XDILocalMessenger.class.getResourceAsStream("tooltip" + (c + 1) + "_" + (sampleTooltips.get(c).size() + 1) + ".txt");
					ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
					ByteArrayOutputStream outputStream3 = new ByteArrayOutputStream();
					ByteArrayOutputStream outputStream4 = new ByteArrayOutputStream();

					try {

						while ((i = inputStream2.read()) != -1) outputStream2.write(i);
						while ((i = inputStream3.read()) != -1) outputStream3.write(i);
						while ((i = inputStream4.read()) != -1) outputStream4.write(i);
						sampleInputs.get(c).add(new String(outputStream2.toByteArray()).trim());
						sampleMessages.get(c).add(new String(outputStream3.toByteArray()).trim());
						sampleTooltips.get(c).add(new String(outputStream4.toByteArray()).trim());
					} catch (Exception ex) {

						break;
					} finally {

						try { inputStream2.close(); } catch (Exception ex) { }
						try { inputStream3.close(); } catch (Exception ex) { }
						try { inputStream4.close(); } catch (Exception ex) { }
						try { outputStream2.close(); } catch (Exception ex) { }
						try { outputStream3.close(); } catch (Exception ex) { }
						try { outputStream4.close(); } catch (Exception ex) { }
					}
				}
			}
		}
	}


	public XDILocalMessenger() {

		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		String category = request.getParameter("category");
		if (category == null) category = "1";

		String sample = request.getParameter("sample");
		if (sample == null) sample = "1";

		request.setAttribute("sampleCategories", sampleCategories);
		request.setAttribute("sampleInputs", sampleInputs);
		request.setAttribute("sampleMessages", sampleMessages);
		request.setAttribute("sampleTooltips", sampleTooltips);
		request.setAttribute("category", category);
		request.setAttribute("sample", sample);
		request.setAttribute("resultFormat", XDIWriterRegistry.getDefault().getFormat());
		request.setAttribute("writeImplied", null);
		request.setAttribute("writeOrdered", "on");
		request.setAttribute("writePretty", null);
		request.setAttribute("useFromInterceptor", null);
		request.setAttribute("useToInterceptor", "on");
		request.setAttribute("useVariablesInterceptor", null);
		request.setAttribute("useRefInterceptor", "on");
		request.setAttribute("useReadOnlyInterceptor", null);
		request.setAttribute("useMessagePolicyInterceptor", null);
		request.setAttribute("useLinkContractInterceptor", null);
		request.setAttribute("input", sampleInputs.get(Integer.parseInt(category) - 1).get(Integer.parseInt(sample) - 1));
		request.setAttribute("message", sampleMessages.get(Integer.parseInt(category) - 1).get(Integer.parseInt(sample) - 1));

		request.getRequestDispatcher("/XDILocalMessenger.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		String category = request.getParameter("category");
		String sample = request.getParameter("sample");
		String resultFormat = request.getParameter("resultFormat");
		String writeImplied = request.getParameter("writeImplied");
		String writeOrdered = request.getParameter("writeOrdered");
		String writePretty = request.getParameter("writePretty");
		String useFromInterceptor = request.getParameter("useFromInterceptor");
		String useToInterceptor = request.getParameter("useToInterceptor");
		String useVariablesInterceptor = request.getParameter("useVariablesInterceptor");
		String useRefInterceptor = request.getParameter("useRefInterceptor");
		String useReadOnlyInterceptor = request.getParameter("useReadOnlyInterceptor");
		String useMessagePolicyInterceptor = request.getParameter("useMessagePolicyInterceptor");
		String useLinkContractInterceptor = request.getParameter("useLinkContractInterceptor");
		String input = request.getParameter("input");
		String message = request.getParameter("message");
		String output = "";
		String outputId = "";
		String stats = "-1";
		String error = null;

		Properties xdiResultWriterParameters = new Properties();

		xdiResultWriterParameters.setProperty(XDIWriterRegistry.PARAMETER_IMPLIED, "on".equals(writeImplied) ? "1" : "0");
		xdiResultWriterParameters.setProperty(XDIWriterRegistry.PARAMETER_ORDERED, "on".equals(writeOrdered) ? "1" : "0");
		xdiResultWriterParameters.setProperty(XDIWriterRegistry.PARAMETER_PRETTY, "on".equals(writePretty) ? "1" : "0");

		XDIReader xdiReader = XDIReaderRegistry.getAuto();
		XDIWriter xdiInputWriter;
		XDIWriter xdiResultWriter = XDIWriterRegistry.forFormat(resultFormat, xdiResultWriterParameters);
		MessageEnvelope messageEnvelope = null;
		MessagingResponse messagingResponse = null;
		Graph graphInput = graphFactory.openGraph();

		long start = System.currentTimeMillis();

		try {

			// parse the input graph and remember its format

			xdiReader.read(graphInput, new StringReader(input));
			String inputFormat = ((AutoReader) xdiReader).getLastSuccessfulReader().getFormat();

			// parse the message envelope

			messageEnvelope = new MessageEnvelope();

			xdiReader.read(messageEnvelope.getGraph(), new StringReader(message));

			// prepare the messaging target

			GraphMessagingContainer messagingContainer = new GraphMessagingContainer();
			messagingContainer.setGraph(graphInput);

			if ("on".equals(useFromInterceptor)) {

				FromInterceptor fromInterceptor = new FromInterceptor();
				messagingContainer.getInterceptors().addInterceptor(fromInterceptor);
			}

			if ("on".equals(useToInterceptor)) {

				ToInterceptor toInterceptor = new ToInterceptor();
				messagingContainer.getInterceptors().addInterceptor(toInterceptor);
			}

			if ("on".equals(useVariablesInterceptor)) {

				VariablesInterceptor variablesInterceptor = new VariablesInterceptor();
				messagingContainer.getInterceptors().addInterceptor(variablesInterceptor);
			}

			if ("on".equals(useRefInterceptor)) {

				RefInterceptor refInterceptor = new RefInterceptor();
				messagingContainer.getInterceptors().addInterceptor(refInterceptor);
			}

			if ("on".equals(useReadOnlyInterceptor)) {

				ReadOnlyInterceptor readOnlyInterceptor = new ReadOnlyInterceptor();
				readOnlyInterceptor.setReadOnlyAddresses(new XDIAddress[] { XDIAddress.create("") });
				messagingContainer.getInterceptors().addInterceptor(readOnlyInterceptor);
			}

			if ("on".equals(useMessagePolicyInterceptor)) {

				MessagePolicyInterceptor messagePolicyInterceptor = new MessagePolicyInterceptor();
				messagePolicyInterceptor.setMessagePolicyGraph(graphInput);
				messagingContainer.getInterceptors().addInterceptor(messagePolicyInterceptor);
			}

			if ("on".equals(useLinkContractInterceptor)) {

				LinkContractInterceptor linkContractInterceptor = new LinkContractInterceptor();
				linkContractInterceptor.setLinkContractsGraph(graphInput);
				messagingContainer.getInterceptors().addInterceptor(linkContractInterceptor);
			}

			messagingContainer.init();

			// send the message envelope and read result

			XDILocalClient client = new XDILocalClient(messagingContainer);

			messagingResponse = client.send(messageEnvelope);

			// output the modified input graph

			xdiInputWriter = XDIWriterRegistry.forFormat(inputFormat, null);

			StringWriter writer1 = new StringWriter();
			xdiInputWriter.write(graphInput, writer1);
			input = StringEscapeUtils.escapeHtml(writer1.getBuffer().toString());

			// output the message result

			StringWriter writer2 = new StringWriter();
			xdiResultWriter.write(messagingResponse.getGraph(), writer2);
			output = StringEscapeUtils.escapeHtml(writer2.getBuffer().toString());

			outputId = UUID.randomUUID().toString();
			OutputCache.put(outputId, messagingResponse.getGraph());
		} catch (Exception ex) {

			if (ex instanceof Xdi2ClientException) {

				messagingResponse = ((Xdi2ClientException) ex).getMessagingResponse();

				// output the message result

				if (messagingResponse != null) {

					StringWriter writer2 = new StringWriter();
					xdiResultWriter.write(messagingResponse.getGraph(), writer2);
					output = StringEscapeUtils.escapeHtml(writer2.getBuffer().toString());

					outputId = UUID.randomUUID().toString();
					OutputCache.put(outputId, messagingResponse.getGraph());
				}
			}

			log.error(ex.getMessage(), ex);
			error = ex.getMessage();
			if (error == null) error = ex.getClass().getName();
		} finally {

			graphInput.close();
		}

		long stop = System.currentTimeMillis();

		stats = "";
		stats += Long.toString(stop - start) + " ms time. ";
		if (messageEnvelope != null) stats += Long.toString(messageEnvelope.getMessageCount()) + " message(s). ";
		if (messageEnvelope != null) stats += Long.toString(messageEnvelope.getOperationCount()) + " operation(s). ";
		if (messagingResponse != null) stats += Long.toString(messagingResponse.getGraph().getRootContextNode(true).getAllStatementCount()) + " result statement(s). ";

		// display results

		request.setAttribute("sampleCategories", sampleCategories);
		request.setAttribute("sampleInputs", sampleInputs);
		request.setAttribute("sampleMessages", sampleMessages);
		request.setAttribute("sampleTooltips", sampleTooltips);
		request.setAttribute("category", category);
		request.setAttribute("sample", sample);
		request.setAttribute("resultFormat", resultFormat);
		request.setAttribute("writeImplied", writeImplied);
		request.setAttribute("writeOrdered", writeOrdered);
		request.setAttribute("writePretty", writePretty);
		request.setAttribute("useFromInterceptor", useFromInterceptor);
		request.setAttribute("useToInterceptor", useToInterceptor);
		request.setAttribute("useVariablesInterceptor", useVariablesInterceptor);
		request.setAttribute("useRefInterceptor", useRefInterceptor);
		request.setAttribute("useReadOnlyInterceptor", useReadOnlyInterceptor);
		request.setAttribute("useMessagePolicyInterceptor", useMessagePolicyInterceptor);
		request.setAttribute("useLinkContractInterceptor", useLinkContractInterceptor);
		request.setAttribute("input", input);
		request.setAttribute("message", message);
		request.setAttribute("output", output);
		request.setAttribute("outputId", outputId);
		request.setAttribute("stats", stats);
		request.setAttribute("error", error);

		request.getRequestDispatcher("/XDILocalMessenger.jsp").forward(request, response);
	}   	  	    
}

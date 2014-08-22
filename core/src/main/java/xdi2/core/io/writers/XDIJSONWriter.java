package xdi2.core.io.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.Graph;
import xdi2.core.Statement;
import xdi2.core.impl.AbstractLiteral;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.io.AbstractXDIWriter;
import xdi2.core.io.MimeType;
import xdi2.core.io.XDIWriterRegistry;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.CopyUtil;
import xdi2.core.util.StatementUtil;
import xdi2.core.util.iterators.CompositeIterator;
import xdi2.core.util.iterators.IterableIterator;
import xdi2.core.util.iterators.MappingContextNodeStatementIterator;
import xdi2.core.util.iterators.MappingLiteralStatementIterator;
import xdi2.core.util.iterators.MappingRelationStatementIterator;
import xdi2.core.util.iterators.SelectingNotImpliedStatementIterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

public class XDIJSONWriter extends AbstractXDIWriter {

	private static final long serialVersionUID = -5510592554616900152L;

	private static final Logger log = LoggerFactory.getLogger(XDIJSONWriter.class);

	public static final String FORMAT_NAME = "XDI/JSON";
	public static final String FILE_EXTENSION = "json";
	public static final MimeType MIME_TYPE = new MimeType("application/xdi+json");

	private static final Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

	private boolean writeImplied;
	private boolean writeOrdered;
	private boolean writePretty;

	public XDIJSONWriter(Properties parameters) {

		super(parameters);
	}

	@Override
	protected void init() {

		// check parameters

		this.writeImplied = "1".equals(this.parameters.getProperty(XDIWriterRegistry.PARAMETER_IMPLIED, XDIWriterRegistry.DEFAULT_IMPLIED));
		this.writeOrdered = "1".equals(this.parameters.getProperty(XDIWriterRegistry.PARAMETER_ORDERED, XDIWriterRegistry.DEFAULT_ORDERED));
		this.writePretty = "1".equals(this.parameters.getProperty(XDIWriterRegistry.PARAMETER_PRETTY, XDIWriterRegistry.DEFAULT_PRETTY));

		if (log.isTraceEnabled()) log.trace("Parameters: writeImplied=" + this.writeImplied + ", writeOrdered=" + this.writeOrdered + ", writePretty=" + this.writePretty);
	}

	private void writeInternal(Graph graph, JsonObject jsonObject) throws IOException {

		// write ordered?

		Graph orderedGraph = null;
		IterableIterator<Statement> statements;

		if (this.writeOrdered) {

			MemoryGraphFactory memoryGraphFactory = new MemoryGraphFactory();
			memoryGraphFactory.setSortmode(MemoryGraphFactory.SORTMODE_ALPHA);
			orderedGraph = memoryGraphFactory.openGraph();
			CopyUtil.copyGraph(graph, orderedGraph, null);

			List<Iterator<? extends Statement>> list = new ArrayList<Iterator<? extends Statement>> ();
			list.add(new MappingContextNodeStatementIterator(orderedGraph.getRootContextNode(true).getAllContextNodes()));
			list.add(new MappingRelationStatementIterator(orderedGraph.getRootContextNode(true).getAllRelations()));
			list.add(new MappingLiteralStatementIterator(orderedGraph.getRootContextNode(true).getAllLiterals()));

			statements = new CompositeIterator<Statement> (list.iterator());
		} else {

			statements = graph.getRootContextNode(true).getAllStatements();
		}

		// ignore implied statements

		if (! this.writeImplied) statements = new SelectingNotImpliedStatementIterator(statements);

		// write the statements

		for (Statement statement : statements) {

			XDIStatement statementAddress = statement.getStatement();

			// put the statement into the JSON object

			this.putStatementIntoJsonObject(statementAddress, jsonObject);
		}

		// done

		if (orderedGraph != null) orderedGraph.close();
	}

	@SuppressWarnings("resource")
	@Override
	public Writer write(Graph graph, Writer writer) throws IOException {

		// write

		JsonObject jsonObject = new JsonObject();

		this.writeInternal(graph, jsonObject);

		JsonWriter jsonWriter = new JsonWriter(writer);
		if (this.writePretty) jsonWriter.setIndent("  ");
		gson.toJson(jsonObject, jsonWriter);
		jsonWriter.flush();
		writer.flush();

		return writer;
	}

	private void putStatementIntoJsonObject(XDIStatement statementAddress, JsonObject jsonObject) throws IOException {

		// nested JSON object?

		if (this.tryPutStatementIntoInnerJsonObject(statementAddress, jsonObject)) return;

		// add the object

		String key = statementAddress.getSubject() + "/" + statementAddress.getPredicate();

		addObjectToJsonObject(statementAddress, jsonObject, key);
	}

	private boolean tryPutStatementIntoInnerJsonObject(XDIStatement statementAddress, JsonObject jsonObject) throws IOException {

		XDIArc subjectFirstSubSegment = statementAddress.getSubject().getFirstArc();

		if (subjectFirstSubSegment == null || (! subjectFirstSubSegment.hasXRef()) || (! subjectFirstSubSegment.getXRef().hasPartialSubjectAndPredicate())) return false;

		XDIAddress innerRootSubject = statementAddress.getSubject().getFirstArc().getXRef().getPartialSubject();
		XDIAddress innerRootPredicate = statementAddress.getSubject().getFirstArc().getXRef().getPartialPredicate();

		XDIStatement reducedStatementAddress = StatementUtil.removeStartAddressStatement(statementAddress, XDIAddress.fromComponent(subjectFirstSubSegment));
		if (reducedStatementAddress == null) return false;

		// find the inner root JSON array

		String innerRootKey = "" + innerRootSubject + "/" + innerRootPredicate;

		JsonArray innerRootJsonArray = (JsonArray) jsonObject.get(innerRootKey);

		if (innerRootJsonArray == null) {

			innerRootJsonArray = new JsonArray();
			jsonObject.add(innerRootKey, innerRootJsonArray);
		}

		// find the inner root JSON object

		JsonObject innerRootJsonObject = findJsonObjectInJsonArray(innerRootJsonArray);

		if (innerRootJsonObject == null) {

			innerRootJsonObject = new JsonObject();
			innerRootJsonArray.add(innerRootJsonObject);
		}

		// put the statement into the inner root JSON object

		this.putStatementIntoJsonObject(reducedStatementAddress, innerRootJsonObject);

		// done

		return true;
	}

	private static JsonObject findJsonObjectInJsonArray(JsonArray jsonArray) {

		for (JsonElement jsonElement : jsonArray) {

			if (jsonElement instanceof JsonObject) return (JsonObject) jsonElement;
		}

		return null;
	}

	private static void addObjectToJsonObject(XDIStatement statementAddress, JsonObject jsonObject, String key) {

		if (statementAddress.isLiteralStatement()) {

			Object literalData = statementAddress.getLiteralData(); 

			jsonObject.add(key, AbstractLiteral.literalDataToJsonElement(literalData));
		} else {

			JsonArray jsonArray = (JsonArray) jsonObject.get(key);

			if (jsonArray == null) {

				jsonArray = new JsonArray();
				jsonObject.add(key, jsonArray);
			}

			jsonArray.add(new JsonPrimitive(statementAddress.getObject().toString()));
		}
	}
}

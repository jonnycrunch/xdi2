package xdi2.tests.core.io.writers;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import junit.framework.TestCase;

import org.junit.Test;

import xdi2.core.Graph;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.io.XDIWriterRegistry;
import xdi2.core.io.writers.XDIDisplayWriter;

public class XDIDisplayWriterTest extends TestCase {

	@Test
	public void testXDIDisplayWriter() throws Exception {
		
		StringBuilder sbJSON = new StringBuilder();
		sbJSON.append("{\"(@!9999!8888)/$add\":[\"@!9999!8888$($msg)$(!1234)\"],");
		sbJSON.append("\"@!9999!8888$($msg)$(!1234)$!($t)/!\":[\"2011-04-10T22:22:22Z\"],");
		sbJSON.append("\"@!9999!8888$($msg)$(!1234)$do/$add\":[");
		sbJSON.append("\"=markus\",");
		sbJSON.append("\"(http://example.com)\",{");
		sbJSON.append("\"=!1111!2222!3$!(+age)/!\":[\"33\"],");
		sbJSON.append("\"=!1111!2222!3$!(+age)/$is+\":[\"+$json$number!\"],");
		sbJSON.append("\"=!1111!2222!3$*(+tel)$!1/!\":[\"+1.206.555.1111\"],");
		sbJSON.append("\"=!1111!2222!3$*(+tel)/+home\":[\"=!1111!2222!3$*(+tel)$!1\"],");
		sbJSON.append("\"=!1111!2222!3$*(+tel)/+work+fax\":[\"=!1111!2222!3$*(+tel)$!2\"]}],");
		sbJSON.append("\"@!9999!8888$($msg)$(!1234)/$do\":[\"=!1111!2222!3$do\"]}");
		String jsonString = sbJSON.toString();
		
		StringBuilder sbXDI = new StringBuilder();
		sbXDI.append("@!9999!8888$($msg)$(!1234)$!($t)\t!\t(data:,2011-04-10T22:22:22Z)\n");
		sbXDI.append("@!9999!8888$($msg)$(!1234)$do\t$add\t=markus\n");
		sbXDI.append("@!9999!8888$($msg)$(!1234)$do\t$add\t(=!1111!2222!3$*(+tel)$!1/!/(data:,+1.206.555.1111))\n");
		sbXDI.append("@!9999!8888$($msg)$(!1234)$do\t$add\t(=!1111!2222!3$!(+age)/!/(data:,33))\n");
		sbXDI.append("@!9999!8888$($msg)$(!1234)$do\t$add\t(=!1111!2222!3$*(+tel)/+work+fax/=!1111!2222!3$*(+tel)$!2)\n");
		sbXDI.append("@!9999!8888$($msg)$(!1234)$do\t$add\t(=!1111!2222!3$*(+tel)/+home/=!1111!2222!3$*(+tel)$!1)\n");
		sbXDI.append("@!9999!8888$($msg)$(!1234)$do\t$add\t(http://example.com)\n");
		sbXDI.append("@!9999!8888$($msg)$(!1234)$do\t$add\t(=!1111!2222!3$!(+age)/$is+/+$json$number!)\n");
		sbXDI.append("@!9999!8888$($msg)$(!1234)\t$do\t=!1111!2222!3$do\n");
		sbXDI.append("(@!9999!8888)\t$add\t@!9999!8888$($msg)$(!1234)");
		String xdiString = sbXDI.toString();
		
		Properties params = new Properties();
		params.setProperty(XDIWriterRegistry.PARAMETER_PRETTY, "1");
		XDIDisplayWriter writer = new XDIDisplayWriter(params);
		Graph graph = (new MemoryGraphFactory()).parseGraph(jsonString);
		StringWriter stringWriter = new StringWriter();
		Writer out = writer.write(graph, stringWriter);
		
		String serializedXDI = out.toString().trim();
		
		assertEquals(xdiString, serializedXDI);
	}
}

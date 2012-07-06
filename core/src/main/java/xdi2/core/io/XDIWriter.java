package xdi2.core.io;



import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.Properties;

import xdi2.core.Graph;

/**
 * Provides methods for writing an XDI graph.
 *
 * @author markus
 */
public interface XDIWriter extends Serializable {

	/**
	 * Writes an XDI graph to a character stream.
	 * @param graph A graph that will be written to the stream.
	 * @param writer The character stream to write to.
	 * @param parameters Optional parameters for the writer.
	 * @return The character stream.
	 */
	public Writer write(Graph graph, Writer writer, Properties parameters) throws IOException;

	/**
	 * Writes an XDI graph to a byte stream.
	 * @param graph A graph that will be written to the stream.
	 * @param stream The byte stream to write to.
	 * @param parameters Optional parameters for the writer.
	 * @return The byte stream.
	 */
	public OutputStream write(Graph graph, OutputStream stream, Properties parameters) throws IOException;

	/**
	 * Returns the format this XDIWriter can write, e.g.
	 * <ul>
	 * <li>XDI/JSON</li>
	 * <li>STATEMENTS</li>
	 * </ul>
	 * @return The format of this XDIWriter.
	 */
	public String getFormat();

	/**
	 * Returns the file extension of this XDIWriter, e.g.
	 * <ul>
	 * <li>.xml</li>
	 * <li>.x3</li>
	 * <li>.txt</li>
	 * </ul>
	 * @return The file extension of this XDIWriter.
	 */
	public String getFileExtension();

	/**
	 * Returns the mime types this XDIWriter can write, e.g.
	 * <ul>
	 * <li>text/xdi</li>
	 * <li>application/xdi+json</li>
	 * </ul>
	 * @return The mime types of this XDIWriter.
	 */
	public MimeType[] getMimeTypes();

	/**
	 * Checks if a given format is supported.
	 * @return True, if supported.
	 */
	public boolean supportsFormat(String format);

	/**
	 * Checks if a given file extension is supported.
	 * @return True, if supported.
	 */
	public boolean supportsFileExtension(String fileExtension);

	/**
	 * Checks if a given mime type is supported.
	 * @return True, if supported.
	 */
	public boolean supportsMimeType(MimeType mimeType);
}

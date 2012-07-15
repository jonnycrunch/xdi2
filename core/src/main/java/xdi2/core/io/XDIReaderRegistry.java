package xdi2.core.io;



import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.io.readers.AutoReader;
import xdi2.core.io.readers.XDIJSONReader;
import xdi2.core.io.readers.XDIStatementsReader;

/**
 * Provides an appropriate XDIReader for a given type.
 *
 * @author markus
 */
public final class XDIReaderRegistry {

	private static final Logger log = LoggerFactory.getLogger(XDIReaderRegistry.class);

	private static String readerClassNames[] = {

		XDIJSONReader.class.getName(),
		XDIStatementsReader.class.getName()
	};

	private static List<Class<XDIReader>> readerClasses;

	private static Map<String, Class<XDIReader>> readerClassesByFormat;
	private static Map<String, Class<XDIReader>> readerClassesByFileExtension;
	private static Map<MimeType, Class<XDIReader>> readerClassesByMimeType;

	static {

		readerClasses = new ArrayList<Class<XDIReader>>();
		readerClassesByFormat = new HashMap<String, Class<XDIReader>>();
		readerClassesByFileExtension = new HashMap<String, Class<XDIReader>>();
		readerClassesByMimeType = new HashMap<MimeType, Class<XDIReader>>();

		for (String readerClassName : readerClassNames) {

			try {

				Class<XDIReader> readerClass = forName(readerClassName);
				readerClasses.add(readerClass);
			} catch (Throwable ex) {

				log.warn("Cannot instantiate XDI Reader " + readerClassName + ": " + ex.getMessage());
				continue;
			}
		}

		if (readerClasses.isEmpty()) throw new RuntimeException("No XDI Readers could be registered.");

		for (Class<XDIReader> readerClass : readerClasses) {

			XDIReader reader;

			try {

				Constructor<XDIReader> constructor = readerClass.getConstructor(Properties.class);
				reader = constructor.newInstance((Properties) null);
			} catch (Exception ex) {

				throw new RuntimeException(ex);
			}

			String format = reader.getFormat();
			String fileExtension = reader.getFileExtension();
			MimeType[] mimeTypes = reader.getMimeTypes();

			if (format != null) readerClassesByFormat.put(format, readerClass);
			if (fileExtension != null) readerClassesByFileExtension.put(fileExtension, readerClass);
			if (mimeTypes != null) for (MimeType mimeType : mimeTypes) readerClassesByMimeType.put(mimeType, readerClass);
		}
	}

	@SuppressWarnings("unchecked")
	private static Class<XDIReader> forName(String readerClassName) throws ClassNotFoundException {

		return (Class<XDIReader>) Class.forName(readerClassName);
	}

	private XDIReaderRegistry() { }

	/**
	 * Returns an XDIReader for the specified format, e.g.
	 * <ul>
	 * <li>XDI/JSON</li>
	 * <li>STATEMENTS</li>
	 * </ul>
	 * @return An XDIReader, or null if no appropriate implementation could be found.
	 */
	public static XDIReader forFormat(String format, Properties parameters) {

		if (AutoReader.FORMAT_NAME.equalsIgnoreCase(format)) return getAuto();

		Class<XDIReader> readerClass = readerClassesByFormat.get(format);
		if (readerClass == null) return null;

		try {

			Constructor<XDIReader> constructor = readerClass.getConstructor(Properties.class);
			return constructor.newInstance(parameters);
		} catch (Exception ex) {

			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns an XDIReader for the specified mime type, e.g.
	 * <ul>
	 * <li>application/xdi+json</li>
	 * <li>text/xdi</li>
	 * </ul>
	 * @param mimeType The desired mime type.
	 * @return An XDIReader, or null if no appropriate implementation could be found.
	 */
	public static XDIReader forMimeType(MimeType mimeType) {

		Class<XDIReader> readerClass = readerClassesByMimeType.get(mimeType);
		if (readerClass == null) return null;

		try {

			Constructor<XDIReader> constructor = readerClass.getConstructor(Properties.class);
			return constructor.newInstance(mimeType.getParameters());
		} catch (Exception ex) {

			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns an XDIReader for the specified file extension, e.g.
	 * <ul>
	 * <ul>
	 * <li>.json</li>
	 * <li>.xdi</li>
	 * </ul>
	 * </ul>
	 * @param fileExtension The desired file extension.
	 * @return An XDIReader, or null if no appropriate implementation could be found.
	 */
	public static XDIReader forFileExtension(String fileExtension, Properties parameters) {

		Class<XDIReader> readerClass = readerClassesByFileExtension.get(fileExtension);
		if (readerClass == null) return null;

		try {

			Constructor<XDIReader> constructor = readerClass.getConstructor(Properties.class);
			return constructor.newInstance(parameters);
		} catch (Exception ex) {

			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns an XDIReader for the default format.
	 * @return An XDIReader.
	 */
	public static XDIReader getDefault() {

		return new AutoReader();
	}

	/**
	 * Returns an XDIReader for auto-detecting the format.
	 * @return An XDIReader.
	 */
	public static AutoReader getAuto() {

		return new AutoReader();
	}

	/**
	 * Returns all formats for which XDIReader implementations exist.
	 * @return A string array of formats.
	 */
	public static String[] getFormats() {

		return readerClassesByFormat.keySet().toArray(new String[readerClassesByFormat.size()]);
	}

	/**
	 * Returns all file extensions for which XDIReader implementations exist.
	 * @return A string array of file extensions.
	 */
	public static String[] getFileExtensions() {

		return readerClassesByFileExtension.keySet().toArray(new String[readerClassesByFileExtension.size()]);
	}

	/**
	 * Returns all mime types for which XDIReader implementations exist.
	 * @return A string array of mime types.
	 */
	public static MimeType[] getMimeTypes() {

		return readerClassesByMimeType.keySet().toArray(new MimeType[readerClassesByMimeType.size()]);
	}
}

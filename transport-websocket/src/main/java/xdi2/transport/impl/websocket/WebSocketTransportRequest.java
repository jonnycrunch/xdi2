package xdi2.transport.impl.websocket;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.transport.TransportRequest;
import xdi2.transport.impl.websocket.endpoint.WebSocketServerMessageHandler;

/**
 * This class represents a WebSocket request to the server.
 * This is used by the WebSocketTransport.
 * 
 * @author markus
 */
public class WebSocketTransportRequest implements TransportRequest {

	private static final Logger log = LoggerFactory.getLogger(WebSocketTransportRequest.class);

	private WebSocketServerMessageHandler webSocketMessageHandler;
	private String requestPath;
	private String negotiatedSubprotocol;
	private Reader reader;

	private WebSocketTransportRequest(WebSocketServerMessageHandler webSocketMessageHandler, String requestPath, String negotiatedSubprotocol, Reader reader) {

		this.webSocketMessageHandler = webSocketMessageHandler;
		this.requestPath = requestPath;
		this.negotiatedSubprotocol = negotiatedSubprotocol;
		this.reader = reader;
	}

	public static WebSocketTransportRequest create(WebSocketServerMessageHandler webSocketMessageHandler, Session session, String contextPath, String endpointPath, Reader reader) {

		String requestUri = session.getRequestURI().getPath();
		if (log.isDebugEnabled()) log.debug("Request URI: " + requestUri);

		String requestPath = requestUri.substring(contextPath.length() + endpointPath.length());
		if (! requestPath.startsWith("/")) requestPath = "/" + requestPath;

		try {

			requestPath = URLDecoder.decode(requestPath, "UTF-8");
		} catch (UnsupportedEncodingException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}

		String negotiatedSubprotocol = session.getNegotiatedSubprotocol();

		return new WebSocketTransportRequest(webSocketMessageHandler, requestPath, negotiatedSubprotocol, reader);
	}

	/*
	 * Getters and setters
	 */

	public WebSocketServerMessageHandler getWebSocketMessageHandler() {

		return this.webSocketMessageHandler;
	}

	public String getRequestPath() {

		return this.requestPath;
	}

	public String getNegotiatedSubprotocol() {

		return this.negotiatedSubprotocol;
	}

	public Reader getReader() {

		return this.reader;
	}
}

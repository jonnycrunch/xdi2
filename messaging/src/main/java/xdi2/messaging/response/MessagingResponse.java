package xdi2.messaging.response;

import java.util.Iterator;

import xdi2.core.Graph;

public interface MessagingResponse {

	/**
	 * Returns the underlying graph of this messaging response.
	 */
	public Graph getGraph();

	/**
	 * Returns the result graphs returned in this messaging response.
	 */
	public Iterator<Graph> getResultGraphs();

	/**
	 * Returns the result graph returned in this messaging response.
	 */
	public Graph getResultGraph();
}

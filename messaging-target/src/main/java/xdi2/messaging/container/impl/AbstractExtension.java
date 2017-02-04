package xdi2.messaging.container.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.container.Extension;
import xdi2.messaging.container.execution.ExecutionContext;
import xdi2.messaging.operations.Operation;


public class AbstractExtension <CONTAINER> implements Extension<CONTAINER> {

	private static final Logger log = LoggerFactory.getLogger(AbstractExtension.class);

	public final static int DEFAULT_INIT_PRIORITY = 100;
	public final static int DEFAULT_SHUTDOWN_PRIORITY = 100;

	private int initPriority;
	private int shutdownPriority;
	private boolean disabled;
	private List<MessageEnvelope> disabledForMessageEnvelope;	// TODO need list to make sure equals() is not used?
	private List<Message> disabledForMessage;				// because ManipulatingInterceptor can change
	private List<Operation> disabledForOperation;			// message envelopes and messages, and then the hashCode can't find it!!!! 

	public AbstractExtension(int initPriority, int shutdownPriority) {

		this.initPriority = initPriority;
		this.shutdownPriority = shutdownPriority;
		this.disabled = false;
		this.disabledForMessageEnvelope = new ArrayList<MessageEnvelope> ();
		this.disabledForMessage = new ArrayList<Message> ();
		this.disabledForOperation = new ArrayList<Operation> ();
	}

	public AbstractExtension() {

		this(DEFAULT_INIT_PRIORITY, DEFAULT_SHUTDOWN_PRIORITY);
	}

	/*
	 * Init and shutdown
	 */

	@Override
	public void init(CONTAINER container) throws Exception {

	}

	@Override
	public void shutdown(CONTAINER container) throws Exception {

	}

	@Override
	public int getInitPriority() {

		return this.initPriority;
	}

	@Override
	public int getShutdownPriority() {

		return this.shutdownPriority;
	}

	/*
	 * Enabled?
	 */

	@Override
	public boolean skip(ExecutionContext executionContext) {

		if (this.disabled) return true;

		if (executionContext != null) {

			boolean disabledForMessageEnvelope = false;
			boolean disabledForMessage = false;
			boolean disabledForOperation = false;

			for (MessageEnvelope currentMessageEnvelope : executionContext.getCurrentMessageEnvelopes()) disabledForMessageEnvelope |= this.disabledForMessageEnvelope.contains(currentMessageEnvelope);
			for (Message currentMessage : executionContext.getCurrentMessages()) disabledForMessage |= this.disabledForMessage.contains(currentMessage);
			for (Operation currentOperation : executionContext.getCurrentOperations()) disabledForOperation |= this.disabledForOperation.contains(currentOperation);

			if (log.isDebugEnabled() && disabledForMessageEnvelope) log.debug("Disabled " + this + " on message envelope ? " + disabledForMessageEnvelope);
			if (log.isDebugEnabled() && disabledForMessage) log.debug("Disabled " + this + " on message ? " + disabledForMessage);
			if (log.isDebugEnabled() && disabledForOperation) log.debug("Disabled " + this + " on operation ? " + disabledForOperation);

			if (disabledForMessageEnvelope) return true;
			if (disabledForMessage) return true;
			if (disabledForOperation) return true;
		}

		return false;
	}

	@Override
	public void setDisabled() {

		this.disabled = true;
	}

	@Override
	public void clearDisabled() {

		this.disabled = false;
	}

	@Override
	public void setDisabledForMessageEnvelope(MessageEnvelope messageEnvelope) {

		if (log.isDebugEnabled()) log.debug("Set disabled " + this + " on message envelope " + messageEnvelope);

		this.disabledForMessageEnvelope.add(messageEnvelope);
	}

	@Override
	public void clearDisabledForMessageEnvelope(MessageEnvelope messageEnvelope) {

		if (! this.disabledForMessageEnvelope.contains(messageEnvelope)) return;
		
		if (log.isDebugEnabled()) log.debug("Clear disabled " + this + " on message envelope " + messageEnvelope);

		this.disabledForMessageEnvelope.remove(messageEnvelope);
	}

	@Override
	public void setDisabledForMessage(Message message) {

		if (log.isDebugEnabled()) log.debug("Set disabled " + this + " on message " + message);

		this.disabledForMessage.add(message);
	}

	@Override
	public void clearDisabledForMessage(Message message) {

		if (! this.disabledForMessage.contains(message)) return;

		if (log.isDebugEnabled()) log.debug("Clear disabled " + this + " on message " + message);

		this.disabledForMessage.remove(message);
	}

	@Override
	public void setDisabledForOperation(Operation operation) {

		if (log.isDebugEnabled()) log.debug("Set disabled " + this + " on operation " + operation);

		this.disabledForOperation.add(operation);
	}

	@Override
	public void clearDisabledForOperation(Operation operation) {

		if (! this.disabledForOperation.contains(operation)) return;

		if (log.isDebugEnabled()) log.debug("Clear disabled " + this + " on operation " + operation);

		this.disabledForOperation.remove(operation);
	}
}

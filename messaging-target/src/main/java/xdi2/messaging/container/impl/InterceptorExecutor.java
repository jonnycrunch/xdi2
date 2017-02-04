package xdi2.messaging.container.impl;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.Graph;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIStatement;
import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.container.MessagingContainer;
import xdi2.messaging.container.exceptions.Xdi2MessagingException;
import xdi2.messaging.container.execution.ExecutionContext;
import xdi2.messaging.container.execution.ExecutionResult;
import xdi2.messaging.container.interceptor.ExecutionResultInterceptor;
import xdi2.messaging.container.interceptor.InterceptorList;
import xdi2.messaging.container.interceptor.InterceptorResult;
import xdi2.messaging.container.interceptor.MessageEnvelopeInterceptor;
import xdi2.messaging.container.interceptor.MessageInterceptor;
import xdi2.messaging.container.interceptor.OperationInterceptor;
import xdi2.messaging.container.interceptor.TargetInterceptor;
import xdi2.messaging.operations.Operation;

public class InterceptorExecutor {

	private static final Logger log = LoggerFactory.getLogger(InterceptorExecutor.class);

	private InterceptorExecutor() {

	}

	/*
	 * Methods for executing interceptors
	 */

	public static InterceptorResult executeMessageEnvelopeInterceptorsBefore(InterceptorList<MessagingContainer> interceptorList, MessageEnvelope messageEnvelope, ExecutionContext executionContext, ExecutionResult executionResult) throws Xdi2MessagingException {

		InterceptorResult interceptorResultBefore = InterceptorResult.DEFAULT;

		for (Iterator<MessageEnvelopeInterceptor> messageEnvelopeInterceptors = findMessageEnvelopeInterceptors(interceptorList); messageEnvelopeInterceptors.hasNext(); ) {

			MessageEnvelopeInterceptor messageEnvelopeInterceptor = messageEnvelopeInterceptors.next();

			if (messageEnvelopeInterceptor.skip(executionContext)) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled message envelope interceptor " + messageEnvelopeInterceptor.getClass().getSimpleName() + " (before).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing message envelope interceptor " + messageEnvelopeInterceptor.getClass().getSimpleName() + " (before).");

			try {

				executionContext.pushInterceptor(messageEnvelopeInterceptor, "MessageEnvelopeInterceptor: before");

				InterceptorResult interceptorResult = messageEnvelopeInterceptor.before(messageEnvelope, executionContext, executionResult);
				interceptorResultBefore = interceptorResultBefore.or(interceptorResult);

				if (interceptorResult.isSkipSiblingInterceptors()) {

					if (log.isDebugEnabled()) log.debug("Skipping sibling message envelope interceptors (before) according to " + messageEnvelopeInterceptor.getClass().getSimpleName() + ".");
					return interceptorResultBefore;
				}
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return interceptorResultBefore;
	}

	public static InterceptorResult executeMessageEnvelopeInterceptorsAfter(InterceptorList<MessagingContainer> interceptorList, MessageEnvelope messageEnvelope, ExecutionContext executionContext, ExecutionResult executionResult) throws Xdi2MessagingException {

		InterceptorResult interceptorResultAfter = InterceptorResult.DEFAULT;

		for (Iterator<MessageEnvelopeInterceptor> messageEnvelopeInterceptors = findMessageEnvelopeInterceptors(interceptorList); messageEnvelopeInterceptors.hasNext(); ) {

			MessageEnvelopeInterceptor messageEnvelopeInterceptor = messageEnvelopeInterceptors.next();

			if (messageEnvelopeInterceptor.skip(executionContext)) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled message envelope interceptor " + messageEnvelopeInterceptor.getClass().getSimpleName() + " (after).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing message envelope interceptor " + messageEnvelopeInterceptor.getClass().getSimpleName() + " (after).");

			try {

				executionContext.pushInterceptor(messageEnvelopeInterceptor, "MessageEnvelopeInterceptor: after");

				InterceptorResult interceptorResult = messageEnvelopeInterceptor.after(messageEnvelope, executionContext, executionResult);
				interceptorResultAfter = interceptorResultAfter.or(interceptorResult);

				if (interceptorResult.isSkipSiblingInterceptors()) {

					if (log.isDebugEnabled()) log.debug("Skipping sibling message envelope interceptors (after) according to " + messageEnvelopeInterceptor.getClass().getSimpleName() + ".");
					return interceptorResultAfter;
				}
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return interceptorResultAfter;
	}

	public static void executeMessageEnvelopeInterceptorsException(InterceptorList<MessagingContainer> interceptorList, MessageEnvelope messageEnvelope, ExecutionContext executionContext, ExecutionResult executionResult, Xdi2MessagingException ex) throws Xdi2MessagingException {

		for (Iterator<MessageEnvelopeInterceptor> messageEnvelopeInterceptors = findMessageEnvelopeInterceptors(interceptorList); messageEnvelopeInterceptors.hasNext(); ) {

			MessageEnvelopeInterceptor messageEnvelopeInterceptor = messageEnvelopeInterceptors.next();

			if (messageEnvelopeInterceptor.skip(executionContext)) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled message envelope interceptor " + messageEnvelopeInterceptor.getClass().getSimpleName() + " (exception).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing message envelope interceptor " + messageEnvelopeInterceptor.getClass().getSimpleName() + " (exception).");

			try {

				executionContext.pushInterceptor(messageEnvelopeInterceptor, "MessageEnvelopeInterceptor: exception");

				messageEnvelopeInterceptor.exception(messageEnvelope, executionContext, executionResult, ex);
			} catch (Exception ex2) {

				throw executionContext.processException(ex2);
			} finally {

				executionContext.popInterceptor();
			}
		}
	}

	public static InterceptorResult executeMessageInterceptorsBefore(InterceptorList<MessagingContainer> interceptorList, Message message, ExecutionContext executionContext, ExecutionResult executionResult) throws Xdi2MessagingException {

		InterceptorResult interceptorResultBefore = InterceptorResult.DEFAULT;

		for (Iterator<MessageInterceptor> messageInterceptors = findMessageInterceptors(interceptorList); messageInterceptors.hasNext(); ) {

			MessageInterceptor messageInterceptor = messageInterceptors.next();

			if (messageInterceptor.skip(executionContext)) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled message interceptor " + messageInterceptor.getClass().getSimpleName() + " (before).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing message interceptor " + messageInterceptor.getClass().getSimpleName() + " (before).");

			try {

				executionContext.pushInterceptor(messageInterceptor, "MessageInterceptor: before");

				InterceptorResult interceptorResult = messageInterceptor.before(message, executionContext, executionResult);
				interceptorResultBefore = interceptorResultBefore.or(interceptorResult);

				if (interceptorResult.isSkipSiblingInterceptors()) {

					if (log.isDebugEnabled()) log.debug("Skipping sibling message interceptors (before) according to " + messageInterceptor.getClass().getSimpleName() + ".");
					return interceptorResultBefore;
				}
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return interceptorResultBefore;
	}

	public static InterceptorResult executeMessageInterceptorsAfter(InterceptorList<MessagingContainer> interceptorList, Message message, ExecutionContext executionContext, ExecutionResult executionResult) throws Xdi2MessagingException {

		InterceptorResult interceptorResultAfter = InterceptorResult.DEFAULT;

		for (Iterator<MessageInterceptor> messageInterceptors = findMessageInterceptors(interceptorList); messageInterceptors.hasNext(); ) {

			MessageInterceptor messageInterceptor = messageInterceptors.next();

			if (messageInterceptor.skip(executionContext)) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled message interceptor " + messageInterceptor.getClass().getSimpleName() + " (after).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing message interceptor " + messageInterceptor.getClass().getSimpleName() + " (after).");

			try {

				executionContext.pushInterceptor(messageInterceptor, "MessageInterceptor: after");

				InterceptorResult interceptorResult = messageInterceptor.after(message, executionContext, executionResult);
				interceptorResultAfter = interceptorResultAfter.or(interceptorResult);

				if (interceptorResult.isSkipSiblingInterceptors()) {

					if (log.isDebugEnabled()) log.debug("Skipping sibling message interceptors (after) according to " + messageInterceptor.getClass().getSimpleName() + ".");
					return interceptorResultAfter;
				}
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return interceptorResultAfter;
	}

	public static InterceptorResult executeOperationInterceptorsBefore(InterceptorList<MessagingContainer> interceptorList, Operation operation, Graph operationResultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		InterceptorResult interceptorResultBefore = InterceptorResult.DEFAULT;

		for (Iterator<OperationInterceptor> operationInterceptors = findOperationInterceptors(interceptorList); operationInterceptors.hasNext(); ) {

			OperationInterceptor operationInterceptor = operationInterceptors.next();

			if (operationInterceptor.skip(executionContext)) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled operation interceptor " + operationInterceptor.getClass().getSimpleName() + " (before).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing operation interceptor " + operationInterceptor.getClass().getSimpleName() + " (before).");

			try {

				executionContext.pushInterceptor(operationInterceptor, "OperationInterceptor: before");

				InterceptorResult interceptorResult = operationInterceptor.before(operation, operationResultGraph, executionContext);
				interceptorResultBefore = interceptorResultBefore.or(interceptorResult);

				if (interceptorResult.isSkipSiblingInterceptors()) {

					if (log.isDebugEnabled()) log.debug("Skipping sibling operation interceptors (before) according to " + operationInterceptor.getClass().getSimpleName() + ".");
					return interceptorResultBefore;
				}
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return interceptorResultBefore;
	}

	public static InterceptorResult executeOperationInterceptorsAfter(InterceptorList<MessagingContainer> interceptorList, Operation operation, Graph operationResultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		InterceptorResult interceptorResultAfter = InterceptorResult.DEFAULT;

		for (Iterator<OperationInterceptor> operationInterceptors = findOperationInterceptors(interceptorList); operationInterceptors.hasNext(); ) {

			OperationInterceptor operationInterceptor = operationInterceptors.next();

			if (operationInterceptor.skip(executionContext)) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled operation interceptor " + operationInterceptor.getClass().getSimpleName() + " (after).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing operation interceptor " + operationInterceptor.getClass().getSimpleName() + " (after).");

			try {

				executionContext.pushInterceptor(operationInterceptor, "OperationInterceptor: after");

				InterceptorResult interceptorResult = operationInterceptor.after(operation, operationResultGraph, executionContext);
				interceptorResultAfter = interceptorResultAfter.or(interceptorResult);

				if (interceptorResult.isSkipSiblingInterceptors()) {

					if (log.isDebugEnabled()) log.debug("Skipping sibling operation interceptors (after) according to " + operationInterceptor.getClass().getSimpleName() + ".");
					return interceptorResultAfter;
				}
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return interceptorResultAfter;
	}

	public static XDIAddress executeTargetInterceptorsAddress(InterceptorList<MessagingContainer> interceptorList, XDIAddress targetAddress, Operation operation, Graph operationResultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		for (Iterator<TargetInterceptor> targetInterceptors = findTargetInterceptors(interceptorList); targetInterceptors.hasNext(); ) {

			TargetInterceptor targetInterceptor = targetInterceptors.next();

			if (targetInterceptor.skip(executionContext)) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled target interceptor " + targetInterceptor.getClass().getSimpleName() + " with operation " + operation.getOperationXDIAddress() + " on address " + targetAddress + ".");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing target interceptor " + targetInterceptor.getClass().getSimpleName() + " with operation " + operation.getOperationXDIAddress() + " on address " + targetAddress + ".");

			try {

				executionContext.pushInterceptor(targetInterceptor, "TargetInterceptor: address: " + targetAddress);

				targetAddress = targetInterceptor.targetAddress(targetAddress, operation, operationResultGraph, executionContext);

				if (targetAddress == null) {

					if (log.isDebugEnabled()) log.debug("Address has been skipped by interceptor " + targetInterceptor.getClass().getSimpleName() + ".");
					return null;
				}

				if (log.isDebugEnabled()) log.debug("Interceptor " + targetInterceptor.getClass().getSimpleName() + " returned address: " + targetAddress + ".");
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return targetAddress;
	}

	public static XDIStatement executeTargetInterceptorsStatement(InterceptorList<MessagingContainer> interceptorList, XDIStatement targetStatement, Operation operation, Graph operationResultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		for (Iterator<TargetInterceptor> targetInterceptors = findTargetInterceptors(interceptorList); targetInterceptors.hasNext(); ) {

			TargetInterceptor targetInterceptor = targetInterceptors.next();

			if (targetInterceptor.skip(executionContext)) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled target interceptor " + targetInterceptor.getClass().getSimpleName() + " with operation " + operation.getOperationXDIAddress() + " on statement " + targetStatement + ".");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing target interceptor " + targetInterceptor.getClass().getSimpleName() + " with operation " + operation.getOperationXDIAddress() + " on statement " + targetStatement + ".");

			try {

				executionContext.pushInterceptor(targetInterceptor, "TargetInterceptor: statement: " + targetStatement);

				targetStatement = targetInterceptor.targetStatement(targetStatement, operation, operationResultGraph, executionContext);

				if (targetStatement == null) {

					if (log.isDebugEnabled()) log.debug("Statement has been skipped by interceptor " + targetInterceptor.getClass().getSimpleName() + ".");
					return null;
				}

				if (log.isDebugEnabled()) log.debug("Interceptor " + targetInterceptor.getClass().getSimpleName() + " returned statement: " + targetStatement + ".");
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}

		return targetStatement;
	}

	public static void executeResultInterceptorsFinish(InterceptorList<MessagingContainer> interceptorList, MessagingContainer messagingContainer, ExecutionContext executionContext, ExecutionResult executionResult) throws Xdi2MessagingException {

		for (Iterator<ExecutionResultInterceptor> executionResultInterceptors = findExecutionResultInterceptors(interceptorList); executionResultInterceptors.hasNext(); ) {

			ExecutionResultInterceptor executionResultInterceptor = executionResultInterceptors.next();

			if (executionResultInterceptor.skip(executionContext)) {

				if (log.isDebugEnabled()) log.debug("Skipping disabled execution result interceptor " + executionResultInterceptor.getClass().getSimpleName() + " (finish).");
				continue;
			}

			if (log.isDebugEnabled()) log.debug("Executing execution result interceptor " + executionResultInterceptor.getClass().getSimpleName() + " (finish).");

			try {

				executionContext.pushInterceptor(executionResultInterceptor, "ExecutionResultInterceptor: finish");

				executionResultInterceptor.finish(messagingContainer, executionContext, executionResult);
			} catch (Exception ex) {

				throw executionContext.processException(ex);
			} finally {

				executionContext.popInterceptor();
			}
		}
	}

	/*
	 * Methods for finding interceptors
	 */

	public static Iterator<MessageEnvelopeInterceptor> findMessageEnvelopeInterceptors(InterceptorList<MessagingContainer> interceptorList) {

		return interceptorList.findInterceptors(MessageEnvelopeInterceptor.class);
	}

	public static Iterator<MessageInterceptor> findMessageInterceptors(InterceptorList<MessagingContainer> interceptorList) {

		return interceptorList.findInterceptors(MessageInterceptor.class);
	}

	public static Iterator<OperationInterceptor> findOperationInterceptors(InterceptorList<MessagingContainer> interceptorList) {

		return interceptorList.findInterceptors(OperationInterceptor.class);
	}

	public static Iterator<TargetInterceptor> findTargetInterceptors(InterceptorList<MessagingContainer> interceptorList) {

		return interceptorList.findInterceptors(TargetInterceptor.class);
	}

	public static Iterator<ExecutionResultInterceptor> findExecutionResultInterceptors(InterceptorList<MessagingContainer> interceptorList) {

		return interceptorList.findInterceptors(ExecutionResultInterceptor.class);
	}
}

package xdi2.messaging.tests.target.contributor;

import xdi2.core.Graph;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIStatement;
import xdi2.messaging.context.ExecutionContext;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.operations.GetOperation;
import xdi2.messaging.target.contributor.AbstractContributor;
import xdi2.messaging.target.contributor.ContributorMount;
import xdi2.messaging.target.contributor.ContributorResult;

@ContributorMount(contributorAddresses={"(#test)"})
public class TestContributor3 extends AbstractContributor {

	@Override
	public ContributorResult executeGetOnAddress(
			XDIAddress[] contributorAddresses,
			XDIAddress contributorsAddress,
			XDIAddress relativeTargetAddress,
			GetOperation operation,
			Graph resultGraph,
			ExecutionContext executionContext) throws Xdi2MessagingException {

		resultGraph.setStatement(XDIStatement.fromRelationComponents(
				XDIAddress.create("" + contributorsAddress + "=markus"),
				XDIAddress.create("" + "#friend"),
				XDIAddress.create("" + contributorsAddress + "=animesh")));

		return ContributorResult.DEFAULT;
	}
}

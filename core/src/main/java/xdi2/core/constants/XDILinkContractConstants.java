package xdi2.core.constants;

import xdi2.core.features.multiplicity.Multiplicity;
import xdi2.core.xri3.impl.XDI3Segment;
import xdi2.core.xri3.impl.XDI3SubSegment;

/**
 * Constants for XDI link contracts.
 * 
 * @author markus
 */
public final class XDILinkContractConstants {

	public static final XDI3Segment XRI_S_DO = new XDI3Segment("$do");

	public static final XDI3Segment XRI_S_IS_DO = new XDI3Segment("$is$do");

	public static final XDI3SubSegment XRI_SS_DO = new XDI3SubSegment("$do");
	public static final XDI3SubSegment XRI_SS_IF = new XDI3SubSegment("$if");

	public static final XDI3Segment XRI_S_GET = new XDI3Segment("$get");
	public static final XDI3Segment XRI_S_ALL = new XDI3Segment("$all");

	public static final XDI3Segment XRI_S_ADD = new XDI3Segment("$add");
	public static final XDI3Segment XRI_S_MOD = new XDI3Segment("$mod");

	public static final XDI3Segment XRI_S_DEL = new XDI3Segment("$del");
	public static final XDI3Segment XRI_S_VARIABLE_REF = new XDI3Segment("($)");

	public static final XDI3SubSegment XRI_SS_GET = new XDI3SubSegment("$get");
	public static final XDI3SubSegment XRI_SS_ALL = new XDI3SubSegment("$all");
	public static final XDI3SubSegment XRI_SS_ADD = new XDI3SubSegment("$add");
	public static final XDI3SubSegment XRI_SS_MOD = new XDI3SubSegment("$mod");
	public static final XDI3SubSegment XRI_SS_DEL = new XDI3SubSegment("$del");

	public static final XDI3SubSegment XRI_SS_AND = Multiplicity.collectionArcXri(new XDI3SubSegment("$and"));
	public static final XDI3SubSegment XRI_SS_OR = Multiplicity.collectionArcXri(new XDI3SubSegment("$or"));
	public static final XDI3SubSegment XRI_SS_NOT = Multiplicity.attributeSingletonArcXri(new XDI3SubSegment("$not"));
	public static final XDI3SubSegment XRI_SS_VARIABLE_REF = new XDI3SubSegment("($)");

	private XDILinkContractConstants() { }
}

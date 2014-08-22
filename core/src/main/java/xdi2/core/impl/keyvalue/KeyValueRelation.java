package xdi2.core.impl.keyvalue;

import xdi2.core.Relation;
import xdi2.core.impl.AbstractRelation;
import xdi2.core.syntax.XDIAddress;

public class KeyValueRelation extends AbstractRelation implements Relation {

	private static final long serialVersionUID = -2152877877561935106L;

	private KeyValueStore keyValueStore;
	private String key;

	private XDIAddress address;
	private XDIAddress targetContextNodeAddress;

	KeyValueRelation(KeyValueContextNode contextNode, KeyValueStore keyValueStore, String key, XDIAddress address, XDIAddress targetContextNodeAddress) {

		super(contextNode);

		this.keyValueStore = keyValueStore;
		this.key = key;

		this.address = address;
		this.targetContextNodeAddress = targetContextNodeAddress;
	}

	@Override
	public XDIAddress getAddress() {

		return this.address;
	}

	@Override
	public XDIAddress getTargetContextNodeAddress() {

		return this.targetContextNodeAddress;
	}

	/*
	 * Misc methods
	 */

	KeyValueStore getKeyValueStore() {

		return this.keyValueStore;
	}

	String getKey() {

		return this.key;
	}
}

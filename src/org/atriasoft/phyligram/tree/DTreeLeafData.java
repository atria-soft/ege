package org.atriasoft.phyligram.tree;

class DTreeLeafData<INTERNAL_DATA_TYPE> extends DTree {
	Object dataPointer = null;
	
	public DTreeLeafData(final INTERNAL_DATA_TYPE dataPointer) {
		super();
		this.dataPointer = dataPointer;
	}
	
	@Override
	boolean isLeaf() {
		return true;
	}
}
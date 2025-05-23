package org.atriasoft.phyligram.tree;

class DTreeLeafInt extends DTree {
	int dataInt0 = 0;
	int dataInt1 = 0;
	
	public DTreeLeafInt(final int dataInt0, final int dataInt1) {
		this.dataInt0 = dataInt0;
		this.dataInt1 = dataInt1;
	}
	
	@Override
	boolean isLeaf() {
		return true;
	}
}

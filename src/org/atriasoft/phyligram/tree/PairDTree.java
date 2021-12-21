package org.atriasoft.phyligram.tree;

import java.util.Set;

public record PairDTree(
		DTree first,
		DTree second) {
	public static int countInSet(final Set<PairDTree> values, final PairDTree sample) {
		int count = 0;
		for (final PairDTree elem : values) {
			if (elem.first != sample.first) {
				continue;
			}
			if (elem.second != sample.second) {
				continue;
			}
			count++;
		}
		return count;
	}
	
	public PairDTree(final DTree first, final DTree second) {
		if (first.uid < second.uid) {
			this.first = first;
			this.second = second;
		} else {
			this.first = second;
			this.second = first;
			
		}
	}
	
	@Override
	public String toString() {
		return "PairDTree [first=" + this.first.uid + ", second=" + this.second.uid + "]";
	}
}

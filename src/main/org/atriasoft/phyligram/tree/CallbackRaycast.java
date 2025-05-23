package org.atriasoft.phyligram.tree;

import org.atriasoft.phyligram.math.Ray;

public interface CallbackRaycast {
	public float callback(DTree node, Ray ray);
}
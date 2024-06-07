/** @file
 * @author Edouard DUPIN
 * @copyright 2011, Edouard DUPIN, all right reserved
 * @license MPL v2.0 (see license file)
 */
package org.atriasoft.phyligram.shape;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ege.internal.Log;

public class Concave extends Shape {
	private List<Vector3f> vertexes = new ArrayList<>();
	
	private final List<Integer> indices = new ArrayList<>();
	
	public void addTriangle(final List<Integer> index) {
		/*
		if (m_indices.size() == 0) {
			m_indices = _index;
			return;
		}
		*/
		if (index.size() % 3 != 0) {
			LOGGER.error("wrong number of faces : {} ==> not a multiple of 3", index.size());
			return;
		}
		for (final Integer it : index) {
			this.indices.add(it);
		}
	}
	
	public void clear() {
		this.vertexes.clear();
		this.indices.clear();
	}
	
	public List<Integer> getIndices() {
		return this.indices;
	}
	
	public List<Vector3f> getVertex() {
		return this.vertexes;
	}
	
	@Override
	public boolean parse(final String _line) {
		/*
		if (super.parse(_line) == true) {
			return true;
		}
		// TODO ...
		*/
		return false;
	}
	
	public void setListOfVertex(final List<Vector3f> vertexes) {
		this.vertexes = vertexes;
	}
}

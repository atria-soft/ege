package org.atriasoft.arkon;
/* I use the TriangleMesh from my dual mesh library, but I add fields to it,
 * so I'm declaring that here for type checking purposes. */

//#import TriangleMesh from'@redblobgames/dual-mesh';

class Mesh extends TriangleMesh {
	float[] s_length; /* indexed on s */
}

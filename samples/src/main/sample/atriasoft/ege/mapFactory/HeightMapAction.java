package sample.atriasoft.ege.mapFactory;

import sample.atriasoft.ege.mapFactory.model.Map;

public class HeightMapAction implements MapAction {
	private final Ground ground;
	private final Map map;
	private final float[][] before;
	private final float[][] after;

	public HeightMapAction(final Ground ground, final Map map, final float[][] before, final float[][] after) {
		this.ground = ground;
		this.map = map;
		this.before = before;
		this.after = after;
	}

	@Override
	public void undo() {
		restoreHeightMap(this.before);
	}

	@Override
	public void redo() {
		restoreHeightMap(this.after);
	}

	private void restoreHeightMap(final float[][] data) {
		for (int y = 0; y < this.ground.sizeY; y++) {
			for (int x = 0; x < this.ground.sizeX; x++) {
				this.ground.heightMap[y][x] = data[y][x];
			}
		}
		this.ground.updateMesh();
		this.map.updateEntityPositions();
	}

	public static float[][] snapshotHeightMap(final Ground ground) {
		final float[][] snapshot = new float[ground.sizeY][ground.sizeX];
		for (int y = 0; y < ground.sizeY; y++) {
			for (int x = 0; x < ground.sizeX; x++) {
				snapshot[y][x] = ground.heightMap[y][x];
			}
		}
		return snapshot;
	}

	public static boolean hasChanges(final float[][] before, final float[][] after, final int sizeY, final int sizeX) {
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				if (before[y][x] != after[y][x]) {
					return true;
				}
			}
		}
		return false;
	}
}

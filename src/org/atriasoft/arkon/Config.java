package org.atriasoft.arkon;
/*
 * From https://www.redblobgames.com/maps/mapgen4/
 * Copyright 2018 Red Blob Games <redblobgames@gmail.com>
 * License: Apache v2.0 <http://www.apache.org/licenses/LICENSE-2.0.html>
 *
 * Configuration parameters shared by the point precomputation and the
 * map generator. Some of these objects are empty because they will be
 * filled in by the map generator.
 */

class Config {
	public class ConfinElement {
		public float initialValue;
		public float min;
		public float max;
		
		public ConfinElement(float initialValue, float min, float max) {
			this.initialValue = initialValue;
			this.min = min;
			this.max = max;
		}
		
	}
	
	public int spacing = 5;
	public int mountainSpacing = 35;
	public int mountainDensity = 1500;
	
	public ConfinElement mesh_seed = new ConfinElement(187, 1, 1 << 30);
	public ConfinElement mesh_island = new ConfinElement(0.5f, 0, 1);
	public ConfinElement mesh_noisy_coastlines = new ConfinElement(0.01f, 0, 0.1f);
	public ConfinElement mesh_hill_height = new ConfinElement(0.02f, 0, 0.1f);
	public ConfinElement mesh_mountain_jagged = new ConfinElement(0, 0, 1);
	public ConfinElement mesh_mountain_sharpness = new ConfinElement(10, 9.5f, 12.5f);
	public ConfinElement mesh_ocean_depth = new ConfinElement(1.5f, 1, 3);
	public ConfinElement biomes_wind_angle_deg = new ConfinElement(0, 0, 360);
	public ConfinElement biomes_raininess = new ConfinElement(0.9f, 0, 2);
	public ConfinElement biomes_rain_shadow = new ConfinElement(0.5f, 0.1f, 2);
	public ConfinElement biomes_evaporation = new ConfinElement(0.5f, 0, 1);
	public ConfinElement rivers_lg_min_flow = new ConfinElement(2.7f, -5, 5);
	public ConfinElement rivers_lg_river_width = new ConfinElement(-2.7f, -5, 5);
	public ConfinElement rivers_flow = new ConfinElement(0.2f, 0, 1);
	public ConfinElement render_zoom = new ConfinElement(100 / 480, 100 / 1000, 100 / 50);
	public ConfinElement render_x = new ConfinElement(500, 0, 1000);
	public ConfinElement render_y = new ConfinElement(500, 0, 1000);
	public ConfinElement render_light_angle_deg = new ConfinElement(80, 0, 360);
	public ConfinElement render_slope = new ConfinElement(2, 0, 5);
	public ConfinElement render_flat = new ConfinElement(2.5f, 0, 5);
	public ConfinElement render_ambient = new ConfinElement(0.25f, 0, 1);
	public ConfinElement render_overhead = new ConfinElement(30, 0, 60);
	public ConfinElement render_tilt_deg = new ConfinElement(0, 0, 90);
	public ConfinElement render_rotate_deg = new ConfinElement(0, -180, 180);
	public ConfinElement render_mountain_height = new ConfinElement(50, 0, 250);
	public ConfinElement render_outline_depth = new ConfinElement(1, 0, 2);
	public ConfinElement render_outline_strength = new ConfinElement(15, 0, 30);
	public ConfinElement render_outline_threshold = new ConfinElement(0, 0, 100);
	public ConfinElement render_outline_coast = new ConfinElement(0, 0, 1);
	public ConfinElement render_outline_water = new ConfinElement(10.0f, 0, 20); // things start going wrong when this is high
	public ConfinElement render_biome_colors = new ConfinElement(1, 0, 1);
	
}

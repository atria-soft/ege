package sample.atriasoft.ege.mapFactory.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.atriasoft.ege.Entity;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.components.ComponentMesh;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentRenderMeshPalette;
import org.atriasoft.ege.components.ComponentTexturePalette;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Quaternion;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sample.atriasoft.ege.mapFactory.Ground;

public class MapProject {
	private static final Logger LOGGER = LoggerFactory.getLogger(MapProject.class);
	private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	private MapProject() {
	}

	public static void save(final Path file, final Map map) throws IOException {
		final ObjectNode root = MAPPER.createObjectNode();

		// Ground
		final ObjectNode groundNode = root.putObject("ground");
		final Ground ground = map.ground;
		groundNode.put("sizeX", ground.sizeX);
		groundNode.put("sizeY", ground.sizeY);

		final ArrayNode palettes = groundNode.putArray("palettes");
		palettes.add(ground.baseNamePalette);
		palettes.add(ground.baseNamePalette2);
		palettes.add(ground.baseNamePalette3);
		palettes.add(ground.baseNamePalette4);

		final ArrayNode heightMapNode = groundNode.putArray("heightMap");
		for (int yyy = 0; yyy < ground.sizeY; yyy++) {
			final ArrayNode row = heightMapNode.addArray();
			for (int xxx = 0; xxx < ground.sizeX; xxx++) {
				row.add(ground.heightMap[yyy][xxx]);
			}
		}

		final ArrayNode colorMapNode = groundNode.putArray("colorMap");
		for (int yyy = 0; yyy < ground.sizeY; yyy++) {
			final ArrayNode row = colorMapNode.addArray();
			for (int xxx = 0; xxx < ground.sizeX * 2; xxx++) {
				row.add(ground.colorMap[yyy][xxx]);
			}
		}

		// Entities
		final ArrayNode entitiesNode = root.putArray("entities");
		for (final Entity entity : map.placedEntities) {
			final String meshPath = map.entityMeshPaths.get(entity);
			if (meshPath == null) {
				continue;
			}
			final ComponentPosition posComp = (ComponentPosition) entity.getComponent("position");
			if (posComp == null) {
				continue;
			}
			final Transform3D transform = posComp.getTransform();

			final ObjectNode entityNode = entitiesNode.addObject();
			entityNode.put("meshFile", meshPath);

			final ArrayNode posNode = entityNode.putArray("position");
			posNode.add(transform.position().x());
			posNode.add(transform.position().y());
			posNode.add(transform.position().z());

			final ArrayNode oriNode = entityNode.putArray("orientation");
			oriNode.add(transform.orientation().x());
			oriNode.add(transform.orientation().y());
			oriNode.add(transform.orientation().z());
			oriNode.add(transform.orientation().w());

			final ArrayNode scaleNode = entityNode.putArray("scale");
			scaleNode.add(transform.scale().x());
			scaleNode.add(transform.scale().y());
			scaleNode.add(transform.scale().z());
		}

		MAPPER.writeValue(file.toFile(), root);
		LOGGER.info("Project saved to {}", file);
	}

	public static void load(final Path file, final Map map, final Environement env) throws IOException {
		final JsonNode root = MAPPER.readTree(Files.readString(file));

		// Clear existing entities
		for (final Entity entity : map.placedEntities) {
			env.rmEntity(entity);
		}
		map.placedEntities.clear();
		map.entityMeshPaths.clear();

		// Ground
		final JsonNode groundNode = root.get("ground");
		if (groundNode != null) {
			final Ground ground = map.ground;

			final JsonNode palettesNode = groundNode.get("palettes");
			if (palettesNode != null && palettesNode.size() >= 4) {
				ground.baseNamePalette = palettesNode.get(0).asText();
				ground.baseNamePalette2 = palettesNode.get(1).asText();
				ground.baseNamePalette3 = palettesNode.get(2).asText();
				ground.baseNamePalette4 = palettesNode.get(3).asText();
			}

			final JsonNode heightMapNode = groundNode.get("heightMap");
			if (heightMapNode != null) {
				for (int yyy = 0; yyy < ground.sizeY && yyy < heightMapNode.size(); yyy++) {
					final JsonNode row = heightMapNode.get(yyy);
					for (int xxx = 0; xxx < ground.sizeX && xxx < row.size(); xxx++) {
						ground.heightMap[yyy][xxx] = (float) row.get(xxx).asDouble();
					}
				}
			}

			final JsonNode colorMapNode = groundNode.get("colorMap");
			if (colorMapNode != null) {
				for (int yyy = 0; yyy < ground.sizeY && yyy < colorMapNode.size(); yyy++) {
					final JsonNode row = colorMapNode.get(yyy);
					for (int xxx = 0; xxx < ground.sizeX * 2 && xxx < row.size(); xxx++) {
						ground.colorMap[yyy][xxx] = row.get(xxx).asText();
					}
				}
			}

			ground.updateMesh();
		}

		// Entities
		final JsonNode entitiesNode = root.get("entities");
		if (entitiesNode != null) {
			for (final JsonNode entityNode : entitiesNode) {
				final String meshFile = entityNode.get("meshFile").asText();

				final JsonNode posNode = entityNode.get("position");
				final Vector3f position = new Vector3f(
						(float) posNode.get(0).asDouble(),
						(float) posNode.get(1).asDouble(),
						(float) posNode.get(2).asDouble());

				final JsonNode oriNode = entityNode.get("orientation");
				final Quaternion orientation = new Quaternion(
						(float) oriNode.get(0).asDouble(),
						(float) oriNode.get(1).asDouble(),
						(float) oriNode.get(2).asDouble(),
						(float) oriNode.get(3).asDouble());

				final JsonNode scaleNode = entityNode.get("scale");
				final Vector3f scale = new Vector3f(
						(float) scaleNode.get(0).asDouble(),
						(float) scaleNode.get(1).asDouble(),
						(float) scaleNode.get(2).asDouble());

				final Entity entity = new Entity(env);
				entity.addComponent(new ComponentPosition(new Transform3D(position, orientation, scale)));
				final Uri meshUri = new Uri("FILE", meshFile);
				entity.addComponent(new ComponentMesh(meshUri));
				entity.addComponent(new ComponentTexturePalette(meshUri));
				entity.addComponent(new ComponentRenderMeshPalette(
						new Uri("DATA", "basicPalette.vert"),
						new Uri("DATA", "basicPalette.frag")));
				env.addEntity(entity);
				map.placedEntities.add(entity);
				map.entityMeshPaths.put(entity, meshFile);
			}
		}

		LOGGER.info("Project loaded from {} ({} entities)", file, map.placedEntities.size());
	}
}

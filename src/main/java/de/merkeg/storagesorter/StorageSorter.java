package de.merkeg.storagesorter;

import de.merkeg.storagesorter.config.Config;
import de.merkeg.storagesorter.event.AutoSortingEvent;
import de.merkeg.storagesorter.event.ControllerSelectionEvent;
import de.merkeg.storagesorter.event.StorageSelectionEvent;
import lombok.SneakyThrows;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static de.merkeg.storagesorter.SharedStorageData.configPath;
import static de.merkeg.storagesorter.SharedStorageData.storagePath;

public class StorageSorter implements ModInitializer {
	public static final String MOD_ID = "storage-sorter";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		LOGGER.info("Loading Mod");
		AttackBlockCallback.EVENT.register(new ControllerSelectionEvent());
		UseBlockCallback.EVENT.register(new StorageSelectionEvent());
		ServerTickEvents.START_WORLD_TICK.register(new AutoSortingEvent());

		loadPersistentData();
	}

	public void loadPersistentData() {
		LOGGER.info("Loading persistent data");

		Path configDir = FabricLoader.getInstance().getConfigDir();
		Path modConfigDir = configDir.resolve(MOD_ID);
    try {
      Files.createDirectories(modConfigDir);

			configPath = modConfigDir.resolve("config.json");
			storagePath = modConfigDir.resolve("storage.json");

			StorageSystem[] systems = loadOrCreateWithDefaults(storagePath, new StorageSystem[]{});
			if(systems != null) {
				SharedStorageData.storageSystems = Arrays.stream(systems).collect(Collectors.toSet());
			}

			Config.instance = loadOrCreateWithDefaults(configPath, new Config());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

	@SneakyThrows
  private static <T> T loadOrCreateWithDefaults(Path path, T def) {
		if(!Files.exists(path)) {
			try (Writer writer = Files.newBufferedWriter(path)) {
				SharedStorageData.gson.toJson(def, writer);
			}
		}
		try (Reader reader = Files.newBufferedReader(path)) {
			return (T) SharedStorageData.gson.fromJson(reader, def.getClass());
		}
	}


}
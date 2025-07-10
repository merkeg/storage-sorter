package de.merkeg.storagesorter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.merkeg.storagesorter.config.Config;
import de.merkeg.storagesorter.event.AutoSortingEvent;
import de.merkeg.storagesorter.event.ControllerSelectionEvent;
import de.merkeg.storagesorter.event.StorageEvent;
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
import java.util.Arrays;
import java.util.stream.Collectors;

import static de.merkeg.storagesorter.SharedStorageData.configPath;
import static de.merkeg.storagesorter.SharedStorageData.storagePath;

public class StorageSorter implements ModInitializer {
	public static final String MOD_ID = "storage-sorter";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Loading Mod");
		AttackBlockCallback.EVENT.register(new ControllerSelectionEvent());
		UseBlockCallback.EVENT.register(new StorageEvent());
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


			if(!Files.exists(storagePath)) Files.write(storagePath, Arrays.stream(new String[]{"[]"}).toList(), StandardCharsets.UTF_8);

			if(!Files.exists(configPath)) {
				try (Writer writer = Files.newBufferedWriter(configPath)) {
					Config config = new Config();
					SharedStorageData.gson.toJson(config, writer);
				}
			}

			try (Reader reader = Files.newBufferedReader(configPath)) {
				Config.instance = SharedStorageData.gson.fromJson(reader, Config.class);
			}

			try (Reader reader = Files.newBufferedReader(storagePath)) {

				StorageSystem[] systems = SharedStorageData.gson.fromJson(reader, StorageSystem[].class);

				if(systems == null) return;

				SharedStorageData.storageSystems = Arrays.stream(systems)
								.collect(Collectors.toSet());
			}

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


}
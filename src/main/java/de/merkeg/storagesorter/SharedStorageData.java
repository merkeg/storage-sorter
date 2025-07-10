package de.merkeg.storagesorter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.merkeg.storagesorter.config.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.nio.file.Path;
import java.util.*;

public class SharedStorageData {

  public static Map<PlayerEntity, StorageSystem> selectedSystems = new HashMap<>();

  public static Set<StorageSystem> storageSystems = new HashSet<>();

  public static Path storagePath;
  public static Path configPath;
  public static Gson gson = new GsonBuilder().setPrettyPrinting().create();



  public static void selectController(PlayerEntity player, BlockPos pos) {
    selectedSystems.put(player, StorageLoader.getOrCreateStorage(pos, player.getWorld()));
  }
}

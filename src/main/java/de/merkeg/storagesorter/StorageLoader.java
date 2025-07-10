package de.merkeg.storagesorter;

import lombok.SneakyThrows;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.Writer;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class StorageLoader {

  public static StorageSystem getOrCreateStorage(BlockPos blockPos, World world) {

    if(!hasStorage(blockPos)) {
      StorageSystem storageSystem = StorageSystem.builder().controller(blockPos).worldName(world.getRegistryKey().getValue().toString()).storage(new HashSet<>()).build();
      SharedStorageData.storageSystems.add(storageSystem);
      return storageSystem;
    }

    return SharedStorageData.storageSystems.stream().filter(s -> s.getController().equals(blockPos)).findFirst().get();
  }

  public static boolean hasStorage(BlockPos blockPos) {
    return SharedStorageData.storageSystems.stream().anyMatch(s -> s.getController().equals(blockPos));
  }

  public static Set<StorageSystem> getStorageSystemsForWorld(String worldName) {
    return SharedStorageData.storageSystems.stream().filter(s -> s.getWorldName().equals(worldName)).collect(Collectors.toSet());
  }

  @SneakyThrows
  public static void saveStorageData() {
    try (Writer writer = Files.newBufferedWriter(SharedStorageData.storagePath)) {
      SharedStorageData.gson.toJson(SharedStorageData.storageSystems, writer);
    }
  }
}

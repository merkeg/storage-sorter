package de.merkeg.storagesorter.config;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class Config {
  public transient static Config instance;

  private String controller = "minecraft:barrel";
  private Set<String> storage = Set.of("minecraft:chest","minecraft:trapped_chest","minecraft:barrel");
  private String tool = "minecraft:wooden_hoe";
  private int sortCooldownMs = 20;

  public static Block getControllerBlock() {
    return Registries.BLOCK.get(Identifier.of(instance.controller));
  }

  public static Set<Block> getStorageBlocks() {
    return instance.storage.stream().map(s -> Registries.BLOCK.get(Identifier.of(s))).collect(Collectors.toSet());
  }

  public static Item getToolItem() {
    return Registries.ITEM.get(Identifier.of(instance.tool));
  }

}

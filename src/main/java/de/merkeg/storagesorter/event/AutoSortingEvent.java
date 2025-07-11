package de.merkeg.storagesorter.event;

import de.merkeg.storagesorter.SharedStorageData;
import de.merkeg.storagesorter.StorageLoader;
import de.merkeg.storagesorter.StorageSorter;
import de.merkeg.storagesorter.StorageSystem;
import de.merkeg.storagesorter.config.Config;
import de.merkeg.storagesorter.util.InventoryUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public class AutoSortingEvent implements ServerTickEvents.StartWorldTick {

  private int tickCounter = 0;

  @Override
  public void onStartTick(ServerWorld world) {
    tickCounter++;

    if(tickCounter < Config.instance.getSortCooldownMs()) {
      return;
    }

    tickCounter = 0;

    String worldName = world.getRegistryKey().getValue().toString();
    Set<StorageSystem> storageSystemsForWorld = StorageLoader.getStorageSystemsForWorld(worldName);

    if(storageSystemsForWorld.isEmpty()) {
      return;
    }

    boolean changes = false;
    for(StorageSystem storageSystem : storageSystemsForWorld) {
      BlockState controllerState = world.getBlockState(storageSystem.getController());

      if(!controllerState.getBlock().equals(Config.getControllerBlock())) {
        StorageSorter.LOGGER.warn("Controller block was destroyed, removing storage");
        SharedStorageData.storageSystems.remove(storageSystem);
        changes = true;
        continue;
      }

      Inventory controllerInventory = (Inventory) world.getBlockEntity(storageSystem.getController());

      Set<Inventory> storage = new HashSet<>();
      Set<BlockPos> visited = new HashSet<>();
      for(BlockPos blockPos : storageSystem.getStorage()) {

        if(visited.contains(blockPos)) continue;
        visited.add(blockPos);

        BlockState blockState = world.getBlockState(blockPos);
        if(!Config.getStorageBlocks().contains(blockState.getBlock())) {
          StorageSorter.LOGGER.warn("Storage block was destroyed, removing block");
          storageSystem.getStorage().remove(blockPos);
          changes = true;
          break;
        }


        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        Inventory inventory = null;
        if (blockEntity instanceof ChestBlockEntity chest) {
          inventory = ChestBlock.getInventory((ChestBlock) blockState.getBlock(), blockState, world, blockPos, false);

          if(blockState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
            Direction face = ChestBlock.getFacing(blockState);
            BlockEntity neighbour = world.getBlockEntity(blockPos.offset(face));
            if(neighbour instanceof ChestBlockEntity) {
              visited.add(neighbour.getPos());
            }
          }

        } else if (blockEntity instanceof BarrelBlockEntity barrel) {
          inventory = barrel;
        }

        if(inventory != null) {
          storage.add(inventory);
        }


      }

      Map<Item, Set<Inventory>> lut = generateLUT(storage);

      for(int i = 0; i < controllerInventory.size(); i++) {
        ItemStack item = controllerInventory.getStack(i);

        if(item.isEmpty()) {
          continue;
        }

        if(!lut.containsKey(item.getItem())) {
          continue;
        }

        for(Inventory inventory : lut.get(item.getItem())) {
          InventoryUtil.insertItemIntoInventory(item, inventory);
          if(inventory.isEmpty()) {
            break;
          }
        }
      }
    }

    if(changes) {
      StorageLoader.saveStorageData();
    }

  }

  private Map<Item, Set<Inventory>> generateLUT(Set<Inventory> storage) {
    Map<Item, Set<Inventory>> lut = new HashMap<>();
    for(Inventory inventory : storage) {
      for(int i = 0; i < inventory.size(); i++) {
        ItemStack item = inventory.getStack(i);
        if(item.isEmpty()) {
          continue;
        }
        if(!lut.containsKey(item.getItem())) {
          lut.put(item.getItem(), new HashSet<>());
        }
        lut.get(item.getItem()).add(inventory);
      }
    }
    return lut;
  }

}

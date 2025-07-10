package de.merkeg.storagesorter.event;

import de.merkeg.storagesorter.config.Config;
import de.merkeg.storagesorter.util.BlockUtil;
import de.merkeg.storagesorter.SharedStorageData;
import de.merkeg.storagesorter.StorageLoader;
import de.merkeg.storagesorter.StorageSystem;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Set;

public class StorageEvent implements UseBlockCallback {

  @Override
  public ActionResult interact(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult blockHitResult) {
    if(world.isClient) {
      return ActionResult.PASS;
    }

    if(hand != Hand.MAIN_HAND) {
      return ActionResult.PASS;
    }

    Item item = playerEntity.getStackInHand(hand).getItem();
    if(item != Config.getToolItem()) {
      return ActionResult.PASS;
    }

    if(!(playerEntity instanceof ServerPlayerEntity)) {
      return ActionResult.PASS;
    }

    ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) playerEntity;

    BlockPos blockPos = blockHitResult.getBlockPos();
    Block clickedBlock = world.getBlockState(blockPos).getBlock();
    Block blockBelow = world.getBlockState(blockPos.down()).getBlock();

    if(!Config.getStorageBlocks().contains(clickedBlock)) {
      return ActionResult.PASS;
    }

    if(!SharedStorageData.selectedSystems.containsKey(playerEntity)) {
      serverPlayerEntity.sendMessage(Text.literal("You did not select any storage controller").formatted(Formatting.RED));
      return ActionResult.FAIL;

    }
    StorageSystem storageSystem = SharedStorageData.selectedSystems.get(playerEntity);

    Set<BlockPos> storage = BlockUtil.findConnectedBlocksOfSameType(world, blockPos);

    if(storageSystem.getStorage().contains(blockPos)) {
      storageSystem.getStorage().removeAll(storage);
      serverPlayerEntity.sendMessage(Text.literal("Removed "+ storage.size() +" storage containers from system").formatted(Formatting.RED));
    } else {
      storageSystem.getStorage().addAll(storage);
      serverPlayerEntity.sendMessage(Text.literal("Added "+ storage.size() +" storage containers to system").formatted(Formatting.GREEN));
    }

    StorageLoader.saveStorageData();
    return ActionResult.FAIL;
  }
}

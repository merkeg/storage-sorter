package de.merkeg.storagesorter.event;

import de.merkeg.storagesorter.config.Config;
import de.merkeg.storagesorter.util.BlockUtil;
import de.merkeg.storagesorter.SharedStorageData;
import de.merkeg.storagesorter.StorageLoader;
import de.merkeg.storagesorter.StorageSystem;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Set;

public class StorageSelectionEvent implements UseBlockCallback {

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

    if(!(playerEntity instanceof ServerPlayerEntity serverPlayerEntity)) {
      return ActionResult.PASS;
    }

    BlockPos blockPos = blockHitResult.getBlockPos();
    BlockState blockState = world.getBlockState(blockPos);
    Block clickedBlock = blockState.getBlock();

    if(!Config.getStorageBlocks().contains(clickedBlock)) {
      return ActionResult.PASS;
    }

    if(!SharedStorageData.selectedSystems.containsKey(playerEntity)) {
      serverPlayerEntity.sendMessage(Text.literal("You did not select any storage controller").formatted(Formatting.RED));
      return ActionResult.FAIL;
    }

    StorageSystem storageSystem = SharedStorageData.selectedSystems.get(playerEntity);
    Set<BlockPos> containers = null;

    if(playerEntity.isSneaking()) {
      containers = BlockUtil.findConnectedBlocksOfSameType(world, blockPos);
    } else if(blockState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE){
      Direction face = ChestBlock.getFacing(blockState);
      BlockEntity neighbour = world.getBlockEntity(blockPos.offset(face));
      if(neighbour instanceof ChestBlockEntity) {
        containers = Set.of(blockPos, neighbour.getPos());
      }
    } else {
      containers = Set.of(blockPos);
    }



    if(storageSystem.getStorage().contains(blockPos)) {
      storageSystem.getStorage().removeAll(containers);
      serverPlayerEntity.sendMessage(Text.literal("Removed "+ containers.size() +" storage containers from system").formatted(Formatting.RED));
    } else {
      storageSystem.getStorage().addAll(containers);
      serverPlayerEntity.sendMessage(Text.literal("Added "+ containers.size() +" storage containers to system").formatted(Formatting.GREEN));
    }

    StorageLoader.saveStorageData();
    return ActionResult.FAIL;
  }
}

package de.merkeg.storagesorter.event;

import de.merkeg.storagesorter.SharedStorageData;
import de.merkeg.storagesorter.config.Config;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ControllerSelectionEvent implements AttackBlockCallback {



  @Override
  public ActionResult interact(PlayerEntity playerEntity, World world, Hand hand, BlockPos blockPos, Direction direction) {
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

    Block clickedBlock = world.getBlockState(blockPos).getBlock();
    Block blockBelow = world.getBlockState(blockPos.down()).getBlock();

    if(clickedBlock != Config.getControllerBlock() || blockBelow != Blocks.REDSTONE_BLOCK) {
      return ActionResult.PASS;
    }

    serverPlayerEntity.sendMessage(Text.literal("Selected Storage at ["+ blockPos.getX() +", "+ blockPos.getY() +", "+ blockPos.getZ() +"]").formatted(Formatting.GREEN));
    SharedStorageData.selectController(playerEntity, blockPos);

    return ActionResult.SUCCESS;
  }
}

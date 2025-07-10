package de.merkeg.storagesorter.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class BlockUtil {

  public static Set<BlockPos> findConnectedBlocksOfSameType(World world, BlockPos startPos) {
    Set<BlockPos> visited = new HashSet<>();
    Stack<BlockPos> stack = new Stack<>();

    BlockState startState = world.getBlockState(startPos);
    Block startBlock = startState.getBlock();

    stack.push(startPos);

    while (!stack.isEmpty()) {
      BlockPos currentPos = stack.pop();

      if (visited.contains(currentPos)) continue;
      visited.add(currentPos);

      // Prüfe alle 6 Nachbarn
      for (BlockPos neighborPos : new BlockPos[]{
              currentPos.up(),
              currentPos.down(),
              currentPos.north(),
              currentPos.south(),
              currentPos.east(),
              currentPos.west()
      }) {
        if (!visited.contains(neighborPos)) {
          BlockState neighborState = world.getBlockState(neighborPos);
          if (neighborState.getBlock() == startBlock) {
            stack.push(neighborPos);
          }
        }
      }
    }

    return visited;
  }
}

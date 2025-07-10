package de.merkeg.storagesorter;

import lombok.*;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StorageSystem {

  private BlockPos controller;
  private String worldName;
  private Set<BlockPos> storage;
}

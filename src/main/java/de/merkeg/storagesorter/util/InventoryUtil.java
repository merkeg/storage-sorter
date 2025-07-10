package de.merkeg.storagesorter.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class InventoryUtil {

  public static boolean insertItemIntoInventory(ItemStack stackToInsert, Inventory inventory) {
    // Erst versuchen, in bestehende Stacks zu stecken
    for (int i = 0; i < inventory.size(); i++) {
      ItemStack slotStack = inventory.getStack(i);

      if (!slotStack.isEmpty()
              && canCombine(slotStack, stackToInsert)
              && slotStack.getCount() < slotStack.getMaxCount()) {

        int transferable = Math.min(stackToInsert.getCount(),
                slotStack.getMaxCount() - slotStack.getCount());

        slotStack.increment(transferable);
        stackToInsert.decrement(transferable);

        if (stackToInsert.isEmpty()) {
          return true; // Alles eingefügt
        }
      }
    }

    // Dann in leere Slots legen
    for (int i = 0; i < inventory.size(); i++) {
      if (inventory.getStack(i).isEmpty()) {
        inventory.setStack(i, stackToInsert.copy());
        stackToInsert.setCount(0);
        return true; // Eingefügt
      }
    }

    return false; // Inventar voll
  }

  public static boolean canCombine(ItemStack a, ItemStack b) {
    return a.getItem() == b.getItem();
  }
}

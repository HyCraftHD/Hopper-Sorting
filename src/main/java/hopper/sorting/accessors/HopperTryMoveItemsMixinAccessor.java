package hopper.sorting.accessors;

import net.minecraft.world.item.Item;

import java.util.Set;

public interface HopperTryMoveItemsMixinAccessor {

    void setSortingHopperItems(Set<Item> items);
    Set<Item> getSortingHopperItems();
}

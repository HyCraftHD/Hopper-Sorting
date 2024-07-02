package hopper.sorting;

import net.minecraft.world.item.Item;

import java.util.Set;

public interface SortingHopper {

    void setSortingHopperItems(Set<Item> items);

    Set<Item> getSortingHopperItems();
}

package hopper.sorting.mixin;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

public interface HopperTryMoveItemsMixinAccessor {

    void setItems(Set<Item> items);
    Set<Item> getItems();
}

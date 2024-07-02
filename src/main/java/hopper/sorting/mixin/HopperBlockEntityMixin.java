package hopper.sorting.mixin;

import hopper.sorting.SortingHopper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends RandomizableContainerBlockEntity implements SortingHopper {

    @Unique
    @Final
    private static final String ITEM_LIST_KEY = "sortingHopperItems";
    @Unique
    @Final
    private static final String SORTING_ITEM_ID = "items";
    @Mutable
    @Unique
    @Final
    private Set<Item> filteredItems;

    //Not used, needed for RandomizableContainerBlockEntity
    @Deprecated
    private HopperBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void constuctor(BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        filteredItems = new HashSet<>();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack itemStack) {
        if (!filteredItems.isEmpty() && !filteredItems.contains(itemStack.getItem())) {
            return false;
        }
        return super.canPlaceItem(slot, itemStack);
    }

    @Inject(at = @At("TAIL"), method = "loadAdditional(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V")
    private void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        ListTag itemList = compoundTag.getList(ITEM_LIST_KEY, ListTag.TAG_COMPOUND);
        for (int i = 0; i < itemList.size(); i++) {
            CompoundTag tag = itemList.getCompound(i);
            String key = tag.getString(SORTING_ITEM_ID);
            if (key.isEmpty()) {
                continue;
            }
            ResourceLocation resourceLocation = ResourceLocation.tryParse(key);
            if (resourceLocation == null) {
                continue;
            }
            var optional = BuiltInRegistries.ITEM.getOptional(resourceLocation);
            optional.ifPresent(item -> filteredItems.add(item));
        }
    }

    @Inject(at = @At("TAIL"), method = "saveAdditional(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V")
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (filteredItems.isEmpty()) {
            return;
        }
        ListTag itemList = new ListTag();
        for (Item item : filteredItems) {
            CompoundTag tag = new CompoundTag();
            tag.putString(SORTING_ITEM_ID, BuiltInRegistries.ITEM.getKey(item).toString());
            itemList.add(tag);
        }
        compoundTag.put(ITEM_LIST_KEY, itemList);
    }

    @Override
    public void setSortingHopperItems(Set<Item> items) {
        filteredItems.clear();
        filteredItems.addAll(items);
    }

    @Override
    public Set<Item> getSortingHopperItems() {
        return filteredItems;
    }
}

package hopper.sorting.mixin;

import hopper.sorting.accessors.HopperTryMoveItemsMixinAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(HopperBlockEntity.class)
public abstract class HopperTryMoveItemsMixin extends RandomizableContainerBlockEntity implements HopperTryMoveItemsMixinAccessor {

    @Unique
    private static final String MOD_ID = "sortingHopper";
    @Mutable
    @Unique
    @Final
    private Set<Item> filteredItems;

    //Not used
    private HopperTryMoveItemsMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void constuctor(BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        filteredItems = new HashSet<>();
    }

//    @Inject(cancellable = true, at = @At("HEAD"), method = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;canPlaceItemInContainer(Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/core/Direction;)Z")
//    private static void canPlaceItemInContainer(Container container, ItemStack itemStack, int i, Direction direction, CallbackInfoReturnable<Boolean> cir) {
//        if(container instanceof HopperBlockEntity hopper) {
//            //System.out.println("container = " + container + ", itemStack = " + itemStack + ", i = " + i + ", direction = " + direction + ", cir = " + cir + ", pos = " + hopper.getBlockPos());
//            var mixin = (HopperTryMoveItemsMixin)(Object)hopper;
//
//            if(!mixin.filteredItems.isEmpty() && !mixin.filteredItems.contains(itemStack.getItem())) {
//                cir.setReturnValue(false);
//            }
//        }
//    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack itemStack) {
        var mixin = (HopperTryMoveItemsMixin)(Object)this;
        if(!mixin.filteredItems.isEmpty() && !mixin.filteredItems.contains(itemStack.getItem())) {
            return false;
        }
        return super.canPlaceItem(slot, itemStack);
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;loadAdditional(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V")
    private void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        ListTag itemList = compoundTag.getList(MOD_ID, ListTag.TAG_COMPOUND);
        System.out.println(itemList + " | " + compoundTag);
        for (int i = 0; i < itemList.size(); i++) {
            CompoundTag tag = itemList.getCompound(i);
            String key = tag.getString("item");
            if(key.isEmpty()) {
                continue;
            }
            ResourceLocation resourceLocation = ResourceLocation.tryParse(key);
            if(resourceLocation == null) {
                continue;
            }
            var optional =  BuiltInRegistries.ITEM.getOptional(resourceLocation);
            optional.ifPresent(item -> filteredItems.add(item));
        }
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;saveAdditional(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V")
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        if(filteredItems.isEmpty()) {
            return;
        }
        ListTag itemList = new ListTag();
        for(Item item : filteredItems) {
            CompoundTag tag = new CompoundTag();
            tag.putString("item", BuiltInRegistries.ITEM.getKey(item).toString());
            itemList.add(tag);
        }
        compoundTag.put(MOD_ID, itemList);
        System.out.println("SAVEEEEEEEEEEEEEEEEEEEEEEEEE" + compoundTag.toString());
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

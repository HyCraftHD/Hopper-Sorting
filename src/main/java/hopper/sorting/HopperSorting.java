package hopper.sorting;

import hopper.sorting.accessors.HopperTryMoveItemsMixinAccessor;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HopperSorting implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("hopper-sorting");

    @Override
    public void onInitialize() {

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if(world.isClientSide) {
                return InteractionResult.PASS;
            }
            if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() != Items.HOPPER) {
                return InteractionResult.PASS;
            }
            if (world.getBlockState(pos).getBlock() != Blocks.HOPPER) {
                return InteractionResult.PASS;
            }
            System.out.println("Left click");
            openCustomInventory(player, pos);

            return InteractionResult.SUCCESS;
        });
		/*
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("opencustomgui").executes(context -> {
				ServerPlayer player = context.getSource().getPlayer();
				openCustomInventory(player);
				return 1;
			}));
		});
		*/

    }

    private void openCustomInventory(Player player, BlockPos pos) {

        player.openMenu(new SimpleMenuProvider((containerId, inventory, player1111) -> {

            ChestMenu menu = new ChestMenu(MenuType.GENERIC_9x4, containerId, inventory, new SimpleContainer(4 * 9), 4) {

                @Override
                public void removed(Player player) {
                    System.out.println("Inv close");

                    BlockEntity blockEntity = player.getCommandSenderWorld().getBlockEntity(pos);

                    if (blockEntity instanceof HopperBlockEntity hopper) {
                        var accessor = (HopperTryMoveItemsMixinAccessor) (Object) hopper;

                        Set<Item> items = new HashSet<>();
                        for (int i = 0; i < 36; i++) {
                            items.add(this.getSlot(i).getItem().getItem());
                        }
                        items.remove(Items.AIR);

                        accessor.setSortingHopperItems(items);

                        hopper.setChanged();
                    }

                    super.removed(player);
                }

                @Override
                public void clicked(int slot, int button, ClickType clickType, Player player) {
                    System.out.println("Slot: " + slot + " Button: " + button + " ClickType: " + clickType);
                    if (clickType != ClickType.PICKUP) {
                        return;
                    }
                    if (this.getSlot(slot).getItem().isEmpty()) {
                        return;
                    }
                    if (slot >= 0 && slot <= 35) { //Oberes Inv
                        this.setItem(slot, containerId, Items.AIR.getDefaultInstance());
                    } else { //Unteres Inv
                        ItemStack item = this.getSlot(slot).getItem();

                        if (containsItem(item)) {
                            return;
                        }
                        int nextSlot = nextFreeSlot();
                        if (nextSlot == -1) {
                            return;
                        }
                        this.setItem(nextSlot, containerId, item.getItem().getDefaultInstance());
                    }


                    //super.clicked(slot, button, clickType, player);
                }

                private int nextFreeSlot() {
                    for (int i = 0; i < 36; i++) {
                        if (this.getSlot(i).getItem().isEmpty()) {
                            return i;
                        }
                    }
                    return -1;
                }

                private boolean containsItem(ItemStack item) {
                    for (int i = 0; i < 36; i++) {
                        if (this.getSlot(i).getItem().getItem() == item.getItem()) {
                            return true;
                        }
                    }
                    return false;
                }
            };

            BlockEntity blockEntity = player.getCommandSenderWorld().getBlockEntity(pos);
            System.out.println("Inv open " + pos);
            if (blockEntity instanceof HopperBlockEntity hopper) {
                var accessor = (HopperTryMoveItemsMixinAccessor) (Object) hopper;
                System.out.println("Items: " + accessor.getSortingHopperItems());
                int i = 0;
                for (var item : accessor.getSortingHopperItems()) {
                    menu.setItem(i, containerId, item.getDefaultInstance());
                    i++;
                }
            }

            return menu;
        }, Component.literal("Title Screen")));
    }
}
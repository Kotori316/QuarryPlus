package com.yogpc.qp.compat;

import com.yogpc.qp.tile.TileRefinery;


public class RefineryRecipeHelper {
    private final TileRefinery tile;

    private RefineryRecipeHelper(final TileRefinery tr) {
        this.tile = tr;
    }
/*
    @Override
    public int getCraftingItemStackSize() {
        return 0;
    }

    @Override
    public ItemStack getCraftingItemStack(final int slotid) {
        return null;
    }

    // BC 6.2.X or later
    public ItemStack decrCraftingItemStack(final int slotid, final int val) {
        return null;
    }

    // BC 6.1.X
    public ItemStack decrCraftingItemgStack(final int slotid, final int val) {
        return null;
    }

    @Override
    public FluidStack getCraftingFluidStack(final int id) {
        return this.tile.src[id];
    }

    @Override
    public FluidStack decrCraftingFluidStack(final int id, final int val) {
        final FluidStack ret = this.tile.src[id];
        if (ret == null)
            return null;
        if (val >= ret.amount) {
            this.tile.src[id] = null;
            return ret;
        }
        this.tile.src[id] = ret.copy();
        this.tile.src[id].amount -= val;
        ret.amount = val;
        return ret;
    }

    @Override
    public int getCraftingFluidStackSize() {
        return this.tile.src.length;
    }

    public static void get(final TileRefinery tr) {
        if (!ModAPIManager.INSTANCE.hasAPI(QuarryPlus.Optionals.Buildcraft_recipes) || tr.cached != null)
            return;
        final IRefineryRecipeManager irrm = BuildcraftRecipeRegistry.refineryRecipes;
        if (irrm == null)
            return;
        for (final IFlexibleRecipe<FluidStack> ifr : irrm.getRecipes()) {
            final CraftingResult<FluidStack> cr = ifr.craft(new RefineryRecipeHelper(tr), true);
            if (cr == null || !check(cr.crafted, tr))
                continue;
            ifr.craft(new RefineryRecipeHelper(tr), false);
            tr.rem_energy = (double) cr.energyCost / 10;
            tr.rem_time = cr.craftingTime;
            tr.cached = cr.crafted.copy();
            get(tr);
            return;
        }
        tr.rem_energy = 0;
        tr.rem_time = 0;
        tr.cached = null;
    }

    private static boolean check(final FluidStack fs, final TileRefinery tr) {
        final Byte i = tr.get().get(Integer.valueOf(Enchantment.efficiency.effectId));
        return tr.res == null || tr.res.isFluidEqual(fs)
                && tr.buf - tr.res.amount >= fs.amount * (i == null ? 0 : i.byteValue() + 1);
    }*/
}

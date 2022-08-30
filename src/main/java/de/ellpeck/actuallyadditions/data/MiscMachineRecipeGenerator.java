package de.ellpeck.actuallyadditions.data;

import com.google.gson.JsonObject;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.crafting.FermentingRecipe;
import de.ellpeck.actuallyadditions.mod.crafting.MiningLensRecipe;
import de.ellpeck.actuallyadditions.mod.crafting.PressingRecipe;
import de.ellpeck.actuallyadditions.mod.fluids.InitFluids;
import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Path;
import java.util.function.Consumer;

public class MiscMachineRecipeGenerator extends RecipeProvider {
    public MiscMachineRecipeGenerator(DataGenerator pGenerator) {
        super(pGenerator);
    }

    @Override
    protected void saveAdvancement(DirectoryCache pCache, JsonObject pAdvancementJson, Path pPath) {
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        consumer.accept(new PressingRecipe.FinishedRecipe(folderRecipe("pressing", "canola"),
            Ingredient.of(ActuallyItems.CANOLA.get()), new FluidStack(InitFluids.CANOLA_OIL.get(), 80)));

        consumer.accept(new FermentingRecipe.FinishedRecipe(folderRecipe("fermenting", "refined_canola"),
            new FluidStack(InitFluids.CANOLA_OIL.get(), 80), new FluidStack(InitFluids.REFINED_CANOLA_OIL.get(), 80), 100));
    }

    private String getItemName(IItemProvider item) {
        return ForgeRegistries.ITEMS.getKey(item.asItem()).getPath();
    }

    private ResourceLocation folderRecipe(String folder, String recipe) {
        return new ResourceLocation(ActuallyAdditions.MODID, folder + "/" + recipe);
    }
}

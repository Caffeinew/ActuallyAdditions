/*
 * This file ("TheWildPlants.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.blocks.metalists;

import com.google.common.base.Preconditions;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import net.minecraft.block.Block;
import net.minecraft.item.Rarity;
import net.minecraft.util.IStringSerializable;

@Deprecated
public enum TheWildPlants implements IStringSerializable {

    CANOLA("canola", Rarity.RARE, ActuallyBlocks.CANOLA),
    FLAX("flax", Rarity.RARE, ActuallyBlocks.FLAX),
    RICE("rice", Rarity.RARE, ActuallyBlocks.RICE),
    COFFEE("coffee", Rarity.RARE, ActuallyBlocks.COFFEE);

    final String name;
    final Rarity rarity;
    final Block normal;

    TheWildPlants(String name, Rarity rarity, Block normal) {
        this.name = name;
        this.rarity = rarity;
        this.normal = Preconditions.checkNotNull(normal, "TheWildPlants was loaded before block init!");
    }

    @Override
    public String getString() {
        return this.name;
    }

    public Rarity getRarity() {
        return this.rarity;
    }

    public Block getNormalVersion() {
        return this.normal;
    }
}

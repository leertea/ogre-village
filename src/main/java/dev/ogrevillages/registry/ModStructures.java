package dev.ogrevillages.registry;

import dev.ogrevillages.OgreVillagesMod;
import dev.ogrevillages.worldgen.OgreVillagePiece;
import dev.ogrevillages.worldgen.OgreVillageStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, OgreVillagesMod.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, OgreVillagesMod.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<OgreVillageStructure>> OGRE_VILLAGE =
            STRUCTURE_TYPES.register("ogre_village",
                    () -> (StructureType<OgreVillageStructure>) () -> OgreVillageStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> OGRE_VILLAGE_PIECE =
            PIECES.register("ogre_village_piece",
                    () -> (StructurePieceType.ContextlessType) OgreVillagePiece::new);
}

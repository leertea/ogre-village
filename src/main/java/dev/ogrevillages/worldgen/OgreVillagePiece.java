package dev.ogrevillages.worldgen;

import dev.ogrevillages.entity.OgreEntity;
import dev.ogrevillages.registry.ModEntities;
import dev.ogrevillages.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Ein Teil des Ogre-Dorfs: entweder ein Haus (9x9, Dach bis y=11) oder der zentrale Platz (9x9).
 * Die Bauwerke werden im Code gebaut (keine NBT-Dateien nötig) und spawnen beim Bau ihre Ogres.
 *
 * Koordinaten im Haus (lokal, Blickrichtung "Norden"; die Rotation übernimmt StructurePiece):
 * x = 0..8 von Westen nach Osten, z = 0..8 von der Veranda (Eingang) nach hinten, y = 3 ist die Bodenebene.
 */
public class OgreVillagePiece extends StructurePiece {
    public static final int HUT = 0;
    public static final int PLAZA = 1;

    private final int kind;
    private boolean spawned;

    public OgreVillagePiece(int kind, BoundingBox box, Direction orientation) {
        super(ModStructures.OGRE_VILLAGE_PIECE.get(), 0, box);
        this.kind = kind;
        setOrientation(orientation);
    }

    public OgreVillagePiece(CompoundTag tag) {
        super(ModStructures.OGRE_VILLAGE_PIECE.get(), tag);
        this.kind = tag.getInt("Kind");
        this.spawned = tag.getBoolean("Spawned");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("Kind", kind);
        tag.putBoolean("Spawned", spawned);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos pivot) {
        if (kind == PLAZA) {
            buildPlaza(level, random, chunkBB);
        } else {
            buildHut(level, random, chunkBB);
        }
    }

    // ---------------------------------------------------------------- Hilfsmethoden

    private void fill(WorldGenLevel level, BoundingBox bb, int x1, int y1, int z1, int x2, int y2, int z2,
                      BlockState state) {
        generateBox(level, bb, x1, y1, z1, x2, y2, z2, state, state, false);
    }

    private static BlockState log(Direction.Axis axis) {
        return Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static BlockState stairs(Direction facing) {
        return Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing);
    }

    /** Glasscheibe in einer Wand, die entlang der lokalen z-Achse läuft (Seitenwände). */
    private static BlockState paneAlongZ() {
        return Blocks.GLASS_PANE.defaultBlockState()
                .setValue(BlockStateProperties.NORTH, true)
                .setValue(BlockStateProperties.SOUTH, true);
    }

    /** Glasscheibe in einer Wand, die entlang der lokalen x-Achse läuft (Vorder-/Rückwand). */
    private static BlockState paneAlongX() {
        return Blocks.GLASS_PANE.defaultBlockState()
                .setValue(BlockStateProperties.EAST, true)
                .setValue(BlockStateProperties.WEST, true);
    }

    // ---------------------------------------------------------------- Haus

    private void buildHut(WorldGenLevel level, RandomSource random, BoundingBox bb) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState stoneBricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState plaster = Blocks.WHITE_TERRACOTTA.defaultBlockState();
        BlockState darkPlanks = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        BlockState floor = Blocks.SPRUCE_PLANKS.defaultBlockState();

        // Gelände freiräumen (Bäume, Hügel) und Fundament bis zur Bodenebene auffüllen
        fill(level, bb, 0, 4, 0, 8, 13, 8, air);
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 8; z++) {
                for (int y = 0; y <= 2; y++) {
                    int r = random.nextInt(4);
                    BlockState s = r == 0 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                            : r == 1 ? Blocks.COBBLESTONE.defaultBlockState() : stoneBricks;
                    placeBlock(level, s, x, y, z, bb);
                }
            }
        }

        // Boden: Steinsockel unter den Wänden, Holzboden innen, Steinplatten auf der Veranda
        fill(level, bb, 1, 3, 1, 7, 3, 7, stoneBricks);
        fill(level, bb, 2, 3, 2, 6, 3, 6, floor);
        fill(level, bb, 2, 3, 0, 6, 3, 0, stoneBricks);

        // Wände: Steinsockel (y4), weißer Putz (y5-6)
        for (int x = 1; x <= 7; x++) {
            for (int z = 1; z <= 7; z++) {
                if (x != 1 && x != 7 && z != 1 && z != 7) {
                    continue;
                }
                placeBlock(level, stoneBricks, x, 4, z, bb);
                placeBlock(level, plaster, x, 5, z, bb);
                placeBlock(level, plaster, x, 6, z, bb);
            }
        }

        // Dunkle Holzpfosten
        int[][] posts = {{1, 1}, {7, 1}, {1, 7}, {7, 7}, {3, 1}, {5, 1}, {4, 7}, {1, 4}, {7, 4}};
        for (int[] p : posts) {
            fill(level, bb, p[0], 4, p[1], p[0], 6, p[1], log(Direction.Axis.Y));
        }
        // Verandapfosten
        fill(level, bb, 1, 4, 0, 1, 7, 0, log(Direction.Axis.Y));
        fill(level, bb, 7, 4, 0, 7, 7, 0, log(Direction.Axis.Y));

        // Querbalken auf Wandhöhe (y7)
        fill(level, bb, 1, 7, 0, 7, 7, 0, log(Direction.Axis.X));
        fill(level, bb, 1, 7, 1, 7, 7, 1, log(Direction.Axis.X));
        fill(level, bb, 1, 7, 7, 7, 7, 7, log(Direction.Axis.X));
        fill(level, bb, 1, 7, 4, 7, 7, 4, log(Direction.Axis.X));
        fill(level, bb, 1, 7, 2, 1, 7, 6, log(Direction.Axis.Z));
        fill(level, bb, 7, 7, 2, 7, 7, 6, log(Direction.Axis.Z));
        // Auflage unter dem Dach
        fill(level, bb, 1, 8, 1, 1, 8, 7, darkPlanks);
        fill(level, bb, 7, 8, 1, 7, 8, 7, darkPlanks);

        // Fenster (y5)
        placeBlock(level, paneAlongX(), 2, 5, 1, bb);
        placeBlock(level, paneAlongX(), 6, 5, 1, bb);
        for (int x : new int[]{2, 3, 5, 6}) {
            placeBlock(level, paneAlongX(), x, 5, 7, bb);
        }
        for (int z : new int[]{2, 3, 5, 6}) {
            placeBlock(level, paneAlongZ(), 1, 5, z, bb);
            placeBlock(level, paneAlongZ(), 7, 5, z, bb);
        }

        // Tür (Vorderwand, Mitte): lokal "Norden" = Richtung ins Haus hinein
        BlockState door = Blocks.DARK_OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.OPEN, false);
        placeBlock(level, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 4, 4, 1, bb);
        placeBlock(level, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 4, 5, 1, bb);
        placeBlock(level, log(Direction.Axis.X), 4, 6, 1, bb);

        // Satteldach (Firstrichtung = z), Traufen stehen 1 Block über
        for (int k = 0; k <= 3; k++) {
            fill(level, bb, k, 8 + k, 0, k, 8 + k, 8, stairs(Direction.EAST));
            fill(level, bb, 8 - k, 8 + k, 0, 8 - k, 8 + k, 8, stairs(Direction.WEST));
        }
        fill(level, bb, 4, 11, 0, 4, 11, 8, darkPlanks);

        // Giebelflächen vorn (z0) und hinten (z8)
        for (int z : new int[]{0, 8}) {
            fill(level, bb, 1, 8, z, 7, 8, z, plaster);
            fill(level, bb, 2, 9, z, 6, 9, z, plaster);
            fill(level, bb, 3, 10, z, 5, 10, z, plaster);
        }

        // Einrichtung: Läufer, Bett, Ofen, Fass, Werkbank, kleiner Tisch mit Laterne
        BlockState carpet = Blocks.RED_CARPET.defaultBlockState();
        fill(level, bb, 4, 4, 2, 4, 4, 6, carpet);
        placeBlock(level, Blocks.RED_BED.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
                .setValue(BedBlock.PART, BedPart.FOOT), 2, 4, 5, bb);
        placeBlock(level, Blocks.RED_BED.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
                .setValue(BedBlock.PART, BedPart.HEAD), 2, 4, 6, bb);
        placeBlock(level, Blocks.FURNACE.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH), 6, 4, 6, bb);
        placeBlock(level, Blocks.BARREL.defaultBlockState(), 5, 4, 6, bb);
        placeBlock(level, Blocks.CRAFTING_TABLE.defaultBlockState(), 6, 4, 5, bb);
        placeBlock(level, Blocks.DARK_OAK_FENCE.defaultBlockState(), 2, 4, 2, bb);
        placeBlock(level, Blocks.LANTERN.defaultBlockState(), 2, 5, 2, bb);
        placeBlock(level, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 4, 6, 4, bb);

        // Veranda: Laternen unter dem Dach, Heuballen und Fass als Deko
        placeBlock(level, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2, 6, 0, bb);
        placeBlock(level, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 6, 6, 0, bb);
        placeBlock(level, Blocks.HAY_BLOCK.defaultBlockState(), 8, 4, 2, bb);
        placeBlock(level, Blocks.HAY_BLOCK.defaultBlockState(), 8, 4, 3, bb);
        placeBlock(level, Blocks.HAY_BLOCK.defaultBlockState(), 8, 5, 2, bb);
        placeBlock(level, Blocks.BARREL.defaultBlockState(), 0, 4, 6, bb);

        // Bewohner
        if (!spawned) {
            BlockPos p = getWorldPos(3, 4, 4).immutable();
            if (bb.isInside(p)) {
                spawned = true;
                int count = 1 + random.nextInt(2);
                for (int i = 0; i < count; i++) {
                    spawnOgre(level, p, random);
                }
            }
        }
    }

    // ---------------------------------------------------------------- Dorfplatz

    private void buildPlaza(WorldGenLevel level, RandomSource random, BoundingBox chunkBB) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState path = Blocks.DIRT_PATH.defaultBlockState();
        BlockState gravel = Blocks.COBBLESTONE.defaultBlockState();
        BlockState fence = Blocks.DARK_OAK_FENCE.defaultBlockState();

        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 8; z++) {
                int dx = x - 4;
                int dz = z - 4;
                if (dx * dx + dz * dz > 20) {
                    continue;
                }
                for (int y = 1; y <= 4; y++) {
                    placeBlock(level, air, x, y, z, chunkBB);
                }
                boolean ring = dx * dx + dz * dz <= 2;
                placeBlock(level, ring ? gravel : (random.nextInt(4) == 0 ? gravel : path), x, 0, z, chunkBB);
            }
        }

        placeBlock(level, Blocks.CAMPFIRE.defaultBlockState(), 4, 1, 4, chunkBB);

        int[][] posts = {{1, 1}, {7, 1}, {1, 7}, {7, 7}};
        for (int[] p : posts) {
            placeBlock(level, fence, p[0], 1, p[1], chunkBB);
            placeBlock(level, fence, p[0], 2, p[1], chunkBB);
            placeBlock(level, Blocks.LANTERN.defaultBlockState(), p[0], 3, p[1], chunkBB);
        }

        if (!spawned) {
            BlockPos p = getWorldPos(4, 1, 6).immutable();
            if (chunkBB.isInside(p)) {
                spawned = true;
                spawnOgre(level, p, random);
            }
        }
    }

    // ---------------------------------------------------------------- Spawn

    private void spawnOgre(WorldGenLevel level, BlockPos pos, RandomSource random) {
        OgreEntity ogre = ModEntities.OGRE.get().create(level.getLevel());
        if (ogre == null) {
            return;
        }
        ogre.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        ogre.finalizeSpawn((ServerLevelAccessor) level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
        ogre.setPersistenceRequired();
        ogre.restrictTo(pos, 24);
        level.addFreshEntityWithPassengers(ogre);
    }
}

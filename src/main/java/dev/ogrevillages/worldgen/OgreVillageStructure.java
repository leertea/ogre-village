package dev.ogrevillages.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ogrevillages.registry.ModStructures;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

/**
 * Ogre-Dorf: zentraler Platz mit Lagerfeuer + 4-6 Hütten im Kreis, Türen zeigen zur Mitte.
 */
public class OgreVillageStructure extends Structure {
    public static final MapCodec<OgreVillageStructure> CODEC =
            RecordCodecBuilder.mapCodec(i -> i.group(settingsCodec(i)).apply(i, OgreVillageStructure::new));

    public OgreVillageStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        return onTopOfChunkCenter(ctx, Heightmap.Types.WORLD_SURFACE_WG, builder -> generatePieces(builder, ctx));
    }

    private void generatePieces(StructurePiecesBuilder builder, GenerationContext ctx) {
        int cx = ctx.chunkPos().getMiddleBlockX();
        int cz = ctx.chunkPos().getMiddleBlockZ();
        RandomSource random = ctx.random();

        int centerY = surfaceY(ctx, cx, cz);
        if (centerY < 0) {
            return;
        }

        // Dorfplatz (9x9), Boden-Ebene = Oberfläche - 1
        builder.addPiece(new OgreVillagePiece(OgreVillagePiece.PLAZA,
                new BoundingBox(cx - 4, centerY - 1, cz - 4, cx + 4, centerY + 3, cz + 4),
                Direction.NORTH));

        int huts = 4 + random.nextInt(3);
        float offset = random.nextFloat() * Mth.TWO_PI;
        for (int i = 0; i < huts; i++) {
            float angle = offset + i * Mth.TWO_PI / huts + (random.nextFloat() - 0.5F) * 0.3F;
            int radius = 16 + random.nextInt(5);
            int hx = cx + Mth.floor(Mth.cos(angle) * radius);
            int hz = cz + Mth.floor(Mth.sin(angle) * radius);

            int y = surfaceY(ctx, hx, hz);
            if (y < 0) {
                continue;
            }
            // Tür-Seite zeigt zur Dorfmitte (Orientierung = Richtung von der Mitte zur Hütte)
            Direction dir = Direction.getNearest(hx - cx, 0, hz - cz);
            // Haus: 9x9, lokal y0..13 (y3 = Bodenebene = Oberfläche - 1)
            BoundingBox box = new BoundingBox(hx - 4, y - 4, hz - 4, hx + 4, y + 9, hz + 4);
            builder.addPiece(new OgreVillagePiece(OgreVillagePiece.HUT, box, dir));
        }
    }

    /** Erste freie Y über dem Boden, oder -1 bei Wasser/Meer. */
    private static int surfaceY(GenerationContext ctx, int x, int z) {
        int top = ctx.chunkGenerator().getFirstOccupiedHeight(
                x, z, Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState());
        int floor = ctx.chunkGenerator().getFirstOccupiedHeight(
                x, z, Heightmap.Types.OCEAN_FLOOR_WG, ctx.heightAccessor(), ctx.randomState());
        if (top != floor || top <= ctx.chunkGenerator().getSeaLevel()) {
            return -1;
        }
        return top;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.OGRE_VILLAGE.get();
    }
}

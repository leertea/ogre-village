package dev.ogrevillages;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.ogrevillages.entity.OgreEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Arrays;
import java.util.Comparator;

/**
 * /create structure ogre            -> platziert ein Ogre-Dorf (inkl. Ogres) an deiner Position
 * /ogrevillage                      -> Kurzform, macht dasselbe
 * /ogrevillage evolve <stufe>       -> setzt den nächsten Ogre (8 Blöcke) auf die gewählte Stufe (zum Testen)
 *
 * Das Dorf wird über den Vanilla-Befehl "/place structure" erzeugt, der auch die Ogres spawnt.
 * Falls ein anderes Mod (z. B. Create) ebenfalls /create nutzt, werden die Zweige zusammengeführt;
 * /ogrevillage funktioniert in jedem Fall.
 */
public class ModCommands {
    private static final String STRUCTURE_ID = OgreVillagesMod.MODID + ":ogre_village";

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("create")
                        .then(Commands.literal("structure")
                                .then(Commands.literal("ogre")
                                        .requires(src -> src.hasPermission(2))
                                        .executes(ModCommands::spawnVillage))));

        event.getDispatcher().register(
                Commands.literal("ogrevillage")
                        .requires(src -> src.hasPermission(2))
                        .executes(ModCommands::spawnVillage)
                        .then(Commands.literal("evolve")
                                .then(Commands.argument("stage", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                Arrays.stream(OgreEntity.Stage.values()).map(s -> s.id), builder))
                                        .executes(ModCommands::evolve))));
    }

    private static int spawnVillage(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        BlockPos p = BlockPos.containing(src.getPosition());
        String cmd = "place structure " + STRUCTURE_ID + " " + p.getX() + " " + p.getY() + " " + p.getZ();
        src.getServer().getCommands().performPrefixedCommand(src, cmd);
        src.sendSuccess(() -> Component.literal("Ogre-Dorf wird erzeugt..."), true);
        return 1;
    }

    private static int evolve(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String id = StringArgumentType.getString(ctx, "stage");
        OgreEntity.Stage stage = OgreEntity.Stage.byId(id);
        if (stage == null) {
            src.sendFailure(Component.literal("Unbekannte Stufe: " + id));
            return 0;
        }
        Vec3 pos = src.getPosition();
        var nearest = src.getLevel()
                .getEntitiesOfClass(OgreEntity.class, new AABB(pos, pos).inflate(8.0))
                .stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(pos)));
        if (nearest.isEmpty()) {
            src.sendFailure(Component.literal("Kein Ogre in 8 Blöcken Reichweite."));
            return 0;
        }
        nearest.get().evolveTo(stage);
        return 1;
    }
}

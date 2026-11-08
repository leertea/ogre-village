package dev.ogrevillages;

import dev.ogrevillages.entity.OgreEntity;
import dev.ogrevillages.registry.ModEntities;
import dev.ogrevillages.registry.ModStructures;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@Mod(OgreVillagesMod.MODID)
public class OgreVillagesMod {
    public static final String MODID = "ogrevillages";

    public OgreVillagesMod(IEventBus modBus) {
        ModEntities.ENTITY_TYPES.register(modBus);
        ModEntities.ITEMS.register(modBus);
        ModStructures.STRUCTURE_TYPES.register(modBus);
        ModStructures.PIECES.register(modBus);

        modBus.addListener(this::onAttributes);
        modBus.addListener(this::onSpawnPlacements);
        modBus.addListener(this::onCreativeTab);

        NeoForge.EVENT_BUS.addListener(ModCommands::register);
    }

    private void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.OGRE.get(), OgreEntity.createAttributes().build());
    }

    private void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                ModEntities.OGRE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                OgreEntity::checkOgreSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
    }

    private void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModEntities.OGRE_SPAWN_EGG.get());
        }
    }
}

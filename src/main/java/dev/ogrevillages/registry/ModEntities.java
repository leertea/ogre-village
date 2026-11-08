package dev.ogrevillages.registry;

import dev.ogrevillages.OgreVillagesMod;
import dev.ogrevillages.entity.OgreEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, OgreVillagesMod.MODID);
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(OgreVillagesMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<OgreEntity>> OGRE =
            ENTITY_TYPES.register("ogre", () -> EntityType.Builder
                    .of(OgreEntity::new, MobCategory.CREATURE)
                    .sized(0.7f, 2.0f)
                    .eyeHeight(1.8f)
                    .clientTrackingRange(10)
                    .build("ogre"));

    public static final DeferredItem<Item> OGRE_SPAWN_EGG =
            ITEMS.register("ogre_spawn_egg",
                    () -> new DeferredSpawnEggItem(OGRE, 0x8E2F23, 0x2A1A14, new Item.Properties()));
}

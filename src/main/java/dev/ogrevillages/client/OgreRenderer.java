package dev.ogrevillages.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ogrevillages.OgreVillagesMod;
import dev.ogrevillages.entity.OgreEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Rendert den Ogre mit dem Spieler-Modell (64x64-Skins inkl. zweiter Ebene) plus Horn-Layer.
 * Der Skin hängt nur von der Variante ab (textures/entity/ogre/skin_<n>.png), nicht von der Entwicklungsstufe.
 */
public class OgreRenderer extends HumanoidMobRenderer<OgreEntity, PlayerModel<OgreEntity>> {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[OgreEntity.VARIANT_COUNT];

    static {
        for (int v = 0; v < TEXTURES.length; v++) {
            TEXTURES[v] = ResourceLocation.fromNamespaceAndPath(
                    OgreVillagesMod.MODID, "textures/entity/ogre/skin_" + v + ".png");
        }
    }

    public OgreRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.6F);
        this.addLayer(new OgreHornLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(OgreEntity entity) {
        return TEXTURES[Math.floorMod(entity.getVariant(), TEXTURES.length)];
    }

    /** Spielermodell ist 1,8 m hoch; Ogre ist 2 m groß. */
    @Override
    protected void scale(OgreEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.11F, 1.11F, 1.11F);
    }
}

package dev.ogrevillages.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ogrevillages.OgreVillagesMod;
import dev.ogrevillages.entity.OgreEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Eigenes 3D-Modell: zwei gebogene Hörner, die am Kopf des Spieler-Modells sitzen.
 * Eigene Textur (16x16), damit die 64x64-Skins unverändert bleiben können.
 */
public class OgreHornLayer extends RenderLayer<OgreEntity, PlayerModel<OgreEntity>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(OgreVillagesMod.MODID, "ogre_horns"), "main");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OgreVillagesMod.MODID, "textures/entity/ogre/ogre_horns.png");

    private final ModelPart horns;

    public OgreHornLayer(RenderLayerParent<OgreEntity, PlayerModel<OgreEntity>> parent, EntityModelSet models) {
        super(parent);
        this.horns = models.bakeLayer(LAYER).getChild("horns");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition horns = root.addOrReplaceChild("horns", CubeListBuilder.create(), PartPose.ZERO);

        // Linkes Horn: Basis + gebogene Spitze (Kopf-Ursprung: Nacken, Oberseite bei y = -8)
        PartDefinition left = horns.addOrReplaceChild("horn_l",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(2.5F, -7.5F, -2.0F, -0.15F, 0.0F, 0.45F));
        left.addOrReplaceChild("tip_l",
                CubeListBuilder.create().texOffs(0, 6).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -0.35F, 0.0F, 0.45F));

        // Rechtes Horn (gespiegelt)
        PartDefinition right = horns.addOrReplaceChild("horn_r",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(-2.5F, -7.5F, -2.0F, -0.15F, 0.0F, -0.45F));
        right.addOrReplaceChild("tip_r",
                CubeListBuilder.create().texOffs(0, 6).mirror().addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -0.35F, 0.0F, -0.45F));

        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, OgreEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) {
            return;
        }
        poseStack.pushPose();
        // Hörner folgen der Kopfbewegung
        this.getParentModel().head.translateAndRotate(poseStack);
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.horns.render(poseStack, vc, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F));
        poseStack.popPose();
    }
}

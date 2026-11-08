package dev.ogrevillages.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * Ogre mit der kompletten Tensura-Ogre-Linie als Entwicklungsstufen.
 *
 * Die Werte stammen aus den Standard-Configs von Tensura: Reincarnated 2.0.1.5 (OgreConfig).
 * Tensura addiert sie auf die Spieler-Basiswerte (HP 20, Angriff 1, Angriffstempo 4, Bewegung 0.09,
 * Rückstoß-Resistenz 0.1, Spirit-HP 60); die Summen stehen hier schon fertig im Enum.
 *
 * Entwicklung durch Benennen: Tensuras Naming-Menü (Taste N) setzt den Namen am Mob. Dadurch wird
 * setCustomName() aufgerufen, und ein Ogre wird zum Enlightened Ogre. Alle weiteren Stufen sind
 * (noch) nur per Befehl erreichbar: /ogrevillage evolve <stufe>.
 */
public class OgreEntity extends PathfinderMob {
    /** Anzahl Skins (textures/entity/ogre/skin_<n>.png). Der Skin wird beim Spawn gewürfelt und bleibt bei jeder Entwicklung gleich. */
    public static final int VARIANT_COUNT = 4;

    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(OgreEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STAGE =
            SynchedEntityData.defineId(OgreEntity.class, EntityDataSerializers.INT);

    /**
     * Stufen der Ogre-Linie.
     * Reihenfolge der Werte: id, HP, Angriff, Angriffstempo, Bewegung, Rückstoß-Res.,
     * Aura min/max, Magicule min/max, Spirit-HP.
     */
    public enum Stage {
        OGRE("ogre",
                26, 2.0, 4.1, 0.10, 0.2, 1500, 2500, 300, 600, 75),
        KIJIN("kijin",
                30, 4.0, 4.2, 0.11, 0.3, 5000, 5000, 5000, 5000, 160),
        ENLIGHTENED_OGRE("enlightened_ogre",
                120, 5.0, 4.3, 0.11, 0.3, 100000, 100000, 100000, 100000, 400),
        MYSTIC_ONI("mystic_oni",
                160, 5.0, 4.4, 0.12, 0.3, 40000, 100000, 40000, 100000, 460),
        WICKED_ONI("wicked_oni",
                130, 4.5, 4.4, 0.12, 0.3, 40000, 100000, 40000, 100000, 520),
        SPIRIT_ONI("spirit_oni",
                520, 6.0, 4.5, 0.14, 0.4, 400000, 400000, 400000, 400000, 3500),
        DEATH_ONI("death_oni",
                420, 5.5, 4.5, 0.14, 0.4, 400000, 400000, 400000, 400000, 3100),
        DIVINE_ONI("divine_oni",
                1200, 8.0, 4.7, 0.19, 0.6, 1000000, 1000000, 1000000, 1000000, 6200),
        DIVINE_FIGHTER("divine_fighter",
                960, 7.5, 4.7, 0.19, 0.6, 1000000, 1000000, 1000000, 1000000, 5900);

        public final String id;
        public final double health;
        public final double attackDamage;
        public final double attackSpeed;
        public final double movementSpeed;
        public final double knockbackResistance;
        public final double minAura;
        public final double maxAura;
        public final double minMagicule;
        public final double maxMagicule;
        public final double spiritualHealth;

        Stage(String id, double health, double attackDamage, double attackSpeed,
              double movementSpeed, double knockbackResistance, double minAura, double maxAura,
              double minMagicule, double maxMagicule, double spiritualHealth) {
            this.id = id;
            this.health = health;
            this.attackDamage = attackDamage;
            this.attackSpeed = attackSpeed;
            this.movementSpeed = movementSpeed;
            this.knockbackResistance = knockbackResistance;
            this.minAura = minAura;
            this.maxAura = maxAura;
            this.minMagicule = minMagicule;
            this.maxMagicule = maxMagicule;
            this.spiritualHealth = spiritualHealth;
        }

        public String translationKey() {
            return "entity.ogrevillages." + id;
        }

        @Nullable
        public static Stage byId(String id) {
            for (Stage s : values()) {
                if (s.id.equals(id)) {
                    return s;
                }
            }
            return null;
        }
    }

    /** Wurden die Tensura-Skills der aktuellen Stufe schon vergeben? (wird beim Entwickeln zurückgesetzt) */
    private boolean skillsGranted;

    public OgreEntity(EntityType<? extends OgreEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        if (getNavigation() instanceof GroundPathNavigation nav) {
            nav.setCanOpenDoors(true);
        }
    }

    /** Skills werden im ersten Server-Tick vergeben, nicht schon beim Spawn (z. B. während der Weltgenerierung). */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!skillsGranted && this.tickCount > 5) {
            skillsGranted = true;
            OgreSkills.grant(this);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        Stage base = Stage.OGRE;
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, base.health)
                .add(Attributes.MOVEMENT_SPEED, base.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, base.attackDamage)
                .add(Attributes.ATTACK_SPEED, base.attackSpeed)
                .add(Attributes.KNOCKBACK_RESISTANCE, base.knockbackResistance)
                .add(Attributes.JUMP_STRENGTH, 0.5)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkOgreSpawnRules(EntityType<OgreEntity> type, ServerLevelAccessor level,
                                              MobSpawnType reason, BlockPos pos, RandomSource random) {
        return Mob.checkMobSpawnRules(type, level, reason, pos, random)
                && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Sprint-Tempo ca. 1.3 x Grundgeschwindigkeit
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    // ---------------------------------------------------------------- Daten

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_STAGE, 0);
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, Math.floorMod(variant, VARIANT_COUNT));
    }

    public Stage getStage() {
        Stage[] all = Stage.values();
        return all[Math.floorMod(this.entityData.get(DATA_STAGE), all.length)];
    }

    private void setStage(Stage stage) {
        this.entityData.set(DATA_STAGE, stage.ordinal());
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(getStage().translationKey());
    }

    // ---------------------------------------------------------------- Entwicklung

    /**
     * Tensuras Naming (Taste N) ruft nach erfolgreichem Benennen setCustomName() am Mob auf.
     * Das gilt auch für Namensschilder. Ein Ogre wird dadurch zum Enlightened Ogre.
     */
    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        if (name != null
                && !this.level().isClientSide
                && this.tickCount > 0
                && this.isAlive()
                && getStage() == Stage.OGRE) {
            evolveTo(Stage.ENLIGHTENED_OGRE);
        }
    }

    public void evolveTo(Stage stage) {
        setStage(stage);
        applyStageStats(true);
        applyTensuraStats();
        this.skillsGranted = false;
        this.setPersistenceRequired();

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    getX(), getY() + 1.0, getZ(), 40, 0.4, 0.8, 0.4, 0.3);
            serverLevel.playSound(null, blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 0.8F);
            Component msg = Component.translatable("message.ogrevillages.evolved",
                    this.getDisplayName(), Component.translatable(stage.translationKey()));
            for (var player : serverLevel.getPlayers(p -> p.distanceToSqr(this) < 256.0)) {
                player.sendSystemMessage(msg);
            }
        }
    }

    private void applyStageStats(boolean heal) {
        Stage s = getStage();
        setBase(Attributes.MAX_HEALTH, s.health);
        setBase(Attributes.ATTACK_DAMAGE, s.attackDamage);
        setBase(Attributes.ATTACK_SPEED, s.attackSpeed);
        setBase(Attributes.MOVEMENT_SPEED, s.movementSpeed);
        setBase(Attributes.KNOCKBACK_RESISTANCE, s.knockbackResistance);
        if (heal) {
            this.setHealth(this.getMaxHealth());
        }
    }

    /**
     * Setzt Tensuras Aura-, Magicule- und Spirit-HP-Werte, falls Tensura installiert ist und die Attribute
     * an diesem Mob vorhanden sind. Ohne Tensura passiert nichts (keine Compile-Abhängigkeit).
     */
    private void applyTensuraStats() {
        Stage s = getStage();
        setTensuraAttribute("max_aura", between(s.minAura, s.maxAura));
        setTensuraAttribute("max_magicule", between(s.minMagicule, s.maxMagicule));
        setTensuraAttribute("max_spiritual_health", s.spiritualHealth);
    }

    private double between(double min, double max) {
        return max <= min ? min : min + this.random.nextDouble() * (max - min);
    }

    private void setTensuraAttribute(String path, double value) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("tensura", path);
        BuiltInRegistries.ATTRIBUTE.getHolder(id).ifPresent(holder -> setBase(holder, value));
    }

    private void setBase(Holder<Attribute> attribute, double value) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    // ---------------------------------------------------------------- Spawn / NBT

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData groupData) {
        setVariant(this.random.nextInt(VARIANT_COUNT));
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, groupData);
        applyTensuraStats();
        return data;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant());
        tag.putInt("Stage", getStage().ordinal());
        tag.putBoolean("SkillsGranted", skillsGranted);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariant(tag.getInt("Variant"));
        this.entityData.set(DATA_STAGE, tag.getInt("Stage"));
        this.skillsGranted = tag.getBoolean("SkillsGranted");
        applyStageStats(false);
    }

    // Platzhalter-Sounds (Vanilla), später durch eigene ersetzbar
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }
}

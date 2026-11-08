package dev.ogrevillages.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Gibt Ogres Skills aus Tensura: Reincarnated.
 *
 * Die Skills werden über Tensuras eigene Methode SkillHelper.learnSkill(LivingEntity, ManasSkill) vergeben,
 * per Reflection. So braucht dieser Mod Tensura weder zum Kompilieren noch zum Starten; ist Tensura nicht
 * installiert (oder ein Skill-Name falsch), wird einfach nichts vergeben und einmal eine Warnung geloggt.
 *
 * Auswahl: die Skills, die Tensura den Ogre-Rassen selbst gibt (Strength; Self Regeneration + Steel Strength;
 * Ogre Berserker + Ultraspeed Regeneration; Divine Ki Release + Divine Berserker), ergänzt um passende
 * Passiv-Skills und Resistenzen. Aktive Skills (z. B. Ogre Berserker) sind vorhanden, werden von der
 * Mob-KI aber noch nicht benutzt.
 */
public final class OgreSkills {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String SKILL_PACKAGE = "io.github.manasmods.tensura.registry.skill.";
    private static final String HELPER_CLASS = "io.github.manasmods.tensura.ability.SkillHelper";

    // Format: "<Registry-Klasse>.<FELD>"
    private static final String STRENGTH = "CommonSkills.STRENGTH";
    private static final String SELF_REGENERATION = "CommonSkills.SELF_REGENERATION";
    private static final String STEEL_STRENGTH = "ExtraSkills.STEEL_STRENGTH";
    private static final String STRENGTHEN_BODY = "ExtraSkills.STRENGTHEN_BODY";
    private static final String DANGER_SENSE = "ExtraSkills.DANGER_SENSE";
    private static final String ULTRASPEED_REGENERATION = "ExtraSkills.ULTRASPEED_REGENERATION";
    private static final String MAJESTY = "ExtraSkills.MAJESTY";
    private static final String OGRE_BERSERKER = "IntrinsicSkills.OGRE_BERSERKER";
    private static final String DIVINE_KI_RELEASE = "IntrinsicSkills.DIVINE_KI_RELEASE";
    private static final String DIVINE_BERSERKER = "UniqueSkills.DIVINE_BERSERKER";
    private static final String PAIN_RESISTANCE = "ResistanceSkills.PAIN_RESISTANCE";
    private static final String PHYSICAL_RESISTANCE = "ResistanceSkills.PHYSICAL_ATTACK_RESISTANCE";
    private static final String FLAME_RESISTANCE = "ResistanceSkills.FLAME_ATTACK_RESISTANCE";
    private static final String SPIRITUAL_RESISTANCE = "ResistanceSkills.SPIRITUAL_ATTACK_RESISTANCE";
    private static final String DARKNESS_RESISTANCE = "ResistanceSkills.DARKNESS_ATTACK_RESISTANCE";
    private static final String ABNORMAL_RESISTANCE = "ResistanceSkills.ABNORMAL_CONDITION_RESISTANCE";

    private static final Set<String> WARNED = new HashSet<>();
    private static Method learnMethod;
    private static boolean learnMethodSearched;

    private OgreSkills() {
    }

    /** Vollständige Skill-Liste einer Stufe (Skills früherer Stufen bleiben beim Entwickeln erhalten). */
    public static List<String> forStage(OgreEntity.Stage stage) {
        List<String> enlightened = List.of(STRENGTH, SELF_REGENERATION, STEEL_STRENGTH, PAIN_RESISTANCE);
        List<String> wicked = List.of(STRENGTH, OGRE_BERSERKER, ULTRASPEED_REGENERATION,
                PAIN_RESISTANCE, PHYSICAL_RESISTANCE);
        List<String> mystic = concat(enlightened, DANGER_SENSE, FLAME_RESISTANCE);
        List<String> spirit = concat(mystic, SPIRITUAL_RESISTANCE, STRENGTHEN_BODY);
        List<String> death = concat(wicked, DANGER_SENSE, DARKNESS_RESISTANCE);

        return switch (stage) {
            case OGRE -> List.of(STRENGTH);
            case KIJIN -> List.of(STRENGTH, STRENGTHEN_BODY, PAIN_RESISTANCE);
            case ENLIGHTENED_OGRE -> enlightened;
            case MYSTIC_ONI -> mystic;
            case WICKED_ONI -> wicked;
            case SPIRIT_ONI -> spirit;
            case DEATH_ONI -> death;
            case DIVINE_ONI -> concat(spirit, DIVINE_KI_RELEASE, MAJESTY, ABNORMAL_RESISTANCE);
            case DIVINE_FIGHTER -> concat(death, DIVINE_KI_RELEASE, DIVINE_BERSERKER, ABNORMAL_RESISTANCE);
        };
    }

    private static List<String> concat(List<String> base, String... more) {
        List<String> result = new ArrayList<>(base);
        result.addAll(List.of(more));
        return result;
    }

    /** Vergibt alle Skills der aktuellen Stufe (nur serverseitig aufrufen). */
    public static void grant(OgreEntity ogre) {
        if (!ModList.get().isLoaded("tensura")) {
            return;
        }
        Method learn = findLearnMethod();
        if (learn == null) {
            return;
        }
        for (String id : forStage(ogre.getStage())) {
            try {
                Object skill = resolveSkill(id);
                learn.invoke(null, ogre, skill);
            } catch (ReflectiveOperationException | RuntimeException e) {
                warnOnce(id, e);
            }
        }
    }

    private static Object resolveSkill(String id) throws ReflectiveOperationException {
        int dot = id.indexOf('.');
        Class<?> registry = Class.forName(SKILL_PACKAGE + id.substring(0, dot));
        Object holder = registry.getField(id.substring(dot + 1)).get(null);
        return ((Supplier<?>) holder).get();
    }

    private static Method findLearnMethod() {
        if (learnMethodSearched) {
            return learnMethod;
        }
        learnMethodSearched = true;
        try {
            Class<?> helper = Class.forName(HELPER_CLASS);
            for (Method m : helper.getMethods()) {
                if (m.getName().equals("learnSkill")
                        && m.getParameterCount() == 2
                        && m.getParameterTypes()[0] == LivingEntity.class
                        && m.getParameterTypes()[1].getName().endsWith(".ManasSkill")) {
                    learnMethod = m;
                    break;
                }
            }
            if (learnMethod == null) {
                LOGGER.warn("[ogrevillages] Tensura SkillHelper.learnSkill(LivingEntity, ManasSkill) not found; ogres get no skills.");
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.warn("[ogrevillages] Could not access Tensura skill API; ogres get no skills.", e);
        }
        return learnMethod;
    }

    private static void warnOnce(String id, Exception e) {
        if (WARNED.add(id)) {
            LOGGER.warn("[ogrevillages] Could not give skill {}: {}", id, e.toString());
        }
    }
}

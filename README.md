# Ogre Villages – Tensura Addon (NeoForge 1.21.1)

Fügt Ogres als Mobs und Ogre-Dörfer hinzu. Gebaut gegen **Tensura: Reincarnated 2.0.1.5**.

## Was drin ist

- **Ogre-Mob** (`ogrevillages:ogre`): Spieler-Modell (64x64-Skins, inkl. zweiter Ebene) plus **eigenes 3D-Modell** (zwei gebogene Hörner mit eigener Textur). Spawn-Ei im Kreativ-Tab.
- **Ganze Ogre-Linie als Stufen** mit den Standard-Stats aus Tensuras `OgreConfig`: Ogre, Kijin, Enlightened Ogre, Mystic Oni, Wicked Oni, Spirit Oni, Death Oni, Divine Oni, Divine Fighter (HP, Angriff, Angriffstempo, Bewegung, Rückstoß-Resistenz; Aura/Magicule/Spirit-HP werden als Tensura-Attribute gesetzt, falls Tensura geladen ist). Die Werte stehen im Enum `OgreEntity.Stage`.
- **Tensura-Naming**: Der Ogre steht im Entity-Tag `tensura:can_be_named` (`data/tensura/tags/entity_type/can_be_named.json`). Mit **N** öffnest du Tensuras Naming-Menü und benennst ihn nach Tensuras Regeln (Magicule-Kosten, Ziel muss sich unterwerfen usw.). Danach entwickelt er sich vom **Ogre zum Enlightened Ogre**.
- **Weitere Stufen** sind noch nicht über Spielmechaniken erreichbar (Spirit, Harvest Festival, Essences, EP kommen von Tensura). Zum Testen: `/ogrevillage evolve <stufe>` (OP) setzt den nächsten Ogre (8 Blöcke) auf die Stufe.
- **Natürliches Spawnen**: einzeln oder zu zweit in allen Overworld-Biomen (`neoforge/biome_modifier/ogre_spawns.json`, weight 4).
- **Ogre-Dorf**: Dorfplatz mit Lagerfeuer + 4–6 Häuser (Steinsockel, weißer Putz, dunkle Holzbalken, Satteldach, Veranda, Tür, Fenster, Bett, Ofen usw.), Eingänge zur Mitte, mit Ogres als Bewohner. Ogres können Türen öffnen. Generiert in Wald-, Taiga-, Ebenen- und Wiesenbiomen (`structure_set/ogre_villages.json`).
- **Command**: `/create structure ogre` (oder `/ogrevillage`, OP-Level 2) platziert ein Dorf an deiner Position, **inklusive Ogres**.

Der Mod braucht Tensura **nicht** zum Kompilieren (keine Abhängigkeit im Code), nur zum Spielen der Tensura-Funktionen.

## Jar über GitHub bauen

1. Auf github.com ein neues Repository anlegen (z. B. `ogre-villages`).
2. Die ZIP zuerst **entpacken** (GitHub entpackt sie nicht selbst). Dann den **Inhalt** des Ordners `ogre_villages` hochladen: Add file → Upload files, alle Dateien und Ordner per Drag-and-drop hineinziehen (auch `.github` und `gradle`), dann *Commit changes*. Alternativ per Git pushen.
   Fehlt nach dem Upload der Ordner `.github`: Add file → Create new file, als Namen `.github/workflows/build.yml` eintippen und den Inhalt der Datei aus dem entpackten Ordner einfügen.
3. Reiter **Actions** öffnen → Workflow **Build Mod** läuft bei jedem Push automatisch (oder manuell mit „Run workflow“).
4. Nach dem Durchlauf unter **Artifacts** → `ogre-villages-jar` herunterladen. Die Jar im ZIP kommt in den `mods`-Ordner.
5. Optional: Tag `v0.1.0` anlegen → GitHub erstellt automatisch ein **Release** mit der Jar.

Hinweis: Der Code wurde bisher nicht kompiliert. Schlägt der erste Build fehl, bitte das Log (roter Schritt „Build jar“) schicken.

## Skins

64x64-Skins (normale Arme, nicht „slim“) liegen in `src/main/resources/assets/ogrevillages/textures/entity/ogre/`:

```
skin_0.png ... skin_3.png   (4 Skins)
ogre_horns.png              (Hörner, 16x16)
```

Jeder Ogre bekommt beim Spawn zufällig einen der Skins und **behält ihn bei jeder Entwicklung**. Mehr Skins: `skin_4.png` usw. anlegen und `VARIANT_COUNT` in `OgreEntity.java` erhöhen. Haben die Skins eigene Hörner, die Zeile `addLayer` in `OgreRenderer.java` entfernen.

## Skills (aus Tensura, per `SkillHelper.learnSkill`)

Ogres lernen ihre Skills kurz nach dem Spawn bzw. nach jeder Entwicklung. Frühere Skills bleiben erhalten. Auswahl und Zuordnung stehen in `OgreSkills.java`.

| Stufe | Skills |
|---|---|
| Ogre | Strength |
| Kijin | Strength, Strengthen Body, Pain Resistance |
| Enlightened Ogre | Strength, Self Regeneration, Steel Strength, Pain Resistance |
| Mystic Oni | + Danger Sense, Flame Attack Resistance |
| Wicked Oni | Strength, Ogre Berserker, Ultraspeed Regeneration, Pain Resistance, Physical Attack Resistance |
| Spirit Oni | Mystic Oni + Spiritual Attack Resistance, Strengthen Body |
| Death Oni | Wicked Oni + Danger Sense, Darkness Attack Resistance |
| Divine Oni | Spirit Oni + Divine Ki Release, Majesty, Abnormal Condition Resistance |
| Divine Fighter | Death Oni + Divine Ki Release, Divine Berserker, Abnormal Condition Resistance |

Die Skills, die Tensura den Rassen selbst gibt (Strength; Self Regeneration + Steel Strength; Ogre Berserker + Ultraspeed Regeneration; Divine Ki Release + Divine Berserker), sind enthalten, der Rest sind passende Passiv-Skills und Resistenzen. Aktive Skills (z. B. Ogre Berserker) sind vorhanden, werden von der Mob-KI aber noch nicht eingesetzt. Ohne Tensura passiert nichts, bei Problemen steht eine Warnung im Log (`[ogrevillages]`).

## Noch offen

- Ogres folgen dem Spieler, der sie benannt hat, noch nicht (dafür müsste die Entity Tensuras `ISubordinate` implementieren, d. h. eine Compile-Abhängigkeit auf Tensura, ManasCore, Architectury und GeckoLib).
- Skill-Einsatz durch die Mob-KI, Evolution über EP/Spirit/Essences.

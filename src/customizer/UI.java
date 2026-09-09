package customizer;

import arc.func.Cons;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.ui.Styles;

public class UI {
    public static UnitType body = UnitTypes.corvus;
    public static UnitType legSource = UnitTypes.toxopid;

    public static boolean flying = false;
    public static boolean legs = true;

    public static int legCount = 6;
    public static float legLength = 30f;

    public static float hpMult = 1.0f;
    public static float spdMult = 1.0f;

    public static Seq<WConfig> weapons = new Seq<>();

    public static class WConfig {
        public Weapon weapon;
        public float reloadMult = 1.0f;
        public float damageMult = 1.0f;

        public WConfig(Weapon weapon) {
            this.weapon = weapon;
        }
    }

    public static void init() {
        if (UnitTypes.corvus.weapons.size > 0) {
            weapons.add(new WConfig(UnitTypes.corvus.weapons.first()));
        }

        Vars.ui.hudGroup.fill(p -> {
            p.top().right().marginTop(100f);
            p.button("Кастомизация", Styles.cleart, UI::showMain).width(150f).height(40f);
        });
    }

    public static void showMain() {
        Dialog dialog = new Dialog("Кастомизатор");
        Table cont = dialog.cont;
        cont.clear();

        cont.pane(t -> {
            t.defaults().pad(4f).left();

            // 1. Корпус и Мобильность
            t.add("[accent]1. Корпус и Мобильность[]").row();
            t.add("Корпус: ");
            t.button(body.localizedName, () -> pickUnit("Корпус", u -> { body = u; showMain(); })).width(200f).row();

            t.add("Летающий: ");
            t.button(flying ? "[green]Да[]" : "[red]Нет[]", () -> { flying = !flying; showMain(); }).width(200f).row();

            if (!flying) {
                t.add("Ноги паука: ");
                t.button(legs ? "[green]Да[]" : "[red]Нет[]", () -> { legs = !legs; showMain(); }).width(200f).row();
            }

            // 2. Ноги
            if (!flying && legs) {
                t.add("[accent]2. Настройка Ног[]").row();
                t.add("Стиль ног: ");
                t.button(legSource.localizedName, () -> pickUnit("Ноги", u -> { legSource = u; showMain(); })).width(200f).row();

                t.add("Кол-во ног: " + legCount);
                Slider sCount = new Slider(2, 12, 1, false);
                sCount.setValue(legCount);
                sCount.changed(() -> legCount = (int) sCount.getValue());
                t.add(sCount).width(180f).row();

                t.add("Длина ног: " + (int) legLength);
                Slider sLen = new Slider(10, 80, 1, false);
                sLen.setValue(legLength);
                sLen.changed(() -> legLength = sLen.getValue());
                t.add(sLen).width(180f).row();
            }

            // 3. Статы
            t.add("[accent]3. Характеристики[]").row();
            t.add("ХП (x" + (int)(hpMult * 10) / 10f + "):");
            Slider sHp = new Slider(0.1f, 10f, 0.1f, false);
            sHp.setValue(hpMult);
            sHp.changed(() -> hpMult = sHp.getValue());
            t.add(sHp).width(180f).row();

            t.add("Скорость (x" + (int)(spdMult * 10) / 10f + "):");
            Slider sSpd = new Slider(0.1f, 5f, 0.1f, false);
            sSpd.setValue(spdMult);
            sSpd.changed(() -> spdMult = sSpd.getValue());
            t.add(sSpd).width(180f).row();

            // 4. Оружие
            t.add("[accent]4. Оружие[]").row();
            for (int i = 0; i < weapons.size; i++) {
                int idx = i;
                WConfig wc = weapons.get(i);
                Table wt = new Table(Styles.flatBox).pad(6f);
                wt.add("[yellow]" + (idx + 1) + ". " + (wc.weapon.name == null || wc.weapon.name.isEmpty() ? "Пушка" : wc.weapon.name) + "[]").row();

                wt.add("Скорострельность: x" + (int)(wc.reloadMult * 10) / 10f);
                Slider sr = new Slider(0.1f, 5f, 0.1f, false);
                sr.setValue(wc.reloadMult);
                sr.changed(() -> wc.reloadMult = sr.getValue());
                wt.add(sr).row();

                wt.add("Урон: x" + (int)(wc.damageMult * 10) / 10f);
                Slider sd = new Slider(0.1f, 5f, 0.1f, false);
                sd.setValue(wc.damageMult);
                sd.changed(() -> wc.damageMult = sd.getValue());
                wt.add(sd).row();

                wt.button("Удалить", Styles.cleart, () -> {
                    weapons.remove(idx);
                    showMain();
                }).width(80f);

                t.add(wt).colspan(2).fillX().row();
            }

            t.button("+ Добавить пушку", () -> pickWeapon(wc -> {
                weapons.add(wc);
                showMain();
            })).width(200f).padTop(6f).row();

        }).grow().row();

        cont.button("Применить", Styles.defaultt, () -> {
            apply();
            dialog.hide();
        }).size(180f, 45f).pad(8f).row();

        dialog.addCloseButton();
        dialog.show();
    }

    private static void pickUnit(String title, Cons<UnitType> cons) {
        Dialog d = new Dialog(title);
        d.cont.pane(t -> {
            int i = 0;
            for (UnitType type : Vars.content.units()) {
                t.button(type.localizedName, () -> {
                    cons.get(type);
                    d.hide();
                }).width(140f).pad(2f);
                if (++i % 3 == 0) t.row();
            }
        });
        d.addCloseButton();
        d.show();
    }

    private static void pickWeapon(Cons<WConfig> cons) {
        Dialog d = new Dialog("Выбор пушки");
        d.cont.pane(t -> {
            for (UnitType type : Vars.content.units()) {
                if (type.weapons.size == 0) continue;
                t.add("[accent]" + type.localizedName + "[]").left().row();
                for (Weapon w : type.weapons) {
                    t.button(" - " + (w.name == null || w.name.isEmpty() ? "Пушка" : w.name), () -> {
                        cons.get(new WConfig(w));
                        d.hide();
                    }).width(220f).left().row();
                }
            }
        });
        d.addCloseButton();
        d.show();
    }

    private static void apply() {
        if (Vars.player.unit() == null) return;
        Unit cur = Vars.player.unit();

        UnitType custom = new UnitType("custom-" + System.currentTimeMillis()) {{
            constructor = flying ? UnitTypes.flare.constructor : 
                         (legs ? UnitTypes.toxopid.constructor : body.constructor);
            
            region = body.region;
            health = body.health * hpMult;
            speed = body.speed * spdMult;
            flying = UI.flying;
            armor = body.armor;
            hitSize = body.hitSize;

            if (!flying && legs) {
                legCount = UI.legCount;
                legLength = UI.legLength;
                legGroupSize = legSource.legGroupSize;
                legExtension = legSource.legExtension;
                legBaseOffset = legSource.legBaseOffset;
                legPairOffset = legSource.legPairOffset;
                legMoveSpace = legSource.legMoveSpace;
                legForwardSplat = legSource.legForwardSplat;
            }

            weapons.clear();
            for (WConfig wc : UI.weapons) {
                Weapon w = wc.weapon;
                w.reload = Math.max(1f, wc.weapon.reload / wc.reloadMult);
                weapons.add(w);
            }
        }};

        custom.init();

        Unit newUnit = custom.create(Vars.player.team());
        newUnit.set(cur.x, cur.y);
        newUnit.add();

        Vars.player.unit(newUnit);
        cur.destroy();
    }
}

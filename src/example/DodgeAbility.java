package example;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Groups;
import mindustry.gen.Unit;

public class DodgeAbility extends Ability {
    public float cooldown = 600f; // Перезарядка (10 сек)
    public float range = 100f;    // Радиус реагирования 
    public float dodgeForce = 12f; // Сила рывка
    public float dashDuration = 10f; // Время неуязвимости и следа 

    protected float timer = cooldown; 
    protected float dashTimer = 0f;
    protected float trailTick = 0f;

    // Эффект следа
    public static final Effect dodgeTrail = new Effect(20f, e -> {
        Draw.alpha(e.fout() * 0.5f); 
        if (e.data instanceof TextureRegion region) {
            Draw.rect(region, e.x, e.y, e.rotation - 90f);
        }
    });

    @Override
    public void update(Unit unit) {
        timer += Time.delta;

        if (timer >= cooldown) {
            boolean[] danger = {false};
            Groups.bullet.intersect(unit.x - range, unit.y - range, range * 2, range * 2, b -> {
                if (!danger[0] && b.team != unit.team && b.type != null && b.type.damage > 0) {
                    danger[0] = true;
                }
            });

            if (danger[0]) {
                // Юнит случайно выбирает угол: +90 (влево) или -90 (вправо)
                float angleOffset = Mathf.chance(0.5) ? 90f : -90f;
                
                Vec2 dodgeVec = new Vec2().trns(unit.rotation + angleOffset, dodgeForce);
                unit.vel.add(dodgeVec);
                
                unit.apply(StatusEffects.invincible, dashDuration);
                
                dashTimer = dashDuration; 
                timer = 0f; 
            }
        }

        // Шлейф
        if (dashTimer > 0) {
            trailTick += Time.delta;
            if (trailTick >= 2f) { 
                dodgeTrail.at(unit.x, unit.y, unit.rotation, unit.type.region);
                trailTick = 0f;
            }
            dashTimer -= Time.delta;
        }
    }

    // --- ИНТЕГРАЦИЯ В МЕНЮ ИНФОРМАЦИИ О ЮНИТЕ ---
    @Override
    public String localized() {
        return "Уклонение (рывок в сторону)\n[lightgray]Перезарядка:[] " + (cooldown / 60f) + " сек\n[lightgray]Дистанция:[] ~4 блока";
    }
}
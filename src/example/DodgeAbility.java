package example;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.util.Time;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Groups;
import mindustry.gen.Unit;

public class DodgeAbility extends Ability {
    public float cooldown = 600f; // Перезарядка (10 сек)
    public float range = 120f;    // Радиус реагирования (в пикселях)
    public float dodgeForce = 20f; // Сила рывка
    public float dashDuration = 15f; // Длительность шлейфа и неуязвимости (в тиках)

    protected float timer = cooldown; 
    protected float dashTimer = 0f;
    protected float trailTick = 0f;

    // Визуальный эффект полупрозрачного следа
    public static final Effect dodgeTrail = new Effect(30f, e -> {
        Draw.alpha(e.fout() * 0.5f); 
        if (e.data instanceof TextureRegion region) {
            Draw.rect(region, e.x, e.y, e.rotation - 90f);
        }
    });

    @Override
    public void update(Unit unit) {
        timer += Time.delta;

        // 1. Поиск летящих в нас пуль
        if (timer >= cooldown) {
            boolean[] danger = {false};
            Groups.bullet.intersect(unit.x - range, unit.y - range, range * 2, range * 2, b -> {
                if (!danger[0] && b.team != unit.team && b.type != null && b.type.damage > 0) {
                    danger[0] = true;
                }
            });

            // 2. Активация уворота
            if (danger[0]) {
                Vec2 dodgeVec = new Vec2().trns(unit.rotation - 180f, dodgeForce);
                unit.vel.add(dodgeVec);
                
                // ВЫДАЧА НЕУЯЗВИМОСТИ на время рывка (ванильный статус-эффект)
                unit.apply(StatusEffects.invincible, dashDuration);
                
                dashTimer = dashDuration; 
                timer = 0f; 
            }
        }

        // 3. Отрисовка шлейфа (afterimage)
        if (dashTimer > 0) {
            trailTick += Time.delta;
            if (trailTick >= 2f) { // Частота появления фантомов
                dodgeTrail.at(unit.x, unit.y, unit.rotation, unit.type.region);
                trailTick = 0f;
            }
            dashTimer -= Time.delta;
        }
    }
}
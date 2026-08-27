package example;

import mindustry.content.UnitTypes;
import mindustry.mod.Mod;

public class ExampleJavaMod extends Mod {

    public ExampleJavaMod() {
        // Конструктор мода
    }

    @Override
    public void loadContent() {
        // Добавляем нашу способность ванильному юниту
        UnitTypes.antumbra.abilities.add(new DodgeAbility());
    }
}
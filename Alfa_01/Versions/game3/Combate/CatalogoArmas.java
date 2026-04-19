package com.root.game.Combate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CatalogoArmas {

    private static final List<Arma> armas = new ArrayList<>();
    private static final Random random = new Random();

    static {
        armas.add(new Arma(
            "Claymore",
            15,
            "Cartas/Frente/Armas/claymore.jpeg"
        ));

        armas.add(new Arma(
            "Espada Curta",
            5,
            "Cartas/Frente/Armas/punhal.jpeg"
        ));

    }

    public static Arma gerarArmaAleatoria() {
        Arma modelo = armas.get(random.nextInt(armas.size()));
        return modelo.copiar();
    }
}

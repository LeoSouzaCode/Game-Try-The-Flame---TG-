package com.root.game.Combate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CatalogoInimigos {

    private static final List<Inimigo> inimigos = new ArrayList<>();
    private static final Random random = new Random();

    static {
        inimigos.add(new Inimigo(
            "Esqueleto",
            20,
            "Cartas/Frente/Inimigo/frenteTeste0.jpg"
        ));

        inimigos.add(new Inimigo(
            "Esqueleto",
            20,
            "Cartas/Frente/Inimigo/frenteTeste1.jpg"
        ));

        inimigos.add(new Inimigo(
            "Lobo",
            12,
            "Cartas/Frente/Inimigo/frenteTeste2.jpg"
        ));

        inimigos.add(new Inimigo(
            "Espírito",
            23,
            "Cartas/Frente/Inimigo/frenteTeste3.jpg"
        ));

        inimigos.add(new Inimigo(
            "Zumbi",
            15,
            "Cartas/Frente/Inimigo/frenteTeste5.jpg"
        ));

        inimigos.add(new Inimigo(
            "Espirito",
            23,
            "Cartas/Frente/Inimigo/frenteTeste6.jpg"
        ));

        inimigos.add(new Inimigo(
            "Rato",
            6,
            "Cartas/Frente/Inimigo/frenteTeste8.jpg"
        ));

        inimigos.add(new Inimigo(
            "Lobo",
            12,
            "Cartas/Frente/Inimigo/frenteTeste9.jpg"
        ));

        inimigos.add(new Inimigo(
            "Rato",
            6,
            "Cartas/Frente/Inimigo/frenteTeste10.jpg"
        ));

        inimigos.add(new Inimigo(
            "Zumbi",
            15,
            "Cartas/Frente/Inimigo/frenteTeste11.jpg"
        ));

    }

    public static Inimigo gerarInimigoAleatorio() {
        Inimigo modelo = inimigos.get(random.nextInt(inimigos.size()));
        return modelo.copiar();
    }
}

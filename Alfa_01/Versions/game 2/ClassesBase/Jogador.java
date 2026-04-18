package com.root.game.ClassesBase;

public class Jogador extends Entidade {

    private int chamasColetadas = 0;

    public Jogador() {
        vida = 10;
        ataque = 5;
        defesa = 1;
    }

    public void coletarChama() {
        chamasColetadas++;
    }

    public boolean venceuJogo() {
        return chamasColetadas >= 3;
    }
}


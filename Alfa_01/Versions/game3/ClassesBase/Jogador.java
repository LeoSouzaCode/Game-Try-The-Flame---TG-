package com.root.game.ClassesBase;

public class Jogador{

    private int chamasColetadas = 0;

    public void coletarChama() {
        chamasColetadas++;
    }

    public boolean venceuJogo() {
        return chamasColetadas >= 3;
    }
}


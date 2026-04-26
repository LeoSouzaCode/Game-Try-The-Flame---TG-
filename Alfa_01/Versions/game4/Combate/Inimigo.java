package com.root.game.Combate;

public class Inimigo extends EntidadeCombate {

    public Inimigo(String nome, int vida, String texturaPath) {
        super(nome, vida, texturaPath);
    }

    public int getVida() {
        return pontosDeVida;
    }

    public Inimigo copiar() {
        return new Inimigo(this.nome, this.pontosDeVida, this.texturaPath);
    }
}

package com.root.game.Combate;

public abstract class EntidadeCombate {

    protected String nome;
    protected int pontosDeVida;
    protected String texturaPath;

    public EntidadeCombate(String nome, int pontosDeVida, String texturaPath) {
        this.nome = nome;
        this.pontosDeVida = pontosDeVida;
        this.texturaPath = texturaPath;
    }

    public String getNome() {
        return nome;
    }

    public int getPontosDeVida() {
        return pontosDeVida;
    }

    public void setPontosDeVida(int pontosDeVida) {
        this.pontosDeVida = Math.max(0, pontosDeVida);
    }

    public String getTexturaPath() {
        return texturaPath;
    }

    public boolean estaVivo() {
        return pontosDeVida > 0;
    }
}

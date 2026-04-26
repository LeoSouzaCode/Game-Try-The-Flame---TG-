package com.root.game.Combate;

public class Jogador {

    private int vida;
    private Arma armaEquipada;

    public Jogador(int vida) {
        this.vida = vida;
    }

    public int getVida() {
        return vida;
    }

    public void setVida(int vida) {
        this.vida = Math.max(0, vida);
    }

    public boolean estaVivo() {
        return vida > 0;
    }

    public Arma getArmaEquipada() {
        return armaEquipada;
    }

    public void setArmaEquipada(Arma armaEquipada) {
        this.armaEquipada = armaEquipada;
    }

    public void removerArma() {
        this.armaEquipada = null;
    }
}

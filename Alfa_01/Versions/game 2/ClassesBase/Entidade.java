package com.root.game.ClassesBase;

public abstract class Entidade {

    protected int vida;
    protected int ataque;
    protected int defesa;

    public boolean estaViva() {
        return vida > 0;
    }

    public void receberDano(int dano) {
        int danoFinal = Math.max(0, dano - defesa);
        vida -= danoFinal;
    }

    public int atacar() {
        return ataque;
    }
}

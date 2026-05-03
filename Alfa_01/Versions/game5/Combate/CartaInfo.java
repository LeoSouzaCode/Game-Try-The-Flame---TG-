package com.root.game.Combate;

import com.root.game.CorpoPrincipal.Tabuleiro;

public class CartaInfo {

    private final Tabuleiro.TipoCarta tipo;
    private EstadoCarta estado;

    private Inimigo inimigo;
    private Arma armaDentro;

    public CartaInfo(Tabuleiro.TipoCarta tipo) {
        this.tipo = tipo;
        this.estado = EstadoCarta.FECHADA;
    }

    public Tabuleiro.TipoCarta getTipo() {
        return tipo;
    }

    public EstadoCarta getEstado() {
        return estado;
    }

    public void setEstado(EstadoCarta estado) {
        this.estado = estado;
    }

    public Inimigo getInimigo() {
        return inimigo;
    }

    public void setInimigo(Inimigo inimigo) {
        this.inimigo = inimigo;
    }

    public Arma getArmaDentro() {
        return armaDentro;
    }

    public void setArmaDentro(Arma armaDentro) {
        this.armaDentro = armaDentro;
    }

}

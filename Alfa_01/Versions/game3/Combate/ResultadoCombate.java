package com.root.game.Combate;

public class ResultadoCombate {

    private final boolean jogadorVenceu;
    private final boolean jogadorMorreu;
    private final boolean armaQuebrou;

    private final int vidaInicialJogador;
    private final int vidaFinalJogador;

    private final int durabilidadeInicialArma;
    private final int durabilidadeFinalArma;

    private final String nomeInimigo;
    private final String mensagemResultado;

    public ResultadoCombate(
        boolean jogadorVenceu,
        boolean jogadorMorreu,
        boolean armaQuebrou,
        int vidaInicialJogador,
        int vidaFinalJogador,
        int durabilidadeInicialArma,
        int durabilidadeFinalArma,
        String nomeInimigo,
        String mensagemResultado
    ) {
        this.jogadorVenceu = jogadorVenceu;
        this.jogadorMorreu = jogadorMorreu;
        this.armaQuebrou = armaQuebrou;
        this.vidaInicialJogador = vidaInicialJogador;
        this.vidaFinalJogador = vidaFinalJogador;
        this.durabilidadeInicialArma = durabilidadeInicialArma;
        this.durabilidadeFinalArma = durabilidadeFinalArma;
        this.nomeInimigo = nomeInimigo;
        this.mensagemResultado = mensagemResultado;
    }

    public boolean isJogadorVenceu() {
        return jogadorVenceu;
    }

    public boolean isJogadorMorreu() {
        return jogadorMorreu;
    }

    public boolean isArmaQuebrou() {
        return armaQuebrou;
    }

    public int getVidaInicialJogador() {
        return vidaInicialJogador;
    }

    public int getVidaFinalJogador() {
        return vidaFinalJogador;
    }

    public int getDurabilidadeInicialArma() {
        return durabilidadeInicialArma;
    }

    public int getDurabilidadeFinalArma() {
        return durabilidadeFinalArma;
    }

    public String getNomeInimigo() {
        return nomeInimigo;
    }

    public String getMensagemResultado() {
        return mensagemResultado;
    }
}

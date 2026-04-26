package com.root.game.Combate;

public class SistemaCombate {

    public ResultadoCombate resolverCombate(Jogador jogador, Inimigo inimigo) {

        int danoTotal = inimigo.getVida();

        int vidaInicialJogador = jogador.getVida();

        Arma arma = jogador.getArmaEquipada();
        int durabilidadeInicialArma = arma != null ? arma.getDurabilidade() : 0;

        boolean armaQuebrou = false;
        int durabilidadeFinalArma = durabilidadeInicialArma;

        if (arma != null && !arma.estaQuebrada()) {
            int durabilidadeAtual = arma.getDurabilidade();

            if (durabilidadeAtual >= danoTotal) {
                arma.setDurabilidade(durabilidadeAtual - danoTotal);
                durabilidadeFinalArma = arma.getDurabilidade();
                danoTotal = 0;

                if (arma.estaQuebrada()) {
                    armaQuebrou = true;
                    jogador.removerArma();
                    durabilidadeFinalArma = 0;
                }

            } else {
                danoTotal -= durabilidadeAtual;
                arma.setDurabilidade(0);
                jogador.removerArma();
                armaQuebrou = true;
                durabilidadeFinalArma = 0;
            }
        }

        if (danoTotal > 0) {
            jogador.setVida(jogador.getVida() - danoTotal);
        }

        boolean jogadorMorreu = !jogador.estaVivo();
        boolean jogadorVenceu = !jogadorMorreu;

        String mensagemResultado;
        if (jogadorVenceu) {
            mensagemResultado = "Você venceu! O " + inimigo.getNome() + " foi derrotado.";
        } else {
            mensagemResultado = "Você foi derrotado por " + inimigo.getNome() + ".";
        }

        return new ResultadoCombate(
            jogadorVenceu,
            armaQuebrou,
            vidaInicialJogador,
            jogador.getVida(),
            durabilidadeInicialArma,
            durabilidadeFinalArma,
            mensagemResultado
        );
    }
}

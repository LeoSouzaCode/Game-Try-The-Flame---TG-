package com.root.game.Animacoes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.math.Interpolation;
import com.root.game.CorpoPrincipal.Tabuleiro;
import com.root.game.Utils.PosicaoCartaProvider;
import com.root.game.ClassesBase.Cartas;

public class AnimacaoTabuleiro {

    private final Stage stageTabuleiro;
    private final Cartas[][] cartasVisuais;
    private final float cartaLargura;
    private final float cartaAltura;
    private final float espaco;
    private final PosicaoCartaProvider posicaoProvider;

    public AnimacaoTabuleiro(
        Stage stageTabuleiro,
        Cartas[][] cartasVisuais,
        float cartaLargura,
        float cartaAltura,
        float espaco,
        PosicaoCartaProvider posicaoProvider
    ) {
        this.stageTabuleiro = stageTabuleiro;
        this.cartasVisuais = cartasVisuais;
        this.cartaLargura = cartaLargura;
        this.cartaAltura = cartaAltura;
        this.espaco = espaco;
        this.posicaoProvider = posicaoProvider;
    }

    public void animarMovimentoJogadorComEsteira(
        int antigaLinha,
        int antigaColuna,
        int novaLinha,
        int novaColuna,
        Runnable aoFinalizar
    ) {
        Cartas jogador = cartasVisuais[antigaLinha][antigaColuna];
        if (jogador == null) {
            if (aoFinalizar != null) aoFinalizar.run();
            return;
        }

        float levantarDuracao = 0.10f;
        float esteiraDuracao = 0.22f;
        float descidaDuracao = 0.09f;

        float baseX = getCartaX(antigaColuna);
        float baseY = getCartaY(antigaLinha);

        float destinoX = getCartaX(novaColuna);
        float destinoY = getCartaY(novaLinha);

        jogador.clearActions();
        jogador.setOrigin(Align.center);
        jogador.toFront();
        jogador.setPosition(baseX, baseY);

        jogador.addAction(
            Actions.sequence(
                // levanta
                Actions.parallel(
                    Actions.moveTo(baseX, baseY + 22f, levantarDuracao, Interpolation.pow2Out),
                    Actions.scaleTo(1.08f, 1.20f, levantarDuracao, Interpolation.pow2Out)
                ),

                // esteira começa
                Actions.run(() -> animarEsteiraBonita(
                    antigaLinha, antigaColuna, novaLinha, novaColuna, esteiraDuracao
                )),

                // espera a esteira quase terminar
                Actions.delay(esteiraDuracao * 0.88f),

                // jogador aparece acima da nova casa
                Actions.run(() -> {
                    jogador.setPosition(destinoX, destinoY + 18f);
                    jogador.toFront();
                }),

                // desce
                Actions.parallel(
                    Actions.moveTo(destinoX, destinoY, descidaDuracao, Interpolation.pow2In),
                    Actions.scaleTo(1f, 1f, descidaDuracao, Interpolation.fade)
                ),

                Actions.run(() -> {
                    if (aoFinalizar != null) aoFinalizar.run();
                })
            )
        );
    }



    private void animarEsteiraBonita(
        int antigaLinha,
        int antigaColuna,
        int novaLinha,
        int novaColuna,
        float duracao
    ) {
        int dx = novaColuna - antigaColuna;
        int dy = novaLinha - antigaLinha;

        float passoX = (cartaLargura + espaco) * 1.10f;
        float passoY = (cartaAltura + espaco) * 1.10f;

        if (dx != 0) {
            int linha = antigaLinha;

            for (int j = 0; j < Tabuleiro.COLUNAS; j++) {
                Cartas carta = cartasVisuais[linha][j];
                if (carta == null) continue;
                if (j == antigaColuna) continue;

                float atraso = Math.abs(j - antigaColuna) * 0.012f;
                float moveX = -dx * passoX;

                boolean saiPelaEsquerda = (moveX < 0 && j == 0);
                boolean saiPelaDireita = (moveX > 0 && j == Tabuleiro.COLUNAS - 1);
                boolean saiDoTabuleiro = saiPelaEsquerda || saiPelaDireita;

                animarCartaEsteiraComEntradaESaida(
                    carta,
                    moveX,
                    0f,
                    atraso,
                    duracao,
                    saiDoTabuleiro
                );
            }
        } else if (dy != 0) {
            int coluna = antigaColuna;

            for (int i = 0; i < Tabuleiro.LINHAS; i++) {
                Cartas carta = cartasVisuais[i][coluna];
                if (carta == null) continue;
                if (i == antigaLinha) continue;

                float atraso = Math.abs(i - antigaLinha) * 0.012f;

                // se ficar invertido no seu visual, troque para -dy * passoY
                float moveY = dy * passoY;

                boolean saiPorBaixo = (moveY < 0 && i == 0);
                boolean saiPorCima = (moveY > 0 && i == Tabuleiro.LINHAS - 1);
                boolean saiDoTabuleiro = saiPorBaixo || saiPorCima;

                animarCartaEsteiraComEntradaESaida(
                    carta,
                    0f,
                    moveY,
                    atraso,
                    duracao,
                    saiDoTabuleiro
                );
            }
        }
    }

    private void animarCartaEsteiraComEntradaESaida(
        Cartas carta,
        float moveX,
        float moveY,
        float delay,
        float duracao,
        boolean cartaSaiDoTabuleiro
    ) {
        if (carta == null) return;

        carta.clearActions();

        float inicioX = carta.getX();
        float inicioY = carta.getY();

        if (!cartaSaiDoTabuleiro) {
            carta.addAction(
                Actions.sequence(
                    Actions.delay(delay),
                    Actions.parallel(
                        Actions.moveTo(inicioX + moveX, inicioY + moveY, duracao, Interpolation.smoother),
                        Actions.sequence(
                            Actions.scaleTo(1.03f, 0.98f, duracao * 0.45f, Interpolation.fade),
                            Actions.scaleTo(1f, 1f, duracao * 0.55f, Interpolation.fade)
                        )
                    )
                )
            );
            return;
        }

        float bordaEsquerda = getCartaX(0);
        float bordaDireita = getCartaX(Tabuleiro.COLUNAS - 1);
        float bordaBaixo = getCartaY(Tabuleiro.LINHAS - 1);
        float bordaCima = getCartaY(0);

        float extraBorda = (cartaAltura + espaco) * 2f;

        // horizontal usa carta temporária
        if (moveX != 0f) {
            float saidaX = inicioX + (moveX < 0 ? -extraBorda : extraBorda);
            float saidaY = inicioY;

            float entradaX = (moveX < 0)
                ? bordaDireita + extraBorda
                : bordaEsquerda - extraBorda;
            float entradaY = inicioY;

            float destinoEntradaX = (moveX < 0)
                ? bordaDireita
                : bordaEsquerda;
            float destinoEntradaY = inicioY;

            Image cartaTemp = new Image(carta.getTexturaAtual());
            cartaTemp.setSize(carta.getWidth(), carta.getHeight());
            cartaTemp.setOrigin(Align.center);
            cartaTemp.setPosition(entradaX, entradaY);
            cartaTemp.setScale(0.90f, 0.90f);
            cartaTemp.getColor().a = 0f;
            stageTabuleiro.addActor(cartaTemp);
            cartaTemp.toFront();

            carta.addAction(
                Actions.sequence(
                    Actions.delay(delay),
                    Actions.parallel(
                        Actions.moveTo(saidaX, saidaY, duracao * 0.46f, Interpolation.pow2In),
                        Actions.scaleTo(0.90f, 0.90f, duracao * 0.46f, Interpolation.fade),
                        Actions.fadeOut(duracao * 0.42f, Interpolation.fade)
                    ),
                    Actions.run(() -> {
                        carta.setPosition(inicioX, inicioY);
                        carta.setScale(1f, 1f);
                        carta.getColor().a = 0f;
                        carta.setVisible(false);
                    })
                )
            );

            cartaTemp.addAction(
                Actions.sequence(
                    Actions.delay(delay + duracao * 0.40f),
                    Actions.parallel(
                        Actions.moveTo(destinoEntradaX, destinoEntradaY, duracao * 0.54f, Interpolation.pow2Out),
                        Actions.scaleTo(1f, 1f, duracao * 0.54f, Interpolation.fade),
                        Actions.fadeIn(duracao * 0.50f, Interpolation.fade)
                    ),
                    Actions.removeActor()
                )
            );

            return;
        }

        // vertical usa a própria carta
        float saidaX = inicioX;
        float saidaY = inicioY + (moveY < 0 ? -extraBorda : extraBorda);

        float entradaY = (moveY < 0)
            ? bordaCima + extraBorda
            : bordaBaixo - extraBorda;

        carta.addAction(
            Actions.sequence(
                Actions.delay(delay),
                Actions.parallel(
                    Actions.moveTo(saidaX, saidaY, duracao * 0.46f, Interpolation.pow2In),
                    Actions.scaleTo(0.90f, 0.90f, duracao * 0.46f, Interpolation.fade),
                    Actions.fadeOut(duracao * 0.42f, Interpolation.fade)
                ),
                Actions.run(() -> {
                    carta.setPosition(inicioX, entradaY);
                    carta.setScale(0.90f, 0.90f);
                    carta.getColor().a = 0f;
                    carta.setVisible(true);
                }),
                Actions.parallel(
                    Actions.moveTo(inicioX, inicioY, duracao * 0.54f, Interpolation.pow2Out),
                    Actions.scaleTo(1f, 1f, duracao * 0.54f, Interpolation.fade),
                    Actions.fadeIn(duracao * 0.50f, Interpolation.fade)
                )
            )
        );
    }

    public void animarCartaMovimentoInvalido(int linha, int coluna) {
        Cartas carta = cartasVisuais[linha][coluna];
        if (carta == null) return;

        carta.toFront();

        final float xOriginal = carta.getX();
        final float yOriginal = carta.getY();
        final Color corOriginal = new Color(carta.getColor());

        carta.addAction(
            Actions.sequence(
                Actions.parallel(
                    Actions.color(new Color(1f, 0.15f, 0.15f, corOriginal.a), 0.08f),
                    Actions.sequence(
                        Actions.moveTo(xOriginal - 12f, yOriginal, 0.025f),
                        Actions.moveTo(xOriginal + 12f, yOriginal, 0.04f),
                        Actions.moveTo(xOriginal - 10f, yOriginal, 0.035f),
                        Actions.moveTo(xOriginal + 8f, yOriginal, 0.03f),
                        Actions.moveTo(xOriginal - 4f, yOriginal, 0.02f),
                        Actions.moveTo(xOriginal, yOriginal, 0.02f)
                    )
                ),
                Actions.color(corOriginal, 0.14f),
                Actions.run(() -> {
                    carta.setPosition(xOriginal, yOriginal);
                    carta.setColor(corOriginal);
                })
            )
        );
    }

    public void resetarEstadoVisualDasCartas() {
        for (int i = 0; i < Tabuleiro.LINHAS; i++) {
            for (int j = 0; j < Tabuleiro.COLUNAS; j++) {
                Cartas carta = cartasVisuais[i][j];
                if (carta == null) continue;

                carta.clearActions();
                carta.setVisible(true);
                carta.setScale(1f, 1f);
                carta.setRotation(0f);
                carta.setColor(1f, 1f, 1f, 1f);
                carta.getColor().a = 1f;
            }
        }
    }

    private float getCartaX(int coluna) {
        return posicaoProvider.getCartaX(coluna);
    }

    private float getCartaY(int linha) {
        return posicaoProvider.getCartaY(linha);
    }
}

package com.root.game.Animacoes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.math.Interpolation;
import com.root.game.CorpoPrincipal.Tabuleiro;
import com.root.game.Utils.PosicaoCartaProvider;
import com.root.game.CorpoPrincipal.Cartas;

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

    //Anima o movimento do jogador com a esteira.
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

        final float levantarDuracao = 0.12f;
        final float esteiraDuracao = 0.30f;
        final float moverJogadorDuracao = 0.24f;

        final float origemX = getCartaX(antigaColuna);
        final float origemY = getCartaY(antigaLinha);

        final float destinoX = getCartaX(novaColuna);
        final float destinoY = getCartaY(novaLinha);

        jogador.clearActions();
        jogador.setOrigin(Align.center);
        jogador.setVisible(true);
        jogador.setPosition(origemX, origemY);
        jogador.setScale(1f, 1f);
        jogador.getColor().a = 1f;
        jogador.toFront();

        jogador.addAction(
            Actions.sequence(


                //1) O jogador se destaca da carta atual.
                Actions.parallel(
                    Actions.moveTo(origemX, origemY + 18f, levantarDuracao, Interpolation.sineOut),
                    Actions.scaleTo(1.07f, 1.12f, levantarDuracao, Interpolation.sineOut)
                ),

                //2) A esteira começa enquanto o jogador está destacado.
                Actions.run(() -> {
                    animarDeslizamentoEsteira(
                        antigaLinha,
                        antigaColuna,
                        novaLinha,
                        novaColuna
                    );

                    jogador.toFront();
                }),

                //3) Aguarda a maior parte da esteira.
                Actions.delay(esteiraDuracao * 0.82f),

                //4) Movimento contínuo até o destino.
                Actions.parallel(
                    Actions.moveTo(destinoX, destinoY, moverJogadorDuracao, Interpolation.sine),
                    Actions.scaleTo(1f, 1f, moverJogadorDuracao, Interpolation.sine)
                ),

                //5) Libera atualização lógica.
                Actions.run(() -> {
                    if (aoFinalizar != null) {
                        aoFinalizar.run();
                    }
                })
            )
        );
    }

    //Desliza a linha ou coluna afetada pela esteira,
    //também dispara a carta temporária de entrada com o movimento para não ter atraso visual.
    private void animarDeslizamentoEsteira(int antigaLinha, int antigaColuna, int novaLinha, int novaColuna) {
        int dx = novaColuna - antigaColuna;
        int dy = novaLinha - antigaLinha;

        animarEntradaTemporariaEsteira(
            antigaLinha,
            antigaColuna,
            novaLinha,
            novaColuna
        );

        if (dx != 0) {
            animarLinhaEsteira(antigaLinha, antigaColuna, dx);
        } else if (dy != 0) {
            animarColunaEsteira(antigaColuna, antigaLinha, dy);
        }
    }

    //Anima a linha horizontal da esteira.
    //dx = 1 → jogador foi para direita, cartas vão para esquerda.
    //dx = -1 → jogador foi para esquerda, cartas vão para direita.
    private void animarLinhaEsteira(int linha, int colunaJogador, int dx) {
        float deslocamento = cartaLargura + espaco;
        float moveX = -dx * deslocamento;

        int colunaSaida = dx == 1 ? 0 : Tabuleiro.COLUNAS - 1;

        for (int coluna = 0; coluna < Tabuleiro.COLUNAS; coluna++) {
            Cartas carta = cartasVisuais[linha][coluna];

            if (carta == null) continue;
            if (coluna == colunaJogador) continue;

            float atraso = calcularAtrasoSuave(coluna, colunaJogador);

            if (coluna == colunaSaida) {
                animarCartaSaindoEsteira(carta, moveX, 0f, atraso);
            } else {
                animarCartaDeslizandoNaEsteira(carta, moveX, 0f, atraso);
            }
        }

        //Caso o jogador esteja exatamente na borda de saída, nenhuma carta real seria animada saindo, porque o jogador é ignorado.
        //Criamos uma saída temporária discreta para manter o efeito visual.
        if (colunaSaida == colunaJogador) {
            int colunaReferencia = dx == 1 ? 1 : Tabuleiro.COLUNAS - 2;

            Cartas referencia = cartasVisuais[linha][colunaReferencia];

            if (referencia != null) {
                float saidaX = getCartaX(colunaSaida);
                float saidaY = getCartaY(linha);

                animarSaidaTemporariaEsteira(
                    referencia,
                    saidaX,
                    saidaY,
                    moveX,
                    0f
                );
            }
        }
    }

    //Anima a coluna vertical da esteira.
    //dy = 1  -> jogador foi para baixo, cartas sobem.
    //dy = -1 -> jogador foi para cima, cartas descem.
    //O sinal aqui é diferente do horizontal porque no libGDX o eixo Y cresce para cima.
    private void animarColunaEsteira(int coluna, int linhaJogador, int dy) {
        float deslocamento = cartaAltura + espaco;
        float moveY = dy * deslocamento;

        int linhaSaida = dy == 1 ? 0 : Tabuleiro.LINHAS - 1;

        for (int linha = 0; linha < Tabuleiro.LINHAS; linha++) {
            Cartas carta = cartasVisuais[linha][coluna];

            if (carta == null) continue;
            if (linha == linhaJogador) continue;

            float atraso = calcularAtrasoSuave(linha, linhaJogador);

            if (linha == linhaSaida) {
                animarCartaSaindoEsteira(carta, 0f, moveY, atraso);
            } else {
                animarCartaDeslizandoNaEsteira(carta, 0f, moveY, atraso);
            }
        }

        //Se o jogador está na borda de saída, criamos saída temporária para não perder o efeito visual da carta caindo.
        if (linhaSaida == linhaJogador) {
            int linhaReferencia = dy == 1 ? 1 : Tabuleiro.LINHAS - 2;

            Cartas referencia = cartasVisuais[linhaReferencia][coluna];

            if (referencia != null) {
                float saidaX = getCartaX(coluna);
                float saidaY = getCartaY(linhaSaida);

                animarSaidaTemporariaEsteira(
                    referencia,
                    saidaX,
                    saidaY,
                    0f,
                    moveY
                );
            }
        }
    }

    //Anima uma carta comum deslizando com a esteira, usada para cartas internas, que não entram nem saem do tabuleiro.
    private void animarCartaDeslizandoNaEsteira(Cartas carta, float moveX, float moveY, float delay) {
        if (carta == null) return;

        carta.clearActions();
        carta.setOrigin(Align.center);

        //Garante continuidade visual.
        //Algumas cartas podem vir de animações anteriores com alpha baixo ou invisíveis.
        carta.setVisible(true);
        carta.getColor().a = 1f;
        carta.setScale(1f, 1f);

        final float inicioX = carta.getX();
        final float inicioY = carta.getY();

        carta.addAction(
            Actions.sequence(
                Actions.delay(delay),
                Actions.parallel(
                    Actions.moveTo(inicioX + moveX, inicioY + moveY, (float) 0.3, Interpolation.sine),
                    Actions.sequence(
                        Actions.scaleTo(1.025f, 0.985f, (float) 0.3 * 0.38f, Interpolation.sineOut),
                        Actions.scaleTo(1f, 1f, (float) 0.3 * 0.62f, Interpolation.sineIn)
                    )
                )
            )
        );
    }

    //Anima a carta que está saindo pela borda da esteira.
    //Ela desliza na mesma direção do tabuleiro, mas perde opacidade e reduz levemente a escala para parecer que está sendo removida.
    private void animarCartaSaindoEsteira(Cartas carta, float moveX, float moveY, float delay) {
        if (carta == null) return;

        carta.clearActions();
        carta.setOrigin(Align.center);

        final float inicioX = carta.getX();
        final float inicioY = carta.getY();

        carta.addAction(
            Actions.sequence(
                Actions.delay(delay),

                Actions.parallel(
                    Actions.moveTo(
                        inicioX + moveX,
                        inicioY + moveY,
                        (float) 0.3,
                        Interpolation.sineIn
                    ),
                    Actions.fadeOut((float) 0.3 * 0.85f, Interpolation.fade),
                    Actions.scaleTo(0.92f, 0.92f, (float) 0.3, Interpolation.sineIn)
                ),

                Actions.run(() -> {

                    //Não removemos a carta real permanentemente.
                    //Ela será reposicionada e restaurada por reset/sincronização após o callback lógico do tabuleiro.
                    carta.setVisible(false);
                })
            )
        );
    }

    //Calcula um atraso pequeno para criar sensação de onda.
    //O atraso é intencionalmente baixo para não parecer travado.
    private float calcularAtrasoSuave(int indiceCarta, int indiceJogador) {
        return Math.abs(indiceCarta - indiceJogador) * 0.008f;
    }

    //Anima o movimento da carta não adjacente e não revelada quando clicada
    public void animarCartaMovimentoInvalido(int linha, int coluna) {
        Cartas carta = cartasVisuais[linha][coluna];
        if (carta == null) return;

        carta.setBloqueandoAnimacaoClique(true);
        carta.toFront();

        final float xOriginal = carta.getX();
        final float yOriginal = carta.getY();
        final float scaleXOriginal = carta.getScaleX();
        final float scaleYOriginal = carta.getScaleY();
        final Color corOriginal = new Color(carta.getColor());

        carta.clearActions();

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
                    carta.setScale(scaleXOriginal, scaleYOriginal);
                    carta.setColor(corOriginal);
                    carta.setBloqueandoAnimacaoClique(false);
                })
            )
        );
    }

    private float getCartaX(int coluna) {
        return posicaoProvider.getCartaX(coluna);
    }

    private float getCartaY(int linha) {
        return posicaoProvider.getCartaY(linha);
    }

    //Cria uma carta temporária saindo pela borda.
    //Usada quando o jogador está na borda de saída e, por isso, não há uma carta real para animar saindo.
    private void animarSaidaTemporariaEsteira(Cartas cartaReferencia, float inicioX, float inicioY, float moveX, float moveY) {
        if (cartaReferencia == null) return;

        Image cartaTemp = new Image(cartaReferencia.getTexturaAtual());

        cartaTemp.setSize(cartaReferencia.getWidth(), cartaReferencia.getHeight());
        cartaTemp.setOrigin(Align.center);
        cartaTemp.setPosition(inicioX, inicioY);
        cartaTemp.setScale(1f, 1f);
        cartaTemp.getColor().a = 0.75f;

        stageTabuleiro.addActor(cartaTemp);

        //Garante que a carta temporária fique atrás das cartas reais.
        //Isso evita ela passar por cima do jogador durante a esteira.
        cartaTemp.toBack();

        cartaTemp.addAction(
            Actions.sequence(
                Actions.parallel(
                    Actions.moveTo(
                        inicioX + moveX,
                        inicioY + moveY,
                        (float) 0.3,
                        Interpolation.sineIn
                    ),
                    Actions.fadeOut((float) 0.3 * 0.85f, Interpolation.fade),
                    Actions.scaleTo(0.92f, 0.92f, (float) 0.3, Interpolation.sineIn)
                ),
                Actions.removeActor()
            )
        );
    }

    //Cria uma carta temporária entrando pela borda da esteira. Esta carta é apenas visual.
    //A carta real será sincronizada depois pelo grid.
    private void animarEntradaTemporariaEsteira(int antigaLinha, int antigaColuna, int novaLinha, int novaColuna) {
        int dx = novaColuna - antigaColuna;
        int dy = novaLinha - antigaLinha;

        int linhaEntrada;
        int colunaEntrada;

        float origemX;
        float origemY;
        float destinoX;
        float destinoY;

        float passoX = cartaLargura + espaco;
        float passoY = cartaAltura + espaco;

        if (dx == 1) {
            linhaEntrada = antigaLinha;
            colunaEntrada = Tabuleiro.COLUNAS - 1;

            destinoX = getCartaX(colunaEntrada);
            destinoY = getCartaY(linhaEntrada);

            origemX = destinoX + passoX;
            origemY = destinoY;

        } else if (dx == -1) {
            linhaEntrada = antigaLinha;
            colunaEntrada = 0;

            destinoX = getCartaX(colunaEntrada);
            destinoY = getCartaY(linhaEntrada);

            origemX = destinoX - passoX;
            origemY = destinoY;

        } else if (dy == 1) {
            linhaEntrada = Tabuleiro.LINHAS - 1;
            colunaEntrada = antigaColuna;

            destinoX = getCartaX(colunaEntrada);
            destinoY = getCartaY(linhaEntrada);

            origemX = destinoX;
            origemY = destinoY - passoY;

        } else if (dy == -1) {
            linhaEntrada = 0;
            colunaEntrada = antigaColuna;

            destinoX = getCartaX(colunaEntrada);
            destinoY = getCartaY(linhaEntrada);

            origemX = destinoX;
            origemY = destinoY + passoY;

        } else {
            return;
        }

        Cartas cartaReferencia = cartasVisuais[linhaEntrada][colunaEntrada];
        if (cartaReferencia == null) return;

        //A carta temporária de entrada representa uma carta nova ainda desconhecida visualmente.
        //Por isso usamos o verso neutro, e não a textura da carta usada como referência.
        //Isso evita que ela herde aparência de carta revelada, destaque ou brilho.
        Image cartaTemp = new Image(cartaReferencia.getTexturaVerso());

        cartaTemp.setSize(cartaReferencia.getWidth(), cartaReferencia.getHeight());
        cartaTemp.setOrigin(Align.center);
        cartaTemp.setPosition(origemX, origemY);
        cartaTemp.setScale(0.96f, 0.96f);

        //A carta temporária de entrada deve ser visualmente neutra.
        //Ela não pode herdar sensação de destaque da carta usada como referência.
        cartaTemp.setColor(1f, 1f, 1f, 0f);

        stageTabuleiro.addActor(cartaTemp);

        //Fica atrás do jogador, mas visível acima do fundo.
        //Depois garantimos que o jogador volte à frente.
        cartaTemp.toFront();

        //Garante que a carta temporária fique atrás das cartas reais
        //Isso evita ela passar por cima do jogador durante a esteira.
        cartaTemp.toBack();

        //A carta temporária entra com a esteira e permanece por alguns instantes na posição final.
        //Esse pequeno "segurar" evita o buraco visual entre: fim da animação temporária, sincronização da carta real no grid.
        cartaTemp.addAction(
            Actions.sequence(
                Actions.parallel(
                    Actions.moveTo(destinoX, destinoY, (float) 0.3, Interpolation.sine),
                    Actions.alpha(1f, (float) 0.3 * 0.75f, Interpolation.fade),
                    Actions.scaleTo(1f, 1f, (float) 0.3, Interpolation.sine)
                ),

                //Mantém a carta visível até a sincronização visual terminar.
                //Esse delay remove a piscada.
                Actions.delay(0.22f),

                Actions.removeActor()
            )
        );
    }

}

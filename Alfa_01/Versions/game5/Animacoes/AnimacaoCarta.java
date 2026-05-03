package com.root.game.Animacoes;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Timer;

//Classe responsável por animações visuais de cartas individuais.

//RESPONSABILIDADES:
//flip de carta, flutuação/idle, dissolução de carta em zoom, impacto visual do jogador no combate, quebra visual da arma
//derrota visual do inimigo

public class AnimacaoCarta {

    //Executa a animação de flip horizontal:
    //fecha quase totalmente no eixo X, troca o conteúdo no meio e reabre em seguida.
    //{carta} imagem da carta
    //{aposFlip} callback chamado no exato momento da troca visual
    public void aplicarFlip(Image carta, Runnable aposFlip) {
        carta.setOrigin(Align.center);

        carta.addAction(
            Actions.sequence(
                Actions.scaleTo(0.05f, 1f, 0.12f, Interpolation.fade),
                Actions.run(aposFlip),
                Actions.scaleTo(1f, 1f, 0.12f, Interpolation.fade)
            )
        );
    }

    //Aplica uma animação contínua de flutuação vertical.
    //{carta} imagem que ficará flutuando
    public void aplicarIdleFlutuacao(Image carta) {
        carta.addAction(
            Actions.forever(
                Actions.sequence(
                    Actions.moveBy(0, 8, 1.2f, Interpolation.sine),
                    Actions.moveBy(0, -8, 1.2f, Interpolation.sine)
                )
            )
        );
    }

    //Dissolve suavemente a carta de zoom: reduz escala, sobe levemente e perde opacidade
    //{cartaZoom} imagem da carta em destaque
    //{aoFinalizar} callback executado ao final da animação
    public void dissolverCartaZoom(Image cartaZoom, Runnable aoFinalizar) {
        cartaZoom.clearActions();

        cartaZoom.addAction(
            Actions.sequence(
                Actions.parallel(
                    Actions.fadeOut(0.13f),
                    Actions.scaleTo(0.80f, 0.80f, 0.22f, Interpolation.fade),
                    Actions.moveBy(0f, 15f, 0.22f, Interpolation.fade)
                ),
                Actions.run(() -> {
                    if (aoFinalizar != null) {
                        aoFinalizar.run();
                    }
                })
            )
        );
    }

    //Executa um pequeno avanço/impacto visual do jogador para ataque no combate.
    public void animarImpactoJogador(Image cartaJogador) {
        cartaJogador.addAction(
            Actions.sequence(
                Actions.parallel(
                    Actions.moveBy(-20f, 0f, 0.08f, Interpolation.fade),
                    Actions.scaleTo(1.05f, 1.05f, 0.08f, Interpolation.fade)
                ),
                Actions.parallel(
                    Actions.moveBy(20f, 0f, 0.08f, Interpolation.fade),
                    Actions.scaleTo(1f, 1f, 0.08f, Interpolation.fade)
                )
            )
        );
    }

    //Anima a quebra da miniatura da arma.
    //Usa a mesma linguagem visual da quebra do inimigo:
    //rachaduras, fragmentos, colapso;
    public void animarQuebraArma(Image miniArma, Stage stage, Runnable aoFinalizar) {
        if (miniArma == null) {
            if (aoFinalizar != null) aoFinalizar.run();
            return;
        }

        miniArma.clearActions();
        miniArma.setOrigin(Align.center);
        miniArma.toFront();

        criarRachadurasSobreCarta(stage, miniArma);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                criarFragmentosQuebra(stage, miniArma, 8);
            }
        }, 0.18f);

        miniArma.addAction(
            Actions.sequence(
                /*
                 * Pausa curta para as rachaduras aparecerem.
                 */
                Actions.delay(0.12f),

                /*
                 * Colapso sem tremulação.
                 */
                Actions.parallel(
                    Actions.fadeOut(0.48f, Interpolation.fade),
                    Actions.scaleTo(0.45f, 0.45f, 0.48f, Interpolation.pow2In),
                    Actions.moveBy(0f, -22f, 0.48f, Interpolation.pow2In)
                ),

                Actions.run(() -> {
                    miniArma.remove();

                    if (aoFinalizar != null) {
                        aoFinalizar.run();
                    }
                })
            )
        );
    }

    //Anima a derrota do inimigo com sensação de carta quebrando.
    //Estratégia visual:
    //cria rachaduras temporárias sobre a carta, aplica impacto/tremor, espalha fragmentos, colapsa e dissolve a carta.
    public void animarDerrotaInimigo(Image cartaInimigo, Stage stage, Runnable aoFinalizar) {
        if (cartaInimigo == null) {
            if (aoFinalizar != null) aoFinalizar.run();
            return;
        }

        cartaInimigo.clearActions();
        cartaInimigo.setOrigin(Align.center);
        cartaInimigo.toFront(); //As rachaduras aparecem primeiro sobre a carta.
        criarRachadurasSobreCarta(stage, cartaInimigo);

        //Os fragmentos aparecem um pouco depois do impacto dando a sensação de quebra.
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                criarFragmentosQuebra(stage, cartaInimigo, 16);
            }
        }, 0.24f);

        cartaInimigo.addAction(
            Actions.sequence(

                // Impacto forte inicial
                // Impacto mais controlado, com menos tremulação.
                Actions.parallel(
                    Actions.sequence(
                        Actions.moveBy(-7f, 0f, 0.04f, Interpolation.fade),
                        Actions.moveBy(14f, 0f, 0.05f, Interpolation.fade),
                        Actions.moveBy(-7f, 0f, 0.04f, Interpolation.fade)
                    ),
                    Actions.sequence(
                        Actions.scaleTo(1.04f, 0.98f, 0.08f, Interpolation.fade),
                        Actions.scaleTo(1f, 1f, 0.08f, Interpolation.fade)
                    )
                ),

                // Pausa curta para “mostrar” as rachaduras
                Actions.delay(0.10f),

                //A carta recua/desce um pouco mais, mas sem girar, para evitar a sensação de distorção diagonal.
                Actions.parallel(
                    Actions.fadeOut(0.62f, Interpolation.fade),
                    Actions.scaleTo(0.58f, 0.58f, 0.62f, Interpolation.pow2In),
                    Actions.moveBy(0f, -46f, 0.62f, Interpolation.pow2In)
                ),

                Actions.run(() -> {
                    if (aoFinalizar != null) {
                        aoFinalizar.run();
                    }
                })
            )
        );
    }

    //Reseta transformações visuais básicas de uma imagem. Útil antes de aplicar outra animação.
    public void resetarTransformacoes(Image imagem) {
        if (imagem == null) return;

        imagem.clearActions();
        imagem.setScale(1f, 1f);
        imagem.setRotation(0f);
        imagem.getColor().a = 1f;
    }

    //Anima a saída da tela de combate após a resolução.
    //Usado quando o combate terminou e a tela será fechada.
    //A ideia é evitar corte seco entre: carta do inimigo derrotada, fechamento da tela de combate, retorno para o tabuleiro.
    //Cria fragmentos visuais simples para simular quebra, os fragmentos são pequenas imagens escuras criadas por Pixmap.
    //São temporários e removidos automaticamente após a animação.
    private void criarFragmentosQuebra(Stage stage, Image origem, int quantidade) {
        if (stage == null || origem == null) return;

        for (int i = 0; i < quantidade; i++) {
            Image fragmento = criarFragmentoVisual();

            float centroX = origem.getX() + origem.getWidth() / 2f;
            float centroY = origem.getY() + origem.getHeight() / 2f;

            float offsetX = (float) (Math.random() * origem.getWidth() - origem.getWidth() / 2f);
            float offsetY = (float) (Math.random() * origem.getHeight() - origem.getHeight() / 2f);

            fragmento.setPosition(centroX + offsetX, centroY + offsetY);

            float tamanho = 5f + (float) Math.random() * 10f;
            fragmento.setSize(tamanho, tamanho * (0.6f + (float) Math.random()));

            fragmento.setOrigin(Align.center);
            fragmento.setRotation((float) Math.random() * 180f);
            fragmento.getColor().a = 0.85f;

            stage.addActor(fragmento);
            fragmento.toFront();

            float direcaoX = -50f + (float) Math.random() * 100f;
            float direcaoY = -35f + (float) Math.random() * 90f;

            fragmento.addAction(
                Actions.sequence(
                    Actions.parallel(
                        Actions.moveBy(direcaoX, direcaoY, 0.45f, Interpolation.pow2Out),
                        Actions.rotateBy(180f + (float) Math.random() * 180f, 0.45f, Interpolation.fade),
                        Actions.fadeOut(0.45f, Interpolation.fade),
                        Actions.scaleTo(0.2f, 0.2f, 0.45f, Interpolation.fade)
                    ),
                    Actions.removeActor()
                )
            );
        }
    }

    //Cria um fragmento visual escuro, como é usado em poucas quantidades e removido rapidamente,
    //funciona bem para simular estilhaços sem asset extra.
    private Image criarFragmentoVisual() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.08f, 0.08f, 0.08f, 1f));
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return new Image(new TextureRegionDrawable(new TextureRegion(texture)));
    }

    //Cria rachaduras espalhadas por quase toda a carta.
    private void criarRachadurasSobreCarta(Stage stage, Image carta) {
        if (stage == null || carta == null) return;

        float x = carta.getX();
        float y = carta.getY();
        float w = carta.getWidth();
        float h = carta.getHeight();

        // Rachaduras principais
        criarLinhaRachadura(stage, x + w * 0.50f, y + h * 0.06f, 3.4f, h * 0.88f, -5f);
        criarLinhaRachadura(stage, x + w * 0.37f, y + h * 0.14f, 2.2f, h * 0.64f, 12f);
        criarLinhaRachadura(stage, x + w * 0.63f, y + h * 0.20f, 2.2f, h * 0.58f, -14f);

        // Ramificações superiores
        criarLinhaRachadura(stage, x + w * 0.50f, y + h * 0.74f, 2.3f, h * 0.24f, 46f);
        criarLinhaRachadura(stage, x + w * 0.45f, y + h * 0.78f, 2.1f, h * 0.20f, -52f);
        criarLinhaRachadura(stage, x + w * 0.61f, y + h * 0.70f, 2.0f, h * 0.18f, 34f);

        // Ramificações centrais
        criarLinhaRachadura(stage, x + w * 0.52f, y + h * 0.54f, 2.3f, h * 0.28f, 58f);
        criarLinhaRachadura(stage, x + w * 0.46f, y + h * 0.48f, 2.2f, h * 0.26f, -55f);
        criarLinhaRachadura(stage, x + w * 0.36f, y + h * 0.45f, 2.0f, h * 0.22f, -34f);
        criarLinhaRachadura(stage, x + w * 0.65f, y + h * 0.42f, 2.0f, h * 0.22f, 38f);

        // Ramificações inferiores
        criarLinhaRachadura(stage, x + w * 0.50f, y + h * 0.30f, 2.2f, h * 0.28f, 36f);
        criarLinhaRachadura(stage, x + w * 0.44f, y + h * 0.24f, 2.0f, h * 0.24f, -42f);
        criarLinhaRachadura(stage, x + w * 0.60f, y + h * 0.20f, 2.0f, h * 0.20f, 50f);
    }

    //Cria uma linha escura fina usada como rachadura.
    private void criarLinhaRachadura(Stage stage, float x, float y, float largura, float altura, float rotacao) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0f, 0f, 0f, 0.95f));
        pixmap.fill();

        Texture textura = new Texture(pixmap);
        pixmap.dispose();

        Image linha = new Image(new TextureRegionDrawable(new TextureRegion(textura)));
        linha.setSize(largura, altura);
        linha.setOrigin(Align.center);
        linha.setPosition(x, y);
        linha.setRotation(rotacao);
        linha.getColor().a = 0f;

        stage.addActor(linha);
        linha.toFront();

        linha.addAction(
            Actions.sequence(
                Actions.fadeIn(0.08f, Interpolation.fade),
                Actions.delay(0.38f),
                Actions.fadeOut(0.20f, Interpolation.fade),
                Actions.removeActor()
            )
        );
    }
}

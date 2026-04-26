package com.root.game.Animacoes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;

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

    //Executa a animação visual de quebra/desaparecimento da arma
    //{aoFinalizar} callback opcional ao final
    public void animarQuebraArma(Image miniArma, Runnable aoFinalizar) {
        if (miniArma == null) {
            if (aoFinalizar != null) aoFinalizar.run();
            return;
        }

        miniArma.addAction(
            Actions.sequence(
                Actions.parallel(
                    Actions.fadeOut(0.35f),
                    Actions.scaleTo(0.4f, 0.4f, 0.35f, Interpolation.fade),
                    Actions.rotateBy(-120f, 0.35f, Interpolation.fade)
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

    //Executa a animação completa de derrota do inimigo:
    public void animarDerrotaInimigo(Image cartaInimigo, Runnable aoFinalizar) {
        cartaInimigo.addAction(
            Actions.sequence(

                // impacto inicial
                Actions.parallel(
                    Actions.sequence(
                        Actions.moveBy(-10f, 0f, 0.04f, Interpolation.fade),
                        Actions.moveBy(20f, 0f, 0.06f, Interpolation.fade),
                        Actions.moveBy(-16f, 0f, 0.05f, Interpolation.fade),
                        Actions.moveBy(10f, 0f, 0.04f, Interpolation.fade),
                        Actions.moveBy(-4f, 0f, 0.03f, Interpolation.fade)
                    ),
                    Actions.sequence(
                        Actions.rotateBy(-4f, 0.05f, Interpolation.fade),
                        Actions.rotateBy(8f, 0.06f, Interpolation.fade),
                        Actions.rotateBy(-7f, 0.05f, Interpolation.fade),
                        Actions.rotateBy(3f, 0.04f, Interpolation.fade)
                    ),
                    Actions.sequence(
                        Actions.color(new Color(1f, 1f, 1f, 1f), 0.08f),
                        Actions.color(new Color(0.82f, 0.82f, 0.82f, 1f), 0.10f)
                    )
                ),

                // colapso
                Actions.parallel(
                    Actions.moveBy(0f, 22f, 0.22f, Interpolation.sineOut),
                    Actions.scaleTo(1.08f, 0.92f, 0.22f, Interpolation.fade),
                    Actions.rotateBy(-8f, 0.22f, Interpolation.fade),
                    Actions.color(new Color(0.92f, 0.92f, 0.92f, 1f), 0.22f)
                ),

                // desaparecimento
                Actions.parallel(
                    Actions.fadeOut(0.42f),
                    Actions.scaleTo(0.18f, 1.18f, 0.42f, Interpolation.pow2In),
                    Actions.rotateBy(-10f, 0.42f, Interpolation.pow2In),
                    Actions.moveBy(0f, -18f, 0.42f, Interpolation.pow2In),
                    Actions.color(new Color(1f, 1f, 1f, 0f), 0.42f)
                ),

                Actions.delay(0.10f),

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
}

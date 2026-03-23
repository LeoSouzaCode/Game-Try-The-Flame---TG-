package com.root.game.ClassesBase;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;
import com.root.game.CorpoPrincipal.TCC_0_01;
import com.root.game.Utils.TextureManager;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.math.Interpolation;

public class Cartas extends Actor {

    private Texture frente;
    private Texture verso;
    private Texture glow;

    private boolean revelada = false;
    private boolean hover = false;
    private final TCC_0_01 jogo;

    public static final float LARGURA = 108;
    public static final float ALTURA = 144;

    public Cartas(String frentePath, String versoPath,
                  float x, float y,
                  int linha, int coluna,
                  TCC_0_01 jogo) {

        this.frente = TextureManager.get(frentePath);
        this.verso = TextureManager.get(versoPath);
        this.glow = TextureManager.get("Cartas/Efeitos/glow.png");
        this.jogo = jogo;


        setBounds(x, y, LARGURA, ALTURA);
        setOrigin(Align.center);

        addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (jogo.isFinalizado() || jogo.isAnimandoTabuleiro()) return;

                hover = true;

                addAction(Actions.scaleTo(1.1f, 1.1f, 0.12f, Interpolation.fade));
                toFront();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (jogo.isAnimandoTabuleiro()) return;

                hover = false;

                addAction(Actions.scaleTo(1f, 1f, 0.12f, Interpolation.fade));
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {

                jogo.clicarCarta(linha, coluna);

            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Texture tex = revelada ? frente : verso;
        if (tex == null) return;

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        batch.draw(
            tex,
            getX(), getY(),
            getOriginX(), getOriginY(),
            getWidth(), getHeight(),
            getScaleX(), getScaleY(),
            getRotation(),
            0, 0,
            tex.getWidth(), tex.getHeight(),
            false, false
        );

        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void setRevelada(boolean estado) {
        revelada = estado;
    }

    public void setTexturaFrente(Texture textura) {
        this.frente = textura;
    }

    public Texture getTexturaAtual() {
        return revelada ? frente : verso;
    }

}

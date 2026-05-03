package com.root.game.UI;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.root.game.Combate.Jogador;
import com.root.game.Utils.TextureManager;

//Controla o HUD do jogo, texto de chamas, vida do jogador, arma equipada, miniatura da arma clicável

public class HUDController {

    private final Stage stageUI;
    private final Skin skin;

    private Label labelHUD;
    private Image imagemArmaHUD;

    public HUDController(Stage stageUI, Skin skin) {
        this.stageUI = stageUI;
        this.skin = skin;
    }

    //criação do HUD
    public void criarHUD() {

        labelHUD = new Label("", skin);
        labelHUD.setPosition(20, stageUI.getViewport().getWorldHeight() - 25);

        stageUI.addActor(labelHUD);
    }

    //Atualiza o HUD
    public void atualizarHUD(Jogador jogador, int chamasColetadas) {

        if (labelHUD == null) return;

        String textoArma = "Sem arma";

        if (jogador != null && jogador.getArmaEquipada() != null) {
            textoArma = jogador.getArmaEquipada().getNome()
                + " (" + jogador.getArmaEquipada().getDurabilidade() + ")";
        }

        if (jogador != null) {
            labelHUD.setText(
                "Chamas: " + chamasColetadas + " / 3"
                    + "    Vida: " + jogador.getVida()
                    + "    Arma: " + textoArma
            );
        }

        atualizarMiniaturaArma(jogador);
    }

    //Miniatura da arma
    private void atualizarMiniaturaArma(Jogador jogador) {

        if (imagemArmaHUD != null) {
            imagemArmaHUD.remove();
            imagemArmaHUD = null;
        }

        if (jogador == null || jogador.getArmaEquipada() == null) {
            return;
        }

        String textura = jogador.getArmaEquipada().getTexturaPath();

        imagemArmaHUD = new Image(
            new TextureRegionDrawable(
                new TextureRegion(TextureManager.get(textura))
            )
        );

        imagemArmaHUD.setSize(64, 86);
        imagemArmaHUD.setPosition(
            stageUI.getViewport().getWorldWidth() - 90,
            stageUI.getViewport().getWorldHeight() - 110
        );

        stageUI.addActor(imagemArmaHUD);
    }

    //Evento de click na arma
    public void setClickArmaListener(Runnable aoClicar) {

        if (imagemArmaHUD == null) return;

        imagemArmaHUD.clearListeners();

        imagemArmaHUD.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                aoClicar.run();
            }
        });
    }

}

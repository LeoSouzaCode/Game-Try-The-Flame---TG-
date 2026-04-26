package com.root.game.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.root.game.Combate.CartaInfo;

/**
 * Gerencia os popups e overlays do jogo.
 */
public class PopupManager {

    private final Stage stageUI;
    private final Stage stageCartaZoom;
    private final Skin skin;

    public PopupManager(Stage stageUI, Stage stageCartaZoom, Skin skin) {
        this.stageUI = stageUI;
        this.stageCartaZoom = stageCartaZoom;
        this.skin = skin;
    }

    public void mostrarConfirmacaoCarta(Runnable confirmar, Runnable cancelar) {
        if (stageUI.getRoot().findActor("popupConfirmacao") != null) return;

        Window popup = new Window("", skin);
        popup.setName("popupConfirmacao");

        Label texto = new Label("Revelar esta carta?", skin);
        texto.setAlignment(Align.center);

        TextButton btnConfirmar = new TextButton("Confirmar", skin);
        TextButton btnCancelar = new TextButton("Cancelar", skin);

        btnConfirmar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popup.remove();
                confirmar.run();
            }
        });

        btnCancelar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popup.remove();
                if (cancelar != null) cancelar.run();
            }
        });

        popup.add(texto).colspan(2).pad(20);
        popup.row();
        popup.add(btnConfirmar).width(140).pad(10);
        popup.add(btnCancelar).width(140).pad(10);

        popup.pack();
        centralizar(stageUI, popup);
        stageUI.addActor(popup);
    }

    public void mostrarPopupMensagem(String mensagem, Runnable confirmar) {
        Window popup = new Window("", skin);

        Label texto = new Label(mensagem, skin);
        texto.setAlignment(Align.center);

        TextButton btn = new TextButton("Confirmar", skin);

        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popup.remove();
                confirmar.run();
            }
        });

        popup.add(texto).pad(20);
        popup.row();
        popup.add(btn).width(180).pad(15);

        popup.pack();
        centralizarZoom(popup);
        stageCartaZoom.addActor(popup);
    }

    public void mostrarPopupArmaBau(
        CartaInfo cartaInfo,
        boolean jogadorTemArma,
        Runnable aoEquipar,
        Runnable aoNaoEquipar
    ) {
        String nome = "Item desconhecido";
        int durabilidade = 0;

        if (cartaInfo != null && cartaInfo.getArmaDentro() != null) {
            nome = cartaInfo.getArmaDentro().getNome();
            durabilidade = cartaInfo.getArmaDentro().getDurabilidade();
        }

        String texto;
        String btnPrincipal;
        String btnSecundario;

        if (jogadorTemArma) {
            texto = "Você encontrou uma arma:\n" + nome +
                "\nDurabilidade: " + durabilidade +
                "\n\nDeseja trocar sua arma?";
            btnPrincipal = "Trocar";
            btnSecundario = "Manter";
        } else {
            texto = "Você encontrou uma arma:\n" + nome +
                "\nDurabilidade: " + durabilidade +
                "\n\nDeseja equipar?";
            btnPrincipal = "Equipar";
            btnSecundario = "Ignorar";
        }

        Window popup = new Window("", skin);
        popup.setName("popupArmaBau");

        Label label = new Label(texto, skin);
        label.setAlignment(Align.center);

        TextButton btn1 = new TextButton(btnPrincipal, skin);
        TextButton btn2 = new TextButton(btnSecundario, skin);

        btn1.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popup.remove();
                aoEquipar.run();
            }
        });

        btn2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popup.remove();
                aoNaoEquipar.run();
            }
        });

        popup.add(label).colspan(2).pad(20);
        popup.row();
        popup.add(btn1).width(180).pad(10);
        popup.add(btn2).width(180).pad(10);

        popup.pack();
        centralizarZoom(popup);
        stageCartaZoom.addActor(popup);
    }

    public void mostrarGameOver() {
        stageCartaZoom.clear();

        Image overlay = criarOverlayBloqueador(0.85f);
        stageCartaZoom.addActor(overlay);

        Label titulo = new Label("GAME OVER", skin);
        titulo.setFontScale(2.2f);
        titulo.setAlignment(Align.center);
        titulo.setWidth(500);

        Label sub = new Label(
            "Você foi derrotado.\nO menu de runs será ligado depois.",
            skin
        );
        sub.setAlignment(Align.center);
        sub.setWidth(500);

        float centroX = stageCartaZoom.getViewport().getWorldWidth() / 2f;
        float centroY = stageCartaZoom.getViewport().getWorldHeight() / 2f;

        titulo.setPosition(
            centroX - titulo.getWidth() / 2f,
            centroY + 40
        );

        sub.setPosition(
            centroX - sub.getWidth() / 2f,
            centroY - 40
        );

        stageCartaZoom.addActor(titulo);
        stageCartaZoom.addActor(sub);
    }

    //Cria um overlay bloqueador com alpha configurável
    public Image criarOverlayBloqueador(float alpha) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        Image overlay = new Image(new TextureRegionDrawable(texture));
        overlay.setSize(
            stageCartaZoom.getViewport().getWorldWidth(),
            stageCartaZoom.getViewport().getWorldHeight()
        );
        overlay.setColor(0f, 0f, 0f, alpha);

        overlay.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                event.stop();
            }
        });

        return overlay;
    }

    private void centralizar(Stage stage, Window popup) {
        popup.setPosition(
            stage.getViewport().getWorldWidth() / 2f - popup.getWidth() / 2f,
            stage.getViewport().getWorldHeight() / 2f - popup.getHeight() / 2f
        );
    }

    private void centralizarZoom(Window popup) {
        popup.setPosition(
            stageCartaZoom.getViewport().getWorldWidth() / 2f - popup.getWidth() / 2f,
            stageCartaZoom.getViewport().getWorldHeight() / 2f - 300
        );
    }

    public void mostrarPopupInimigo(Runnable aoLutar, Runnable aoSair) {
        Window popup = new Window("", skin);
        popup.setName("popupCombateInimigo");

        Label texto = new Label("Inimigo encontrado!\nDeseja lutar?", skin);
        texto.setAlignment(Align.center);

        TextButton btnLutar = new TextButton("Lutar", skin);
        TextButton btnSair = new TextButton("Sair", skin);

        btnLutar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popup.remove();
                aoLutar.run();
            }
        });

        btnSair.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popup.remove();
                aoSair.run();
            }
        });

        popup.add(texto).colspan(2).pad(20);
        popup.row();
        popup.add(btnLutar).width(160).pad(10);
        popup.add(btnSair).width(160).pad(10);

        popup.pack();

        popup.setPosition(
            stageCartaZoom.getViewport().getWorldWidth() / 2f - popup.getWidth() / 2f,
            stageCartaZoom.getViewport().getWorldHeight() / 2f - 300
        );

        stageCartaZoom.addActor(popup);
        popup.toFront();
    }
}

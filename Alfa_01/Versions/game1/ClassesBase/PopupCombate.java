package com.root.game.ClassesBase;

import com.badlogic.gdx.scenes.scene2d.ui.*;

public class PopupCombate extends Window {

    public PopupCombate(Skin skin, Runnable lutar, Runnable fugir) {

        super("Decisão", skin);

        setSize(400, 250);
        setMovable(false);
        setModal(true);

        Label texto = new Label("O que deseja fazer?", skin);

        TextButton btnLutar = new TextButton("Lutar", skin);
        TextButton btnFugir = new TextButton("Fugir", skin);

        btnLutar.addListener(e -> {
            if (!btnLutar.isPressed()) return false;
            lutar.run();
            remove();
            return true;
        });

        btnFugir.addListener(e -> {
            if (!btnFugir.isPressed()) return false;
            fugir.run();
            remove();
            return true;
        });

        add(texto).colspan(2).pad(20);
        row();
        add(btnLutar).pad(20);
        add(btnFugir).pad(20);

        pack();
    }
}


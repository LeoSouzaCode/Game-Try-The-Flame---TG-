package com.root.game.ClassesBase;

import com.badlogic.gdx.scenes.scene2d.ui.*;

public class PopupCombate extends Window {

    public PopupCombate(Skin skin, Runnable combate, Runnable sair) {

        super("Decisão", skin);

        setSize(400, 250);
        setMovable(false);
        setModal(true);

        Label texto = new Label("O que deseja fazer?", skin);

        TextButton btnCombate = new TextButton("Combate", skin);
        TextButton btnSair = new TextButton("Sair", skin);

        btnCombate.addListener(e -> {
            if (!btnCombate.isPressed()) return false;
            combate.run();
            remove();
            return true;
        });

        btnSair.addListener(e -> {
            if (!btnSair.isPressed()) return false;
            sair.run();
            remove();
            return true;
        });

        add(texto).colspan(2).pad(20);
        row();
        add(btnCombate).pad(20);
        add(btnSair).pad(20);

        pack();
    }
}


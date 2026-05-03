package com.root.game.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.root.game.Combate.CartaInfo;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.root.game.Animacoes.AnimacaoCarta;
import com.root.game.Utils.TextureManager;

//Gerencia os popups e overlays do jogo.

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
                animarFechamentoPopup(popup, confirmar);
            }
        });

        btnCancelar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                animarFechamentoPopup(popup, cancelar);
            }
        });

        popup.add(texto).colspan(2).pad(20);
        popup.row();
        popup.add(btnConfirmar).width(140).pad(10);
        popup.add(btnCancelar).width(140).pad(10);

        popup.pack();
        centralizar(stageUI, popup);
        stageUI.addActor(popup);
        animarAberturaPopup(popup);
        popup.toFront();
    }

    public void mostrarPopupMensagem(String mensagem, Runnable confirmar) {
        Window popup = new Window("", skin);

        Label texto = new Label(mensagem, skin);
        texto.setAlignment(Align.center);

        TextButton btn = new TextButton("Confirmar", skin);

        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                animarFechamentoPopup(popup, confirmar);
            }
        });

        popup.add(texto).pad(20);
        popup.row();
        popup.add(btn).width(180).pad(15);

        popup.pack();
        centralizarZoom(popup);
        stageCartaZoom.addActor(popup);
        animarAberturaPopup(popup);
        popup.toFront();
    }

    public void mostrarPopupArmaBau(CartaInfo cartaInfo, boolean jogadorTemArma, Runnable aoEquipar, Runnable aoNaoEquipar) {
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
                animarFechamentoPopup(popup, aoEquipar);
            }
        });

        btn2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                animarFechamentoPopup(popup, aoNaoEquipar);
            }
        });

        popup.add(label).colspan(2).pad(20);
        popup.row();
        popup.add(btn1).width(180).pad(10);
        popup.add(btn2).width(180).pad(10);

        popup.pack();
        centralizarZoom(popup);
        stageCartaZoom.addActor(popup);
        animarAberturaPopup(popup);
        popup.toFront();
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
                animarFechamentoPopup(popup, aoLutar);
            }
        });

        btnSair.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                animarFechamentoPopup(popup, aoSair);
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
        animarAberturaPopup(popup);
        popup.toFront();
    }

    //Popup usado quando o jogador clica em carta já revelada.
    //Opções: visualizar a carta em zoom, cancelar e voltar ao tabuleiro
    public void mostrarConfirmacaoVisualizarCarta(Runnable visualizar, Runnable cancelar) {
        if (stageUI.getRoot().findActor("popupVisualizarCarta") != null) return;

        Window popup = new Window("", skin);
        popup.setName("popupVisualizarCarta");

        Label texto = new Label("Visualizar esta carta?", skin);
        texto.setAlignment(Align.center);

        TextButton btnVisualizar = new TextButton("Visualizar", skin);
        TextButton btnCancelar = new TextButton("Cancelar", skin);

        btnVisualizar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                animarFechamentoPopup(popup, visualizar);
            }
        });

        btnCancelar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                animarFechamentoPopup(popup, cancelar);
            }
        });

        popup.add(texto).colspan(2).pad(20);
        popup.row();
        popup.add(btnVisualizar).width(170).pad(10);
        popup.add(btnCancelar).width(170).pad(10);

        popup.pack();

        popup.setPosition(
            stageUI.getViewport().getWorldWidth() / 2f - popup.getWidth() / 2f,
            stageUI.getViewport().getWorldHeight() / 2f - popup.getHeight() / 2f
        );

        stageUI.addActor(popup);
        animarAberturaPopup(popup);
        popup.toFront();
    }

    //Mostra informações de uma carta revelada com uma ação principal.
    //Usado para cartas reveladas adjacentes.
    //Exemplo:
    //inimigo: Combater / Cancelar, baú: Abrir baú / Cancelar, chama: Coletar / Cancelar
    public void mostrarPopupCartaReveladaComAcao(String mensagem, String textoAcao, Runnable acaoPrincipal, Runnable cancelar) {
        Window popup = new Window("", skin);
        popup.setName("popupCartaReveladaComAcao");

        Label texto = new Label(mensagem, skin);
        texto.setAlignment(Align.center);

        TextButton btnAcao = new TextButton(textoAcao, skin);
        TextButton btnCancelar = new TextButton("Cancelar", skin);

        btnAcao.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                animarFechamentoPopup(popup, acaoPrincipal);
            }
        });

        btnCancelar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                animarFechamentoPopup(popup, cancelar);
            }
        });

        popup.add(texto).colspan(2).pad(20);
        popup.row();
        popup.add(btnAcao).width(180).pad(10);
        popup.add(btnCancelar).width(180).pad(10);

        popup.pack();

        popup.setPosition(
            stageUI.getViewport().getWorldWidth() / 2f - popup.getWidth() / 2f,
            stageUI.getViewport().getWorldHeight() / 5f - popup.getHeight() / 2f
        );

        stageCartaZoom.addActor(popup);
        animarAberturaPopup(popup);
        popup.toFront();
    }

    //Exibe a arma equipada em destaque.
    //Usado no HUD do tabuleiro, na miniatura da arma durante o combate.
    public void mostrarDetalheArmaEquipada(
        String nomeArma,
        int durabilidade,
        String texturaPath,
        AnimacaoCarta animacaoCarta,
        Runnable aoFechar
    ) {
        Image overlay = criarOverlayBloqueador(0.75f);
        stageCartaZoom.addActor(overlay);

        Image cartaArma = new Image(new TextureRegionDrawable(
            new TextureRegion(TextureManager.get(texturaPath))
        ));

        cartaArma.setSize(260, 360);
        cartaArma.setOrigin(Align.center);
        cartaArma.setPosition(
            stageCartaZoom.getViewport().getWorldWidth() / 2f - 130,
            stageCartaZoom.getViewport().getWorldHeight() / 2f - 140
        );

        Label labelInfo = new Label(
            nomeArma + "\nDurabilidade: " + durabilidade,
            skin
        );
        labelInfo.setAlignment(Align.center);
        labelInfo.setPosition(
            stageCartaZoom.getViewport().getWorldWidth() / 1.93f - 120,
            120
        );

        TextButton btnFechar = new TextButton("Fechar", skin);
        btnFechar.setSize(160, 50);
        btnFechar.setPosition(
            stageCartaZoom.getViewport().getWorldWidth() / 2f - 80,
            50
        );

        stageCartaZoom.addActor(cartaArma);
        stageCartaZoom.addActor(labelInfo);
        stageCartaZoom.addActor(btnFechar);

        overlay.toFront();
        cartaArma.toFront();
        labelInfo.toFront();
        btnFechar.toFront();

        if (animacaoCarta != null) {
            animacaoCarta.aplicarIdleFlutuacao(cartaArma);
        }

        btnFechar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                //Remove apenas os elementos deste popup.
                //Não limpa o stage inteiro, pois pode existir combate por baixo.
                overlay.remove();
                cartaArma.remove();
                labelInfo.remove();
                btnFechar.remove();

                if (aoFechar != null) {
                    aoFechar.run();
                }
            }
        });
    }

    //Mostra as opções de uma arma encontrada em baú já revelado.
    //Diferença para mostrarPopupArmaBau():
    //não exibe "Você encontrou uma arma", mostra apenas informações diretas da arma;
    //mantém as opções conforme o jogador já tenha arma ou não.
    public void mostrarPopupArmaBauRevelado(
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

        String texto = nome + "\nDurabilidade: " + durabilidade;

        String btnPrincipal = jogadorTemArma ? "Trocar arma" : "Equipar";
        String btnSecundario = jogadorTemArma ? "Manter atual" : "Não equipar";

        Window popup = new Window("", skin);
        popup.setName("popupArmaBauRevelado");

        Label label = new Label(texto, skin);
        label.setAlignment(Align.center);

        TextButton btn1 = new TextButton(btnPrincipal, skin);
        TextButton btn2 = new TextButton(btnSecundario, skin);

        btn1.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                animarFechamentoPopup(popup, aoEquipar);
            }
        });

        btn2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                animarFechamentoPopup(popup, aoNaoEquipar);
            }
        });

        popup.add(label).colspan(2).pad(20);
        popup.row();
        popup.add(btn1).width(190).pad(10);
        popup.add(btn2).width(190).pad(10);

        popup.pack();

        popup.setPosition(
            stageCartaZoom.getViewport().getWorldWidth() / 2f - popup.getWidth() / 2f,
            stageCartaZoom.getViewport().getWorldHeight() / 2f - 300
        );

        stageCartaZoom.addActor(popup);
        animarAberturaPopup(popup);
        popup.toFront();
    }

    //Aplica animação de abertura em janela/popup.
    //Efeito: abre no eixo X, parecido com carta abrindo.
    private void animarAberturaPopup(Actor actor) {
        if (actor == null) return;

        actor.clearActions();
        actor.setOrigin(Align.center);
        actor.setScale(0.01f, 1f);
        actor.getColor().a = 0f;

        actor.addAction(
            Actions.parallel(
                Actions.scaleTo(1f, 1f, 0.18f, Interpolation.fade),
                Actions.fadeIn(0.14f, Interpolation.fade)
            )
        );
    }

    //Fecha o popup com animação no eixo Y.
    //Efeito: fecha verticalmente; remove o ator somente após a animação.
    private void animarFechamentoPopup(Actor actor, Runnable aoFinalizar) {
        if (actor == null) {
            if (aoFinalizar != null) aoFinalizar.run();
            return;
        }

        actor.clearActions();
        actor.setOrigin(Align.center);

        actor.addAction(
            Actions.sequence(
                Actions.parallel(
                    Actions.scaleTo(1f, 0.01f, 0.16f, Interpolation.fade),
                    Actions.fadeOut(0.12f, Interpolation.fade)
                ),
                Actions.run(() -> {
                    actor.remove();

                    if (aoFinalizar != null) {
                        aoFinalizar.run();
                    }
                })
            )
        );
    }

}

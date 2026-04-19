package com.root.game.CorpoPrincipal;

import com.root.game.Combate.CartaInfo;
import com.root.game.Combate.Inimigo;
import com.root.game.Combate.Jogador;
import com.root.game.Combate.ResultadoCombate;
import com.root.game.Combate.SistemaCombate;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.root.game.Animacoes.AnimacaoTabuleiro;
import com.root.game.ClassesBase.Cartas;
import com.root.game.ClassesBase.PopupCombate;
import com.root.game.Utils.PosicaoCartaProvider;
import com.root.game.Utils.TextureManager;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class TCC_0_01 extends ApplicationAdapter {

    private Stage stageCartaZoom;
    private Stage stageTabuleiro;
    private Stage stageUI;
    private Stage stageAnimacao;


    private BitmapFont fonte;
    private com.badlogic.gdx.scenes.scene2d.ui.Label labelMensagem;
    private com.badlogic.gdx.scenes.scene2d.ui.Label labelHUD;
    private com.badlogic.gdx.scenes.scene2d.ui.Skin skin;

    private boolean jogoFinalizado = false;
    private Label labelChamas;
    private Image imagemArmaHUD;
    private Label labelVidaArmaHUD;
    private boolean animandoTabuleiro = false;
    private boolean telaModalAberta = false;
    private Jogador jogadorCombate;
    private SistemaCombate sistemaCombate;

    private Tabuleiro tabuleiro;
    private Cartas[][] cartasVisuais;

    private AnimacaoTabuleiro animacaoTabuleiro;

    public boolean isFinalizado() {
        return jogoFinalizado;
    }

    public boolean isAnimandoTabuleiro() {
        return animandoTabuleiro;
    }

    // tamanho visual
    private static final float CARTA_LARGURA = 108;
    private static final float CARTA_ALTURA = 144;
    private static final float ESPACO = 8;

    @Override
    public void create() {
        stageTabuleiro = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1280, 720));

        stageUI = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1280, 720));

        stageCartaZoom = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1280, 720));

        stageAnimacao = new Stage(
            new com.badlogic.gdx.utils.viewport.FitViewport(1280, 720)
        );

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stageCartaZoom);
        multiplexer.addProcessor(stageUI);
        multiplexer.addProcessor(stageAnimacao);
        multiplexer.addProcessor(stageTabuleiro);
        Gdx.input.setInputProcessor(multiplexer);


        fonte = criarFonte(32, Color.WHITE);

        tabuleiro = new Tabuleiro();
        jogadorCombate = new Jogador(50);
        sistemaCombate = new SistemaCombate();
        cartasVisuais = new Cartas[Tabuleiro.LINHAS][Tabuleiro.COLUNAS];
        criarTabuleiroVisual();

        criarUI();

        atualizarHUD();

        sincronizarTabuleiroVisual();
        atualizarDestaqueCartas();

    }

    @Override
    public void resize(int width, int height) {
        stageTabuleiro.getViewport().update(width, height, true);
        stageUI.getViewport().update(width, height, true);
        stageCartaZoom.getViewport().update(width, height, true);
        stageAnimacao.getViewport().update(width, height, true);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);

        stageTabuleiro.act(delta);
        stageUI.act(delta);
        stageCartaZoom.act(delta);
        stageAnimacao.act(delta);

        stageTabuleiro.draw();
        stageUI.draw();
        stageCartaZoom.draw();
        stageAnimacao.draw();
    }

    @Override
    public void dispose() {
        stageTabuleiro.dispose();
        stageUI.dispose();
        stageCartaZoom.dispose();
        stageAnimacao.dispose();
        fonte.dispose();
        TextureManager.disposeAll();
    }

    private void criarUI() {

        skin = new com.badlogic.gdx.scenes.scene2d.ui.Skin();

        skin.add("default-font", fonte);

        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle style = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();

        style.font = fonte;
        style.fontColor = Color.WHITE;

        skin.add("default", style);

        // ===== Window Style =====
        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle windowStyle = new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle();

        windowStyle.titleFont = fonte;
        windowStyle.titleFontColor = Color.WHITE;

// fundo simples (pode usar qualquer textura sua)
        windowStyle.background = criarDrawableCor(new Color(0f, 0f, 0f, 0.85f));

        skin.add("default", windowStyle);

        labelMensagem = new com.badlogic.gdx.scenes.scene2d.ui.Label("", skin);
        labelMensagem.setPosition(20, 20);
        stageUI.addActor(labelMensagem);

        labelHUD = new com.badlogic.gdx.scenes.scene2d.ui.Label("Chamas: 0/3", skin);
        labelHUD.setPosition(20, stageUI.getViewport().getWorldHeight() - 50);
        stageUI.addActor(labelHUD);

        // ===== Button Style =====
        com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle buttonStyle = new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle();

        buttonStyle.font = fonte;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.GRAY;

        buttonStyle.up = criarDrawableCor(new Color(0.18f, 0.18f, 0.18f, 1f));
        buttonStyle.down = criarDrawableCor(new Color(0.10f, 0.10f, 0.10f, 1f));

        skin.add("default", buttonStyle);

        if (labelChamas == null) {

            labelChamas = new Label("Chamas: 0 / 3", skin);

            labelChamas.setPosition(20, stageUI.getViewport().getWorldHeight() - 40);

        }

    }

    // ================================
    // CRIAÇÃO DO TABULEIRO VISUAL
    // ================================
    private void criarTabuleiroVisual() {

        float larguraTotal = Tabuleiro.COLUNAS * CARTA_LARGURA + (Tabuleiro.COLUNAS - 1) * ESPACO;
        float alturaTotal = Tabuleiro.LINHAS * CARTA_ALTURA + (Tabuleiro.LINHAS - 1) * ESPACO;

        float startX = (stageTabuleiro.getViewport().getWorldWidth() - larguraTotal) / 2f;
        float startY = (stageTabuleiro.getViewport().getWorldHeight() - alturaTotal) / 2f;

        for (int i = 0; i < Tabuleiro.LINHAS; i++) {
            for (int j = 0; j < Tabuleiro.COLUNAS; j++) {

                String frente = obterTextura(i, j);
                String verso = "Cartas/Versos/versoTeste.jpg";

                float x = startX + j * (CARTA_LARGURA + ESPACO);
                float y = startY + (Tabuleiro.LINHAS - 1 - i) * (CARTA_ALTURA + ESPACO);

                Cartas carta = new Cartas(frente, verso, x, y, i, j, this);

                if (i == tabuleiro.getJogadorLinha() && j == tabuleiro.getJogadorColuna()) {
                    carta.setRevelada(true);
                }

                cartasVisuais[i][j] = carta;
                stageTabuleiro.addActor(carta);
            }
        }

        animacaoTabuleiro = new AnimacaoTabuleiro(
            stageTabuleiro,
            cartasVisuais,
            CARTA_LARGURA,
            CARTA_ALTURA,
            ESPACO,
            new PosicaoCartaProvider() {
                @Override
                public float getCartaX(int coluna) {
                    return TCC_0_01.this.getCartaX(coluna);
                }

                @Override
                public float getCartaY(int linha) {
                    return TCC_0_01.this.getCartaY(linha);
                }
            }
        );
    }

    // ================================
    // TRADUZ TIPO → TEXTURA
    // ================================
    private String obterTextura(int linha, int coluna) {

        if (linha == tabuleiro.getJogadorLinha() && coluna == tabuleiro.getJogadorColuna()) {
            return "Cartas/Frente/Jogador/jogadorTeste.png";
        }

        CartaInfo cartaInfo = tabuleiro.getCartaInfo(linha, coluna);

        if (cartaInfo == null) {
            return "Cartas/Versos/versoTeste.jpg";
        }

        Tabuleiro.TipoCarta tipo = cartaInfo.getTipo();

        switch (tipo) {
            case INIMIGO:
                if (cartaInfo.getInimigo() != null) {
                    return cartaInfo.getInimigo().getTexturaPath();
                }
                return "Cartas/Frente/Inimigo/frenteTeste0.jpg";

            case BAU:
                return "Cartas/Frente/Bau/frenteTeste7.jpg";

            case CHAMA:
                return "Cartas/Frente/Chama/frenteTeste4.jpg";

            case PAREDE:
                return "Cartas/Frente/Parede/paredeTeste1.png";

            case VAZIO:
                return "Cartas/Versos/versoTeste.jpg";

            default:
                return "Cartas/Frente/Inimigo/frenteTeste0.jpg";
        }
    }

    // ================================
    // CLIQUE EM CARTA (chamado pela Carta)
    // ================================
    public void clicarCarta(int linha, int coluna) {

        if (jogoFinalizado || animandoTabuleiro || telaModalAberta) return;

        if (!podeRevelarCarta(linha, coluna)) {
            mostrarMensagem("Você só pode revelar cartas adjacentes.");
            animacaoTabuleiro.animarCartaMovimentoInvalido(linha, coluna);
            return;
        }

        Tabuleiro.TipoCarta tipo = tabuleiro.getCarta(linha, coluna);

        if (tipo == Tabuleiro.TipoCarta.VAZIO) {
            mostrarMensagem("Não há nada nesta posição.");
            return;
        }

        mostrarConfirmacaoCarta(() -> {
            mostrarCartaEvento(linha, coluna, tipo);
        });

    }

    private void mostrarCartaEvento(int linha, int coluna, Tabuleiro.TipoCarta tipo) {

        tabuleiro.revelarCarta(linha, coluna);
        telaModalAberta = true;

        // remove temporariamente a carta do grid para não aparecer atrás
        Cartas cartaOriginal = cartasVisuais[linha][coluna];
        cartaOriginal.remove();

        String textura = obterTextura(linha, coluna);

        TextureRegion region = new TextureRegion(TextureManager.get(textura));
        Image cartaZoom = new Image(new TextureRegionDrawable(region));

        float maxLargura = 300f;
        float maxAltura = 400f;

        float texLargura = region.getRegionWidth();
        float texAltura = region.getRegionHeight();

        float escala = Math.min(maxLargura / texLargura, maxAltura / texAltura);

        float larguraFinal = texLargura * escala;
        float alturaFinal = texAltura * escala;

        cartaZoom.setSize(larguraFinal, alturaFinal);
        cartaZoom.setOrigin(Align.center);
        cartaZoom.setScale(0.01f);

        cartaZoom.setPosition(stageCartaZoom.getViewport().getWorldWidth() / 2f - larguraFinal / 2f, stageCartaZoom.getViewport().getWorldHeight() / 2f - 120);

        // limpa qualquer zoom anterior
        stageCartaZoom.clear();

        // overlay escuro
        Image overlay = criarOverlayBloqueador(0.65f);
        stageCartaZoom.addActor(overlay);
        stageCartaZoom.addActor(cartaZoom);
        cartaZoom.toFront();

        cartaZoom.addAction(Actions.sequence(

            Actions.run(() -> aplicarFlip(cartaZoom, () -> {
                cartaZoom.setDrawable(new TextureRegionDrawable(new TextureRegion(TextureManager.get(textura))));
            })),

            Actions.delay(0.25f),

            Actions.scaleTo(1f, 1f, 0.25f, Interpolation.fade),

            Actions.run(() -> aplicarIdleFlutuacao(cartaZoom))));

        switch (tipo) {

            case INIMIGO: {
                mostrarMensagem("Inimigo encontrado!");

                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {

                        PopupCombate popup = new PopupCombate(
                            skin,

                            () -> {
                                stageCartaZoom.clear();
                                telaModalAberta = true;
                                mostrarTelaCombate(linha, coluna, cartaOriginal);
                            },

                            () -> {
                                dissolverCartaZoom(cartaZoom, () -> {
                                    stageCartaZoom.clear();
                                    telaModalAberta = false;
                                    restaurarCartaOriginal(linha, coluna, cartaOriginal);
                                    sincronizarTabuleiroVisual();
                                    atualizarDestaqueCartas();
                                    mostrarMensagem("Você saiu.");
                                });
                            }
                        );

                        stageCartaZoom.addActor(popup);
                        popup.setPosition(
                            stageCartaZoom.getViewport().getWorldWidth() / 2f - popup.getWidth() / 2f,
                            stageCartaZoom.getViewport().getWorldHeight() / 2f - 300
                        );
                        popup.toFront();
                    }
                }, 0.22f);

                atualizarDestaqueCartas();
                break;
            }

            case CHAMA: {
                mostrarPopupMensagem("Chama coletada!", () -> {
                    dissolverCartaZoom(cartaZoom, () -> {
                        stageCartaZoom.clear();
                        telaModalAberta = false;
                        restaurarCartaOriginal(linha, coluna, cartaOriginal);
                        coletarChama(linha, coluna);
                    });
                });

                atualizarDestaqueCartas();
                break;
            }

            case BAU: {
                mostrarPopupMensagem("Baú encontrado!", () -> {

                    CartaInfo cartaInfo = tabuleiro.getCartaInfo(linha, coluna);

                    if (cartaInfo == null || cartaInfo.getArmaDentro() == null) {
                        dissolverCartaZoom(cartaZoom, () -> {
                            stageCartaZoom.clear();
                            telaModalAberta = false;
                            restaurarCartaOriginal(linha, coluna, cartaOriginal);
                            sincronizarTabuleiroVisual();
                            atualizarDestaqueCartas();
                            mostrarMensagem("Baú vazio.");
                        });
                        return;
                    }

                    aplicarFlip(cartaZoom, () -> {
                        cartaZoom.setDrawable(new TextureRegionDrawable(
                            new TextureRegion(TextureManager.get(cartaInfo.getArmaDentro().getTexturaPath()))
                        ));
                    });

                    mostrarPopupArmaBau(
                        linha,
                        coluna,

                        () -> {
                            dissolverCartaZoom(cartaZoom, () -> {
                                stageCartaZoom.clear();
                                telaModalAberta = false;
                                restaurarCartaOriginal(linha, coluna, cartaOriginal);
                                coletarBau(linha, coluna);
                            });
                        },

                        () -> {
                            dissolverCartaZoom(cartaZoom, () -> {
                                stageCartaZoom.clear();
                                telaModalAberta = false;
                                tabuleiro.esconderCarta(linha, coluna);
                                restaurarCartaOriginal(linha, coluna, cartaOriginal);
                                sincronizarTabuleiroVisual();
                                atualizarDestaqueCartas();
                                mostrarMensagem("Você deixou o item no baú.");
                            });
                        }
                    );
                });

                atualizarDestaqueCartas();
                break;
            }

            case VAZIO: {
                stageCartaZoom.clear();
                telaModalAberta = false;
                restaurarCartaOriginal(linha, coluna, cartaOriginal);
                sincronizarTabuleiroVisual();
                atualizarDestaqueCartas();
                mostrarMensagem("Não há nada nesta posição.");
                break;
            }

            case PAREDE: {
                mostrarPopupMensagem("Parede encontrada.\nNão é possível avançar.", () -> {
                    dissolverCartaZoom(cartaZoom, () -> {
                        stageCartaZoom.clear();
                        telaModalAberta = false;
                        restaurarCartaOriginal(linha, coluna, cartaOriginal);
                        sincronizarTabuleiroVisual();
                        atualizarDestaqueCartas();
                    });
                });

                atualizarDestaqueCartas();
                break;
            }

        }
    }

    private void coletarChama(int linha, int coluna) {

        int antigaLinha = tabuleiro.getJogadorLinha();
        int antigaColuna = tabuleiro.getJogadorColuna();

        tabuleiro.coletarChama(linha, coluna);
        tabuleiro.consumirCarta(linha, coluna);

        atualizarHUD();

        if (tabuleiro.getChamasColetadas() >= 3) {
            jogoFinalizado = true;
            mostrarMensagem("Você venceu!");
        }

        atualizarTabuleiroComAnimacao(antigaLinha, antigaColuna, linha, coluna);
    }

    private void coletarBau(int linha, int coluna) {

        CartaInfo cartaInfo = tabuleiro.getCartaInfo(linha, coluna);

        if (cartaInfo != null && cartaInfo.getArmaDentro() != null) {
            jogadorCombate.setArmaEquipada(cartaInfo.getArmaDentro());
            mostrarMensagem("Você equipou: " + cartaInfo.getArmaDentro().getNome());
        } else {
            mostrarMensagem("Baú vazio.");
        }

        tabuleiro.consumirCarta(linha, coluna);

        atualizarHUD();

        int antigaLinha = tabuleiro.getJogadorLinha();
        int antigaColuna = tabuleiro.getJogadorColuna();

        atualizarTabuleiroComAnimacao(antigaLinha, antigaColuna, linha, coluna);
    }

    private void moverJogadorPara(int linha, int coluna) {
        int antigaLinha = tabuleiro.getJogadorLinha();
        int antigaColuna = tabuleiro.getJogadorColuna();

        atualizarTabuleiroComAnimacao(antigaLinha, antigaColuna, linha, coluna);
    }

    private BitmapFont criarFonte(int tamanho, Color cor) {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("Fonts/MorrisRomanAlternate-Black.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = tamanho;
        p.color = cor;

        BitmapFont f = gen.generateFont(p);
        gen.dispose();
        return f;
    }

    private void mostrarMensagem(String texto) {

        labelMensagem.setText(texto);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                labelMensagem.setText("");
            }
        }, 2f);
    }

    private void sincronizarTabuleiroVisual() {

        int jogadorLinha = tabuleiro.getJogadorLinha();
        int jogadorColuna = tabuleiro.getJogadorColuna();

        for (int i = 0; i < Tabuleiro.LINHAS; i++) {
            for (int j = 0; j < Tabuleiro.COLUNAS; j++) {

                Cartas carta = cartasVisuais[i][j];
                if (carta == null) continue;

                carta.setPosicaoGrid(i, j);

                carta.clearActions();
                carta.setVisible(true);
                carta.getColor().a = 1f;
                carta.setScale(1f, 1f);
                carta.setRotation(0f);
                carta.setPosition(getCartaX(j), getCartaY(i));

                // PRIORIDADE TOTAL PARA O JOGADOR
                if (i == jogadorLinha && j == jogadorColuna) {

                    carta.setTexturaFrente(
                        TextureManager.get("Cartas/Frente/Jogador/jogadorTeste.png")
                    );

                    carta.setRevelada(true);
                    carta.toFront();

                } else {

                    carta.setTexturaFrente(TextureManager.get(obterTextura(i, j)));

                    if (tabuleiro.getCarta(i, j) == Tabuleiro.TipoCarta.VAZIO) {
                        carta.setRevelada(false);
                    } else {
                        carta.setRevelada(tabuleiro.cartaEstaRevelada(i, j));
                    }
                }
            }
        }
    }

    private void aplicarFlip(Image carta, Runnable aposFlip) {

        carta.setOrigin(Align.center);

        carta.addAction(Actions.sequence(

            // fecha quase totalmente (evita scaleX = 0)
            Actions.scaleTo(0.05f, 1f, 0.12f, Interpolation.fade),

            // troca textura exatamente no meio
            Actions.run(aposFlip),

            // abre novamente
            Actions.scaleTo(1f, 1f, 0.12f, Interpolation.fade)));
    }

    private void aplicarIdleFlutuacao(Image carta) {

        carta.addAction(Actions.forever(Actions.sequence(Actions.moveBy(0, 8, 1.2f, Interpolation.sine), Actions.moveBy(0, -8, 1.2f, Interpolation.sine))));
    }

    private void restaurarCartaOriginal(int linha, int coluna, Cartas cartaOriginal) {
        if (cartaOriginal.getStage() == null) {
            stageTabuleiro.addActor(cartaOriginal);
        }

        cartasVisuais[linha][coluna] = cartaOriginal;

        cartaOriginal.clearActions();
        cartaOriginal.setVisible(true);
        cartaOriginal.setScale(1f, 1f);
        cartaOriginal.setRotation(0f);
        cartaOriginal.getColor().a = 1f;
        cartaOriginal.setPosition(getCartaX(coluna), getCartaY(linha));
        cartaOriginal.setPosicaoGrid(linha, coluna);

        // PRIORIDADE TOTAL PARA O JOGADOR
        if (linha == tabuleiro.getJogadorLinha() && coluna == tabuleiro.getJogadorColuna()) {
            cartaOriginal.setTexturaFrente(
                TextureManager.get("Cartas/Frente/Jogador/jogadorTeste.png")
            );
            cartaOriginal.setRevelada(true);
            cartaOriginal.toFront();
        } else {
            cartaOriginal.setTexturaFrente(TextureManager.get(obterTextura(linha, coluna)));

            if (tabuleiro.getCarta(linha, coluna) == Tabuleiro.TipoCarta.VAZIO) {
                cartaOriginal.setRevelada(false);
            } else {
                cartaOriginal.setRevelada(tabuleiro.cartaEstaRevelada(linha, coluna));
            }
        }
    }

    private Drawable criarDrawableCor(Color cor) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(cor);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private void mostrarPopupMensagem(String mensagem, Runnable confirmar) {

        Window popup = new Window("", skin);

        Label texto = new Label(mensagem, skin);
        TextButton btnConfirmar = new TextButton("Confirmar", skin);

        btnConfirmar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popup.remove();
                confirmar.run();
            }
        });

        popup.add(texto).pad(20);
        popup.row();
        popup.add(btnConfirmar).pad(15).width(180);

        popup.pack();

        popup.setPosition(stageCartaZoom.getViewport().getWorldWidth() / 2f - popup.getWidth() / 2f, stageCartaZoom.getViewport().getWorldHeight() / 2f - 300);

        stageCartaZoom.addActor(popup);
        popup.setName("popupConfirmacao");

    }

    private void atualizarHUD() {

        if (labelHUD == null) return;

        String textoArma = "Sem arma";

        if (jogadorCombate != null && jogadorCombate.getArmaEquipada() != null) {
            textoArma = jogadorCombate.getArmaEquipada().getNome()
                + " (" + jogadorCombate.getArmaEquipada().getDurabilidade() + ")";
        }

        labelHUD.setText(
            "Chamas: " + tabuleiro.getChamasColetadas() + " / 3"
                + "    Vida: " + jogadorCombate.getVida()
                + "    Arma: " + textoArma
        );

        atualizarMiniaturaArmaHUD();
    }

    private void mostrarConfirmacaoCarta(Runnable confirmar) {

        if (stageUI.getRoot().findActor("popupConfirmacao") != null) return;

        Window popup = new Window("", skin);
        popup.setName("popupConfirmacao");

        Label texto = new Label("Revelar esta carta?", skin);
        texto.setAlignment(Align.center);

        TextButton confirmarBtn = new TextButton("Confirmar", skin);
        TextButton cancelarBtn = new TextButton("Cancelar", skin);

        confirmarBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                popup.remove();
                confirmar.run();
            }
        });

        cancelarBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                popup.remove();
            }
        });

        popup.add(texto).colspan(2).pad(20);
        popup.row();
        popup.add(confirmarBtn).width(140).pad(10);
        popup.add(cancelarBtn).width(140).pad(10);

        popup.pack();

        popup.setPosition(stageUI.getViewport().getWorldWidth() / 2f - popup.getWidth() / 2f, stageUI.getViewport().getWorldHeight() / 2f - popup.getHeight() / 2f);

        stageUI.addActor(popup);
    }

    private boolean podeRevelarCarta(int linha, int coluna) {

        int jLinha = tabuleiro.getJogadorLinha();
        int jColuna = tabuleiro.getJogadorColuna();

        int dLinha = Math.abs(linha - jLinha);
        int dColuna = Math.abs(coluna - jColuna);

        // permite apenas cartas adjacentes (cima, baixo, esquerda, direita)
        return (dLinha + dColuna) == 1;
    }

    private void atualizarDestaqueCartas() {

        int jLinha = tabuleiro.getJogadorLinha();
        int jColuna = tabuleiro.getJogadorColuna();

        for (int i = 0; i < cartasVisuais.length; i++) {
            for (int j = 0; j < cartasVisuais[i].length; j++) {

                Cartas carta = cartasVisuais[i][j];
                if (carta == null) continue;

                int dLinha = Math.abs(i - jLinha);
                int dColuna = Math.abs(j - jColuna);

                boolean adjacente = (dLinha + dColuna) == 1;
                boolean jogador = (i == jLinha && j == jColuna);

                carta.clearActions();

                if (jogador) {

                    carta.setColor(1f, 1f, 1f, 1f);

                } else if (adjacente) {

                    carta.setColor(1f, 1f, 1f, 0.25f);

                    carta.addAction(Actions.forever(Actions.sequence(Actions.color(new Color(0.85f, 0.9f, 1f, 1f), 0.8f), Actions.color(Color.WHITE, 0.8f))));

                } else {

                    // efeito holográfico leve
                    carta.setColor(0.8f, 0.85f, 1f, 0.65f);

                }
            }
        }
    }

    private void dissolverCartaZoom(Image cartaZoom, Runnable aoFinalizar) {

        cartaZoom.clearActions();

        cartaZoom.addAction(Actions.sequence(Actions.parallel(Actions.fadeOut(0.13f), Actions.scaleTo(0.80f, 0.80f, 0.22f, Interpolation.fade), Actions.moveBy(0f, 15f, 0.22f, Interpolation.fade)), Actions.run(() -> {
            if (aoFinalizar != null) {
                aoFinalizar.run();
            }
        })));
    }

    private float getCartaX(int coluna) {
        float larguraTotal = Tabuleiro.COLUNAS * CARTA_LARGURA + (Tabuleiro.COLUNAS - 1) * ESPACO;
        float startX = (stageTabuleiro.getViewport().getWorldWidth() - larguraTotal) / 2f;
        return startX + coluna * (CARTA_LARGURA + ESPACO);
    }

    private float getCartaY(int linha) {
        float alturaTotal = Tabuleiro.LINHAS * CARTA_ALTURA + (Tabuleiro.LINHAS - 1) * ESPACO;
        float startY = (stageTabuleiro.getViewport().getWorldHeight() - alturaTotal) / 2f;
        return startY + (Tabuleiro.LINHAS - 1 - linha) * (CARTA_ALTURA + ESPACO);
    }

    private void atualizarTabuleiroComAnimacao(int antigaLinha, int antigaColuna, int novaLinha, int novaColuna) {

        if (animandoTabuleiro) return;

        animandoTabuleiro = true;

        animacaoTabuleiro.animarMovimentoJogadorComEsteira(
            antigaLinha,
            antigaColuna,
            novaLinha,
            novaColuna,
            () -> {
                System.out.println("CALLBACK ANIMACAO - antes moverJogador\n");
                tabuleiro.imprimirGridDebug();

                tabuleiro.moverJogador(novaLinha, novaColuna);

                System.out.println("CALLBACK ANIMACAO - depois moverJogador\n");
                tabuleiro.imprimirGridDebug();

                tabuleiro.aplicarEsteira(antigaLinha, antigaColuna, novaLinha, novaColuna);

                System.out.println("CALLBACK ANIMACAO - depois aplicarEsteira\n");
                tabuleiro.imprimirGridDebug();

                sincronizarTabuleiroVisual();
                animacaoTabuleiro.resetarEstadoVisualDasCartas();
                atualizarDestaqueCartas();

                animandoTabuleiro = false;
            }
        );
    }

    private void mostrarPopupArmaBau(int linha, int coluna, Runnable aoEquipar, Runnable aoNaoEquipar) {

        CartaInfo cartaInfo = tabuleiro.getCartaInfo(linha, coluna);

        String nomeItem = "Item desconhecido";
        int durabilidade = 0;
        boolean jogadorJaTemArma = jogadorCombate != null && jogadorCombate.getArmaEquipada() != null;

        if (cartaInfo != null && cartaInfo.getArmaDentro() != null) {
            nomeItem = cartaInfo.getArmaDentro().getNome();
            durabilidade = cartaInfo.getArmaDentro().getDurabilidade();
        }

        String textoPopup;
        String textoBotaoPrincipal;
        String textoBotaoSecundario;

        if (jogadorJaTemArma) {
            textoPopup =
                "Você encontrou uma arma:\n" + nomeItem +
                    "\nDurabilidade: " + durabilidade +
                    "\n\nDeseja trocar sua arma equipada por esta?";
            textoBotaoPrincipal = "Trocar arma";
            textoBotaoSecundario = "Não trocar";
        } else {
            textoPopup =
                "Você encontrou uma arma:\n" + nomeItem +
                    "\nDurabilidade: " + durabilidade +
                    "\n\nDeseja equipar esta arma?";
            textoBotaoPrincipal = "Equipar arma";
            textoBotaoSecundario = "Não equipar";
        }

        Window popup = new Window("", skin);
        popup.setName("popupArmaBau");

        Label texto = new Label(textoPopup, skin);
        texto.setAlignment(Align.center);

        TextButton btnPrincipal = new TextButton(textoBotaoPrincipal, skin);
        TextButton btnSecundario = new TextButton(textoBotaoSecundario, skin);

        btnPrincipal.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popup.remove();
                aoEquipar.run();
            }
        });

        btnSecundario.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popup.remove();
                aoNaoEquipar.run();
            }
        });

        popup.add(texto).colspan(2).pad(20);
        popup.row();
        popup.add(btnPrincipal).width(190).pad(10);
        popup.add(btnSecundario).width(190).pad(10);

        popup.pack();

        popup.setPosition(
            stageCartaZoom.getViewport().getWorldWidth() / 2f - popup.getWidth() / 2f,
            stageCartaZoom.getViewport().getWorldHeight() / 2f - 300
        );

        stageCartaZoom.addActor(popup);
        popup.toFront();
    }

    private void mostrarTelaCombate(int linha, int coluna, Cartas cartaOriginal) {

        CartaInfo cartaInfo = tabuleiro.getCartaInfo(linha, coluna);

        if (cartaInfo == null || cartaInfo.getInimigo() == null) {
            mostrarMensagem("Erro: inimigo não encontrado.");
            restaurarCartaOriginal(linha, coluna, cartaOriginal);
            return;
        }

        Inimigo inimigo = cartaInfo.getInimigo();

        stageCartaZoom.clear();

        Image overlay = criarOverlayBloqueador(0.75f);
        stageCartaZoom.addActor(overlay);

        Image cartaInimigo = new Image(new TextureRegionDrawable(
            new TextureRegion(TextureManager.get(inimigo.getTexturaPath()))
        ));
        cartaInimigo.setSize(280, 380);
        cartaInimigo.setPosition(300, 300);
        stageCartaZoom.addActor(cartaInimigo);

        Image cartaJogador = new Image(new TextureRegionDrawable(
            new TextureRegion(TextureManager.get("Cartas/Frente/Jogador/jogadorTeste.png"))
        ));
        cartaJogador.setSize(280, 380);
        cartaJogador.setPosition(700, 300);
        stageCartaZoom.addActor(cartaJogador);

        aplicarIdleFlutuacao(cartaInimigo);
        aplicarIdleFlutuacao(cartaJogador);
        Image miniArmaCombate;

        if (jogadorCombate.getArmaEquipada() != null) {
            miniArmaCombate = new Image(new TextureRegionDrawable(
                new TextureRegion(TextureManager.get(jogadorCombate.getArmaEquipada().getTexturaPath()))
            ));
            miniArmaCombate.setSize(70, 95);
            miniArmaCombate.setPosition(
                cartaJogador.getX() + cartaJogador.getWidth() - 1,
                cartaJogador.getY() + 5
            );
            stageCartaZoom.addActor(miniArmaCombate);
        } else {
            miniArmaCombate = null;
        }

        Label labelVidaInimigo = new Label(
            inimigo.getNome() + " - Vida: " + inimigo.getVida(),
            skin
        );
        labelVidaInimigo.setPosition(300, 215);
        stageCartaZoom.addActor(labelVidaInimigo);

        Integer durabilidadeInicial = null;
        if (jogadorCombate.getArmaEquipada() != null) {
            durabilidadeInicial = jogadorCombate.getArmaEquipada().getDurabilidade();
        }

        Label labelVidaJogador = new Label(
            montarTextoStatusJogadorCombate(jogadorCombate.getVida(), durabilidadeInicial),
            skin
        );
        labelVidaJogador.setPosition(700, 215);
        stageCartaZoom.addActor(labelVidaJogador);

        TextButton btnVoltar = new TextButton("Voltar", skin);
        btnVoltar.setSize(140, 50);
        btnVoltar.setPosition(40, stageCartaZoom.getViewport().getWorldHeight() - 90);

        TextButton btnLutar = new TextButton("Lutar", skin);
        btnLutar.setSize(180, 60);
        btnLutar.setPosition(460, 90);

        TextButton btnFurtividade = new TextButton("Furtividade", skin);
        btnFurtividade.setSize(180, 60);
        btnFurtividade.setPosition(660, 90);

        btnVoltar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stageCartaZoom.clear();
                telaModalAberta = false;
                restaurarCartaOriginal(linha, coluna, cartaOriginal);
                sincronizarTabuleiroVisual();
                atualizarDestaqueCartas();
            }
        });

        btnFurtividade.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarMensagem("Furtividade ainda não implementada.");
            }
        });

        btnLutar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                btnLutar.setDisabled(true);
                btnFurtividade.setDisabled(true);
                btnVoltar.setDisabled(true);

                ResultadoCombate resultado = sistemaCombate.resolverCombate(jogadorCombate, inimigo);

                final int[] vidaInimigoExibida = { inimigo.getVida() };
                final int[] vidaJogadorExibida = { resultado.getVidaInicialJogador() };
                final int[] durabilidadeExibida = { resultado.getDurabilidadeInicialArma() };

                cartaJogador.addAction(Actions.sequence(
                    Actions.parallel(
                        Actions.moveBy(-20f, 0f, 0.08f, Interpolation.fade),
                        Actions.scaleTo(1.05f, 1.05f, 0.08f, Interpolation.fade)
                    ),
                    Actions.parallel(
                        Actions.moveBy(20f, 0f, 0.08f, Interpolation.fade),
                        Actions.scaleTo(1f, 1f, 0.08f, Interpolation.fade)
                    )
                ));

                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        if (vidaInimigoExibida[0] > 0) {
                            vidaInimigoExibida[0]--;
                            labelVidaInimigo.setText(inimigo.getNome() + " - Vida: " + vidaInimigoExibida[0]);
                        } else {
                            cancel();
                        }
                    }
                }, 0f, 0.08f);

                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {

                        boolean terminouVida = vidaJogadorExibida[0] <= resultado.getVidaFinalJogador();
                        boolean terminouArma = durabilidadeExibida[0] <= resultado.getDurabilidadeFinalArma();

                        if (durabilidadeExibida[0] > resultado.getDurabilidadeFinalArma()) {
                            durabilidadeExibida[0]--;
                        } else if (vidaJogadorExibida[0] > resultado.getVidaFinalJogador()) {
                            vidaJogadorExibida[0]--;
                        }

                        labelVidaJogador.setText(
                            montarTextoStatusJogadorCombate(
                                vidaJogadorExibida[0],
                                durabilidadeExibida[0] > 0 ? durabilidadeExibida[0] : null
                            )
                        );

                        if (terminouVida && terminouArma) {
                            cancel();

                            if (resultado.isArmaQuebrou()) {
                                if (miniArmaCombate != null) {
                                    miniArmaCombate.addAction(Actions.sequence(
                                        Actions.parallel(
                                            Actions.fadeOut(0.35f),
                                            Actions.scaleTo(0.4f, 0.4f, 0.35f, Interpolation.fade),
                                            Actions.rotateBy(-120f, 0.35f, Interpolation.fade)
                                        ),
                                        Actions.removeActor()
                                    ));
                                }
                                mostrarMensagem("Sua arma quebrou!");
                            }

                            cartaJogador.clearActions();
                            cartaInimigo.clearActions();

                            cartaJogador.setScale(1f, 1f);
                            cartaJogador.setRotation(0f);

                            cartaInimigo.addAction(Actions.sequence(

                                // impacto inicial: pequena travada + tremor curto
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

                                // "colapso" da carta: sobe um pouco, perde forma, clareia e gira pouco
                                Actions.parallel(
                                    Actions.moveBy(0f, 22f, 0.22f, Interpolation.sineOut),
                                    Actions.scaleTo(1.08f, 0.92f, 0.22f, Interpolation.fade),
                                    Actions.rotateBy(-8f, 0.22f, Interpolation.fade),
                                    Actions.color(new Color(0.92f, 0.92f, 0.92f, 1f), 0.22f)
                                ),

                                // desaparecimento principal: "achata" e some
                                Actions.parallel(
                                    Actions.fadeOut(0.42f),
                                    Actions.scaleTo(0.18f, 1.18f, 0.42f, Interpolation.pow2In),
                                    Actions.rotateBy(-10f, 0.42f, Interpolation.pow2In),
                                    Actions.moveBy(0f, -18f, 0.42f, Interpolation.pow2In),
                                    Actions.color(new Color(1f, 1f, 1f, 0f), 0.42f)
                                ),

                                Actions.delay(0.10f),

                                Actions.run(() -> {
                                    atualizarHUD();
                                    mostrarMensagem(resultado.getMensagemResultado());

                                    stageCartaZoom.clear();
                                    telaModalAberta = false;
                                    restaurarCartaOriginal(linha, coluna, cartaOriginal);

                                    if (resultado.isJogadorVenceu()) {
                                        tabuleiro.consumirCarta(linha, coluna);
                                        moverJogadorPara(linha, coluna);
                                    } else {
                                        mostrarGameOverTela();
                                    }
                                })
                            ));
                        }
                    }
                }, 0.35f, 0.09f);
            }
        });

        stageCartaZoom.addActor(btnVoltar);
        stageCartaZoom.addActor(btnLutar);
        stageCartaZoom.addActor(btnFurtividade);
    }

    private String montarTextoStatusJogadorCombate(int vidaExibida, Integer durabilidadeExibida) {

        String texto = "Jogador - Vida: " + vidaExibida;

        if (jogadorCombate.getArmaEquipada() != null && durabilidadeExibida != null) {
            texto += "\nArma: " + jogadorCombate.getArmaEquipada().getNome()
                + " (" + durabilidadeExibida + ")";
        } else if (durabilidadeExibida != null && durabilidadeExibida > 0) {
            texto += "\nArma: equipada (" + durabilidadeExibida + ")";
        } else {
            texto += "\nArma: Sem arma";
        }

        return texto;
    }

    private void atualizarMiniaturaArmaHUD() {

        if (imagemArmaHUD != null) {
            imagemArmaHUD.remove();
            imagemArmaHUD = null;
        }

        if (labelVidaArmaHUD != null) {
            labelVidaArmaHUD.remove();
            labelVidaArmaHUD = null;
        }

        if (jogadorCombate == null || jogadorCombate.getArmaEquipada() == null) {
            return;
        }

        String texturaArma = jogadorCombate.getArmaEquipada().getTexturaPath();

        imagemArmaHUD = new Image(new TextureRegionDrawable(
            new TextureRegion(TextureManager.get(texturaArma))
        ));
        imagemArmaHUD.setSize(64, 86);
        imagemArmaHUD.setPosition(
            stageUI.getViewport().getWorldWidth() - 90,
            stageUI.getViewport().getWorldHeight() - 110
        );

        imagemArmaHUD.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarPopupDetalheArmaHUD();
            }
        });

        labelVidaArmaHUD = new Label(
            String.valueOf(jogadorCombate.getArmaEquipada().getDurabilidade()),
            skin
        );
        labelVidaArmaHUD.setPosition(
            imagemArmaHUD.getX() + 18,
            imagemArmaHUD.getY() - 28
        );

        stageUI.addActor(imagemArmaHUD);
        stageUI.addActor(labelVidaArmaHUD);
    }

    private void mostrarPopupDetalheArmaHUD() {

        if (jogadorCombate == null || jogadorCombate.getArmaEquipada() == null) {
            mostrarMensagem("Nenhuma arma equipada.");
            return;
        }

        telaModalAberta = true;
        stageCartaZoom.clear();

        Image overlay = criarOverlayBloqueador(0.75f);
        stageCartaZoom.addActor(overlay);

        Image cartaArma = new Image(new TextureRegionDrawable(
            new TextureRegion(TextureManager.get(jogadorCombate.getArmaEquipada().getTexturaPath()))
        ));
        cartaArma.setSize(260, 360);
        cartaArma.setOrigin(Align.center);
        cartaArma.setPosition(
            stageCartaZoom.getViewport().getWorldWidth() / 2f - 130,
            stageCartaZoom.getViewport().getWorldHeight() / 2f - 140
        );

        Label labelInfo = new Label(
            jogadorCombate.getArmaEquipada().getNome()
                + "\nDurabilidade: " + jogadorCombate.getArmaEquipada().getDurabilidade(),
            skin
        );
        labelInfo.setAlignment(Align.center);
        labelInfo.setPosition(
            stageCartaZoom.getViewport().getWorldWidth() / 2f - 120,
            120
        );

        TextButton btnFechar = new TextButton("Fechar", skin);
        btnFechar.setSize(160, 50);
        btnFechar.setPosition(
            stageCartaZoom.getViewport().getWorldWidth() / 2f - 80,
            50
        );
        btnFechar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stageCartaZoom.clear();
                telaModalAberta = false;
            }
        });

        stageCartaZoom.addActor(cartaArma);
        stageCartaZoom.addActor(labelInfo);
        stageCartaZoom.addActor(btnFechar);

        aplicarIdleFlutuacao(cartaArma);
    }

    private void mostrarGameOverTela() {

        jogoFinalizado = true;
        stageCartaZoom.clear();

        Image overlay = criarOverlayBloqueador(0.85f);

        Label labelGameOver = new Label("GAME OVER", skin);
        labelGameOver.setFontScale(2.2f);
        labelGameOver.setPosition(
            stageCartaZoom.getViewport().getWorldWidth() / 2f - 140,
            stageCartaZoom.getViewport().getWorldHeight() / 2f + 40
        );

        Label labelSub = new Label(
            "Você foi derrotado.\nO menu de runs será ligado depois.",
            skin
        );
        labelSub.setAlignment(Align.center);
        labelSub.setPosition(
            stageCartaZoom.getViewport().getWorldWidth() / 2f - 170,
            stageCartaZoom.getViewport().getWorldHeight() / 2f - 40
        );

        stageCartaZoom.addActor(overlay);
        stageCartaZoom.addActor(labelGameOver);
        stageCartaZoom.addActor(labelSub);
    }

    private Image criarOverlayBloqueador(float alpha) {
        Image overlay = new Image(new TextureRegionDrawable(
            new TextureRegion(TextureManager.get("Cartas/Versos/versoTeste.jpg"))
        ));

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

}

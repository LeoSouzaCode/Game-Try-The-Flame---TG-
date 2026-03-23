package com.root.game.CorpoPrincipal;

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
import com.root.game.ClassesBase.Cartas;
import com.root.game.ClassesBase.PopupCombate;
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
    private boolean animandoTabuleiro = false;

    private Tabuleiro tabuleiro;
    private Cartas[][] cartasVisuais;

    public boolean isFinalizado() {
        return jogoFinalizado;
    }

    public boolean isAnimandoTabuleiro() {
        return animandoTabuleiro;
    }

    String verso = "";

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
        fonte.dispose();
        stageAnimacao.dispose();
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

                // posição inicial do jogador
                if (i == tabuleiro.getJogadorLinha() && j == tabuleiro.getJogadorColuna()) {
                    carta.setRevelada(true);
                }

                cartasVisuais[i][j] = carta;
                stageTabuleiro.addActor(carta);
                ;
            }
        }
    }

    // ================================
    // TRADUZ TIPO → TEXTURA
    // ================================
    private String obterTextura(int linha, int coluna) {

        if (linha == tabuleiro.getJogadorLinha() && coluna == tabuleiro.getJogadorColuna()) {

            return "Cartas/Frente/Jogador/jogadorTeste.png";
        }

        Tabuleiro.TipoCarta tipo = tabuleiro.getCarta(linha, coluna);

        switch (tipo) {
            case INIMIGO:
                return "Cartas/Frente/Inimigo/frenteTeste0.jpg";
            case BAU:
                return "Cartas/Frente/Bau/frenteTeste7.jpg";
            case CHAMA:
                return "Cartas/Frente/Chama/frenteTeste4.jpg";
            case PAREDE:
                return "Cartas/Frente/Parede/paredeTeste1.png";
            default:
                return "Cartas/Frente/Inimigo/frenteTeste0.jpg";
        }
    }

    // ================================
    // CLIQUE EM CARTA (chamado pela Carta)
    // ================================
    public void clicarCarta(int linha, int coluna) {

        if (animandoTabuleiro) return;

        if (!podeRevelarCarta(linha, coluna)) {
            animarCartaMovimentoInvalido(linha, coluna);
            mostrarMensagem("Você só pode revelar cartas adjacentes.");
            return;
        }

        Tabuleiro.TipoCarta tipo = tabuleiro.getCarta(linha, coluna);

        mostrarConfirmacaoCarta(() -> {
            mostrarCartaEvento(linha, coluna, tipo);
        });
    }

    private void mostrarCartaEvento(int linha, int coluna, Tabuleiro.TipoCarta tipo) {

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
        Image overlay = new Image(new TextureRegionDrawable(new TextureRegion(TextureManager.get("Cartas/Versos/versoTeste.jpg"))));
        overlay.setSize(stageCartaZoom.getViewport().getWorldWidth(), stageCartaZoom.getViewport().getWorldHeight());
        overlay.setColor(0f, 0f, 0f, 0.65f);

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

                        PopupCombate popup = new PopupCombate(skin,

                            () -> {
                                dissolverCartaZoom(cartaZoom, () -> {
                                    stageCartaZoom.clear();
                                    restaurarCartaOriginal(linha, coluna, cartaOriginal);
                                    executarCombate(linha, coluna);
                                });
                            },

                            () -> {
                                dissolverCartaZoom(cartaZoom, () -> {
                                    stageCartaZoom.clear();
                                    restaurarCartaOriginal(linha, coluna, cartaOriginal);
                                    mostrarMensagem("Você fugiu.");
                                });
                            }
                        );
                        stageCartaZoom.addActor(popup);
                        popup.setPosition(stageCartaZoom.getViewport().getWorldWidth() / 2f - popup.getWidth() / 2f, stageCartaZoom.getViewport().getWorldHeight() / 2f - 300);
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
                        restaurarCartaOriginal(linha, coluna, cartaOriginal);
                        coletarChama(linha, coluna);
                    });
                });

                atualizarDestaqueCartas();
                break;
            }

            case BAU: {
                mostrarPopupMensagem("Baú coletado!", () -> {
                    dissolverCartaZoom(cartaZoom, () -> {
                        stageCartaZoom.clear();
                        restaurarCartaOriginal(linha, coluna, cartaOriginal);
                        coletarBau(linha, coluna);
                    });
                });

                atualizarDestaqueCartas();
                break;
            }

            case PAREDE: {
                mostrarPopupMensagem("Parede encontrada.\nNão é possível avançar.", () -> {
                    dissolverCartaZoom(cartaZoom, () -> {
                        stageCartaZoom.clear();
                        restaurarCartaOriginal(linha, coluna, cartaOriginal);
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

        atualizarHUD();

        if (tabuleiro.getChamasColetadas() >= 3) {
            jogoFinalizado = true;
            mostrarMensagem("Você venceu!");
        }

        atualizarTabuleiroComAnimacao(antigaLinha, antigaColuna, linha, coluna);
    }

    private void coletarBau(int linha, int coluna) {

        int antigaLinha = tabuleiro.getJogadorLinha();
        int antigaColuna = tabuleiro.getJogadorColuna();

        atualizarTabuleiroComAnimacao(antigaLinha, antigaColuna, linha, coluna);
    }

    private void executarCombate(int linha, int coluna) {

        mostrarMensagem("Combate iniciado!");

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

                carta.clearActions();
                carta.setVisible(true);
                carta.getColor().a = 1f;
                carta.setScale(1f, 1f);
                carta.setRotation(0f);
                carta.setPosition(getCartaX(j), getCartaY(i));

                if (i == jogadorLinha && j == jogadorColuna) {

                    carta.setTexturaFrente(
                        TextureManager.get("Cartas/Frente/Jogador/jogadorTeste.png")
                    );

                    carta.setRevelada(true);
                    carta.toFront();

                } else {

                    carta.setTexturaFrente(TextureManager.get(obterTextura(i, j)));
                    carta.setRevelada(false);
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

        labelHUD.setText("Chamas: " + tabuleiro.getChamasColetadas() + " / 3");
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

    private void animarMovimentoJogadorComEsteira(int antigaLinha, int antigaColuna, int novaLinha, int novaColuna, Runnable aoFinalizar) {
        animandoTabuleiro = true;

        Cartas jogador = cartasVisuais[antigaLinha][antigaColuna];
        if (jogador == null) {
            animandoTabuleiro = false;
            if (aoFinalizar != null) aoFinalizar.run();
            return;
        }

        float levantarDuracao = 0.10f;
        float esteiraDuracao = 0.22f;
        float descidaDuracao = 0.09f;

        float baseX = getCartaX(antigaColuna);
        float baseY = getCartaY(antigaLinha);

        float destinoX = getCartaX(novaColuna);
        float destinoY = getCartaY(novaLinha);

        jogador.clearActions();
        jogador.setOrigin(Align.center);
        jogador.toFront();

        jogador.addAction(
            Actions.sequence(
                // 1) levanta sem sair do lugar
                Actions.parallel(
                    Actions.moveTo(baseX, baseY + 22f, levantarDuracao, Interpolation.pow2Out),
                    Actions.scaleTo(1.08f, 1.20f, levantarDuracao, Interpolation.pow2Out)
                ),

                // 2) esteira roda por baixo
                Actions.run(() -> animarEsteiraCompletaSobJogador(
                    antigaLinha, antigaColuna, novaLinha, novaColuna, esteiraDuracao
                )),

                // 3) espera a esteira quase terminar
                Actions.delay(esteiraDuracao * 0.88f),

                // 4) jogador já aparece acima da nova casa
                Actions.run(() -> jogador.setPosition(destinoX, destinoY + 18f)),

                // 5) só desce reto
                Actions.parallel(
                    Actions.moveTo(destinoX, destinoY, descidaDuracao, Interpolation.pow2In),
                    Actions.scaleTo(1f, 1f, descidaDuracao, Interpolation.fade)
                ),

                Actions.run(() -> {
                    if (aoFinalizar != null) aoFinalizar.run();
                })
            )
        );
    }

    private void animarEsteiraCompletaSobJogador(int antigaLinha, int antigaColuna, int novaLinha, int novaColuna, float duracao) {
        int dx = novaColuna - antigaColuna;
        int dy = novaLinha - antigaLinha;

        float passoX = (CARTA_LARGURA + ESPACO) * 1.10f;
        float passoY = (CARTA_ALTURA + ESPACO) * 1.10f;

        if (dx != 0) {
            int linha = antigaLinha;

            for (int j = 0; j < Tabuleiro.COLUNAS; j++) {
                Cartas carta = cartasVisuais[linha][j];
                if (carta == null) continue;
                if (j == antigaColuna) continue;

                float atraso = Math.abs(j - antigaColuna) * 0.012f;
                float moveX = -dx * passoX;

                boolean saiPelaEsquerda = (moveX < 0 && j == 0);
                boolean saiPelaDireita = (moveX > 0 && j == Tabuleiro.COLUNAS - 1);
                boolean saiDoTabuleiro = saiPelaEsquerda || saiPelaDireita;

                animarCartaEsteiraComEntradaESaida(
                    carta,
                    moveX,
                    0f,
                    atraso,
                    duracao,
                    saiDoTabuleiro
                );
            }
        } else if (dy != 0) {
            int coluna = antigaColuna;

            for (int i = 0; i < Tabuleiro.LINHAS; i++) {
                Cartas carta = cartasVisuais[i][coluna];
                if (carta == null) continue;
                if (i == antigaLinha) continue;

                float atraso = Math.abs(i - antigaLinha) * 0.012f;

                // Se ficar invertido no seu tabuleiro, troque para -dy * passoY
                float moveY = dy * passoY;

                boolean saiPorBaixo = (moveY < 0 && i == 0);
                boolean saiPorCima = (moveY > 0 && i == Tabuleiro.LINHAS - 1);
                boolean saiDoTabuleiro = saiPorBaixo || saiPorCima;

                animarCartaEsteiraComEntradaESaida(
                    carta,
                    0f,
                    moveY,
                    atraso,
                    duracao,
                    saiDoTabuleiro
                );
            }
        }
    }

    private void animarCartaEsteiraComEntradaESaida(Cartas carta, float moveX, float moveY, float delay, float duracao, boolean cartaSaiDoTabuleiro) {

        System.out.println("ENTROU NO METODO NOVO DA ESTEIRA");
        if (carta == null) return;

        carta.clearActions();

        float inicioX = carta.getX();
        float inicioY = carta.getY();

        if (!cartaSaiDoTabuleiro) {
            carta.addAction(
                Actions.sequence(
                    Actions.delay(delay),
                    Actions.parallel(
                        Actions.moveTo(inicioX + moveX, inicioY + moveY, duracao, Interpolation.smoother),
                        Actions.sequence(
                            Actions.scaleTo(1.02f, 0.98f, duracao * 0.45f, Interpolation.fade),
                            Actions.scaleTo(1f, 1f, duracao * 0.55f, Interpolation.fade)
                        )
                    )
                )
            );
            return;
        }

        float bordaEsquerda = getCartaX(0);
        float bordaDireita = getCartaX(Tabuleiro.COLUNAS - 1);
        float bordaBaixo = getCartaY(Tabuleiro.LINHAS - 1);
        float bordaCima = getCartaY(0);

        float extraBorda = (CARTA_ALTURA + ESPACO) * 1.15f;

        // ---------------------------
// ---------------------------
// HORIZONTAL: usa carta temporária
// ---------------------------
        if (moveX != 0f) {
            float saidaX = inicioX + (moveX < 0 ? -extraBorda : extraBorda);
            float saidaY = inicioY;

            // nasce fora da borda oposta
            float entradaX = (moveX < 0)
                ? bordaDireita + extraBorda
                : bordaEsquerda - extraBorda;
            float entradaY = inicioY;

            // entra na CASA DA BORDA OPOSTA
            float destinoEntradaX = (moveX < 0)
                ? bordaDireita
                : bordaEsquerda;
            float destinoEntradaY = inicioY;

            Image cartaTemp = new Image(carta.getTexturaAtual());
            cartaTemp.setSize(carta.getWidth(), carta.getHeight());
            cartaTemp.setOrigin(Align.center);
            cartaTemp.setPosition(entradaX, entradaY);
            cartaTemp.setScale(0.90f, 0.90f);
            cartaTemp.getColor().a = 0f;
            stageTabuleiro.addActor(cartaTemp);
            cartaTemp.toFront();

            carta.addAction(
                Actions.sequence(
                    Actions.delay(delay),

                    // carta real sai
                    Actions.parallel(
                        Actions.moveTo(saidaX, saidaY, duracao * 0.46f, Interpolation.pow2In),
                        Actions.scaleTo(0.90f, 0.90f, duracao * 0.46f, Interpolation.fade),
                        Actions.fadeOut(duracao * 0.42f, Interpolation.fade)
                    ),

                    // mantém a carta real escondida até o sincronizarTabuleiroVisual()
                    Actions.run(() -> {
                        carta.setPosition(inicioX, inicioY);
                        carta.setScale(1f, 1f);
                        carta.getColor().a = 0f;
                        carta.setVisible(false);
                    })
                )
            );

            cartaTemp.addAction(
                Actions.sequence(
                    Actions.delay(delay + duracao * 0.40f),

                    // entrada espelhada indo para a BORDA CERTA
                    Actions.parallel(
                        Actions.moveTo(destinoEntradaX, destinoEntradaY, duracao * 0.54f, Interpolation.pow2Out),
                        Actions.scaleTo(1f, 1f, duracao * 0.54f, Interpolation.fade),
                        Actions.fadeIn(duracao * 0.50f, Interpolation.fade)
                    ),

                    Actions.removeActor()
                )
            );

            return;
        }

        // ---------------------------
        // VERTICAL: usa a própria carta
        // ---------------------------
        float saidaX = inicioX;
        float saidaY = inicioY + (moveY < 0 ? -extraBorda : extraBorda);

        float entradaY = (moveY < 0)
            ? bordaCima + extraBorda
            : bordaBaixo - extraBorda;

        carta.addAction(
            Actions.sequence(
                Actions.delay(delay),
                Actions.parallel(
                    Actions.moveTo(saidaX, saidaY, duracao * 0.46f, Interpolation.pow2In),
                    Actions.scaleTo(0.90f, 0.90f, duracao * 0.46f, Interpolation.fade),
                    Actions.fadeOut(duracao * 0.42f, Interpolation.fade)
                ),
                Actions.run(() -> {
                    carta.setPosition(inicioX, entradaY);
                    carta.setScale(0.90f, 0.90f);
                    carta.getColor().a = 0f;
                    carta.setVisible(true);
                }),
                Actions.parallel(
                    Actions.moveTo(inicioX, inicioY, duracao * 0.54f, Interpolation.pow2Out),
                    Actions.scaleTo(1f, 1f, duracao * 0.54f, Interpolation.fade),
                    Actions.fadeIn(duracao * 0.50f, Interpolation.fade)
                )
            )
        );
    }

    private void resetarEstadoVisualDasCartas() {
        for (int i = 0; i < Tabuleiro.LINHAS; i++) {
            for (int j = 0; j < Tabuleiro.COLUNAS; j++) {
                Cartas carta = cartasVisuais[i][j];
                if (carta == null) continue;

                carta.clearActions();
                carta.setVisible(true);
                carta.setScale(1f, 1f);
                carta.setRotation(0f);
                carta.setColor(1f, 1f, 1f, 1f);
                carta.getColor().a = 1f;
            }
        }
    }

    private void atualizarTabuleiroComAnimacao(int antigaLinha, int antigaColuna, int novaLinha, int novaColuna) {

        if (animandoTabuleiro) return;

        animarMovimentoJogadorComEsteira(antigaLinha, antigaColuna, novaLinha, novaColuna, () -> {

            tabuleiro.moverJogador(novaLinha, novaColuna);
            tabuleiro.aplicarEsteira(antigaLinha, antigaColuna, novaLinha, novaColuna);

            sincronizarTabuleiroVisual();
            resetarEstadoVisualDasCartas();
            atualizarDestaqueCartas();

            animandoTabuleiro = false;
        });
    }

    private void animarCartaMovimentoInvalido(int linha, int coluna) {
        Cartas carta = cartasVisuais[linha][coluna];
        if (carta == null) return;

        carta.toFront();

        final float xOriginal = carta.getX();
        final float yOriginal = carta.getY();

        // guarda a cor atual completa, inclusive alpha
        final Color corOriginal = new Color(carta.getColor());

        carta.addAction(
            Actions.sequence(
                Actions.parallel(
                    // deixa mais vermelha, mas preservando o alpha original
                    Actions.color(new Color(1f, 0.15f, 0.15f, corOriginal.a), 0.08f),

                    // tremida
                    Actions.sequence(
                        Actions.moveTo(xOriginal - 12f, yOriginal, 0.025f),
                        Actions.moveTo(xOriginal + 12f, yOriginal, 0.04f),
                        Actions.moveTo(xOriginal - 10f, yOriginal, 0.035f),
                        Actions.moveTo(xOriginal + 8f, yOriginal, 0.03f),
                        Actions.moveTo(xOriginal - 4f, yOriginal, 0.02f),
                        Actions.moveTo(xOriginal, yOriginal, 0.02f)
                    )
                ),

                // volta exatamente para a cor/transparência anterior
                Actions.color(corOriginal, 0.14f),

                Actions.run(() -> {
                    carta.setPosition(xOriginal, yOriginal);
                    carta.setColor(corOriginal);
                })
            )
        );
    }
}

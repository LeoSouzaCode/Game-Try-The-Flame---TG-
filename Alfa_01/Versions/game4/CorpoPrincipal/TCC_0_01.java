package com.root.game.CorpoPrincipal;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.root.game.Animacoes.AnimacaoCarta;
import com.root.game.Animacoes.AnimacaoTabuleiro;
import com.root.game.Combate.CartaInfo;
import com.root.game.Combate.Jogador;
import com.root.game.Combate.SistemaCombate;
import com.root.game.Fluxos.FluxoCarta;
import com.root.game.Fluxos.FluxoCombate;
import com.root.game.UI.HUDController;
import com.root.game.UI.PopupManager;
import com.root.game.Utils.PosicaoCartaProvider;
import com.root.game.Utils.TextureManager;

import java.util.Objects;

//Classe principal do jogo.

//RESPONSABILIDADE DESTA CLASSE:
//inicializar o jogo e os stages, criar o tabuleiro visual, orquestrar os controladores extraídos (HUD, popups, fluxo de cartas e combate);
//manter o estado global da run;
//sincronizar o grid lógico com o grid visual;
//iniciar animação de movimento/esteira.

public class TCC_0_01 extends ApplicationAdapter {

    //Stages
    private Stage stageCartaZoom;
    private Stage stageTabuleiro;
    private Stage stageUI;
    private Stage stageAnimacao;

    //UI base
    private BitmapFont fonte;
    private Label labelMensagem;
    private Skin skin;

    //Estado global do jogo
    private boolean jogoFinalizado = false;
    private boolean animandoTabuleiro = false;
    private boolean telaModalAberta = false;

    //Modelo / lógica
    private Tabuleiro tabuleiro;
    private Jogador jogadorCombate;

    //Visual do tabuleiro
    private Cartas[][] cartasVisuais;

    //Controladores / fluxos extraídos
    private AnimacaoTabuleiro animacaoTabuleiro;
    private AnimacaoCarta animacaoCarta;
    private PopupManager popupManager;
    private HUDController hudController;
    private FluxoCombate fluxoCombate;
    private FluxoCarta fluxoCarta;

    //Constantes visuais das cartas
    private static final float CARTA_LARGURA = 108;
    private static final float CARTA_ALTURA = 144;
    private static final float ESPACO = 8;

    //Informa se o jogo terminou, usado pelas cartas visuais para bloquear interação.
    public boolean isFinalizado() {
        return jogoFinalizado;
    }

    //Informa se o tabuleiro está animando, usado pelas cartas visuais para bloquear clique durante esteira/movimento.
    public boolean isAnimandoTabuleiro() {
        return animandoTabuleiro;
    }

    //Ciclo de vida
    @Override
    public void create() {

        //Criação dos stages
        stageTabuleiro = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1280, 720));
        stageUI = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1280, 720));
        stageCartaZoom = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1280, 720));
        stageAnimacao = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1280, 720));

        //Input multiplexer, Ordem importante: zoom > UI > animação > tabuleiro
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stageCartaZoom);
        multiplexer.addProcessor(stageUI);
        multiplexer.addProcessor(stageAnimacao);
        multiplexer.addProcessor(stageTabuleiro);
        Gdx.input.setInputProcessor(multiplexer);

        //Fontes / skin base
        fonte = criarFonte();
        criarUI();

        //Modelo principal
        tabuleiro = new Tabuleiro();
        jogadorCombate = new Jogador(50);
        SistemaCombate sistemaCombate = new SistemaCombate();

        //Controladores extraídos
        animacaoCarta = new AnimacaoCarta();

        popupManager = new PopupManager(stageUI, stageCartaZoom, skin);
        hudController = new HUDController(stageUI, skin);

        fluxoCombate = new FluxoCombate(
            stageCartaZoom,
            skin,
            animacaoCarta,
            popupManager,
            sistemaCombate
        );

        fluxoCarta = new FluxoCarta(
            stageCartaZoom,
            tabuleiro,
            animacaoCarta,
            popupManager
        );

        //Grid visual
        cartasVisuais = new Cartas[Tabuleiro.LINHAS][Tabuleiro.COLUNAS];
        criarTabuleiroVisual();

        //HUD
        hudController.criarHUD();
        atualizarHUDCompleto();

        //Estado visual inicial
        sincronizarTabuleiroVisual();
        atualizarDestaqueCartas();
    }

    //controla o tamanho e proporção da tela
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

    //Criação de UI base
    private void criarUI() {
        skin = new Skin();

        // Fonte padrão
        skin.add("default-font", fonte);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = fonte;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        // Estilo de janela
        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle windowStyle =
            new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle();

        windowStyle.titleFont = fonte;
        windowStyle.titleFontColor = Color.WHITE;
        windowStyle.background = criarDrawableCor(new Color(0f, 0f, 0f, 0.85f));

        skin.add("default", windowStyle);

        // Estilo de botão
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = fonte;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.GRAY;
        buttonStyle.up = criarDrawableCor(new Color(0.18f, 0.18f, 0.18f, 1f));
        buttonStyle.down = criarDrawableCor(new Color(0.10f, 0.10f, 0.10f, 1f));

        skin.add("default", buttonStyle);

        // Label de mensagem inferior
        labelMensagem = new Label("", skin);
        labelMensagem.setPosition(20, 20);
        stageUI.addActor(labelMensagem);
    }

    // TABULEIRO VISUAL
    //Cria as cartas visuais iniciais do tabuleiro, cada célula do grid lógico ganha um ator visual correspondente.
    //O jogador começa revelado na posição inicial.
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

    //Traduz o conteúdo lógico da célula em caminho de textura.
    //PRIORIDADE: se a célula é a posição do jogador, usa textura do jogador, se a carta for null, usa verso;
    //caso contrário, escolhe pela CartaInfo/TipoCarta.
    private String obterTextura(int linha, int coluna) {
        if (linha == tabuleiro.getJogadorLinha() && coluna == tabuleiro.getJogadorColuna()) {
            return "Cartas/Frente/Jogador/jogadorTeste.png";
        }

        CartaInfo cartaInfo = tabuleiro.getCartaInfo(linha, coluna);

        if (cartaInfo == null) {
            return "Cartas/Versos/versoTeste.jpg";
        }

        Tabuleiro.TipoCarta tipo = cartaInfo.getTipo();

        if (Objects.requireNonNull(tipo) == Tabuleiro.TipoCarta.INIMIGO) {
            if (cartaInfo.getInimigo() != null) {
                return cartaInfo.getInimigo().getTexturaPath();
            }
            return "Cartas/Frente/Inimigo/frenteTeste0.jpg";
        } else if (tipo == Tabuleiro.TipoCarta.BAU) {
            return "Cartas/Frente/Bau/frenteTeste7.jpg";
        } else if (tipo == Tabuleiro.TipoCarta.CHAMA) {
            return "Cartas/Frente/Chama/frenteTeste4.jpg";
        } else if (tipo == Tabuleiro.TipoCarta.PAREDE) {
            return "Cartas/Frente/Parede/paredeTeste1.png";
        } else if (tipo == Tabuleiro.TipoCarta.VAZIO) {
            return "Cartas/Versos/versoTeste.jpg";
        }
        return "Cartas/Frente/Inimigo/frenteTeste0.jpg";
    }

    // CLIQUE / REVELAÇÃO DE CARTA
    //Entrada principal de clique em carta
    //Fluxo:
    //1. bloqueia se jogo terminou / tabuleiro animando / modal aberto;
    //2. valida adjacência;
    //3. abre popup de confirmação;
    //4. delega o fluxo específico para FluxoCarta;
    //5. encaminha callbacks por tipo de evento.
    public void clicarCarta(int linha, int coluna) {
        if (jogoFinalizado || animandoTabuleiro || telaModalAberta) return;

        // Clique inválido:
        // a carta não é adjacente ao jogador.
        // Exibe mensagem e ‘feedback’ visual.
        if (!fluxoCarta.podeRevelar(
            tabuleiro.getJogadorLinha(),
            tabuleiro.getJogadorColuna(),
            linha,
            coluna
        )) {
            mostrarMensagem("Você só pode revelar cartas adjacentes.");

            if (animacaoTabuleiro != null) {
                animacaoTabuleiro.animarCartaMovimentoInvalido(linha, coluna);
            }

            return;
        }

        popupManager.mostrarConfirmacaoCarta(() -> {
            telaModalAberta = true;

            Cartas cartaOriginal = cartasVisuais[linha][coluna];

            fluxoCarta.revelarCarta(
                linha,
                coluna,
                cartaOriginal,

                // INIMIGO
                () -> {
                    Runnable acaoVoltar = () -> {
                        stageCartaZoom.clear();
                        telaModalAberta = false;
                        restaurarCartaOriginal(linha, coluna, cartaOriginal);
                        sincronizarTabuleiroVisual();
                        atualizarDestaqueCartas();
                    };

                    Runnable acaoVitoria = () -> {
                        stageCartaZoom.clear();
                        telaModalAberta = false;
                        restaurarCartaOriginal(linha, coluna, cartaOriginal);

                        tabuleiro.consumirCarta(linha, coluna);
                        moverJogadorPara(linha, coluna);

                        atualizarHUDCompleto();
                    };

                    Runnable acaoDerrota = () -> {
                        stageCartaZoom.clear();
                        telaModalAberta = false;
                        restaurarCartaOriginal(linha, coluna, cartaOriginal);

                        jogoFinalizado = true;
                        popupManager.mostrarGameOver();
                    };

                    popupManager.mostrarPopupInimigo(
                        // LUTAR
                        () -> fluxoCombate.mostrarTelaCombate(
                            tabuleiro.getCartaInfo(linha, coluna),
                            jogadorCombate,
                            linha,
                            coluna,
                            cartaOriginal,
                            acaoVoltar,
                            acaoVitoria,
                            acaoDerrota,
                            this::mostrarMensagem
                        ),

                        // SAIR
                        () -> {
                            Image cartaZoomAtual = fluxoCarta.getCartaZoomAtual();

                            Runnable finalizar = () -> {
                                stageCartaZoom.clear();
                                telaModalAberta = false;
                                restaurarCartaOriginal(linha, coluna, cartaOriginal);
                                sincronizarTabuleiroVisual();
                                atualizarDestaqueCartas();
                                mostrarMensagem("Você saiu.");
                            };

                            if (cartaZoomAtual != null) {
                                animacaoCarta.dissolverCartaZoom(cartaZoomAtual, finalizar);
                            } else {
                                finalizar.run();
                            }
                        }
                    );
                },

                // CHAMA
                () -> popupManager.mostrarPopupMensagem("Chama coletada!", () -> {
                    Image cartaZoomAtual = fluxoCarta.getCartaZoomAtual();

                    Runnable finalizar = () -> {
                        stageCartaZoom.clear();
                        telaModalAberta = false;
                        restaurarCartaOriginal(linha, coluna, cartaOriginal);
                        coletarChama(linha, coluna);
                    };

                    if (cartaZoomAtual != null) {
                        animacaoCarta.dissolverCartaZoom(cartaZoomAtual, finalizar);
                    } else {
                        finalizar.run();
                    }
                }),

                // BAÚ
                () -> popupManager.mostrarPopupMensagem("Baú encontrado!", () -> {
                    CartaInfo cartaInfo = tabuleiro.getCartaInfo(linha, coluna);

                    if (cartaInfo == null || cartaInfo.getArmaDentro() == null) {
                        popupManager.mostrarPopupMensagem("Baú vazio.", () -> {
                            Image cartaZoomAtual = fluxoCarta.getCartaZoomAtual();

                            Runnable finalizar = () -> {
                                stageCartaZoom.clear();
                                telaModalAberta = false;
                                restaurarCartaOriginal(linha, coluna, cartaOriginal);
                                sincronizarTabuleiroVisual();
                                atualizarDestaqueCartas();
                            };

                            if (cartaZoomAtual != null) {
                                animacaoCarta.dissolverCartaZoom(cartaZoomAtual, finalizar);
                            } else {
                                finalizar.run();
                            }
                        });
                        return;
                    }

                    // Flip do baú para mostrar a arma contida
                    Image cartaZoomAtual = fluxoCarta.getCartaZoomAtual();

                    if (cartaZoomAtual != null) {
                        animacaoCarta.aplicarFlip(cartaZoomAtual, () -> cartaZoomAtual.setDrawable(
                            new TextureRegionDrawable(
                                new TextureRegion(
                                    TextureManager.get(cartaInfo.getArmaDentro().getTexturaPath())
                                )
                            )
                        ));
                    }

                    // Aguarda o flip terminar antes de abrir o popup
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            boolean jogadorJaTemArma =
                                jogadorCombate != null && jogadorCombate.getArmaEquipada() != null;

                            popupManager.mostrarPopupArmaBau(
                                cartaInfo,
                                jogadorJaTemArma,

                                // EQUIPAR / TROCAR
                                () -> {
                                    Image zoomAtual = fluxoCarta.getCartaZoomAtual();

                                    Runnable finalizar = () -> {
                                        stageCartaZoom.clear();
                                        telaModalAberta = false;
                                        restaurarCartaOriginal(linha, coluna, cartaOriginal);
                                        coletarBau(linha, coluna);
                                    };

                                    if (zoomAtual != null) {
                                        animacaoCarta.dissolverCartaZoom(zoomAtual, finalizar);
                                    } else {
                                        finalizar.run();
                                    }
                                },

                                // NÃO EQUIPAR / NÃO TROCAR
                                () -> {
                                    Image zoomAtual = fluxoCarta.getCartaZoomAtual();

                                    Runnable finalizar = () -> {
                                        stageCartaZoom.clear();
                                        telaModalAberta = false;

                                        // o baú continua no tabuleiro e permanece revelado.
                                        restaurarCartaOriginal(linha, coluna, cartaOriginal);
                                        sincronizarTabuleiroVisual();
                                        atualizarDestaqueCartas();
                                        mostrarMensagem("Você deixou o item no baú.");
                                    };

                                    if (zoomAtual != null) {
                                        animacaoCarta.dissolverCartaZoom(zoomAtual, finalizar);
                                    } else {
                                        finalizar.run();
                                    }
                                }
                            );
                        }
                    }, 0.26f);
                }),

                // PAREDE
                () -> popupManager.mostrarPopupMensagem(
                    "Parede encontrada.\nNão é possível avançar.",
                    () -> {
                        Image cartaZoomAtual = fluxoCarta.getCartaZoomAtual();

                        Runnable finalizar = () -> {
                            stageCartaZoom.clear();
                            telaModalAberta = false;
                            restaurarCartaOriginal(linha, coluna, cartaOriginal);
                            sincronizarTabuleiroVisual();
                            atualizarDestaqueCartas();
                        };

                        if (cartaZoomAtual != null) {
                            animacaoCarta.dissolverCartaZoom(cartaZoomAtual, finalizar);
                        } else {
                            finalizar.run();
                        }
                    }
                ),

                // VAZIO
                () -> {
                    stageCartaZoom.clear();
                    telaModalAberta = false;
                    restaurarCartaOriginal(linha, coluna, cartaOriginal);
                    mostrarMensagem("Não há nada nesta posição.");
                },

                // RESTAURAÇÃO GENÉRICA
                () -> {
                    stageCartaZoom.clear();
                    telaModalAberta = false;
                    restaurarCartaOriginal(linha, coluna, cartaOriginal);
                },

                this::mostrarMensagem
            );
        }, null);
    }

    // FLUXOS DE COLETA / MOVIMENTO
    //Trata a coleta de chama.
    //Regras:
    //incrementa as chamas no tabuleiro, consome a carta da posição, atualiza HUD; verifica vitória; move jogador com animação/esteira.
    private void coletarChama(int linha, int coluna) {
        int antigaLinha = tabuleiro.getJogadorLinha();
        int antigaColuna = tabuleiro.getJogadorColuna();

        tabuleiro.coletarChama(linha, coluna);
        tabuleiro.consumirCarta(linha, coluna);

        atualizarHUDCompleto();

        if (tabuleiro.getChamasColetadas() >= 3) {
            jogoFinalizado = true;
            mostrarMensagem("Você venceu!");
        }

        atualizarTabuleiroComAnimacao(antigaLinha, antigaColuna, linha, coluna);
    }

    //Trata o fluxo completo do baú, se houver arma, pergunta se deseja equipar/trocar, se equipar, consome o baú e move o jogador;
    //se não equipar, mantém o baú no tabuleiro
    //Etapa final do baú:
    //equipa a arma (se houver), consome a carta e move o jogador para a posição.
    private void coletarBau(int linha, int coluna) {
        CartaInfo cartaInfo = tabuleiro.getCartaInfo(linha, coluna);

        if (cartaInfo != null && cartaInfo.getArmaDentro() != null) {
            jogadorCombate.setArmaEquipada(cartaInfo.getArmaDentro());
            mostrarMensagem("Você equipou: " + cartaInfo.getArmaDentro().getNome());
        } else {
            mostrarMensagem("Baú vazio.");
        }

        tabuleiro.consumirCarta(linha, coluna);

        atualizarHUDCompleto();

        int antigaLinha = tabuleiro.getJogadorLinha();
        int antigaColuna = tabuleiro.getJogadorColuna();

        atualizarTabuleiroComAnimacao(antigaLinha, antigaColuna, linha, coluna);
    }

    //Encapsula a movimentação do jogador para uma nova posição, sempre usando a animação de esteira.
    private void moverJogadorPara(int linha, int coluna) {
        int antigaLinha = tabuleiro.getJogadorLinha();
        int antigaColuna = tabuleiro.getJogadorColuna();

        atualizarTabuleiroComAnimacao(antigaLinha, antigaColuna, linha, coluna);
    }

    //Atualiza o HUD e reinstala o click da miniatura de arma.
    private void atualizarHUDCompleto() {
        hudController.atualizarHUD(jogadorCombate, tabuleiro.getChamasColetadas());
        hudController.setClickArmaListener(this::mostrarPopupDetalheArmaHUD);
    }

    // MENSAGENS E FONTES
    private BitmapFont criarFonte() {
        FreeTypeFontGenerator gen =
            new FreeTypeFontGenerator(Gdx.files.internal("Fonts/MorrisRomanAlternate-Black.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter p =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        p.size = 32;
        p.color = Color.WHITE;

        BitmapFont f = gen.generateFont(p);
        gen.dispose();
        return f;
    }

    //Exibe mensagem temporária no canto inferior da UI.
    private void mostrarMensagem(String texto) {
        labelMensagem.setText(texto);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                labelMensagem.setText("");
            }
        }, 2f);
    }

    // SINCRONIZAÇÃO VISUAL
    //Sincroniza todas as cartas visuais com o estado lógico do tabuleiro, este é um dos métodos mais importantes da classe.
    //Ele garante, posição correta, textura correta, reset de transformações, revelação correta, prioridade visual do jogador.
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

                if (i == jogadorLinha && j == jogadorColuna) {
                    carta.setTexturaFrente(TextureManager.get("Cartas/Frente/Jogador/jogadorTeste.png"));
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

    //Restaura uma carta visual removida temporariamente do stage durante o zoom/revelação.
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

        if (linha == tabuleiro.getJogadorLinha() && coluna == tabuleiro.getJogadorColuna()) {
            cartaOriginal.setTexturaFrente(TextureManager.get("Cartas/Frente/Jogador/jogadorTeste.png"));
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

    //Atualiza o destaque visual das cartas adjacentes ao jogador
    //jogador: cor normal
    //adjacentes: brilho pulsante
    //demais: tom reduzido/holográfico leve
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
                    carta.addAction(
                        Actions.forever(
                            Actions.sequence(
                                Actions.color(new Color(0.85f, 0.9f, 1f, 1f), 0.8f),
                                Actions.color(Color.WHITE, 0.8f)
                            )
                        )
                    );
                } else {
                    carta.setColor(0.8f, 0.85f, 1f, 0.65f);
                }
            }
        }
    }

    // POSIÇÕES VISUAIS
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

    // MOVIMENTO + ESTEIRA
    //Executa a animação de movimento do jogador com esteira.
    //A ordem lógica é:
    //1. animação visual;
    //2. mover jogador no grid;
    //3. aplicar esteira;
    //4. sincronizar visual;
    //5. resetar estado visual;
    //6. reabilitar o jogo.

    private void atualizarTabuleiroComAnimacao(int antigaLinha, int antigaColuna, int novaLinha, int novaColuna) {
        if (animandoTabuleiro) return;

        animandoTabuleiro = true;

        animacaoTabuleiro.animarMovimentoJogadorComEsteira(
            antigaLinha,
            antigaColuna,
            novaLinha,
            novaColuna,
            () -> {
                System.out.println("CALLBACK ANIMACAO - antes moverJogador");
                tabuleiro.imprimirGridDebug();

                tabuleiro.moverJogador(novaLinha, novaColuna);

                System.out.println("CALLBACK ANIMACAO - depois moverJogador");
                tabuleiro.imprimirGridDebug();

                tabuleiro.aplicarEsteira(antigaLinha, antigaColuna, novaLinha, novaColuna);

                System.out.println("CALLBACK ANIMACAO - depois aplicarEsteira");
                tabuleiro.imprimirGridDebug();

                sincronizarTabuleiroVisual();
                animacaoTabuleiro.resetarEstadoVisualDasCartas();
                atualizarDestaqueCartas();

                animandoTabuleiro = false;
            }
        );
    }

    // POPUP DE DETALHE DA ARMA
    //Exibe a arma equipada em destaque.
    //Este popup continua local porque faz parte da integração direta com o clique da miniatura da HUD.
    private void mostrarPopupDetalheArmaHUD() {
        if (jogadorCombate == null || jogadorCombate.getArmaEquipada() == null) {
            mostrarMensagem("Nenhuma arma equipada.");
            return;
        }

        telaModalAberta = true;
        stageCartaZoom.clear();

        // IMPORTANTE:
        // o PopupManager precisa expor um método público compatível.
        // Use um destes nomes no PopupManager:
        // - criarOverlayBloqueador(float alpha)
        // ou ajuste aqui conforme o nome real da sua classe.
        Image overlay = popupManager.criarOverlayBloqueador(0.75f);
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
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                stageCartaZoom.clear();
                telaModalAberta = false;
            }
        });

        stageCartaZoom.addActor(cartaArma);
        stageCartaZoom.addActor(labelInfo);
        stageCartaZoom.addActor(btnFechar);

        animacaoCarta.aplicarIdleFlutuacao(cartaArma);
    }

    // UTILS
    //Cria um drawable sólido simples para janelas e botões.
    private Drawable criarDrawableCor(Color cor) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(cor);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return new TextureRegionDrawable(new TextureRegion(texture));
    }
}

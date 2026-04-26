package com.root.game.Fluxos;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Timer;
import com.root.game.Animacoes.AnimacaoCarta;
import com.root.game.CorpoPrincipal.Cartas;
import com.root.game.Combate.CartaInfo;
import com.root.game.Combate.Inimigo;
import com.root.game.Combate.Jogador;
import com.root.game.Combate.ResultadoCombate;
import com.root.game.Combate.SistemaCombate;
import com.root.game.UI.PopupManager;
import com.root.game.Utils.TextureManager;

/**
 * Responsável por montar e controlar a tela de combate.
 *
 * Esta classe cuida apenas do fluxo visual e da resolução do combate.
 * A lógica macro do jogo continua no controlador principal.
 */
public class FluxoCombate {

    private final Stage stageCartaZoom;
    private final Skin skin;
    private final AnimacaoCarta animacaoCarta;
    private final PopupManager popupManager;
    private final SistemaCombate sistemaCombate;

    public FluxoCombate(
        Stage stageCartaZoom,
        Skin skin,
        AnimacaoCarta animacaoCarta,
        PopupManager popupManager,
        SistemaCombate sistemaCombate
    ) {
        this.stageCartaZoom = stageCartaZoom;
        this.skin = skin;
        this.animacaoCarta = animacaoCarta;
        this.popupManager = popupManager;
        this.sistemaCombate = sistemaCombate;
    }

    /**
     * Abre a tela de combate.
     *
     * Os parâmetros linha, coluna e cartaOriginal são mantidos na assinatura
     * para ficar compatível com a chamada atual da TCC_0_01, mesmo que esta
     * classe não use todos diretamente.
     */
    public void mostrarTelaCombate(
        CartaInfo cartaInfo,
        Jogador jogadorCombate,
        int linha,
        int coluna,
        Cartas cartaOriginal,
        Runnable onVoltar,
        Runnable onVitoria,
        Runnable onDerrota,
        java.util.function.Consumer<String> onMensagem
    ) {
        if (cartaInfo == null || cartaInfo.getInimigo() == null) {
            onMensagem.accept("Erro: inimigo não encontrado.");
            if (onVoltar != null) onVoltar.run();
            return;
        }

        Inimigo inimigo = cartaInfo.getInimigo();

        stageCartaZoom.clear();

        Image overlay = popupManager.criarOverlayBloqueador(0.75f);
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

        animacaoCarta.aplicarIdleFlutuacao(cartaInimigo);
        animacaoCarta.aplicarIdleFlutuacao(cartaJogador);

        Image miniArmaCombate = null;

        if (jogadorCombate.getArmaEquipada() != null) {
            miniArmaCombate = new Image(new TextureRegionDrawable(
                new TextureRegion(TextureManager.get(jogadorCombate.getArmaEquipada().getTexturaPath()))
            ));
            miniArmaCombate.setSize(70, 95);
            miniArmaCombate.setPosition(
                cartaJogador.getX() + cartaJogador.getWidth() + 5,
                cartaJogador.getY() + 5
            );
            stageCartaZoom.addActor(miniArmaCombate);
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
            montarTextoStatusJogadorCombate(
                jogadorCombate,
                jogadorCombate.getVida(),
                durabilidadeInicial
            ),
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
                if (onVoltar != null) onVoltar.run();
            }
        });

        btnFurtividade.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onMensagem.accept("Furtividade ainda não implementada.");
            }
        });

        final Image miniArmaFinal = miniArmaCombate;

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

                animacaoCarta.animarImpactoJogador(cartaJogador);

                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        if (vidaInimigoExibida[0] > 0) {
                            vidaInimigoExibida[0]--;
                            labelVidaInimigo.setText(
                                inimigo.getNome() + " - Vida: " + vidaInimigoExibida[0]
                            );
                        } else {
                            cancel();
                        }
                    }
                }, 0f, 0.08f);

                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        boolean terminouVida =
                            vidaJogadorExibida[0] <= resultado.getVidaFinalJogador();

                        boolean terminouArma =
                            durabilidadeExibida[0] <= resultado.getDurabilidadeFinalArma();

                        if (durabilidadeExibida[0] > resultado.getDurabilidadeFinalArma()) {
                            durabilidadeExibida[0]--;
                        } else if (vidaJogadorExibida[0] > resultado.getVidaFinalJogador()) {
                            vidaJogadorExibida[0]--;
                        }

                        labelVidaJogador.setText(
                            montarTextoStatusJogadorCombate(
                                jogadorCombate,
                                vidaJogadorExibida[0],
                                durabilidadeExibida[0] > 0 ? durabilidadeExibida[0] : null
                            )
                        );

                        if (terminouVida && terminouArma) {
                            cancel();

                            if (resultado.isArmaQuebrou()) {
                                animacaoCarta.animarQuebraArma(miniArmaFinal, null);
                                onMensagem.accept("Sua arma quebrou!");
                            }

                            animacaoCarta.resetarTransformacoes(cartaJogador);
                            cartaInimigo.clearActions();

                            animacaoCarta.animarDerrotaInimigo(cartaInimigo, () -> {
                                onMensagem.accept(resultado.getMensagemResultado());

                                if (resultado.isJogadorVenceu()) {
                                    if (onVitoria != null) onVitoria.run();
                                } else {
                                    if (onDerrota != null) onDerrota.run();
                                }
                            });
                        }
                    }
                }, 0.35f, 0.09f);
            }
        });

        stageCartaZoom.addActor(btnVoltar);
        stageCartaZoom.addActor(btnLutar);
        stageCartaZoom.addActor(btnFurtividade);
    }

    /**
     * Monta o texto do status do jogador durante o combate.
     */
    private String montarTextoStatusJogadorCombate(
        Jogador jogadorCombate,
        int vidaExibida,
        Integer durabilidadeExibida
    ) {
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
}

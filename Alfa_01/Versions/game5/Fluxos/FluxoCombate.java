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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.root.game.Animacoes.AnimacaoCarta;
import com.root.game.Combate.CartaInfo;
import com.root.game.Combate.Inimigo;
import com.root.game.Combate.Jogador;
import com.root.game.Combate.ResultadoCombate;
import com.root.game.Combate.SistemaCombate;
import com.root.game.UI.PopupManager;
import com.root.game.Utils.TextureManager;
import com.badlogic.gdx.scenes.scene2d.Touchable;

//Responsável por montar e controlar a tela de combate.
//Esta classe cuida apenas do fluxo visual e da resolução do combate.
//A lógica macro do jogo continua no controlador principal.
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

    //Abre a tela de combate.
    //Os parâmetros linha, coluna e cartaOriginal são mantidos na assinatura
    //para ficar compatível com a chamada atual da TCC_0_01, mesmo que esta classe não use todos diretamente.
    public void mostrarTelaCombate(
        CartaInfo cartaInfo,
        Jogador jogadorCombate,
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

        //Trava lógica do combate.
        //Mesmo que o botão receba múltiplos eventos de clique rapidamente, o combate só pode ser resolvido uma vez.
        final boolean[] combateEmAndamento = { false };

        if (jogadorCombate.getArmaEquipada() != null) {
            miniArmaCombate = new Image(new TextureRegionDrawable(
                new TextureRegion(TextureManager.get(jogadorCombate.getArmaEquipada().getTexturaPath()))
            ));

            miniArmaCombate.setSize(70, 95);
            miniArmaCombate.setOrigin(Align.center);
            miniArmaCombate.setPosition(
                cartaJogador.getX() + cartaJogador.getWidth() + 5,
                cartaJogador.getY() + 5
            );

            stageCartaZoom.addActor(miniArmaCombate);

            Image miniArmaClicavel = miniArmaCombate;

            miniArmaClicavel.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {

                    //Só permite visualizar a arma antes da resolução começar.
                    if (combateEmAndamento[0]) {
                        event.stop();
                        return;
                    }

                    popupManager.mostrarDetalheArmaEquipada(
                        jogadorCombate.getArmaEquipada().getNome(),
                        jogadorCombate.getArmaEquipada().getDurabilidade(),
                        jogadorCombate.getArmaEquipada().getTexturaPath(),
                        animacaoCarta,
                        null
                    );
                }
            });
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

                //Impede clique duplo ou múltiplos cliques durante a animação.
                if (combateEmAndamento[0]) {
                    event.stop();
                    return;
                }

                combateEmAndamento[0] = true;

                //Bloqueio visual e funcional dos botões.
                //setDisabled sozinho nem sempre impede múltiplos eventos em Scene2D, por isso usamos também Touchable.disabled.
                btnLutar.setDisabled(true);
                btnFurtividade.setDisabled(true);
                btnVoltar.setDisabled(true);

                btnLutar.setTouchable(Touchable.disabled);
                btnFurtividade.setTouchable(Touchable.disabled);
                btnVoltar.setTouchable(Touchable.disabled);
                if (miniArmaFinal != null) {
                    miniArmaFinal.setTouchable(Touchable.disabled);
                }

                ResultadoCombate resultado = sistemaCombate.resolverCombate(jogadorCombate, inimigo);

                final int vidaInicialInimigo = inimigo.getVida();
                final int vidaInicialJogador = resultado.getVidaInicialJogador();
                final int vidaFinalJogador = Math.max(0, resultado.getVidaFinalJogador());

                final int durabilidadeInicialArma = resultado.getDurabilidadeInicialArma();
                final int durabilidadeFinalArma = Math.max(0, resultado.getDurabilidadeFinalArma());

                //Calcula quanto "recurso" o jogador gastou no combate.
                //Se o jogador venceu, o inimigo deve chegar a 0.
                //Se o jogador perdeu, o inimigo só deve perder vida proporcional
                //ao quanto o jogador conseguiu resistir.
                int danoAbsorvidoPelaArma = durabilidadeInicialArma - durabilidadeFinalArma;
                int danoRecebidoPeloJogador = vidaInicialJogador - vidaFinalJogador;
                int danoTotalSustentado = danoAbsorvidoPelaArma + danoRecebidoPeloJogador;

                final int vidaFinalInimigo = resultado.isJogadorVenceu()
                    ? 0
                    : Math.max(0, vidaInicialInimigo - danoTotalSustentado);

                final int[] vidaInimigoExibida = { vidaInicialInimigo };
                final int[] vidaJogadorExibida = { vidaInicialJogador };
                final int[] durabilidadeExibida = { durabilidadeInicialArma };

                animacaoCarta.animarImpactoJogador(cartaJogador);

                //Timer único: vida do inimigo, vida do jogador e durabilidade da arma descem ao mesmo tempo.
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {

                        boolean alterouAlgo = false;

                        // Inimigo desce apenas até a vida final correta.
                        if (vidaInimigoExibida[0] > vidaFinalInimigo) {
                            vidaInimigoExibida[0]--;
                            alterouAlgo = true;
                        }

                        // Primeiro reduz a durabilidade da arma.
                        if (durabilidadeExibida[0] > durabilidadeFinalArma) {
                            durabilidadeExibida[0]--;
                            alterouAlgo = true;
                        }
                        // Depois reduz a vida do jogador.
                        else if (vidaJogadorExibida[0] > vidaFinalJogador) {
                            vidaJogadorExibida[0]--;
                            alterouAlgo = true;
                        }

                        labelVidaInimigo.setText(
                            inimigo.getNome() + " - Vida: " + vidaInimigoExibida[0]
                        );

                        labelVidaJogador.setText(
                            montarTextoStatusJogadorCombate(
                                jogadorCombate,
                                vidaJogadorExibida[0],
                                durabilidadeExibida[0] > 0 ? durabilidadeExibida[0] : null
                            )
                        );

                        //Quando nada mais precisa mudar, a animação numérica terminou.
                        if (!alterouAlgo) {
                            cancel();

                            if (resultado.isArmaQuebrou()) {
                                animacaoCarta.animarQuebraArma(miniArmaFinal, stageCartaZoom, null);
                                onMensagem.accept("Sua arma quebrou!");
                            }

                            //Se venceu, inimigo recebe animação de derrota.
                            //Se perdeu, não faz sentido animar o inimigo morrendo.
                            if (resultado.isJogadorVenceu()) {
                                animacaoCarta.resetarTransformacoes(cartaJogador);
                                cartaInimigo.clearActions();

                                animacaoCarta.animarDerrotaInimigo(cartaInimigo, stageCartaZoom, () -> {
                                    onMensagem.accept(resultado.getMensagemResultado());

                                    if (onVitoria != null) {
                                        onVitoria.run();
                                    }
                                });

                            } else {
                                onMensagem.accept(resultado.getMensagemResultado());

                                if (onDerrota != null) {
                                    onDerrota.run();
                                }
                            }
                        }
                    }
                }, 0.20f, 0.08f);
            }
        });

        stageCartaZoom.addActor(btnVoltar);
        stageCartaZoom.addActor(btnLutar);
        stageCartaZoom.addActor(btnFurtividade);
    }

    //Monta o texto do status do jogador durante o combate.
    private String montarTextoStatusJogadorCombate(Jogador jogadorCombate, int vidaExibida, Integer durabilidadeExibida) {
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

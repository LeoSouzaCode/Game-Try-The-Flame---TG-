package com.root.game.Fluxos;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.root.game.Animacoes.AnimacaoCarta;
import com.root.game.CorpoPrincipal.Cartas;
import com.root.game.Combate.CartaInfo;
import com.root.game.CorpoPrincipal.Tabuleiro;
import com.root.game.UI.PopupManager;
import com.root.game.Utils.TextureManager;

/**
 * Controla o fluxo de revelação visual da carta.
 */
public class FluxoCarta {

    private final Stage stageCartaZoom;
    private final Tabuleiro tabuleiro;
    private final AnimacaoCarta animacaoCarta;
    private final PopupManager popupManager;
    private Image cartaZoomAtual;

    public FluxoCarta(
        Stage stageCartaZoom,
        Tabuleiro tabuleiro,
        AnimacaoCarta animacaoCarta,
        PopupManager popupManager
    ) {
        this.stageCartaZoom = stageCartaZoom;
        this.tabuleiro = tabuleiro;
        this.animacaoCarta = animacaoCarta;
        this.popupManager = popupManager;
    }

    public Image getCartaZoomAtual() {
        return cartaZoomAtual;
    }

    public boolean podeRevelar(int jLinha, int jColuna, int linha, int coluna) {
        int dLinha = Math.abs(linha - jLinha);
        int dColuna = Math.abs(coluna - jColuna);
        return (dLinha + dColuna) == 1;
    }

    public void revelarCarta(
        int linha,
        int coluna,
        Cartas cartaOriginal,
        Runnable onInimigo,
        Runnable onChama,
        Runnable onBau,
        Runnable onParede,
        Runnable onVazio,
        Runnable restaurarCarta,
        java.util.function.Consumer<String> mensagem
    ) {
        Tabuleiro.TipoCarta tipo = tabuleiro.getCarta(linha, coluna);

        if (tipo == Tabuleiro.TipoCarta.VAZIO) {
            mensagem.accept("Não há nada nesta posição.");
            onVazio.run();
            return;
        }

        tabuleiro.revelarCarta(linha, coluna);
        cartaOriginal.remove();

        String textura = obterTextura(linha, coluna);
        cartaZoomAtual = criarCartaZoom(textura);

        stageCartaZoom.clear();
        stageCartaZoom.addActor(popupManager.criarOverlayBloqueador(0.65f));
        stageCartaZoom.addActor(cartaZoomAtual);

        animacaoCarta.aplicarFlip(cartaZoomAtual, () -> {
            cartaZoomAtual.setDrawable(
                new TextureRegionDrawable(
                    new TextureRegion(TextureManager.get(textura))
                )
            );
        });

        animacaoCarta.aplicarIdleFlutuacao(cartaZoomAtual);

        switch (tipo) {
            case INIMIGO:
                mensagem.accept("Inimigo encontrado!");
                onInimigo.run();
                break;

            case CHAMA:
                mensagem.accept("Chama encontrada!");
                onChama.run();
                break;

            case BAU:
                mensagem.accept("Baú encontrado!");
                onBau.run();
                break;

            case PAREDE:
                mensagem.accept("Parede encontrada.");
                onParede.run();
                break;

            case VAZIO:
            default:
                onVazio.run();
                break;
        }
    }

    private Image criarCartaZoom(String textura) {
        TextureRegion region = new TextureRegion(TextureManager.get(textura));
        Image cartaZoom = new Image(new TextureRegionDrawable(region));

        float maxLargura = 300f;
        float maxAltura = 400f;

        float escala = Math.min(
            maxLargura / region.getRegionWidth(),
            maxAltura / region.getRegionHeight()
        );

        cartaZoom.setSize(
            region.getRegionWidth() * escala,
            region.getRegionHeight() * escala
        );

        cartaZoom.setOrigin(Align.center);
        cartaZoom.setScale(0.01f);
        cartaZoom.setPosition(640 - cartaZoom.getWidth() / 2f, 360 - 120);

        return cartaZoom;
    }

    private String obterTextura(int linha, int coluna) {
        CartaInfo cartaInfo = tabuleiro.getCartaInfo(linha, coluna);

        if (cartaInfo == null) return "Cartas/Versos/versoTeste.jpg";

        switch (cartaInfo.getTipo()) {
            case INIMIGO:
                return cartaInfo.getInimigo() != null
                    ? cartaInfo.getInimigo().getTexturaPath()
                    : "Cartas/Frente/Inimigo/frenteTeste0.jpg";

            case BAU:
                return "Cartas/Frente/Bau/frenteTeste7.jpg";

            case CHAMA:
                return "Cartas/Frente/Chama/frenteTeste4.jpg";

            case PAREDE:
                return "Cartas/Frente/Parede/paredeTeste1.png";

            case VAZIO:
            default:
                return "Cartas/Versos/versoTeste.jpg";
        }
    }

}

package com.root.game.CorpoPrincipal;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.root.game.Utils.TextureManager;

//Representa a carta visual exibida no tabuleiro.

//RESPONSABILIDADE DESTA CLASSE:
//desenhar a carta no Stage;
//saber a sua posição lógica atual no grid (linha/coluna);
//encaminhar eventos de hover e clique para o controlador principal;
//alternar entre verso e frente com base no estado visual "revelada".

//IMPORTANTE:
//Esta classe NÃO decide regra de jogo.
//Ela apenas delega o clique para TCC_0_01, que é quem valida:
//se o jogo terminou;
//se o tabuleiro está a animar;
//se a carta é adjacente;
//qual é o evento da carta clicada.

public class Cartas extends Actor {

    private Texture frente; //Textura exibida quando a carta está revelada
    private final Texture verso; //Textura exibida quando a carta está virada

    private int linha; //Linha na esteira da carta dentro da matriz do tabuleiro
    private int coluna; //Coluna na esteira da carta dentro da matriz do tabuleiro

    private boolean revelada = false; //Estado visual: true = mostra frente; false = mostra verso
    private boolean bloqueandoAnimacaoClique = false; // Bloqueia interação da carta durante animações específicas.

    public static final float LARGURA = 108; //Largura padrão visual da carta no tabuleiro
    public static final float ALTURA = 144; //Altura padrão visual da carta no tabuleiro

    /**
     * Cria uma carta visual.
     * @param frentePath textura da frente
     * @param versoPath textura do verso
     * @param x posição X inicial no stage
     * @param y posição Y inicial no stage
     * @param linha linha lógica inicial no grid
     * @param coluna coluna lógica inicial no grid
     * @param jogo controlador principal
     */

    public Cartas(String frentePath, String versoPath, float x, float y, int linha, int coluna, TCC_0_01 jogo) {

        this.frente = TextureManager.get(frentePath);
        this.verso = TextureManager.get(versoPath);
        //this.jogo = jogo;
        this.linha = linha;
        this.coluna = coluna;

        // Define área clicável e tamanho visual da carta.
        setBounds(x, y, LARGURA, ALTURA);

        // Origem central para escalas e rotações ficarem visualmente corretas.
        setOrigin(Align.center);

        addListener(new ClickListener() {

            //Ao entrar com o mouse/toque virtual sobre a carta, ignora se o jogo terminou;
            //ignora se o tabuleiro está animando, ignora se a carta está bloqueada;
            //aplica o hover.
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (jogo.isFinalizado() || jogo.isAnimandoTabuleiro() || bloqueandoAnimacaoClique) return;

                clearActions();
                addAction(Actions.scaleTo(1.1f, 1.1f, 0.12f, Interpolation.fade));
                toFront();
            }

            //Ao sair da carta:
            //volta à escala normal, exceto se houver bloqueio de animação.
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (jogo.isAnimandoTabuleiro() || bloqueandoAnimacaoClique) return;

                clearActions();
                addAction(Actions.scaleTo(1f, 1f, 0.12f, Interpolation.fade));
            }

            //Clique simples a classe não resolve regra nenhuma, apenas informa ao controlador principal qual posição lógica foi clicada.
            @Override
            public void clicked(InputEvent event, float x, float y) {
                jogo.clicarCarta(Cartas.this.linha, Cartas.this.coluna);
            }
        });
    }

    //Desenha a carta no stage.
    //Se revelada = true, desenha a textura da frente. Caso contrário, desenha o verso.
    //O draw respeita posição, origem, escala, rotação, alpha herdado do parent
    @Override
    public void draw(Batch batch, float parentAlpha) {

        Texture tex = revelada ? frente : verso;
        if (tex == null) return;

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        batch.draw(
            tex,
            getX(), getY(),
            getOriginX(), getOriginY(),
            getWidth(), getHeight(),
            getScaleX(), getScaleY(),
            getRotation(),
            0, 0,
            tex.getWidth(), tex.getHeight(),
            false, false
        );

        // Restaura cor padrão do batch para não contaminar outros atores.
        batch.setColor(1f, 1f, 1f, 1f);
    }

    //Atualiza a posição lógica da carta na matriz.
    //Deve ser chamado sempre que a referência visual passar a representar outra célula.
    public void setPosicaoGrid(int linha, int coluna) {
        this.linha = linha;
        this.coluna = coluna;
    }

    //Define se visualmente a carta está revelada.
    //Isso não altera a lógica da CartaInfo; apenas o ator visual.
    public void setRevelada(boolean estado) {
        revelada = estado;
    }

    //Troca a textura da frente da carta.
    //Útil quando a carta visual precisa representar outro conteúdo após sincronização.
    public void setTexturaFrente(Texture textura) {
        this.frente = textura;
    }

    //Retorna a textura atualmente exibida.
    public Texture getTexturaAtual() {
        return revelada ? frente : verso;
    }

    //Ativa/desativa o bloqueio de clique durante animações específicas.
    public void setBloqueandoAnimacaoClique(boolean bloqueando) {
        this.bloqueandoAnimacaoClique = bloqueando;
    }
}

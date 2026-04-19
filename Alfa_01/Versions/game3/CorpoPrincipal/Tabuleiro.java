package com.root.game.CorpoPrincipal;

import com.root.game.Combate.CartaInfo;
import com.root.game.Combate.Arma;
import com.root.game.Combate.EstadoCarta;
import com.root.game.Combate.Inimigo;
import com.root.game.Combate.CatalogoArmas;
import com.root.game.Combate.CatalogoInimigos;

public class Tabuleiro {

    public static final int LINHAS = 4;
    public static final int COLUNAS = 5;

    private CartaInfo[][] grid;

    private int jogadorLinha = 0;
    private int jogadorColuna = 0;

    private int chamasColetadas = 0;

    public Tabuleiro() {
        grid = new CartaInfo[LINHAS][COLUNAS];
        inicializar();
    } //faz com que o tabuleiro exista, cria o grid

    public enum TipoCarta {
        INIMIGO,
        BAU,
        CHAMA,
        PAREDE,
        VAZIO
    }

    private void inicializar() {
        grid[0][0] = criarCarta(gerarTipoCartaAleatoria());

        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {

                if (i == 0 && j == 0) continue;

                if (i == 0 && j == 1) {
                    grid[i][j] = criarCarta(TipoCarta.INIMIGO);
                    continue;
                }

                if (i == 1 && j == 0) {
                    grid[i][j] = criarCarta(TipoCarta.INIMIGO);
                    continue;
                }

                grid[i][j] = criarCarta(gerarTipoCartaAleatoria());
            }
        }

        garantirUmaChama();

    }

    private CartaInfo criarCarta(TipoCarta tipo) {
        CartaInfo carta = new CartaInfo(tipo);

        if (tipo == TipoCarta.INIMIGO) {
            carta.setInimigo(gerarInimigoAleatorio());
        }

        if (tipo == TipoCarta.BAU) {
            carta.setArmaDentro(gerarArmaAleatoria());
        }

        return carta;
    }

    private Inimigo gerarInimigoAleatorio() {
        return CatalogoInimigos.gerarInimigoAleatorio();
    }

    private Arma gerarArmaAleatoria() {
        return CatalogoArmas.gerarArmaAleatoria();
    }

    private TipoCarta gerarTipoCartaAleatoria() {

        int r = (int)(Math.random() * 100);

        if (r >= 10 && r < 30) {
            return TipoCarta.BAU;
        }

        if (r >= 30 && r < 40) {
            return TipoCarta.PAREDE;
        }

        return TipoCarta.INIMIGO;
    } //gera aleatoriamente cartas no tabuleiro

    public boolean podeMover(int novaLinha, int novaColuna) {

        if (novaLinha < 0 || novaLinha >= LINHAS ||
            novaColuna < 0 || novaColuna >= COLUNAS) {
            return false;
        }

        int dx = Math.abs(novaColuna - jogadorColuna);
        int dy = Math.abs(novaLinha - jogadorLinha);

        if ((dx + dy) != 1) {
            return false;
        }

        if (getTipoSeguro(novaLinha, novaColuna) == TipoCarta.PAREDE) {
            return false;
        }

        return true;
    }

    public TipoCarta getCarta(int linha, int coluna) {
        return getTipoSeguro(linha, coluna);
    }

    private TipoCarta getTipoSeguro(int linha, int coluna) {
        if (grid[linha][coluna] == null) {
            return TipoCarta.VAZIO;
        }
        return grid[linha][coluna].getTipo();
    }

    public CartaInfo getCartaInfo(int linha, int coluna) {
        return grid[linha][coluna];
    }

    public int getJogadorLinha() {
        return jogadorLinha;
    }

    public int getJogadorColuna() {
        return jogadorColuna;
    }

    public int getChamasColetadas() {
        return chamasColetadas;
    }

    public void imprimirGrid() {
        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                System.out.print(getTipoSeguro(i, j) + "\t");
            }
            System.out.println();
        }
    }

    public TipoCarta moverJogador(int novaLinha, int novaColuna) {

        if (!podeMover(novaLinha, novaColuna))
            return null;

        TipoCarta tipoDestino = getTipoSeguro(novaLinha, novaColuna);

        jogadorLinha = novaLinha;
        jogadorColuna = novaColuna;

        return tipoDestino;
    }

    public int[] gerarNovaChamaUnica() {

        for (int tentativas = 0; tentativas < 300; tentativas++) {

            int l = (int)(Math.random() * LINHAS);
            int c = (int)(Math.random() * COLUNAS);

            if (l == jogadorLinha && c == jogadorColuna) continue;
            if (getTipoSeguro(l, c) == TipoCarta.PAREDE) continue;
            if (getTipoSeguro(l, c) == TipoCarta.CHAMA) continue;

            grid[l][c] = criarCarta(TipoCarta.CHAMA);
            return new int[]{l, c};
        }

        return null;
    }

    public void imprimirGridDebug() {
        System.out.println("------ GRID ------");
        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                System.out.print(getTipoSeguro(i, j) + "\t");
            }
            System.out.println();
        }
        System.out.println("------------------");
    }

    public int[] coletarChama(int linha, int coluna) {

        chamasColetadas++;

        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                if (getTipoSeguro(i, j) == TipoCarta.CHAMA) {
                    grid[i][j] = gerarCartaEsteiraSegura();
                }
            }
        }

        garantirUmaChama();

        return null;
    }

    public void aplicarEsteira(int antigaLinha, int antigaColuna, int novaLinha, int novaColuna) {

        int dx = novaColuna - antigaColuna;
        int dy = novaLinha - antigaLinha;

        if (dx == 1) { // jogador foi para a direita
            esteiraEsquerda(antigaLinha);
        }
        else if (dx == -1) { // jogador foi para a esquerda
            esteiraDireita(antigaLinha);
        }
        else if (dy == 1) { // jogador foi para baixo
            esteiraCima(antigaColuna);
        }
        else if (dy == -1) { // jogador foi para cima
            esteiraBaixo(antigaColuna);
        }

        preencherNulos();

        if (!existeMovimentoValido()) {
            gerarSaidaEmergencial();
        }

        if (!existeChama()) {
            garantirUmaChama();
        }
    }

    private boolean existeChama() {

        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                if (getTipoSeguro(i, j) == TipoCarta.CHAMA) {
                    return true;
                }
            }
        }

        return false;
    }

    private void esteiraDireita(int linha) {

        CartaInfo ultima = gerarCartaEsteiraSegura();

        for (int c = COLUNAS - 1; c > 0; c--) {
            grid[linha][c] = grid[linha][c - 1];
        }

        grid[linha][0] = ultima;
    }

    private void esteiraEsquerda(int linha) {

        CartaInfo primeira = gerarCartaEsteiraSegura();

        for (int c = 0; c < COLUNAS - 1; c++) {
            grid[linha][c] = grid[linha][c + 1];
        }

        grid[linha][COLUNAS - 1] = primeira;
    }

    private void esteiraBaixo(int coluna) {

        CartaInfo ultima = gerarCartaEsteiraSegura();

        for (int l = LINHAS - 1; l > 0; l--) {
            grid[l][coluna] = grid[l - 1][coluna];
        }

        grid[0][coluna] = ultima;
    }

    private void esteiraCima(int coluna) {

        CartaInfo primeira = gerarCartaEsteiraSegura();

        for (int l = 0; l < LINHAS - 1; l++) {
            grid[l][coluna] = grid[l + 1][coluna];
        }

        grid[LINHAS - 1][coluna] = primeira;
    }

    private CartaInfo gerarCartaEsteiraSegura() {

        TipoCarta tipo;

        do {
            tipo = gerarTipoCartaAleatoria();
        }
        while (tipo == TipoCarta.CHAMA);

        return criarCarta(tipo);
    }

    public boolean existeMovimentoValido() {

        int l = jogadorLinha;
        int c = jogadorColuna;

        if (l > 0 && getTipoSeguro(l - 1, c) != TipoCarta.PAREDE) return true;
        if (l < LINHAS - 1 && getTipoSeguro(l + 1, c) != TipoCarta.PAREDE) return true;
        if (c > 0 && getTipoSeguro(l, c - 1) != TipoCarta.PAREDE) return true;
        if (c < COLUNAS - 1 && getTipoSeguro(l, c + 1) != TipoCarta.PAREDE) return true;

        return false;
    }

    private void gerarSaidaEmergencial() {

        int l = jogadorLinha;
        int c = jogadorColuna;

        if (l > 0) grid[l - 1][c] = criarCarta(TipoCarta.INIMIGO);
        else if (l < LINHAS - 1) grid[l + 1][c] = criarCarta(TipoCarta.INIMIGO);
        else if (c > 0) grid[l][c - 1] = criarCarta(TipoCarta.INIMIGO);
        else if (c < COLUNAS - 1) grid[l][c + 1] = criarCarta(TipoCarta.INIMIGO);
    }

    private void garantirUmaChama() {

        int quantidade = 0;

        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                if (getTipoSeguro(i, j) == TipoCarta.CHAMA) {
                    quantidade++;
                }
            }
        }

        if (quantidade == 0) {
            gerarNovaChamaUnica();
            return;
        }

        if (quantidade > 1) {
            boolean manteveUma = false;

            for (int i = 0; i < LINHAS; i++) {
                for (int j = 0; j < COLUNAS; j++) {
                    if (getTipoSeguro(i, j) == TipoCarta.CHAMA) {
                        if (!manteveUma) {
                            manteveUma = true;
                        } else {
                            grid[i][j] = gerarCartaEsteiraSegura();
                        }
                    }
                }
            }
        }
    }

    public void revelarCarta(int linha, int coluna) {
        CartaInfo carta = grid[linha][coluna];
        if (carta != null) {
            carta.setEstado(EstadoCarta.REVELADA);
        }
    }

    public boolean cartaEstaRevelada(int linha, int coluna) {
        CartaInfo carta = grid[linha][coluna];
        return carta != null && carta.getEstado() == EstadoCarta.REVELADA;
    }

    public void esconderCarta(int linha, int coluna) {
        CartaInfo carta = grid[linha][coluna];
        if (carta != null) {
            carta.setEstado(EstadoCarta.FECHADA);
        }
    }

    public void consumirCarta(int linha, int coluna) {
        grid[linha][coluna] = null;
    }

    private void preencherNulos() {
        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                if (grid[i][j] == null) {
                    grid[i][j] = gerarCartaEsteiraSegura();
                    grid[i][j].setEstado(EstadoCarta.FECHADA);
                }
            }
        }
    }

}

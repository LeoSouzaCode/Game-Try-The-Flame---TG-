package com.root.game.CorpoPrincipal;

public class Tabuleiro {

    public static final int LINHAS = 4;
    public static final int COLUNAS = 5;

    private TipoCarta[][] grid;

    private int jogadorLinha = 0;
    private int jogadorColuna = 0;

    private int bausAtivos = 0;
    private int chamasColetadas = 0;

    public Tabuleiro() {
        grid = new TipoCarta[LINHAS][COLUNAS];
        inicializar();
    } //faz com que o tabuleiro exista, cria o grid

    public enum TipoCarta {
        INIMIGO,
        BAU,
        CHAMA,
        PAREDE
    }

    private void inicializar() {
        grid[0][0] = gerarCartaAleatoria();

        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {

                if (i == 0 && j == 0) continue;

                if (i == 0 && j == 1) {
                    grid[i][j] = TipoCarta.INIMIGO;
                    continue;
                }

                if (i == 1 && j == 0) {
                    grid[i][j] = TipoCarta.INIMIGO;
                    continue;
                }

                grid[i][j] = gerarCartaAleatoria();
            }
        }

        garantirUmaChama();

    }

    private TipoCarta gerarCartaAleatoria() {

        int r = (int)(Math.random() * 100); //chances 100%

        if (bausAtivos < 3 && r >= 10 && r < 30) {
            bausAtivos++;
            return TipoCarta.BAU;
        } //se tiver menos que 3 baus + 20% de chances de gerar bau

        if (r >= 30 && r < 40) {
            return TipoCarta.PAREDE;
        } //10% de chances de gerar uma parede

        return TipoCarta.INIMIGO; //70% de chances de gerar inimigo
    } //gera aleatoriamente cartas no tabuleiro

    public boolean podeMover(int novaLinha, int novaColuna) {

        // fora do tabuleiro
        if (novaLinha < 0 || novaLinha >= LINHAS ||
            novaColuna < 0 || novaColuna >= COLUNAS) {
            return false;
        }

        int dx = Math.abs(novaColuna - jogadorColuna);
        int dy = Math.abs(novaLinha - jogadorLinha);

        // só permite 1 casa ortogonal
        if ((dx + dy) != 1) {
            return false;
        }

        // bloqueia parede
        if (grid[novaLinha][novaColuna] == TipoCarta.PAREDE) {
            return false;
        }

        return true;
    }

    public TipoCarta getCarta(int linha, int coluna) {
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

    public void imprimir() {
        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                System.out.print(grid[i][j] + "\t");
            }
            System.out.println();
        }
    }

    public TipoCarta moverJogador(int novaLinha, int novaColuna) {

        if (!podeMover(novaLinha, novaColuna))
            return null;

        TipoCarta tipoDestino = grid[novaLinha][novaColuna];

        jogadorLinha = novaLinha;
        jogadorColuna = novaColuna;

        return tipoDestino;
    }

    public int[] gerarNovaChamaUnica() {

        for (int tentativas = 0; tentativas < 300; tentativas++) {

            int l = (int)(Math.random() * LINHAS);
            int c = (int)(Math.random() * COLUNAS);

            if (l == jogadorLinha && c == jogadorColuna) continue;
            if (grid[l][c] == TipoCarta.PAREDE) continue;
            if (grid[l][c] == TipoCarta.CHAMA) continue;

            grid[l][c] = TipoCarta.CHAMA;
            return new int[]{l, c};
        }

        return null;
    }

    public void debugImprimirGrid() {
        System.out.println("------ GRID ------");
        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                if (i == jogadorLinha && j == jogadorColuna) {
                    System.out.print("JOGADOR\t");
                } else {
                    System.out.print(grid[i][j] + "\t");
                }
            }
            System.out.println();
        }
        System.out.println("------------------");
    }

    public int[] coletarChama(int linha, int coluna) {

        chamasColetadas++;

        // remove qualquer chama existente
        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                if (grid[i][j] == TipoCarta.CHAMA) {
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

        if (dx == 1) { // direita
            esteiraDireita(novaLinha, novaColuna);
        }
        else if (dx == -1) { // esquerda
            esteiraEsquerda(novaLinha, novaColuna);
        }
        else if (dy == 1) { // baixo
            esteiraBaixo(novaLinha, novaColuna);
        }
        else if (dy == -1) { // cima
            esteiraCima(novaLinha, novaColuna);
        }

        if (!existeMovimentoValido()) {
            gerarSaidaEmergencial();
        }

// garante que sempre exista uma chama
        if (!existeChama()) {
            garantirUmaChama();
        }
    }

    private boolean existeChama() {

        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                if (grid[i][j] == TipoCarta.CHAMA) {
                    return true;
                }
            }
        }

        return false;
    }

    private void esteiraDireita(int linha, int jogadorColuna) {

        for (int c = jogadorColuna; c < COLUNAS - 1; c++) {
            grid[linha][c] = grid[linha][c + 1];
        }

        grid[linha][COLUNAS - 1] = gerarCartaEsteiraSegura();
    }

    private void esteiraEsquerda(int linha, int jogadorColuna) {

        for (int c = jogadorColuna; c > 0; c--) {
            grid[linha][c] = grid[linha][c - 1];
        }

        grid[linha][0] = gerarCartaEsteiraSegura();
    }

    private void esteiraBaixo(int jogadorLinha, int coluna) {

        for (int l = jogadorLinha; l < LINHAS - 1; l++) {
            grid[l][coluna] = grid[l + 1][coluna];
        }

        grid[LINHAS - 1][coluna] = gerarCartaEsteiraSegura();
    }

    private void esteiraCima(int jogadorLinha, int coluna) {

        for (int l = jogadorLinha; l > 0; l--) {
            grid[l][coluna] = grid[l - 1][coluna];
        }

        grid[0][coluna] = gerarCartaEsteiraSegura();
    }

    private TipoCarta gerarCartaEsteiraSegura() {

        TipoCarta carta;

        do {
            carta = gerarCartaAleatoria();
        }
        while (carta == TipoCarta.CHAMA);

        return carta;
    }

    public boolean existeMovimentoValido() {

        int l = jogadorLinha;
        int c = jogadorColuna;

        if (l > 0 && grid[l - 1][c] != TipoCarta.PAREDE) return true;
        if (l < LINHAS - 1 && grid[l + 1][c] != TipoCarta.PAREDE) return true;
        if (c > 0 && grid[l][c - 1] != TipoCarta.PAREDE) return true;
        if (c < COLUNAS - 1 && grid[l][c + 1] != TipoCarta.PAREDE) return true;

        return false;
    }

    private void gerarSaidaEmergencial() {

        int l = jogadorLinha;
        int c = jogadorColuna;

        if (l > 0) grid[l - 1][c] = TipoCarta.INIMIGO;
        else if (l < LINHAS - 1) grid[l + 1][c] = TipoCarta.INIMIGO;
        else if (c > 0) grid[l][c - 1] = TipoCarta.INIMIGO;
        else if (c < COLUNAS - 1) grid[l][c + 1] = TipoCarta.INIMIGO;
    }

    private void garantirUmaChama() {

        int quantidade = 0;
        int ultimaLinha = -1;
        int ultimaColuna = -1;

        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                if (grid[i][j] == TipoCarta.CHAMA) {
                    quantidade++;
                    ultimaLinha = i;
                    ultimaColuna = j;
                }
            }
        }

        // se não existir nenhuma, cria uma
        if (quantidade == 0) {
            gerarNovaChamaUnica();
            return;
        }

        // se existir mais de uma, mantém só uma
        if (quantidade > 1) {
            boolean manteveUma = false;

            for (int i = 0; i < LINHAS; i++) {
                for (int j = 0; j < COLUNAS; j++) {
                    if (grid[i][j] == TipoCarta.CHAMA) {
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

}

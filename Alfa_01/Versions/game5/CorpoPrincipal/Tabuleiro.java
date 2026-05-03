package com.root.game.CorpoPrincipal;

import com.root.game.Combate.Arma;
import com.root.game.Combate.CartaInfo;
import com.root.game.Combate.CatalogoArmas;
import com.root.game.Combate.CatalogoInimigos;
import com.root.game.Combate.EstadoCarta;
import com.root.game.Combate.Inimigo;

//Classe responsável pela lógica do tabuleiro.

//RESPONSABILIDADES:
//manter o grid de CartaInfo, manter posição do jogador, gerar cartas aleatórias, garantir existência de apenas uma CHAMA;
//aplicar a mecânica de esteira, controlar revelação/consumo das cartas.

//IMPORTANTE PARA ESTE PROJETO:
//o grid pode conter null temporariamente;
//consultas de tipo devem ser feitas com getTipoSeguro(), a esteira deve preservar os mesmos objetos CartaInfo sempre que possível;
//idealmente, apenas uma nova carta deve entrar por movimento.

public class Tabuleiro {

    public static final int LINHAS = 4; //Quantidade de linhas do grid lógico.
    public static final int COLUNAS = 5; //Quantidade de colunas do grid lógico.

    //Grid principal do jogo.
    //Cada posição pode conter uma CartaInfo válida, null temporariamente, durante transições ou após consumo.
    private final CartaInfo[][] grid;

    private int jogadorLinha = 0; //Linha atual do jogador no grid
    private int jogadorColuna = 0; //Coluna atual do jogador no grid
    private int chamasColetadas = 0; //Quantidade de chamas coletadas na run atual.

    //Tipos de carta usados no sistema.
    //VAZIO é um tipo interno de segurança para tratar null no grid.
    public enum TipoCarta {
        INIMIGO,
        BAU,
        CHAMA,
        PAREDE,
        VAZIO
    }

    //Construtor do tabuleiro, cria o grid e inicializa o conteúdo inicial
    public Tabuleiro() {
        grid = new CartaInfo[LINHAS][COLUNAS];
        inicializar();
    }

    //Preenche o tabuleiro inicial.

    //Regras atuais:
    //o jogador começa em (0,0);
    //as posições (0,1) e (1,0) são forçadas como INIMIGO, o restante é aleatório;
    //ao final, garante ao menos uma CHAMA no tabuleiro.
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

    //Cria uma CartaInfo conforme o tipo.
    //Regras:
    //INIMIGO recebe um inimigo aleatório, BAU recebe uma arma aleatória, outros tipos ficam apenas com o seu tipo base.
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

    //Puxa um inimigo aleatório ao catálogo.
    private Inimigo gerarInimigoAleatorio() {
        return CatalogoInimigos.gerarInimigoAleatorio();
    }

    //Puxa uma arma aleatória ao catálogo
    private Arma gerarArmaAleatoria() {
        return CatalogoArmas.gerarArmaAleatoria();
    }

    //Chances/porcentagem de geração de um tipo aleatório para cartas normais do tabuleiro
    //Distribuição atual:
    //10..29 → BAU
    //30..39 → PAREDE
    //restante → INIMIGO
    //CHAMA não sai daqui, ela é controlada separadamente.
    private TipoCarta gerarTipoCartaAleatoria() {

        int r = (int) (Math.random() * 100);

        // Faixa de chance de baú: 20%
        // Porém só gera se ainda houver menos de 3 no tabuleiro.
        if (r >= 10 && r < 30) {
            if (contarBausNoTabuleiro() < 3) {
                return TipoCarta.BAU;
            }
        }

        // Faixa de chance de parede: 10%
        if (r >= 30 && r < 40) {
            return TipoCarta.PAREDE;
        }

        return TipoCarta.INIMIGO;
    }

    //Verifica se o jogador pode se mover para a posição informada.
    //Regras:
    //deve estar dentro do tabuleiro, deve ser adjacente ortogonalmente, não pode ser PAREDE.
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

    //Consulta pública do tipo de carta numa posição, usa a versão segura para proteger contra null.
    public TipoCarta getCarta(int linha, int coluna) {
        return getTipoSeguro(linha, coluna);
    }

    //Consulta segura do tipo da posição.
    //REGRA FUNDAMENTAL DO PROJETO:
    //se a posição estiver null, ela é tratada como VAZIO.
    public TipoCarta getTipoSeguro(int linha, int coluna) {
        if (grid[linha][coluna] == null) {
            return TipoCarta.VAZIO;
        }
        return grid[linha][coluna].getTipo();
    }

    //Retorna a referência bruta de CartaInfo na posição. Pode ser null
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

    //Atualiza a posição do jogador.
    //IMPORTANTE:
    //Apenas move o jogador lógicamente
    //Ele não consome carta, não revela carta e não aplica a esteira.
    public void moverJogador(int novaLinha, int novaColuna) {
        if (!podeMover(novaLinha, novaColuna))
            return;

        getTipoSeguro(novaLinha, novaColuna);

        jogadorLinha = novaLinha;
        jogadorColuna = novaColuna;

    }

    //Gera uma única chama em posição aleatória válida.
    //Restrições:
    //não pode nascer na posição do jogador, não pode nascer em PAREDE, não pode sobrepor outra CHAMA, não pode nascer em carta já REVELADA.
    public void gerarNovaChamaUnica() {

        for (int tentativas = 0; tentativas < 300; tentativas++) {

            int l = (int) (Math.random() * LINHAS);
            int c = (int) (Math.random() * COLUNAS);

            if (l == jogadorLinha && c == jogadorColuna) continue;
            if (getTipoSeguro(l, c) == TipoCarta.PAREDE) continue;
            if (getTipoSeguro(l, c) == TipoCarta.CHAMA) continue;
            if (cartaEstaRevelada(l, c)) continue;

            grid[l][c] = criarCarta(TipoCarta.CHAMA);
            return;
        }

    }

    //Debug expandido do grid, uso para saber se a esteira esta se comportando de forma correta com os meus movimentos
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

    //Trata a coleta da chama.
    //Comportamento atual: incrementa contador, remove qualquer CHAMA existente no grid substituindo por carta segura;
    //garante exatamente uma nova CHAMA depois.
    public void coletarChama(int ignoredLinha, int ignoredColuna) {
        chamasColetadas++;

        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                if (getTipoSeguro(i, j) == TipoCarta.CHAMA) {
                    grid[i][j] = gerarCartaEsteiraSegura();
                }
            }
        }

        garantirUmaChama();

    }

    //Aplica o efeito de esteira após o movimento do jogador.
    //Regras atuais:
    //se o jogador foi para a direita, a linha anda para a esquerda;
    //se foi para a esquerda, a linha anda para a direita;
    //se foi para baixo, a coluna sobe;
    //se foi para cima, a coluna desce.
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

        // ATENÇÃO:
        // isto funciona como "corretivo", mas quebra a filosofia da esteira controlando exatamente qual carta entra no tabuleiro.
        preencherNulos();

        if (!existeMovimentoValido()) {
            gerarSaidaEmergencial();
        }

        if (!existeChama()) {
            garantirUmaChama();
        }
    }

    //Verifica se existe alguma CHAMA no tabuleiro
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

    //Desloca uma linha para a direita uma nova carta entra pela esquerda.
    private void esteiraDireita(int linha) {
        CartaInfo ultima = gerarCartaEsteiraSegura();

        for (int c = COLUNAS - 1; c > 0; c--) {
            grid[linha][c] = grid[linha][c - 1];
        }

        grid[linha][0] = ultima;
    }

    //Desloca uma linha para a esquerda e uma nova carta entra pela direita.
    private void esteiraEsquerda(int linha) {
        CartaInfo primeira = gerarCartaEsteiraSegura();

        for (int c = 0; c < COLUNAS - 1; c++) {
            grid[linha][c] = grid[linha][c + 1];
        }

        grid[linha][COLUNAS - 1] = primeira;
    }

    //Desloca uma coluna para baixo uma nova carta entra por cima.
    private void esteiraBaixo(int coluna) {
        CartaInfo ultima = gerarCartaEsteiraSegura();

        for (int l = LINHAS - 1; l > 0; l--) {
            grid[l][coluna] = grid[l - 1][coluna];
        }

        grid[0][coluna] = ultima;
    }

    //Desloca uma coluna para cima uma nova carta entra por baixo.
    private void esteiraCima(int coluna) {
        CartaInfo primeira = gerarCartaEsteiraSegura();

        for (int l = 0; l < LINHAS - 1; l++) {
            grid[l][coluna] = grid[l + 1][coluna];
        }

        grid[LINHAS - 1][coluna] = primeira;
    }

    //Gera uma carta válida para entrar pela esteira.
    //CHAMA é proibida aqui, porque a presença de CHAMA é controlada separadamente por garantirUmaChama().
    private CartaInfo gerarCartaEsteiraSegura() {
        TipoCarta tipo;

        do {
            tipo = gerarTipoCartaAleatoria();
        }
        while (tipo == TipoCarta.CHAMA);

        return criarCarta(tipo);
    }

    //Verifica se o jogador possui ao menos um movimento ortogonal disponível.
    public boolean existeMovimentoValido() {
        int l = jogadorLinha;
        int c = jogadorColuna;

        if (l > 0 && getTipoSeguro(l - 1, c) != TipoCarta.PAREDE) return true;
        if (l < LINHAS - 1 && getTipoSeguro(l + 1, c) != TipoCarta.PAREDE) return true;
        if (c > 0 && getTipoSeguro(l, c - 1) != TipoCarta.PAREDE) return true;
        if (c < COLUNAS - 1 && getTipoSeguro(l, c + 1) != TipoCarta.PAREDE) return true;

        return false;
    }

    //Se o jogador ficar sem saída válida força a criação de uma carta acessível nesse caso um INIMIGO.
    private void gerarSaidaEmergencial() {
        int l = jogadorLinha;
        int c = jogadorColuna;

        if (l > 0) grid[l - 1][c] = criarCarta(TipoCarta.INIMIGO);
        else grid[l + 1][c] = criarCarta(TipoCarta.INIMIGO);
    }

    //Garante a regra de exatamente uma CHAMA ativa, se não houver nenhuma, cria uma, se houver mais de uma, mantém apenas a primeira encontrada.
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

    //Marca a carta como revelada no modelo lógico.
    public void revelarCarta(int linha, int coluna) {
        CartaInfo carta = grid[linha][coluna];
        if (carta != null) {
            carta.setEstado(EstadoCarta.REVELADA);
        }
    }

    //Consulta se a carta está revelada no modelo lógico.
    public boolean cartaEstaRevelada(int linha, int coluna) {
        CartaInfo carta = grid[linha][coluna];
        return carta != null && carta.getEstado() == EstadoCarta.REVELADA;
    }

    //Remove a carta da posição.
    public void consumirCarta(int linha, int coluna) {
        grid[linha][coluna] = null;
    }

    //PREENCHIMENTO GLOBAL DE NULOS
    //Recompõe qualquer posição null do grid com uma nova carta fechada.
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

    //Conta quantos baús existem atualmente no tabuleiro
    private int contarBausNoTabuleiro() {
        int quantidade = 0;

        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                if (getTipoSeguro(i, j) == TipoCarta.BAU) {
                    quantidade++;
                }
            }
        }

        return quantidade;
    }
}

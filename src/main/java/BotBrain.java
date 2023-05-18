public class BotBrain {

    private void initializeBoard(char[][] board, int boardSize) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = ' ';
            }
        }
    }

    private char[][] board;

    private char mySymbol = 'X';

    private char enemySymbol = 'O';
    public BotBrain(int boardsize) {
        board = new char[boardsize][boardsize];
        initializeBoard(board,boardsize);
    }

    public void receiveEnemyMove(int x, int y) {
        board[x][y] = enemySymbol;
    }

    public void receiveMyMove(int x, int y) {
        board[x][y] = mySymbol;
    }
    static int test = 0;
    public Player.Move getBestMove(){
        return new Player.Move(test++,0);

    }
}

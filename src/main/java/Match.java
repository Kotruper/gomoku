import java.util.ArrayList;
import java.util.List;

public class Match implements Runnable {
    private class SlotFilledException extends Exception{public SlotFilledException(String errMsg){super(errMsg);}}
    private Player p1 = null, p2 = null;
    private int boardSize;
    private char[][] board;
    private int turn = 1;
    private int inARow;
    private int maxMoves;

    public Match(Player player1, int boardSize){
        this.p1 = player1;
        this.boardSize = boardSize;
        this.board = new char[boardSize][boardSize];
        inARow = boardToRowAmount(boardSize);
        maxMoves = boardSize * boardSize;
        initializeBoard(board);
        p1.setSymbol('X');
    }

    private void initializeBoard(char[][] board) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = ' ';
            }
        }
    }

    @Override
    public void run(){
        while (p2 == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Player currentPlayer;
        Player.Move lastMove = null;
        do{
            currentPlayer = getCurrentPlayer();
            try {
                lastMove = getMove(currentPlayer);
                updateBoardState(lastMove, currentPlayer.getSymbol());
                turn++;
                p1.sendBoardState(board,p2);
                p2.sendBoardState(board,p1);
            }catch (SlotFilledException e){
                currentPlayer.sendErr(e);
            }catch (IndexOutOfBoundsException e){
                currentPlayer.sendErr(e);
            } catch (InterruptedException e) {
                currentPlayer.sendErr(e);
            }

            }while(!hasGameEnded(lastMove));
        System.out.println(currentPlayer.getName()+" has won!");
        //return currentPlayer;
    }

    public void playerJoin(Player p2){
        this.p2 = p2;
        this.p2.setSymbol('O');
    }

    private Player.Move getMove(Player currentPlayer) throws InterruptedException {
        Player.Move lastMove;
        do {
            lastMove = currentPlayer.getMove();
            Thread.sleep(100);
        }while (lastMove == null);
        currentPlayer.lastMove = null;
        return lastMove;
    }

    private Player getCurrentPlayer() {
        if(turn % 2 == 1){
            return p1;
        }
        else {
            return p2;
        }
    }
    private void updateBoardState(Player.Move pos, char symbol) throws SlotFilledException {
        if (outOfBounds(pos.X, pos.Y)) {
            throw new IndexOutOfBoundsException("Selected position is outside of the board:[" + pos.X + "," + pos.Y + "]!");
        } else if (board[pos.X][pos.Y] != ' '){
            throw new SlotFilledException("Selected position already has a symbol:["+board[pos.X][pos.Y]+"]!");
        } else {
            board[pos.X][pos.Y] = symbol;
        }
    }
    private boolean hasGameEnded(Player.Move pos){
        if(pos == null){
            return false;
        }
        if (outOfBounds(pos.X, pos.Y))
            return false;
        char symbol = board[pos.X][pos.Y];
        if (symbol == ' ')
            return false;
        /* obsługa remisu?
        if (turn == maxTurn){
            return true;
        }
        */
        boolean gameEnded = checkLine(1, 0, symbol, pos) || checkLine(0, 1, symbol, pos) || checkLine(1, 1, symbol, pos) || checkLine(1, -1, symbol, pos);
        return gameEnded;
    }

    private boolean checkLine(int xShift, int yShift, char symbol, Player.Move pos){
        int countNInARow = 0;
        int startX = pos.X - inARow * xShift;
        int startY = pos.Y - inARow * yShift;

        for (int i = 0; i < (inARow * 2) - 1; i++) {
            int checkX = startX + xShift * i, checkY = startY + yShift * i;
            if (outOfBounds(checkX,checkY)){
                countNInARow = 0;
            }else {
                if (board[checkX][checkY] == symbol)
                    countNInARow ++;
                else
                    countNInARow = 0;
            }
            if(countNInARow == inARow)
                return true;
        }
        return false;
    }
    private boolean outOfBounds(int x, int y){
        if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) {
            return true;
        }else {
            return false;
        }
    }
    private int boardToRowAmount(int size){
        if (size <= 3){
            return 3;
        }else if(size <= 10){
            return 4;
        }else
            return 5;
    }
}

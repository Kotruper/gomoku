import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import javax.print.Doc;

public class Match extends Thread {
    public String getGameName() {
        return gameName;
    }

    private class SlotFilledException extends Exception{public SlotFilledException(String errMsg){super(errMsg);}}
    private Player p1, p2 = null;

    private String gameName;
    private int boardSize;
    private char[][] board;
    private int turn = 1;
    private int inARow;
    private int maxMoves;

    public Match(int boardSize, String gameName){
        this.boardSize = boardSize;
        this.gameName = gameName;
        this.board = new char[boardSize][boardSize];
        inARow = boardToRowAmount(boardSize);
        maxMoves = boardSize * boardSize;
        initializeBoard(board);
    }

    public Match(Player player1, int boardSize, String gameName){
        playerJoin(player1);
        this.gameName = gameName;
        this.boardSize = boardSize;
        this.board = new char[boardSize][boardSize];
        inARow = boardToRowAmount(boardSize);
        maxMoves = boardSize * boardSize;
        initializeBoard(board);
    }

    public Player getP1(){
        return p1;
    }
    public Player getP2(){
        return p2;
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
        while (p1 == null || p1.ses == null || p2 == null || p2.ses == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
        p1.sendMatchInfo(p2, boardSize);
        p2.sendMatchInfo(p1, boardSize);
        Player currentPlayer;
        Player.Move lastMove = null;
        do{
            currentPlayer = getCurrentPlayer();
            try {
                lastMove = getMove(currentPlayer);
                updateBoardState(lastMove, currentPlayer.getSymbol());
                turn++;
                p1.sendMove(lastMove, currentPlayer.getSymbol());
                p2.sendMove(lastMove, currentPlayer.getSymbol());
            }catch (SlotFilledException e){
                currentPlayer.sendErr("This spot is already occupied!");
            }catch (IndexOutOfBoundsException e){
                currentPlayer.sendErr("This spot is out of bounds!");
            } catch (InterruptedException e) {
                return;
                //getOtherPlayer(currentPlayer).sendErr("The other player has left!"); //Do nothing? maybe some database stuff, I dunno
            }

        }while(!hasGameEnded(lastMove));
        Boolean isDraw = (turn == maxMoves);
        if(isDraw == false) {
            Server.dbCollection.updateOne(new Document("Username", currentPlayer.getName()), Updates.set("Wins", (int) ((Document) Server.dbCollection.find(new Document("Username", currentPlayer.getName())).first()).get("Wins") + 1));
            ScoreTable.broadcastUpdatedScores();
        }
        p1.sendMatchResult(currentPlayer, isDraw);
        p2.sendMatchResult(currentPlayer, isDraw);  //TODO: send results to a database
    }

    public void playerJoin(Player p){
        if (p1 == null){
            this.p1 = p;
            this.p1.setSymbol('X');
        }else{
            this.p2 = p;
            this.p2.setSymbol('O');
        }
    }
    public void playerLeaves(Player p){
        getOtherPlayer(p).sendErr("The other player has left!");
    }
    private Player getOtherPlayer(Player p){
        if (p == p1)
            return p2;
        else
            return p1;
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
        if (turn == maxMoves) {
            return true;
        }
        return checkLine(1, 0, symbol, pos) || checkLine(0, 1, symbol, pos) || checkLine(1, 1, symbol, pos) || checkLine(1, -1, symbol, pos);
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
        return x < 0 || x >= boardSize || y < 0 || y >= boardSize;
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

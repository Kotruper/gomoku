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
        maxMoves = boardSize * boardSize + 1;
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
    public int getSize(){
        return boardSize;
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
        while (p1 == null || p1.ready == false || p2 == null || p2.ready == false ){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
        p1.sendMatchInfo(p2, boardSize, inARow);
        p2.sendMatchInfo(p1, boardSize, inARow);
        Player currentPlayer;
        Player.Move lastMove = null;
        Line winningLine = null;
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
            winningLine = hasGameEnded(lastMove);
        }while(winningLine == null);
        Boolean isDraw = (turn == maxMoves);
        if(isDraw == false) {
            if (currentPlayer.ses != null){
                Server.dbCollection.updateOne(new Document("Username", currentPlayer.getName()), Updates.set("Wins", (int) ((Document) Server.dbCollection.find(new Document("Username", currentPlayer.getName())).first()).get("Wins") + 1));
            }
            ScoreTable.broadcastUpdatedScores();
        }
        p1.sendMatchResult(currentPlayer, isDraw, winningLine);
        p2.sendMatchResult(currentPlayer, isDraw, winningLine);  //TODO: send results to a database
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
        Player other = getOtherPlayer(p);
        if (other != null)
            other.sendErr("The other player has left!");
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
    private Line hasGameEnded(Player.Move pos){
        if(pos == null){
            return null;
        }
        if (outOfBounds(pos.X, pos.Y))
            return null;
        char symbol = board[pos.X][pos.Y];
        if (symbol == ' ')
            return null;
        if (turn == maxMoves) {
            return new Line();
        }
        Line winningLine = checkLine(1, 0, symbol, pos);
        if(winningLine == null) winningLine = checkLine(0, 1, symbol, pos);
        if(winningLine == null) winningLine = checkLine(1, 1, symbol, pos);
        if(winningLine == null) winningLine = checkLine(1, -1, symbol, pos);
        return winningLine;
    }

    private Line checkLine(int xShift, int yShift, char symbol, Player.Move pos){
        int countNInARow = 0;
        int startX = pos.X - (inARow-1) * xShift;
        int startY = pos.Y - (inARow-1) * yShift;

        Line winningLine = new Line();

        for (int i = 0; i < (inARow * 2) - 1; i++) {
            int checkX = startX + xShift * i, checkY = startY + yShift * i;
            if (outOfBounds(checkX,checkY)){
                countNInARow = 0;
            }else {
                if (board[checkX][checkY] == symbol) {
                    countNInARow++;
                    if (!winningLine.hasStart) {
                        winningLine.setXY1(checkX, checkY);
                    }
                    winningLine.setXY2(checkX, checkY);
                }
                else{
                    countNInARow = 0;
                    winningLine.setXY1(-1,-1);
                }

            }
            if(countNInARow == inARow)
                return winningLine;
        }
        return null;
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

    public class Line{
        public int X1, Y1, X2, Y2;
        public Boolean hasStart = false;
        Line(int x1, int y1, int x2, int y2){
            X1 = x1; Y1 = y1; X2 = x2; Y2 = y2;
        }
        Line(){
            X1 = -1; Y1 = -1; X2 = -1; Y2 = -1;
        }
        public void setXY1(int x, int y){
            X1 = x;
            Y1 = y;
            if(x<0 || y<0){
                hasStart = false;
            }
            else
                hasStart = true;
        }
        public void setXY2(int x, int y){
            X2 = x;
            Y2 = y;
        }

        @Override
        public String toString() {
            return "(["+X1+","+Y1+"],["+X2+","+Y2+"])";
        }
    }
}

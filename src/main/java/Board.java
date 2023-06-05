import java.util.ArrayList;
import java.util.Arrays;

public class Board {
    private char[][] boardMatrix; // " ", "X", "O"
    private Player.Move lastMove = null;

    private void initializeBoard(int boardSize) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                boardMatrix[i][j] = ' ';
            }
        }
    }

    public Board(int boardSize) {
        boardMatrix = new char[boardSize][boardSize];
        initializeBoard(boardSize);
    }
    // Fake copy constructor (only copies the boardMatrix)
    public Board(Board board) {
        char[][] matrixToCopy = board.getBoardMatrix();
        boardMatrix = new char[matrixToCopy.length][matrixToCopy.length];
        for(int i=0;i<matrixToCopy.length; i++) {
            for(int j=0; j<matrixToCopy.length; j++) {
                boardMatrix[i][j] = matrixToCopy[i][j];
            }
        }
        this.lastMove = board.lastMove;
    }
    public int getBoardSize() {
        return boardMatrix.length;
    }
    public void removeStoneNoGUI(int posX, int posY){
        boardMatrix[posY][posX] = ' ';
    }
    public void removeStoneNoGUI(Player.Move move){
        boardMatrix[move.Y][move.X] = ' ';
    }
    public void addStoneNoGUI(int posX, int posY, char symbol) {
        boardMatrix[posY][posX] = symbol;
    }
    public void addStoneNoGUI(Player.Move move, char symbol) {
        addStoneNoGUI(move.X, move.Y,symbol);
    }
    public boolean addStone(int posX, int posY, char symbol) {
        // Check whether the cell is empty or not
        if(boardMatrix[posY][posX] != ' ') return false;

        lastMove = new Player.Move(posX,posY);
        boardMatrix[posY][posX] = symbol;
        return true;
    }
    public ArrayList<Player.Move> generateMoves() {
        ArrayList<Player.Move> moveList = new ArrayList<Player.Move>();

        int boardSize = boardMatrix.length;

        // Look for cells that has at least one stone in an adjacent cell.
        for(int i=0; i<boardSize; i++) {
            for(int j=0; j<boardSize; j++) {

                if(boardMatrix[i][j] != ' ') continue;

                if(i > 0) {
                    if(j > 0) {
                        if(boardMatrix[i-1][j-1] > 0 ||
                                boardMatrix[i][j-1] > 0) {
                            Player.Move move = new Player.Move(j,i);
                            moveList.add(move);
                            continue;
                        }
                    }
                    if(j < boardSize-1) {
                        if(boardMatrix[i-1][j+1] > 0 ||
                                boardMatrix[i][j+1] > 0) {
                            Player.Move move = new Player.Move(j,i);
                            moveList.add(move);
                            continue;
                        }
                    }
                    if(boardMatrix[i-1][j] > 0) {
                        Player.Move move = new Player.Move(j,i);
                        moveList.add(move);
                        continue;
                    }
                }
                if( i < boardSize-1) {
                    if(j > 0) {
                        if(boardMatrix[i+1][j-1] > 0 ||
                                boardMatrix[i][j-1] > 0) {
                            Player.Move move = new Player.Move(j,i);
                            moveList.add(move);
                            continue;
                        }
                    }
                    if(j < boardSize-1) {
                        if(boardMatrix[i+1][j+1] > 0 ||
                                boardMatrix[i][j+1] > 0) {
                            Player.Move move = new Player.Move(j,i);
                            moveList.add(move);
                            continue;
                        }
                    }
                    if(boardMatrix[i+1][j] > 0) {
                        Player.Move move = new Player.Move(j,i);
                        moveList.add(move);
                        continue;
                    }
                }

            }
        }
        moveList.sort((o1, o2) -> o1.cartDist(lastMove) - o2.cartDist(lastMove));
        return moveList;

    }

    public String key(){
        //Byte[] packed = new Byte[(getBoardSize()*getBoardSize()/4 + 8)];
        //Arrays.fill(packed,(byte)0);
        String key = "";
        for (char[] row : boardMatrix){
            for (char symbol: row){
                key+=symbol;
            }
        }
        /*
        for (int rowI=0; rowI<boardMatrix.length; rowI++){
            for (int colI=0; colI<boardMatrix.length; colI++){
                byte adding = 0;
                int index = rowI * boardMatrix.length + colI;
                int subIndex = (index % 4) * 2;
                int uberIndex = Math.floorDiv(index,4);
                switch (boardMatrix[colI][rowI]){
                    case ' ':
                        adding = 0;
                        break;
                    case 'X':
                        adding = 1;
                        break;
                    case 'O':
                        adding = 2;
                        break;
                }
                packed[uberIndex] = (byte) (packed[uberIndex] | (byte) (adding<<subIndex));
            }
        }
         */
        return key;
    }
    public char[][] getBoardMatrix() {
        return boardMatrix;
    }

}
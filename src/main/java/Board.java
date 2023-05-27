import java.util.ArrayList;

public class Board {
    private char[][] boardMatrix; // " ", "X", "O"

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

        return moveList;

    }
    public char[][] getBoardMatrix() {
        return boardMatrix;
    }

}
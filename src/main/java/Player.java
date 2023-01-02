import org.eclipse.jetty.websocket.api.Session;

import java.net.Socket;
import java.util.Scanner;

public class Player {
    Move lastMove = null;

    public void setSession(Session s){this.ses = s;}
    Session ses = null;
    public String getName() {
        return name;
    }
    private String name;
    private char symbol;
    public Player(String name){
        this.name = name;
    }
    public void setSymbol(char x) {
        symbol = x;
    }

    //public boolean ready = false;

    public void sendBoardState(char[][] board, Player other) throws InterruptedException {
        String message = "";
        for (char[] row : board){
            message = (String.valueOf(row)+"\n");
            Server.sendMessage(this,other,message);
            Thread.sleep(10);
        }

    }

    public void setLastMove(int x, int y){
        lastMove = new Move(x,y);
    }

    public void oldGetMove() {
        System.out.println(name+", twoja kolej!");
        Scanner input = new Scanner(System.in);
        int x = input.nextInt();
        int y = input.nextInt();
        lastMove = new Move(x,y);
    }

    public Move getMove() {
        //oldGetMove(); //Temp
        return lastMove;
    }
    public char getSymbol() {
        return symbol;
    }

    public void sendErr(Exception e) {
        System.out.println(e.toString());
    }

    private void displayMessage(String message){

    }

    public static class Move {
        public int X;
        public int Y;
        public Move(int x, int y){
            this.X=x;
            this.Y=y;
        }
    }
}

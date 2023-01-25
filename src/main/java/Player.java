import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;

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
/*
    public void sendBoardState(char[][] board, Player receiver) throws InterruptedException {
        String message = "";
        for (char[] row : board){
            message = (String.valueOf(row)+"\n");
            Server.sendMessage(this,receiver,message);
            Thread.sleep(10);
        }
    }
*/
    public void sendMove(Move move, char symbol) throws InterruptedException {
        JSONObject message = new JSONObject()
                .put("type","game_move")
                .put("x",move.X)
                .put("y",move.Y)
                .put("symbol",symbol);
        Server.sendMessage(this, message);
        Thread.sleep(10);
    }

    public void setLastMove(int x, int y){
        lastMove = new Move(x,y);
    }

    public Move getMove() {
        //oldGetMove(); //Temp
        return lastMove;
    }
    public char getSymbol() {
        return symbol;
    }

    public void sendErr(String error) {
        JSONObject message = new JSONObject()
                .put("type","game_error")
                .put("value",error);
        Server.sendMessage(this, message);
    }
    public void sendMatchInfo(Player other, int boardsize) {
        JSONObject message = new JSONObject()
                .put("type","game_start")
                .put("yourname",this.name)
                .put("othername",other.name)
                .put("symbol",this.symbol)
                .put("roomSize", boardsize);
        Server.sendMessage(this, message);
    }
    public void sendMatchResult(Player winner, Boolean draw) {
        JSONObject message = new JSONObject()
                .put("type","game_end")
                .put("isDraw",draw)
                .put("winner", winner.name);
        Server.sendMessage(this, message);
    }
    public void sendMessage(Player receiver, String message) {
        JSONObject messageJson = new JSONObject()
                .put("type","chat")
                .put("sender", this.name)
                .put("message",message);
        //Server.sendMessage(this, messageJson); //Locally show message, do in JS
        Server.sendMessage(receiver,messageJson);
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

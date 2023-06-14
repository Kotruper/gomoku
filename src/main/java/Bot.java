import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;

public class Bot extends Player{

    private BotBrain brain;
    public Bot(String name){
        super(name);
        this.ready = true;
    }

    public void sendMove(Move move, char symbol) throws InterruptedException {
        if(symbol != getSymbol()){
            brain.receiveEnemyMove(move.X, move.Y);
        }
        Thread.sleep(10);
    }

    @Override
    public Move getMove() {
        lastMove = brain.getBestMove();
        return lastMove;
    }

    public void sendErr(String error) {

    }
    public void sendMatchInfo(Player other, int boardsize) {
        this.brain = new BotBrain(boardsize);
    }

    @Override
    public void sendMatchResult(Player winner, Boolean draw, Match.Line line) {
    }

}

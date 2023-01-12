import org.eclipse.jetty.websocket.api.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameController {   //TODO: join from game list, join game by ID
    private static Match emptyGame = null;

    //public static Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();

    public static void joinAvailableGame(Player p) {
        Match game = null;
        if (emptyGame == null){
            game = new Match(p, 15); //AAAAAAAAAAA
            emptyGame = game;
            Thread t = new Thread(game);
            t.start();
        }
        else {
            emptyGame.playerJoin(p);
            emptyGame = null;
        }
    }
}

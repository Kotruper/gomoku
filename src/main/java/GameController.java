import org.eclipse.jetty.websocket.api.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameController {   //TODO: join from game list, join game by ID
    private static Match emptyGame = null;
    public static Map<Player, Match> playerMatchMap = new ConcurrentHashMap<>();

    public static void joinAvailableGame(Player p) {
        Match game = null;
        if (emptyGame == null){
            game = new Match(p, 15); //AAAAAAAAAAA
            emptyGame = game;
            game.start();
            playerMatchMap.put(p, game);
        }
        else {
            emptyGame.playerJoin(p);
            playerMatchMap.put(p, emptyGame);
            emptyGame = null;
        }
    }

    public static void playerLeftGame(Player p){
        Match game = playerMatchMap.get(p);
        game.playerLeaves(p);
        game.interrupt();
        playerMatchMap.remove(game.getP1());
        playerMatchMap.remove(game.getP2());
    }
}

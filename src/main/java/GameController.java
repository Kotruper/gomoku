import org.eclipse.jetty.websocket.api.Session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameController {   //TODO: join from game list, join game by ID
    public static Map<Player, Match> playerMatchMap = new ConcurrentHashMap<>();

    public static void joinAvailableGame(Player p) { //change this
        Match emptyGame = getEmptyGame();
        if (emptyGame == null){
            createGame(p,3);
        }
        else {
            joinGame(p,emptyGame);
        }
    }

    public static void joinGame(Player p, Match game){
        game.playerJoin(p);
        playerMatchMap.put(p, game);
    }
    public static Match getGameById(long id){
        for(Match game : playerMatchMap.values()){
            if (game.getId() == id){
                return game;
            }
        }
        return null;
    }
    public static Collection<Match> getGamesList(){
        Collection<Match> games = playerMatchMap.values();
        Set<Match> set = new HashSet<>(games.size());
        games.removeIf(p -> !set.add(p));
        return games;
    }

    private static Match getEmptyGame(){
        for(Match game : getGamesList()){
            if(game.getP2() == null)
                return game;
        }
        return null;
    }


    public static void createGame(Player p, int boardSize){
        Match game = new Match(p, boardSize);
        game.start();
        playerMatchMap.put(p, game);
    }

    public static void playerLeftGame(Player p){
        Match game = playerMatchMap.get(p);
        game.playerLeaves(p);
        game.interrupt();
        playerMatchMap.remove(game.getP1());
        playerMatchMap.remove(game.getP2());
    }
}

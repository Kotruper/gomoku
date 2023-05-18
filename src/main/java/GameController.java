import org.eclipse.jetty.websocket.api.Session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameController {   //TODO: join from game list, join game by ID
    public static Map<Player, Match> playerMatchMap = new ConcurrentHashMap<>();

    private static int defaultRoomNumber = 1;
    public static void joinAvailableGame(Player p) { //change this
        Match emptyGame = getEmptyGame();
        if (emptyGame == null){
            Match game = createGame(p,15, "Room #"+defaultRoomNumber++);
            //Bot stuff
            Bot bocik = new Bot("BOT");
            joinGame(bocik,game);
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


    public static Match createGame(Player p, int boardSize, String gameName){
        Match game = new Match(p, boardSize, gameName);
        game.start();
        playerMatchMap.put(p, game);
        //Bot Stuff
        Bot bocik = new Bot("BOT");
        joinGame(bocik,game);
        return game;
    }

    public static void playerLeftGame(Player p){
        Match game = playerMatchMap.get(p);
        game.playerLeaves(p);
        game.interrupt();
        playerMatchMap.remove(game.getP1());
        playerMatchMap.remove(game.getP2());
    }
}

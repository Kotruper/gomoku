import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.http.HttpStatus;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;
import spark.*;

import javax.swing.text.html.HTMLDocument;

import static j2html.TagCreator.*;
import static spark.Spark.*;

public class Server {
    static Map<String, Player> sessionIDPlayerMap = new ConcurrentHashMap<>();
    public static MongoClient mongoClient;
    public static MongoDatabase database;
    public static MongoCollection dbCollection;

    public static void main(String[] args) throws UnknownHostException {

        mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://dbAdmin:MuyZV2OGxcAgXXQ7@cluster0.d177qaw.mongodb.net/?retryWrites=true&w=majority"));
        database = mongoClient.getDatabase("DB_java");
        dbCollection = database.getCollection("Users");

        staticFiles.location("/public"); //index.html is served at localhost:4567 (default port)
        staticFiles.expireTime(1);
        webSocket("/socket", GameWebSocketHandler.class);
        webSocket("/scores", ScoresWebSocketHandler.class);
        init();
        get("/join",(req,res)->{
            if(ensureUserIsLoggedIn(req,res)){
                String user = req.session().attribute("currentUser");
                Player newPlayer = new Player(user);
                sessionIDPlayerMap.put(req.session().id(), newPlayer);
                GameController.joinAvailableGame(newPlayer);
                res.redirect("/game.html");
            }
            return res;
        });
        post("/post/login",(req,res)->{
            String username = req.queryParams("username");
            String password = req.queryParams("password");
            if(UserController.authenticate(username, password)){
                req.session(true).attribute("currentUser",username);
                res.cookie("currentUser", username);
                res.redirect("/index.html");
            }
            else{
                res.redirect("/login.html");    //TODO how to respond with errors
                res.status(HttpStatus.UNAUTHORIZED_401);
            }
            return res;
        });
        post("/post/register",(req,res)->{
            String username = req.queryParams("username");
            String password = req.queryParams("password");
            if(UserController.createUser(username, password)){
                req.session(true).attribute("currentUser",username);
                res.cookie("currentUser", username);
                res.redirect("/index.html");
            }
            else{
                res.redirect("/login.html"); //TODO how to respond with errors
                res.status(HttpStatus.BAD_REQUEST_400);
            }
            return res;
        });
        get("/logout",(req,res)->{
            req.session().removeAttribute("currentUser");
            res.removeCookie("currentUser");
            sessionIDPlayerMap.remove(req.session().id());
            res.redirect("index.html");
            return res;
        });
        get("/createGame/:size/:gamename",(req,res)->{ //TODO Dodać obsługę tworzenia gry z nazwą
            if(ensureUserIsLoggedIn(req,res)){
                String user = req.session().attribute("currentUser");
                Player newPlayer = new Player(user);
                sessionIDPlayerMap.put(req.session().id(), newPlayer);

                int boardSize = Integer.decode(req.params("size"));
                String gameName = req.params("gamename");
                gameName = java.net.URLDecoder.decode(gameName, StandardCharsets.UTF_8);
                GameController.createGame(newPlayer,boardSize, gameName);
                res.redirect("/game.html");
            }
            return res;
        });
        get("/join/:id",(req,res)->{
            int gameId = Integer.decode(req.params("id"));
            Match game = GameController.getGameById(gameId);

            if(ensureUserIsLoggedIn(req,res) && game != null){
                String user = req.session().attribute("currentUser");
                Player newPlayer = new Player(user);
                sessionIDPlayerMap.put(req.session().id(), newPlayer);

                GameController.joinGame(newPlayer, game);
                res.redirect("/game.html");
            }
            return res;
        });
        get("/get/gameList",(req,res)->{
            JSONObject message = new JSONObject();
            Collection<Match> games = GameController.getGamesList();
            for (Match game : games){
                JSONObject gameInfo = new JSONObject()
                        .put("id",game.getId())
                        .put("player1", game.getP1() != null ? game.getP1().getName() : null)
                        .put("player2", game.getP2() != null ? game.getP2().getName() : null)
                        .put("gamename", game.getGameName());
                message.append("game",gameInfo);
            }
            return String.valueOf(message);
        });
    }

    public static Boolean ensureUserIsLoggedIn(Request request, Response response) {
        if (request.session().attribute("currentUser") == null) {
            request.session().attribute("loginRedirect", request.pathInfo());
            response.redirect("/login.html");
            return false;
        }
        else
            return true;
    }
    public static void sendMessage(Player receiver, JSONObject messageJson) {
        try {
            receiver.ses.getRemote().sendString(String.valueOf(messageJson));
        }catch (WebSocketException e){

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String createHtmlMessage(String message) {
        return article(
                b("Server message:"),
                span(attrs(".timestamp"), new SimpleDateFormat("HH:mm:ss").format(new Date())),
                p(message)
        ).render();
    }
}

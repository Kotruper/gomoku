import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;
import spark.*;

import static j2html.TagCreator.*;
import static spark.Spark.*;

public class Server {
    static Map<String, Player> sessionIDPlayerMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        staticFiles.location("/public"); //index.html is served at localhost:4567 (default port)
        staticFiles.expireTime(600);
        webSocket("/socket", GameWebSocketHandler.class);
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
                res.body("No such user found.");    //TODO how to respond with errors
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
                res.body("Username is taken."); //TODO how to respond with errors
            }
            return res;
        });
        post("/post/logout",(req,res)->{
            req.session().removeAttribute("currentUser");
            res.removeCookie("currentUser");
            res.redirect("index.html");
            return res;
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

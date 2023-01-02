import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        post("/post/username",(req,res)->{
            String user = req.queryParamOrDefault("username","UsernameNotFound");
            Player newPlayer = new Player(user);
            req.session(true).attribute("currentUser",user);
            sessionIDPlayerMap.put(req.session().id(), newPlayer);
            GameController.joinAvailableGame(newPlayer);
            res.redirect("/game.html");
            return res;
        });
    }

    public static void ensureUserIsLoggedIn(Request request, Response response) {
        if (request.session().attribute("currentUser") == null) {
            request.session().attribute("loginRedirect", request.pathInfo());
            response.redirect("/login");
        }
    }

    public static void sendMessage(Player sender, Player receiver, String message) {

        try {
            receiver.ses.getRemote().sendString(String.valueOf(new JSONObject()
                    .put("userMessage", createHtmlMessageFromSender(sender.getName(), message))
                    .put("userlist", List.of(sender.getName(),receiver.getName()))
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String createHtmlMessageFromSender(String sender, String message) {
        return article(
                b(sender + " says:"),
                span(attrs(".timestamp"), new SimpleDateFormat("HH:mm:ss").format(new Date())),
                p(message)
        ).render();
    }
}

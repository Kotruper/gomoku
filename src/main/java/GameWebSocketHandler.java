import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;

import java.net.HttpCookie;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class GameWebSocketHandler {

    static Map<Session, Player> sessionPlayerMap = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        String id = idFromSession(user);
        Player p = Server.sessionIDPlayerMap.get(id);
        sessionPlayerMap.put(user,p);
        p.setSession(user);
        System.out.println(p.getName());
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        Player p = sessionPlayerMap.get(user);
        GameController.playerLeftGame(p);
        sessionPlayerMap.remove(user);
    } //TODO: close match on leave

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {   //Use JSON for this
        JSONObject messageJSON = new JSONObject(message);
        int x = messageJSON.getInt("x");
        int y = messageJSON.getInt("y");
        Player p = sessionPlayerMap.get(user);
        //int x = message.charAt(0)-'0';
        //int y = message.charAt(1)-'0';
        p.setLastMove(x,y);
    }

    static private String idFromSession(Session user) {
        for (HttpCookie cookie : user.getUpgradeRequest().getCookies()) { //JSON???
            if (cookie.getName().equals("JSESSIONID")) {
                String id = cookie.getValue();
                return id.substring(0,id.length()-6);
            }
        }
        return null;
    }
}

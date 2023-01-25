import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.util.concurrent.ConcurrentLinkedQueue;

@WebSocket
public class ScoresWebSocketHandler {

    static ConcurrentLinkedQueue<Session> sessions = new ConcurrentLinkedQueue<Session>();

    @OnWebSocketConnect
    public void onConnect(Session ses) throws Exception {
        sessions.add(ses);
        ScoreTable.sendScores(ses);
    }

    @OnWebSocketClose
    public void onClose(Session ses, int statusCode, String reason) {
        sessions.remove(ses);
    }

}

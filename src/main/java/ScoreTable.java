
import org.bson.Document;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ScoreTable {
    public ScoreTable(){}

    private static JSONObject getScores(){
        JSONObject jsonScores = new JSONObject();
        ArrayList<Document> users = new ArrayList<>();
        Server.dbCollection.find().into(users);
        for (Document user : users){
            JSONObject userScore = new JSONObject()
                    .put("username",user.get("Username"))
                    .put("score",user.get("Wins"));
            jsonScores.append("entry",userScore);
        }
        return jsonScores;
    }
    public static void broadcastUpdatedScores(){
        JSONObject jsonScores = getScores();
        for (Session ses : ScoresWebSocketHandler.sessions){
            try {
                ses.getRemote().sendString(String.valueOf(jsonScores));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void sendScores(Session ses) {
        JSONObject jsonScores = getScores();
        try {
            ses.getRemote().sendString(String.valueOf(jsonScores));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

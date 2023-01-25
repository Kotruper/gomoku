import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import org.bson.Document;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
//TODO: DB connection, password req?
public class UserController {

//    private static Map<String, String> userHashPasswordMap = new ConcurrentHashMap<>();

    public static boolean createUser(String username, String password) {
        if (((Document) Server.dbCollection.find(new Document("Username",username)).first()) == null) {
            return false;
        } else {
            try {
//                userHashPasswordMap.put(username, toHexString(getSHA(password)));
                Server.dbCollection.insertOne(new Document("Username", username).append("Password", toHexString(getSHA(password))).append("Wins", 0)); // dodawanie do DB krotki (Username, Password, Wins)
            }
            catch (NoSuchAlgorithmException e){
                System.out.println("Exception thrown for incorrect algorithm: " + e);
            }
            return true;
        }
    }

    public static boolean authenticate(String username, String password) {
        try {
            String hashedPass = toHexString(getSHA(password));
//            return (Objects.equals(userHashPasswordMap.get(username), hashedPass)) && password != null;
            return (Objects.equals(((Document) Server.dbCollection.find(new Document("Username",username)).first()).get("Password"), hashedPass)) && password != null; // do sprawdzanie przez bazę danych
        }
        catch (NoSuchAlgorithmException e){
            System.out.println("Exception thrown for incorrect algorithm: " + e);
            return false;
        }
    }


    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }
}


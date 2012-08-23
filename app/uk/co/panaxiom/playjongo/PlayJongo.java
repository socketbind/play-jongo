package uk.co.panaxiom.playjongo;

import java.net.UnknownHostException;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import play.Logger;
import play.Play;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoURI;
import com.mongodb.gridfs.GridFS;

public class PlayJongo {

    private static volatile PlayJongo INSTANCE = null;

    private Mongo mongo = null;
    private Jongo jongo = null;
    private GridFS gridfs = null;

    private PlayJongo() throws UnknownHostException, MongoException {
        MongoURI uri = new MongoURI(Play.application().configuration().getString("playjongo.uri"));

        if (Play.isTest()) {
            mongo = new Mongo("localhost", 27017);
            jongo = new Jongo(mongo.getDB("test"));
        } else {
            mongo = new Mongo(uri);
            DB db = mongo.getDB(uri.getDatabase());

            // Authenticate the user if necessary
            if (uri.getUsername() != null) {
                db.authenticate(uri.getUsername(), uri.getPassword());
            }
            jongo = new Jongo(db);
        }

        if (Play.application().configuration().getBoolean("playjongo.gridfs.enabled")) {
            gridfs = new GridFS(jongo.getDatabase());
        }
    }

    public static PlayJongo getInstance() {
        if (INSTANCE == null) {
            synchronized (PlayJongo.class) {
                if (INSTANCE == null) {
                    try {
                        INSTANCE = new PlayJongo();
                    } catch (UnknownHostException e) {
                        Logger.error("UnknownHostException", e);
                    } catch (MongoException e) {
                        Logger.error("MongoException", e);
                    }
                }
            }
        }
        return INSTANCE;
    }

    public static void forceNewInstance() {
        INSTANCE = null;
        getInstance();
    }

    public static Mongo mongo() {
        return getInstance().mongo;
    }

    public static Jongo jongo() {
        return getInstance().jongo;
    }

    public static GridFS gridfs() {
        return getInstance().gridfs;
    }

    public static MongoCollection getCollection(String name) {
        return getInstance().jongo.getCollection(name);
    }

    public static DB getDatabase() {
        return getInstance().jongo.getDatabase();
    }
}
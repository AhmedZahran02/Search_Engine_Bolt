package DB;

import Crawler.WebCrawler;
import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Scanner;

public class mongoDB {

    public static int MAX_PAGES_NUM = 6000;
    private static MongoClient client;
    private static MongoDatabase DB;
    MongoCollection<Document> seedCollection;
    MongoCollection<Document> crawlerCollection;

    // Indexing Collections
    MongoCollection<Document> IndexedPages;
    MongoCollection<Document> wordsCollection;



    public mongoDB(String DB_Name) {

        if (client == null) {
//            ConnectionString connectionString = new ConnectionString("mongodb+srv://ahmedr2001:eng3469635@javasearchengine.8xarqeo.mongodb.net/?retryWrites=true&w=majority");
            ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
            client = MongoClients.create(connectionString);
            DB = client.getDatabase(DB_Name);
            seedCollection = DB.getCollection("Seed");
            crawlerCollection = DB.getCollection("CrawledPages");
//            crawlerCollection.drop();
//            seedCollection.drop();
        } else {
            System.out.println("Already connected to the client");
        }
    }

    public void initializeSeed() {
        if (crawlerCollection.countDocuments() >= MAX_PAGES_NUM) {
            System.out.println("Crawling has reached its limit which is equal to " + MAX_PAGES_NUM + " ,System is rebooting");
            crawlerCollection.drop();
            seedCollection.drop();
        }
        if (seedCollection.countDocuments() == 0) {
            try {
                File file = new File("seed.txt").getAbsoluteFile();
                Scanner cin = new Scanner(file);
                while (cin.hasNextLine()) {
                    String url = cin.nextLine();
                    if (WebCrawler.handleRobot("*",url,-1)){
                        org.jsoup.nodes.Document jdoc =WebCrawler.getDocument(url);
                        if (jdoc != null){
                            Document doc = new Document("URL", url).append("KEY", WebCrawler.toHexString(WebCrawler.getSHA(jdoc.body().toString()))).append("BODY",jdoc.body().toString());
                            seedCollection.insertOne(doc);
                        }
                    }
                }
                cin.close();
            } catch (FileNotFoundException | NoSuchAlgorithmException e) {
                System.out.println("Reading seed file failed :" + e);
            }
        } else {
            System.out.println("Crawling hasn't reached its limit yet , so System is Continued");
        }
    }

    public void addToCrawledPages(Document doc) {
        synchronized (this) {
            if (doc == null) return;
            if (getNumOfCrawledPages() + getSeedSize() < mongoDB.MAX_PAGES_NUM) {
                crawlerCollection.insertOne(doc);
            }
        }
    }

    public boolean isCrawled(Document doc) {
        synchronized (this) {
            return crawlerCollection.find(eq("KEY",doc.get("KEY"))).cursor().hasNext();
        }
    }

    public void pushSeed(Document doc) {
        synchronized (this) {
            if (doc == null) return;
            if (getNumOfCrawledPages() + getSeedSize() < mongoDB.MAX_PAGES_NUM) {
                seedCollection.insertOne(doc);
            }
        }
    }

    public Document popSeed() {
        synchronized (this) {
            return seedCollection.findOneAndDelete(new Document());
        }
    }

    public boolean isSeeded(Document doc) {
        synchronized (this) {
            return seedCollection.find(eq("KEY",doc.get("KEY"))).cursor().hasNext();
        }
    }

    public long getSeedSize() {
        synchronized (this) {
            return seedCollection.countDocuments();
        }
    }

    public long getNumOfCrawledPages() {
        synchronized (this) {
            return crawlerCollection.countDocuments();
        }
    }

    // Indexing Functions

    public boolean isIndexed(String url) {

        return IndexedPages.find(new Document("url", url)).iterator().hasNext();
    }



    public static void List_All(MongoCollection collection) {
//        Listing All Mongo Documents in Collection
        FindIterable<Document> iterDoc = collection.find();
        int i = 1;
// Getting the iterator
        System.out.println("Listing All Mongo Documents");
        Iterator it = iterDoc.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
            i++;
        }
//specific document retrieving in a collection
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("name", "Manga");
        System.out.println("Retrieving specific Mongo Document");
        MongoCursor<Document> cursor = collection.find(searchQuery).iterator();
        while (cursor.hasNext()) {
            System.out.println(cursor.next());
        }
    }

}

package Indexer;
import DB.mongoDB;
import org.bson.Document;
import java.util.HashMap;
import java.util.List;


public class WebIndexer {
    mongoDB DB ;
    HashMap<String, Integer> indexedPages; //
    HashMap<String, List<Document>> index; // (Inverted File ) This stores for each word the documents that it was present in

    public void startIndexer(String body , String url){
        // 1 - Checking if the page has been indexed before
        if (DB.isIndexed(url)) {
            System.out.println("This page has been indexed before");
            return;
        } else {
            System.out.println("New page is being indexed ");
        }
        // 2 - Cleaning
        Cleaner cleaner = new Cleaner() ;
        body = cleaner.runCleaner(body);
        // 3 -  Tokenization
        Tokenizer tokenizer = new Tokenizer();



    }


}

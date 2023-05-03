package QueryProcessor;

import DB.mongoDB;
import Indexer.Stemmer;
import Indexer.Tokenizer;
import org.bson.Document;

import java.util.*;

public class PhraseSearching {
    private final Tokenizer tokenizer;
    private final Stemmer stemmer;
    private final mongoDB DB;

    public static void main(String args[]) {
        PhraseSearching phraseSearching= new PhraseSearching();
        phraseSearching.run("Alexander McQueen");

    }

    public PhraseSearching() {
        this.DB  = new mongoDB("Bolt");

        this.tokenizer = new Tokenizer();
        this.stemmer = new Stemmer();
    }


    // the idea is to loop though words in phrase
    // get all urls exist in all words
    // then loop on urls and check if contain phrase or not
    public void run(String phrase) {
        // 1 . get all words of phrase
        List<String> words = tokenizer.runTokenizer(phrase);
        words = stemmer.runStemmer(words);
        // 2 . get all urls contain all words exist
        Set<String> urls = new HashSet<>();

        for (String word : words) {

            Set<String> urls_word = getUrlsContainWord(word);                 //store urls contain word
            if(urls_word == null) {
                System.out.println("Error in word : " + word);
                continue;
            }
            if(urls.isEmpty())
                urls.addAll(urls_word);
            else
                urls.retainAll(urls_word);

        }

        Iterator<String> iterator = urls.iterator();
        while (iterator.hasNext()) {
            String url = iterator.next();
            String url_body = DB.getUrlBody(url);
            if (url_body == null) {
                System.out.println("remove: " + url);
                iterator.remove();
            }
        }

        System.out.println("== all urls ====");
        System.out.println(urls);
        //return urls;
    }

    private Set<String> getUrlsContainWord(String word) {
        Set<String> urls_res = new HashSet<>();                 //store urls contain word
        List<Document> wordDocs = DB.getWordDocuments(word);    //get Documents from inverted file in DB

        // ======= not found a word  in  DB ========
        if(wordDocs == null || wordDocs.isEmpty()) return null;

        for (Document wordDoc : wordDocs) { //loop though documents contain word:word
            @SuppressWarnings("unchecked")
            List<Document> pages = (List<Document>) wordDoc.get("pages");   //get key pages that contain all urls
            for (Document page : pages) {                                   //loop through pages and get url
                String url = page.getString("url");
                urls_res.add(url);
            }
        }
        //System.out.println("word : " + word);
        //System.out.println(urls_res);

        return  urls_res;
    }
}

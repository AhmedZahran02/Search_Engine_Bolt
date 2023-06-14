package com.bolt.Brain.QueryProcessor;

import com.bolt.Brain.Utils.Stemmer;
import com.bolt.Brain.Utils.StopWordsRemover;
//import com.bolt.Brain.Utils.Synonymization;
import com.bolt.Brain.Utils.Tokenizer;
import com.bolt.SpringBoot.CrawlerService;
import com.bolt.SpringBoot.Page;
import com.bolt.SpringBoot.WordsDocument;
import com.bolt.SpringBoot.WordsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QueryProcessor {
    ProcessQueryUnit processQueryUnit;
    private CrawlerService crawlerService;
    private WordsService wordsService;


    private static ParagraphService paragraphService;

    List<String> originalWords ;

    public QueryProcessor(CrawlerService crawlerService,WordsService wordsService) throws IOException {
        tokenizer = new Tokenizer();
        stopWordsRemover = new StopWordsRemover();
//        synonymization = new Synonymization();
        stemmer = new Stemmer();
        this.crawlerService=crawlerService;
        this.wordsService=wordsService;
    }

    public List<String> getOriginalWords(){
        return originalWords;
    }
    public List<WordsDocument> run(String query) throws IOException {

        List<BooleanItem> items = extractBooleanItem(query);

        // solve phrases of operands
        for(int i = 0;i < items.size(); i++) {
            if(!(items.get(i) instanceof WordItem || items.get(i) instanceof PhraseItem) ) {
                items.get(i).execute(items, i);
                i--;
            }
        }
        //solve single phrases and words
        for(int i = 0;i < items.size(); i++) {
            if((items.get(i) instanceof WordItem || items.get(i) instanceof PhraseItem) )
                items.get(i).executeOne(items, i);

        }

        //======= Variables Section ========//
        List<WordsDocument> results = new ArrayList<>();

        List<String> words = process(query);                //1. process query and return all words after processing
        List<WordsDocument> results = getWordResult(words);
        System.out.println(words);

        //===== Get Documents into results ===== //
        for (String word : words) {
            results.addAll(wordsService.findWords(word));
        }
        // ==== Handel phrases ==== //

        //======== testing ======
        for(WordsDocument wDoc: results) {
            for(Page url: wDoc.getPages()){
                for(Integer pid : url.getParagraphIndexes()) {
                    String paragraph = paragraphService.findParagraph(pid).getParagraph();
                    System.out.println(paragraph);
                    System.out.println();
                    System.out.println();

        return results;
    }

    private void printingResults(List<List<WordsDocument>> phrase_results) {
        for(List<WordsDocument> phrase_res: phrase_results) {
            for(WordsDocument wDoc: phrase_res) {
                for(Page pg: wDoc.getPages()) {
                    for(Integer pix: pg.getParagraphIndexes()) {
                        String paragraph = paragraphService.findParagraph(pix).getParagraph();
                        System.out.println(pg.getUrlId() + " " + pix + ": " + paragraph);
                    }
                }
            }
        }
    }
    private List<String> extractPhrases(String query) {
        List<String> phrases = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(query);

        while (matcher.find()) {
            phrases.add(matcher.group(1));
        }

        return phrases;
    }

    public List<String> process(String query) throws IOException {
        List<String> words;
        query = query.replaceAll("[^a-zA-Z1-9]", " "); //1.remove single characters and numbers
        words = tokenizer.runTokenizer(query);                          //2.Convert words to list + toLowerCase
//        words = synonymization.runSynonymization(words//3.Replace words with its steam synonyms
        words = tokenizer.runTokenizer(query);                          //3.Convert words to (list + toLowerCase)
        words = stemmer.runStemmer(words);                              //4.return to it's base
        words = stopWordsRemover.runStopWordsRemover(words);            //4.Remove Stop Words
        words = words.stream().distinct()                               //5.Remove Duplicates
                .collect(Collectors.toList());
        return words;
    }

    private List<WordsDocument> getWordResult(List<String> words) {
        List<WordsDocument> results = new ArrayList<>();

        //===== Get Documents into results ===== //
        for (String word : words) {
            results.addAll(wordsService.findWords(word));
        }
        return results;
    }

<<<<<<< HEAD
    public static List<WordsDocument> getWordResult(String word) {

        return wordsService.findWords(word);
    }







    public static List<WordsDocument> runPhraseSearching(List<WordsDocument> results, Pattern pattern) {
        HashMap<Integer, Boolean> pargraphIndexsStore = new HashMap<>();

        Iterator<WordsDocument> iteratorWDoc = results.iterator();
        while(iteratorWDoc.hasNext()) {                                  //1. loop on word search results
            WordsDocument wDoc = iteratorWDoc.next();
            Iterator<Page> iteratorPages = wDoc.getPages().iterator();
            while(iteratorPages.hasNext()) {                                //2. loop on urls
                Page pg = iteratorPages.next();
                List<Integer> paragraphIndexes = pg.getParagraphIndexes();
                for(int i = 0;i < paragraphIndexes.size();i++ ) {           //3. loop on p in url
                    Integer paragraphId = paragraphIndexes.get(i) ;
                    //3.1 if it already processed skip
                    if(pargraphIndexsStore.containsKey(paragraphId)) {
                        if(!pargraphIndexsStore.get(paragraphId)) {
                            removeParagraphData(pg, i);
                            i--;
                        }
                        continue;
                    }

                    String paragraph = paragraphService.findParagraph(paragraphId).getParagraph();

                    if(! wordsExistInParagraph(pattern, paragraph)) {           // check pattern matcher
                        removeParagraphData(pg, i);
                        i--; // to calibrate loop
                        pargraphIndexsStore.put(paragraphId, false);

                    } else  pargraphIndexsStore.put(paragraphId, true);
                }
                if(pg.getParagraphIndexes().isEmpty()) iteratorPages.remove();
            }
            if(wDoc.getPages().isEmpty()) iteratorWDoc.remove();
        }
        return results;
    }

    // loop on phrases and get all results


    private Pattern regexPatternPhrase(List<String> words) {
        String regex = ".*" + String.join(".*", words) + ".*";
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    private static boolean wordsExistInParagraph(Pattern pattern, String paragraph) {
        Matcher matcher = pattern.matcher(paragraph);
        return  matcher.matches();
    }

    public static void removeParagraphData(Page pg, int i) {
        if(pg.getTagIndexes()!=null) {
            pg.getTagIndexes().remove(i);
        }
        if(pg.getParagraphIndexes() != null) {
            pg.getParagraphIndexes().remove(i);
        }
        if(pg.getWordIndexes() != null) {
            pg.getWordIndexes().remove(i);
        }
        if(pg.getTagTypes() != null) {
            pg.getTagTypes().remove(i);
        }
    }


}
=======
}
>>>>>>> main

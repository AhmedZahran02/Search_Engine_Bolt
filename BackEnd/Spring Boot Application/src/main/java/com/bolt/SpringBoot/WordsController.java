package com.bolt.SpringBoot;

import com.bolt.Brain.QueryProcessor.QueryProcessor;
import com.bolt.Brain.Ranker.MainRanker;
import org.bson.Document;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.codec.StringDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.print.Doc;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://127.0.0.1:5173")
@RestController
@RequestMapping("/search")
public class WordsController {

    @Autowired
    private WordsService wordsService;
    @Autowired
    private CrawlerService crawlerService;

    @Autowired
    private UrlsService urlsService;

    @Autowired
    private ParagraphService paragraphService;

    @GetMapping("/all")
    public ResponseEntity<List<WordsDocument>> allWords() {
        return new ResponseEntity<List<WordsDocument>>(wordsService.allWords(), HttpStatus.OK);
    }

    @GetMapping("/p")
    public ResponseEntity<List<String>> getParagraph(@RequestParam String[] pids) {
        return new ResponseEntity<List<String>>(paragraphService.getParagraphs(pids), HttpStatus.OK);
    }
    @GetMapping

    public ResponseEntity<List<Document>> search(@RequestParam String q) throws IOException {
        QueryProcessor queryProcessor = new QueryProcessor(crawlerService, wordsService, paragraphService);
        List<WordsDocument> RelatedDocuments = queryProcessor.run(q);
//        return new ResponseEntity<List<WordsDocument>>(RelatedDocuments, HttpStatus.OK);
        MainRanker mainRanker = new MainRanker(RelatedDocuments, urlsService );
        List<Document>list = mainRanker.runRanker();
        return new ResponseEntity<List<Document>>(list, HttpStatus.OK);
    }
}

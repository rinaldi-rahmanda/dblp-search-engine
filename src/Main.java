import handler.DBLPHandler;
import indexer.DBLPIndexer;
import model.DBLP;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.xml.sax.SAXException;
import parser.DBLPParser;
import searcher.DBLPSearcher;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {
        String indexPath = "resources/index";
        String rawFilePath = "resources/dblp.xml";

        Analyzer analyzer = new EnglishAnalyzer();
        new StandardAnalyzer();

        System.out.println("Initializing DBLP search engine...");
        Path path = Paths.get(indexPath);
        if (!Files.exists(path)) {
            System.out.println("DBLP index not found. Generating...");

            DBLP dblp;
            try {
                dblp = new DBLPParser(new DBLPHandler()).parse(rawFilePath);
            } catch (ParserConfigurationException | SAXException e) {
                throw new RuntimeException(e);
            }

            new DBLPIndexer(analyzer).index(dblp, indexPath);
        }

        System.out.println("Loading DBLP index...");
        DBLPSearcher searcher = new DBLPSearcher(indexPath, analyzer);
        if (args.length > 0) {
            int topN = 10;
            if (args.length > 1) {
                topN = Integer.parseInt(args[1]);
            }
            List<String> results = searcher.search(args[0], topN);
            for (String result : results) {
                System.out.println(result);
            }
        }

        analyzer.close();
    }
}

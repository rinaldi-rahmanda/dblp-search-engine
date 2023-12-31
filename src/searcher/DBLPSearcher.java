package searcher;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DBLPSearcher {
    private IndexReader reader;
    private IndexSearcher searcher;
    private QueryParser parser;

    public DBLPSearcher(String indexPath, Analyzer analyzer) throws IOException {
        Path path = Paths.get(indexPath);
        Directory indexDir = FSDirectory.open(path);
        reader = DirectoryReader.open(indexDir);
        searcher = new IndexSearcher(reader);
        parser = new QueryParser("title", analyzer);
    }

    public List<String> search(String queryString) throws ParseException, IOException {
        return search(queryString, 10);
    }
    public List<String> search(String queryString, int topN) throws ParseException, IOException {
        Query query = parser.parse(queryString);

        long startTime = System.nanoTime();
        TopDocs topDocs = searcher.search(query, topN);
        System.out.printf("Search duration: %f seconds\n", (System.nanoTime() - startTime)  / 1000000000.0);

        List<String> results = new ArrayList<>();
        for (int rank = 0; rank < topDocs.scoreDocs.length; rank++) {
            ScoreDoc scoreDoc = topDocs.scoreDocs[rank];
            Document doc = reader.document(scoreDoc.doc);

            results.add(String.format(
                    "(%d) [Doc ID: %d, Match Score: %.04f] %s by %s, published %s in %s",
                    rank + 1,
                    scoreDoc.doc,
                    scoreDoc.score,
                    doc.getField("title").stringValue(),
                    Arrays.stream(doc.getFields("author")).map(IndexableField::stringValue).collect(Collectors.joining(", ")),
                    doc.getField("year").stringValue(),
                    doc.getField("venue").stringValue()));
        }

        return results;
    }

    public List<String> similarDocs(int docID) throws ParseException, IOException {
        Document sourceDoc = reader.document(docID);

        long startTime = System.nanoTime();
        TopDocs topDocs = searcher.search(parser.parse(QueryParser.escape(sourceDoc.get("title"))), 11);
        System.out.printf("Search duration: %f seconds\n", (System.nanoTime() - startTime)  / 1000000000.0);

        List<String> results = new ArrayList<>();
        int rank = 0;
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            if (scoreDoc.doc == docID) {
                continue;
            }
            Document doc = reader.document(scoreDoc.doc);

            results.add(String.format(
                    "(%d) [Doc ID: %d, Match Score: %.04f] %s by %s, published %s in %s",
                    ++rank,
                    scoreDoc.doc,
                    scoreDoc.score,
                    doc.getField("title").stringValue(),
                    Arrays.stream(doc.getFields("author")).map(IndexableField::stringValue).collect(Collectors.joining(", ")),
                    doc.getField("year").stringValue(),
                    doc.getField("venue").stringValue()));
        }

        return results;
    }
}

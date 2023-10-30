package indexer;

import model.Article;
import model.DBLP;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DBLPIndexer {
    private final String DOC_TITLE = "title";
    private final String DOC_AUTHOR = "author";
    private final String DOC_VENUE = "venue";
    private final String DOC_YEAR = "year";
    private final String DOC_S_YEAR = "year";
    private final String DOC_PUB_TYPE = "pub_type";

    private Analyzer analyzer;

    public DBLPIndexer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public void index(DBLP dblp, String indexPath) throws IOException {
        Path path = Paths.get(indexPath);
        Directory directory = FSDirectory.open(path);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        int articlesSize = dblp.getArticles().size();
        int decile = (int) Math.ceil(articlesSize / 10.0);
        int decileCount = 0;
        int indexCount = 0;
        long indexStartTime = System.nanoTime();
        long decileStartTime = indexStartTime;
        for (Article article : dblp.getArticles()) {
            if (article.getTitle() == null || article.getVenue() == null) {
                continue;
            }

            Document document = new Document();
            document.add(new TextField(DOC_TITLE, article.getTitle(), Field.Store.YES));
            for (String author : article.getAuthors()) {
                document.add(new TextField(DOC_AUTHOR, author, Field.Store.YES));
            }
            document.add(new TextField(DOC_VENUE, article.getVenue(), Field.Store.YES));
            document.add(new StringField(DOC_PUB_TYPE, article.getPublicationType(), Field.Store.NO));
            document.add(new IntPoint(DOC_YEAR, article.getYear()));
            document.add(new StringField(DOC_S_YEAR, String.valueOf(article.getYear()), Field.Store.YES));
            indexWriter.addDocument(document);
            indexCount++;
            if (indexCount == decile) {
                long currentTime = System.nanoTime();
                System.out.printf(
                        "[INDEX] Decile #%d index time: %f seconds, total elapsed time: %f seconds\n",
                        ++decileCount,
                        (currentTime - decileStartTime) / 1000000000.0,
                        (currentTime - indexStartTime) / 1000000000.0);
                indexCount = 0;
                decileStartTime = currentTime;
            }
        }

        if (decileCount != 10) {
            long currentTime = System.nanoTime();
            System.out.printf(
                    "[INDEX] Decile #%d index time: %f seconds, total elapsed time: %f seconds\n",
                    ++decileCount,
                    (currentTime - decileStartTime) / 1000000000.0,
                    (currentTime - indexStartTime) / 1000000000.0);
        }

        indexWriter.close();
    }
}

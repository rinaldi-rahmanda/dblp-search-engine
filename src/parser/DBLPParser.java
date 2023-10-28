package parser;

import model.Article;
import model.DBLP;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DBLPParser {
    private class DBLPHandler extends DefaultHandler {
        private final String TAG_ARTICLE = "article";
        private final String TAG_INPROCEEDINGS = "inproceedings";
        private final String TAG_AUTHOR = "author";
        private final String TAG_TITLE = "title";
        private final String TAG_JOURNAL = "journal";
        private final String TAG_BOOKTITLE = "booktitle";
        private final String TAG_YEAR = "year";

        private DBLP dblp;
        private List<Article> articles;
        private Article article;
        private List<String> authors = new ArrayList<>();
        private StringBuilder tagValue;

        @Override
        public void startDocument() throws SAXException {
            dblp = new DBLP();
            articles = new ArrayList<>();
        }

        @Override
        public void endDocument() throws SAXException {
            dblp.setArticles(articles);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case TAG_ARTICLE:
                    article = new Article("article");
                    break;
                case TAG_INPROCEEDINGS:
                    article = new Article("inproceedings");
                    break;
                case TAG_AUTHOR:
                    if (authors == null) {
                        authors = new ArrayList<>();
                    }
                case TAG_TITLE:
                case TAG_BOOKTITLE:
                case TAG_JOURNAL:
                case TAG_YEAR:
                    if (article == null) {
                        break;
                    }
                    tagValue = new StringBuilder();
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (qName) {
                case TAG_ARTICLE:
                case TAG_INPROCEEDINGS:
                    article.setAuthors(authors);
                    authors = new ArrayList<>();
                    articles.add(article);
                    article = null;
                    break;
                case TAG_AUTHOR:
                    if (article == null) {
                        break;
                    }
                    authors.add(tagValue.toString());
                    break;
                case TAG_TITLE:
                    if (article == null) {
                        break;
                    }
                    article.setTitle(tagValue.toString());
                    break;
                case TAG_JOURNAL:
                case TAG_BOOKTITLE:
                    if (article == null) {
                        break;
                    }
                    article.setVenue(tagValue.toString());
                    break;
                case TAG_YEAR:
                    if (article == null) {
                        break;
                    }
                    article.setYear(Integer.parseInt(tagValue.toString()));
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            tagValue.append(ch, start, length);
        }

        public DBLP getDBLP() {
            return dblp;
        }
    }

    public DBLP parse(String filePath) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        DBLPHandler handler = new DBLPHandler();

        long startTime = System.nanoTime();
        saxParser.parse(new File(filePath), handler);
        long endTime = System.nanoTime();
        System.out.printf("[PARSE] Document parse time: %f seconds\n", (endTime - startTime) / 1000000000.0);

        return handler.getDBLP();
    }
}



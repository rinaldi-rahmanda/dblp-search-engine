package parser;

import handler.DBLPHandler;
import model.DBLP;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;

public class DBLPParser {
    private final DBLPHandler handler;

    public DBLPParser(DBLPHandler handler) {
        this.handler = handler;
    }

    public DBLP parse(String filePath) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();

        long startTime = System.nanoTime();
        saxParser.parse(new File(filePath), handler);
        long endTime = System.nanoTime();
        System.out.printf("[PARSE] Document parse time: %f seconds\n", (endTime - startTime) / 1000000000.0);

        return handler.getDBLP();
    }
}



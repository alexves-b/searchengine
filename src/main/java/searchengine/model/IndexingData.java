package searchengine.model;

import lombok.Data;
import org.jsoup.nodes.Document;

import java.util.TreeSet;

@Data
public class IndexingData {
    int code;
    Document document;

    TreeSet<String> resultList;

}

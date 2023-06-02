package searchengine.response;

import lombok.Data;
import searchengine.model.DataForSnippet;

import java.util.List;

@Data
public class QueryResponse {
    boolean result;
    int count;
    private List<DataForSnippet> data;
    String error;
}

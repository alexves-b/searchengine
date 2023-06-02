package searchengine.services;

import searchengine.response.IndexResponse;
import searchengine.model.Url;

public interface IndexServise {
    IndexResponse startIndexing() throws InterruptedException;

    IndexResponse stopIndexing();
    IndexResponse pageOnSite(Url url);

}

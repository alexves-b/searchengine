package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.response.QueryResponse;
@Service
public interface QuerryService {
    QueryResponse findQueryFromSiteEngine(String query, String siteUrl, Integer offset, Integer limit);
}

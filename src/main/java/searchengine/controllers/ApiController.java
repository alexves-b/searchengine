package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.*;
import searchengine.response.IndexResponse;
import searchengine.response.QueryResponse;
import searchengine.services.StatisticsService;
import searchengine.services.impl.IndexServiceImpl;
import searchengine.services.impl.QuerrySericeImpl;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final  IndexServiceImpl indexService;
    private final QuerrySericeImpl querrySerice;
    private final StatisticsService statisticsService;


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {

        return ResponseEntity.ok(statisticsService.getStatistics());
    }
    @GetMapping("/startIndexing")
    public ResponseEntity<IndexResponse> startIndexing() throws InterruptedException {
        return ResponseEntity.ok(indexService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexResponse> stopIndexing() throws InterruptedException {
        return ResponseEntity.ok(indexService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexResponse> reIndexPage(@RequestParam Url url) {
        return ResponseEntity.ok(indexService.pageOnSite(url));
    }

    @GetMapping("/search")
    public ResponseEntity<QueryResponse> searchQuery(@RequestParam String query,
                                                     @RequestParam(required = false) String site,
                                                     @RequestParam Integer offset,
                                                     @RequestParam Integer limit){
        //Задать в квери респпонс что он должен вернуть, чтобы увидеть что выйдет из фронта
         return ResponseEntity.ok(querrySerice.findQueryFromSiteEngine(query,site,offset,limit));
    }
}

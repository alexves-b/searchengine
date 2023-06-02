package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.*;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.StatisticsService;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final SitesList sites;
    @Autowired
    LemmaRepository lemmaRepository;
    @Autowired
    PageRepository pageRepository;
    @Autowired
    SiteRepository siteRepository;
    @Override
    public StatisticsResponse getStatistics() {
        String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };

        TotalStatistics total = new TotalStatistics();

        List <DetailedStatisticsItem> detailed = new ArrayList<>();

        List <Site> sitesList = sites.getSites();
        List <Site> siteListFromDb = siteRepository.findAll();

        for(int i = 0; i < siteListFromDb.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());


            List <Page> pages = pageRepository.getCountPageBySiteId(site.getId());
            List <Lemma> lemmas = lemmaRepository.getCountLemmaBySiteId(site.getId());
            item.setPages(pages.size());
            item.setLemmas(lemmas.size());
                if(i < siteListFromDb.size() && siteListFromDb.get(i).getLastError() != null) {
                    item.setError(siteListFromDb.get(i).getLastError());
                }
            item.setStatus(siteListFromDb.get(i).getStatus().toString());
            item.setStatusTime(Date.from(siteListFromDb.get(i).getStatusTime()
                    .atZone(ZoneId.systemDefault()).minusHours(4).toInstant()).getTime());


            total.setPages(total.getPages() + pages.size());
            total.setLemmas(total.getLemmas() + lemmas.size());
            detailed.add(item);
        }

        total.setSites(sitesList.size());
        total.setIndexing(true);
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.repository.*;
import searchengine.response.IndexResponse;
import searchengine.services.IndexServise;
import searchengine.utils.MyUtils;
import searchengine.utils.ParseUrl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexServise {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LinkStorage linkStorage;
    private final SitesList sites;
    private final IndexResponse indexResponse = new IndexResponse();

    @Override
    public IndexResponse startIndexing(){
        //Проверять запущена ли индексация
        if(!ParseUrl.isIndexing()) {
            indexResponse.setResult(true);
            ParseUrl.setShutDown(false);
            startIndexingAllSites();
        } else {
            indexResponse.setResult(false);
            indexResponse.setError("Индексация уже запущена");
        }
        return indexResponse;
    }

    @Override
    public IndexResponse stopIndexing() {

        if (!ParseUrl.isIndexing()) {
            indexResponse.setResult(false);
            indexResponse.setError("Индексация не запущена");
        }
        ParseUrl.setShutDown(true);
        indexResponse.setResult(true);
        ParseUrl.setIsIndexing(false);

        return indexResponse;
    }
    @Override
    public IndexResponse pageOnSite(Url url) {
        //Если индексация сайта не была сделана, проверить есть ли сайт в конфиге,
        // если есть добавить сайт и проиндексировать его.
        for (Site site: siteRepository.findAll()) {
            if (url.getUrl().startsWith(site.getUrl())) {
                url.setConfigContainsUrl(true);
                indexResponse.setResult(true);
                MyUtils myUtils = new MyUtils(linkStorage,
                        pageRepository,siteRepository,site,lemmaRepository,indexRepository);
                System.out.println(myUtils.prepareToIndexOnePage(url.getUrl(),site));
            }
        }
        if (!url.isConfigContainsUrl()) {
            indexResponse.setError("Данная страница находится за пределами сайтов," +
                    "указанных в конфигурационном файле");
            indexResponse.setResult(false);
        }
        return indexResponse;
    }

    public void startIndexingAllSites(){
        List <Site> sitesList = sites.getSites();
        ParseUrl.setIsIndexing(true);
        indexRepository.deleteAll();
        lemmaRepository.deleteAll();
        pageRepository.deleteAll();
        siteRepository.deleteAll();
        List <Site> siteListIndexing =  addSiteFromConfigToDb(sitesList);

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        for (int i = 0; i < siteListIndexing.size(); i++) {
            System.out.println("Url change " + sitesList.get(i).getUrl());
            try {
                final int finalI = i;
                Runnable task = () -> {
                    ParseUrl parseUrl = new ParseUrl(linkStorage, siteListIndexing.get(finalI).getUrl(),
                            siteListIndexing.get(finalI), pageRepository, siteRepository,
                            lemmaRepository, indexRepository);
                    forkJoinPool.invoke(parseUrl);

                    if (siteListIndexing.get(finalI).getLastError() == null) {
                        siteListIndexing.get(finalI).setStatus(StatusType.INDEXED);
                    }  else {
                        siteListIndexing.get(finalI).setStatus(StatusType.FAILED);
                    }
                    siteListIndexing.get(finalI).setStatusTime(LocalDateTime.now().plusHours(4));
                    siteRepository.save(siteListIndexing.get(finalI));
                };
                Thread thread = new Thread(task);
                thread.start();

                //System.out.println("thread.getName()=" +thread.getName() +"  thread.getId()=" + thread.getId() );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public List<Site> addSiteFromConfigToDb(List<Site> sitesList){
        List <Site> siteListAfrerStartIndexing = new ArrayList<>();
        System.out.println(sitesList.size()+" sl-size");
        for (int j = 0; j < sitesList.size(); j++) {
            Site site = new Site(StatusType.INDEXING, LocalDateTime.now().plusHours(4), sitesList.get(j).getUrl(), sitesList.get(j).getName());
            site.setId(j+1);
            siteRepository.save(site);
            siteListAfrerStartIndexing.add(site);
            System.out.println(site);
        }
        return siteListAfrerStartIndexing;
    }


}

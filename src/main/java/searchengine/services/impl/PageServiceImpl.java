package searchengine.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.services.MyUtils;
import searchengine.services.PageService;
import searchengine.services.ParseUrl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
public class PageServiceImpl implements PageService {


    private final SitesList sites;
    private List<Site> sitesList;

    @Autowired
    SiteRepository siteRepository;


    PageRepository pageRepository;

    public static String siteParse;
    public static volatile int idSite;

    public PageServiceImpl(SitesList sites, SiteRepository siteRepository, PageRepository pageRepository) {
        this.sites = sites;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }


    public void init() throws InterruptedException {
        sitesList = sites.getSites();
        System.out.println(sitesList.size()+" sl-size");
        for (int j = 0; j < sitesList.size(); j++) {
                Site site = new Site(j + 1, StatusType.INDEXING, LocalDateTime.now(), sitesList.get(j).getUrl(), sitesList.get(j).getName());
                siteRepository.save(site);
                siteRepository.delete(site);
                idSite = j+1;
        }
        parseSite();
    }


    public void parseSite() throws InterruptedException {

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        for (int i = 0; i < sitesList.size(); i++) {
            siteParse = sitesList.get(i).getUrl();
            System.out.println("Url change " + sitesList.get(i).getUrl());
            try {
                ParseUrl parseUrl = new ParseUrl(sitesList.get(i).getUrl(),pageRepository);
                parseUrl.setSite(sitesList.get(i));
                forkJoinPool.invoke(parseUrl);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println("Готовимся вздремнуть");
            Thread.sleep(5000);
            MyUtils.noDoublesUrlList.clear();
        }
    }


    @Override
    public Page addPage(Page page) {
        Page savedPage = pageRepository.save(page);
        return savedPage;
    }

    @Override
    public void delete(int id) {
        pageRepository.deleteById(id);
    }

    @Override
    public Page editPage(Page page) {
        return pageRepository.saveAndFlush(page);
    }

    @Override
    public List<Page> getAll() {
        return pageRepository.findAll();
    }



}

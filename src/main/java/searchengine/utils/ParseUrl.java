package searchengine.utils;

import searchengine.model.*;
import searchengine.repository.*;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;


public class ParseUrl extends RecursiveTask<Set<String>> {
    @Override
    public String toString() {
        return url;
    }

    private final String url;

    private final Set<String> noDoublesUrlList = new HashSet<>() ;
    PageRepository pageRepository;
    SiteRepository siteRepository;
    LemmaRepository lemmaRepository;
    IndexRepository indexRepository;

    LinkStorage linkStorage;
   static volatile boolean isShutDown = false;

    public static boolean isIndexing() {
        return isIndexing;
    }

    public static void setIsIndexing(boolean isIndexing) {
        ParseUrl.isIndexing = isIndexing;
    }

    static volatile boolean isIndexing = false;


    public static boolean isShutDown() {
        return isShutDown;
    }
    public static void setShutDown(boolean shutDown) {
        isShutDown = shutDown;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    Site site;
    public ParseUrl(LinkStorage linkStorage,String url,Site site, PageRepository pageRepository, SiteRepository siteRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.linkStorage = linkStorage;
        this.url = url;
        this.site = site;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;

    }

    public Site getSite() {
        return site;
    }

    @Override
    protected Set<String> compute() throws ConcurrentModificationException {
        try {

            Set<String> urlList;
            List<ParseUrl> taskList;
            if (!isShutDown()) {
                isIndexing = true;
                //System.out.println(linkStorage.getLinksVisitCount());
                MyUtils myUtils = new MyUtils(linkStorage,pageRepository, siteRepository, site, lemmaRepository,indexRepository);
                urlList = myUtils.getSiteMap(url);
                //System.out.println(" urlList.size() - "+ urlList.size());
                    taskList = myUtils.taskList(urlList);
                    for (ParseUrl task : taskList) {
                        String taskString = task.fork().toString();
                        //linkStorage.addLinkToSetNotVisit(Collections.singleton(taskString));
                        noDoublesUrlList.add(taskString);
                    }
                    if (taskList.size() > 0) {
                        for (ParseUrl task : taskList) {
                    task.join();
                  }
                    }
                System.out.println("linkStorage.getLinksNotVisitCount()=" + linkStorage.getLinksNotVisitCount());
            }
            if (isShutDown) {
               shutDownFJP();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return Collections.emptySet();
        }
        return noDoublesUrlList;
    }

    public void shutDownFJP() {
        System.out.println("Clearing taskList");
        ForkJoinPool pool = ForkJoinPool.commonPool();
        pool.shutdownNow();
        pool.shutdown();
        System.out.println("Pool.isShutdown() " + pool.isShutdown());
        Optional<Site> site1 = siteRepository.findById(site.getId());
        if (site1.isPresent()) {
            site1.get().setStatus(StatusType.FAILED);
            site1.get().setLastError("Индексация остановлена пользователем");
            siteRepository.save(site1.get());
            ParseUrl.setIsIndexing(false);
        }
    }
}
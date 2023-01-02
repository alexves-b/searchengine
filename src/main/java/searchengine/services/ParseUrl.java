package searchengine.services;

import searchengine.model.Page;
import searchengine.model.PageRepository;
import searchengine.model.Site;

import java.util.*;
import java.util.concurrent.RecursiveTask;


public class ParseUrl extends RecursiveTask<Set<String>> {

    private final String url;
    PageRepository pageRepository;

    Site site;

    public ParseUrl(String url, PageRepository pageRepository) {
        this.url = url;
        this.pageRepository = pageRepository;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public Site getSite() {
        return site;
    }

    @Override
    protected Set <String> compute() throws ConcurrentModificationException{
        try {
            MyUtils myUtils = new MyUtils(pageRepository,site);
            System.out.println(url);
            List <String> urlList = new ArrayList<>(myUtils.getSiteMap(url));
            List <ParseUrl> taskList = myUtils.taskList(urlList);
            for (ParseUrl task : taskList) {
                task.fork();
            }
            for (ParseUrl task : taskList) {
                MyUtils.noDoublesUrlList.addAll(task.join());
            }

        } catch (ConcurrentModificationException exception) {
            exception.printStackTrace();
        }
        return MyUtils.noDoublesUrlList;
    }
}

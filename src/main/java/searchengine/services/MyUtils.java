package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.model.*;

import java.util.*;

public class MyUtils{
    public static Set<String> noDoublesUrlList = Collections.synchronizedSet(new LinkedHashSet<>());
    static TreeSet <String> result = new TreeSet<>();
    Site site;

    public Set<String> getNoDoublesUrlList() {
        return noDoublesUrlList;
    }

    public MyUtils(PageRepository pageRepository,Site site) {
        this.pageRepository = pageRepository;
        this.site = site;
    }

    @Autowired
    PageRepository pageRepository;

    public TreeSet<String> getSiteMap(String url){
        result.clear();

        try {
            Thread.sleep(300);
            Document doc = Jsoup.connect(url)
                    .userAgent("AlexSearchBot2")
                    .ignoreContentType(true).ignoreHttpErrors(true)
                    .get();

            Connection.Response response = doc.connection().response();
            int code = response.statusCode();



            if (doc.html().contains("<!doctype html>") && url.startsWith(site.getUrl())){
                Page page = new Page(site,url.substring(site.getUrl().length()),code, doc.html());
                pageRepository.save(page);
                System.out.println(pageRepository.count());
                   // System.out.println(url + " - " + site.getUrl());
            }
            //LemmaFinder lemmaFinder = LemmaFinder.getInstance();
            //System.out.println(lemmaFinder.getLemmaSet(doc.wholeText()));
            //Map<String,Integer> lemmas = lemmaFinder.collectLemmas(doc.html());

            Elements element = doc.select("a");
            for (int i = 0; i < element.size(); i++) {
                synchronized ( element) {
                        if(!noDoublesUrlList.contains(element.get(i).absUrl("href"))
                                && !element.get(i).absUrl("href").contains("#")&& url.startsWith(site.getUrl()))  {
                            //System.out.println(site.getUrl() + " site.getUrl()" + url + " url");
                            noDoublesUrlList.add(element.get(i).attr("abs:href"));
                            result.add(element.get(i).attr("abs:href"));
                        }
                }
            }
    } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
}
public List<ParseUrl> taskList(List<String> urls) {
        List<ParseUrl> taskList = new ArrayList<>();
        for (String url: urls) {
            ParseUrl parseUrl = new ParseUrl(url,pageRepository);
            parseUrl.setSite(site);
            taskList.add(parseUrl);
        }
        return taskList;
}

}
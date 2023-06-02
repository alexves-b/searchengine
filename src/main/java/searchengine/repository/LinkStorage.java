package searchengine.repository;

import org.springframework.stereotype.Repository;
import searchengine.model.Site;

import java.util.*;

@Repository
public class LinkStorage {
    private final Set<String> noDoublesUrlListNotVisit;
    private final Set<String> visitedLink = Collections.synchronizedSet(new LinkedHashSet<>());

    public LinkStorage(Set<String> noDoublesUrlList) throws InterruptedException {
        this.noDoublesUrlListNotVisit = noDoublesUrlList;
    }
    public synchronized Set<String> getNoDoublesUrlListNotVisit() {
        return noDoublesUrlListNotVisit;
    }
    public synchronized void addLinkToSetNotVisit(Set <String> set){
        //System.out.println(getLinksVisitCount());
        if (set != null) {
            noDoublesUrlListNotVisit.addAll(set);
        }
    }

    public synchronized void addLinkToSetVisit(String url){
        //System.out.println(getLinksVisitCount());
        if (url != null) {
            visitedLink.add(url);
        }
    }

    public int getLinksNotVisitCount(){
        return noDoublesUrlListNotVisit.size();
    }
    public int getLinksVisitCount(){
        return visitedLink.size();
    }

    public synchronized Set<String> getVisitedLinkSet() {
        return new LinkedHashSet<>(visitedLink);
    }

    public synchronized String deleteLinkAfterEndIndexingSite(Site site){
        visitedLink.removeIf(string -> string.startsWith(site.getUrl()));
        return "links for site=" + site.getName() + " was deleted From setVisitedLinks"  ;
    }
}

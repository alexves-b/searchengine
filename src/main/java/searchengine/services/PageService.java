package searchengine.services;

import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

public interface PageService{
    Page addPage(Page page);
    void delete(int id);

    Page editPage(Page page);
    List<Page> getAll();


}

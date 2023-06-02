package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page,Integer> {

    Page findPageByPathAndSiteId(String path,Integer siteId);

    List<Page> findDuplicateByPathAndSite(String path, Site site);

    List <Page> getCountPageBySiteId(Integer site_id);

    List <Page> findPageBylemmaEntitiesIdAndSiteId(Integer lemma_id, Integer site_id);

    void deleteAllPageBySiteId(Integer id);
}

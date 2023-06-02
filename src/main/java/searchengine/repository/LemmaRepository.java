package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository <Lemma,Integer> {

  List<Lemma> findByLemmaAndSiteId(String lemma,Integer siteId);
  List <Lemma> findListByLemmaAndSiteId(String lemma,Integer siteId);

  List<Lemma> findByLemma(String lemma);

  List <Lemma> getCountLemmaBySiteId(Integer site_id);

  void deleteAllLemmaBySiteId(Integer id);


}

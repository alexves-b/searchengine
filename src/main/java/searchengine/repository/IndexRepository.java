package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Index;

import java.util.List;

public interface IndexRepository extends JpaRepository <Index,Integer> {
    void deleteAllIndexByPageId(Integer pageId);

    List <Index> findAllIndexIdByLemmaId(Integer lemma_id);

    Index findIndexIdByLemmaIdAndPageId(Integer lemma_id,Integer page_id);

    List <Index> findIndexesByPageId(Integer page_id);
}

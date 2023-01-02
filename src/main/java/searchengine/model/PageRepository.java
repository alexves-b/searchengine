package searchengine.model;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;

@Repository
public interface PageRepository extends JpaRepository<Page,Integer> {

    List<Page> findDuplicateByPath(String path);
}

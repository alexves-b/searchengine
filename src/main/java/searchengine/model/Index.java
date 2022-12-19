package searchengine.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "`index`")
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "page_id",nullable = false)
    int pageId;

    @Column(name = "lemma_id",nullable = false)
    int lemmaId;

    @Column(name = "`rank`",nullable = false)
    float rank;

}

package searchengine.model;

import lombok.*;

import javax.persistence.*;
import javax.persistence.ForeignKey;

@Entity
@Table(name = "`index`")
@NoArgsConstructor
@Getter
@Setter
public class Index {


    @javax.persistence.Id
    @Column(nullable = false)
    @SequenceGenerator(
            name = "index_seq",
            sequenceName = "index_sequence",
            allocationSize = 500)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "index_seq")
    int id;

    public Index(Page page, Lemma lemma, float rank) {
        this.page = page;
        this.lemma = lemma;
        this.rank = rank;
    }
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_index_page_id"), name = "page_id", nullable = false)
    Page page;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_index_lemma_id"), name = "lemma_id", nullable = false)
    Lemma lemma;

    @Column(name = "`rank`",nullable = false)
    float rank;

    @Override
    public String toString() {
        return "Index{" +
                "id=" + id +
                ", page=" + page +
                ", lemma=" + lemma +
                ", rank=" + rank +
                '}';
    }
}

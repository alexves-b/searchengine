package searchengine.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.persistence.Index;

@Table(name = "lemma", indexes =@Index (name = "lemma_index", columnList = "lemma, site_id, id", unique = true))
@Entity
@Getter
@Setter
public class Lemma implements Comparable<Lemma>{

    @Id
    @Column(nullable = false)
    @SequenceGenerator(
            name = "lemma_seq",
            sequenceName = "lemma_sequence",
            allocationSize = 250)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lemma_seq")
    int id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "site_id",nullable = false)
    Site site;


    @Column(nullable = false)
    String lemma;

    @Column(nullable = false)
    int frequency;

    public Lemma() {
    }

    public Lemma(Site site, String lemma, int frequency) {
        this.site = site;
        this.lemma = lemma;
        this.frequency = frequency;
    }

    public int compareTo(Lemma o) {
        return Double.compare(getFrequency(),o.getFrequency());
    }


    @Override
    public String toString() {
        return "Lemma{" +
                "id=" + id +
                ", site_id=" + site.getId() +
                ", lemma='" + lemma + '\'' +
                ", frequency=" + frequency +
                '}';
    }
}

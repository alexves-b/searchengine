package searchengine.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "lemma")
public class Lemma{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "site_id",nullable = false)
    int siteId;

    @Column(nullable = false)
    String lemma;

    @Column(nullable = false)
    int frequency;

}

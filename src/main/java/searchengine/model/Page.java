package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Setter
@NoArgsConstructor
@Getter
@Table(name = "page", indexes = @javax.persistence.Index(name = "path_siteId_index", columnList = "path, site_id", unique = true))

public class Page implements Comparable<Page>{

    public Page(Site site, String path, int code, String content,String title) {
        this.site = site;
        this.path = path;
        this.code = code;
        this.content = content;
        this.title = title;
    }


    @Id
    @Column(name = "id", nullable = false)
    @SequenceGenerator(
            name = "page_seq",
            sequenceName = "page_sequence",
            allocationSize = 40)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "page_seq")
    Integer id;

    public void setSite(Site site) {
        this.site = site;
    }

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Site.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(foreignKey = @ForeignKey(name = "site_page_FK"), columnDefinition = "Integer",
            referencedColumnName = "id", name = "site_id", nullable = false, updatable = false)
    private Site site;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "search_index",
            joinColumns = {@JoinColumn(name = "page_id")},
            inverseJoinColumns = {@JoinColumn(name = "lemma_id")})
    private Set<Lemma> lemmaEntities = new HashSet<>();

    @Column(nullable = false,length = 1100)
    String path;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return path.equals(page.path) && content.equals(page.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, content);
    }

    @Column(nullable = false)
    int code;

    @Column(columnDefinition = "MEDIUMTEXT",nullable = false)
    String content;

    String title;

    @Override
    public int compareTo(Page o) {
        return CharSequence.compare(getPath(),o.getPath());
    }

    @Override
    public String toString() {
        return "Page{" + "site=" + site +
                "id=" + id +
                ", path='" + path + '\'' +
                ", code=" + code +
                '}';
    }
}

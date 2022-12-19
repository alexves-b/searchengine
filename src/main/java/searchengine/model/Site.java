package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "site")
@NoArgsConstructor

public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('INDEXING', 'INDEXED', 'FAILED')",nullable = false)
    StatusType status;

    @Column(name = "status_time",nullable = false)
    private LocalDateTime statusTime;

    @Column(name = "last_error")
    private String lastError;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "site")
    private Set<Page> pageSet;


    public Site(int site_id, StatusType status, LocalDateTime statusTime, String url, String name) {
        this.id = site_id;
        this.status = status;
        this.statusTime = statusTime;
        this.url = url;
        this.name = name;
    }

}

package searchengine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Url {
    String url;
    boolean configContainsUrl = false;
    public Url(String url) {
        this.url = url;
    }
}

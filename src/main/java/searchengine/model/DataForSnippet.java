package searchengine.model;

@lombok.Data
public class DataForSnippet implements Comparable<DataForSnippet> {

   String site;

   String siteName;
   String uri;

   String title;

   String snippet;

   Double relevanse;

   @Override
   public int compareTo(DataForSnippet o) {
      return Double.compare(o.getRelevanse(),getRelevanse());
   }
}

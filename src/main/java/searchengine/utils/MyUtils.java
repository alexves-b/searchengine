package searchengine.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import searchengine.model.*;
import searchengine.repository.*;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class MyUtils {
    TreeSet<String> result = new TreeSet<>();
    final Site site;


    public MyUtils(LinkStorage linkStorage, PageRepository pageRepository, SiteRepository siteRepository, Site site, LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.linkStorage = linkStorage;
        this.site = site;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }


    LinkStorage linkStorage;


    PageRepository pageRepository;

    SiteRepository siteRepository;

    final LemmaRepository lemmaRepository;

    IndexRepository indexRepository;


    public Set<String> getSiteMap(String url) {

        result = new TreeSet<>();
        try {
            Thread.sleep(160);
            if (url.startsWith(site.getUrl()) && !url.contains("#")
                    && !linkStorage.getVisitedLinkSet().contains(url) &&
            !url.startsWith("[") &&
                    ((url.startsWith("http") || url.startsWith("www")))) {
                    System.setProperty("javax.net.ssl.trustStore", "C:\\Program Files\\Java\\jdk-17\\lib\\security\\cacerts");
                   IndexingData response = indexingOnePage(url);

                   if (response == null) {
                       return Collections.emptySet();
                   }
                    updateSiteIndexingTime();
                    result = response.getResultList();

                    if (result.size() > 0) {
                        linkStorage.addLinkToSetNotVisit(result);
                    }

                }

        } catch (Exception ex) {
            if (url.equals(site.getUrl())) {
                site.setLastError("главная страница сайта не доступна");
                siteRepository.save(site);
            }
            System.out.println(url);
            ex.printStackTrace();
        }

        return result;
    }

    public List<ParseUrl> taskList(Set<String> urls) {
        List<ParseUrl> taskList = new ArrayList<>();
        for (String url : urls) {
            ParseUrl parseUrl = new ParseUrl(linkStorage, url, site, pageRepository, siteRepository, lemmaRepository, indexRepository);
            taskList.add(parseUrl);
        }
        return taskList;
    }

    public ArrayList<String> clearHtmlTag(Document doc) {
        String[] textWithoutHtmlTags = doc.html().replaceAll("[^А-я]{2,50}", " ").split("\\s");
        ArrayList<String> wordsForLemmas = new ArrayList<>();
        for (String string : textWithoutHtmlTags) {
            string = string.trim();
            if (!string.isBlank() && string.length() > 2) {
                wordsForLemmas.add(string);
            }
        }
        return wordsForLemmas;
    }

    public Set<Index> getIndexesForLemma(Lemma lemma, Map<String, Integer> lemmaMap, Page page) {
        Set <Index> indexesSet = new HashSet<>();
        for (Map.Entry<String, Integer> elem : lemmaMap.entrySet()) {
            Integer frequencyOnThePage = elem.getValue();
            if (lemma.getLemma().equals(elem.getKey()) && Objects.equals(lemma.getSite().getId(), site.getId())) {
                if (page != null) {
                    //System.out.println(" pageFromRep.getPath()" + pageFromRep.getPath());
                    Index index = new Index(page, lemma, frequencyOnThePage);
                    indexesSet.add(index);
                }
            }
        }
        return  indexesSet;
    }

    public void saveLemmaSetAndIndextoDb(Document doc, Page page) {

        String clearHtmlTagToString = String.valueOf(clearHtmlTag(doc));
        try {
            LemmaFinder lemmaFinder = LemmaFinder.getInstance();
            // System.out.println(lemmaFinder.getLemmaSet(clearHtmlTagToString));
            Set<String> lemmaSet = lemmaFinder.getLemmaSet(clearHtmlTagToString);
            Map<String, Integer> lemmaMap = lemmaFinder.collectLemmas(clearHtmlTagToString);
            Set <Index> indexesSet = new HashSet<>();
            for (String s : lemmaSet) {
                //рассмотреть вариант получения 1 леммы.
                if (lemmaMap.get(s) != null) {
                    synchronized (lemmaRepository) {
                        List<Lemma> lemmaOptional = lemmaRepository.findListByLemmaAndSiteId(s, site.getId());
                        //настроить синхронизацию лемм при заливке в базу
                        Lemma lemma = new Lemma(site, s, 1);
                        if (lemmaOptional.size() > 0) {
                            if (lemmaOptional.size() >1) {
                                System.out.println("lemmaOptional.size()=" + lemmaOptional.size());
                            }
                            lemmaOptional.get(0).setFrequency(lemmaOptional.get(0).getFrequency() + 1);
                            lemma = lemmaRepository.save(lemmaOptional.get(0));
                        } else {
                            lemma = lemmaRepository.save(lemma);
                        }
                        indexesSet.addAll(getIndexesForLemma(lemma, lemmaMap, page));
                    }
                }
            }
            indexRepository.saveAll(indexesSet);
            //вынести сохранение индексов вот сюда?
        } catch (Exception ex) {
            ex.printStackTrace();
        }



    }

    public void updateSiteIndexingTime() {
        Optional<Site> optionalSite = siteRepository.findById(site.getId());
        if (optionalSite.isPresent()) {
            optionalSite.get().setStatusTime(LocalDateTime.now().plusHours(4));
            optionalSite.get().setStatus(StatusType.INDEXING);
            //System.out.println(optionalSite.get().getId()  + " " + optionalSite.get().getUrl());
            //page.setSite(optionalSite.get());
            siteRepository.save(optionalSite.get());
        }
    }

    public synchronized Integer findDuplicatePageAndSavetoDb(Page page) {
        int pageId = 0;
        List<Page> duplicate = pageRepository.findDuplicateByPathAndSite(page.getPath(), site);
        //   System.out.println("duplicate.size() - " + duplicate.size());
        if (duplicate.size() == 0) {
            pageId = pageRepository.save(page).getId();
            //    System.out.println("Пишу страничку в базу");
        } else {
            for (Page value : duplicate) {
                if (!value.getPath().equals(page.getPath())) {
                    pageId = pageRepository.save(page).getId();
                } else {
                    pageId = value.getId();
                }
            }
        }
        return pageId;
    }


    @Transactional
    public String prepareToIndexOnePage(String url, Site site) {
        String urlSubstringForSearch = url.substring(site.getUrl().length() - 1);
        Optional<Page> pageFromRepository = Optional.ofNullable(pageRepository.findPageByPathAndSiteId(urlSubstringForSearch, site.getId()));
        if (pageFromRepository.isPresent()) {
            List<Index> listIndexes = indexRepository.findIndexesByPageId(pageFromRepository.get().getId());
            for (Index index : listIndexes) {
                Optional<Lemma> lemma = lemmaRepository.findById(index.getLemma().getId());
                lemma.ifPresent(lemma1 -> {
                    lemma1.setFrequency(lemma1.getFrequency() - 1);
                    System.out.println(lemma1.getFrequency());
                    lemmaRepository.save(lemma1);
                });
                indexRepository.deleteById(index.getId());
            }
            pageRepository.deleteById(pageFromRepository.get().getId());
        }
        try {
            IndexingData response = indexingOnePage(url);
            if (response!= null) {
                return "индексация страницы прошла успешно";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "индексация произошла с ошибкой";
    }

    public IndexingData connectToPageAndReturnDocument(String url) {
        IndexingData indexingData = new IndexingData();
        TreeSet <String> listStringFromPage;
        Document doc2;
        try {
            Connection.Response jsoupResponse = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .userAgent("Alex Bot")
                    .execute();

            Document doc = jsoupResponse.parse();
            int code = jsoupResponse.statusCode();
            //добавить сюда жпг пнг и пдф

            //System.out.println(url);
            String title = doc.title();
            String textAfterClean = Jsoup
                    .clean(doc.html(), Safelist.simpleText())
                    .replaceAll("[^А-Яа-яЁё\\d\\s,.!]+", " ");

            doc2 = new Document(site.getUrl());
            doc2.text(textAfterClean);
            doc2.title(title);
            indexingData.setDocument(doc2);
            indexingData.setCode(code);
            listStringFromPage = getLinksFromDocument(doc,url,site);
            indexingData.setResultList(listStringFromPage);

            //System.out.println(linkStorage.getLinksNotVisitCount());
            //System.out.println("linkStorage.getVisitedLinkSet().contains(url)" + linkStorage.getVisitedLinkSet().contains(url));
            //System.out.println(linkStorage.getLinksVisitCount());

            //System.out.println("Число ссылок после удаления " + linkStorage.getLinksVisitCount());
            //метод леманизации и записи в базу лемм и индексов
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return indexingData;
    }


    private TreeSet<String> getLinksFromDocument(Document doc,String url, Site site){
        TreeSet <String> linksFromDocument = new TreeSet<>();
        Elements element = doc.select("a");

        for (int i = 0; i < element.size(); i++) {
            String elementForAdd = element.get(i).attr("abs:href").toLowerCase();

            if (elementForAdd.startsWith(site.getUrl()) &&
                    !elementForAdd.contains("#")
                    && !linkStorage.getVisitedLinkSet().contains(elementForAdd)) {
                if (elementForAdd.endsWith(".jpg")
                        || elementForAdd.endsWith(".jpeg")
                        || elementForAdd.endsWith(".png")
                        || elementForAdd.endsWith(".pdf")
                        || elementForAdd.endsWith(".doc")
                        || elementForAdd.endsWith(".xlsx")
                        ||elementForAdd.endsWith(".xls")
                        || elementForAdd.contains("month=")
                        || elementForAdd.contains("=ica")
                        || elementForAdd.contains("?")
                        || elementForAdd.contains("&")
                        || elementForAdd.equals(url))
                {
                    continue;
                }
                linksFromDocument.add(elementForAdd);
                              //System.out.println("from result " + elementForAdd);
            }
        }
        return linksFromDocument;
    }

    private void createEntityPageAndIndexingIt(String url,Site site,Document doc,Integer code){
        Page page = new Page(site, url.substring(site.getUrl().length() - 1), code, doc.html(), doc.title());
        syncAddToVisitedLinkStorage(url);
        page.setId(findDuplicatePageAndSavetoDb(page));
        saveLemmaSetAndIndextoDb(doc, page);
    }

    private void syncAddToVisitedLinkStorage(String url){
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        try {
            linkStorage.addLinkToSetVisit(url);
            linkStorage.getNoDoublesUrlListNotVisit().remove(url);
        } finally {
            lock.unlock();
        }
    }

    private IndexingData indexingOnePage(String url) {
        IndexingData response = connectToPageAndReturnDocument(url);
        if (response.getCode() != 200) {
            return null;
        }
        createEntityPageAndIndexingIt(url,site,response.getDocument(), response.getCode());
        return response;
    }
}
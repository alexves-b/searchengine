package searchengine.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.response.QueryResponse;
import searchengine.services.QuerryService;
import searchengine.utils.LemmaFinder;

import java.io.IOException;
import java.util.*;

@Service
public class QuerrySericeImpl implements QuerryService {

    @Autowired
    LemmaRepository lemmaRepository;
    @Autowired
    SiteRepository siteRepository;
    Site site;
    @Autowired
    PageRepository pageRepository;

    @Autowired
    IndexRepository indexRepository;

    HashSet<Page> pagesWithFirstLemma = new HashSet<>();
    List<Lemma> listAllLemmaFromQuery = new ArrayList<>();

    Integer lemmaNeedToBeGoodFrequencyPage = 0;
    LemmaFinder lemmaFinder;

    {
        try {
            lemmaFinder = LemmaFinder.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    HashMap <Page,Double> pageRankSum = new HashMap<>();
    double maxRankAllPage = 0;


    @Override
    public QueryResponse findQueryFromSiteEngine(String query, String siteUrl, Integer offset, Integer limit) {
        QueryResponse response = new QueryResponse();


        try {
            LemmaFinder lemmaFinder = LemmaFinder.getInstance();
            List<Site> siteListFromDb = siteRepository.findAll();
            if (siteUrl != null) {
                site = findSiteFromSiteUrl(siteUrl, siteListFromDb);
                System.out.println("siteUrl=" + siteUrl);
                addLemmaFromQuerytoList(lemmaFinder,query,site);
            } else {
                for (Site site:siteListFromDb) {
                    addLemmaFromQuerytoList(lemmaFinder,query,site);
                }
            }

            query = query.toLowerCase().replaceAll("[^А-Яа-яёЁ]{2,50}", "");
            Collections.sort(listAllLemmaFromQuery);

            printLemmaListBeforeAndAfterRemoveLemmaFrequencyMore80Persentage();

            // если у лемм сайт id одинаковый - то их в один список. Если нет - то в разные
            //говнометод который надо переписать

            //вроде все работает до этого чудо цикла с методами.

            //кривой кусок текста и

            //Берем сайт, получаем его список лемм.

            for (Site site : siteListFromDb) {
                List<Lemma> siteIdListLemma = listAllLemmaFromQuery.stream()
                        .filter(l -> l.getSite().getId().equals(site.getId())).toList();
                if (siteIdListLemma.size() >= (int)(lemmaNeedToBeGoodFrequencyPage * 0.8)) {
                    System.out.println("Число лемм на сайте соответствует запросу");
                    TreeSet <Page> listPageByLemma = methodReturnPagesContainsLessFrequency(siteIdListLemma);
                    pagesWithFirstLemma.addAll(listPageByLemma);
                }
            }

            System.out.println(pagesWithFirstLemma);

            double allLemmaRankOnPage = 0;

            for (Page page : pagesWithFirstLemma) {
                for (Lemma lemma: listAllLemmaFromQuery) {
                    int pageId =  page.getId();
                    int lemmaId = lemma.getId();
                     Index indexPage = indexRepository.findIndexIdByLemmaIdAndPageId(lemmaId,pageId);
                     if (indexPage != null) {
                         allLemmaRankOnPage += indexPage.getRank();
                     }
                }
                if (maxRankAllPage <= allLemmaRankOnPage) {
                    maxRankAllPage = allLemmaRankOnPage;
                }
                pageRankSum.put(page,allLemmaRankOnPage);
                //System.out.println("allLemmaRankOnPage=" +allLemmaRankOnPage);
                allLemmaRankOnPage = 0;
            }

            //System.out.println(maxRankAllPage);
            System.out.println("offset=" + offset + " limit=" + limit);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    //Рассчет ранка


        if (!query.isBlank()) {

            List <Page> pagelistForShow = new ArrayList<>(pagesWithFirstLemma);
            response.setCount(pagelistForShow.size());

            int countForSubstring = Math.min((offset + limit), pagelistForShow.size());

            Collections.sort(pagelistForShow);

            if (pagelistForShow.size() > 10)  {
               pagelistForShow = pagelistForShow.subList(offset,countForSubstring);
            }

            response.setResult(true);

            List <DataForSnippet> dataForSnippetList = new ArrayList<>();


            for (Page page: pagelistForShow) {

                Double relevance = maxRankAllPage / pageRankSum.get(page);
                Map <Integer,String> durtyWordsMap = lemmaFinder.getDurtyPositionMap(page.getContent());
                Map <Integer,String> lemmaMap = getLemmaMapFromDurtyWordsPosition(durtyWordsMap);

                System.out.println(lemmaMap);

                //System.out.println(page.getContent());
                //рассчет полезности сниппета!?
                //find first?

                //Обходим лемма меп и ищем лучший сниппет используя позиции.
                // Ищем сниппет в который будут входить все леммы из мепы?

                // обрезали по лемму получили по ласт индекс оф точку или запятую перед искомым словом
                // Обрезали после - получили как-то следующий знак препинания после. Если длинна строки меньше
                // общей желаемой длинны - проделали ещё раз.

                //System.out.println(indexForSubstring);
                StringBuilder snippetFromPage = getSnippetFromPageContent(page,lemmaMap,durtyWordsMap);

                //System.out.println(stringForSnippet);

                //Надо посмотреть все позиции лемм в мепе,
                // если они рядом собрать из них сниппет выделяя исходный текст


                DataForSnippet dataForSnippet = new DataForSnippet();
                String siteNameWithoutLastSlash = page.getSite().getUrl().substring(0,page.getSite().getUrl().length()-1);
                dataForSnippet.setSite(siteNameWithoutLastSlash);
                dataForSnippet.setSiteName(page.getSite().getName());
                dataForSnippet.setUri(page.getPath());
                dataForSnippet.setTitle(page.getTitle());
                dataForSnippet.setSnippet( snippetFromPage +"\n" + "relev=" + relevance);
                dataForSnippet.setRelevanse(relevance);
                dataForSnippetList.add(dataForSnippet);
            }
            Collections.sort(dataForSnippetList);
            response.setData(dataForSnippetList);


        } else {
            response.setResult(false);
            response.setError("Задан пустой поисквый запрос");
        }
        pagesWithFirstLemma.clear();
        listAllLemmaFromQuery.clear();
        return response;

    }

    private StringBuilder getSnippetFromPageContent(Page page, Map<Integer, String> lemmaMap,
                                                    Map <Integer,String> durtyWordsMap) {
        StringBuilder snippetFromPage = new StringBuilder();
        String stringForSnippet;
        try {

            for (Map.Entry<Integer, String> entry : lemmaMap.entrySet()) {

                stringForSnippet = page.getContent();
                String partContent = stringForSnippet.substring(1,entry.getKey());

                int indexBeforeLemmaToSubstring = Math.min(partContent.lastIndexOf(","),
                        partContent.lastIndexOf("."));
                int indexAfterLemmaToSubstring = Math.max(stringForSnippet.indexOf(",",entry.getKey()),
                        stringForSnippet.indexOf(".",entry.getKey()));
                System.out.println("entry.getKey()=" + entry.getKey()
                        + " indexBeforeLemmaToSubstring=" + indexBeforeLemmaToSubstring
                        + " indexAfterLemmaToSubstring=" +indexAfterLemmaToSubstring);
                stringForSnippet = stringForSnippet.substring(indexBeforeLemmaToSubstring+3,
                        indexAfterLemmaToSubstring);

                System.out.println(durtyWordsMap.get(entry.getKey()));
                if (stringForSnippet.contains(durtyWordsMap.get(entry.getKey()))) {
                    stringForSnippet =stringForSnippet.replace(durtyWordsMap.get(entry.getKey()),
                            "<b>" + durtyWordsMap.get(entry.getKey()) + "</b>");
                }
                snippetFromPage.append(stringForSnippet).append("<br>").append("<br>");
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return snippetFromPage;
    }


    public Site findSiteFromSiteUrl(String siteUrl, List<Site> siteListFromDb){
        Site siteFromQuery = null;
        for (Site site : siteListFromDb) {
            if (site.getUrl().contains(siteUrl)) {
                siteFromQuery = site;
                System.out.println(siteFromQuery.getUrl());
            }
        }
        return siteFromQuery;
    }

    public void addLemmaFromQuerytoList(LemmaFinder lemmaFinder,String query, Site site) {
        String siteUrl = site.getUrl();
        Integer idSiteFromQuerry = site.getId();
        Set<String> lemmaQuery = lemmaFinder.getLemmaSet(query);
        List <Lemma> lemmaFromRepository;
        lemmaNeedToBeGoodFrequencyPage = lemmaQuery.size();
        List <Lemma> tempList = new ArrayList<>();
        for (String lemma : lemmaQuery) {
            if (siteUrl == null) {
                lemmaFromRepository = lemmaRepository.findByLemma(lemma);
            } else {
                lemmaFromRepository = lemmaRepository.findByLemmaAndSiteId(lemma, idSiteFromQuerry);
            }

            tempList.addAll(lemmaFromRepository);
        }
        if(tempList.size()==lemmaQuery.size()) {
            listAllLemmaFromQuery.addAll(tempList);
        }
    }


    public void printLemmaListBeforeAndAfterRemoveLemmaFrequencyMore80Persentage(){
        System.out.println(listAllLemmaFromQuery);

        List <Lemma> listLemmaForCycle = new ArrayList<>(listAllLemmaFromQuery);

        for (Lemma lemmaFromQuery : listLemmaForCycle) {
            float lemmaFromQueryFrequency = lemmaFromQuery.getFrequency();
            int siteIdFromLemmaQuery = lemmaFromQuery.getSite().getId();
            float pageCount = pageRepository.getCountPageBySiteId(siteIdFromLemmaQuery).size();
            //System.out.println("pageCount=" + pageCount);
            //System.out.println("lemmaFromQueryFrequency=" + lemmaFromQueryFrequency);
            float persentageLemmaOnSite = lemmaFromQueryFrequency / pageCount;
            if (persentageLemmaOnSite > 0.8) {
                listAllLemmaFromQuery.remove(lemmaFromQuery);
            }
        }
        System.out.println("Список оставшихся лемм после сортировки и удаления слишком частых");
        System.out.println(listAllLemmaFromQuery);

    }

    public TreeSet<Page> methodReturnPagesContainsLessFrequency(List <Lemma> lemmaFormChouseSiteId){
        TreeSet <Page> pageContainsLessFrequency = new TreeSet<>();
        for (int i = 0; i < lemmaFormChouseSiteId.size();i++) {
            List <Page> tempList = new ArrayList<>();
            List<Index> listIndexByFirstFrequencyLemma = indexRepository.findAllIndexIdByLemmaId(lemmaFormChouseSiteId.get(i).getId());
                for (Index index : listIndexByFirstFrequencyLemma) {
                        tempList.add(index.getPage());
                    }
            if (i > 0) {
                pageContainsLessFrequency.retainAll(tempList);
            } else {
                pageContainsLessFrequency = new TreeSet<>(tempList);
            }
        }

        System.out.println( "Список пейджей 1= " +pageContainsLessFrequency);


    return pageContainsLessFrequency;
    }

    private Map<Integer, String> getLemmaMapFromDurtyWordsPosition(Map<Integer, String> durtyWordsMap)
    {
        Map <Integer,String> lemmaMap = new HashMap<>();
        //System.out.println(durtyWordsMap);

        for (Map.Entry<Integer, String> entry : durtyWordsMap.entrySet()) {
            Set<String> lemmasFromDurtySet = lemmaFinder.getLemmaSet(entry.getValue());
            for (Lemma lemma : listAllLemmaFromQuery) {
                for (String lemmaFromDurtyWords : lemmasFromDurtySet) {
                    if (lemmaFromDurtyWords.equals(lemma.getLemma())) {
                        lemmaMap.put(entry.getKey(), lemma.getLemma());
                    }
                }
            }
        }
        return lemmaMap;
    }
}

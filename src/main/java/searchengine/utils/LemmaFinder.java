package searchengine.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

@Slf4j
public class LemmaFinder {
    private final LuceneMorphology luceneMorphology;
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ","ЧАСТ","МС", "МС-П", "ВВОДН"};

    public static LemmaFinder getInstance() throws IOException {
        LuceneMorphology morphology= new RussianLuceneMorphology();
        return new LemmaFinder(morphology);
    }

    private LemmaFinder(LuceneMorphology luceneMorphology) {
        this.luceneMorphology = luceneMorphology;
    }

    public LemmaFinder(){
        throw new RuntimeException("Disallow construct");
    }

    /**
     * Метод разделяет текст на слова, находит все леммы и считает их количество.
     *
     * @param text текст из которого будут выбираться леммы
     * @return ключ является леммой, а значение количеством найденных лемм
     */
    public Map<String, Integer> collectLemmas(String text) {

        String[] words = arrayContainsRussianWords(text);
        HashMap<String, Integer> lemmas = new HashMap<>();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }


            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);
            if (normalForms.isEmpty()) {
                continue;
            }

            for (String string: normalForms) {
                if (lemmas.containsKey(string)) {
                    lemmas.put(string, lemmas.get(string) + 1);
                } else {
                    lemmas.put(string, 1);
                }
            }
        }

        return lemmas;
    }


    /**
     * @param text текст из которого собираем все леммы
     * @return набор уникальных лемм найденных в тексте
     */
    public Set<String> getLemmaSet(String text) {
        String[] textArray = arrayContainsRussianWords(text);
        Set<String> lemmaSet = new HashSet<>();
        for (String word : textArray) {
            if (!word.isEmpty() && isCorrectWordForm(word)) {
                List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
                if (anyWordBaseBelongToParticle(wordBaseForms)) {
                    continue;

                }
                lemmaSet.addAll(luceneMorphology.getNormalForms(word));
            }
        }
        return lemmaSet;
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    private String[] arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("ё", "е")
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    private boolean isCorrectWordForm(String word) {
        List<String> wordInfo = luceneMorphology.getMorphInfo(word);
        for (String morphInfo : wordInfo) {
            if (morphInfo.matches(WORD_TYPE_REGEX)) {
                return false;
            }
        }
        return true;
    }

    public Map <Integer,String> getDurtyPositionMap(String text) {
        int indexWord = 0;
        int indexEndWord = 0;
        Map <Integer,String> durtyPosition = new HashMap<>();
        String[] split = text.replaceAll("ё", "е")
                .replaceAll("([^А-Яа-я\\s])", " ")
                .trim()
                .split("\\s+");
        for (String s : split) {
            if (indexWord == 0) {
                indexWord = text.indexOf(s);
            } else {
                indexWord = text.indexOf(s, indexEndWord);
                indexEndWord = indexWord + s.length();
            }
            if (indexWord > 0) {
                durtyPosition.put(indexWord, s);
            }

            //Получаем стартовый индекс и дальше получаем следующий индекс оф и записываем ппозицию

        }
        return durtyPosition;
    }

    public Map<Integer,String> getLemmaPositionMap(String text) {

        String[] textArray = arrayContainsRussianWords(text);
        Map<Integer,String> lemmaMap = new HashMap<>();
        for (int i = 0; i < textArray.length;i++) {
            if (!textArray[i].isEmpty() && isCorrectWordForm(textArray[i])) {
                List<String> wordBaseForms = luceneMorphology.getMorphInfo(textArray[i]);
                if (anyWordBaseBelongToParticle(wordBaseForms)) {
                    continue;
                }
                 List <String> listNormalForms = luceneMorphology.getNormalForms(textArray[i]);
                int indexWord = 0;
                int indexEndWord = 0;

                for (String word: listNormalForms) {
                    if (indexWord == 0) {
                        indexWord = text.indexOf(word);
                    } else {
                        indexWord = text.indexOf(word,indexEndWord);
                        indexEndWord = indexWord + word.length();
                    }
                        lemmaMap.put(indexWord,word);
                }
            }
        }
        return lemmaMap;
    }
}

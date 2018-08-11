package it.uniroma1.lcl.babelarity;

import java.util.*;
import java.util.stream.Collectors;


public class Word implements LinguisticObject
{
    private String word;
    private List<String> lemmas;
    private static Map<String, Word> instances = new HashMap<>();

    private Word(String s)
    {
        this.word = s;
        this.lemmas = new ArrayList<>();
    }

    public static Word fromString(String s)
    {
        if(instances.containsKey(s))
            return instances.get(s);
        Word w = new Word(s);
        instances.put(s, w);
        return w;
    }

    public static List<Word> fromListOfString(List<String> l) {return l.stream().map(Word::fromString).collect(Collectors.toList()); }


    public void addLemma(String lemma) {lemmas.add(lemma); }
    public void addLemmas(List<String> lemmas) {this.lemmas.addAll(lemmas); }

    public List<String> getLemmas() {return lemmas; }

    @Override
    public String toString(){return word; }

    @Override
    public boolean equals(Object obj)
    {
        if(obj==this) return true;
        if(obj==null || obj.getClass() != this.getClass()) return false;
        Word w = (Word)obj;
        return w.word.equals(word) && w.lemmas.equals(lemmas);
    }

    @Override
    public int hashCode() {return Objects.hash(word, lemmas); }
}

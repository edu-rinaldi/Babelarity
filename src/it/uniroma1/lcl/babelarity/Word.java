package it.uniroma1.lcl.babelarity;

import java.util.*;
import java.util.stream.Collectors;


public class Word implements LinguisticObject
{
    private String word;
    public static Map<String, Word> instances = new HashMap<>();


    private Word(String s)
    {
        this.word = s.toLowerCase();
    }

    public static Word fromString(String s)
    {
        if(instances.containsKey(s))
            return instances.get(s);
        Word w = new Word(s);
        instances.put(s, w);
        return w;
    }

    public static Collection<Word> fromCollection(Collection<String> c) {return c.stream().map(Word::fromString).collect(Collectors.toList()); }

    @Override
    public String toString(){return word; }

    @Override
    public boolean equals(Object obj)
    {
        if(obj==this) return true;
        if(obj==null || obj.getClass() != this.getClass()) return false;
        Word w = (Word)obj;
        return w.word.equals(word);
    }

    @Override
    public int hashCode() {return word.hashCode(); }
}

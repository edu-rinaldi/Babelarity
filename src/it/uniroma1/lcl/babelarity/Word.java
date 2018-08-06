package it.uniroma1.lcl.babelarity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Word implements LinguisticObject
{
    private String word;
    private List<String> lemmas;

    public Word(String s)
    {
        this.word = s;
        lemmas = new ArrayList<>();
    }

    public static Word fromString(String s)
    {
        return new Word(s);
    }

    public static List<Word> fromListOfString(List<String> l)
    {
        return l.stream().map(Word::fromString).collect(Collectors.toList());
    }

    public List<String> findLemmasFromSource(Path p)
    {
        if (lemmas.size() == 0)
        {
            try(Stream<String> stream = Files.lines(p))
            {
                lemmas = stream
                        .filter(line->line.split("\t")[0].equals(word))
                        .flatMap(line-> Arrays.stream(line.split("\t"))
                                        .skip(1).collect(Collectors.toList())
                                        .stream())
                        .collect(Collectors.toList());
            }
            catch (IOException e){e.printStackTrace(); }
        }
        return lemmas;
    }

    @Override
    public String toString(){return word; }

    @Override
    public boolean equals(Object obj) {
        if(obj==this) return true;
        if(obj==null || obj.getClass() != this.getClass()) return false;
        Word w = (Word)obj;
        return w.word.equals(word) && w.lemmas.equals(lemmas);
    }

    @Override
    public int hashCode() {return Objects.hash(word, lemmas); }
}

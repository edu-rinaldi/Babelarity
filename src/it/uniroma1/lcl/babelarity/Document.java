package it.uniroma1.lcl.babelarity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class Document implements LinguisticObject, Serializable
{
    private String id;
    private String title;
    private String content;

    public Document(String id, String title, String content)
    {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public String getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getContent()
    {
        return content;
    }

    public Set<String> getWords(Set<String> sw)
    {
        return Arrays.stream(getContent().replaceAll("\\W"," ").toLowerCase().split(" "))
                .map(String::trim)
                .filter(w-> !sw.contains(w))
                .collect(Collectors.toSet());
    }

    public Set<String> getWords()
    {
        return getWords(Set.of());
    }
}

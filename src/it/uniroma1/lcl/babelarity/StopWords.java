package it.uniroma1.lcl.babelarity;


import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StopWords
{
    private Set<String> sw;
    private static StopWords instance;

    private StopWords()
    {
        sw = new HashSet<>();
        try(BufferedReader br = Files.newBufferedReader(Paths.get("resources/utils/english-stop-words-large.txt")))
        {
            while(br.ready())
                sw.add(br.readLine());
        }
        catch (IOException e){e.printStackTrace();}
    }

    public static StopWords getInstance()
    {
        if(instance!=null) return instance;
        instance = new StopWords();
        return instance;
    }

    public boolean isStopWord(String w)
    {
        return sw.contains(w);
    }

    public Set<String> toSet() {return sw;}
    public List<String> toList() {return new ArrayList<>(sw);}

}

package it.uniroma1.lcl.babelarity.utils;


import javafx.scene.paint.Stop;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//togliere singleton
public class StopWords
{
    private Set<String> sw;
    private Path path;

    public StopWords(Path p)
    {
        sw = new HashSet<>();
        path = p;
        try(BufferedReader br = Files.newBufferedReader(p))
        {
            while(br.ready())
                sw.add(br.readLine());
        }
        catch (IOException e){e.printStackTrace();}
    }

    public StopWords() {this(Paths.get("resources/utils/english-stop-words-large.txt")); }

    public boolean isStopWord(String w)
    {
        return sw.contains(w);
    }

    public Set<String> toSet() {return sw;}
    public List<String> toList() {return new ArrayList<>(sw);}

    public Path getPathSource() {return path;}

}

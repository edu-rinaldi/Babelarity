package it.uniroma1.lcl.babelarity.utils.stopword;


import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Classe che si occupa del caricamento da file e della gestione di stop words.
 */
public class StopWords
{
    private Set<String> sw;
    private Path path;

    /**
     * Costruttore di oggetti {@code StopWords} sovraccaricato con un parametro che
     * specifica il path del file da dove caricare le stop-words.
     * @param p Specifica il path del file da dove caricare le stop-words.
     */
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

    /**
     * Costruttore di oggetti {@code StopWords} con riferimento di default ad un file.
     */
    public StopWords() {this(Paths.get("src/it/uniroma1/lcl/babelarity/utils/stopword/english-stop-words-large.txt")); }

    /**
     * Metodo di utilità che indica se una certa stringa è una stop word.
     * @param w Stringa da verificare.
     * @return Booleano {@code True} se è una stop word, altrimenti {@code False}.
     */
    public boolean isStopWord(String w) { return sw.contains(w); }

    /**
     * Metodo di utilità che restituisce un {@code Set} contenente tutte le stop words caricate.
     * @return {@code Set} contenente tutte le stop words caricate.
     */
    public Set<String> toSet() {return sw;}

    /**
     * Metodo di utilità che restituisce una lista contenente tutte le stop words caricate.
     * @return Lista contenente tutte le stop words caricate.
     */
    public List<String> toList() {return new ArrayList<>(sw);}

    /**
     * Metodo che restituisce il path del file dal quale abbiamo prelevato le StopWords.
     * @return Restituisce il path del file dal quale abbiamo prelevato le StopWords.
     */
    public Path getPathSource() {return path;}

}

package it.uniroma1.lcl.babelarity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Questa classe incapsula una stringa e permette di trattare gli oggetti {@code String}
 * come {@code LinguisticObject}.
 */
public class Word implements LinguisticObject
{
    private String word;
    private static Map<String, Word> instances = new HashMap<>();


    private Word(String s)
    {
        this.word = s.toLowerCase();
    }

    /**
     * Questo metodo permette alla classe di utilizzare il singleton
     * pattern, così da evitare, su grandi quantità di dati, di costruire
     * tante {@code Word} che incapsulano la stessa stringa.
     * Se la stringa {@code s} e' gia' associata con un oggetto {@code Word} ne ritorna l'istanza,
     * altrimenti ne crea una nuova e associa {@code s} con l'oggetto appena creato.
     *
     * @param s Stringa che deve essere inizializzata come istanza
     *          di {@code Word}.
     * @return La singola istanza di {@code Word} per una certa stringa.
     */
    public static Word fromString(String s)
    {
        if(instances.containsKey(s))
            return instances.get(s);
        Word w = new Word(s);
        instances.put(s, w);
        return w;
    }

    /**
     * Questo metodo applica il singleton pattern su una Collection di {@code String}.
     * Attraverso uno stream su {@code stringCollection} viene fatto un map con riferimento a metodo
     * {@code fromString()} e il tutto viene collezionato in una lista.
     * Con il return c'è un up-cast a {@code Collection}, così da poter avere come parametro un
     * qualsiasi tipo di {@code Collection}.
     * @param stringCollection Una {@code Collection} di {@code String}, dove ogni elemento
     *                         verrà trasformato in {@code Word}.
     * @return {@code Collection} di {@code Word} costruita a partire da una {@code Collection} di {@code String}.
     */
    public static Collection<Word> fromCollection(Collection<String> stringCollection) {return stringCollection.stream().map(Word::fromString).collect(Collectors.toList()); }

    /**
     * Questo metodo fa l'override del metodo {@code toString()} della classe {@code Object}
     * ed inoltre fa da getter per il campo incapsulato.
     * @return Stringa che viene incapsulata con la classe.
     */
    @Override
    public String toString(){return word; }

    /**
     *  {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if(obj==this) return true;
        if(obj==null || obj.getClass() != this.getClass()) return false;
        Word w = (Word)obj;
        return w.word.equals(word);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {return word.hashCode(); }
}

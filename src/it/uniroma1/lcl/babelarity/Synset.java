package it.uniroma1.lcl.babelarity;

import java.util.*;

/**
 * L'interfaccia {@code Synset} definisce i metodi per accedere
 * alle varie informazioni di un Synset, per scrivere
 */
public interface Synset extends LinguisticObject
{
    /**
     * Metodo getter del synset ID.
     * @return ID del {@code Synset}.
     */
    String getID();

    /**
     * Metodo getter del POS (Part of speech).
     * @return POS del {@code Synset}.
     */
    PartOfSpeech getPOS();

    /**
     * Metodo getter della lista dei lemmi/concetti presenti nel synset.
     * @return Lista di lemmi/concetti contenuti nel synset.
     */
    List<String> getLemmas();

    /**
     * Metodo che restituisce un {@code Set} contenente tutti i synset che hanno una
     * relazione di qualunque tipo con questo synset.
     * @return Set contenente tutti i synset che hanno una relazione di qualunque tipo con questo synset.
     */
    Set<Synset> getRelations();

    /**
     * Metodo che restituisce un {@code Set} contenente tutti i synset che hanno una relazione
     * di tipo {@code String typeRel}.
     * @param typeRel Stringa che definisce il tipo di relazioni da cercare.
     * @return {@code Set} contenente tutti i synset che hanno una relazione
     *         di tipo {@code String typeRel}.
     */
    Set<Synset> getRelations(String typeRel);

    /**
     * Metodo che restituisce un {@code Set} contenente tutti i synset che hanno una relazione
     * con almeno un tipo definito dall'array {@code typeRel}.
     * @param typeRel Definisce i tipi di relazioni da cercare.
     * @return {@code Set} contenente tutti i synset che hanno una relazione
     *         con almeno un tipo definito dall'array {@code typeRel}.
     */
    Set<Synset> getRelations(String... typeRel);

    /**
     * Metodo che consente l'aggiunta di una nuova relazione al synset.
     * @param typeRel Definisce il tipo di relazione da aggiungere.
     * @param node Definisce il {@code Synset} da mettere in relazione.
     */
    void addRelation(String typeRel, Synset node);

    /**
     * Metodo che consente di aggiungere un nuovo glosse al {@code Synset}.
     * @param glosse Definisce il glosse da aggiungere al {@code Synset}.
     */
    void addGlosse(String glosse);

    /**
     * Metodo che consente l'aggiunta di più glosse al {@code Synset}.
     * @param glosses Collezione che contiene i glosse da aggiungere al {@code Synset}.
     */
    void addGlosses(Collection<String> glosses);

    /**
     * Metodo che consente l'aggiunta di un nuovo lemma al {@code Synset}.
     * @param lemma Definisce il lemma da aggiungere al {@code Synset}.
     */
    void addLemma(String lemma);

    /**
     * Metodo che consente l'aggiunta di più lemmi al {@code Synset} contemporaneamente.
     * @param lemmas Collezione che contiene i lemmi da aggiungere al {@code Synset}.
     */
    void addLemmas(Collection<String> lemmas);

    /**
     * Metodo che restituisce un {@code Synset} a caso in una collezione.
     * @param s Collezione di {@code Synset} tra cui scegliere.
     * @return {@code Synset} a caso da {@code Collection<Synset> s}.
     */
    static Synset randomNode(Collection<Synset> s)
    {
        //mappo la collezione in lista
        List<Synset> keys = new ArrayList<>(s);
        //numero a random che fungera' da indice
        int ran = new Random().nextInt(keys.size());
        //prendi un elemento a caso nella lista
        return keys.get(ran);
    }


}

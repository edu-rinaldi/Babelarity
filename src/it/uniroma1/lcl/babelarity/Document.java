package it.uniroma1.lcl.babelarity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Questa classe rappresenta un documento caratterizzato dalla seguente struttura:
 * -ID univoco
 * -Titolo
 * -Contenuto
 *
 * La classe implementa l'interfaccia {@code LinguisticObject} poichè è possibile
 * calcolare una similarità fra Documenti implementando l'interfaccia BabelDocumentSimilarity.
 *
 * La classe implementa l'interfaccia {@code Serializable} ed è quindi possibile salvare
 * gli oggetti {@code Document} su file.
 */
public class Document implements LinguisticObject, Serializable
{
    private String id;
    private String title;
    private String content;

    /**
     * Costruttore della classe {@code Document}.
     * @param id Identificativo univoco del documento.
     * @param title Titolo del documento.
     * @param content Contenuto testuale del documento.
     */
    public Document(String id, String title, String content)
    {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    /**
     * Metodo getter del campo ID
     * @return Identificativo del documento.
     */
    public String getId() { return id; }

    /**
     * Metodo getter del titolo del documento.
     * @return Titolo del documento.
     */
    public String getTitle() {return title; }

    /**
     * Metodo getter del contenuto testuale del documento.
     * @return Contenuto testuale del documento.
     */
    public String getContent() {return content; }

    /**
     * Restituisce un insieme contenente tutte le parole
     * del documento. E' possibile filtrare il documento
     * con un insieme di StopWord o parole indesiderate.
     * @param sw Insieme che contiene tutte le StopWords o le parole indesiderate.
     * @return Insieme contenente tutte le parole del documento.
     */
    public Set<String> getWords(Set<String> sw)
    {
        return Arrays.stream(getContent().replaceAll("\\W"," ").toLowerCase().split(" "))
                .map(String::trim)
                .filter(w-> !sw.contains(w))
                .collect(Collectors.toSet());
    }

    /**
     * Restituisce un insieme contenente tutte le parole
     * del documento.
     * @return Insieme contenente tutte le parole del documento.
     */
    public Set<String> getWords() { return getWords(Set.of()); }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if(obj==this) return true;
        if(obj==null || this.getClass()!=obj.getClass()) return false;
        Document d = (Document) obj;
        return this.id.equals(d.id);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {return id.hashCode();}
}

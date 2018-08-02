package it.uniroma1.lcl.babelarity;

import java.util.Iterator;
import java.util.List;

public class MiniBabelNet implements Iterable<Synset>
{

    private static MiniBabelNet instance;
    private List<Synset> synsets;
    private List<String> lemmas;



    private MiniBabelNet()
    {

    }

    static MiniBabelNet getInstance()
    {
        if (instance == null)
            instance = new MiniBabelNet();
        return instance;
    }

    public List<Synset> getSynsets(String word)
    {
        return null;
    }

    public Synset getSynset(String id)
    {
        return null;
    }

    public List<String> getLemmas(String word)
    {
        return null;
    }

    /**
     * Restituisce le informazioni inerenti al Synset fornito in input sotto forma di stringa.
     * Il formato della stringa è il seguente:
     * ID\tPOS\tLEMMI\tGLOSSE\tRELAZIONI
     * Le componenti LEMMI, GLOSSE e RELAZIONI possono contenere più elementi, questi sono separati dal carattere ";"
     * Le relazioni devono essere condificate nel seguente formato:
     * TARGETSYNSET_RELNAME   es. bn:00081546n_has-kind
     *
     * es: bn:00047028n	NOUN	word;intelligence;news;tidings	Information about recent and important events	bn:0000001n_has-kind;bn:0000001n_is-a
     *
     * @param s
     * @return
     */
    public String getSynsetSummary(Synset s)
    {
        return null;
    }

    public void setLexicalSimilarityStrategy()
    {
        //da fare robe...
    }

    public void setSemanticSimilarityStrategy()
    {

    }

    public void setDocumentSimilarityStrategy()
    {

    }

    public double computeSimilarity(LinguisticObject o1, LinguisticObject o2)
    {
        return 0;
    }




    @Override
    public Iterator<Synset> iterator()
    {
        return null;
    }
}

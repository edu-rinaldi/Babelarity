package it.uniroma1.lcl.babelarity;


import java.util.List;

public class BabelSynset implements Synset
{
    private String id;
    private PartOfSpeech pos;
    private List<String> lemmas;
    private List<String> glosses;


    public BabelSynset(String id, List<String>lemmas)
    {
        this.id = id;
        this.lemmas = lemmas;
        this.glosses = loadGloesses(id);
    }

    private List<String> loadGloesses(String id)
    {
        return null;
    }


}

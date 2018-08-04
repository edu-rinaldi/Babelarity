package it.uniroma1.lcl.babelarity;

import java.util.List;
import java.util.Map;

public class BabelSynset implements Synset
{
    private String id;
    private PartOfSpeech pos;
    private List<String> lemmas;
    private List<String> glosses;
    private Map<BabelSynset, String> relations;


    public BabelSynset(String id, List<String> lemmas)
    {
        this.id = id;
        this.pos = PartOfSpeech.getByChar(id.charAt(id.length()-1));
        this.lemmas = lemmas;
        this.glosses = loadGloesses();
        this.relations = loadRelations();
    }

    public String getID() {return id; }
    public PartOfSpeech getPOS() {return pos;}
    public List<String> getLemmas() {return lemmas;}

    private List<String> loadGloesses()
    {
        return null;
    }

    private Map<BabelSynset, String> loadRelations()
    {
        return null;
    }

    @Override
    public String toString() {
        return id+"\t"+pos+"\t"+lemmas+"\t"+glosses+"\t"+relations;
    }
}

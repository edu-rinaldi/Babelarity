package it.uniroma1.lcl.babelarity;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class BabelSynset implements Synset
{
    private String id;
    private PartOfSpeech pos;
    private List<String> words;
    private List<String> glosses;
    private HashMap<String, List<BabelSynset>> relations;


    public BabelSynset(String id, List<String> words)
    {
        this.id = id;
        this.pos = PartOfSpeech.getByChar(id.charAt(id.length()-1));
        this.words = words;
        this.glosses = new ArrayList<>();
        this.relations = new HashMap<>();
    }

    @Override
    public String getID() {return id; }
    public PartOfSpeech getPOS() {return pos;}
    public List<String> getWords() {return words;}
    public List<String> getLemmas() {return words.stream().map(MiniBabelNet.getInstance()::getLemmas).collect(Collectors.toList()); }

    public void addGlosse(String glosse) {glosses.add(glosse); }
    public void addGlosses(List<String> glosses) {this.glosses.addAll(glosses); }
    public void addRelation(String type, BabelSynset node)
    {
        relations.merge(type, new ArrayList<>(List.of(node)), (v1,v2)->{
            v1.addAll(v2);
            return v1;
        });
    }

    @Override
    public String toString()
    {
        String lems = String.join(";", getLemmas().get(0));
        String glos = String.join(";", glosses);
        String rels  = "";
        return id+"\t"+pos+"\t"+lems+"\t"+glos+"\t"+rels;
    }

}

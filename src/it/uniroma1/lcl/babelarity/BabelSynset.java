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
    private Map<String, HashSet<BabelSynset>> relations;
    private int dist;


    public BabelSynset(String id, List<String> words)
    {
        this.id = id;
        this.pos = PartOfSpeech.getByChar(id.charAt(id.length()-1));
        this.words = words;
        this.glosses = new ArrayList<>();
        this.relations = new HashMap<>();
        this.dist = Integer.MAX_VALUE;
    }

    @Override
    public String getID() {return id; }
    public PartOfSpeech getPOS() {return pos;}
    public List<String> getLemmas() {return words;}


    public Set<BabelSynset> getRelations(String typeRel)
    {
        Set<BabelSynset> r = relations.get(typeRel);
        return r!=null ? r: new HashSet<>();
    }

    public Set<BabelSynset> getRelations()
    {
        return relations.entrySet().stream().flatMap(e->e.getValue().stream())
                .collect(toSet());
    }


    public void addRelation(String typeRel, BabelSynset node)
    {
        relations.merge(typeRel, new HashSet<>(Set.of(node)), (v1,v2)->
        {
            v1.addAll(v2);
            return v1;
        } );
    }

    public void setDist(int x) {dist = x;}
    public int getDist() {return dist;}


    public void addGlosse(String glosse) {glosses.add(glosse); }
    public void addGlosses(List<String> glosses) {this.glosses.addAll(glosses); }

    @Override
    public boolean equals(Object obj)
    {
        if(obj==this) return true;
        if(obj==null || obj.getClass() != this.getClass()) return false;
        BabelSynset b = (BabelSynset)obj;
        return this.getID().equals(b.getID());
    }

    @Override
    public int hashCode() {return getID().hashCode(); }

    @Override
    public String toString()
    {
        String lems = String.join(";", words);
        String glos = String.join(";", glosses);
        return id;
//        return id+"\t"+pos+"\t"+lems+"\t"+glos+"\t"+rels;
    }

}

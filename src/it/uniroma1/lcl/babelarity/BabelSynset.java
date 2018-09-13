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
    private Map<String, List<Synset>> relations;

    public BabelSynset(String id)
    {
        this(id, new ArrayList<>());
    }

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

    @Override
    public PartOfSpeech getPOS() {return pos;}

    @Override
    public List<String> getLemmas() {return words;}

    @Override
    public Set<Synset> getRelations(String typeRel)
    {
        List<Synset> r = relations.get(typeRel);
        return r!=null ? new HashSet<>(r) : new HashSet<>();
    }

    @Override
    public Set<Synset> getRelations(String... typeRel)
    {
        return Arrays.stream(typeRel)
                .filter(t->relations.get(t)!=null)
                .flatMap(t->relations.get(t).stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Synset> getRelations()
    {
        return getRelations("allType");
    }

    @Override
    public void addRelation(String typeRel, Synset node)
    {
        relations.merge(typeRel, new ArrayList<>(List.of(node)), (v1,v2)-> { v1.addAll(v2); return v1; } );
        relations.merge("allType", new ArrayList<>(List.of(node)), (v1,v2)-> { v1.addAll(v2); return v1; });
    }

    @Override
    public void addGlosse(String glosse) {glosses.add(glosse); }

    @Override
    public void addGlosses(Collection<String> glosses) {this.glosses.addAll(glosses); }

    @Override
    public void addLemma(String lemma) {words.add(lemma); }

    @Override
    public void addLemmas(Collection<String> lemmas) {words.addAll(lemmas); }


    @Override
    public boolean equals(Object obj)
    {
        if(obj==this) return true;
        if(obj==null || obj.getClass() != this.getClass()) return false;
        BabelSynset b = (BabelSynset)obj;
        return this.getID().equals(b.getID()) && this.pos==b.pos && this.words.equals(b.words);
    }

    @Override
    public int hashCode() {return Objects.hash(id,pos, words); }

    @Override
    public String toString()
    {
        String lems = String.join(";", words);
        String glos = String.join(";", glosses);
        String rels = relations.entrySet()
                .stream()
                .filter(e->!e.getKey().equals("has-kind2") && !e.getKey().equals("allType"))
                .flatMap(e->e.getValue().stream().map(v->v.getID()+"_"+e.getKey()))
                .collect(joining(";"));
        return id+"\t"+pos+"\t"+lems+"\t"+glos+"\t"+rels;
    }



}

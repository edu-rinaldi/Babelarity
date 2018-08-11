package it.uniroma1.lcl.babelarity;

import java.util.*;
import static java.util.stream.Collectors.*;

public class BabelSynset implements Synset
{
    private String id;
    private PartOfSpeech pos;
    private List<Word> words;
    private List<String> glosses;
    private List<Relation> relations;


    public BabelSynset(String id, List<Word> words)
    {
        this.id = id;
        this.pos = PartOfSpeech.getByChar(id.charAt(id.length()-1));
        this.words = words;
        this.glosses = new ArrayList<>();
        this.relations = new ArrayList<>();
    }

    @Override
    public String getID() {return id; }
    public PartOfSpeech getPOS() {return pos;}
    public List<Word> getWords() {return words;}
    public List<List<String>> getLemmas() {return words.stream().map(Word::getLemmas).collect(toList()); }

    public void addGlosse(String glosse) {glosses.add(glosse); }
    public void addGlosses(List<String> glosses) {this.glosses.addAll(glosses); }
    public void addRelation(Relation r) { relations.add(r); }
    public void addRelations(List<Relation> relations) {this.relations.addAll(relations); }

    @Override
    public String toString()
    {
        String lems = String.join(";", getLemmas().get(0));
        String glos = String.join(";", glosses);
        String rels  = relations.stream().map(Relation::toString).collect(joining(";"));
        return id+"\t"+pos+"\t"+lems+"\t"+glos+"\t"+rels;
    }

}

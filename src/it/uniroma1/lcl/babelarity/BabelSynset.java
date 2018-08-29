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
    private HashSet<BabelSynset> neighbours;
    private int dist;


    public BabelSynset(String id, List<String> words)
    {
        this.id = id;
        this.pos = PartOfSpeech.getByChar(id.charAt(id.length()-1));
        this.words = words;
        this.glosses = new ArrayList<>();
        this.neighbours = new HashSet<>();
        this.dist = Integer.MAX_VALUE;
    }

    @Override
    public String getID() {return id; }
    public PartOfSpeech getPOS() {return pos;}
    public List<String> getLemmas() {return words;}


    public void addNeighbour(BabelSynset n)
    {
        neighbours.add(n);
    }
    public Set<BabelSynset> getNeighbours()
    {
        return neighbours;
    }
    public void setDist(int x) {dist = x;}
    public int getDist() {return dist;}


    public void addGlosse(String glosse) {glosses.add(glosse); }
    public void addGlosses(List<String> glosses) {this.glosses.addAll(glosses); }


    @Override
    public String toString()
    {
        String lems = String.join(";", words);
        String glos = String.join(";", glosses);
        return id+"\t";
//        return id+"\t"+pos+"\t"+lems+"\t"+glos+"\t"+rels;
    }

}

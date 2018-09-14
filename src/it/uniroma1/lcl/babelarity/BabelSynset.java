package it.uniroma1.lcl.babelarity;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * Classe che rappresenta un BabelSynset ovvero una implementazione
 * dell'interfaccia {@code Synset}.
 */
public class BabelSynset implements Synset
{
    private String id;
    private PartOfSpeech pos;
    private List<String> words;
    private List<String> glosses;
    private Map<String, List<Synset>> relations;

    /**
     * Costruttore di un {@code BabelSynset} che richiede
     * l'identificativo del {@code Synset}
     * @param id Identificativo del {@code Synset}.
     */
    public BabelSynset(String id)
    {
        this(id, new ArrayList<>());
    }

    /**
     * Overload del costruttore di un {@code BabelSynset}
     * che richiede l'identificativo del {@code Synset} e
     * una lista dei suoi concetti.
     * @param id Identificativo del {@code Synset}.
     * @param words Lista di concetti del {@code Synset}.
     */
    public BabelSynset(String id, List<String> words)
    {
        this.id = id;
        this.pos = PartOfSpeech.getByChar(id.charAt(id.length()-1));
        this.words = words;
        this.glosses = new ArrayList<>();
        this.relations = new HashMap<>();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public String getID() {return id; }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public PartOfSpeech getPOS() {return pos;}

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public List<String> getLemmas() {return words;}

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public Set<Synset> getRelations(String typeRel)
    {
        List<Synset> r = relations.get(typeRel);
        return r!=null ? new HashSet<>(r) : new HashSet<>();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public Set<Synset> getRelations(String... typeRel)
    {
        if(typeRel.length==0) return getRelations();
        return Arrays.stream(typeRel)
                .filter(t->relations.get(t)!=null)
                .flatMap(t->relations.get(t).stream())
                .collect(Collectors.toSet());
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public Set<Synset> getRelations()
    {
        return getRelations("allType");
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void addRelation(String typeRel, Synset node)
    {
        relations.merge(typeRel, new ArrayList<>(List.of(node)), (v1,v2)-> { v1.addAll(v2); return v1; } );
        relations.merge("allType", new ArrayList<>(List.of(node)), (v1,v2)-> { v1.addAll(v2); return v1; });
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void addGlosse(String glosse) {glosses.add(glosse); }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void addGlosses(Collection<String> glosses) {this.glosses.addAll(glosses); }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void addLemma(String lemma) {words.add(lemma); }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void addLemmas(Collection<String> lemmas) {words.addAll(lemmas); }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if(obj==this) return true;
        if(obj==null || obj.getClass() != this.getClass()) return false;
        BabelSynset b = (BabelSynset)obj;
        return this.getID().equals(b.getID()) && this.pos==b.pos && this.words.equals(b.words);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {return Objects.hash(id,pos, words); }

    /**
     *  Questo metodo restituisce una stringa che rappresenta lo stato del {@code BabelSynset}.
     *  Con formato id\tpos\tlemma1;lemma2;lemmaN\tglosse1;glosse2;glosseN\trel1;rel2;relN
     */
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

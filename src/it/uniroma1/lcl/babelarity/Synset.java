package it.uniroma1.lcl.babelarity;

import java.util.*;

public interface Synset extends LinguisticObject
{
    String getID();

    PartOfSpeech getPOS();

    List<String> getLemmas();

    Set<Synset> getRelations();

    Set<Synset> getRelations(String typeRel);

    Set<Synset> getRelations(String... typeRel);

    void addRelation(String typeRel, Synset node);

    void addGlosse(String glosse);

    void addGlosses(Collection<String> glosses);

    static Synset randomNode(Collection<Synset> s)
    {
        //mappo la collezione in lista
        List<Synset> keys = new ArrayList<>(s);
        //numero a random che fungera' da indice
        int ran = new Random().nextInt(keys.size());
        //prendi un elemento a caso nella lista
        return keys.get(ran);
    }


}

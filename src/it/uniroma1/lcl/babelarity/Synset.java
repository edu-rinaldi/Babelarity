package it.uniroma1.lcl.babelarity;

import java.util.List;
import java.util.Set;

public interface Synset extends LinguisticObject
{
    String getID();
    Set<? extends Synset> getRelations(String typeRel);

}

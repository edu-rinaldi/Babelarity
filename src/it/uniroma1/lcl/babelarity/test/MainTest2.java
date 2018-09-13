package it.uniroma1.lcl.babelarity.test;

import it.uniroma1.lcl.babelarity.*;
import it.uniroma1.lcl.babelarity.exception.NotASynsetException;
import javafx.util.Pair;

import java.util.*;

public class MainTest2
{

    public static void main(String[] args) {
        MiniBabelNet b = MiniBabelNet.getInstance();


        Synset s1 = b.getSynset("bn:00034472n");

        Synset s2 = b.getSynset("bn:00015008n");
        Synset s3 = b.getSynset("bn:00081546n");
        Synset s4 = b.getSynset("bn:00070528n");
        Synset ss = b.getSynset("bn:00036821n");

        Synset s5 = b.getSynset("bn:00024712n");
        Synset s6 = b.getSynset("bn:00029345n");
        Synset s7 = b.getSynset("bn:00035023n");
        Synset s8 = b.getSynset("bn:00010605n");

        List<Pair<Synset,Synset>> tests = List.of(
                new Pair<>(ss,ss),
                new Pair<>(s1,s2),
                new Pair<>(s1,s3),
                new Pair<>(s3,s4),
                new Pair<>(s2,s4),
                new Pair<>(s5,s6),
                new Pair<>(s5,s7),
                new Pair<>(s7,s8),
                new Pair<>(s6,s8)
        );


        for(Pair<Synset,Synset> p: tests) {
            System.out.println(b.computeSimilarity(p.getKey(),p.getValue()));
        }

    }



}

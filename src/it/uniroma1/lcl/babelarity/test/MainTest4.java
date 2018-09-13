package it.uniroma1.lcl.babelarity.test;


import it.uniroma1.lcl.babelarity.MiniBabelNet;
import it.uniroma1.lcl.babelarity.Synset;

import java.util.HashMap;
import java.util.stream.Collectors;

public class MainTest4
{
    public static void main(String[] args) {
        MiniBabelNet mb = MiniBabelNet.getInstance();
        HashMap<String, Integer> occ = new HashMap<>();
        for(Synset s : mb)
            occ.merge(s.getID()+s.getPOS(),1,(v1,v2)->v1+v2);
        System.out.println(occ.entrySet().stream().filter(e->e.getValue()>1).collect(Collectors.toList()));
    }
}

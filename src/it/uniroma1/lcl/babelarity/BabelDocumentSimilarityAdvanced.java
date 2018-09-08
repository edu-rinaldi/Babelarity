package it.uniroma1.lcl.babelarity;

import it.uniroma1.lcl.babelarity.exception.NotADocumentException;
import it.uniroma1.lcl.babelarity.utils.StopWords;

import java.util.*;
import java.util.stream.Collectors;

public class BabelDocumentSimilarityAdvanced implements BabelDocumentSimilarity {

    private Set<String> stopWords;
    private double restartProb;
    private int maxIteration;
    private MiniBabelNet miniBabelNet;

    public BabelDocumentSimilarityAdvanced(int maxIteration, MiniBabelNet miniBabelNet)
    {
        this.stopWords = new StopWords().toSet();
        this.restartProb = 0.9;
        this.maxIteration = maxIteration;
        this.miniBabelNet = miniBabelNet;
    }

    public BabelDocumentSimilarityAdvanced(MiniBabelNet miniBabelNet) {this(100000, miniBabelNet); }

    private Set<BabelSynset> grandChilds(BabelSynset s)
    {
        //Inizializzo i visitati con i figli/vicini di s
        Set<BabelSynset> v = new HashSet<>(s.getRelations());

        //per ogni figlio aggiungo ai visitati i loro figli (nipoti di s)
        for(BabelSynset s2: s.getRelations())
            v.addAll(s2.getRelations());

        //restituisco i vicini
        return v;
    }

    private HashMap<BabelSynset, Set<BabelSynset>> getDocGraph(Set<BabelSynset> docSynsets)
    {

        //inizializzo mappa del grafo
        HashMap<BabelSynset, Set<BabelSynset>> g = new HashMap<>();


        /*
        Per ogni synset nel doc faccio una intersezione fra i suoi vicini a dist. 2
        e gli altri synset del doc
        */
        for(BabelSynset s1: docSynsets)
        {
            Set<BabelSynset> intersection = new HashSet<>(docSynsets);
            Set<BabelSynset> visited = grandChilds(s1);
            visited.remove(s1);
            intersection.retainAll(visited);

            /*
            Se c'e intersezione, metto s1 in collegamento con
            gli altri nodi (quelli nel set) e viceversa, cosi'
            da avere un grafo bidirezionale.
             */
            if(!intersection.isEmpty())
            {
                g.merge(s1, intersection,(v1,v2)->{ v1.addAll(v2); return v1; });
                for(BabelSynset s: intersection)
                    g.merge(s, new HashSet<>(Set.of(s1)), (v1,v2)->{ v1.addAll(v2); return v1; });
            }
        }
        return g;
    }

    //random walk del doc
    private int[] randomWalk(Map<BabelSynset, Set<BabelSynset>> graph, Map<BabelSynset, Integer> indexMap)
    {
        int[] v = new int[indexMap.size()];
        BabelSynset start = BabelSynset.randomNode(graph.keySet());
        for (int i=0; i<maxIteration;i++)
        {
            double random = Math.random();
            if(random<restartProb) start = BabelSynset.randomNode(graph.keySet());
            v[indexMap.get(start)]++;
            start = BabelSynset.randomNode(graph.get(start));
        }

        return v;
    }

    @Override
    public double compute(LinguisticObject o1, LinguisticObject o2) throws NotADocumentException
    {
        if(!(o1 instanceof Document) || !(o2 instanceof Document)) throw new NotADocumentException();

        Document d1 = (Document)o1;
        Document d2 = (Document)o2;

        Set<BabelSynset> synsetsDoc1 = new HashSet<>(miniBabelNet.getSynsets(d1.getWords(stopWords)));
        Set<BabelSynset> synsetsDoc2 = new HashSet<>(miniBabelNet.getSynsets(d2.getWords(stopWords)));

        HashMap<BabelSynset, Set<BabelSynset>> g1 = getDocGraph(synsetsDoc1);
        HashMap<BabelSynset, Set<BabelSynset>> g2 = getDocGraph(synsetsDoc2);

        Set<BabelSynset> graphsNodes = g1.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        graphsNodes.addAll(g2.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()));

        //creo una mappa di indici per i due vettori che mi serviranno dopo
        Map<BabelSynset, Integer> indexMap = new HashMap<>();
        int index = 0;
        for(BabelSynset b : graphsNodes)
            indexMap.put(b,index++);

        //randomWalk() su i due grafi
        int[] v1 = randomWalk(g1, indexMap);
        int[] v2 = randomWalk(g2, indexMap);

        //cosine similarity
        //todo: fare funzione generale
        double numerator = 0;
        double sqrt1 = 0;
        double sqrt2 = 0;
        for(int i=0; i<v1.length; i++)
        {
            double val1 = v1[i];
            double val2 = v2[i];

            numerator += val1*val2;
            sqrt1 += val1*val1;
            sqrt2 += val2*val2;
        }

        sqrt1 = Math.sqrt(sqrt1);
        sqrt2 = Math.sqrt(sqrt2);

        double result = numerator/(sqrt1*sqrt2);

        return result;
    }
}

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

    public BabelDocumentSimilarityAdvanced(int maxIteration)
    {
        this.stopWords = new StopWords().toSet();
        this.restartProb = 0.1;
        this.maxIteration = maxIteration;
        this.miniBabelNet = MiniBabelNet.getInstance();
    }

    public BabelDocumentSimilarityAdvanced() {this(100000); }

    private Set<Synset> grandChilds(Synset s)
    {
        //Inizializzo i visitati con i figli/vicini di s
        Set<Synset> v = new HashSet<>(s.getRelations());

        //per ogni figlio aggiungo ai visitati i loro figli (nipoti di s)
        for(Synset s2: s.getRelations())
            v.addAll(s2.getRelations());

        //restituisco i vicini
        return v;
    }

    private HashMap<Synset, Set<Synset>> getDocGraph(Set<Synset> docSynsets)
    {

        //inizializzo mappa del grafo
        HashMap<Synset, Set<Synset>> g = new HashMap<>();


        /*
        Per ogni synset nel doc faccio una intersezione fra i suoi vicini a dist. 2
        e gli altri synset del doc
        */
        for(Synset s1: docSynsets)
        {
            Set<Synset> intersection = new HashSet<>(docSynsets);
            Set<Synset> visited = grandChilds(s1);
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
                for(Synset s: intersection)
                    g.merge(s, new HashSet<>(Set.of(s1)), (v1,v2)->{ v1.addAll(v2); return v1; });
            }
        }
        return g;
    }

    //random walk del doc
    private int[] randomWalk(Map<Synset, Set<Synset>> graph, Map<Synset, Integer> indexMap)
    {
        int[] v = new int[indexMap.size()];
        Synset start = Synset.randomNode(graph.keySet());
        for (int i=0; i<maxIteration;i++)
        {
            double random = Math.random();
            if(restartProb>random) start = Synset.randomNode(graph.keySet());
            v[indexMap.get(start)]++;
            start = Synset.randomNode(graph.get(start));
        }
        return v;
    }

    private double cosineSimilarity(int[] v1, int[] v2)
    {
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

        return numerator/(sqrt1*sqrt2);
    }

    @Override
    public double compute(LinguisticObject o1, LinguisticObject o2) throws NotADocumentException
    {
        if(!(o1 instanceof Document) || !(o2 instanceof Document)) throw new NotADocumentException();

        Document d1 = (Document)o1;
        Document d2 = (Document)o2;

        Set<Synset> synsetsDoc1 = new HashSet<>(miniBabelNet.getSynsets(d1.getWords(stopWords)));
        Set<Synset> synsetsDoc2 = new HashSet<>(miniBabelNet.getSynsets(d2.getWords(stopWords)));
        HashMap<Synset, Set<Synset>> g1 = getDocGraph(synsetsDoc1);
        HashMap<Synset, Set<Synset>> g2 = getDocGraph(synsetsDoc2);

        Set<Synset> graphsNodes = g1.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        graphsNodes.addAll(g2.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()));

        //creo una mappa di indici per i due vettori che mi serviranno dopo
        Map<Synset, Integer> indexMap = new HashMap<>();
        int index = 0;
        for(Synset b : graphsNodes)
            indexMap.put(b,index++);

        //randomWalk() su i due grafi
        int[] v1 = randomWalk(g1, indexMap);
        int[] v2 = randomWalk(g2, indexMap);

        //cosine similarity
        return cosineSimilarity(v1, v2);
    }
}

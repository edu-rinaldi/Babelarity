package it.uniroma1.lcl.babelarity;

import it.uniroma1.lcl.babelarity.exception.NotADocumentException;
import it.uniroma1.lcl.babelarity.utils.StopWords;

import java.util.*;
import java.util.stream.Collectors;

import static it.uniroma1.lcl.babelarity.utils.CosineSimilarity.cosineSimilarity;

/**
 * Questa classe è una implementazione di una metrica avanzata per il
 * calcolo della similarità fra {@code Document}.
 *
 * Implementa l'interfaccia {@code BabelDocumentSimilarity} e quindi
 * deve implementare il metodo {@code compute(LinguisticObject o1, LinguisticObject o2)}.
 */
public class BabelDocumentSimilarityAdvanced implements BabelDocumentSimilarity {

    private Set<String> stopWords;
    private double restartProb;
    private int maxIteration;
    private MiniBabelNet miniBabelNet;

    private Map<Document, Map<Synset, Set<Synset>>> documentsGraph;

    /**
     * Un oggetto di questa classe può essere costruito a partire
     * da un parametro che specifica il numero di iterazioni massime
     * da far eseguire al metodo {@code randomWalk()}, più questo numero
     * è alto e più la precisione dei risultati potrebbe aumentare.
     * @param maxIteration Numero di iterazioni massime da far eseguire
     *                     al metodo {@code randomWalk()}
     */
    public BabelDocumentSimilarityAdvanced(int maxIteration)
    {
        this.stopWords = new StopWords().toSet();
        this.restartProb = 0.1;
        this.maxIteration = maxIteration;
        this.miniBabelNet = MiniBabelNet.getInstance();
        documentsGraph =  new HashMap<>();
    }

    /**
     * Se non viene specificato nessun parametro, il campo maxIteration,
     * di default viene settato a {@code 100000}.
     */
    public BabelDocumentSimilarityAdvanced() {this(100000); }

    /**
     * Questo metodo restituisce un {@code Set} contenente tutti i figli
     * e nipoti di un {@code Synset} senza dover fare una visita BFS a distanza 2
     * che richiederebbe molto più tempo.
     * @param s {@code Synset} dal quale prendere i figli e i nipoti.
     * @return {@code Set} contenente tutti i figli e nipoti.
     */
    private Set<Synset> grandChilds(Synset s)
    {
        return s.getRelations()
                .stream()
                .flatMap(s2->s2.getRelations().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Crea il grafo del documento sottoforma di mappa
     * @param docSynsets {@code Set} che contiene tutti i {@code Synset} in un documento.
     * @return Grafo del documento sottoforma di mappa; synset->set_di_vicini.
     */
    private HashMap<Synset, Set<Synset>> createDocGraph(Set<Synset> docSynsets)
    {

        //Inizializzo mappa del grafo
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

    /**
     * Metodo randomWalk che passa da un nodo a un altro in modo casuale e tiene
     * il conteggio di quante volte passa su ogni nodo
     * @param graph Grafo del documento sottoforma di mappa.
     * @param indexMap Mappa indici dei Synset per creare un vettore che mantiene
     *                 il conteggio delle visite a un nodo.
     * @return Vettore che contiene per ciascuna posizione il conteggio di quante volte
     * tramite randomWalk si è capitati in un certo nodo.
     */
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

    /**
     * Metodo che restituisce un grafo del documento passato come parametro, però
     * se già è stato crato lo preleva dalla memoria, altrimenti lo crea sul momento.
     * @param d Documento del quale vogliamo il grafo
     * @return Un grafo del documento passato come parametro.
     */
    private Map<Synset, Set<Synset>> getDocumentGraph(Document d)
    {
        if(documentsGraph.containsKey(d)) return documentsGraph.get(d);
        Set<Synset> synsetsDoc1 = new HashSet<>(miniBabelNet.getSynsets(d.getWords(stopWords)));
        documentsGraph.put(d, createDocGraph(synsetsDoc1));
        return documentsGraph.get(d);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public double compute(LinguisticObject o1, LinguisticObject o2) throws NotADocumentException
    {
        if(!(o1 instanceof Document) || !(o2 instanceof Document)) throw new NotADocumentException();

        Document d1 = (Document)o1;
        Document d2 = (Document)o2;

        Map<Synset, Set<Synset>> g1 = getDocumentGraph(d1);
        Map<Synset, Set<Synset>> g2 = getDocumentGraph(d2);

        Set<Synset> graphsNodes = new HashSet<>(g1.keySet());
        graphsNodes.addAll(g2.keySet());

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

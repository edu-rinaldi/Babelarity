package it.uniroma1.lcl.babelarity.test;

import it.uniroma1.lcl.babelarity.*;
import it.uniroma1.lcl.babelarity.utils.StopWords;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class MainTest3
{
    public static Set<BabelSynset> getParole(Document d, Set<String> sw)
    {
        MiniBabelNet b = MiniBabelNet.getInstance();
        /*
            1) Prende le singole parole
            2) Trimma
            3) mappa in synset
            4) mette tutto in un set
         */
        return Arrays.stream(d.getContent().replaceAll("^[A-Za-z0-9]"," ").toLowerCase().split(" "))
                .map(String::trim)
                .filter(w-> !sw.contains(w) && b.getSynsets(w).size()>0)
                .map(w->b.getSynsets(w).get(0))
                .collect(Collectors.toSet());
    }


    public static Set<BabelSynset> bfsDist(BabelSynset start, int dist,int curDist, Set<BabelSynset> visited)
    {
        //caso base: nodo visitato o distanzaCorrente>distanzaMax
        if(curDist>dist || visited.contains(start)) return visited;

        //nodo appena visitato e aggiunto alla lista di visitati così da escluderlo successivamente
        visited.add(start);

        for(BabelSynset n: start.getRelations())
            //aggiorno i visitati ricorsivamente sui figli del nodo corrente
            visited = bfsDist(n,dist,curDist+1, visited);

        //restituisco i visitati
        return visited;
    }

    //TODO: Usa questo al posto di bfsDist()
    public static Set<BabelSynset> vicini(BabelSynset s)
    {
        //Inizializzo i visitati con i figli/vicini di s
        Set<BabelSynset> v = new HashSet<>(s.getRelations());

        //per ogni figlio aggiungo ai visitati i loro figli (nipoti di s)
        for(BabelSynset s2: s.getRelations())
            v.addAll(s2.getRelations());

        //restituisco i vicini
        return v;
    }

    public static HashMap<BabelSynset, Set<BabelSynset>> getGraph(Set<BabelSynset> bsd)
    {

        //inizializzo mappa del grafo
        HashMap<BabelSynset, Set<BabelSynset>> g1 = new HashMap<>();


        /*
            Per ogni synset nel doc faccio una intersezione fra i suoi vicini a dist. 2
            e gli altri synset del doc
         */
        for(BabelSynset s1: bsd)
        {
            Set<BabelSynset> intersection = new HashSet<>(bsd);
//            Set<BabelSynset> visited = bfsDist(s1,2,0,new HashSet<>());
            Set<BabelSynset> visited = vicini(s1);
            visited.remove(s1);
            intersection.retainAll(visited);

            /*
                Se c'e intersezione, metto s1 in collegamento con
                gli altri nodi (quelli nel set) e viceversa, cosi'
                da avere un grafo bidirezionale.
             */
            if(!intersection.isEmpty())
            {
                g1.merge(s1, intersection,(v1,v2)->{
                    v1.addAll(v2);
                    return v1;
                });
                for(BabelSynset s: intersection)
                    g1.merge(s, new HashSet<>(Set.of(s1)), (v1,v2)->{
                        v1.addAll(v2);
                        return v1;
                    });
            }
        }
        return g1;
    }

    public static BabelSynset randomNode(Collection<BabelSynset> s)
    {
        //mappo la collezione in lista
        List<BabelSynset> keys = new ArrayList<>(s);
        //numero a random che fungera' da indice
        int ran = new Random().nextInt(keys.size());
        //prendi un elemento a caso nella lista
        return keys.get(ran);
    }

    //random walk del doc
    //TODO: controllare con Gianmarco l'    if(random<r)
    public static int[] randomWalk(double r, int k, Map<BabelSynset, Set<BabelSynset>> graph, Map<BabelSynset, Integer> indexMap)
    {
        int[] v = new int[indexMap.size()];
        BabelSynset start = randomNode(graph.keySet());
        while(k>0)
        {
            double random = Math.random();
            if(random<r) start = randomNode(graph.keySet());
            v[indexMap.get(start)]++;
            start = randomNode(graph.get(start));
            k--;
        }
        return v;
    }

    public static void main(String[] args)
    {
        MiniBabelNet mb = MiniBabelNet.getInstance();
        CorpusManager cm = CorpusManager.getInstance();

        String[] paths = new String[]
        {
            "C_programming_language.txt", "Java_programming_language.txt", "Cristiano_Ronaldo.txt",
            "Thomas_Muller.txt", "Eugenio_Montale.txt", "Umberto_Eco.txt", "Tourism_in_the_Netherlands.txt",
            "Cultural_tourism.txt", "Council_of_the_European_Union.txt", "European_Union_law.txt", "Java_virtual_machine.txt"
        };
        List<Pair<String,String>> tests = List.of(new Pair<>(paths[0],paths[1]), //c_programming & java_programming
                                                new Pair<>(paths[0],paths[2]),  //c_programming & cristiano_ronaldo
                                                new Pair<>(paths[2],paths[3]),  //cristiano_ronaldo & thomas_muller
                                                new Pair<>(paths[1],paths[3]),  //java_programming & thomas_muller
                                                new Pair<>(paths[4], paths[5]), //Eugenio_montale & Umberto_Eco
                                                new Pair<>(paths[4], paths[6]), //Eugenio_montale && tourism_in_netherlands
                                                new Pair<>(paths[6], paths[7]), //tourism_in_netherlands & cultural_tourism
                                                new Pair<>(paths[5], paths[7]), //Umberto_eco & cultural_tourism
                                                new Pair<>(paths[8], paths[9]), //Council_of_Eur & European_Union_law
                                                new Pair<>(paths[8], paths[10]), //Council_of_Eur & Java_virtual_machine
                                                new Pair<>(paths[10], paths[1]), //Java_virtual_machine & java_programming
                                                new Pair<>(paths[9], paths[1])); //European_Union_law & java_programming-

        for(Pair<String,String> p: tests)
        {
            String s1 = p.getKey();
            String s2 = p.getValue();

            System.out.println("Similarita tra: "+s1+"\t"+s2);

            //parse dei docs
            Document d1 = cm.parseDocument("resources/documents/"+s1);
            Document d2 = cm.parseDocument("resources/documents/"+s2);

            //istanza di StopWords
            StopWords sw = StopWords.getInstance();


            //prendo parole->synset nei due doc
            Set<BabelSynset> bsd1 = getParole(d1, sw.toSet());
            Set<BabelSynset> bsd2 = getParole(d2, sw.toSet());

            //costruisco il grafo per il primo doc ed il secondo
            HashMap<BabelSynset, Set<BabelSynset>> g1 = getGraph(bsd1);
            HashMap<BabelSynset, Set<BabelSynset>> g2 = getGraph(bsd2);


            //creo una mappa di indici per i due vettori che mi serviranno dopo
            Map<BabelSynset, Integer> indexMap = new HashMap<>();
            int index = 0;
            for(BabelSynset b : mb.getSynsets())
                indexMap.put(b,index++);

            //r :   probabilita di restart
            double r = 0.9;
            //k :   numero iterazioni
            int k = 100000;


            //randomWalk() su i due grafi
            int[] v1 = randomWalk(r, k, g1, indexMap);
            int[] v2 = randomWalk(r, k, g2, indexMap);


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

            System.out.println(result);
        }







    }
}

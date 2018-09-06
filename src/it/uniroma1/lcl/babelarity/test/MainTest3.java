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
        return Arrays.stream(d.getContent().replaceAll("^[A-Za-z0-9]"," ").toLowerCase().split(" "))
                .map(String::trim)
                .filter(w-> !sw.contains(w) && b.getSynsets(w).size()>0)
                .map(w->b.getSynsets(w).get(0))
                .collect(Collectors.toSet());
    }


    public static Set<BabelSynset> bfsDist(BabelSynset start, int dist,int curDist, Set<BabelSynset> visited)
    {
        if(curDist>dist || visited.contains(start)) return visited;
        visited.add(start);
        for(BabelSynset n: start.getRelations())
            visited = bfsDist(n,dist,curDist+1, visited);
        return visited;
    }

    public static Set<BabelSynset> vicini(BabelSynset s)
    {
        Set<BabelSynset> v = new HashSet<>(s.getRelations());
        for(BabelSynset s2: s.getRelations())
        {
            v.addAll(s2.getRelations());
        }
        return v;
    }

    public static HashMap<BabelSynset, Set<BabelSynset>> getGraph(Set<BabelSynset> bsd)
    {
        HashMap<BabelSynset, Set<BabelSynset>> g1 = new HashMap<>();
        for(BabelSynset s1: bsd)
        {
            Set<BabelSynset> intersection = new HashSet<>(bsd);
//            System.out.println(bfsDist(s1,2,0,new HashSet<>()));
//            Set<BabelSynset> visited = bfsDist(s1,2,0,new HashSet<>());
            Set<BabelSynset> visited = vicini(s1);
            visited.remove(s1);
            intersection.retainAll(visited);
//            System.out.println(intersection);
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
        List<BabelSynset> keys = new ArrayList<>(s);
        int ran = new Random().nextInt(keys.size());
        if(ran>= keys.size())
            System.out.println(ran);
        return keys.get(ran);
    }

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
            "Cultural_tourism.txt"
        };
        List<Pair<String,String>> tests = List.of(new Pair<>(paths[0],paths[1]),
                                                new Pair<>(paths[0],paths[2]),
                                                new Pair<>(paths[2],paths[3]),
                                                new Pair<>(paths[1],paths[3]),
                                                new Pair<>(paths[4], paths[5]),
                                                new Pair<>(paths[4], paths[6]),
                                                new Pair<>(paths[6], paths[7]),
                                                new Pair<>(paths[5], paths[7]));

        for(Pair<String,String> p: tests)
        {
            String s1 = p.getKey();
            String s2 = p.getValue();

            System.out.println("Similarita tra: "+s1+"\t"+s2);
            Document d1 = cm.parseDocument("resources/documents/"+s1);
            Document d2 = cm.parseDocument("resources/documents/"+s2);
            StopWords sw = StopWords.getInstance();
//            System.out.println("Creando set di synset per documento1");
            Set<BabelSynset> bsd1 = getParole(d1, sw.toSet());
//            System.out.println("Creando set di synset per documento2");
            Set<BabelSynset> bsd2 = getParole(d2, sw.toSet());

//            System.out.println("Creando Grafo 1");
            HashMap<BabelSynset, Set<BabelSynset>> g1 = getGraph(bsd1);
//            System.out.println("Creando Grafo 2");
            HashMap<BabelSynset, Set<BabelSynset>> g2 = getGraph(bsd2);

            Map<BabelSynset, Integer> indexMap = new HashMap<>();
            int index = 0;
            for(BabelSynset b : mb.getSynsets())
                indexMap.put(b,index++);

            double r = 0.9;
            int k = 100000;
//            System.out.println("Random Walk g1");
            int[] v1 = randomWalk(r, k, g1, indexMap);
//            System.out.println("Random Walk g2");
            int[] v2 = randomWalk(r, k, g2, indexMap);

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

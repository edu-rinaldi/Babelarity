/*
package it.uniroma1.lcl.babelarity;


import javafx.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main
{
    public static HashSet<String> getStopWords()
    {
        HashSet<String> stopwords = new HashSet<>();
        try(BufferedReader br = Files.newBufferedReader(Paths.get("resources/utils/english-stop-words-large.txt")))
        {
            while(br.ready())
                stopwords.add(br.readLine());
        }
        catch (IOException e){e.printStackTrace();}
        return stopwords;
    }

    public static List<List<String>> getFrasi(HashSet<String> sw)
    {
        MiniBabelNet b = MiniBabelNet.getInstance();
        List<List<String>> frasi = new ArrayList<>();
        try
        {
            for(String p : new File("resources/corpus/").list())
            {
                String txt = new String(Files.readAllBytes(Paths.get("resources/corpus/", p)))
                        .replaceAll("^[A-Za-z0-9.;?!:]"," ");
                frasi.addAll(Arrays.stream(txt.split("[.?!:]"))
                        .map(s-> Arrays.stream(s.trim().split(" "))
                                .filter(w-> !sw.contains(w) && b.getLemmas(w)!=null)
                                .map(b::getLemmas)
                                .collect(Collectors.toList())
                        ).collect(Collectors.toList()));
            }
        }
        catch (IOException e){e.printStackTrace();}
        return frasi;
    }

    public static List<Set<String>> getFrasiF(HashSet<String> sw,Map<String,Integer> counter)
    {
        MiniBabelNet b = MiniBabelNet.getInstance();
        List<Set<String>> frasi = new ArrayList<>();
        try
        {
            for(String p : new File("resources/corpus/").list())
            {
                String txt = new String(Files.readAllBytes(Paths.get("resources/corpus/", p)))
                        .replaceAll("^[A-Za-z0-9]"," ");
                frasi.add(Arrays.stream(txt.split(" "))
                        .filter(w->{
                            String w1 = w.trim();
                            return !sw.contains(w1) && b.getLemmas(w1)!=null;
                        })
                        .map(w->{
                            String s = b.getLemmas(w.trim());
                            counter.merge(s,1,(v1,v2)->v1+v2);
                            return s;
                        })
                        .collect(Collectors.toSet()));
            }
        }
        catch (IOException e){e.printStackTrace();}
        return frasi;
    }

    public static double logb(double a, double b) {return Math.log(a)/Math.log(b); }
    public static double log2(double a) {return logb(a,2); }


    public static void main(String[] args)
    {

        MiniBabelNet b = MiniBabelNet.getInstance();
//        CorpusManager c = CorpusManager.getInstance();

        HashSet<String> stopwords = getStopWords();
        HashMap<String, Integer> counter = new HashMap<>();
        LinkedHashMap<String,Integer> indexMap = new LinkedHashMap<>();
        List<Set<String>> frasi = getFrasiF(stopwords, counter);


        System.out.println("creando mappa indici..");

        //mappa indici
        int id = 0;
        Set<String> V = new HashSet<>();
        for(Set<String> frase: frasi)
            for (String w: frase)
                if(!indexMap.containsKey(w))
                {
                    indexMap.put(w, id++);
                    V.add(w);
                }



        System.out.println("creando matrice occorrenze..");

        //matrice occorrenze
        short[][] mat = new short[id+1][id+1];
//        Arrays.stream(mat).forEach(r->Arrays.fill(r, 1));
        for(Set<String> frase: frasi)
            for(String w: frase)
                for(String w2: frase)
                    if(!w.equals(w2)) mat[indexMap.get(w)][indexMap.get(w2)]+=1;


        System.out.println("Calcolo pmi..");

        //matrice pmi
        int numDocumenti = frasi.size();
        frasi.clear();
        float[][] pmi = new float[mat.length][mat.length];

        for(String w1: V)
            for(String w2: V)
            {
                int i = indexMap.get(w1);
                int j = indexMap.get(w2);
                float pxy = i==j ? counter.get(w1) : mat[i][j];

                pmi[i][j] = Math.max((float)Math.log((pxy/numDocumenti)/
                        (((float)counter.get(w1)/numDocumenti)*((float)counter.get(w2)/numDocumenti))), 0);
            }

        System.out.println("Inizio test..");
        List<Pair<String,String>> testString = List.of(new Pair<>("test","exam"),
                                                        new Pair<>("pop", "rock"),
                                                        new Pair<>("test","pop"),
                                                        new Pair<>("exam", "rock"),
                                                        new Pair<>("test","test"),
                                                        new Pair<>("port","ship"),
                                                        new Pair<>("fear","emotion"),
                                                        new Pair<>("port","fear"),
                                                        new Pair<>("ship", "emotion"),
                                                        new Pair<>("port","port"),
                                                        new Pair<>("government","politician"),
                                                        new Pair<>("politician","dog"),
                                                        new Pair<>("car","bus"),
                                                        new Pair<>("bike","dog"));
        File f = new File("testRes.txt");

        for(Pair<String,String> pair : testString)
        {
            String w1 = pair.getKey();
            String w2 = pair.getValue();
            System.out.println("Inizio test per: "+w1+"\t"+w2);
            int indexW1 = indexMap.get(w1);
            int indexW2 = indexMap.get(w2);
            float numerator = 0;
            float sqrt1 = 0;
            float sqrt2 = 0;
            for(int i=0; i<pmi.length; i++)
            {
                float val1 = pmi[i][indexW1];
                float val2 = pmi[i][indexW2];

                numerator += val1*val2;
                sqrt1 += Math.pow(val1, 2);
                sqrt2 += Math.pow(val2, 2);

            }
            sqrt1 = (float)Math.sqrt(sqrt1);
            sqrt2 = (float)Math.sqrt(sqrt2);
            float result = numerator/(sqrt1*sqrt2);
            String msg = w1+" && "+w2+" ==> "+result+"\n";
            System.out.println(msg);
            try {
                FileWriter fw = new FileWriter(f.getAbsolutePath(), true);
                fw.append(msg);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }




    }
}
*/
package it.uniroma1.lcl.babelarity;

import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Main{
    public static void main(String[] args) {
        BabelLexicalSimilarity bl = new BabelLexicalSimilarity();
        List<Pair<String,String>> testString = List.of(new Pair<>("test","exam"),
                new Pair<>("pop", "rock"),
                new Pair<>("test","pop"),
                new Pair<>("exam", "rock"),
                new Pair<>("test","test"),
                new Pair<>("port","ship"),
                new Pair<>("fear","emotion"),
                new Pair<>("port","fear"),
                new Pair<>("ship", "emotion"),
                new Pair<>("port","port"),
                new Pair<>("government","politician"),
                new Pair<>("politician","dog"),
                new Pair<>("car","bus"),
                new Pair<>("bike","dog"),
                new Pair<>("teacher","doctor"),
                new Pair<>("teacher","test"),
                new Pair<>("russian","italian"),
                new Pair<>("american","italian"),
                new Pair<>("italian","human"));

        File f = new File("testRes.txt");
        for(Pair<String,String> p : testString)
        {
            String s1 = p.getKey();
            String s2 = p.getValue();
            double result = bl.computeSimilarity(Word.fromString(s1),Word.fromString(s2));
            String msg = s1+" && "+s2+" ==> "+result+"\n";
            System.out.println(msg);

            try {
                FileWriter fw = new FileWriter(f.getAbsolutePath(), true);
                fw.append(msg);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
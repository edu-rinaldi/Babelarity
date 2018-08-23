package it.uniroma1.lcl.babelarity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class BabelLexicalSimilarity
{

    private HashMap<String, Integer> indexMap;
    private Set<String> v;
    private HashMap<String, Integer> occ;
    private List<Set<String>> docList;
    private short[][] coOcc;
    private float[][] pmi;
    private String corpusPath;
    private Set<String> sw;

    public BabelLexicalSimilarity()
    {
        this("resources/corpus/");
    }

    public BabelLexicalSimilarity(String corpusPath)
    {
        this.corpusPath = corpusPath;
        System.out.println("Caricamento stopwords");
        sw = StopWords.getInstance().toSet();
        System.out.println("Caricamento docs");
        docList = getDocumentsFiltered();
        System.out.println("Caricamento indexmap");
        indexMap = getIndexMap();
        System.out.println("Calcolo cooccorenze");
        calculateCoOccurences();
        System.out.println("calcolo pmi");
        calculatePMI();
    }

    private List<Set<String>> getDocumentsFiltered()
    {
        if(docList==null) docList = createDocumentsFilteredList();
        return docList;
    }

    private List<Set<String>> createDocumentsFilteredList()
    {
        MiniBabelNet b = MiniBabelNet.getInstance();
        occ = new HashMap<>();
        List<Set<String>> documents = new ArrayList<>();
        File corpus = new File(corpusPath);
        try
        {
            if(corpus.exists() && corpus.canRead())
                for(File f : corpus.listFiles())
                {
                    //read text and filter it
                    String txt = new String(Files.readAllBytes(f.toPath())).replaceAll("^[a-zA-Z0-9]"," ");

                    //split into words
                    documents.add(Arrays.stream(txt.split(" "))
                            .filter(w->{
                                String w1 = w.trim();
                                //get only words that are not SW and have a lemma form
                                return !sw.contains(w1) && b.getLemmas(w1)!=null;
                            })
                            .map(w->{
                                //map into lemma and count single occurrence for each word
                                String s = b.getLemmas(w.trim());
                                occ.merge(s,1,(v1,v2)->v1+v2);
                                return s;
                            })
                            .collect(Collectors.toSet()));
                }
        }
        catch (IOException e){e.printStackTrace();}
        return documents;
    }

    private HashMap<String, Integer> createIndexMap()
    {
        int id = 0;
        v = new HashSet<>();
        HashMap<String, Integer> indexMap = new HashMap<>();
        for(Set<String> doc:  docList)
            for (String w: doc)
                if(!indexMap.containsKey(w))
                {
                    indexMap.put(w, id++);
                    v.add(w);
                }
        return indexMap;
    }

    private HashMap<String,Integer> getIndexMap()
    {
        if(indexMap == null) indexMap = createIndexMap();
        return indexMap;
    }

    private void calculateCoOccurences()
    {
        coOcc = new short[indexMap.size()][indexMap.size()];
        for(Set<String> doc: docList)
            for(String w: doc)
                for(String w2: doc)
                    if(!w.equals(w2)) coOcc[indexMap.get(w)][indexMap.get(w2)]+=1;
    }

    private void calculatePMI()
    {
        int numDocuments = docList.size();
        pmi = new float[coOcc.length][coOcc.length];
        for(String w1: v)
            for(String w2: v)
            {
                int i = indexMap.get(w1);
                int j = indexMap.get(w2);
                float pxy = i==j ? occ.get(w1) : coOcc[i][j];

                pmi[i][j] = Math.max((float)Math.log((pxy/numDocuments)/
                        (((float)occ.get(w1)/numDocuments)*((float)occ.get(w2)/numDocuments))), 0);
            }
    }


    private double cosineSimilarity(String s1, String s2)
    {
        int indexW1 = indexMap.get(s1);
        int indexW2 = indexMap.get(s2);
        double numerator = 0;
        double sqrt1 = 0;
        double sqrt2 = 0;
        for(int i=0; i<pmi.length; i++)
        {
            double val1 = pmi[i][indexW1];
            double val2 = pmi[i][indexW2];

            numerator += val1*val2;
            sqrt1 += Math.pow(val1, 2);
            sqrt2 += Math.pow(val2, 2);

        }
        sqrt1 = Math.sqrt(sqrt1);
        sqrt2 = Math.sqrt(sqrt2);

        return numerator/(sqrt1*sqrt2);
    }

    public double computeSimilarity(Word w1, Word w2)
    {
        String wo1 = w1.toString();
        String wo2 = w2.toString();
        System.out.println("calcolo similarita'");
        return cosineSimilarity(wo1,wo2);
    }
}

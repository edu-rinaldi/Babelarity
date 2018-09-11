package it.uniroma1.lcl.babelarity;

import it.uniroma1.lcl.babelarity.exception.LemmaNotFoundException;
import it.uniroma1.lcl.babelarity.utils.StopWords;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BabelLexicalSimilarityAdvanced implements BabelLexicalSimilarity
{

    private HashMap<String, Integer> indexMap;
    private List<String> v;
    private HashMap<String, Integer> occ;
    private List<List<String>> docList;
    private short[][] coOcc;
    private float[][] pmi;
    private String corpusPath;
    private Set<String> sw;
    private MiniBabelNet miniBabelNet;

    public BabelLexicalSimilarityAdvanced()
    {
        this("resources/corpus/");
    }

    public BabelLexicalSimilarityAdvanced(String corpusPath)
    {
        this.corpusPath = corpusPath;
        this.miniBabelNet = MiniBabelNet.getInstance();

        System.out.println("Caricamento stopwords");
        sw = new StopWords().toSet();

        System.out.println("Caricamento docs");
        docList = getDocumentsFiltered();

        System.out.println("Caricamento indexmap");
        indexMap = getIndexMap();

        System.out.println("Calcolo cooccorenze");
        coOcc = calculateCoOccurences();

        System.out.println("calcolo pmi");
        pmi = calculatePMI();
    }

    private String checkString(String s) throws LemmaNotFoundException {
        if(miniBabelNet.getLemmas(s)==null)
        {
            if(!miniBabelNet.isLemma(s))
                throw new LemmaNotFoundException();
        }
        else { s = miniBabelNet.getLemmas(s); }
        return s;
    }

    private List<List<String>> getDocumentsFiltered()
    {
        occ = new HashMap<>();
        List<List<String>> documents = new ArrayList<>();
        File corpus = new File(corpusPath);
        try
        {
            if(corpus.exists() && corpus.canRead())
                for(File f : Objects.requireNonNull(corpus.listFiles()))
                {
                    //read text and filter it
                    String txt = new String(Files.readAllBytes(f.toPath())).replaceAll("\\W"," ");

                    //split into words
                    documents.add(Arrays.stream(txt.split(" "))
                            .filter(w->{
                                String w1 = w.trim();
                                //get only words that are not SW and have a lemma form
                                return !sw.contains(w1) && (miniBabelNet.getLemmas(w1)!=null || miniBabelNet.isLemma(w1));
                            })
                            .map(w->{
                                //map into lemma and count single occurrence for each word
                                String s = miniBabelNet.isLemma(w.trim()) ? w.trim() : miniBabelNet.getLemmas(w.trim());
                                occ.merge(s,1,(v1,v2)->v1+v2);
                                return s;
                            })
                            .distinct()
                            .collect(Collectors.toList()));
                }
        }
        catch (IOException e){e.printStackTrace();}
        return documents;
    }

    private HashMap<String,Integer> getIndexMap()
    {
        int id = 0;
        v = new ArrayList<>();
        HashMap<String, Integer> indexMap = new HashMap<>();
        for(int i=0; i<docList.size(); i++)
            for (int j=0; j<docList.get(i).size(); j++)
                if(!indexMap.containsKey(docList.get(i).get(j)))
                {
                    indexMap.put(docList.get(i).get(j), id++);
                    v.add(docList.get(i).get(j));
                }
        return indexMap;
    }

    private short[][] calculateCoOccurences()
    {
        short[][] coOccurances = new short[indexMap.size()][indexMap.size()];
        HashSet<String> counted = new HashSet<>();
        for(int i=0; i<docList.size();i++)
            for(String w1: docList.get(i))
            {
                counted.add(w1);
                int i1 = indexMap.get(w1);
                for(String w2: docList.get(i))
                {
                    if(counted.contains(w2)) continue;
                    int i2 = indexMap.get(w2);
                    coOccurances[Math.min(i1,i2)][Math.max(i1,i2)]+=1;
                }
            }
        return coOccurances;
    }

    private float[][] calculatePMI()
    {
        int numDocuments = docList.size();

        docList = null;

        float[][] pmi = new float[coOcc.length][coOcc.length];

        HashSet<Integer> counted = new HashSet<>();

        for(int n=0; n<v.size();n++)
        {
            for(int k=0; k<v.size(); k++)
            {
                if(counted.contains(k)) continue;
                int i = Math.min(indexMap.get(v.get(n)), indexMap.get(v.get(k)));
                int j = Math.max(indexMap.get(v.get(k)), indexMap.get(v.get(n)));
                float pxy = i==j ? occ.get(v.get(n)) : coOcc[i][j];

                pmi[i][j] = Math.max((float)Math.log((pxy/numDocuments)/
                        (((float)occ.get(v.get(n))/numDocuments)*((float)occ.get(v.get(k))/numDocuments))), 0);
            }
            counted.add(n);
        }
        coOcc = null;
        return pmi;
    }


    private double stringSimilarity(String s1, String s2)
    {
        int indexW1 = indexMap.get(s1);
        int indexW2 = indexMap.get(s2);

        return cosineSimilarity(pmi[indexW1], pmi[indexW2]);
    }

    private double cosineSimilarity(float[] v1, float[] v2)
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
    public double compute(LinguisticObject w1, LinguisticObject w2) throws LemmaNotFoundException
    {
        String wo1 = checkString(w1.toString());
        String wo2 = checkString(w2.toString());
        return wo1.equals(wo2)? 1 : stringSimilarity(wo1,wo2);
    }

}

package it.uniroma1.lcl.babelarity;

import it.uniroma1.lcl.babelarity.exception.NoLemmaFormException;
import it.uniroma1.lcl.babelarity.utils.stopword.StopWords;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static it.uniroma1.lcl.babelarity.utils.CosineSimilarity.cosineSimilarity;

/**
 * Questa classe è una implementazione di una metrica avanzata per il
 * calcolo della similarità fra {@code Word}.
 *
 * Implementa l'interfaccia {@code BabelLexicalSimilarity} e quindi
 * deve implementare il metodo {@code compute(LinguisticObject o1, LinguisticObject o2)}.
 */
public class BabelLexicalSimilarityAdvanced implements BabelLexicalSimilarity
{

    private HashMap<String, Integer> indexMap;
    private HashMap<String, Integer> occ;
    private List<List<String>> docList;
    private short[][] coOcc;
    private float[][] pmi;
    private String corpusPath;
    private Set<String> sw;
    private MiniBabelNet miniBabelNet;

    /**
     * Costruttore della classe che richiama il costruttore sovraccaricato
     */
    public BabelLexicalSimilarityAdvanced()
    {
        this("resources/corpus/");
    }

    /**
     * Un oggetto della classe si può costruire a partire da una Stringa
     * che indica il path del corpus da utilizzare per l'implementazione
     * di questa metrica avanzata.
     * @param corpusPath Indica il path del corpus da utilizzare per l'implementazione
     */
    public BabelLexicalSimilarityAdvanced(String corpusPath)
    {
        this.corpusPath = corpusPath;
        this.miniBabelNet = MiniBabelNet.getInstance();

        sw = new StopWords().toSet();

        docList = getDocumentsFiltered();
        indexMap = getIndexMap();
        coOcc = calculateCoOccurences();
        pmi = calculatePMI();
    }

    /**
     * Questo metodo controlla che la stringa sia utilizzabile per la similarità lessicale,
     * andando a cercare il suo lemma e andando a verificare che esso non sia già lemma di se stesso.
     * In caso contrario solleva un'eccezione.
     * @param s Stringa da verificare.
     * @return Lemma della stringa passa al metodo.
     * @throws NoLemmaFormException Eccezione lanciata se non c'è nessun lemma per questa stringa.
     */
    private String checkString(String s) throws NoLemmaFormException {
        //Se non trova un lemma
        if(miniBabelNet.getLemma(s)==null)
        {
            //e non è già di suo un lemma allora lancia un'eccezione
            if(!miniBabelNet.isLemma(s))
                throw new NoLemmaFormException();
        }
        //se trova un lemma lo assegna
        else { s = miniBabelNet.getLemma(s); }
        return s;
    }

    /**
     * Metodo che restituisce una lista di liste di stringhe.
     * La lista più interna rappresenta la raccolta di parole, prese singolarmente, da un documento
     * del corpus. Quella più esterna rappresenta quindi una raccolta di "documenti" filtrati.
     * Non viene utilizzata la classe {@code Document} per problemi di memoria e per velocizzare
     * l'implementazione della metrica.
     * Le parole vengono filtrate tra stopwords e parole che non hanno lemmi.
     * Inoltre per velocizzare l'implementazione della classe, il conteggio delle occorrenze singole
     * viene fatto durante il filtraggio.
     * @return Una lista di liste di stringhe, che rappresentano tutte le parole dei documenti del corpus.
     */
    private List<List<String>> getDocumentsFiltered()
    {
        occ = new HashMap<>();
        List<List<String>> documents = new ArrayList<>();
        File[] corpus = new File(corpusPath).listFiles();
        try
        {
            if(corpus!=null)
                for(int i=0; i<corpus.length;i++)
                {
                    //Legge tutto il testo di un documento e prende solo le parole
                    String txt = new String(Files.readAllBytes(corpus[i].toPath())).replaceAll("\\W"," ");

                    //Splitta in parole
                    documents.add(Arrays.stream(txt.split(" "))
                            .filter(w->{
                                String w1 = w.trim();
                                //Prende solo le parole che non sono stop word e hanno almeno un lemma
                                return !sw.contains(w1) && (miniBabelNet.getLemma(w1)!=null || miniBabelNet.isLemma(w1));
                            })
                            .map(w->{
                                //Mappa le stringhe nella forma lemma e conta le occorrenze singole
                                String s = miniBabelNet.isLemma(w.trim()) ? w.trim() : miniBabelNet.getLemma(w.trim());
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

    /**
     * Questo metodo crea una mappa di indici del tipo parola:indice,
     * verrà poi utilizzata con la matrice delle cooccorrenze e la matrice del
     * pmi, così da evitare di creare una mappa per le cooccorrenze (pesa molto in memoria).
     * @return Mappa di indici del tipo parola:indice.
     */
    private HashMap<String,Integer> getIndexMap()
    {
        int id = 0;
        HashMap<String, Integer> indexMap = new HashMap<>();
        for(int i=0; i<docList.size(); i++)
            for (int j=0; j<docList.get(i).size(); j++)
                if(!indexMap.containsKey(docList.get(i).get(j)))
                    indexMap.put(docList.get(i).get(j), id++);
        return indexMap;
    }

    /**
     * Metodo che calcola le cooccorrenze delle parole nei documenti
     * generando una matrice di cooccorrenze.
     * Si opta per gli {@code short} poichè con gli {@code int} sarebbe finita
     * la memoria e inoltre la grandezza degli short va più che bene se si evita
     * di contare le occorrenze singole sulla diagonale.
     * @return Matrice di {@code short} con cooccorrenze tra parole.
     */
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

    /**
     * Metodo che calcola il PMI tra due parole e ne restituisce
     * una matrice di {@code float}.
     * Si è optato per i {@code float} poichè con i {@code double}
     * il programma poteva girare solo su pc con memorie grandi.
     * @return Matrice del PMI.
     */
    private float[][] calculatePMI()
    {
        int numDocuments = docList.size();

        docList = null;

        float[][] pmi = new float[coOcc.length][coOcc.length];

        HashSet<String> counted = new HashSet<>();
        List<String> v = new ArrayList<>(indexMap.keySet());
        for(String w1: v)
        {
            for(String w2: v)
            {
                if(counted.contains(w2)) continue;
                int i = Math.min(indexMap.get(w1), indexMap.get(w2));
                int j = Math.max(indexMap.get(w1), indexMap.get(w2));
                float pxy = i==j ? occ.get(w1) : coOcc[i][j];
                pmi[i][j] = Math.max((float)Math.log((pxy/numDocuments)/
                        (((float)occ.get(w1)/numDocuments)*((float)occ.get(w2)/numDocuments))), 0);
            }
            counted.add(w1);
        }
        coOcc = null;
        return pmi;
    }


    /**
     * Metodo che tramite il {@code cosineSimilarity(v1,v2)}
     * calcola la similarità lessicale.
     * @param s1 Prima stringa.
     * @param s2 Seconda stringa.
     * @return un valore tra 0 e 1 che esprime la similarità lessicale.
     */
    private double lexicalSimilarity(String s1, String s2)
    {
        int indexW1 = indexMap.get(s1);
        int indexW2 = indexMap.get(s2);

        return cosineSimilarity(pmi[indexW1], pmi[indexW2]);
    }


    /**
     *
     * {@inheritDoc}
     */
    @Override
    public double compute(LinguisticObject w1, LinguisticObject w2) throws NoLemmaFormException
    {
        String wo1 = checkString(w1.toString());
        String wo2 = checkString(w2.toString());
        return wo1.equals(wo2)? 1 : lexicalSimilarity(wo1,wo2);
    }

}

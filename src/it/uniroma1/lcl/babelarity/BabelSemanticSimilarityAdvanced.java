package it.uniroma1.lcl.babelarity;

import it.uniroma1.lcl.babelarity.exception.NotASynsetException;
import it.uniroma1.lcl.babelarity.utils.RangeMapper;
import java.util.*;

/**
 * Questa classe è una implementazione di una metrica avanzata per il
 * calcolo della similarità fra {@code Synset}.
 *
 * Implementa l'interfaccia {@code BabelSemanticSimilarity} e quindi
 * deve implementare il metodo {@code compute(LinguisticObject o1, LinguisticObject o2)}.
 */
public class BabelSemanticSimilarityAdvanced implements BabelSemanticSimilarity
{
    private MiniBabelNet miniBabelNet;
    private Set<Synset> roots;
    private Map<PartOfSpeech, Integer> maxDepthPos;
    private double averageDepth;
    private double lowRange;
    private double highRange;

    /**
     * Costruttore della classe che richiede un'istanza di {@code MiniBabelNet}
     * per poter accedere ai vari metodi di utilità che sono forniti con esso.
     * Il costruttore prevede il calcolo della profondità massima assoluta tra i vari POS
     * e la profondità media fra i vari POS.
     * Entrambe servono per la mappatura del range di valori che l'LCH restituisce, calcolando
     * caso migliore e caso peggiore.
     */
    public BabelSemanticSimilarityAdvanced()
    {
        this.miniBabelNet = MiniBabelNet.getInstance();
        roots = miniBabelNet.getHypernymTrees();
        maxDepthPos = miniBabelNet.getMaxDepthHypernymTrees();

        //profondità media fra le profondità massime di ogni pos
        averageDepth = (double)maxDepthPos.values().stream().reduce(Integer::sum).orElse(0)/(maxDepthPos.size()+1);

        //profondità massima fra le profondità massime di ogni pos
        double maxAbsoluteDepth = maxDepthPos.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        //limite inferiore e superiore del range di valori per questa similarità
        lowRange = lch(maxAbsoluteDepth, averageDepth, 2);
        highRange = lch(1, averageDepth);
    }


    /**
     * Metodo di utilità, ricorsivo, che risale l'albero di iperonimia.
     * Questo lavoro in stretto contatto con il metodo {@code MiniBabelNet::minDistance}
     * poichè mi restituisce la lunghezza del path LCS tra i due {@code Synset} passati come parametro.
     * @param s1 {@code Synset} da cui partire.
     * @param s2 {@code Synset} dove arrivare.
     * @param curDist distanza attuale da {@code Synset s1} e il {@code Synset} visitato attualmente.
     * @param minDist distanza minima trovata al momento.
     * @param visited {@code Set} di nodi già visitati.
     * @return lunghezza del path LCS tra i due {@code Synset} passati come parametro.
     */
    private int riseUpTree(Synset s1, Synset s2, int curDist,int minDist, Set<Synset> visited)
    {
        //caso base: sono finiti i "padri"
        if (s1.getRelations("is-a").isEmpty()) return minDist;
        for(Synset s: s1.getRelations("is-a"))
        {
            //se il nodo non è stato visitato
            if(visited.contains(s)) continue;
            //aggiungilo ai visitati
            visited.add(s);
            //calcola la distanza con s2
            int dist = miniBabelNet.minDistance(s,s2,"has-kind2");
            //se è valida ed è minore di quella attuale, sostituiscila
            if(dist> -1 && curDist+dist<minDist) minDist = curDist+dist;
            //trova il minimo salendo ricorsivamente
            minDist = riseUpTree(s, s2, curDist+1, minDist, visited);
        }
        return minDist;
    }

    /**
     * Metodo privato che calcola la lunghezza del path lcs.
     * Se i due {@code Synset} sono identici allora restituisce 1, senza
     * applicare inutilmente l'algoritmo.
     * @param s1 Primo {@code Synset}.
     * @param s2 Secondo {@code Synset}.
     * @return la lunghezza del path LCS
     */
    private int lcsLength(Synset s1, Synset s2)
    {
        //Se i due synset sono uguali è inutile applicare un algoritmo di ricerca
        if(s1.equals(s2)) return 1;
        //Altrimenti sali nell'albero
        return riseUpTree(s1,s2, 1, Integer.MAX_VALUE, new HashSet<>());
    }

    /**
     * Metodo che calcola l'LCH.
     * @param lcs LCS tra due concetti.
     * @param averageDepth Profondità media degli alberi di iperonimia per ogni POS.
     * @return LCH.
     */
    private double lch(double lcs, double averageDepth){return -Math.log(lcs/(2*averageDepth)); }

    /**
     * Overload del metodo precedente, si eleva alla potenza l'LCS
     * così da rendere più evidente il distacco tra LCS simili.
     * @param lcs LCS tra due concetti.
     * @param averageDepth Profondità media degli alberi di iperonimia per ogni POS.
     * @param lcsPower Potenza alla quale elevare l'LCS.
     * @return LCH.
     */
    private double lch(double lcs, double averageDepth, int lcsPower){ return -Math.log(Math.pow(lcs, lcsPower)/(2*averageDepth)); }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public double compute(LinguisticObject s1, LinguisticObject s2) throws NotASynsetException
    {
        if(!(s1 instanceof Synset) || !(s2 instanceof Synset)) throw new NotASynsetException();

        Synset sy1 = (Synset) s1;
        Synset sy2 = (Synset) s2;
        double lcs = lcsLength(sy1, sy2);
        //se con la metrica avanzata non trovo lcs valido, applico la metrica baseline
        if(lcs == Integer.MAX_VALUE)
        {
            double result = 1.0/(miniBabelNet.minDistance(sy1,sy2)+1);
            return Double.isInfinite(result) ? 0 : result;
        }
        //altrimenti calcolo lch e mappo i valori
        return RangeMapper.map(lch(lcs, averageDepth, 2), lowRange, highRange, 0, 1);
    }


}

package it.uniroma1.lcl.babelarity;

import it.uniroma1.lcl.babelarity.exception.NotALinguisticObject;
import it.uniroma1.lcl.babelarity.utils.BabelPath;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Questa classe è una rappresentazione di un Grafo tra {@code Synset}
 * e permette il calcolo della similarità tra {@code LinguisticObject}.
 *
 * La classe implementa l'interfaccia {@code Iterable} sui {@code Synset},
 * così tramite un forEach possiamo scorrere fra i nodi di MiniBabelNet.
 */
public class MiniBabelNet implements Iterable<Synset>
{
    private static MiniBabelNet instance;

    private Map<String, List<Synset>> wordToSynsets;
    private Map<String, List<String>> wordToLemma;
    private Set<String> lemmas;
    private Map<String, Synset> synsetMap;

    private BabelLexicalSimilarity babelLexicalSimilarity;
    private BabelSemanticSimilarity babelSemanticSimilarity;
    private BabelDocumentSimilarity babelDocumentSimilarity;

    private MiniBabelNet()
    {

        Pair<Map<String,Synset>, Map<String,List<Synset>>> pkg = parseDictionary();
        synsetMap = pkg.getKey();
        wordToSynsets = pkg.getValue();

        Pair<Map<String,List<String>>,Set<String>> pkg2 = parseLemmas();
        wordToLemma = pkg2.getKey();
        lemmas = pkg2.getValue();

        parseGlosses();
        parseRelations();
    }

    /**
     * Metodo per singleton pattern, restituisce sempre la stessa istanza di
     * {@code MiniBabelNet}.
     * Dopo aver creato la nuova istanza setta di default tre algoritmi per il
     * calcolo della similarità fra {@code LinguisticObject}.
     * @return L'unica istanza di {@code MiniBabelNet}.
     */
    public static MiniBabelNet getInstance()
    {
        if (instance == null)
        {
            instance = new MiniBabelNet();
            instance.setLexicalSimilarityStrategy(new BabelLexicalSimilarityAdvanced());
            instance.setSemanticSimilarityStrategy(new BabelSemanticSimilarityAdvanced());
            instance.setDocumentSimilarityStrategy(new BabelDocumentSimilarityAdvanced());
        }
        return instance;
    }

    private Pair<Map<String,Synset>, Map<String, List<Synset>>> parseDictionary()
    {
        Map<String, Synset> synsetMap = new HashMap<>();
        Map<String, List<Synset>> wordToSynsets = new HashMap<>();
        try(BufferedReader br = Files.newBufferedReader(BabelPath.DICTIONARY_FILE_PATH.getPath()))
        {
            while(br.ready())
            {
                //prendo ogni riga, la splitto per "\t"
                List<String> infos = new ArrayList<>(List.of(br.readLine().toLowerCase().split("\t")));

                //la prima info è il synset ID, il resto sono i concetti del Synset
                Synset babelSynset = new BabelSynset(infos.remove(0), infos);
                synsetMap.put(babelSynset.getID(), babelSynset);
                for(String info : infos)
                    wordToSynsets.merge(info, new ArrayList<>(List.of(babelSynset)),(v1,v2)->{v1.addAll(v2); return v1;});
            }
        }
        catch(IOException e) {e.printStackTrace();}
        return new Pair<>(synsetMap, wordToSynsets);
    }

    private Pair<Map<String, List<String>>, Set<String>> parseLemmas()
    {
        Map<String, List<String>> wordToLemma = new HashMap<>();
        Set<String> lemmas = new HashSet<>();

        try(BufferedReader br = Files.newBufferedReader(BabelPath.LEMMATIZATION_FILE_PATH.getPath()))
        {
            while (br.ready())
            {
                String[] line = br.readLine().split("\t");
                wordToLemma.merge(line[0].toLowerCase(),
                        new ArrayList<>(List.of(line[1].toLowerCase())),
                        (v1,v2)->{v1.addAll(v2); return v1;});
                lemmas.add(line[1].toLowerCase());
            }
        }
        catch (IOException e){e.printStackTrace();}
        return new Pair<>(wordToLemma, lemmas);
    }

    private void parseGlosses()
    {
        try(Stream<String> stream = Files.lines(BabelPath.GLOSSES_FILE_PATH.getPath()))
        {
            stream.map(l->new ArrayList<>(List.of(l.split("\t"))))
                    .filter(l-> l.get(0).startsWith("bn:"))
                    .forEach(l->getSynset(l.remove(0)).addGlosses(l));
        }
        catch (IOException e) {e.printStackTrace(); }
    }

    private void parseRelations()
    {
        try(Stream<String> stream = Files.lines(BabelPath.RELATIONS_FILE_PATH.getPath()))
        {
            stream.map(line->line.split("\t"))
                    .forEach(rel-> {
                        getSynset(rel[0]).addRelation(rel[2], getSynset(rel[1]));
                        if(rel[2].equals("is-a"))
                            getSynset(rel[1]).addRelation("has-kind2", getSynset(rel[0]));
                        if(rel[2].equals("has-kind"))
                            getSynset(rel[0]).addRelation("has-kind2", getSynset(rel[1]));
                    });
        }
        catch (IOException e) {e.printStackTrace(); }
    }

    /**
     * Metodo che restituisce una lista con tutti i {@code Synset} associati ad ogni parola
     * della {@code Collection<String> words}.
     * @param words {@code Collection} di {@code String} che contiene le parole da cui prendere
     *                                i {@code Synset}.
     * @return Una lista con tutti i {@code Synset} associati ad ogni parola
     *         della {@code Collection<String> words}.
     */
    public List<Synset> getSynsets(Collection<String> words)
    {
        return words.stream().filter(w->getSynsets(w).size()>0).map(w->getSynsets(w).get(0)).collect(Collectors.toList());
    }

    /**
     * Metodo che restituisce una lista di tutti i {@code Synset} associati a una {@code String word}.
     * @param word Stringa da cui cercare i Synset.
     * @return Una lista di tutti i {@code Synset} associati a una {@code String word}.
     */
    public List<Synset> getSynsets(String word)
    {
        List<Synset> ls = wordToSynsets.get(word);
        return ls!=null ? ls : new ArrayList<>();
    }

    /**
     * Metodo che restituisce uno {@code Stream} di tutti i {@code Synset} presenti in {@code MiniBabelNet}.
     * @return {@code Stream} di tutti i {@code Synset} presenti in {@code MiniBabelNet}.
     */
    public Stream<Synset> getSynsetsStream() { return synsetMap.entrySet().stream().map(e->getSynset(e.getKey())); }

    /**
     * Metodo che restituisce una lista di tutti i {@code Synset} presenti in {@code MiniBabelNet}.
     * @return Una lista di tutti i {@code Synset} presenti in {@code MiniBabelNet}.
     */
    public Collection<Synset> getSynsets() { return getSynsetsStream().collect(Collectors.toList()); }

    /**
     * Dato un ID restituisce il Synset con quel particolare ID.
     * Se non esiste un {@code Synset} con quell'id allora restituisce null.
     * @param id Identificativo del synset da ricercare.
     * @return Synset con identificativo {@code id}.
     */
    public Synset getSynset(String id) { return synsetMap.get(id); }

    /**
     * Metodo che restituisce un resoconto del {@code Synset}, attraverso il {@code toString()} del {@code Synset}.
     * @param s {@code Synset} che deve essere "resoconto".
     * @return Restituisce un resoconto del {@code Synset}, attraverso il {@code toString()} del {@code Synset}.
     */
    public String getSynsetSummary(Synset s) { return s.toString(); }

    /**
     * Metodo che restituisce per una stringa le sue forme "lemma" in una lista.
     * @param word Stringa da lemmatizzare.
     * @return Lista contenente tutte le forme lemma del parametro @{@code word}.
     */
    public List<String> getLemmas(String word)
    {
        List<String> lemmas = wordToLemma.get(word);
        return lemmas==null ? new ArrayList<>() : lemmas;
    }

    /**
     * Metodo che restituisce il primo lemma che trova associato a una stringa.
     * @param word Stringa da lemmatizzare.
     * @return Forma lemma della parola {@code word}.
     */
    public String getLemma(String word)
    {
        List<String> lemmas = getLemmas(word);
        return lemmas.isEmpty() ? null : lemmas.get(0);
    }

    /**
     * Controlla se una parola si trova già nella sua forma lemma.
     * @param word Stringa da esaminare.
     * @return Booleano {@code true} se è già nella sua forma lemma, altrimenti {@code false}.
     */
    public boolean isLemma(String word) {return lemmas.contains(word); }

    /**
     * Metodo che restituisce il numero di {@code Synset} di cui è composto {@code MiniBabelNet}.
     * @return Numero di {@code Synset} di cui è composto {@code MiniBabelNet}.
     */
    public int size() {return synsetMap.size(); }

    /**
     * Metodo che consente l'utilizzo dello Strategy Pattern, settando
     * il campo {@code babelLexicalSimilarity} con un'interfaccia funzionale tramite lambda o
     * una classe che la implementi.
     * @param babelLexicalSimilarity Un oggetto {@code BabelLexicalSimilarity} che
     *                               estendendo l'interfaccia {@code SimilarityStrategy}
     *                               espone principalmente il metodo {@code compute(LinguisticObject o1, LinguisticObject o2)}.
     */
    public void setLexicalSimilarityStrategy(BabelLexicalSimilarity babelLexicalSimilarity) {this.babelLexicalSimilarity =  babelLexicalSimilarity; }

    /**
     * Metodo che consente l'utilizzo dello Strategy Pattern, settando
     * il campo {@code babelSemanticSimilarity} con un'interfaccia funzionale tramite lambda o
     * una classe che la implementi.
     * @param babelSemanticSimilarity Un oggetto {@code BabelSemanticSimilarity} che
     *                               estendendo l'interfaccia {@code SimilarityStrategy}
     *                               espone principalmente il metodo {@code compute(LinguisticObject o1, LinguisticObject o2)}
     */
    public void setSemanticSimilarityStrategy(BabelSemanticSimilarity babelSemanticSimilarity) {this.babelSemanticSimilarity = babelSemanticSimilarity; }

    /**
     * Metodo che consente l'utilizzo dello Strategy Pattern, settando
     * il campo {@code babelDocumentSimilarity} con un'interfaccia funzionale tramite lambda o
     * una classe che la implementi.
     * @param babelDocumentSimilarity Un oggetto {@code BabelDocumentSimilarity} che
     *                               estendendo l'interfaccia {@code SimilarityStrategy}
     *                               espone principalmente il metodo {@code compute(LinguisticObject o1, LinguisticObject o2)}
     */
    public void setDocumentSimilarityStrategy(BabelDocumentSimilarity babelDocumentSimilarity) {this.babelDocumentSimilarity = babelDocumentSimilarity; }

    /**
     * Questo metodo dati due {@code LinguisticObject} ne calcola la similarità, restituendo
     * un valore compreso tra 0 ed 1.
     * @param o1 Primo {@code LinguisticObject}.
     * @param o2 Secondo {@code LinguisticObject}.
     * @return Un valore compreso tra 0 ed 1 che esprime la similarità tra i due {@code LinguisticObject}.
     */
    public double computeSimilarity(LinguisticObject o1, LinguisticObject o2)
    {
        SimilarityStrategy strategy = (v1,v2)->0;
        if(o1 instanceof Word && o2 instanceof Word) strategy = this.babelLexicalSimilarity;
        else if(o1 instanceof BabelSynset && o2 instanceof BabelSynset) strategy = this.babelSemanticSimilarity;
        else if(o1 instanceof Document && o2 instanceof Document) strategy = this.babelDocumentSimilarity;
        try {
            return strategy.compute(o1,o2);
        } catch (NotALinguisticObject e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public Iterator<Synset> iterator() { return synsetMap.values().iterator(); }

    /**
     * Questo metodo restituisce tutte le radici di alberi con relazioni di
     * iperonimia fra nodi.
     * Il codice attraverso uno {@code Stream<Synset>} cerca tutti i nodi
     * che NON hanno relazioni "is-a", ovvero a salire ma che hanno almeno
     * un figlio con relazioni "has-kind".
     * @return Un {@code Set<Synset>} contenente tutte le radici di alberi con
     * relazioni di iperonimia fra nodi.
     */
    public Set<Synset> getHypernymTrees()
    {
        return getSynsetsStream()
                .filter(b->b.getRelations("is-a").isEmpty() && !b.getRelations("has-kind2").isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Calcola la profondità massima tra i vari alberi di iperonimia per tutti i pos.
     * @return Una Mappa che va da {@code PartOfSpeech} a {@code Integer}, dove
     * l'intero rappresenta la profondità massima in un albero di iperonimia con soli nodi
     * con quel determinato POS.
     */
    public Map<PartOfSpeech, Integer> getMaxDepthHypernymTrees()
    {
        return getHypernymTrees().stream()
                .collect(Collectors.toMap(Synset::getPOS,b->maxDepth(b,"has-kind2"),Math::max));
    }

    /**
     * Metodo che restituisce la profondità massima di un albero di {@code Synset}
     * data una radice {@code root} e un tipo di relazione "verso il basso".
     * @param root Radice dell'albero di cui calcolare la profondità.
     * @param downRelation Tipo di relazione "verso il basso".
     * @return Profondità massima di un albero di {@code Synset}.
     */
    public int maxDepth(Synset root, String downRelation)
    {
        //Inizializzo maxDepth con l'Integer più piccolo
        int maxDepth = Integer.MIN_VALUE;

        //Creo il closedSet e l'openSet per sapere quali nodi ho già visitato e devo ancora visitare
        Set<Synset> closedSet = new HashSet<>();
        LinkedList<Synset> openSet = new LinkedList<>();

        //Creo una mappa dove inserirò le distanze dei nodi dalla radice
        HashMap<Synset, Integer> distanceMap = new HashMap<>();

        //Setto la distanza della radice da se stesso con 0
        distanceMap.put(root,0);

        //Aggiungo la radice all'openSet
        openSet.add(root);

        while (!openSet.isEmpty())
        {
            //Prendo un Synset a caso dall'openSet
            Synset current = openSet.pop();

            //Aggiungo il Synset che stiamo visitando al closedSet, così da non visitarlo dopo
            closedSet.add(current);

            for(Synset child : current.getRelations(downRelation))
            {
                //Setto la distanza dei figli dalla radice (distanza_corrente+1)
                distanceMap.put(child, distanceMap.get(current)+1);

                //Se non è nel closedSet aggiungi il figlio all'openSet per visitarlo successivamente
                if(!closedSet.contains(child)) openSet.add(child);

                //se trovi una profondità che sia maggiore di quella attuale, allora sostituisci maxDepth
                if(maxDepth<distanceMap.get(child)) maxDepth = distanceMap.get(child);
            }
        }

        return maxDepth;
    }


    /**
     * Metodo che dato un nodo di inizio e uno di fine, tramite
     * ricerca BFS restituisce una distanza fra il {@code Synset start}.
     * ed il {@code Synset end}. E' possibile specificare una certa relazione
     * fra Synset.
     * @param start {@code Synset} di partenza.
     * @param end   {@code Synset} di arrivo.
     * @param rels  Relazione fra i nodi da esplorare.
     * @return distanza fra il {@code Synset start} ed il {@code Synset end}, {@code -1}
     * se non trova un percorso.
     */
    public int distance(Synset start, Synset end, String... rels)
    {
       //openSet, coda di nodi da visitare
        LinkedList<Synset> openSet = new LinkedList<>();
        //closedSet, insieme di nodi già visitati
        Set<Synset> closedSet = new HashSet<>();

        //Creo una mappa dove inserirò le distanze dei nodi dal Synset di inizio
        HashMap<Synset, Integer> distanceMap = new HashMap<>();

        //Setto la distanza dell'inizio da se stesso con 0
        distanceMap.put(start, 0);

        //Aggiungo il Synset iniziale all'openSet
        openSet.add(start);

        while (!openSet.isEmpty())
        {
            //Prendo il Synset con distanza minore dal Synset di inizio
            Synset current = openSet.stream().min(Comparator.comparing(distanceMap::get)).get();

            //Lo tolgo dall'openSet e lo aggiungo al closedSet
            openSet.remove(current);
            closedSet.add(current);

            //Se trovo il nodo di fine, restituisco la sua distanza dal nodo iniziale
            if(current.equals(end))
                return distanceMap.get(current);

            //Altrimenti vedo tra i suoi vicini/figli
            for(Synset child :current.getRelations(rels))
            {

                //Calcolo la distanza del figlio
                int newDist = distanceMap.get(current)+1;

                //Controllo se il Synset è già stato visitato
                if(newDist<distanceMap.getOrDefault(child, Integer.MAX_VALUE) && !closedSet.contains(child))
                {
                    openSet.add(child);
                    distanceMap.put(child, newDist);
                }
            }
        }
        return -1;
    }

    /**
     * Questo metodo tramite l'algoritmo dijkstra, restituisce la grandezza
     * del percorso minimo fra due {@code Synset}.
     * @param root  {@code Synset} di inizio.
     * @param end   {@code Synset} di arrivo.
     * @param rels  Relazione tra cui cercare.
     * @return  Un {@code int} che rappresenta la grandezza del percorso minimo fra due {@code Synset}
     * o {@code -1} se non trova un percorso.
     */
    public int minDistance(Synset root, Synset end, String... rels)
    {
        //Creo una mappa dove inserirò le distanze dei nodi dal Synset di inizio
        HashMap<Synset, Integer> distanceMap = new HashMap<>();

        //Setto la distanza del Synset di inizio da se stesso con 0
        distanceMap.put(root, 0);

        //Mi creo una coda di priorità sui Synset da visitare, con priorità basata sulla distanza dal synset di inizio.
        PriorityQueue<Synset> queue = new PriorityQueue<>(11, Comparator.comparing(distanceMap::get));
        //closedSet che contiene tutti i nodi già visitati
        Set<Synset> closedSet = new HashSet<>();

        //Aggiungo il nodo iniziale alla coda.
        queue.add(root);

        while (!queue.isEmpty())
        {
            //Prendo il primo nodo della coda, quello con priorità maggiore
            Synset current = queue.remove();

            //Lo aggiungo al closedSet
            closedSet.add(current);

            //Se è il nodo di arrivo allora ritorno la sua distanza con il nodo di inizio
            if(current.equals(end))
                return distanceMap.get(current);

            //Altrimenti cerco tra i suoi vicini/figli
            for(Synset child : current.getRelations(rels))
            {
                //Mi calcolo la nuova distanza dei figli
                int newDist = distanceMap.get(current)+1;
                //Se è minore di quella che avevo precedentemente calcolato e il figlio deve essere visitato lo aggiungo
                //alla coda
                if(newDist<distanceMap.getOrDefault(child, Integer.MAX_VALUE) && !closedSet.contains(child))
                {
                    distanceMap.put(child, newDist);
                    queue.add(child);
                }
            }
        }
        return -1;
    }


}

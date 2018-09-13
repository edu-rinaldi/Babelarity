package it.uniroma1.lcl.babelarity;

import it.uniroma1.lcl.babelarity.utils.BabelPath;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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

                Synset babelSynset = new BabelSynset(infos.remove(0), infos);
                synsetMap.put(babelSynset.getID(), babelSynset);
                for(String info : infos)
                {
                    if (wordToSynsets.containsKey(info)) wordToSynsets.get(info).add(babelSynset);
                    else wordToSynsets.put(info, new ArrayList<>(List.of(babelSynset)));
                }
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


    public void setLexicalSimilarityStrategy(BabelLexicalSimilarity babelLexicalSimilarity) {this.babelLexicalSimilarity =  babelLexicalSimilarity; }
    public void setSemanticSimilarityStrategy(BabelSemanticSimilarity babelSemanticSimilarity) {this.babelSemanticSimilarity = babelSemanticSimilarity; }
    public void setDocumentSimilarityStrategy(BabelDocumentSimilarity babelDocumentSimilarity) {this.babelDocumentSimilarity = babelDocumentSimilarity; }

    public double computeSimilarity(LinguisticObject o1, LinguisticObject o2)
    {
        SimilarityStrategy strategy = (v1,v2)->0;
        if(o1 instanceof Word && o2 instanceof Word) strategy = this.babelLexicalSimilarity;
        if(o1 instanceof BabelSynset && o2 instanceof BabelSynset) strategy = this.babelSemanticSimilarity;
        if(o1 instanceof Document && o2 instanceof Document) strategy = this.babelDocumentSimilarity;
        try {
            return strategy.compute(o1,o2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Iterator<Synset> iterator() { return synsetMap.values().iterator(); }


    public Set<Synset> getRoots()
    {
        return synsetMap.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .filter(b->b.getRelations("is-a").isEmpty() && !b.getRelations("has-kind").isEmpty())
                .collect(Collectors.toSet());
    }

    public int maxDepth(Synset root, String downRelation)
    {
        //set default max
        int max = Integer.MIN_VALUE;

        //creating closed and open set
        Set<Synset> closedSet = new HashSet<>();
        LinkedList<Synset> openSet = new LinkedList<>();

        //Distance map
        HashMap<Synset, Integer> distanceMap = new HashMap<>();

        //set distance from root (0)
        distanceMap.put(root,0);

        //add root to open set
        openSet.add(root);

        while (!openSet.isEmpty())
        {
            //pop a synset
            Synset current = openSet.pop();

            //add current to closed set
            closedSet.add(current);

            for(Synset child : current.getRelations(downRelation))
            {
                //set child distance from root (current.distance+1)
                distanceMap.put(child, distanceMap.get(current)+1);
                //if not in closed set then add to open set because it must be visited
                if(!closedSet.contains(child)) openSet.add(child);

                //if it finds a distance that is higher then actual, replace it
                if(max<distanceMap.get(child)) max = distanceMap.get(child);
            }
        }
        return max;
    }


    public int minDistance(Synset root, Synset end, String... rels)
    {
        HashMap<Synset, Integer> distanceMap = new HashMap<>();
        distanceMap.put(root, 0);
        PriorityQueue<Synset> queue = new PriorityQueue<>(11, Comparator.comparing(distanceMap::get));
        Set<Synset> closed = new HashSet<>();
        queue.add(root);

        while (!queue.isEmpty())
        {
            Synset current = queue.remove();
            closed.add(current);

            if(current.equals(end))
                return distanceMap.get(current);

            for(Synset child : current.getRelations(rels))
            {
                int alt = distanceMap.get(current)+1;
                if(alt<distanceMap.getOrDefault(child, Integer.MAX_VALUE) && !closed.contains(child))
                {
                    distanceMap.put(child, alt);
                    queue.add(child);
                }
            }
        }
        return -1;
    }

    public int minDistance(Synset root, Synset end)
    {
        HashMap<Synset, Integer> distanceMap = new HashMap<>();
        distanceMap.put(root, 0);
        PriorityQueue<Synset> queue = new PriorityQueue<>(11, Comparator.comparing(distanceMap::get));
        Set<Synset> closed = new HashSet<>();
        queue.add(root);

        while (!queue.isEmpty())
        {
            Synset current = queue.remove();
            closed.add(current);

            if(current.equals(end))
                return distanceMap.get(current);

            for(Synset child : current.getRelations())
            {
                int alt = distanceMap.get(current)+1;
                if(alt<distanceMap.getOrDefault(child, Integer.MAX_VALUE) && !closed.contains(child))
                {
                    distanceMap.put(child, alt);
                    queue.add(child);
                }
            }
        }
        return -1;
    }

    public int distance(Synset start, Synset end, String... rels)
    {
        //Queue of node that must be visited
        LinkedList<Synset> openSet = new LinkedList<>();
        //Set of nodes that have been already visited
        Set<Synset> closedSet = new HashSet<>();

        //distance map
        HashMap<Synset, Integer> distanceMap = new HashMap<>();

        //set distance from root
        distanceMap.put(start, 0);
        //add to openSet the root
        openSet.add(start);

        while (!openSet.isEmpty())
        {
            //get from the openSet the node with lower distance
            Synset current = openSet.stream().min(Comparator.comparing(distanceMap::get)).get();

            //remove from openSet the current node and add it to closed
            openSet.remove(current);
            closedSet.add(current);

            //if we found the end node
            if(current.equals(end))
                return distanceMap.get(current);

            //go down in the tree using has-kind relations
            for(Synset child :current.getRelations(rels))
            {

                //childs distance
                int newDist = distanceMap.get(current)+1;

                //check if child has been already visited
                if(newDist<distanceMap.getOrDefault(child, Integer.MAX_VALUE) && !closedSet.contains(child))
                {
                    openSet.add(child);
                    distanceMap.put(child, newDist);
                }
            }
        }
        return -1;
    }

    public int distance(Synset start, Synset end)
    {
        //Queue of node that must be visited
        LinkedList<Synset> openSet = new LinkedList<>();
        //Set of nodes that have been already visited
        Set<Synset> closedSet = new HashSet<>();

        //distance map
        HashMap<Synset, Integer> distanceMap = new HashMap<>();

        //set distance from root
        distanceMap.put(start, 0);
        //add to openSet the root
        openSet.add(start);

        while (!openSet.isEmpty())
        {
            //get from the openSet the node with lower distance
            Synset current = openSet.stream().min(Comparator.comparing(distanceMap::get)).get();

            //remove from openSet the current node and add it to closed
            openSet.remove(current);
            closedSet.add(current);

            //if we found the end node
            if(current.equals(end))
                return distanceMap.get(current);

            //go down in the tree using has-kind relations
            for(Synset child :current.getRelations())
            {

                //childs distance
                int newDist = distanceMap.get(current)+1;

                //check if child has been already visited
                if(newDist<distanceMap.getOrDefault(child, Integer.MAX_VALUE) && !closedSet.contains(child))
                {
                    openSet.add(child);
                    distanceMap.put(child, newDist);
                }
            }
        }
        return -1;
    }


}

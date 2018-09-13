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

    public List<Synset> getSynsets(Collection<String> words)
    {
        return words.stream().filter(w->getSynsets(w).size()>0).map(w->getSynsets(w).get(0)).collect(Collectors.toList());
    }

    public List<Synset> getSynsets(String word)
    {
        List<Synset> ls = wordToSynsets.get(word);
        return ls!=null ? ls : new ArrayList<>();
    }

    public List<Synset> getSynsets()
    {
        return synsetMap.entrySet().stream().map(e->getSynset(e.getKey())).collect(Collectors.toList());
    }

    public Synset getSynset(String id) { return synsetMap.get(id); }

    public String getSynsetSummary(Synset s) { return s.toString(); }

    public List<String> getLemmas(String word)
    {
        List<String> lemmas = wordToLemma.get(word);
        return lemmas==null ? new ArrayList<>() : lemmas;
    }
    public String getLemma(String word)
    {
        List<String> lemmas = getLemmas(word);
        return lemmas.isEmpty() ? null : lemmas.get(0);
    }
    public boolean isLemma(String word) {return lemmas.contains(word); }

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

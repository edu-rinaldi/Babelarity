package it.uniroma1.lcl.babelarity;

import it.uniroma1.lcl.babelarity.utils.BabelPath;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class MiniBabelNet implements Iterable<Synset>
{

    private static MiniBabelNet instance;

    private Map<Word, List<BabelSynset>> wordToSynsets;
    private Map<Word, String> wordToLemma;
    private Set<String> lemmas;
    private Map<String, Synset> synsetMap;

    private BabelLexicalSimilarityAdvanced bl;

    private MiniBabelNet()
    {
        wordToSynsets = new HashMap<>();
        wordToLemma = new HashMap<>();
        lemmas = new HashSet<>();
        synsetMap = new HashMap<>();
        parseDictionary();
        loadAllLemmas();
        parseGlosses();
        parseRelations();
    }


    public static MiniBabelNet getInstance()
    {
        if (instance == null)
            instance = new MiniBabelNet();
        return instance;
    }

    public List<BabelSynset> getSynsets(String word)
    {
        List<BabelSynset> ls = wordToSynsets.get(Word.fromString(word));
        return ls!=null ? ls : new ArrayList<>();
    }

    public List<BabelSynset> getSynsets()
    {
        return synsetMap.entrySet().stream().map(e->getSynset(e.getKey())).collect(Collectors.toList());
    }

    public BabelSynset getSynset(String id)
    {
        Synset s = synsetMap.get(id);
        return s instanceof BabelSynset ? (BabelSynset)s : null;
    }

    public String getLemmas(String word) {return wordToLemma.get(Word.fromString(word)); }

    public boolean isLemma(String word) {return lemmas.contains(word); }

    public int size() {return synsetMap.size(); }

    /**
     * Restituisce le informazioni inerenti al Synset fornito in input sotto forma di stringa.
     * Il formato della stringa è il seguente:
     * ID\tPOS\tLEMMI\tGLOSSE\tRELAZIONI
     * Le componenti LEMMI, GLOSSE e RELAZIONI possono contenere più elementi, questi sono separati dal carattere ";"
     * Le relazioni devono essere condificate nel seguente formato:
     * TARGETSYNSET_RELNAME   es. bn:00081546n_has-kind
     *
     * es: bn:00047028n	NOUN	word;intelligence;news;tidings	Information about recent and important events	bn:0000001n_has-kind;bn:0000001n_is-a
     *
     * @param s
     * @return
     */
    public String getSynsetSummary(Synset s)
    {
        //da fare robe...
        return s.toString();
    }

    public void setLexicalSimilarityStrategy() { this.bl = new BabelLexicalSimilarityAdvanced(this); }

    public void setSemanticSimilarityStrategy()
    {
        //da fare robe...
    }

    public void setDocumentSimilarityStrategy()
    {
        //da fare robe...
    }

    public double computeSimilarity(LinguisticObject o1, LinguisticObject o2)
    {
        //da fare robe...
        return 0;
    }

    @Override
    public Iterator<Synset> iterator()
    {
        return synsetMap.values().iterator();
    }

    private void parseDictionary()
    {
        try(BufferedReader br = Files.newBufferedReader(BabelPath.DICTIONARY_FILE_PATH.getPath()))
        {
            while(br.ready())
            {
                //prendo ogni riga, la splitto per "\t"
                List<String> infos = new ArrayList<>(List.of(br.readLine().toLowerCase().split("\t")));

                BabelSynset babelSynset = new BabelSynset(infos.remove(0), infos);
                synsetMap.put(babelSynset.getID(), babelSynset);
                for(String info : infos)
                {
                    Word word = Word.fromString(info);
                    if (wordToSynsets.containsKey(word)) wordToSynsets.get(word).add(babelSynset);
                    else wordToSynsets.put(word, new ArrayList<>(List.of(babelSynset)));
                }
            }
        }
        catch(IOException e) {e.printStackTrace();}
    }

    private void loadAllLemmas()
    {
        try(BufferedReader br = Files.newBufferedReader(BabelPath.LEMMATIZATION_FILE_PATH.getPath()))
        {
            while (br.ready())
            {
                String[] line = br.readLine().split("\t");
                wordToLemma.merge(Word.fromString(line[0].toLowerCase()),line[1].toLowerCase(),(v1,v2)->v1);
                lemmas.add(line[1].toLowerCase());
            }
        }catch (IOException e){e.printStackTrace();}
    }


    private void loadAllLemmas2()
    {
        try(Stream<String> stream = Files.lines(BabelPath.LEMMATIZATION_FILE_PATH.getPath()))
        {
            wordToLemma = stream.map(l->l.split("\t"))
                    .collect(toMap(l->Word.fromString(l[0].toLowerCase()),l->l[1].toLowerCase(),(v1,v2)->v1));
        }
        catch (IOException e){e.printStackTrace();}
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
                    .filter(rel->!rel[1].equals(rel[0]))
                    .forEach(rel-> {
                        getSynset(rel[0]).addRelation(rel[2], getSynset(rel[1]));
                        if(rel[2].equals("is-a"))
                            getSynset(rel[1]).addRelation("has-kind", getSynset(rel[0]));
                    });
        }
        catch (IOException e) {e.printStackTrace(); }
    }

    public Set<BabelSynset> getRoots()
    {
        return synsetMap.entrySet()
                .stream()
                .map(e-> (BabelSynset)e.getValue())
                .filter(b->b.getRelations("is-a").isEmpty() && !b.getRelations("has-kind").isEmpty())
                .collect(Collectors.toSet());
    }

    public int maxDepth(BabelSynset root, String downRelation)
    {
        //set default max
        int max = Integer.MIN_VALUE;

        //creating closed and open set
        Set<BabelSynset> closedSet = new HashSet<>();
        LinkedList<BabelSynset> openSet = new LinkedList<>();

        //set distance from root (0)
        root.setDist(0);

        //add root to open set
        openSet.add(root);

        while (!openSet.isEmpty())
        {
            //pop a synset
            BabelSynset current = openSet.pop();

            //add current to closed set
            closedSet.add(current);

            for(BabelSynset child : current.getRelations(downRelation))
            {
                //set child distance from root (current.distance+1)
                child.setDist(current.getDist()+1);

                //if not in closed set then add to open set because it must be visited
                if(!closedSet.contains(child)) openSet.add(child);

                //if it finds a distance that is higher then actual, replace it
                if(max<child.getDist()) max = child.getDist();
            }
        }
        //reset distance from root
        closedSet.forEach(b->b.setDist(Integer.MAX_VALUE));
        return max;
    }

    public int distance(BabelSynset start, BabelSynset end)
    {
        //Queue of node that must be visited
        LinkedList<BabelSynset> openSet = new LinkedList<>();
        //Set of nodes that have been already visited
        Set<BabelSynset> closedSet = new HashSet<>();

        //set distance from root
        start.setDist(0);

        //add to openSet the root
        openSet.add(start);

        while (!openSet.isEmpty())
        {
            //get from the openSet the node with lower distance
            BabelSynset current = openSet.stream().min(Comparator.comparing(BabelSynset::getDist)).get();

            //remove from openSet the current node and add it to closed
            openSet.remove(current);
            closedSet.add(current);

            //if we found the end node
            if(current.equals(end))
            {
                //get current distance from root
                int dist = current.getDist();
                //reset current distance from root
                closedSet.forEach(b->b.setDist(Integer.MAX_VALUE));
                return dist;
            }

            //go down in the tree using has-kind relations
            for(BabelSynset child :current.getRelations())
            {

                //childs distance
                int newDist = current.getDist()+1;

                //check if child has been already visited
                if(newDist<child.getDist() && !closedSet.contains(child))
                {
                    openSet.add(child);
                    child.setDist(newDist);
                }
            }
        }
        //reset distance
        closedSet.forEach(b->b.setDist(Integer.MAX_VALUE));
        return -1;
    }

}

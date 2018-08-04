package it.uniroma1.lcl.babelarity;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class MiniBabelNet implements Iterable<Synset>
{

    private static MiniBabelNet instance;

    private final Path RESOURCES_PATH;
    private final Path DICTIONARY_FILE_PATH;
    private final Path GLOSSES_FILE_PATH;
    private final Path LEMMATIZATION_FILE_PATH;
    private final Path RELATION_FILE_PATH;

    private Map<Word, List<Synset>> wordToSynsets;
    private Map<String, Synset> synsetMap;

    private MiniBabelNet()
    {
        //init paths constants
        RESOURCES_PATH = Paths.get("resources/miniBabelNet/");
        DICTIONARY_FILE_PATH = Paths.get(RESOURCES_PATH.toString(), "dictionary.txt");
        GLOSSES_FILE_PATH = Paths.get(RESOURCES_PATH.toString(), "glosses.txt");
        LEMMATIZATION_FILE_PATH = Paths.get(RESOURCES_PATH.toString(), "lemmatization-en.txt");
        RELATION_FILE_PATH = Paths.get(RESOURCES_PATH.toString(), "relations.txt");

        //init babelnet
        wordToSynsets = new HashMap<>();
        synsetMap = new HashMap<>();
        parseDictionary();
    }

    public static MiniBabelNet getInstance()
    {
        if (instance == null)
            instance = new MiniBabelNet();
        return instance;
    }

    public List<Synset> getSynsets(String word)
    {
        return wordToSynsets.get(Word.fromString(word));
    }

    public Synset getSynset(String id)
    {
        return synsetMap.get(id);
    }

    public List<String> getLemmas(String word)
    {
        Word w = new Word(word);
        return w.findLemmasFromSource(LEMMATIZATION_FILE_PATH);
    }

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

    public void setLexicalSimilarityStrategy()
    {
        //da fare robe...
    }

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

    /*
        TODO: WORK ON
     */
    private void parseDictionary2()
    {
        try(Stream<String> stream = Files.lines(DICTIONARY_FILE_PATH))
        {

        }
        catch (IOException e){ e.printStackTrace(); }
    }


    private void parseDictionary()
    {
        try(BufferedReader br = Files.newBufferedReader(DICTIONARY_FILE_PATH))
        {
            while(br.ready())
            {
                //prendo ogni riga, la splitto per "\t"
                List<Word> infos = new ArrayList<>(Word.fromListOfString(List.of(br.readLine().split("\t"))));
                BabelSynset babelSynset = new BabelSynset(infos.remove(0).toString(), infos);
                synsetMap.put(babelSynset.getID(), babelSynset);
                for(Word info : infos)
                {
                    if (wordToSynsets.containsKey(info))
                        wordToSynsets.get(info).add(babelSynset);
                    else
                        wordToSynsets.put(info, new ArrayList<>(List.of(babelSynset)));
                }


            }
        }
        catch(IOException e) {e.printStackTrace();}
    }

    public static void main(String[] args) {
        MiniBabelNet b = new MiniBabelNet();
        System.out.println(b.getLemmas("laughing"));
    }
}

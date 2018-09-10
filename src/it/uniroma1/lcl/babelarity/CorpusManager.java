package it.uniroma1.lcl.babelarity;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class CorpusManager implements Iterable<Document>
{
    private static CorpusManager instance;
    private Map<String, Document> documents;

    private CorpusManager() {documents = new HashMap<>(); }

    public static CorpusManager getInstance()
    {
        if(instance==null)
            instance = new CorpusManager();
        return instance;
    }

    public Document parseDocument(Path path)
    {
        Document d = null;
        try(BufferedReader br = Files.newBufferedReader(path))
        {
            String[] firstLine = br.readLine().split("\t");
            StringBuilder txt = new StringBuilder();
            while (br.ready())
                txt.append(br.readLine());
            d = new Document(firstLine[1],firstLine[0], txt.toString());
            documents.put(firstLine[1], d);
        }
        catch (IOException e) {e.printStackTrace(); }

        return d;
    }

    public Document parseDocument(String path) {return parseDocument(Paths.get(path)); }

    public Document loadDocument(String id) {return documents.get(id); }
    public void saveDocument(Document document) {documents.put(document.getId(), document); }

    @Override
    public Iterator<Document> iterator() {return documents.values().iterator(); }
}

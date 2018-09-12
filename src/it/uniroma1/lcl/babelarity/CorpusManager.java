package it.uniroma1.lcl.babelarity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class CorpusManager implements Iterable<Document>
{
    private static CorpusManager instance;
    public final Path STORAGE_PATH;

    private CorpusManager(Path STORAGE_PATH){ this.STORAGE_PATH = STORAGE_PATH;}

    public static CorpusManager getInstance() { return getInstance(Paths.get("resources/storedDocs/")); }
    public static CorpusManager getInstance(Path STORAGE_PATH)
    {
        if(instance==null) instance = new CorpusManager(STORAGE_PATH);
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
        }
        catch (IOException e) {e.printStackTrace(); }

        return d;
    }

    public Document parseDocument(String path) {return parseDocument(Paths.get(path)); }

    public Document loadDocument(String id)
    {
        Document d = null;
        try(FileInputStream fileIn = new FileInputStream(STORAGE_PATH.resolve(id+".ser").toString());
        ObjectInputStream in = new ObjectInputStream(fileIn))
        {
            d = (Document) in.readObject();
        }
        catch (IOException | ClassNotFoundException e){e.printStackTrace();}
        return d;
    }

    public void saveDocument(Document document)
    {
        try (FileOutputStream fileOut = new FileOutputStream(STORAGE_PATH.resolve(document.getId()+".ser").toString());
                ObjectOutputStream out = new ObjectOutputStream(fileOut))
        {
            out.writeObject(document);
        }
        catch (IOException e){ e.printStackTrace();}
    }

    @Override
    public Iterator<Document> iterator()
    {
        List<Document> docs = new ArrayList<>();
        File[] documents = new File(STORAGE_PATH.toString()).listFiles();
        if(documents!=null)
            for (File document : documents)
            {
                String id = document.getName().substring(0, document.getName().indexOf('.'));
                docs.add(loadDocument(id));
            }
        return docs.iterator();
    }
}

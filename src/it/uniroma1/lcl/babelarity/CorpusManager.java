package it.uniroma1.lcl.babelarity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Questa classe si occupa della gestione dei documenti
 * rappresentati con la classe {@code Document}.
 *
 * Implementa l'intefaccia {@code Iterable} così che è possibile
 * iterare con un forEach sui Documenti contenuti nel {@code CorpusManager}.
 */
public class CorpusManager implements Iterable<Document>
{
    private static CorpusManager instance;
    private final Path STORAGE_PATH;

    private CorpusManager(){ this.STORAGE_PATH = Paths.get("resources/documents/");}

    /**
     * Questo metodo ci consente di applicare il pattern Singleton
     * su questa classe, così da evitare istanze multiple di {@code CorpusManager}.
     * @return Un'unica istanza di {@code CorpusManager}.
     */
    public static CorpusManager getInstance()
    {
        if(instance==null) instance = new CorpusManager();
        return instance;
    }


    /**
     * Metodo che consente il parse di un documento.
     * Il documento da parsare DEVE avere il seguente formato:
     * Sulla prima riga ci devono essere: TITOLO\tID_DOCUMENTO
     * Dalla seconda riga ci deve essere il contenuto del documento.
     * @param path Percorso al documento da parsare.
     * @return Una nuova istanza di {@code Document}.
     */
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

    /**
     * Overload del metodo {@code parseDocument(Path path)}, qui è possibile
     * specificare il path tramite stringa.
     * @param path Stringa che rappresenta il path al documento da parsare.
     * @return Una nuova istanza di {@code Document}.
     */
    public Document parseDocument(String path) {return parseDocument(Paths.get(path)); }

    /**
     * Carica da disco un oggetto {@code Document} ricercandolo per id.
     * @param id Identificativo del documento da caricare.
     * @return Un oggetto {@code Document} ricercato per id.
     */
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

    /**
     * Questo metodo salva su disco, nel percorso {@code STORAGE_PATH +"/id_doc.ser"}
     * l'oggetto {@code Document} che gli viene passato come parametro.
     * @param document Documento da salvare su disco.
     */
    public void saveDocument(Document document)
    {
        try (FileOutputStream fileOut = new FileOutputStream(STORAGE_PATH.resolve(document.getId()+".ser").toString());
                ObjectOutputStream out = new ObjectOutputStream(fileOut))
        {
            out.writeObject(document);
        }
        catch (IOException e){ e.printStackTrace();}
    }

    /**
     * Questo metodo restituisce un iteratore sui documenti salvati su disco dal Corpus.
     * @return Iteratore sui documenti salvati su disco dal Corpus.
     */
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

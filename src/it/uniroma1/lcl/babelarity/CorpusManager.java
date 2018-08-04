package it.uniroma1.lcl.babelarity;

import java.nio.file.Path;

public class CorpusManager
{
    private static CorpusManager instance;


    private CorpusManager()
    {

    }

    public static CorpusManager getInstance()
    {
        if(instance==null)
            instance = new CorpusManager();
        return instance;
    }

    public Document parseDocument(Path path)
    {
        return null;
    }

    public Document loadDocument(String id)
    {
        return null;
    }

    public void saveDocument(Document document)
    {

    }
}

package it.uniroma1.lcl.babelarity;

public class Document implements LinguisticObject
{
    private String id;
    private String title;
    private String content;

    public Document(String id, String title, String content)
    {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public String getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getContent()
    {
        return content;
    }
}

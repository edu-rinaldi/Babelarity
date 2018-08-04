package it.uniroma1.lcl.babelarity;

public class Word implements LinguisticObject
{
    private String word;


    public Word(String s)
    {
        this.word = s;
    }

    @Override
    public String toString()
    {
        return word;
    }

    public static Word fromString(String s)
    {
        return new Word(s);
    }


    //TODO: Implementare equals()
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    //TODO: Implementare hashCode()
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

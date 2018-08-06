package it.uniroma1.lcl.babelarity;

public class Relation<T extends Synset>
{
    private T source;
    private T target;
    private String simpleRel;
    private String completeRel;

    public Relation(T source, T target, String simpleRel, String completeRel)
    {
        this.source = source;
        this.target = target;
        this.simpleRel = simpleRel;
        this.completeRel = completeRel;
    }

    public T getSource() {return source; }
    public T getTarget() {return target; }
    public String getSimpleRel() {return simpleRel; }
    public String getCompleteRel() {return completeRel; }

    @Override
    public String toString() {
        return target.getID()+"_"+simpleRel;
    }
}

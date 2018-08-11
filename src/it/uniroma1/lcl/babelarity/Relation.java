package it.uniroma1.lcl.babelarity;

public class Relation<T extends Synset, R>
{
    private T source;
    private T target;
    private R rel;

    public Relation(T source, T target, R rel)
    {
        this.source = source;
        this.target = target;
        this.rel = rel;
    }

    public T getSource() {return source; }
    public T getTarget() {return target; }
    public R getRel() {return rel; }

    @Override
    public String toString() {return target.getID()+"_"+rel; }
}

package it.uniroma1.lcl.babelarity;

@FunctionalInterface
public interface SimilarityStrategy<T extends LinguisticObject>
{
    double compute(T o1, T o2) throws Exception;
}

package it.uniroma1.lcl.babelarity;

@FunctionalInterface
public interface SimilarityStrategy
{
    double compute(LinguisticObject o1, LinguisticObject o2) throws Exception;
}

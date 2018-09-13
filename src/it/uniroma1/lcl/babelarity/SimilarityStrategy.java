package it.uniroma1.lcl.babelarity;

import it.uniroma1.lcl.babelarity.exception.NotALinguisticObject;

/**
 * Interfaccia funzionale che richiede l'implementazione
 * di una strategia di similarità fra {@code LinguisticObject}.
 */
@FunctionalInterface
public interface SimilarityStrategy
{
    /**
     * Metodo che calcola la similarità fra due {@code LinguisticObject}.
     * @param o1 Primo oggetto linguistico da confrontare.
     * @param o2 Secondo oggetto linguistico da confrontare.
     * @return Un valore da 0 a 1 che esprime la similarità fra {@code o1} e {@code o2}.
     * @throws NotALinguisticObject eccezione lanciata se uno dei due parametri non risulta
     *                              essere confrontabile.
     */
    double compute(LinguisticObject o1, LinguisticObject o2) throws NotALinguisticObject;
}

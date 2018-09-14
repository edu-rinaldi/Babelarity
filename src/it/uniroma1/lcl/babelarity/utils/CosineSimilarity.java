package it.uniroma1.lcl.babelarity.utils;

/**
 * Classe di utilità per il calcolo della similarità del coseno tra due vettori.
 * Non è possibile fare un riuso del codice su vettori di primitivi, ma la classe
 * mette comunque a disposizione una funzione con oggetti generici che estendano almeno
 * la classe {@code Number}.
 */
public class CosineSimilarity
{
    /**
     * Metodo che calcola la similarità del coseno tra due vettori di oggetti numerici.
     * @param v1 Primo vettore.
     * @param v2 Secondo vettore.
     * @param <T> Tipo del vettore, deve estendere almeno {@code Number}.
     * @return un valore tra 0 o 1 che indica la similarità tra i due vettori.
     */
    public static <T extends Number> double cosineSimilarity(T[] v1,T[] v2)
    {
        double numerator = 0;
        double sqrt1 = 0;
        double sqrt2 = 0;

        for(int i=0; i<v1.length; i++)
        {
            double val1 = v1[i].doubleValue();
            double val2 = v2[i].doubleValue();

            numerator += val1*val2;
            sqrt1 += val1*val1;
            sqrt2 += val2*val2;
        }
        sqrt1 = Math.sqrt(sqrt1);
        sqrt2 = Math.sqrt(sqrt2);

        return numerator/(sqrt1*sqrt2);
    }

    /**
     * Metodo che calcola la similarità del coseno tra due vettori di {@code float} primitivi.
     * @param v1 Primo vettore.
     * @param v2 Secondo vettore.
     * @return un valore tra 0 o 1 che indica la similarità tra i due vettori.
     */
    public static double cosineSimilarity(float[] v1, float[] v2)
    {
        double numerator = 0;
        double sqrt1 = 0;
        double sqrt2 = 0;

        for(int i=0; i<v1.length; i++)
        {
            double val1 = v1[i];
            double val2 = v2[i];

            numerator += val1*val2;
            sqrt1 += val1*val1;
            sqrt2 += val2*val2;
        }
        sqrt1 = Math.sqrt(sqrt1);
        sqrt2 = Math.sqrt(sqrt2);

        return numerator/(sqrt1*sqrt2);
    }

    /**
     * Metodo che calcola la similarità del coseno tra due vettori di {@code int} primitivi.
     * @param v1 Primo vettore.
     * @param v2 Secondo vettore.
     * @return un valore tra 0 o 1 che indica la similarità tra i due vettori.
     */
    public static double cosineSimilarity(int[] v1, int[] v2)
    {
        double numerator = 0;
        double sqrt1 = 0;
        double sqrt2 = 0;
        for(int i=0; i<v1.length; i++)
        {
            double val1 = v1[i];
            double val2 = v2[i];

            numerator += val1*val2;
            sqrt1 += val1*val1;
            sqrt2 += val2*val2;
        }

        sqrt1 = Math.sqrt(sqrt1);
        sqrt2 = Math.sqrt(sqrt2);

        return numerator/(sqrt1*sqrt2);
    }


}

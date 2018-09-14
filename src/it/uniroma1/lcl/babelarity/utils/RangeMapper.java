package it.uniroma1.lcl.babelarity.utils;

/**
 * Classe di utilità che mappa un certo valore x da un range ad un altro, specificando
 * l'input range e l'output range desiderato.
 * Non c'è un riuso del codice per velocizzare i conti e per renderli più precisi.
 */
public class RangeMapper
{
    /**
     * mappa un certo valore x da un range ad un altro, specificando
     * l'input range e l'output range desiderato.
     * @param x Valore da mappare.
     * @param in_min Estremo inferiore del range in input.
     * @param in_max Estremo superiore del range in input.
     * @param out_min Estremo inferiore del range in output.
     * @param out_max Estremo superiore del range in output.
     * @return Valore mappato dal range in input al range in output.
     */
    public static int map(int x, int in_min, int in_max, int out_min, int out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    /**
     * mappa un certo valore x da un range ad un altro, specificando
     * l'input range e l'output range desiderato.
     * @param x Valore da mappare.
     * @param in_min Estremo inferiore del range in input.
     * @param in_max Estremo superiore del range in input.
     * @param out_min Estremo inferiore del range in output.
     * @param out_max Estremo superiore del range in output.
     * @return Valore mappato dal range in input al range in output.
     */
    public static float map(float x, float in_min, float in_max, float out_min, float out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    /**
     * mappa un certo valore x da un range ad un altro, specificando
     * l'input range e l'output range desiderato.
     * @param x Valore da mappare.
     * @param in_min Estremo inferiore del range in input.
     * @param in_max Estremo superiore del range in input.
     * @param out_min Estremo inferiore del range in output.
     * @param out_max Estremo superiore del range in output.
     * @return Valore mappato dal range in input al range in output.
     */
    public static double map(double x, double in_min, double in_max, double out_min, double out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    /**
     * mappa un certo valore x da un range ad un altro, specificando
     * l'input range e l'output range desiderato.
     * @param x Valore da mappare.
     * @param in_min Estremo inferiore del range in input.
     * @param in_max Estremo superiore del range in input.
     * @param out_min Estremo inferiore del range in output.
     * @param out_max Estremo superiore del range in output.
     * @return Valore mappato dal range in input al range in output.
     */
    public static long map(long x, long in_min, long in_max, long out_min, long out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
}

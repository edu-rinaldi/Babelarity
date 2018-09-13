package it.uniroma1.lcl.babelarity;

import java.util.Arrays;

/**
 * {@code Enum} che definisce il Part of speech di un {@code Synset}.
 */
public enum PartOfSpeech
{


    NOUN('n'), ADV('r'), ADJ('a'), VERB('v');

    private char c;

    /**
     *
     * @param c Identifica il carattere da associare a ciascun POS.
     */
    PartOfSpeech(char c) { this.c  = c; }

    /**
     * Metodo che dato un carattere ritorna il POS corrispondente.
     * @param c Carattere da cui ricavare il POS.
     * @return {@code PartOfSpeech} corrispondente al carattere {@code c}.
     */
    public static PartOfSpeech getByChar(char c)
    {
        return Arrays.stream(PartOfSpeech.values()).filter(p->p.c == c).findFirst().orElse(NOUN);
    }
}

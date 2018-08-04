package it.uniroma1.lcl.babelarity;

import java.util.Arrays;

public enum PartOfSpeech
{

    NOUN('n'), ADV('r'), ADJ('a'), VERB('v');

    char c;

    PartOfSpeech(char c)
    {
        this.c  = c;
    }

    public static PartOfSpeech getByChar(char c)
    {
        return Arrays.stream(PartOfSpeech.values()).filter(p->p.c == c).findFirst().orElse(NOUN);
    }
}

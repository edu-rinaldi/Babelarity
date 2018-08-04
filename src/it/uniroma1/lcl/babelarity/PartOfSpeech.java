package it.uniroma1.lcl.babelarity;

import java.util.Arrays;

public enum PartOfSpeech
{

    NOUN('n'), ADV('r'), ADJ('a'), VERB('v');

    char s;
    PartOfSpeech(char s)
    {
        this.s  = s;
    }

    public static PartOfSpeech getByChar(char c)
    {
        return Arrays.stream(PartOfSpeech.values()).filter(p->p.s == c).findFirst().get();
    }
}

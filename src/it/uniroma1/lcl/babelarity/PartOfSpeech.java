package it.uniroma1.lcl.babelarity;

public enum PartOfSpeech
{

    NOUN('n'), ADV('r'), ADJ('a'), VERB('v');

    char s;
    PartOfSpeech(char s)
    {
        this.s  = s;
    }
}

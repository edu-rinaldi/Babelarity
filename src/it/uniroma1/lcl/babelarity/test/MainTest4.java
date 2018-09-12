package it.uniroma1.lcl.babelarity.test;

import it.uniroma1.lcl.babelarity.CorpusManager;
import it.uniroma1.lcl.babelarity.Document;

public class MainTest4
{
    public static void main(String[] args) {
        CorpusManager cm = CorpusManager.getInstance();
        Document d1 = cm.parseDocument("resources/documents/C_programming_language.txt");
        cm.saveDocument(d1);
        for(Document d: cm) System.out.println(d);
    }
}

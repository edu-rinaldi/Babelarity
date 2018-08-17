package it.uniroma1.lcl.babelarity;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main
{
    public static HashSet<String> getStopWords()
    {
        HashSet<String> stopwords = new HashSet<>();
        try(BufferedReader br = Files.newBufferedReader(Paths.get("resources/utils/english-stop-words-large.txt")))
        {
            while(br.ready())
                stopwords.add(br.readLine());
        }
        catch (IOException e){e.printStackTrace();}
        return stopwords;
    }

    public static List<List<String>> getFrasi(HashSet<String> sw)
    {
        List<List<String>> frasi = new ArrayList<>();
        try
        {
            for(String p : new File("resources/corpus/").list())
            {
                String txt = new String(Files.readAllBytes(Paths.get("resources/corpus/", p))).replaceAll("^[A-Za-z0-9]","");
                frasi.addAll(Arrays.stream(txt.split("[.;?!:]"))
                        .map(s-> Arrays.stream(s.trim().split(" "))
                                .filter(w->!sw.contains(w) && Word.fromString(w).getLemmas().size()>0)
                                .map(w->Word.fromString(w).getLemmas().get(0))
                                .collect(Collectors.toList())
                        ).collect(Collectors.toList()));
            }
        }
        catch (IOException e){e.printStackTrace();}
        return frasi;
    }



    public static void main(String[] args) {
        MiniBabelNet b = MiniBabelNet.getInstance();
//        CorpusManager c = CorpusManager.getInstance();

        HashSet<String> stopwords = getStopWords();
        List<List<String>> frasi = getFrasi(stopwords);

        LinkedHashMap<String,Integer> indexMap = new LinkedHashMap<>();
        int id = 0;
        for(List<String> frase: frasi)
            for (int i = 0; i < frase.size(); i++)
                if(!indexMap.containsKey(frase.get(i)))
                    indexMap.put(frase.get(i), id++);

        short[][] mat = new short[id+1][id+1];
        for(List<String> frase: frasi)
            for(String w: frase)
                for(String w2: frase)
                    mat[indexMap.get(w2)][indexMap.get(w)]+=1;

        /*String[] v = indexMap.entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
                .toArray(new String[indexMap.size()]);*/

        float[][] mat2 = new float[mat.length][mat.length];
        for(int i=0;i<mat.length;i++)
            for(int j=0;j<mat.length;j++)
                mat2[i][j] = mat[i][i]==0 || mat[j][j]==0 ? 0 : (float)(Math.log((float)mat[i][j]/(mat[i][i]*mat[j][j])));
        System.out.println(mat2[indexMap.get("hong")][indexMap.get("kong")]);
//        try(BufferedWriter bw = Files.newBufferedWriter(Paths.get("occorrenze.txt")))
//        {
//            for(String k : v)
//                for(String k2: v)
//                    bw.write(mat[indexMap.get(k2)][indexMap.get(k)]+"\n");
//        }catch (IOException e) {e.printStackTrace();}



        /*try(BufferedWriter bw = Files.newBufferedWriter(Paths.get("lemmss.txt")))
        {
            for(String k : indexMap.keySet().stream().sorted().collect(Collectors.toList()))
                bw.write(k+"\n");
        }catch (IOException e){e.printStackTrace();}*/

    }
}

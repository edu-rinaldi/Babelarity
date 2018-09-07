package it.uniroma1.lcl.babelarity.test;

import it.uniroma1.lcl.babelarity.BabelSynset;
import it.uniroma1.lcl.babelarity.MiniBabelNet;
import it.uniroma1.lcl.babelarity.PartOfSpeech;
import it.uniroma1.lcl.babelarity.Synset;

import java.util.*;

public class MainTest2
{
    public static List<BabelSynset> constructPath(BabelSynset node, Map<BabelSynset,BabelSynset> meta)
    {
        ArrayList<BabelSynset> path = new ArrayList<>();
        path.add(0,node);

        while (meta.get(node)!=null)
        {
            node = meta.get(node);
            path.add(0,node);
        }
        return path;
    }

    public static int dijkstra(BabelSynset root, BabelSynset end, BabelSynset bb)
    {
        LinkedList<BabelSynset> openSet = new LinkedList<>();
        Set<BabelSynset> closedSet = new HashSet<>();
        HashMap<BabelSynset,BabelSynset> meta  = new HashMap<>();

        //settings iniziali
        meta.put(root, null);
        root.setDist(0);
        openSet.add(root);

        while(!openSet.isEmpty())
        {
            BabelSynset u = openSet.stream().min(Comparator.comparing(BabelSynset::getDist)).get();

            openSet.remove(u);
            if(closedSet.contains(u)) continue;
            closedSet.add(u);

            if(u.getID().equals(end.getID()))
            {
                int val = u.getDist();
                closedSet.forEach(b->b.setDist(Integer.MAX_VALUE));

                return val;
            }

            for(BabelSynset v:u.getRelations("has-kind"))
            {
                if(v.getID().equals(bb.getID())) continue;
                int alt = u.getDist()+1;
                if(alt<v.getDist())
                {
                    meta.put(v,u);
                    openSet.add(v);
                    v.setDist(alt);
                }
            }
        }
        closedSet.forEach(b->b.setDist(Integer.MAX_VALUE));
        return -1;
    }

    public static int lcs(BabelSynset s1, BabelSynset s2)
    {
        if(s1.equals(s2)) return 1;
        return risali(s1,s2, 1, Integer.MAX_VALUE, new HashSet<>());
    }

    public static int risali(BabelSynset s1, BabelSynset s2, int curDist,int minDist, HashSet<BabelSynset> visited)
    {
        if (s1.getRelations("is-a").isEmpty()) return minDist;
        for(BabelSynset s: s1.getRelations("is-a"))
        {
            if(visited.contains(s)) continue;
            visited.add(s);
            int dist = dijkstra(s,s2,s1);
            if(dist> -1 && curDist+dist<minDist) minDist = curDist+dist;
            minDist = risali(s, s2, curDist+1, minDist, visited);
        }
        return minDist;
    }


    public static double map(double x, double in_min, double in_max, double out_min, double out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    public static HashSet<BabelSynset> getRoots(MiniBabelNet m)
    {
        HashSet<BabelSynset> roots = new HashSet<>();
        for(Synset b : m)
            if(b.getRelations("is-a").isEmpty() && !b.getRelations("has-kind").isEmpty()) roots.add((BabelSynset)b);
        return roots;
    }

    public static int maxDepth(BabelSynset root)
    {
        int max = Integer.MIN_VALUE;
        Set<BabelSynset> closedSet = new HashSet<>();
        LinkedList<BabelSynset> openSet = new LinkedList<>();
        root.setDist(0);
        openSet.add(root);
        while (!openSet.isEmpty())
        {
            BabelSynset u = openSet.pop();
            if(closedSet.contains(u)) continue;
            closedSet.add(u);
            for(BabelSynset v : u.getRelations("has-kind"))
            {
                v.setDist(u.getDist()+1);
                openSet.add(v);
                if(max<v.getDist()) max = v.getDist();
            }
        }
        closedSet.forEach(b->b.setDist(Integer.MAX_VALUE));
        return max;
    }

    public static int maxDepth(BabelSynset root, int curDepth, int maxDepth)
    {
        if(root.getRelations("has-kind").isEmpty()) return maxDepth;
        for(BabelSynset b: root.getRelations("has-kind"))
        {
            if(curDepth+1 > maxDepth) maxDepth = curDepth+1;
            maxDepth = maxDepth(b, curDepth+1, maxDepth);
        }
        return maxDepth;
    }


    public static void main(String[] args) {
        MiniBabelNet b = MiniBabelNet.getInstance();


        BabelSynset s1 = b.getSynset("bn:00034472n");
        BabelSynset s2 = b.getSynset("bn:00015008n");
        BabelSynset s3 = b.getSynset("bn:00081546n");
        BabelSynset s4 = b.getSynset("bn:00070528n");

        BabelSynset s5 = b.getSynset("bn:00024712n");
        BabelSynset s6 = b.getSynset("bn:00029345n");
        BabelSynset s7 = b.getSynset("bn:00035023n");
        BabelSynset s8 = b.getSynset("bn:00010605n");

        System.out.println(s1+"\t"+s3);
        System.out.println("Inizio lcs");

        //calcolo lcs
        double lcs = lcs(s1, s3);
        System.out.println("LCS:\t"+lcs);



        //prendo tutte le radici
        HashSet<BabelSynset> roots = getRoots(b);


        //in depths metto per ogni pos la profondita' massima
        Map<PartOfSpeech, Integer> depths = new HashMap<>();
        for(BabelSynset root: roots)
            depths.merge(root.getPOS(), maxDepth(root), Math::max);

        //somma maxdepth per ogni pos
        double d = depths.entrySet().stream().map(Map.Entry::getValue).reduce((v1, v2)->v1+v2).get();
        System.out.println(d+"\t"+depths.size());

        //calcolo profondita' media
        double depth = d/depths.size();
        System.out.println(depth);
        double result = -Math.log(lcs/(2*depth));
        //max val: 4.007333185232471
        //min val: -0.301
        System.out.println(map(result, -0.301, 4.007333185232471, 0, 1));
    }



}
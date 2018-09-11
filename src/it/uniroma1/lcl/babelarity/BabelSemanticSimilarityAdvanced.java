package it.uniroma1.lcl.babelarity;

import it.uniroma1.lcl.babelarity.exception.NotASynsetException;
import it.uniroma1.lcl.babelarity.utils.RangeMapper;

import java.util.*;
import java.util.stream.Collectors;

public class BabelSemanticSimilarityAdvanced implements BabelSemanticSimilarity
{
    private MiniBabelNet miniBabelNet;
    private Set<Synset> roots;
    private Map<PartOfSpeech, Integer> maxDepthPos;
    private double averageDepth;
    private double lowRange;
    private double highRange;

    public BabelSemanticSimilarityAdvanced()
    {
        this.miniBabelNet = MiniBabelNet.getInstance();
        roots = miniBabelNet.getRoots();
        maxDepthPos = getMaxDepthPos();

        averageDepth = (double)maxDepthPos.values().stream().reduce(Integer::sum).orElse(0)/(maxDepthPos.size()+1);

        double maxAbsoluteDepth = maxDepthPos.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        lowRange = lch(maxAbsoluteDepth, averageDepth, 2);
        highRange = lch(1, averageDepth);
    }

    private Map<PartOfSpeech, Integer> getMaxDepthPos()
    {
        if(maxDepthPos!=null) return maxDepthPos;
        return roots.stream()
                .collect(Collectors.toMap(Synset::getPOS,b->miniBabelNet.maxDepth(b,"has-kind2"),Math::max));
    }

    private int dijkstra(Synset root, Synset end, Synset filterNode, Set<Synset> closed)
    {
        //Queue of node that must be visited
        LinkedList<Synset> openSet = new LinkedList<>();
        //Distances map
        HashMap<Synset, Integer> distanceMap = new HashMap<>();

        //set distance from root
        distanceMap.put(root, 0);

        //add to openSet the root
        openSet.add(root);

        while (!openSet.isEmpty())
        {
            //get from the openSet the node with lower distance
            Synset current = openSet.stream().min(Comparator.comparing(distanceMap::get)).get();

            //remove from openSet the current node and add it to closed
            openSet.remove(current);
            closed.add(current);

            //if we found the end node
            if(current.equals(end))
                return distanceMap.get(current);

            //go down in the tree using has-kind relations
            for(Synset child :current.getRelations("has-kind2"))
            {
                //if the child is equals to the node we started in riseUp, continue to avoid loops
                if(child.equals(filterNode)) continue;

                //childs distance
                int newDist = distanceMap.get(current)+1;
                //check if child has been already visited
                if(newDist<distanceMap.getOrDefault(child, Integer.MAX_VALUE) && !closed.contains(child))
                {
                    openSet.add(child);
                    distanceMap.put(child, newDist);
                }
            }
        }
        return -1;
    }

    private int riseUpTree(Synset s1, Synset s2, int curDist,int minDist, Set<Synset> visited, Set<Synset> closed)
    {
        if (s1.getRelations("is-a").isEmpty()) return minDist;
        for(Synset s: s1.getRelations("is-a"))
        {
            if(visited.contains(s)) continue;
            visited.add(s);
            int dist = dijkstra(s,s2,s1, closed);
            if(dist> -1 && curDist+dist<minDist) minDist = curDist+dist;
            minDist = riseUpTree(s, s2, curDist+1, minDist, visited, closed);
        }
        return minDist;
    }

    private int lcsLength(Synset s1, Synset s2)
    {
        if(s1.equals(s2)) return 1;
        return riseUpTree(s1,s2, 1, Integer.MAX_VALUE, new HashSet<>(), new HashSet<>());
    }

    private double lch(double lcs, double averageDepth){return -Math.log(lcs/(2*averageDepth)); }
    private double lch(double lcs, double averageDepth, int lcsPower){ return -Math.log(Math.pow(lcs, lcsPower)/(2*averageDepth)); }

    @Override
    public double compute(LinguisticObject s1, LinguisticObject s2) throws NotASynsetException
    {
        if(!(s1 instanceof Synset) || !(s2 instanceof Synset)) throw new NotASynsetException();

        Synset sy1 = (Synset) s1;
        Synset sy2 = (Synset) s2;

        double lcs = lcsLength(sy1, sy2);
        if(lcs == Integer.MAX_VALUE)
        {
            double result = 1.0/(miniBabelNet.distance(sy1,sy2)+1);
            return Double.isInfinite(result) ? 0 : result;
        }
        return RangeMapper.map(lch(lcs, averageDepth, 2), lowRange, highRange, 0, 1);
    }


}

package it.uniroma1.lcl.babelarity;

import it.uniroma1.lcl.babelarity.exception.NotABabelSynsetException;

import java.util.*;
import java.util.stream.Collectors;

public class BabelSemanticSimilarityAdvanced implements BabelSemanticSimilarity
{
    private MiniBabelNet miniBabelNet;
    private Set<BabelSynset> roots;
    private Map<PartOfSpeech, Integer> maxDepthPos;
    private double averageDepth;
    private double lowRange;
    private double highRange;

    public BabelSemanticSimilarityAdvanced(MiniBabelNet miniBabelNet)
    {
        this.miniBabelNet = miniBabelNet;
        roots = miniBabelNet.getRoots();
        maxDepthPos = getMaxDepthPos();
        averageDepth = (double)maxDepthPos.values().stream().reduce(Integer::sum).orElse(0)/(maxDepthPos.size()+1);

        double maxAbsoluteDepth = Math.pow(maxDepthPos.values().stream().mapToInt(Integer::intValue).max().orElse(0),2);
        lowRange = -Math.log(maxAbsoluteDepth /(2*averageDepth));
        highRange = -Math.log(1.0/(2*averageDepth));
    }

    public double map(double x, double out_min, double out_max)
    {
        return (x - lowRange) * (out_max - out_min) / (highRange - lowRange) + out_min;
    }

    public Map<PartOfSpeech, Integer> getMaxDepthPos()
    {
        if(maxDepthPos!=null) return maxDepthPos;
        return roots.stream()
                .collect(Collectors.toMap(BabelSynset::getPOS,b->miniBabelNet.maxDepth(b,"has-kind2"),Math::max));
    }

    private int dijkstra(BabelSynset root, BabelSynset end, BabelSynset filterNode, Set<BabelSynset> closed)
    {
        //Queue of node that must be visited
        LinkedList<BabelSynset> openSet = new LinkedList<>();
        //Set of nodes that have been already visited
        //Set<BabelSynset> closedSet = new HashSet<>();

        //set distance from root
        root.setDist(0);

        //add to openSet the root
        openSet.add(root);

        while (!openSet.isEmpty())
        {
            //get from the openSet the node with lower distance
            BabelSynset current = openSet.stream().min(Comparator.comparing(BabelSynset::getDist)).get();

            //remove from openSet the current node and add it to closed
            openSet.remove(current);
            closed.add(current);

            //if we found the end node
            if(current.equals(end))
            {
                //get current distance from root
                int dist = current.getDist();
                //reset current distance from root
                closed.forEach(b->b.setDist(Integer.MAX_VALUE));
                return dist;
            }

            //go down in the tree using has-kind relations
            for(BabelSynset child :current.getRelations("has-kind2"))
            {
                //if the child is equals to the node we started in riseUp, continue to avoid loops
                if(child.equals(filterNode)) continue;

                //childs distance
                int newDist = current.getDist()+1;

                //check if child has been already visited
                if(newDist<child.getDist() && !closed.contains(child))
                {
                    openSet.add(child);
                    child.setDist(newDist);
                }
            }
        }
        //reset distance
        closed.forEach(b->b.setDist(Integer.MAX_VALUE));
        return -1;
    }

    private int riseUpTree(BabelSynset s1, BabelSynset s2, int curDist,int minDist, Set<BabelSynset> visited, Set<BabelSynset> closed)
    {
        if (s1.getRelations("is-a").isEmpty()) return minDist;
        for(BabelSynset s: s1.getRelations("is-a"))
        {
            if(visited.contains(s)) continue;
            visited.add(s);
            int dist = dijkstra(s,s2,s1, closed);
            if(dist> -1 && curDist+dist<minDist) minDist = curDist+dist;
            minDist = riseUpTree(s, s2, curDist+1, minDist, visited, closed);
        }
        return minDist;
    }

    private int lcsLength(BabelSynset s1, BabelSynset s2)
    {
        if(s1.equals(s2)) return 1;
        return riseUpTree(s1,s2, 1, Integer.MAX_VALUE, new HashSet<>(), new HashSet<>());
    }

    @Override
    public double compute(LinguisticObject s1, LinguisticObject s2) throws NotABabelSynsetException
    {
        if(!(s1 instanceof BabelSynset) || !(s2 instanceof BabelSynset))
            throw new NotABabelSynsetException();

        BabelSynset sy1 = (BabelSynset)s1;
        BabelSynset sy2 = (BabelSynset)s2;

        double lcs = lcsLength(sy1, sy2);
        if(lcs == Integer.MAX_VALUE)
        {
            double result = 1.0/(miniBabelNet.distance(sy1,sy2)+1);
            return Double.isInfinite(result) ? 0 : result;
        }
        return map(-Math.log(Math.pow(lcs,2)/(2*averageDepth)), 0, 1);
    }


}

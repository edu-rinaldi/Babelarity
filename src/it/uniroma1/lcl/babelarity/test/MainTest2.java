package it.uniroma1.lcl.babelarity.test;

import it.uniroma1.lcl.babelarity.BabelSynset;
import it.uniroma1.lcl.babelarity.MiniBabelNet;
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

    public static List<BabelSynset> dijkstra(BabelSynset root, BabelSynset end)
    {
        LinkedList<BabelSynset> openSet = new LinkedList<>();
        Set<BabelSynset> closedSet = new HashSet<>();
        Map<BabelSynset,BabelSynset> meta = new HashMap<>();
        meta.put(root,null);
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
                closedSet.forEach(b->b.setDist(Integer.MAX_VALUE));
                return constructPath(u, meta);
            }

            for(BabelSynset v:u.getNeighbours())
            {
                int alt = u.getDist()+1;
                if(alt<v.getDist())
                {
                    meta.put(v,u);
                    openSet.add(v);
                    v.setDist(alt);
                }
            }
        }
        root.setDist(Integer.MAX_VALUE);
        return new ArrayList<>();
    }

    public static int maxDepth(BabelSynset root)
    {
        int max = Integer.MIN_VALUE;
        int count = 1;
        Set<BabelSynset> closedSet = new HashSet<>();
        LinkedList<BabelSynset> openSet = new LinkedList<>();
        root.setDist(0);
        openSet.add(root);
        while (!openSet.isEmpty())
        {
            BabelSynset u = openSet.pop();
            if(closedSet.contains(u)) continue;
            closedSet.add(u);
            count++;
            for(BabelSynset v : u.getNeighbours())
            {
                v.setDist(u.getDist()+1);
                openSet.add(v);
                if(max<v.getDist()) max = v.getDist();
            }
            u.setDist(Integer.MAX_VALUE);
        }
        System.out.println(count);
        return max;
    }

    public static float map(float x, float in_min, float in_max, float out_min, float out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    public static List<BabelSynset> bfs(BabelSynset root, BabelSynset end)
    {
        //a FIFO openSet
        LinkedList<BabelSynset> openSet = new LinkedList<>();

        //an empty set to maintain visited nodes
        Set<BabelSynset> closedSet = new HashSet<>();

        // a dictionary to maintain meta information (used for path formation)
        // key -> (parent state, action to reach child)
        HashMap<BabelSynset,BabelSynset> meta = new HashMap<>();

        //initialize
        meta.put(root,null);
        openSet.add(root);

        // For each node on the current level expand and process, if no children
        // (leaf) then unwind
        while(!(openSet.size()==0))
        {
            BabelSynset newRoot = openSet.poll();

            // We found the node we wanted so stop and emit a path.
            if(newRoot.getID().equals(end.getID()))
                return constructPath(newRoot,meta);

            // For each child of the current tree process
            for(BabelSynset child : newRoot.getNeighbours())
            {
                //The node has already been processed
                if(closedSet.contains(child))
                    continue;

                // The child is not enqueued to be processed, so enqueue this level of
                // children to be expanded
                if(!openSet.contains(child))
                {
                    meta.put(child,newRoot);
                    openSet.add(child);
                }
            }
            //We finished processing the root of this subtree, so add it to the closed set
            closedSet.add(newRoot);
        }
        return new ArrayList<>();
    }

    public static boolean rec(BabelSynset current, String id, Set<BabelSynset> visited) {
        visited.add(current);
        if(current.getID().equals(id)) {
            System.out.println(current);
            return true;
        }

        for(BabelSynset s : current.getNeighbours())
            if(!visited.contains(s) && rec(s, id, visited)) return true;

        return false;
    }

    public static void main(String[] args) {
        MiniBabelNet b = MiniBabelNet.getInstance();

        BabelSynset entity = b.getSynsets("Entity").get(0);

        BabelSynset start = b.getSynset("bn:00034472n");
        BabelSynset dest = b.getSynset("bn:00015008n");
        List<BabelSynset> l1 = dijkstra(entity, start);
        List<BabelSynset> l2 = dijkstra(entity, dest);
        System.out.println(l1);
        System.out.println(l2);


    }



}

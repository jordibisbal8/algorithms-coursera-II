import edu.princeton.cs.algs4.BreadthFirstDirectedPaths;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class SAP {

    private final Digraph graph;
    private BreadthFirstDirectedPaths bfsv;
    private BreadthFirstDirectedPaths bfsw;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        if (G == null)
            throw new IllegalArgumentException();
        graph = new Digraph(G);
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        return findShortest(v, w)[0];

    }

    private int[] findShortest(int v, int w) {
        if (v < 0 || v > graph.V() || w < 0 || w > graph.V())
            throw new IllegalArgumentException();
        int[] result = new int[2];
        if (v == w) {
            result[0] = 0;
            result[1] = w;
            return result;
        };
        bfsv = new BreadthFirstDirectedPaths(graph, v);
        bfsw = new BreadthFirstDirectedPaths(graph, w);

        int shortestLength = Integer.MAX_VALUE;
        int shortestAncestor = -1;


        for (int i = 0; i < graph.V(); ++i) {
            if (bfsv.hasPathTo(i) && bfsw.hasPathTo(i)) {
                int len = bfsv.distTo(i) + bfsw.distTo(i);
                if (len < shortestLength) {
                    shortestLength = len;
                    shortestAncestor = i;
                }
            }
        }

        if (shortestAncestor == -1) {
            result[0] = -1;
            result[1] = -1;
        }
        else {
            result[0] = shortestLength;
            result[1] = shortestAncestor;
        }
        return result;
    }

        // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        return findShortest(v, w)[1];
    }


    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        isNull(v, w);
        int shortest = -1;
        for (int x: v) {
            for (int y: w) {
                int current = length(x, y);
                if (current == -1) return -1;
                if (shortest == -1) shortest = current;
                else if (current < shortest) {
                    shortest = current;
                }
            }
        }
        return shortest;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        isNull(v, w);
        int shortest = -1;
        int minx = 0;
        int miny = 0;
        for (int x: v) {
            for (int y: w) {
                int current = length(x, y);
                if (current == -1) return -1;
                if (shortest == -1) shortest = current;
                else if (current < shortest) {
                    shortest = current;
                    minx = x;
                    miny = y;
                }
            }
        }
        return ancestor(minx, miny);
    }

    private void isNull(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) {
            throw new NullPointerException("argument is null");
        }
        for (int x: v) {
            if (x < 0 || x >= graph.V()) {
                throw new IndexOutOfBoundsException("vertex " + v + " is not between 0 and " + (graph.V()-1));
            }
        }
        for (int x: w) {
            if (x < 0 || x >= graph.V()) {
                throw new IndexOutOfBoundsException("vertex " + v + " is not between 0 and " + (graph.V()-1));
            }
        }
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        int ancestor = sap.ancestor(5, 0);
        int length = sap.length(5, 0);
        StdOut.println(ancestor);
        StdOut.println(length);
    }

}
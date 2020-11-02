import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DirectedCycle;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.HashMap;


public class WordNet {

    private final HashMap<Integer, String> idToSynset;
    private final HashMap<String, Bag<Integer>> wordToIds;
    private final SAP sap;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null)
            throw new IllegalArgumentException();
        idToSynset = new HashMap<Integer, String>();
        wordToIds = new HashMap<String, Bag<Integer>>();
        readSynsets(synsets);
        sap = new SAP(readHypernyms(hypernyms));
    }

    private void readSynsets(String synsetsFile) {
        In in = new In(synsetsFile);
        while (in.hasNextLine()) {
            String[] words = in.readLine().split(",");
            int id = Integer.parseInt(words[0]);
            idToSynset.put(id, words[1]);
            for (String noun : words[1].split(" ")) {
                Bag<Integer> bag = wordToIds.get(noun);
                if (bag == null) {
                    bag = new Bag<Integer>();
                    bag.add(id);
                    wordToIds.put(noun, bag);
                }
                else {
                    bag.add(id);
                }
            }
        }
    }

    private Digraph readHypernyms(String hypernymsFile) {
        In in = new In(hypernymsFile);
        Digraph digraph = new Digraph(idToSynset.size());
        while (in.hasNextLine()) {
            String[] parts = in.readLine().split(",");
            int v = Integer.parseInt(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                int w = Integer.parseInt(parts[i]);
                digraph.addEdge(v, w);
            }
        }
        checkIfRootedDAG(digraph);
        return digraph;
    }

    private void checkIfRootedDAG(Digraph graph) {
        // Check it is acyclic
        DirectedCycle dc = new DirectedCycle(graph);
        if (dc.hasCycle()) throw new IllegalArgumentException();
        // check if it is rooted
        int counter = 0;
        for (int i = 0; i < graph.V(); i++) {
            if (graph.outdegree(i) == 0) counter++;
            if (counter >= 2) throw new IllegalArgumentException();
        }
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return wordToIds.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null)
            throw new IllegalArgumentException();
        return wordToIds.containsKey(word);
    }

    // distance between nounA and nounB (defined below) O(n)
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new IllegalArgumentException();
        return sap.length(wordToIds.get(nounA), wordToIds.get(nounB));
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below) O(n)
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new IllegalArgumentException();
        return idToSynset.get(sap.ancestor(wordToIds.get(nounA), wordToIds.get(nounB)));
    }

    // do unit testing of this class
    public static void main(String[] args) {
        WordNet wn = new WordNet(args[0], args[1]);
        final Iterable<String> nouns = wn.nouns();
        StdOut.printf(String.valueOf(nouns));
    }
}
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.SET;
import edu.princeton.cs.algs4.StdOut;

public class BoggleSolver {

    private final CustomTrie trie;
    private SET<String> set;

    // Initializes the data structure using the given array of strings as the dictionary.
    // (You can assume each word in the dictionary contains only the uppercase letters A through Z.)
    public BoggleSolver(String[] dictionary) {
        trie = new CustomTrie();
        for (String word: dictionary) {
            trie.add(word);
        }
    }

    // Returns the set of all valid words in the given Boggle board, as an Iterable.
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        if (board.rows() == 0 && board.cols() == 0) throw new IllegalArgumentException();
        set = new SET<>();
        for (int row = 0; row < board.rows(); row++) {
            for (int col = 0; col < board.cols(); col++) {
                dfs(board, row, col);
            }
        }
        return set;
    }

    private boolean isSafe(int i, int j, boolean[][] visited) {
        return (i >= 0 && i < visited.length && j >= 0 && j < visited[0].length && !visited[i][j]);
    }

    private void dfs(BoggleBoard board, int row, int col) {
        boolean[][] visited = new boolean[board.rows()][board.cols()];
        dfs(board, row, col, nextLetter(board, row, col), visited);
    }

    private void dfs(BoggleBoard board, int i, int j, String prefix, boolean[][] visited) {
        visited[i][j] = true;
        if (trie.containsPrefix(prefix)) {
            if (isSafe(i - 1, j - 1, visited)) {
                dfs(board, i - 1, j - 1, prefix + nextLetter(board, i - 1, j - 1), visited);
            }
            if (isSafe(i - 1, j, visited)) {
                dfs(board, i - 1, j, prefix + nextLetter(board, i - 1, j), visited);
            }
            if (isSafe(i - 1, j + 1, visited)) {
                dfs(board, i - 1, j + 1, prefix + nextLetter(board, i - 1, j + 1), visited);
            }
            if (isSafe(i, j - 1, visited)) {
                dfs(board, i, j - 1, prefix + nextLetter(board, i, j - 1), visited);
            }
            if (isSafe(i, j + 1, visited)) {
                dfs(board, i, j + 1, prefix + nextLetter(board, i, j + 1), visited);
            }
            if (isSafe(i + 1, j - 1, visited)) {
                dfs(board, i + 1, j - 1, prefix + nextLetter(board, i + 1, j - 1), visited);
            }
            if (isSafe(i + 1, j, visited)) {
                dfs(board, i + 1, j, prefix + nextLetter(board, i + 1, j), visited);
            }
            if (isSafe(i + 1, j + 1, visited)) {
                dfs(board, i + 1, j + 1, prefix + nextLetter(board, i + 1, j + 1), visited);
            }
        }
        visited[i][j] = false;
        if (trie.contains(prefix) && prefix.length() >= 3 && !set.contains(prefix))
            set.add(prefix);
    }

    private String nextLetter(BoggleBoard board, int i, int j) {
        String nextLetter = String.valueOf(board.getLetter(i, j));
        if (nextLetter.equals("Q")) {
            nextLetter += "U";
        }
        return nextLetter;
    }

    // Returns the score of the given word if it is in the dictionary, zero otherwise.
    // (You can assume the word contains only the uppercase letters A through Z.)
    public int scoreOf(String word) {
        if (word == null) throw new NullPointerException();
        if (trie.contains(word)) return computeScore(word);
        else return 0;
    }

    private int computeScore(String word) {
        if (word.length() == 3 || word.length() == 4) return 1;
        else if (word.length() == 5) return 2;
        else if (word.length() == 6) return 3;
        else if (word.length() == 7) return 5;
        else if (word.length() >= 8) return 11;
        else return 0;
    }

    public static void main(String[] args) {
        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);
        BoggleBoard board = new BoggleBoard(args[1]);
        int score = 0;
        for (String word : solver.getAllValidWords(board)) {
            StdOut.println(word);
            score += solver.scoreOf(word);
        }
        StdOut.println("Score = " + score);
    }
    private static class CustomTrie {
        private static final int R = 26;
        private static final char OFFSET = 'A';

        private Node root;      // root of trie

        // R-way trie node
        private static class Node {
            private Node[] next = new Node[R];
            private boolean isString;
            private Node() {
                for (int i = 0; i < R; i++)
                    next[i] = null;
            }
        }

        private CustomTrie() {
        }

        public void add(String key) {
            if (key == null) throw new IllegalArgumentException("argument to add() is null");
            root = add(root, key, 0);
        }

        private Node add(Node x, String key, int d) {
            if (x == null) x = new Node();
            if (d == key.length()) {
                x.isString = true;
            }
            else {
                int c = key.charAt(d) - OFFSET;
                x.next[c] = add(x.next[c], key, d+1);
            }
            return x;
        }

        public boolean contains(String key) {
            if (key == null) throw new IllegalArgumentException("argument to contains() is null");
            Node x = get(root, key, 0);
            if (x == null) return false;
            return x.isString;
        }

        private Node get(Node x, String key, int d) {
            if (x == null) return null;
            if (d == key.length()) return x;
            int c = key.charAt(d) - OFFSET;
            return get(x.next[c], key, d+1);
        }

        private boolean containsPrefix(String key) {
            Node x = get(root, key, 0);
            return x != null;
        }
    }
}



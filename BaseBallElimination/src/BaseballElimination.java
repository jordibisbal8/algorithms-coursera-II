import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.SET;
import edu.princeton.cs.algs4.StdOut;


import java.util.HashMap;

public class BaseballElimination {
    private final int numberOfTeams;
    private final HashMap<String, Integer> map;
    private final HashMap<Integer, String> mapReverse;
    private final int[] w;
    private final int[] l;
    private final int[] r;
    private final int[][] g;
    private FlowNetwork[] results;
    private FordFulkerson[] resultFF;


    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        In inputFile = new In(filename);
        numberOfTeams = inputFile.readInt();
        w = new int[numberOfTeams];
        l = new int[numberOfTeams];
        r = new int[numberOfTeams];
        g = new int[numberOfTeams][numberOfTeams];
        map = new HashMap<>(numberOfTeams);
        mapReverse = new HashMap<>(numberOfTeams);
        results = new FlowNetwork[numberOfTeams];
        resultFF = new FordFulkerson[numberOfTeams];
        int i = 0;
        while (i < numberOfTeams) {
            String team = inputFile.readString();
            map.put(team, i);
            mapReverse.put(i, team);
            w[i] = inputFile.readInt();
            l[i] = inputFile.readInt();
            r[i] = inputFile.readInt();
            for (int j = 0; j < numberOfTeams; j++) {
                g[i][j] = inputFile.readInt();
            }
            i++;
        }
    }
    public int numberOfTeams() {
        return numberOfTeams;
    }
    // all teams
    public Iterable<String> teams() {
        return map.keySet();
    }
    // number of wins for given team
    public int wins(String team) {
        if (team == null || map.get(team) == null) throw new IllegalArgumentException();
        return w[map.get(team)];
    }
    // number of losses for given team
    public int losses(String team) {
        if (team == null || map.get(team) == null) throw new IllegalArgumentException();
        return l[map.get(team)];
    }

    // number of remaining games for given team
    public int remaining(String team) {
        if (team == null || map.get(team) == null) throw new IllegalArgumentException();
        return r[map.get(team)];
    }
    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        if (team1 == null || team2 == null || map.get(team1) == null || map.get(team2) == null) throw new IllegalArgumentException();
        return g[map.get(team1)][map.get(team2)];
    }
    // is given team eliminated?
    public boolean isEliminated(String team) {
        if (team == null || map.get(team) == null) throw new IllegalArgumentException();
        int x = map.get(team);
        // Trivial elimination
        for (int i = 0; i < numberOfTeams; i++) {
            if (w[x] + r[x] < w[i])
                return true;
        }
        // We store the already computed maxFlows in results
        if (results[x] == null) {
            maxFlowAlgorithm(x);
        }
        // If some edges pointing from s are not full, then there is no scenario in which team x can win the division
        for (FlowEdge e : results[x].adj(0)) {
            if (e.flow() != e.capacity())
                return true;
        }
        return false;
    }

    private void maxFlowAlgorithm(int x) {
        int matches = numberOfTeams * (numberOfTeams - 1) / 2;
        int v = 2 + matches + numberOfTeams;
        FlowNetwork fn = new FlowNetwork(v);
        int t = v - 1;
        int s = 0;
        int nodeID = 1;
        for (int i = 0; i < numberOfTeams; i++) {
            if (i != x) {
                for (int j = i + 1; j < numberOfTeams; j++) {
                    if (i != j && j != x) {
                        fn.addEdge(new FlowEdge(s, nodeID, g[i][j]));
                        fn.addEdge(new FlowEdge(nodeID, matches + i + 1, Integer.MAX_VALUE));
                        fn.addEdge(new FlowEdge(nodeID, matches + j + 1, Integer.MAX_VALUE));
                        nodeID++;
                    }
                }
                fn.addEdge(new FlowEdge(matches + i + 1, t, w[x] + r[x] - w[i]));
            }
        }
        resultFF[x] = new FordFulkerson(fn, s, t);
        results[x] = fn;
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        if (team == null || map.get(team) == null) throw new IllegalArgumentException();
        if (!isEliminated(team))
            return null;
        SET<String> cert = new SET<>();
        int x = map.get(team);
        FordFulkerson fn = resultFF[x];
        for (int i = 0; i < numberOfTeams; i++) {
            if (i == x) continue;
            if (w[x] + r[x] < w[i])
                cert.add(mapReverse.get(i));
        }
        if (cert.size() != 0) return cert;
        for (int i = 0; i < numberOfTeams; i++) {
            if (i == x) continue;
            if (fn.inCut(1 + numberOfTeams * (numberOfTeams - 1) / 2 + i)) {
                cert.add(mapReverse.get(i));
            }
        }
        return cert;
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}

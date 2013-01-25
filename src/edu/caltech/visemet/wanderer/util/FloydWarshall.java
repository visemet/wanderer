package edu.caltech.visemet.wanderer.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Max Hirschhorn #visemet
 */
public class FloydWarshall {

    public static final Logger LOGGER =
            LoggerFactory.getLogger(FloydWarshall.class);

    private static final String PATTERN = "^[\\d]{2}:[\\d]{2}:[\\d]{2}\\.[\\d]{3} \\[main\\] INFO  e.c.v.w.centipedes.CountCentipede - (?<uri>[^ ]+) \\[(?<outgoing>[^]]*)\\]$";

    private FloydWarshall() { }

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args)
            throws FileNotFoundException, IOException {

        final String filename = args[0];

        final Pattern pattern =  Pattern.compile(PATTERN);
        final Matcher matcher = pattern.matcher("");

        final Graph graph = new Graph();

        try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
            String line;

            while ((line = in.readLine()) != null) {
                String string = line.trim();

                matcher.reset(string);

                while (!matcher.matches()) {
                    string += in.readLine().trim();
                    matcher.reset(string);
                }

                final Node node = new Node(matcher.group("uri"));
                node.getOutgoing().addAll(new HashSet<>(Arrays.<String>asList(matcher.group("outgoing").split(", "))));

                graph.getNodes().put(node.getURI(), node);
            }
        }

        graph.initialize();
        graph.bidirect();

        final double avg = graph.avgDiameter();
        final int max = graph.maxDiameter();

        LOGGER.info("average diameter {}", avg);
        LOGGER.info("maximal diameter {}", max);
    }

    public static class Graph {
        private final Map<String, Node> nodes;

        public Graph() {
            this.nodes = new HashMap<>();
        }

        public Map<String, Node> getNodes() {
            return this.nodes;
        }

        public void initialize() {
            for (final String key : new HashSet<>(this.nodes.keySet())) {
                final Node node = this.nodes.get(key);

                for (final String out : node.outgoing) {
                    if (!this.nodes.containsKey(out)) {
                        this.nodes.put(out, new Node(out));
                    }

                    this.nodes.get(out).incoming.add(key);
                }
            }
        }

        public void bidirect() {
            for (final String key : this.nodes.keySet()) {
                final Node node = this.nodes.get(key);

                node.incoming.addAll(node.outgoing);
                node.outgoing.addAll(node.incoming);
            }
        }

        public double avgDiameter() {
            int total = 0;

            int[][] dist = floydWarshall();
            // int[][] dist = blockedFloydWarshall();

            final int size = this.nodes.size();
            for (int i = 0; i < size; i++) {
              for (int j = 0; j < size; j++) {
                  if (i != j) {
                      total += dist[i][j];
                  }
              }
            }

            final int num = size * size - size;

            if (num == 0) {
              return 0.0;
            }

            return (double) total / (double) num;
        }

        public int maxDiameter() {
            int max = 0;

            int[][] dist = floydWarshall();
            // int[][] dist = blockedFloydWarshall();

            final int size = this.nodes.size();
            for (int i = 0; i < size; i++) {
              for (int j = 0; j < size; j++) {
                  if (i != j) {
                      max = Math.max(dist[i][j], max);
                  }
              }
            }

            return max;
        }

        private int[][] floydWarshall() {
            final int size = this.nodes.size();
            final int[][] dist = new int[size][size];

            for (int i = 0; i < dist.length; i++) {
                for (int j = 0; j < dist.length; j++) {
                    dist[i][j] = -1;
                }
            }

            final Map<String, Integer> indices = new HashMap<>();

            final List<String> keys = new ArrayList<>(this.nodes.keySet());
            Collections.sort(keys);

            for (int i = 0; i < size; i++) {
                indices.put(keys.get(i), i);
            }

            for (String key : keys) {
                final int v = indices.get(key);
                dist[v][v] = 0;
            }

            for (String key : keys) {
                final Node node = this.nodes.get(key);
                final int u = indices.get(key);

                for (String out : node.outgoing) {
                    if (indices.containsKey(out)) {
                        final int v = indices.get(out);
                        dist[u][v] = 1;
                    }
                }
            }

            for (int k = 0; k < size; k++) {
                LOGGER.debug("k {}", k);

                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        final int sum = dist[i][k] + dist[k][j];

                        if ((dist[i][k] != -1 && dist[k][j] != -1)
                                && (dist[i][j] == -1 || (sum < dist[i][j]))) {

                            dist[i][j] = sum;
                        }
                    }
                }
            }

            return dist;
        }

        private int[][] blockedFloydWarshall() {
            final int size = this.nodes.size();
            final int[][] dist = new int[size][size];

            for (int i = 0; i < dist.length; i++) {
                for (int j = 0; j < dist.length; j++) {
                    dist[i][j] = -1;
                }
            }

            final Map<String, Integer> indices = new HashMap<>();

            final List<String> keys = new ArrayList<>(this.nodes.keySet());
            Collections.sort(keys);

            for (int i = 0; i < size; i++) {
                indices.put(keys.get(i), i);
            }

            for (String key : keys) {
                final int v = indices.get(key);
                dist[v][v] = 0;
            }

            for (String key : keys) {
                final Node node = this.nodes.get(key);
                final int u = indices.get(key);

                for (String out : node.outgoing) {
                    if (indices.containsKey(out)) {
                        final int v = indices.get(out);
                        dist[u][v] = 1;
                    }
                }
            }

            blockedFloydWarshallHelper(0, 0, 0, 0, 0, 0, size, dist);

            return dist;
        }

        private void blockedFloydWarshallHelper(int row1, int col1, int row2, int col2, int row3, int col3, int N, int[][] dist) {
            if (N <= 64) {
                for (int k = 0; k < N; k++) {
                    final int k2 = k + col2;
                    final int k3 = k + row3;

                    for (int i = 0; i < N; i++) {
                        final int i1 = i + row1;
                        final int i2 = i + row2;

                        for (int j = 0; j < N; j++) {
                            final int j1 = j + col1;
                            final int j3 = j + col3;

                            final int sum = dist[i2][k2] + dist[k3][j3];

                            if ((dist[i2][k2] != -1 && dist[k3][j3] != -1)
                                    && (dist[i1][j1] == -1 || (sum < dist[i1][j1]))) {

                                dist[i1][j1] = sum;
                            }
                        }
                    }
                }
            } else {
                final int P = N / 2;

                blockedFloydWarshallHelper(row1,     col1,     row2,     col2,     row3,     col3,     P,     dist);
                blockedFloydWarshallHelper(row1,     col1 + P, row2,     col2,     row3,     col3 + P, N - P, dist);
                blockedFloydWarshallHelper(row1 + P, col1,     row2 + P, col2,     row3,     col3,     N - P, dist);
                blockedFloydWarshallHelper(row1 + P, col1 + P, row2 + P, col2,     row3,     col3 + P, N - P, dist);
                blockedFloydWarshallHelper(row1 + P, col1 + P, row2 + P, col2 + P, row3 + P, col3 + P, N - P, dist);
                blockedFloydWarshallHelper(row1 + P, col1,     row2 + P, col2 + P, row3 + P, col3,     N - P, dist);
                blockedFloydWarshallHelper(row1,     col1 + P, row2,     col2 + P, row3 + P, col3 + P, N - P, dist);
                blockedFloydWarshallHelper(row1,     col1,     row2,     col2 + P, row3 + P, col3,     N - P, dist);
            }
        }
    }

    public static class Node {

        private final String URI;

        private final Set<String> incoming;

        private final Set<String> outgoing;

        public Node(String URI) {
            this.URI = URI;
            this.incoming = new HashSet<>();
            this.outgoing = new HashSet<>();
        }

        public String getURI() {
            return this.URI;
        }

        public Set<String> getIncoming() {
            return this.incoming;
        }

        public Set<String> getOutgoing() {
            return this.outgoing;
        }
    }
}

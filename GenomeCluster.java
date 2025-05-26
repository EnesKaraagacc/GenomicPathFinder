import java.util.*;

public class GenomeCluster {
    public Map<String, Genome> genomeMap = new LinkedHashMap<>();

    public void addGenome(Genome genome) {
        genomeMap.put(genome.id, genome);
    }

    public boolean contains(String genomeId) {
        return genomeMap.containsKey(genomeId);
    }

    public Genome getMinEvolutionGenome() {
        Genome min = null;
        for (Genome g : genomeMap.values()) {
            if (min == null || g.evolutionFactor < min.evolutionFactor) {
                min = g;
            }
        }
        return min;
    }

    public int dijkstra(String startId, String endId) {
        if (!contains(startId) || !contains(endId)) return -1;

        Map<String, Integer> dist = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));
        Set<String> visited = new HashSet<>();

        for (String id : genomeMap.keySet()) {
            dist.put(id, Integer.MAX_VALUE);
        }
        dist.put(startId, 0);
        pq.add(startId);

        while (!pq.isEmpty()) {
            String current = pq.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            Genome genome = genomeMap.get(current);
            for (Genome.Link link : genome.links) {
                if (contains(link.target)) {
                    int newDist = dist.get(current) + link.adaptationFactor;
                    if (newDist < dist.get(link.target)) {
                        dist.put(link.target, newDist);
                        pq.add(link.target);
                    }
                }
            }
        }

        return dist.get(endId) == Integer.MAX_VALUE ? -1 : dist.get(endId);
    }
}

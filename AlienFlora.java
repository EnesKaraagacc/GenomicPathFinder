import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;

public class AlienFlora {
    private File xmlFile;
    private Map<String, Genome> allGenomes = new HashMap<>();
    private List<GenomeCluster> clusters = new ArrayList<>();

    public AlienFlora(File xmlFile) {
        this.xmlFile = xmlFile;
    }

    public void readGenomes() {
        System.out.println("##Start Reading Flora Genomes##");
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            NodeList genomeNodes = doc.getElementsByTagName("genome");

            // Build genome objects from XML
            for (int i = 0; i < genomeNodes.getLength(); i++) {
                Element genomeElem = (Element) genomeNodes.item(i);
                String id = genomeElem.getElementsByTagName("id").item(0).getTextContent();
                int evo = Integer.parseInt(genomeElem.getElementsByTagName("evolutionFactor").item(0).getTextContent());
                Genome genome = new Genome(id, evo);

                NodeList linkNodes = genomeElem.getElementsByTagName("link");
                for (int j = 0; j < linkNodes.getLength(); j++) {
                    Element linkElem = (Element) linkNodes.item(j);
                    String target = linkElem.getElementsByTagName("target").item(0).getTextContent();
                    int adapt = Integer.parseInt(linkElem.getElementsByTagName("adaptationFactor").item(0).getTextContent());
                    genome.addLink(target, adapt);
                }

                allGenomes.put(id, genome);
            }

            // Discover clusters using DFS, preserving XML order
            Set<String> visited = new HashSet<>();
            List<String> genomeOrder = new ArrayList<>();
            for (int i = 0; i < genomeNodes.getLength(); i++) {
                Element genomeElem = (Element) genomeNodes.item(i);
                String id = genomeElem.getElementsByTagName("id").item(0).getTextContent();
                genomeOrder.add(id);
            }

            for (String id : genomeOrder) {
                if (!visited.contains(id)) {
                    GenomeCluster cluster = new GenomeCluster();
                    dfs(id, visited, cluster);
                    clusters.add(cluster);
                }
            }

            // Print cluster information
            System.out.println("Number of Genome Clusters: " + clusters.size());
            System.out.print("For the Genomes: [");
            for (int i = 0; i < clusters.size(); i++) {
                System.out.print("[");
                List<String> ids = new ArrayList<>(clusters.get(i).genomeMap.keySet());
                System.out.print(String.join(", ", ids));
                System.out.print("]");
                if (i != clusters.size() - 1) System.out.print(", ");
            }
            System.out.println("]");
            System.out.println("##Reading Flora Genomes Completed##");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void evaluateEvolutions() {
        System.out.println("##Start Evaluating Possible Evolutions##");
        List<Double> results = new ArrayList<>();
        int certified = 0;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            NodeList pairNodes = doc.getElementsByTagName("pair");

            for (int i = 0; i < pairNodes.getLength(); i++) {
                Element pairElem = (Element) pairNodes.item(i);
                if (pairElem.getParentNode().getNodeName().equals("possibleEvolutionPairs")) {
                    String id1 = pairElem.getElementsByTagName("firstId").item(0).getTextContent();
                    String id2 = pairElem.getElementsByTagName("secondId").item(0).getTextContent();

                    GenomeCluster cluster1 = null, cluster2 = null;
                    for (GenomeCluster c : clusters) {
                        if (c.contains(id1)) cluster1 = c;
                        if (c.contains(id2)) cluster2 = c;
                    }

                    if (cluster1 == null || cluster2 == null || cluster1 == cluster2) {
                        results.add(-1.0);
                    } else {
                        int min1 = cluster1.getMinEvolutionGenome().evolutionFactor;
                        int min2 = cluster2.getMinEvolutionGenome().evolutionFactor;
                        double score = (min1 + min2) / 2.0;
                        results.add(score);
                        certified++;
                    }
                }
            }

            System.out.println("Number of Possible Evolutions: " + results.size());
            System.out.println("Number of Certified Evolution: " + certified);
            System.out.print("Evolution Factor for Each Evolution Pair: [");
            for (int i = 0; i < results.size(); i++) {
                System.out.print(results.get(i));
                if (i != results.size() - 1) System.out.print(", ");
            }
            System.out.println("]");
            System.out.println("##Evaluated Possible Evolutions##");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void evaluateAdaptations() {
        System.out.println("##Start Evaluating Possible Adaptations##");
        List<Integer> results = new ArrayList<>();
        int certified = 0;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            NodeList pairNodes = doc.getElementsByTagName("pair");

            for (int i = 0; i < pairNodes.getLength(); i++) {
                Element pairElem = (Element) pairNodes.item(i);
                if (pairElem.getParentNode().getNodeName().equals("possibleAdaptationPairs")) {
                    String id1 = pairElem.getElementsByTagName("firstId").item(0).getTextContent();
                    String id2 = pairElem.getElementsByTagName("secondId").item(0).getTextContent();

                    GenomeCluster cluster1 = null, cluster2 = null;
                    for (GenomeCluster c : clusters) {
                        if (c.contains(id1)) cluster1 = c;
                        if (c.contains(id2)) cluster2 = c;
                    }

                    if (cluster1 != null && cluster1 == cluster2) {
                        int pathCost = cluster1.dijkstra(id1, id2);
                        results.add(pathCost);
                        certified++;
                    } else {
                        results.add(-1);
                    }
                }
            }

            System.out.println("Number of Possible Adaptations: " + results.size());
            System.out.println("Number of Certified Adaptations: " + certified);
            System.out.print("Adaptation Factor for Each Adaptation Pair: [");
            for (int i = 0; i < results.size(); i++) {
                System.out.print(results.get(i));
                if (i != results.size() - 1) System.out.print(", ");
            }
            System.out.println("]");
            System.out.println("##Evaluated Possible Evolutions##");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dfs(String id, Set<String> visited, GenomeCluster cluster) {
        visited.add(id);
        Genome g = allGenomes.get(id);
        cluster.addGenome(g);

        for (Genome.Link link : g.links) {
            if (allGenomes.containsKey(link.target) && !visited.contains(link.target)) {
                dfs(link.target, visited, cluster);
            }
        }

        for (Genome other : allGenomes.values()) {
            for (Genome.Link link : other.links) {
                if (link.target.equals(id) && !visited.contains(other.id)) {
                    dfs(other.id, visited, cluster);
                    break;
                }
            }
        }
    }
}

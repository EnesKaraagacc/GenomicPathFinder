import java.util.*;

public class Genome {
    public String id;
    public int evolutionFactor;
    public List<Link> links;

    public Genome(String id, int evolutionFactor) {
        this.id = id;
        this.evolutionFactor = evolutionFactor;
        this.links = new ArrayList<>();
    }

    public void addLink(String target, int adaptationFactor) {
        links.add(new Link(target, adaptationFactor));
    }

    public static class Link {
        public String target;
        public int adaptationFactor;

        public Link(String target, int adaptationFactor) {
            this.target = target;
            this.adaptationFactor = adaptationFactor;
        }
    }
}

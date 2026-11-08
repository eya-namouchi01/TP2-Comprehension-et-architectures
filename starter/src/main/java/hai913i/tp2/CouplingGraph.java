package hai913i.tp2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CouplingGraph {

    public record Edge( String classA, String classB, double weight){
    }
    private List<String> nodes;
    private List<Edge> edges;

    public CouplingGraph( List<String> nodes, List<Edge> edges){
        this.nodes=nodes;
        this.edges= edges;
    }
    public List<String> nodes() {
        return nodes;
    }

    public List<Edge> edges() {
        return edges;
    }

    // Builds the graph from the coupling of step A1
    public static CouplingGraph fromCoupling(Coupling coupling) {
        List<Edge> edges = new ArrayList<>();
        for (Coupling.Pair paire : coupling.nonZeroPairs()) {
            edges.add(new Edge(paire.classA(), paire.classB(), paire.value()));
        }
        return new CouplingGraph(coupling.classes(), edges);
    }

    // The n heaviest edges
    public List<Edge> top(int n) {
        if (n >= edges.size()) {
            //all the edges if there are fewer than n
            return new ArrayList<>(edges);
        }
        return new ArrayList<>(edges.subList(0, n));
    }

    //isolated classes ( having no edge)
    public List<String> isolatedClasses() {
        List<String> isolated = new ArrayList<>();
        for (String c : nodes) {
            boolean found = false;
            for (Edge e : edges) {
                if (e.classA().equals(c) || e.classB().equals(c)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                isolated.add(c);
            }
        }
        return isolated;
    }

    public void saveFile(Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("type;classA;classB;weight");
        for (String c : nodes) {
            lines.add("node;" + c + ";;");
        }
        for (Edge e : edges) {
            lines.add("edge;" + e.classA() + ";" + e.classB() + ";" + e.weight());
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    // DOT export
    public String toDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("graph couplage {\n");
        sb.append("  layout=neato;\n");
        sb.append("  overlap=false;\n");
        sb.append("  sep=\"+40\";\n");
        sb.append("  splines=true;\n");
        sb.append("  node [shape=box, fontname=\"Helvetica\", fontsize=10];\n");
        sb.append("  edge [fontname=\"Helvetica\", fontsize=8, fontcolor=\"gray35\"];\n");
        for (String c : nodes) {
            sb.append("  \"" + c + "\" [label=\"" + Report.shortName(c) + "\"];\n");
        }
        for (Edge e : edges) {
            sb.append("  \"" + e.classA() + "\" -- \"" + e.classB() + "\""
                    + " [label=\"" + Report.num(e.weight()) + "\", penwidth=" + (e.weight() * 30) + "];\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    public static CouplingGraph load(Path file) throws IOException {
        List<String> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            List<String> content= Arrays.stream(line.split(";", -1)).toList();
            if (content.size() != 4) {
                throw new IOException("ligne " + (i + 1) + " invalide : " + line);
            }
            if (content.get(0).equals("node")) nodes.add(content.get(1));
            else  {
                try {
                    edges.add(new Edge(content.get(1), content.get(2), Double.parseDouble(content.get(3))));
                } catch (NumberFormatException e) {
                    throw new IOException("poids invalide ligne " + (i + 1) + " : " + content.get(3));
                }
            }
        }
        return new CouplingGraph(nodes, edges);
    }
}

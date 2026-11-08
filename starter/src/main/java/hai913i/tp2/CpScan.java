package hai913i.tp2;

import java.util.List;

// Step B4 (help for the interpretation): how the modules change with CP.
// The cut only changes when CP crosses the C(G) of a node of the tree,
// so we print C(G) for every merge, then we try CP values one by one.
public class CpScan {

    public static String report(Clustering clustering, Coupling coupling, int classCount) {
        StringBuilder sb = new StringBuilder();

        // 1) C(G) of every internal node, in the order of the merges.
        // These values are the exact thresholds where the cut changes.
        Cut cutForValues = new Cut(clustering.root(), coupling, classCount, 0.0);
        sb.append("C(G) de chaque fusion (seuils exacts) :\n");
        List<Cluster> merges = clustering.merges();
        for (int i = 0; i < merges.size(); i++) {
            Cluster m = merges.get(i);
            sb.append("  Étape " + (i + 1) + " (" + m.members().size() + " classes) : similarité = "
                    + Report.num(m.similarity()) + ", C(G) = "
                    + Report.num(cutForValues.internalCoupling(m)) + "\n");
        }

        // 2) Try CP from 0 to 0.060 by steps of 0.001.
        // Each time the number of modules changes, print the new modules.
        // Once M/2 is violated, it stays violated.
        // We stop at the first violation.
        sb.append("Effet de CP :\n");
        int previous = -1;
        for (int k = 0; k <= 60; k++) {
            double cp = k / 1000.0;      // k / 1000 instead of cp += 0.001: no rounding drift
            Cut cut = new Cut(clustering.root(), coupling, classCount, cp);
            int n = cut.modules().size();
            if (n != previous) {
                sb.append("  à partir de CP = " + Report.num(cp) + " : " + n + " modules, contrainte "
                        + (cut.respectsLimit() ? "respectée" : "violée") + "\n");
                sb.append(modules(cut));
                previous = n;
                if (!cut.respectsLimit()) {
                    sb.append("  La contrainte reste violée pour tout CP plus grand : arrêt.\n");
                    break;
                }
            }
        }
        return sb.toString();
    }

    // One line per module
    private static String modules(Cut cut) {
        StringBuilder sb = new StringBuilder();
        for (Cluster m : cut.modules()) {
            sb.append("      " + m + "\n");
        }
        return sb.toString();
    }
}

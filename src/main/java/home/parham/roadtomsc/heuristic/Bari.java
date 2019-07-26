/*
 * In The Name Of God
 * ======================================
 * [] Project Name : roadtomsc
 *
 * [] Package Name : home.parham.roadtomsc.heuristic
 *
 * [] Creation Date : 13-07-2019
 *
 * [] Created By : Parham Alvani (parham.alvani@gmail.com)
 * =======================================
 */

package home.parham.roadtomsc.heuristic;

import home.parham.roadtomsc.domain.Chain;
import home.parham.roadtomsc.domain.Node;
import home.parham.roadtomsc.domain.Types;
import home.parham.roadtomsc.problem.Config;
import javafx.util.Pair;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Bari {
    private final static Logger logger = Logger.getLogger(Bari.class.getName());

    private Config cfg;
    /**
     * This is a copy from nodes of configuration object. This will be modified during the process of selection.
     */
    private List<Node> nodes;
    private ArrayList<ArrayList<Integer>> placement;

    public Bari(Config cfg) {
        this.cfg = cfg;
        this.placement = new ArrayList<>();
        this.nodes = new ArrayList<>(this.cfg.getNodes());
    }


    /**
     * solve the problem and then return the placement
     */
    public List<List<Integer>> solve() {
        this.cfg.getChains().forEach(this::place);

        return Collections.unmodifiableList(
                this.placement.stream().map(a -> a != null ? Collections.unmodifiableList(a) : null).collect(Collectors.toList())
        );
    }

    /**
     * place places the given chain with Bari algorithm
     * @param chain for placement
     */
    private void place(Chain chain) {
        logger.info(String.format(" -- * chain with %d nodes * -- ", chain.nodes()));

        // chain placement array that maps each vnf to its physical node
        ArrayList<Integer> placement = new ArrayList<>();

        // each node represents the stage in Bari multi-stage graph
        for (int stage = 0; stage < chain.nodes() - 1; stage++) {
            logger.info(String.format(" -- stage (%d) -- ", stage));
            final Types.Type t = chain.getNode(stage);

            // list the available nodes
            List<Node> nodes = this.nodes.stream()
                    .filter(node -> !t.isIngress() || node.isIngress()) // ingress
                    .filter(node -> node.getCores() >= t.getCores()) // number of cores
                    .filter(node -> node.getRam() >= t.getRam()) // amount of ram
                    .filter(node -> !t.isEgress() || node.isEgress()) // egress
                    .collect(Collectors.toList());
            logger.info("selected nodes:");
            nodes.forEach(n -> logger.info(n.getName()));
            // provide a simple integer as score for each node
            Map<Integer, Integer> scores = nodes.stream().collect(Collectors.toMap(
                    n -> this.cfg.getNodeIndex(n.getName()),
                    n -> 0
            ));

            if (stage > 0) { // use bfs from the last placed node
                int selectedNode = placement.get(stage - 1);
                this.bfs(selectedNode, scores);
            }
            Optional<Map.Entry<Integer, Integer>> op = scores.entrySet().stream().min(Comparator.comparingInt(Map.Entry::getValue));
            if (!op.isPresent()) {
                // there is no way to place the given chain
                this.placement.add(null);
                return;
            }

            // select a physical node with minimum cost
            int bestNode = op.get().getKey();
            logger.info(String.format("best node: %d", bestNode));

            Node n = this.nodes.get(bestNode);
            n.setCores(n.getCores() - t.getCores());
            n.setRam(n.getRam() - t.getRam());

            placement.add(bestNode);
        }

        // place the VNFM in last stage

        this.placement.add(placement);
    }

    /**
     * @param root is the source of BFS that has depth 0
     * @param scores is the map that will be filled by the distance between root and its keys
     */
    private void bfs(int root, Map<Integer, Integer> scores) {
        Queue<Pair<Integer, Integer>> q = new LinkedList<>();
        Map<Integer, Boolean> seen = new HashMap<>();

        q.add(new Pair<>(root, 0));

        while (!q.isEmpty()) {
            Pair<Integer, Integer> p = q.remove();
            int source = p.getKey();
            int depth = p.getValue();

            if (seen.getOrDefault(source, false))
                continue;

            scores.computeIfPresent(source, (node, score) -> depth);
            seen.put(source, true);

            this.cfg.getLinks().stream()
                    .filter(l -> l.getSource() == source)
                    .forEach(l -> q.add(new Pair<>(l.getDestination(), depth+1)));
        }
    }
}

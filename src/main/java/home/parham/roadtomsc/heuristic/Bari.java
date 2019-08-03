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

        // List of the feasible's nodes in each stage
        List<List<Node>> feasibleNodes = new ArrayList<>();

        // each node represents the stage in Bari multi-stage graph,
        // in each stage a node selected for previous stage and each stage
        // represents a chain's node.
        for (int stage = 0; stage < chain.nodes() - 1; stage++) {
            logger.info(String.format(" -- stage (%d) -- ", stage));
            final Types.Type t = chain.getNode(stage);

            // list the available nodes
            List<Node> currentFeasibleNodes = this.nodes.stream()
                    .filter(node -> !t.isIngress() || node.isIngress()) // ingress
                    .filter(node -> node.getCores() >= t.getCores()) // number of cores
                    .filter(node -> node.getRam() >= t.getRam()) // amount of ram
                    .filter(node -> !t.isEgress() || node.isEgress()) // egress
                    .collect(Collectors.toList());
            logger.info("current feasible nodes:");
            currentFeasibleNodes.forEach(n -> logger.info(n.getName()));

            if (stage > 0) {
                // provide a boolean that indicates reachability for each node from the previous stage
                // here we select a node for previous stage that has more reachability in current stage
                // by this metric we have more choice in the next stage but there is no guarantee for that
                List<Node> previousStageNodes = feasibleNodes.get(stage - 1);
                List<Map<Integer, Boolean>> previousStageNodesBFSResults = new ArrayList<>();
                previousStageNodes.forEach(node -> {
                    Map<Integer, Boolean> reachability = currentFeasibleNodes.stream().collect(Collectors.toMap(
                            n -> this.cfg.getNodeIndex(n.getName()),
                            n -> false
                    ));
                    this.bfs(this.cfg.getNodeIndex(node.getName()), reachability);
                    previousStageNodesBFSResults.add(reachability);
                });

                // find the node from the previous stage that has maximum routes to current stage feasible nodes
                Optional<Map<Integer, Boolean>> op = previousStageNodesBFSResults.stream()
                        .max(Comparator.comparingInt(
                                result -> result.values().stream().reduce(0, (subtotal, value) -> subtotal + (value ? 1 : 0), Integer::sum)
                        ));

                if (!op.isPresent()) {
                    // there is no way to place the given chain
                    this.placement.add(null);
                    return;
                }

                // current stage feasible nodes are the nodes that are reachable from the selected node of the previous stage
                feasibleNodes.add(
                        op.get().entrySet().stream().filter(Map.Entry::getValue).map(e -> this.nodes.get(e.getKey())).collect(Collectors.toList())
                );

                // select a physical node with maximum reachable nodes
                int bestNode = previousStageNodesBFSResults.indexOf(op.get());
                logger.info(String.format("best node: %d", bestNode));

                Node n = this.nodes.get(bestNode);
                n.setCores(n.getCores() - t.getCores());
                n.setRam(n.getRam() - t.getRam());

                placement.add(bestNode);
            }
        }

        // place the VNFM in last stage

        this.placement.add(placement);
    }

    /**
     * @param root is the source of BFS that has depth 0
     * @param reachability is the map that will be filled by true when the node is reachable from the source
     */
    private void bfs(int root, Map<Integer, Boolean> reachability) {
        Queue<Integer> q = new LinkedList<>();
        Map<Integer, Boolean> seen = new HashMap<>();

        q.add(root);
        s.add(0);

        while (!q.isEmpty()) {
            int source = q.remove();

            if (seen.getOrDefault(source, false))
                continue;

            reachability.computeIfPresent(source, (node, reachable) -> true);
            seen.put(source, true);

            // TODO: check the link bandwidth here
            this.cfg.getLinks().stream()
                    .filter(l -> l.getSource() == source)
                    .forEach(l -> q.add(l.getDestination()));
        }
    }
}

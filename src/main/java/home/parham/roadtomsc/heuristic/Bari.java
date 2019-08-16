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
import home.parham.roadtomsc.domain.Link;
import home.parham.roadtomsc.domain.Node;
import home.parham.roadtomsc.domain.Types;
import home.parham.roadtomsc.problem.Config;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Bari {
    private final static Logger logger = Logger.getLogger(Bari.class.getName());

    /**
     * Problem's configuration
     */
    private Config cfg;

    /**
     * This is a copy from nodes of configuration object. This will be modified during the process of selection.
     */
    private List<Node> nodes;

    /**
     * ManagedVNFs stores the number of managedVNFs for a physical node to count number of license and more
     */
    private Map<Integer, Integer> managedVNFs;

    /**
     * ManagerRoutes holds the route information between each VNF's physical server and it's chain's VNFM
     */

    /**
     * This is a copy from links of configuration object. This will be modified during the process of selection.
     */
    private List<Link> links;

    /**
     * Placement stores the placement of chains' VNF on the physical node.
     * It is used as the final result for Bari algorithm.
     */
    private ArrayList<ArrayList<Integer>> placement;

    public Bari(Config cfg) {
        this.cfg = cfg;
        this.placement = new ArrayList<>();
        this.nodes = new ArrayList<>(this.cfg.getNodes());
        this.links = new ArrayList<>(this.cfg.getLinks());
        this.managedVNFs = new HashMap<>();
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
     *
     * @param chain for placement
     */
    private void place(Chain chain) {
        logger.info(String.format(" -- * chain with %d nodes * -- ", chain.nodes()));

        // chain placement array that maps each vnf to its physical node
        ArrayList<Integer> placement = new ArrayList<>();

        // List of the feasible's nodes in each stage
        List<List<Node>> feasibleNodes = new ArrayList<>();

        // List of the feasible's nodes' paths in each stage
        List<Map<Integer, List<Integer>>> feasibleNodesPath = new ArrayList<>();


        // ==================================================
        // VNF Placement
        // ==================================================

        // each node represents the stage in Bari multi-stage graph,
        // in each stage a node selected for previous stage and each stage
        // represents a chain's node.
        for (int stage = 0; stage < chain.nodes(); stage++) {
            logger.info(String.format(" -- stage (%d) -- ", stage));
            final Types.Type t = chain.getNode(stage);

            // list the available nodes
            List<Node> currentFeasibleNodes = this.nodes.stream()
                    .filter(node -> !t.isIngress() || node.isIngress()) // ingress
                    .filter(node -> node.getCores() >= t.getCores()) // number of cores
                    .filter(node -> node.getRam() >= t.getRam()) // amount of ram
                    .filter(node -> !t.isEgress() || node.isEgress()) // egress
                    .collect(Collectors.toList());
            logger.info("current feasible nodes: " + Arrays.toString(currentFeasibleNodes.stream().map(Node::getName).toArray()));

            if (currentFeasibleNodes.size() == 0) {
                this.placement.add(null);
                return;
            }

            if (stage > 0) {
                // Provide a boolean that indicates reachability for each node from the previous stage.
                // Here we select a node for the previous stage that has more reachability in the current
                // stage by this metric we have more choice in the next stage, but there is no guarantee for that as you know.
                List<Node> previousStageNodes = feasibleNodes.get(stage - 1);
                final Map<Integer, List<Integer>> previousStagePath = stage > 1 ? feasibleNodesPath.get(stage - 2) : new HashMap<>();

                List<Map<Integer, Boolean>> previousStageNodesBFSResults = new ArrayList<>();
                List<Map<Integer, List<Integer>>> previousStageNodesBFSPaths = new ArrayList<>();
                // here we assume chains has the linear format
                int bandwidth = chain.getLink(stage - 1).getBandwidth();
                int previousBandwidth = stage > 1 ? chain.getLink(stage - 2).getBandwidth(): 0;
                previousStageNodes.forEach(node -> {
                    Map<Integer, Boolean> reachability = currentFeasibleNodes.stream().collect(Collectors.toMap(
                            n -> this.cfg.getNodeIndex(n.getName()),
                            n -> false
                    ));
                    // assume the physical link status based on the previous stage paths
                    List<Integer> path = previousStagePath.getOrDefault(this.cfg.getNodeIndex(node.getName()), new ArrayList<>());
                    for (int i = 0; i < path.size() - 1; i++) {
                        int source = path.get(i);
                        int destination = path.get(i + 1);
                        this.links.stream()
                                .filter(l -> l.getSource() == source && l.getDestination() == destination)
                                .forEach(l -> l.setBandwidth(l.getBandwidth() - previousBandwidth));
                    }
                    previousStageNodesBFSPaths.add(this.bfs(this.cfg.getNodeIndex(node.getName()), reachability, bandwidth));
                    // revert the assumption
                    for (int i = 0; i < path.size() - 1; i++) {
                        int source = path.get(i);
                        int destination = path.get(i + 1);
                        this.links.stream()
                                .filter(l -> l.getSource() == source && l.getDestination() == destination)
                                .forEach(l -> l.setBandwidth(l.getBandwidth() + previousBandwidth));
                    }
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
                feasibleNodesPath.add(previousStageNodesBFSPaths.get(bestNode));

                // update the selected node cores and ram
                Node n = previousStageNodes.get(bestNode);
                logger.info(String.format("best node: %s", n.getName()));
                n.setCores(n.getCores() - chain.getNode(stage - 1).getCores());
                n.setRam(n.getRam() - chain.getNode(stage - 1).getRam());

                // update the selected node links of its previous stage
                List<Integer> path = previousStagePath.getOrDefault(this.nodes.indexOf(n), new ArrayList<>());
                for (int i = 0; i < path.size() - 1; i++) {
                    int source = path.get(i);
                    int destination = path.get(i + 1);
                    this.links.stream()
                            .filter(l -> l.getSource() == source && l.getDestination() == destination)
                            .forEach(l -> l.setBandwidth(l.getBandwidth() - previousBandwidth));
                }

                placement.add(this.nodes.indexOf(n));
            } else {
                feasibleNodes.add(currentFeasibleNodes);
            }

            if (stage == chain.nodes() - 1) { // the last stage must be placed in placed
                Node n = currentFeasibleNodes.get(0);
                logger.info(String.format("best node: %s", n.getName()));
                n.setCores(n.getCores() - t.getCores());
                n.setRam(n.getRam() - t.getRam());

                List<Integer> path = feasibleNodesPath.get(stage - 1).getOrDefault(this.nodes.indexOf(n), new ArrayList<>());
                int bandwidth = chain.getLink(stage - 1).getBandwidth();
                for (int i = 0; i < path.size() - 1; i++) {
                    int source = path.get(i);
                    int destination = path.get(i + 1);
                    this.links.stream()
                            .filter(l -> l.getSource() == source && l.getDestination() == destination)
                            .forEach(l -> l.setBandwidth(l.getBandwidth() - bandwidth));
                }

                placement.add(this.nodes.indexOf(n));
            }
        }

        // ==================================================
        // VNFM Placement
        // ==================================================
        // Please note that we consider the management path's direction from the manager to the nodes

        // Place the VNFM based on the current placement of the chain.
        // There is only one VNFM for a chain, and we find it based on the set of available manager nodes.
        logger.info(" -- VNFM -- ");
        // managedNodes is an array of physical nodes that host a manageable VNF of the chain
        List<Integer> managedNodes = new ArrayList<>();
        for (int i : placement) {
            if (chain.getNode(i).isManageable()) {
                managedNodes.add(i);
            }
        }
        Map<Integer, Map<Integer, List<Integer>>> managerRoutes = new HashMap<>();
        Set<Integer> availableManagers = IntStream.range(0, this.nodes.size()).boxed().collect(Collectors.toSet());
        managedNodes.stream().map(id -> this.nodes.get(id).getNotManagerNodes()).forEach(availableManagers::removeAll);
        // Here we check the VNFM resources by counting the newly created instances
        availableManagers = availableManagers.stream().filter(n -> {
            // newly created VNFM instances
            int instances = (int) Math.ceil(
                    (double) (-this.managedVNFs.getOrDefault(n, 0) % this.cfg.getVnfmCapacity()
                            + chain.getNodes().stream().filter(Types.Type::isManageable).count())
                            / this.cfg.getVnfmCapacity());
            Node pn = this.nodes.get(n);
            if (instances * this.cfg.getVnfmCores() > pn.getCores()) {
                return false;
            }
            return instances * this.cfg.getVnfmRam() <= pn.getRam();
        }).collect(Collectors.toSet());
        // Here we check the network connectivity between managers and placed chain
        availableManagers = availableManagers.stream().filter(n -> {
            Map<Integer, Boolean> reachability = managedNodes.stream().distinct().collect(Collectors.toMap(
                    i -> i,
                    i -> false
            ));
            managerRoutes.put(n, bfs(n, reachability, cfg.getVnfmBandwidth()));
            return !reachability.containsValue(false);
        }).collect(Collectors.toSet());
        logger.info("Available managers: " + Arrays.toString(availableManagers.toArray()));

        // Choose physical node randomly and update its core and ram
        Optional<Integer> op = availableManagers.stream().findFirst();
        if (!op.isPresent()) {
            this.placement.add(null);
            return;
        }
        final int selectedManagerIndex = op.get();
        Node selectedManager = this.nodes.get(selectedManagerIndex);
        // Update the number of managedVNFs on selected node
        this.managedVNFs.compute(selectedManagerIndex, (index, managed) -> (managed == null ? 0 : managed) + chain.nodes());
        // newly created VNFM instances
        int instances = (int) Math.ceil(
                (double) (-this.managedVNFs.getOrDefault(selectedManagerIndex, 0) % this.cfg.getVnfmCapacity()
                        + chain.getNodes().stream().filter(Types.Type::isManageable).count())
                        / this.cfg.getVnfmCapacity());
        selectedManager.setCores(selectedManager.getCores() - instances * this.cfg.getVnfmCores());
        selectedManager.setRam(selectedManager.getRam() - instances * this.cfg.getVnfmRam());
        logger.info(String.format("Selected manager is %s with %d VNFM instances for %d VNF",
                selectedManager.getName(), instances,
                chain.getNodes().stream().filter(Types.Type::isManageable).count())
        );
        // Each VNF needs a communication link with its chain VNFM. This link start from its physical node to VNFM.
        // Here we allocate management's bandwidth on this path.
        managedNodes.forEach(n -> {
            List<Integer> path = managerRoutes.get(selectedManagerIndex).get(n);
            for (int i = 0; i < path.size() - 1; i++) {
                int source = path.get(i);
                int destination = path.get(i + 1);
                this.links.stream()
                        .filter(l -> l.getSource() == source && l.getDestination() == destination)
                        .forEach(l -> l.setBandwidth(l.getBandwidth() - this.cfg.getVnfmBandwidth()));
            }
        });

        this.placement.add(placement);
        System.out.println(Arrays.toString(this.links.toArray()));
    }

    /**
     * @param root         is the source of BFS that has depth 0
     * @param reachability is the map that contains nodes which needs reachability check,
     *                     and the node's key will be filled by true when the node is reachable from the source.
     * @return a map that contains the user given keys with their path from the source.
     */
    private Map<Integer, List<Integer>> bfs(int root, Map<Integer, Boolean> reachability, int bandwidth) {
        logger.fine("BFS from " + root + " for " + reachability.keySet().toString());
        Queue<Integer> q = new LinkedList<>();
        Queue<List<Integer>> path = new LinkedList<>();
        Map<Integer, Boolean> seen = new HashMap<>();
        Map<Integer, List<Integer>> result = new HashMap<>();

        q.add(root);
        path.add(new ArrayList<>(root));

        while (!q.isEmpty()) {
            int source = q.remove();
            List<Integer> rootPath = path.remove();
            rootPath.add(source);

            if (seen.getOrDefault(source, false))
                continue;

            // update reachability
            reachability.computeIfPresent(source, (node, reachable) -> true);
            seen.put(source, true);

            // update path
            if (reachability.containsKey(source)) {
                logger.fine(Arrays.toString(rootPath.toArray()));
                result.put(source, rootPath);
            }

            // TODO: check the link bandwidth here
            this.links.stream()
                    .filter(l -> l.getSource() == source)
                    .filter(l -> l.getBandwidth() > bandwidth)
                    .forEach(l -> {
                        q.add(l.getDestination());
                        path.add(new ArrayList<>(rootPath));
                    });
        }
        return result;
    }
}

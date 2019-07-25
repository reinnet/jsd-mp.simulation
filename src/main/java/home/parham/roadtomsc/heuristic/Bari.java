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
import java.util.stream.Collectors;

public class Bari {
    private Config cfg;
    private ArrayList<ArrayList<Integer>> placement;

    public Bari(Config cfg) {
        this.cfg = cfg;
        this.placement = new ArrayList<>();
    }

    // place places the given chain with Bari algorithm
    public void place(int index) {
        Chain chain = this.cfg.getChains().get(index);
        // chain placement array that maps each vnf to its physical node
        ArrayList<Integer> placement = new ArrayList<>();

        // each node represents the stage in Bari multi-stage graph
        for (int stage = 0; stage < chain.nodes() - 1; stage++) {
            final Types.Type t = chain.getNode(stage);

            // list the available nodes
            List<Node> nodes = this.cfg.getNodes().stream()
                    .filter(node -> !t.isIngress() || node.isIngress()) // ingress
                    .filter(node -> node.getCores() >= t.getCores()) // number of cores
                    .filter(node -> node.getRam() >= t.getRam()) // amount of ram
                    .filter(node -> !t.isIngress() || node.isEgress()) // egress
                    .collect(Collectors.toList());
            // provide a simple integer as score for each node
            Map<Integer, Integer> scores = nodes.stream().collect(Collectors.toMap(
                    n -> this.cfg.getNodeIndex(n.getName()),
                    n -> 0
            ));

            if (stage > 1) { // use bfs from the last placed node
                int selectedNode = placement.get(stage - 1);
            }
            int bestNode;
            // select a physical node with minimum cost
            placement.add(bestNode);
        }


        this.placement.add(placement);
    }
}

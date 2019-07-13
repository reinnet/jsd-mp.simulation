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

import java.util.List;
import java.util.stream.Collectors;

public class Bari {
    private Config cfg;

    public Bari(Config cfg) {
        this.cfg = cfg;
    }

    public void place(int index) {
        Chain chain = this.cfg.getChains().get(index);

        // each node represents the stage in Bari multi-stage graph
        for (int stage = 0; stage < chain.nodes() - 1; stage++) {
            final Types.Type t = chain.getNode(stage);
            List<Node> nodes = this.cfg.getNodes().stream()
                    .filter(node -> !t.isIngress() || node.isIngress()) // ingress
                    .filter(node -> node.getCores() >= t.getCores()) // number of cores
                    .filter(node -> node.getRam() >= t.getRam()) // amount of ram
                    .filter(node -> !t.isIngress() || node.isEgress()) // egress
                    .collect(Collectors.toList());
            if (stage > 1) { // use bfs from the last placed node
            }
            // select a physical node with minimum cost
        }
    }
}

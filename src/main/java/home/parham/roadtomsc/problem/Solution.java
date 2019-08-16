/*
 * In The Name Of God
 * ======================================
 * [] Project Name : roadtomsc
 *
 * [] Package Name : home.parham.roadtomsc.problem
 *
 * [] Creation Date : 17-08-2019
 *
 * [] Created By : Parham Alvani (parham.alvani@gmail.com)
 * =======================================
 */

package home.parham.roadtomsc.problem;

import java.util.List;
import java.util.Map;

public class Solution {
    private int cost;
    /**
     * Placement array for each chain and empty array indicates error in placement
     */
    private List<List<String>> vnfPlacement;
    /**
     * Name of chain's manager physical server and `-` indicates error in placement
     */
    private List<String> vnfmPlacement;
    private List<Map<Integer, List<String>>> vnfmRoutes;

    public Solution(int cost, List<List<String>> vnfPlacement, List<String> vnfmPlacement, List<Map<Integer, List<String>>> vnfmRoutes) {
        this.cost = cost;
        this.vnfPlacement = vnfPlacement;
        this.vnfmPlacement = vnfmPlacement;
        this.vnfmRoutes = vnfmRoutes;
    }

    public int getCost() {
        return cost;
    }

    public List<List<String>> getVnfPlacement() {
        return vnfPlacement;
    }

    public List<String> getVnfmPlacement() {
        return vnfmPlacement;
    }

    public List<Map<Integer, List<String>>> getVnfmRoutes() {
        return vnfmRoutes;
    }
}

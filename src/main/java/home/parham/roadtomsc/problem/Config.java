package home.parham.roadtomsc.problem;

import home.parham.roadtomsc.domain.Chain;
import home.parham.roadtomsc.domain.Link;
import home.parham.roadtomsc.domain.Node;

import java.util.Collections;
import java.util.List;

/**
 * Config represents problem configuration
 * by configuration we mean parameters that are given
 * by user.
 */
public class Config {

    /**
     * physical nodes
     */
    private List<Node> nodes;

    /**
     * number of physical nodes
     */
    private int W;

    /**
     * physical links
     */
    private List<Link> links;

    /**
     * connectivity matrix
     */
    private int[][] E;

    /**
     * number of VNF types
     */
    private int F;

    /**
     * SFC requests chains
     */
    private List<Chain> chains;

    /**
     * number of SFC requests
     */
    private int T;

    /**
     * total number of VNFs
     */
    private int V;

    /**
     * total number of virtual links
     */
    private int U;

    /**
     * VNFMs parameters
     */
    private int vnfmRam, vnfmCores, vnfmCapacity, vnfmRadius, vnfmBandwidth, vnfmLicenseFee;

    Config(
            List<Node> nodes,
            int w,
            List<Link> links,
            int[][] e,
            int f,
            List<Chain> chains,
            int t,
            int v,
            int u,
            int vnfmRam,
            int vnfmCores,
            int vnfmCapacity,
            int vnfmRadius,
            int vnfmBandwidth,
            int vnfmLicenseFee
    ) {
        this.nodes = nodes;
        W = w;
        this.links = links;
        E = e;
        F = f;
        this.chains = chains;
        T = t;
        V = v;
        U = u;
        this.vnfmRam = vnfmRam;
        this.vnfmCores = vnfmCores;
        this.vnfmCapacity = vnfmCapacity;
        this.vnfmRadius = vnfmRadius;
        this.vnfmBandwidth = vnfmBandwidth;
        this.vnfmLicenseFee = vnfmLicenseFee;
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public int getW() {
        return W;
    }

    public List<Link> getLinks() {
        return Collections.unmodifiableList(links);
    }

    public int[][] getE() {
        return E;
    }

    public int getF() {
        return F;
    }

    public List<Chain> getChains() {
        return Collections.unmodifiableList(chains);
    }

    public int getT() {
        return T;
    }

    public int getV() {
        return V;
    }

    public int getU() {
        return U;
    }

    public int getVnfmRam() {
        return vnfmRam;
    }

    public int getVnfmCores() {
        return vnfmCores;
    }

    public int getVnfmCapacity() {
        return vnfmCapacity;
    }

    public int getVnfmRadius() {
        return vnfmRadius;
    }

    public int getVnfmBandwidth() {
        return vnfmBandwidth;
    }

    public int getVnfmLicenseFee() {
        return vnfmLicenseFee;
    }
}

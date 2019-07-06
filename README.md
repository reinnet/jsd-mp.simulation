# Simulation

## Introduction

Problem formulation has been implemented in [IBM CPLEX](https://www.ibm.com/analytics/cplex-optimizer).
This formulation can be configured in many aspects and reflects the VNFM placement problem with VNFM
in a data center. Results are placed in the results folder along with their configuration.

## Up and Running

For running simulation, you need a working installation of CPLEX and Java/Maven on your system
then you can run the simulation with the following command:

```sh
cd simulation
CPLEX_LIB="$HOME/ibm/ILOG/CPLEX_Studio128/cplex/bin/x86-64_linux"
mvn install
sed -i "s#path=.*#path=${CPLEX_LIB}#g" .mvn/jvm.config
mvn exec:java
```

## Results
There are the reults of using this simulation for solving the real problem of placing the NFV chains on k-ary fat tree.

> 4 vCPU, 16GB of RAM, without GC limit, 12GB heap size, 8 chains

| k-ary fat tree | time     |
|----------------|----------|
| 6              | 4 sec    |
| 8              | 7 sec    |
| 10             | 1 min    |
| 12             | 5:20 min |

> 4 vCPU, 16GB of RAM, without GC limit, 12GB heap size, 12-ary fat tree

| chains | time         |
|--------|--------------|
| 8      | 5:20 min     |
| 10     | 7:06 min     |
| 11     | 16:33 min    |
| 12     | OOM Killed   |

> 8 vCPU, 22GB of RAM, without GC limit, 12GB heap size, 12-ary fat tree

| chains | time         |
|--------|--------------|
| 12     | 14:53 min    |

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

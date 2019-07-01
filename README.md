# Simulation

## Introduction

Problem formulation has been implemented in [IBM CPLEX](https://www.ibm.com/analytics/cplex-optimizer).
This formulation can be configured in many aspects and reflects the VNFM placement problem with VNFM
in a data center. Results are placed in the results folder along with their configuration.

## Up and Running

For running simulation, you need a working installation of CPLEX on your system
then you can run the simulation with the following java option:

```
"-Djava.library.path=$USER_HOME$/Application/CPLEX_Studio-Community128/cplex/bin/x86-64_linux"
```

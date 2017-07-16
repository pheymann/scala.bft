[![Build Status](https://travis-ci.org/pheymann/scala.bft.svg?branch=develop)](https://travis-ci.org/pheymann/scala.bft)

# Scala.bft (This is a WIP prototype which isn't even close to complete :))
This projects aims to provide scala based implementation of the **B**yzantine **F**ault **T**olerance protocole and furthermore to introduce a **mutli-leader** approach to obtain parallelism and an improved performance.

## BFT And Its Parallelization
This page gives a rough overview about Byzantine Fault Tolerance (BFT) and how the parallelization is done.

Fore a detailed description of the BFT protocol read the paper [Practical Byzantine Fault Tolerance](http://pmg.csail.mit.edu/papers/osdi99.pdf).

## Table of Contents
 - [Introduction](#short-introduction)
 - [BFT in a Nutshell](#bft-in-a-nutshell)
 - [Multi-Leader Approach](#multi-leader-approach)
 - [Machine Learning to predict the best Partitioning](#machine-learning-to-predict-the-best-partitioning)

### Short Introduction
The number of online services which are used in the daily life is largely increasing, just like the
requirements the provider and customers lay down on them. Many of these systems consist of critical
data, e.g. user information or monetary transactions, which specifically demand for:

 - higher robustness,
 - higher availability,
 - stronger consistency.

To deal with them the following two approaches were developed:
  
 - [state machines](https://www.cs.cornell.edu/fbs/publications/SMSurvey.pdf) (SM):<br>
   A service is represented as state machine consisting of a state `S` and a distinct number of operations `o_i` to modify the state.
 - replication technology:<br>
   A number of `n` replicas (mirrors) are created from a state machine acting like a single service to the client.

Thus, the robustness and availability can be increased as the service doesn't fail when on replica is going down. On the other hand it doesn't give any guarantees on the consistency of the underlying data, introducing two types of possible failures to the system:

 - *fail-stop* failure:<br>
   A replica may shutdown and doesn't receives the latest changes on the state. When coming back to production it runs in a different state as the rest of the system, violating the consistency. Reasons for that may by network separations, resource management (e.g. process doesn't get cpu for a certain amount of time), etc.
 - *byzantine* failure:<br>
  An even worse scenario leads to a replica which continues to work but doesn't follow its specification. Reasons for that may attacks on the systems, programming errors (bugs) or hardware glitches. 

Therefore, the replicated state machine service has to become *fault tolerant* and be able to agree on a state consensus. One general concept for handling *fail-stop* and *byzantine* failures is the **B**yzantine **F**ault **T**olerance protocol.

### BFT In A Nutshell
A BFT system consists of a single leader `L` and a number of follower `F` machines with `|F| = n - 1`. When a client `C` requests  an operation `o_i`, the request `r_i` is sent to the leader which starts the three consensus rounds shown in the following figure.

<p align="center">
  <img src="https://pheymann.github.io/scala.bft/img/pbft.svg"/>
<p/>

 1. **Pre-Prepare**: the request `r_i` is delivered from `L` to all `F` (n)
 2. **Prepare**: all replicas send a `prepare` message to all other machines and wait for `2f` acknowledgments. Thereby, a total order can be guaranteed. (n^2)
 3. **Commit**: again all replicas send a `commit` message to all other machines and wait for `2f + 1`  acknowledgments. Now the system agreed on the same request and is able to execute it. (n^2)

Afterwards the replicas returning the result of the operation execution to the requesting client `C`. This is waiting for `f + 1` responses.

As it is easy to see two main problems arise with the protocol:
 - resource consumption: to agree on one request `2n^2 + n` messages are sent within the system. Furthermore `3f + 1` machines have to be provided to hold the consistency guarantees. Compared to `f + 1` machines and `n^2 + n` (?) messages sent for a fail-stop protocol, this is a huge increase.
 - single leader bottleneck (performance): to gain the total order all messages have to be sent through the single leader making him the performance bottleneck. The leader `L` determines the overall system performance.

### Multi-Leader Approach
There are a number of optimizations e.g. avoiding the consensus steps for read only requests, but they all do not reduce the overall number of messages during agreement or machines necessary to provide consistency. But it is possible to improve the performance introducing multiple leader and therefore parallelism into the protocol.

The basic assumption within BFT is that all requests are depending on each other because the state objects they try to access are fully dependent, thus making it necessary to obtain a total order and a single leader. But this hasn't to be true especially in real world applications. There state objects and therefore request might be grouped as they are geographically or access permission related. E.g. some objects of the state may by only accessable with certain rights for certain clients making them independent of changes done to other objects. Or they can be even fully independent as in a distributed hash-map. If you take this information into account it is possible to split up the global state `S` into multiple **partitions** each with its own **partial order**.

--image

#### Basic Architecture
The new parallelized BFT protocol consists of leaders `L`, followers `F` and partitions `P` with `|L| = |F| = |P|`. Every machine running it executes as many BFT instances as partitions exists and for the current state of the protocol every machine holds exactly one leader. Furthermore there will be just one leader per BFT instance. A future improvement should make it possible to add create machines with multiple or no leader at all.

<p align="center">
  <img src="https://pheymann.github.io/scala.bft/img/leader.svg"/>
<p/>

#### Request Types
With the new architecture the requests have to be split up into two types.

##### Simple Request
A simple request only accesses state objects from the same partition, thus avoiding synchronisation between partitions and BFT instances.

<p align="center">
  <img src="https://pheymann.github.io/scala.bft/img/simple_request.svg"/>
<p/>

##### Cross-Border Request
A cross-border request accesses state objects across multiple partitions, thus forcing to synchronize the related partitions. Synchronisation means a request `r_i` has to be in `head position` in all partitions queues `Q` of the partitions which shall be accessed. Here a queue `Q_j` is just a buffer for incoming requests.

<p align="center">
  <img src="https://pheymann.github.io/scala.bft/img/cross_border_request.svg"/>
<p/>

 - **Cross Border Execution** request: <br>
 This instance of request `r_0` is located in the queue of that partition the machine is the leader of. The CBE request will be executed.
 - **Cross Border Synchronization** request: <br>
  This instance of request `r_0` is located in all queues of partitions which are accessed but where the current machine is not the leader of. They are only used the guarantee a deterministic execution of cross-border requests over all replicas.

Only when `r_i` is ready it can be used for consensus and execution. This way the consistency can be guaranteed. But it will happen that `r_i` is at head for some `Q_j` at time `t` and for other `Q_k` not, which introduces active waiting until `HEAD(Q_k) = r_i` for all `k`.

#### Deadlocks caused by Cross-Border Requests
Besides the introduced synchronization and therefore the loss of parallelism the cross-border request have another disadvantage; they can lead to deadlocks. It is possible that multiple cb requests access the replicas at the same time leading to the following situation:

<p align="center">
  <img src="https://pheymann.github.io/scala.bft/img/nondeterminism.svg"/>
<p/>

In this scenario `r_0` waits for `r_1` to be executed and to release its CBS requests, and vis versa. Deadlock resolution strategies will be discussed later.

### Machine Learning to predict the best Partitioning
As described above it is possible to partition the state of the state machine and achieve parallelism within the BFT protocol. But this leads to a new problem: How to find the best partitioning? For some applications it may be an easy answer, as non of there state objects is related to the other. Therefore they just can be distributed by achieving:

 - an equal number of objects in every partition
 - an equal access rates in the partitions
 - etc.

But for data models which aren't that simple the 'best' partitioning has to be predicted out of the underlying data and the application behaviour. A detailed description of a solution I developed will be given later. It is based on graph partitioning.

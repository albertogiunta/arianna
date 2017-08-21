# Ariadne Project #

## IntelliJ Configuration ##

### Gradle Settings ###

Go under Build, Execution and Deployment > Build Tools > Gradle 

In the Gradle tab remove the tick fro the option "Create separate module per source set" 

## Akka Configuration ##

In order to Arianna to be fully operative, the Akka configuration for each component has to be settled properly.

### Admin Application ###

### Cluster ###

The first settings to be settled equally inside in each file.conf are the seed-nodes:
under the property akka.cluster.seed-nodes their value has to be settled to something like "akka.tcp://Arianna-Cluster@<IP Adress>:25520" 
where the IP is the address of the seed-node inside the local network (usually something like 192.168.x.y).

Actually the system is always been tested using only 1 seed-nodes, so use only one seed-node.

Who is the seed-node? The Master Node

##### Master Node #####

The Master node in the Akka-Cluster is the seed-node, that one node that maintain the coherence of the whole.

In order to be settled properly under the property akka.cluster.seed-nodes the IP should be the same specified under
akka.remote.netty-tcp.hostname. The hostname has to be the IP address of the machine inside the local network 
(usually something like 192.168.x.y).

##### Cell Node #####

The Cell nodes of the Cluster are the slave units that connects to the seed-nodes. 
Under akka.remote.netty-tcp.hostname settle its value to the IP address of the machine in the local network
(usually something like 192.168.x.y). 

Each cell has to use its own cell.conf file as there is a property which is unique for each of them in the cluster.
This property is akka.actor.serial-number, which can be settled to any String or Number, 
it only need to be unique for each node (blame the distributed publish-subscribe mediator).

###### NOTE : Ports ######

If the system is deployed under the localhost (127.0.0.1) then all the IP addresses will be the same 
and the ports need to be changed to a unique port for each Cell.

The port of the master node can be left as it is and go incrementally for each cell node.

### Teseo ###

Doesn't need any configuration, can be downloaded from <Here it goes the url>
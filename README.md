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

It can be downloaded from "https://github.com/albertogiunta/teseo"

In order for the App to be working, you need to manually insert or just scan a QRCode with the associated value of the address of the entry cell.

- **CUSTOM FOR EACH USE** IP of the first cell: 192.168.1.107
- **FIXED** URI of the first cell: :8081/uri1

The final address should look like this: *192.168.1.107***:8081/uri1**

You can create QRCodes that will work with your local network [here](http://www.qr-code-generator.com/)

## Running the System ##

### Cluster ###

Master and Cell need to be launched from command line by specifying as parameter 
the path leading to the proper akka configuration the system has to load.

More over, for the Cell node a second parameter is required, containing path leading 
json containing the information of the cell.

###### NOTE ######

The launch order of the Master and Admin Application can be any, Cell Node should be usually executed 
after the Master node.

The suggested order is Master Node > Desktop App > All the Cell in any order

### Admin Application ###

When launching the Application a small form will appear where it's required to place 
the path leading to the akka configuration file of the admin ActorSystem.

### Teseo ###

Download, Install, Run and Enjoy :D

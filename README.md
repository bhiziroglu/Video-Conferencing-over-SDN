# Video-Conferencing-over-SDN

This project provides a Multi-Party WebRTC Videoconferencing Architecture over Software Defined Networks

## Getting Started

These instructions will get you a working copy of the project. This project makes use of some external softwares. It is essential
for the prerequisites to be installed & working in order to run this project.

### Prerequisites

This project makes use of the following software:


* [Floodlight](http://www.projectfloodlight.org/floodlight/)
* [Mininet](http://mininet.org/)
* [Janus](https://janus.conf.meetecho.com/)


### Installing

Instruction on how to install the prerequisites are given in their respective download pages.

This repository has 4 main components.

```
Floodlight Modules
```

```
Mininet Topologies
```


```
Signaling Server
```


```
Traffic Engineering Manager
```

They can be found in their respective folders in the repository. 

* Floodlight Module

  A sample tutorial page is available [here](https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/pages/1343513/How+to+Write+a+Module) explaning the addition of external modules into Floodlight Project.
  All java files in Floodlight-Module folder should be installed in the same module in Floodlight.
  
  Routable service opens a route in the SDN controller. By default, TEM sends requests to /wm/session/desc path.
  
  SignalingServerRestApi service listens for HTTP requests from TEM. It also parses the incoming requests and sends them to Queue Pusher Service. It also updates the flow charts of the switches according to the path received from TEM.
  
  SDNSlicing service gets the parsed switch ID / port ID pairs from SignalingServerRestApi service and opens queues for the paths.
  
  
* Mininet Topologies
  
  We have presented four topologies. Two of them are generated using [Georgia Tech Internetwork Topology Models](https://www.cc.gatech.edu/projects/gtitm/) software.
  The other two are small topologies to test your setup environment.
  There are example usages of NAT and GRE inside those topologies.

* Signaling Server

  Signaling Server is pure JavaScript. It serves a directory and listens to a port.
  It should be inside the HTML directory in your Janus setup. By default, vp9 video call room is served by the server.
  
  
* Traffic Engineering Manager

  TEM is pure Java. It communicates with the Floodlight SDN Controller. TEM gets the topology from SDN controller
  and periodically computes shortest paths between the hosts, Janus server and the end-switches inside the topology.
  
  
## Running the tests

  * Start Floodlight and Traffic Engineering Manager.
  * Run Mininet topologies and open terminals for the hosts that are going to make a video call.
  * Run Janus and Signaling Server from a host.
  * Connect to that host from different hosts. (Hosts can be in different domains if GRE tunnel is turned on.)
 
  
### Sample Video Call

![Video Call 1](/media/video_test1.png?raw=true "Sample Video Call") 
![Video Call 2](/media/video_test2.png?raw=true "Sample Video Call") 
  
  

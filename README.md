#Setup
1. Run: 
  
The sample demo depends on all the NeverwinterDP projects , so make sure that check out all the NeverwinterDP-Commons, Queuengin, Sparkngin, Scribengin , build and install before running the sample.

  ```
  git clone http://github.com/DemandCube/NeverwinterDP-Samples
  cd NeverwinterDP-Samples/cluster
  gradle clean build install -x test
  gradle eclipse
  ```
1. Open Eclipse
1. File -> Import -> General/Existing Project into Workspace
1. Import NeverwinterDP-Samples into your workspace
 

#Files of Importance

##HelloClusterDemo.java
This demo shows you the low level api of the cluster framework

##HelloShellDemo.java
This demo shows you how to use the shell client to interact with the servers

##UseCaseClusterDemo.java
This demo shows you how to setup a group of servers, install the services according to the server role in a real use case



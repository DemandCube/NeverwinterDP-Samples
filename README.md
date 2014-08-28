#Setup
1. Run: 

  ```
  git clone http://github.com/DemandCube/NeverwinterDP-Commons
  cd NeverwinterDP-Commons
  gradle clean build install -x test
  gradle eclipse
  cd ../
  git clone http://github.com/DemandCube/NeverwinterDP-Samples
  cd NeverwinterDP-Samples/cluster
  gradle clean build install -x test
  gradle eclipse
  ```
1. Open Eclipse
1. File -> Import -> General/Existing Project into Workspace
1. Import both NeverwinterDP-Commons and NeverwinterDP-Samples into your workspace
 

#Files of Importance

##HelloClusterDemo.java
This demo shows you the low level api of the cluster framework

##HelloShellDemo.java
This demo shows you how to use the shell client to interact with the servers

##UseCaseClusterDemo.java
This demo shows you how to setup a group of servers, install the services according to the server role in a real use case



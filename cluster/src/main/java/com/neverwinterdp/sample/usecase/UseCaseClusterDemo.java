package com.neverwinterdp.sample.usecase;

import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.shell.Shell;
import com.neverwinterdp.sparkngin.NullDevMessageForwarder;
import com.neverwinterdp.util.FileUtil;

/**
 * @author Tuan
 *
 * This demo show you how to setup a group of servers, install the services according to the server role
 */
public class UseCaseClusterDemo {
  static {
    System.out.println("By default, the log information is output to the build/demo.log file.");
    System.out.println("If you want to print the log to the console, reconfigure src/main/resources/log4j.properties");
    System.setProperty("app.dir", "build/cluster") ;
    System.setProperty("log4j.configuration", "file:src/main/resources/log4j.properties") ;
  }
  
  public Server zkServer, sparknginServer, ringbearerServer, genericServer ;
  public Server[] kafkaServer ;
  public Shell shell ;
  
  public void init() throws Exception {
    FileUtil.removeIfExist("build/cluster", false);
    genericServer = Server.create("-Pserver.name=generic", "-Pserver.roles=generic") ;
    
    zkServer = Server.create("-Pserver.name=zookeeper", "-Pserver.roles=zookeeper") ;
    
    //Create 2 instances of kafka server
    kafkaServer = new Server[2] ;
    for(int i  = 0; i < kafkaServer.length; i++) {
      int id = i + 1;
      kafkaServer[i] = Server.create("-Pserver.name=kafka" + id, "-Pserver.roles=kafka") ;
    }
    sparknginServer = Server.create("-Pserver.name=sparkngin", "-Pserver.roles=sparkngin") ;
    
    ringbearerServer = Server.create("-Pserver.name=ringbearer", "-Pserver.roles=ringbearer") ;
    
    shell = new Shell() ;
    shell.getShellContext().connect();
    //Wait to make sure all the servervices are launched
    Thread.sleep(2000) ;
  }
  
  public void install() throws Exception {
    //Install the zookeeper service. All the properties start with -Pzk: will override the zookeeper server configuration
    //Check zookeeper document for more detail about the properties configuration
    shell.exec(
      "module install --member-role zookeeper -Pmodule.data.drop=true -Pzk:clientPort=2181 --autostart --module Zookeeper"
    ) ;

    //Install the kafka service. All the properties start with -Pkafka: will override the kafka server configuration
    //Check kafka document for more detail about the properties configuration
    String kafkaReplication = kafkaServer.length >= 2 ? "2" : "1" ;
    for(int i  = 0; i < kafkaServer.length; i++) {
      int id = i + 1;
      shell.exec(
        "module install "+ 
        "  --member-name kafka" + id +
        "  --autostart" +
        "  --module Kafka" +
        "  -Pmodule.data.drop=true" +
        "  -Pkafka:broker.id=" + id +
        "  -Pkafka:port=" + (9092 + i) +
        "  -Pkafka:zookeeper.connect=127.0.0.1:2181" +
        "  -Pkafka:default.replication.factor=" + kafkaReplication +
        "  -Pkafka:controller.socket.timeout.ms=90000" +
        "  -Pkafka:controlled.shutdown.enable=true" + 
        "  -Pkafka:controlled.shutdown.max.retries=3" +
        "  -Pkafka:controlled.shutdown.retry.backoff.ms=60000"
      ) ;
    }
    shell.execute(
      "module install --member-role generic -Pmodule.data.drop=true --autostart --module KafkaConsumer"
    ) ;
    
    shell.execute(
        "module install" +
        "  --member-role sparkngin" +
        "  --autostart --module Sparkngin" +
        "  -Pmodule.data.drop=true" +
        "  -Phttp-listen-port=8181" +
        "  -Pforwarder-class=" + NullDevMessageForwarder.class.getName()
    ) ;
    
    shell.execute(
      "module install --member-role ringbearer --autostart --module RingBearer"
    ) ;
         ;
    shell.execute("server registration");
    Thread.sleep(1000);
  }
  
  public void uninstall() {
    shell.execute("module uninstall --member-role ringbearer --timeout 20000 --module RingBearer");
    shell.execute("module uninstall --member-role sparkngin --timeout 20000 --module Sparkngin");
    shell.execute("module uninstall --member-role generic --timeout 20000 --module KafkaConsumer");
    shell.execute("module uninstall --member-role kafka --timeout 20000 --module Kafka");
    shell.execute("module uninstall --member-role zookeeper --timeout 20000 --module Zookeeper");
  }
  
  public void destroy() throws Exception {
    shell.close();
    ringbearerServer.destroy() ;
    sparknginServer.destroy() ;
    genericServer.destroy() ; 
    for(int i  = 0; i < kafkaServer.length; i++) {
      kafkaServer[i].destroy() ;
    }
    zkServer.destroy() ;
  }
  
  static public void main(String[] args) throws Exception {
    UseCaseClusterDemo cluster = new UseCaseClusterDemo() ;
    //Launch the empty servers
    cluster.init() ; 
    //Install the services on the servers
    cluster.install();
    System.out.println("\n\n") ;
    System.out.println("Finish installing the services. We can start sending the data to sparkngin and kafka now.");
    System.out.println("\n\n") ;
    Thread.sleep(15000);
    cluster.uninstall();
    cluster.destroy() ;
  }
}
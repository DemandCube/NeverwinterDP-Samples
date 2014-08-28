package com.neverwinterdp.sample.hello;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.ServerRegistration;
import com.neverwinterdp.server.ServerState;
import com.neverwinterdp.server.cluster.ClusterClient;
import com.neverwinterdp.server.cluster.ClusterMember;
import com.neverwinterdp.server.cluster.hazelcast.HazelcastClusterClient;
import com.neverwinterdp.server.command.ServerCommand;
import com.neverwinterdp.server.command.ServerCommandResult;
import com.neverwinterdp.server.command.ServerCommands;
import com.neverwinterdp.server.command.ServerModuleCommands;
import com.neverwinterdp.server.module.ModuleRegistration;
import com.neverwinterdp.server.module.ModuleRegistration.InstallStatus;
import com.neverwinterdp.server.module.ModuleRegistration.RunningStatus;
import com.neverwinterdp.server.service.ServiceRegistration;
import com.neverwinterdp.util.text.TabularFormater;
/**
 * @author Tuan Nguyen
 * 
 * This demo show you the low level api of the cluster framework:
 *  
 *  1. How to setup 3 servers(master, worker1, worker2) and a client.
 *  2. How to get and list the information of a server and its services info
 *  3. How to get the available modules and the status of the modules, how to install/uninstall a module
 *  4. How to implement a ping command, construct the ping command at the client side, send the command to 
 *     a or a set of servers, execute the command at the server side and return the result  
 */
public class HelloClusterDemo {
  static {
    System.out.println("By default, the log information is output to the build/demo.log file.");
    System.out.println("If you want to print the log to the console, reconfigure src/main/resources/log4j.properties");
    System.setProperty("app.dir", "build/cluster") ;
    System.setProperty("log4j.configuration", "file:src/main/resources/log4j.properties") ;
  }
  
  private Server master, worker1, worker2;
  private ClusterClient clusterClient  ;
  
  public HelloClusterDemo() {
    
  }
  
  public void init() throws Exception {
    header("Init") ;
    info("Create master server with: name = master, roles = [master]") ;
    master  = Server.create("-Pserver.name=master",   "-Pserver.roles=master") ;
    info("Create worker server with: name = worker1, roles = [worker]") ;
    worker1 = Server.create("-Pserver.name=worker1", "-Pserver.roles=worker") ;
    info("Create worker server with: name = worker2, roles = [worker]") ;
    worker2 = Server.create("-Pserver.name=worker2", "-Pserver.roles=worker") ;
    info("Create the cluster client") ;
    clusterClient = new HazelcastClusterClient() ;
  }

  public void serverCommandExample() {
    header("Sample how to get and list the server info and its service infos") ;
    //Expect to find only one member
    ClusterMember[] masterMember = clusterClient.findClusterMemberByName("master") ;
    //Construct and create the command
    ServerCommand<ServerRegistration> command = new ServerCommands.GetServerRegistration() ;
    command.setTimeout(5000);
    //Send the command to the master server and wait max 5s for the result
    ServerCommandResult<ServerRegistration> result = clusterClient.execute(command, masterMember[0]) ;
    
    //Got the result from server and print out the result
    System.out.println("Result From: " + result.getFromMember().getMemberName() + ", " + result.getFromMember());
    System.out.println("Server Info:");
    ServerRegistration reg = result.getResult() ;
    System.out.println("    Server name:  " + reg.getServerName());
    System.out.println("    Server roles: " + reg.getRoles());
    System.out.println("    Server state: " + reg.getServerState());
    System.out.println("Service Info:");
    String[] header = { "Module", "Service Id", "Service State" };
    TabularFormater formater = new TabularFormater(header);
    formater.setIndent("    ");
    for(ServiceRegistration sel : reg.getServices()) {
      formater.addRow(sel.getModule(), sel.getServiceId(), sel.getState());
    }
    System.out.println(formater.getFormatText());
  }
  
  public void moduleCommandExample() {
    header("Sample how to get and list the server info and its service infos") ;
    
    info("Run the get available module command on all the servers") ;
    ServerCommand<ModuleRegistration[]> getAvailableCommand = new ServerModuleCommands.GetAvailableModule() ;
    //Send the command to all the servers and print the results
    printModuleResults("Get Available Modules", clusterClient.execute(getAvailableCommand)) ;
    
    info("Run the install module HelloModuleDisable on the master server") ;
    List<String> moduleToInstall = Arrays.asList("HelloModuleDisable") ;
    Map<String, String> properties = new HashMap<String, String>() ;
    properties.put("hello", "override hello property") ;
    properties.put("hello:hello", "override hello map property") ;
    ServerCommand<ModuleRegistration[]> installCommand = 
        new ServerModuleCommands.InstallModule(moduleToInstall, true, properties) ;
    ClusterMember[] masterMember = clusterClient.findClusterMemberByRole("master") ;
    printModuleResults("Install HelloModuleDisable on master", clusterClient.execute(installCommand, masterMember)) ;
  
    info("Run the uninstall module HelloModule on the worker servers") ;
    ServerCommand<ModuleRegistration[]> uninstallCommand = 
        new ServerModuleCommands.UninstallModule(Arrays.asList("HelloModule")) ;
    ClusterMember[] workerMembers = clusterClient.findClusterMemberByRole("worker") ;
    printModuleResults("Uninstall HelloModule on the worker servers", clusterClient.execute(uninstallCommand, workerMembers)) ;
  
    info("Rerun the get available module command on all the servers") ;
    //Send the command to all the servers and print the results
    printModuleResults("Get Available Modules", clusterClient.execute(getAvailableCommand)) ;
  }
  
  void printModuleResults(String title, ServerCommandResult<ModuleRegistration[]>[] results) {
    for(int i = 0; i < results.length; i++) {
      ServerCommandResult<ModuleRegistration[]> sel = results[i] ;
      System.out.println(title + " on member " + sel.getFromMember());
      if(sel.hasError()) {
        System.out.println("ERROR: " + sel.getError()) ;
      } else {
        TabularFormater formater = new TabularFormater("Module", "Install", "Status");
        formater.setIndent("  ") ;
        ModuleRegistration[] mstatus = sel.getResult() ;
        for(ModuleRegistration selStatus : mstatus) {
          String moduleName = selStatus.getModuleName() ;
          //Since we have the zookeekper, kafka in the dendencies. 
          //Those module will be available as well, but not relate to the demo. So I ignore them
          if(moduleName.indexOf("Hello") < 0) continue ;
          InstallStatus installStatus = selStatus.getInstallStatus() ;
          RunningStatus runningStatus = selStatus.getRunningStatus() ;
          formater.addRow(moduleName, installStatus, runningStatus);
        }
        System.out.println(formater.getFormatText()); 
      }
    }
  }

  public void customPingCommandExample() {
    header("ping") ;
    info("Ping the master servers") ;
    customPingCommand("master") ;
    info("Ping the worker servers") ;
    customPingCommand("worker") ;
    info("Ping all the servers") ;
    customPingCommand(null) ;
  }
  
  private void customPingCommand(String serverRole) {
    //You can use the method clusterClient.findClusterMemberByName(name) or uuid to
    //find a specific server
    PingServerCommand ping = new PingServerCommand(new HashMap<String, String>(), "hello ping") ;
    //Expect the command return the result within 5s , if not a timeout exception will be raised
    ping.setTimeout(5000) ;
    ServerCommandResult<String>[] results = null ;
    if(serverRole != null) {
      //Send the command to a set of the server with the given role
      ClusterMember[] members = clusterClient.findClusterMemberByRole(serverRole) ;
      results = clusterClient.execute(ping, members) ;
    } else {
      //Send the command to all the servers
      results = clusterClient.execute(ping) ;
    }
    //Print the result
    String[] header = { "Server", "Listen IP:PORT", "Return Message" };
    TabularFormater formater = new TabularFormater(header);
    for (ServerCommandResult<String> result : results) {
      formater.addRow(result.getFromMember().getMemberName(), result.getFromMember(), result.getResult());
    }
    System.out.println(formater.getFormatText());
  }
  
  public void shutdown() throws Exception {
    ClusterMember[] masterMember = clusterClient.findClusterMemberByName("master") ;
    ServerCommand<ServerState> shutdown = new ServerCommands.Shutdown() ;
    clusterClient.execute(shutdown, masterMember) ;
  }
  
  public void destroy() throws Exception {
    header("destroy") ;
    info("Destroy the cluster client") ;
    clusterClient.shutdown() ;
    info("Destroy the worker2 server") ;
    worker2.destroy() ;
    info("Destroy the worker1 server") ;
    worker1.destroy() ; 
    info("Destroy the master server") ;
    master.destroy() ; 
    info("Destroy done!!!") ;
  }
  
  private void header(String header) {
    System.out.println("************************************************************************");
    System.out.println(header);
    System.out.println("************************************************************************");
  }
  
  private void info(String message) {
    System.out.println("\n>>>> " + message + "\n");
  }
  
  public static void main(String[] params) throws Exception {
    HelloClusterDemo cluster = new HelloClusterDemo() ;
    cluster.init(); 
    cluster.serverCommandExample() ;
    cluster.moduleCommandExample() ;
    cluster.customPingCommandExample() ;
    cluster.destroy();
  }
}
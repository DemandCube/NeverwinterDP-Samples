package com.neverwinterdp.sample.shell;

import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.shell.Shell;
/**
 * @author Tuan Nguyen
 * 
 * This demo show you how to use the shell client to interact with the cluster:
 *  1. How to setup 3 servers(master, worker1, worker2) and a client.
 *  2. How to list the information of a server and its services info
 *  3. How to list available modules and the status of the modules, how to install/uninstall a module
 *  4. How to ping the servers  
 */
public class HelloShellDemo {
  static {
    System.out.println("By default, the log information is output to the build/demo.log file.");
    System.out.println("If you want to print the log to the console, reconfigure src/main/resources/log4j.properties");
    System.setProperty("app.dir", "build/cluster") ;
    System.setProperty("log4j.configuration", "file:src/main/resources/log4j.properties") ;
  }
  
  private Server master, worker1, worker2;
  Shell shell ;
  
  public HelloShellDemo() {
    
  }
  
  public void init() throws Exception {
    header("Init") ;
    info("Create master server with: name = master, roles = [master]") ;
    master  = Server.create("-Pserver.name=master",   "-Pserver.roles=master") ;
    info("Create worker server with: name = worker1, roles = [worker]") ;
    worker1 = Server.create("-Pserver.name=worker1", "-Pserver.roles=worker") ;
    info("Create worker server with: name = worker2, roles = [worker]") ;
    worker2 = Server.create("-Pserver.name=worker2", "-Pserver.roles=worker") ;
    info("Create the shell client") ;
    shell = new Shell() ;
    shell.getShellContext().connect();
  }

  public void serverCommandExample() {
    header("Sample how to get and list the server info and its service infos") ;
    shell.exec("server registration");
  }
  
  public void moduleCommandExample() {
    header("Sample how to get and list the module info and its service infos") ;

    info("List the installed modules on all the servers") ;
    shell.exec("module list-installed");
    
    info("Install the module HelloModuleDisable on the master server") ;
    //A bug that does not allow string with spaces for the properties. This should be fixed
    shell.exec(
      "module install " + 
      "  -Phello=\"override_hello_property\"" +
      "  -Phello:hello=\"override_hello_map_property\"" +
      "  --member-role master --autostart --module HelloModuleDisable"
    );
    
    info("Uninstall the module HelloModule on the worker server") ;
    shell.exec(
      "module uninstall --member-role worker --module HelloModule"
    );
    
    info("List the installed modules on all the servers again") ;
    shell.exec("module list-installed");
  }
  
  public void pingCommandExample() {
    header("Ping command example") ;
    shell.exec("server ping");
  }
  
  public void shutdown() throws Exception {
    shell.exec("server shutdown --member-role master");
    shell.exec("server shutdown --member-role worker");
    //Or call shutdown all the server in the cluster
    //shell.exec("server shutdown");
  }
  
  public void destroy() throws Exception {
    //The only way to destroy a server from the shell send an exit command to the server, 
    //and the server will call destroy and System.exit() method
    //but since all the servers are running in the same jvm now, it doesn't make sense to  send the exit command
    shell.close() ;
    Thread.sleep(1000);
    worker2.destroy() ;
    worker1.destroy() ; 
    master.destroy() ; 
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
    HelloShellDemo cluster = new HelloShellDemo() ;
    cluster.init(); 
    cluster.serverCommandExample() ;
    cluster.moduleCommandExample() ;
    cluster.pingCommandExample() ;
    cluster.destroy();
  }
}
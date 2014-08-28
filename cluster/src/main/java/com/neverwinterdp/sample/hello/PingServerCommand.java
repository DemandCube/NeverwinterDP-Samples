package com.neverwinterdp.sample.hello;

import java.util.Map;

import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.command.ServerCommand;

/**
 * @author Tuan Nguyen
 * 
 * The command is:
 * - A serializable object that encapsulates all the request parameters in an object
 * - The command can be saving in a queue or sending to a server/service to be executed 
 * 
 * In the cluster framework, the server and service command is used as the following diagram
 *
 *             -------------------------------------------------------
 *             |        Send the command to a server or              |
 *             |        a set of the servers                         |
 *             |                                           --------------------------
 *             |                                           |      Server 1           |
 *    ---------------------                                |-------------------------|
 *    |     Client        |                                |      Server 2           |
 *    |-------------------|                                |-------------------------|
 *    | Construct command |                                |      .........          |
 *    | With the params   |                                |-----------------------  |
 *    |                   |                                |  Server call            |
 *    --------------------|                                | command.execute(server) |
 *           |                                             |                         |
 *           |                                             |                         |
 *           |                                             ---------------------------
 *           |                    Return the result                    |
 *           |---------------------------------------------------------|
 *  
 *  
 */
public class PingServerCommand extends ServerCommand<String> {
  /**
   * The customed request prameters. Note that the customed parameters has to be serializable
   */
  private Map<String, String> params ;
  
  /**
   * Another customed string parameter
   */
  private String message ;
  
  public PingServerCommand(Map<String, String> params, String message) {
    this.params = params ;
    this.message = message ;
  }
  
  /**
   * This method is invoked at the server side and performed a customed action
   * @param  The server is the container object that carry all the informations such server name, role, environment
   *         and the services
   * @return The return result has to be a serializable object and it will be sended back to the client
   */
  public String execute(Server server) throws Exception {
    return "Server " + server.getServerRegistration().getServerName() + " got the ping message: " + message;
  }
}
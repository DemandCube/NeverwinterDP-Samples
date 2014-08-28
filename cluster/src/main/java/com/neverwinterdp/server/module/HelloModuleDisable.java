package com.neverwinterdp.server.module;

import java.util.Map;

import com.neverwinterdp.sample.hello.HelloService;

/**
 * @author Tuan
 * This module class is used to configure the available services for the module. The default parameters and the default 
 * action such auto install when the module is installed, auto start all the services in the module when the module is
 * installed
 * 
 * When a server start, it will scan for all the classes in the package com.neverwinterdp.server.module that have the
 * ModuleConfig annotation, register and install according to the configuration
 */
@ModuleConfig(name = "HelloModuleDisable", autoInstall = false, autostart = false) 
public class HelloModuleDisable extends ServiceModule {
  protected void configure(Map<String, String> properties) {  
    properties.put("hello", "hello property") ;
    properties.put("hello:hello", "hello map property") ;
    
    bind("HelloService", HelloService.class); 
    bind("HelloServiceInstance", new HelloService()); ;
  }
}
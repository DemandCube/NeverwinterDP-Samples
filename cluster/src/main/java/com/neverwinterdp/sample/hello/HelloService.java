package com.neverwinterdp.sample.hello;

import java.util.Map;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.neverwinterdp.server.service.AbstractService;
import com.neverwinterdp.util.LoggerFactory;
import com.neverwinterdp.yara.Counter;
import com.neverwinterdp.yara.MetricRegistry;
import com.neverwinterdp.yara.Timer;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 * 
 * This is a sample service that can be deployed into a module and managed by the server
 * 
 * @see com.neverwinterdp.server.module.HelloModule
 * @see com.neverwinterdp.server.module.HelloModuleDisable
 */
public class HelloService extends AbstractService {
  private Logger logger ;
  
  @Inject @Named("hello")
  private String   helloProperty;
  
  @Inject(optional=true) @Named("helloProperties")
  private Map<String, String>   helloProperties;
  
  private MetricRegistry metricRegistry ;

  public String getHelloProperty() { return helloProperty ; }
  
  public Map<String, String> getHelloProperties() {
    return this.helloProperties ;
  }
  
  @Inject
  public void setLoggerFactory(LoggerFactory lfactory) {
    logger = lfactory.getLogger("HelloService") ;
  }
  
  @Inject
  public void init(MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry ;
  }

  @PreDestroy
  public void onDestroy() {
    logger.info("onDestroy()");
  }
  
  public void start() {
    logger.info("Start start()");
    logger.info("Activating the HelloService...................");
    logger.info("Finish start()");
  }

  public void stop() {
    logger.info("Start stop()");
    logger.info("Stopping the HelloService......................");
    logger.info("Finish stop()");
  }

  /**
   * Sample service that provide by the HelloService
   * @param message
   * @return
   */
  public String hello(String message) {
    Timer helloTimer = metricRegistry.timer("hello-counter") ;
    Timer.Context ctx = helloTimer.time() ;
    Counter helloCounter = metricRegistry.counter("hello-timer") ;
    helloCounter.incr();
    ctx.stop() ;
    return "Hello " + message;
  }
}
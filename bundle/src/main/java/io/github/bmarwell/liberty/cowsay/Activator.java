package io.github.bmarwell.liberty.cowsay;

import com.github.ricksbrown.cowsay.plugin.CowExecutor;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import java.util.Dictionary;

public class Activator implements BundleActivator, ManagedService {

  private static final Logger LOGGER = Logger.getLogger(Activator.class.getName());

  private ServiceRegistration<ManagedService> configRef;

  private String startmessage;

  private String startcowfile = "cow";

  @Override
  public void start(BundleContext context) throws Exception {
    configRef = context.registerService(ManagedService.class, this, this.getConfigDefaults());

    emitStartMessage();
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    this.configRef.unregister();
  }

  @Override
  public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
    if (properties == null) {
      return;
    }

    Object startcowfile = properties.get("startcowfile");
    if (startcowfile instanceof String) {
      this.startcowfile = (String) startcowfile;
    }

    Object startmessage = properties.get("startmessage");
    if (startmessage instanceof String) {
      this.startmessage = (String) startmessage;
    }

    emitStartMessage();
  }

  private void emitStartMessage()  {
    if (this.startmessage != null) {
      CowExecutor cowExecutor = new CowExecutor();
      cowExecutor.setCowfile(this.startcowfile);
      cowExecutor.setMessage(this.startmessage);
      String cowsay = cowExecutor.execute();

      for (String cowsayline : cowsay.split("\n")) {
        LOGGER.log(Level.SEVERE, cowsayline);
      }
    }
  }

  protected Dictionary<String, ?> getConfigDefaults() {
    Hashtable<String, Object> configDefaults = new Hashtable<>();
    configDefaults.put(org.osgi.framework.Constants.SERVICE_PID, "cowsay");
    configDefaults.put("startcowfile", "cow");

    return configDefaults;
  }
}

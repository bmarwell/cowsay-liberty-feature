/*
 * Copyright 2022-2022 the cowsay-liberty-feature team @ https://github.com/bmarwell/cowsay-liberty-feature
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.bmarwell.liberty.cowsay;

import com.github.ricksbrown.cowsay.plugin.CowExecutor;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class Activator implements BundleActivator, ManagedService {

  private static final Logger LOGGER = Logger.getLogger(Activator.class.getName());

  private static final String LOGGING_FORMAT = "COWSY%4d%1s:";

  private ServiceRegistration<ManagedService> configRef;

  private final Lock lock = new ReentrantLock();

  private String startmessage;
  private String startcowfile = "cow";

  private String stopmessage;
  private String stopcowfile = "cow";

  private BundleContext bundleContext;

  @Override
  public void start(BundleContext bundleContext) throws Exception {
    this.bundleContext = bundleContext;
    configRef = bundleContext.registerService(ManagedService.class, this, this.getConfigDefaults());
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    emitStopMessage();

    this.configRef.unregister();
  }

  @Override
  public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
    if (properties == null) {
      return;
    }

    String[] startmessage = (String[]) properties.get("startmessage");
    String[] stopmessage = (String[]) properties.get("stopmessage");

    // Get the configuration admin service
    ConfigurationAdmin configAdmin = null;
    ServiceReference<?> configurationAdminReference = this.bundleContext.getServiceReference(ConfigurationAdmin.class.getName());

    if (configurationAdminReference != null) {
      configAdmin = (ConfigurationAdmin) this.bundleContext.getService(configurationAdminReference);
    }

    if (startmessage != null && startmessage.length > 0) {
      try {
        Configuration config = configAdmin.getConfiguration(startmessage[0], null);
        this.startmessage = (String) config.getProperties().get("message");
        String cowfile = (String) config.getProperties().get("cowfile");
        if (cowfile != null) {
          this.startcowfile = cowfile;
        }
      } catch (IOException ioException) {
        LOGGER.log(Level.SEVERE, "Problem reading startmessage data.", ioException);
      }
    }

    if (stopmessage != null && stopmessage.length > 0) {
      try {
        Configuration config = configAdmin.getConfiguration(stopmessage[0], null);
        this.stopmessage = (String) config.getProperties().get("message");
        String cowfile = (String) config.getProperties().get("cowfile");
        if (cowfile != null) {
          this.stopcowfile = cowfile;
        }
      } catch (IOException ioException) {
        LOGGER.log(Level.SEVERE, "Problem reading stopmessage data.", ioException);
      }
    }

    emitStartMessage();
  }

  private void emitStartMessage() {
    if (this.startmessage != null) {
      CowExecutor cowExecutor = new CowExecutor();
      cowExecutor.setCowfile(this.startcowfile);
      cowExecutor.setMessage(this.startmessage);
      logCowExecutor(cowExecutor);
    }
  }

  private void emitStopMessage() {
    if (this.stopmessage != null) {
      CowExecutor cowExecutor = new CowExecutor();
      cowExecutor.setCowfile(this.stopcowfile);
      cowExecutor.setMessage(this.stopmessage);
      logCowExecutor(cowExecutor);
    }
  }

  private void logCowExecutor(CowExecutor cowExecutor) {
    String cowsay = cowExecutor.execute();

    try {
      lock.lock();
      for (String cowsayline : cowsay.split("\n")) {
        LOGGER.log(Level.SEVERE, () -> String.format(LOGGING_FORMAT, 1, "I").replaceAll(" ", "0") + " " + cowsayline);
      }
    } finally {
      lock.unlock();
    }
  }

  protected Dictionary<String, ?> getConfigDefaults() {
    Hashtable<String, Object> configDefaults = new Hashtable<>();
    configDefaults.put(org.osgi.framework.Constants.SERVICE_PID, "cowsay");

    return configDefaults;
  }
}

package org.apache.taverna.workbench.ui.impl;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.taverna.configuration.app.ApplicationConfiguration;
import org.apache.taverna.configuration.app.impl.ApplicationConfigurationImpl;
import org.apache.taverna.plugin.Plugin;
import org.apache.taverna.plugin.PluginException;
import org.apache.taverna.plugin.PluginManager;
import org.apache.taverna.plugin.xml.jaxb.PluginVersions;
import org.apache.taverna.security.credentialmanager.CMException;
import org.apache.taverna.security.credentialmanager.CredentialManager;
import org.apache.taverna.security.credentialmanager.impl.CredentialManagerImpl;
import org.apache.taverna.servicedescriptions.ServiceDescriptionProvider;
import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;
import org.apache.taverna.servicedescriptions.impl.ServiceDescriptionRegistryImpl;
import org.apache.taverna.services.ServiceRegistry;
import org.apache.taverna.services.impl.ServiceRegistryImpl;
import org.apache.taverna.ui.menu.DefaultMenuBar;
import org.apache.taverna.ui.menu.DefaultToolBar;
import org.apache.taverna.ui.menu.MenuComponent;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.ui.menu.impl.MenuManagerImpl;
import org.apache.taverna.ui.perspectives.design.DesignPerspective;
import org.apache.taverna.ui.perspectives.results.ResultsPerspective;
import org.apache.taverna.workbench.ShutdownSPI;
import org.apache.taverna.workbench.StartupSPI;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.edits.impl.EditManagerImpl;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.impl.FileManagerImpl;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.selection.impl.SelectionManagerImpl;
import org.apache.taverna.workbench.ui.credentialmanager.startup.InitialiseSSLStartupHook;
import org.apache.taverna.workbench.ui.credentialmanager.startup.SetCredManAuthenticatorStartupHook;
import org.apache.taverna.workbench.ui.servicepanel.ServicePanelComponentFactory;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.impl.ContextualViewFactoryRegistryImpl;
import org.apache.taverna.workbench.ui.views.contextualviews.impl.ContextualViewComponentFactory;
import org.apache.taverna.workbench.ui.zaria.PerspectiveSPI;
import org.apache.taverna.workbench.ui.zaria.UIComponentFactorySPI;

public class WorkbenchTest {

	public static void main(String[] args) throws InterruptedException {
		System.setProperty("taverna.app.startup", Paths.get("target").toAbsolutePath().toString());
		
		WorkbenchImpl w = new WorkbenchTest().getWorkbench();
		w.initialize();
		synchronized(w) {
			// It won't actually get interrupted, but it means we persist 
			// as long as w rather than exiting
			w.wait();
		}
	}

	private EditManagerImpl editManagerImpl;
	private FileManagerImpl fileManager;
	private WorkbenchImpl workbench;
	private MenuManagerImpl menuManager;
	private SelectionManagerImpl selectionManager;
	private List<MenuComponent> menuComponents;
	private CredentialManagerImpl credentialManager;
	private ContextualViewFactoryRegistryImpl registry;
	private ServicePanelComponentFactory servicePanelFactory;

	public FileManager getFileManager() {
		if (fileManager == null) {
			fileManager = new FileManagerImpl(getEditManager());
		}
		return fileManager;
	}

	public EditManager getEditManager() {
		if (editManagerImpl == null) {
			editManagerImpl = new EditManagerImpl();
		}
		return editManagerImpl;
	}

	public MenuManager getMenuManager() {
		if (menuManager == null) {
			menuManager = new MenuManagerImpl();
			menuManager.setSelectionManager(getSelectionManager());

			menuManager.setMenuComponents(getMenuComponents());

		}
		return menuManager;
	}

	public List<MenuComponent> getMenuComponents() {
		if (menuComponents == null) {
			menuComponents = new ArrayList<>();
			menuComponents.add(new DefaultMenuBar());
			menuComponents.add(new DefaultToolBar());
			
//			for (MenuComponent mc : ServiceLoader.load(MenuComponent.class)) {
//				try {
//					BeanUtils.copyProperties(this, mc);
//					// This does not work as many of the actions are set in  constructor
					// rather than with setters
//				} catch (IllegalAccessException | InvocationTargetException e) {
//					e.printStackTrace();
//				}
//				menuComponents.add(mc);
//			}
		}
		// FIXME: Should not be an empty list
		return menuComponents;
	}

	public SelectionManager getSelectionManager() {
		if (selectionManager == null) {
			selectionManager = new SelectionManagerImpl();
			selectionManager.setEditManager(getEditManager());
			selectionManager.setFileManager(getFileManager());
			selectionManager.setPerspectives(getPerspectives());
		}
		return selectionManager;
	}

	public ResultsPerspective getResultsPerspective() {
		ResultsPerspective perspective = new ResultsPerspective();
		// TODO: Various setters
		return perspective;
	}

	public DesignPerspective getDesignPerspective() {
		DesignPerspective p = new DesignPerspective();
		p.setEditManager(getEditManager());
		p.setFileManager(getFileManager());
		p.setMenuManager(getMenuManager());
		p.setContextualViewComponentFactory(getContextualViewComponentFactory());
		p.setServicePanelComponentFactory(getServicePanelComponentFactory());
		
		// TODO: More setters
		return p;
	}

	private UIComponentFactorySPI getServicePanelComponentFactory() {
		if (servicePanelFactory == null) {
			servicePanelFactory = new ServicePanelComponentFactory();		
			servicePanelFactory.setEditManager(getEditManager());
			servicePanelFactory.setMenuManager(getMenuManager());
			servicePanelFactory.setSelectionManager(getSelectionManager());
			servicePanelFactory.setServiceDescriptionRegistry(getServiceDescriptionRegistry());
			servicePanelFactory.setServiceRegistry(getServiceRegistry());
		}
		
		return servicePanelFactory;
	}

	public ServiceRegistry getServiceRegistry() {
		return new ServiceRegistryImpl();
	}

	public ServiceDescriptionRegistry getServiceDescriptionRegistry() {
		ServiceDescriptionRegistryImpl serviceDescriptionRegistryImpl = new ServiceDescriptionRegistryImpl(getApplicationConfiguration());		
		List<ServiceDescriptionProvider> serviceProviders = new ArrayList<>();
		for (ServiceDescriptionProvider sdp : ServiceLoader.load(ServiceDescriptionProvider.class)) {
			try {
				BeanUtils.copyProperties(this, sdp);
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
			serviceProviders.add(sdp);
		}
		
		serviceDescriptionRegistryImpl.setServiceDescriptionProvidersList(serviceProviders);
		return serviceDescriptionRegistryImpl;
	}

	public UIComponentFactorySPI getContextualViewComponentFactory() {
		ContextualViewComponentFactory contextualViewComponentFactory = new ContextualViewComponentFactory();
		contextualViewComponentFactory.setEditManager(getEditManager());
		contextualViewComponentFactory.setSelectionManager(getSelectionManager());
		contextualViewComponentFactory.setContextualViewFactoryRegistry(getContextualViewFactoryRegistry());
		return contextualViewComponentFactory;
	}

	public ContextualViewFactoryRegistry getContextualViewFactoryRegistry() {
		if (registry == null) { 
			registry = new ContextualViewFactoryRegistryImpl();
			List<ContextualViewFactory<?>> views = new ArrayList<>();
			for (ContextualViewFactory view : ServiceLoader.load(ContextualViewFactory.class)) {
				try {
					BeanUtils.copyProperties(this, view);
				} catch (Exception e) {
					e.printStackTrace();
				}
				views.add(view);
			}			
			registry.setContextualViewFactories(views);
		}
		return registry;
	}

	public List<PerspectiveSPI> getPerspectives() {
		return Arrays.asList(getDesignPerspective(), getResultsPerspective());
	}

	public WorkbenchImpl getWorkbench() {
		if (workbench == null) {
			workbench = new WorkbenchImpl(getStartupHooks(), getShutdownHooks(), getPerspectives());
			workbench.setApplicationConfiguration(getApplicationConfiguration());
			workbench.setEditManager(getEditManager());
			workbench.setFileManager(getFileManager());
			workbench.setMenuManager(getMenuManager());
			workbench.setSelectionManager(getSelectionManager());
			workbench.setPluginManager(getPluginManager());			
			workbench.setWorkbenchConfiguration(getWorkbenchConfiguration());
		}
		return workbench;
	}

	private WorkbenchConfiguration getWorkbenchConfiguration() {
		return new WorkbenchConfiguration() {
			@Override
			public Map<String, String> getDefaultPropertyMap() {
				return Collections.emptyMap();
			}
			@Override
			public String getUUID() {
				return "5c51f0d0-91b7-4d90-92e4-4416c61f6442";
			}
			@Override
			public String getDisplayName() {
				return "Taverna Test";
			}
			@Override
			public String getFilePrefix() {
				return "taverna-test";
			}
			@Override
			public String getCategory() {
				return "test";
			}
			@Override
			public void restoreDefaults() {
			}

			@Override
			public String getDefaultProperty(String key) {
				return null;
			}

			@Override
			public Set<String> getKeys() {
				return Collections.emptySet();
			}

			@Override
			public void clear() {
			}

			@Override
			public Map<String, String> getInternalPropertyMap() {
				return Collections.emptyMap();
			}

			@Override
			public String getProperty(String key) {
				return null;
			}

			@Override
			public void setProperty(String key, String value) {								
				// We'll just forget it anyway..
			}

			@Override
			public void deleteProperty(String key) {
			}

			@Override
			public List<String> getPropertyStringList(String key) {
				return Collections.emptyList();
			}

			@Override
			public void setPropertyStringList(String key, List<String> value) {
			}

			@Override
			public boolean getCaptureConsole() {
				return false;
			}

			@Override
			public void setCaptureConsole(boolean captureConsole) {
			}

			@Override
			public boolean getWarnInternalErrors() {
				return false;
			}

			@Override
			public void setWarnInternalErrors(boolean warnInternalErrors) {
			}

			@Override
			public int getMaxMenuItems() {
				return 1024;
			}

			@Override
			public void setMaxMenuItems(int maxMenuItems) {
			}

			@Override
			public String getDotLocation() {
				// Assume graphviz' dot is on PATH
				return "dot";
			}

			@Override
			public void setDotLocation(String dotLocation) {
			}
			
		};
	}

	private PluginManager getPluginManager() {
		// Can't install plugins while outside OSGi
		return new PluginManager() {			
			@Override
			public Plugin updatePlugin(PluginVersions pluginVersions) throws PluginException {
				throw new PluginException("Can't install plugins when running outside OSGi");
			}
			@Override
			public void loadPlugins() throws PluginException {
			}
			@Override
			public Plugin installPlugin(String pluginSiteURL, String pluginFile) throws PluginException {
				throw new PluginException("Can't install plugins when running outside OSGi");
			}
			@Override
			public Plugin installPlugin(Path pluginFile) throws PluginException {
				throw new PluginException("Can't install plugins when running outside OSGi");			}
			
			@Override
			public List<PluginVersions> getPluginUpdates() throws PluginException {
				return Collections.emptyList();
			}
			@Override
			public List<Plugin> getInstalledPlugins() throws PluginException {
				return Collections.emptyList();
			}
			@Override
			public List<PluginVersions> getAvailablePlugins() throws PluginException {
				return Collections.emptyList();
			}
			@Override
			public void checkForUpdates() throws PluginException {				
			}
		};
	}

	public List<StartupSPI> getStartupHooks() {
		SetCredManAuthenticatorStartupHook credManAuthenticatorStartupHook = new SetCredManAuthenticatorStartupHook();
		credManAuthenticatorStartupHook.setCredentialManager(getCredentialManager());
		InitialiseSSLStartupHook initialiseSSLStartupHook = new InitialiseSSLStartupHook();
		initialiseSSLStartupHook.setCredentialManager(getCredentialManager());
		return Arrays.asList(initialiseSSLStartupHook, credManAuthenticatorStartupHook);
	}

	public CredentialManager getCredentialManager() {
		if (credentialManager == null) {
			try {
				credentialManager = new CredentialManagerImpl();			
			} catch (CMException e) {
				e.printStackTrace();
			}
		}
		return credentialManager;
	}

	public List<ShutdownSPI> getShutdownHooks() {
		return Arrays.asList();
	}

	public ApplicationConfiguration getApplicationConfiguration() {
		return new ApplicationConfigurationImpl();
	}
	
}

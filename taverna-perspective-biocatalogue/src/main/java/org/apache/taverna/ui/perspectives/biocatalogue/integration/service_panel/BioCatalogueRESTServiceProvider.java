package org.apache.taverna.ui.perspectives.biocatalogue.integration.service_panel;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import org.apache.log4j.Logger;

//import net.sf.taverna.t2.activities.rest.ui.servicedescription.RESTActivityIcon;
import org.apache.taverna.servicedescriptions.AbstractConfigurableServiceProvider;
import org.apache.taverna.servicedescriptions.impl.ServiceDescriptionRegistryImpl;

/**
 * Service provider for REST service added to the Service Panel through the
 * BioCatalogue perspective.
 * 
 * @author Alex Nenadic
 */
public class BioCatalogueRESTServiceProvider extends
	AbstractConfigurableServiceProvider<RESTFromBioCatalogueServiceDescription> {

	public static final String PROVIDER_NAME = "Service Catalogue - selected services";
	  
	private static final URI providerId = URI
	.create("http://taverna.sf.net/2010/service-provider/servicecatalogue/rest");
	
	private static Logger logger = Logger.getLogger(BioCatalogueRESTServiceProvider.class);

	public BioCatalogueRESTServiceProvider(
			RESTFromBioCatalogueServiceDescription restServiceDescription) {
		super(restServiceDescription);
	}
	
	public BioCatalogueRESTServiceProvider() {
		super(new RESTFromBioCatalogueServiceDescription());
	}
	
	@Override
	protected List<? extends Object> getIdentifyingData() {
		return getConfiguration().getIdentifyingData();
	}

	@Override
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
	    callBack.status("Starting Service Catalogue REST Service Provider");
		registerNewRESTMethod(getConfiguration(), callBack);
	}

	@Override
	public Icon getIcon() {
//		return RESTActivityIcon.getRESTActivityIcon();
		return getConfiguration().getIcon();
	}

	@Override
	public String getId() {
		return providerId.toString();
	}

	@Override
	public String getName() {
		return "Service Catalogue REST";
	}
	
	@Override
	public String toString() {
		return "Service Catalogue REST service " + getConfiguration().getName();
	}
	
	public static boolean registerNewRESTMethod(
			RESTFromBioCatalogueServiceDescription restServiceDescription,
			FindServiceDescriptionsCallBack callBack)	{
		if (callBack == null) {
			// We are not adding service through a callback and
			// findServiceDescriptionsAsync() -
			// we are adding directly from the BioCatalogue perspective.
			ServiceDescriptionRegistryImpl serviceDescriptionRegistry = ServiceDescriptionRegistryImpl
					.getInstance();
			serviceDescriptionRegistry
					.addServiceDescriptionProvider(new BioCatalogueRESTServiceProvider(
							restServiceDescription));
			return true;
		} else {
			{
				// Add the REST method to the Service Panel through the callback
				callBack.partialResults(Collections
						.singletonList(restServiceDescription));
				callBack.finished();
				return (true);
			}
		}
	}

}

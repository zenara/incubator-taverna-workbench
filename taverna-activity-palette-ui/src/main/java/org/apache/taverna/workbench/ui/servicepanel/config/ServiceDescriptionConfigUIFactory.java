/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.ui.servicepanel.config;

import javax.swing.JPanel;
import org.apache.taverna.configuration.Configurable;
import org.apache.taverna.configuration.ConfigurationUIFactory;

import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;
import org.apache.taverna.servicedescriptions.ServiceDescriptionsConfiguration;

public class ServiceDescriptionConfigUIFactory implements ConfigurationUIFactory {
	private ServiceDescriptionsConfiguration serviceDescriptionsConfiguration;
	private ServiceDescriptionRegistry serviceDescriptionRegistry;

	@Override
	public boolean canHandle(String uuid) {
		return uuid.equals(serviceDescriptionsConfiguration.getUUID());
	}

	@Override
	public Configurable getConfigurable() {
		return serviceDescriptionsConfiguration;
	}

	@Override
	public JPanel getConfigurationPanel() {
		return new ServiceDescriptionConfigPanel(serviceDescriptionsConfiguration, serviceDescriptionRegistry);
	}

	public void setServiceDescriptionRegistry(ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

	public void setServiceDescriptionsConfiguration(ServiceDescriptionsConfiguration serviceDescriptionsConfiguration) {
		this.serviceDescriptionsConfiguration = serviceDescriptionsConfiguration;
	}
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

package org.apache.taverna.workflow.edits;

import static org.apache.taverna.scufl2.api.common.Scufl2Tools.NESTED_WORKFLOW;

import java.util.List;

import org.apache.taverna.workbench.edits.CompoundEdit;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.InputActivityPort;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;
import org.apache.taverna.scufl2.api.profiles.ProcessorInputPortBinding;
import org.apache.taverna.scufl2.api.profiles.Profile;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Adds an input port to a workflow.
 * 
 * @author David Withers
 */
public class AddWorkflowInputPortEdit extends AbstractEdit<Workflow> {
	private final InputWorkflowPort port;
	private final CompoundEdit nestedPortEdit = new CompoundEdit();

	public AddWorkflowInputPortEdit(Workflow workflow, InputWorkflowPort port) {
		super(workflow);
		this.port = port;
		WorkflowBundle workflowBundle = workflow.getParent();
		if (workflowBundle != null)
			for (Profile profile : workflowBundle.getProfiles())
				for (Activity activity : profile.getActivities())
					if (activity.getType().equals(NESTED_WORKFLOW))
						for (Configuration c : scufl2Tools.configurationsFor(
								activity, profile))
							defineEditsForOneConfiguration(workflow, port,
									workflowBundle, activity, c);
	}

	private void defineEditsForOneConfiguration(Workflow workflow,
			InputWorkflowPort port, WorkflowBundle workflowBundle,
			Activity activity, Configuration c) {
		List<Edit<?>> edits = nestedPortEdit.getChildEdits();
		JsonNode nested = c.getJson().get("nestedWorkflow");
		Workflow nestedWorkflow = workflowBundle.getWorkflows().getByName(
				nested.asText());

		if (nestedWorkflow == workflow) {
			InputActivityPort activityPort = new InputActivityPort();
			activityPort.setName(port.getName());
			activityPort.setDepth(port.getDepth());
			edits.add(new AddChildEdit<>(activity, activityPort));

			for (ProcessorBinding binding : scufl2Tools
					.processorBindingsToActivity(activity)) {
				Processor processor = binding.getBoundProcessor();
				InputProcessorPort processorPort = new InputProcessorPort();
				processorPort.setName(port.getName());
				processorPort.setDepth(port.getDepth());
				edits.add(new AddProcessorInputPortEdit(processor,
						processorPort));

				ProcessorInputPortBinding portBinding = new ProcessorInputPortBinding();
				portBinding.setBoundProcessorPort(processorPort);
				portBinding.setBoundActivityPort(activityPort);
				edits.add(new AddChildEdit<>(binding, portBinding));
			}
		}
	}

	@Override
	protected void doEditAction(Workflow workflow) throws EditException {
		workflow.getInputPorts().addWithUniqueName(port);
		port.setParent(workflow);
		nestedPortEdit.doEdit();
	}

	@Override
	protected void undoEditAction(Workflow workflow) {
		port.setParent(null);
		nestedPortEdit.undo();
	}
}

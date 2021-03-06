package org.apache.taverna.workbench.views.results.workflow;
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

import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.taverna.workbench.views.results.workflow.WorkflowResultTreeNode.ResultTreeNodeState.RESULT_LIST;
import static org.apache.taverna.workbench.views.results.workflow.WorkflowResultTreeNode.ResultTreeNodeState.RESULT_REFERENCE;
import static org.apache.taverna.workbench.views.results.workflow.WorkflowResultTreeNode.ResultTreeNodeState.RESULT_WAITING;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import org.apache.taverna.workbench.views.results.workflow.WorkflowResultTreeNode.ResultTreeNodeState;

import org.apache.log4j.Logger;

import org.apache.taverna.databundle.DataBundles;
import org.apache.taverna.platform.report.ReportListener;
import org.apache.taverna.platform.report.State;

public class WorkflowResultTreeModel extends DefaultTreeModel implements ReportListener {
	private static final long serialVersionUID = 7154527821423588046L;
	private static final Logger logger = Logger.getLogger(WorkflowResultTreeModel.class);

	/** Name of the output port this class models results for */
	private String portName;
	int depthSeen = -1;

	public WorkflowResultTreeModel(String portName) {
		super(new WorkflowResultTreeNode(ResultTreeNodeState.RESULT_TOP));
		this.portName = portName;
	}

	@Override
	public void outputAdded(final Path path, final String portName, final int[] index) {
		// Don't slow down workflow execution, do it in GUI thread
		invokeLater(new Runnable() {
			@Override
			public void run() {
				resultTokenProducedGui(path, portName, index);
			}
		});
	}

	@Override
	public void stateChanged(State oldState, State newState) {
		// TODO Auto-generated method stub
	}

	public void resultTokenProducedGui(Path path, String portName, int[] index) {
		if (!this.portName.equals(portName))
			return;
		if (depthSeen == -1)
			depthSeen = index.length;
		else if (index.length < depthSeen)
			return;

		Path reference = path;
		if (!DataBundles.isList(reference)) {
			insertNewDataTokenNode(reference, index);
			return;
		}

		try {
			WorkflowResultTreeNode parent = (WorkflowResultTreeNode) getRoot();
			parent = getChildAt(parent,0);
			changeState(parent, RESULT_LIST);
			for (int i = 0; i < index.length; i++) {
				parent = getChildAt(parent, index[i]);
				changeState(parent, RESULT_LIST);
			}

			List<Path> list = DataBundles.getList(reference);
			int[] elementIndex = new int[index.length + 1];
			for (int indexElement = 0; indexElement < index.length; indexElement++)
				elementIndex[indexElement] = index[indexElement];
			int c = 0;
			for (Path id : list) {
				elementIndex[index.length] = c;
				resultTokenProducedGui(id, portName, elementIndex);
				c++;
			}
			//if (c == 0) {
			//	parent.setUserObject("Empty list (depth=" + reference.getDepth() + ")" + reference.getLocalPart());
			//	nodeChanged(parent);
			//}
		} catch (NullPointerException | IOException e) {
			logger.error("Error resolving data entity list " + reference, e);
		}
	}

	public void insertNewDataTokenNode(Path reference, int[] index) {
		WorkflowResultTreeNode parent = (WorkflowResultTreeNode) getRoot();
		if (DataBundles.isError(reference)) {
			parent = getChildAt(parent, 0);
			for (int i = 0; i < index.length - 1; i++) {
				parent = getChildAt(parent, index[i]);
				parent = getChildAt(parent, 0);
				changeState(parent, RESULT_LIST);
			}
			if (index.length > 0) {
				WorkflowResultTreeNode child = getChildAt(parent,
						index[index.length - 1]);
				updateNodeWithData(child, reference);
			} else
				updateNodeWithData(parent, reference);
		 } else {
			int depth = index.length;
			if (depth == 0) {
				WorkflowResultTreeNode child = getChildAt(parent, 0);
				updateNodeWithData(child, reference);
			} else {
				parent = getChildAt(parent, 0);
				changeState(parent, RESULT_LIST);
				for (int indexElement = 0; indexElement < depth; indexElement++) {
					WorkflowResultTreeNode child = getChildAt(parent,
							index[indexElement]);
					if (indexElement == depth - 1) // leaf
						updateNodeWithData(child, reference);
					else { // list
						child.setState(RESULT_LIST);
						nodeChanged(child);
					}
					parent = child;
				}
			}
		}
	}

	private void updateNodeWithData(WorkflowResultTreeNode node, Path reference) {
		node.setState(RESULT_REFERENCE);
		node.setReference(reference);
		nodeChanged(node);
	}

	private WorkflowResultTreeNode getChildAt(WorkflowResultTreeNode parent,
			int i) {
		int childCount = getChildCount(parent);
		if (childCount <= i)
			for (int x = childCount; x <= i; x++)
				insertNodeInto(new WorkflowResultTreeNode(RESULT_WAITING),
						parent, x);
		return (WorkflowResultTreeNode) parent.getChildAt(i);
	}

	private void changeState(WorkflowResultTreeNode node, ResultTreeNodeState state) {
		if (!node.isState(state)) {
			node.setState(state);
			nodeChanged(node);
		}
	}

	// Normally used for past workflow runs where data is obtained from provenance
	public void createTree(Path path,  WorkflowResultTreeNode parentNode){
		// If reference contains a list of data references
		if (DataBundles.isList(path))
			// insert list node
			try {
				List<Path> list = DataBundles.getList(path);
				WorkflowResultTreeNode listNode = new WorkflowResultTreeNode(
						path, RESULT_LIST);
				insertNodeInto(listNode, parentNode, parentNode.getChildCount());
				for (Path ref : list)
					createTree(ref, listNode);
			} catch (IOException e) {
				logger.error("Error resolving data entity list " + path, e);
			}
		else { // reference to single data or an error
			// insert data node
			WorkflowResultTreeNode dataNode = new WorkflowResultTreeNode(path,
					RESULT_REFERENCE);
			insertNodeInto(dataNode, parentNode, parentNode.getChildCount());
		}
	}

}

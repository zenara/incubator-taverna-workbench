/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.views.graph.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.ui.menu.DesignOnlyAction;
import net.sf.taverna.t2.workbench.design.actions.AddDataflowInputAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import uk.org.taverna.scufl2.api.core.Workflow;

/**
 * An action that adds a workflow input.
 *
 * @author Alex Nenadic
 * @author Alan R Williams
 *
 */
@SuppressWarnings("serial")
public class AddWFInputAction extends AbstractAction implements DesignOnlyAction {

	private final EditManager editManager;
	private final SelectionManager selectionManager;

	public AddWFInputAction(EditManager editManager, SelectionManager selectionManager) {
		super();
		this.editManager = editManager;
		this.selectionManager = selectionManager;
		putValue(SMALL_ICON, WorkbenchIcons.inputIcon);
		putValue(NAME, "Workflow input port");
		putValue(SHORT_DESCRIPTION, "Workflow input port");
		putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		Workflow workflow = selectionManager.getSelectedWorkflow();
		new AddDataflowInputAction(workflow, null, editManager, selectionManager).actionPerformed(e);
	}


}


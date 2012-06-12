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
package net.sf.taverna.t2.workbench.views.graph.toolbar;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.views.graph.actions.SaveGraphImageAction;

/**
 *
 * @author Alex Nenadic
 *
 */
public class SaveGraphImageToolbarAction extends AbstractMenuAction {

	public static final URI SAVE_GRAPH_IMAGE_TOOLBAR_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#graphToolbarSaveGraphImage");
	private FileManager fileManager;
	private MenuManager menuManager;
	private WorkbenchConfiguration workbenchConfiguration;

	public SaveGraphImageToolbarAction() {
		super(GraphSaveToolbarSection.GRAPH_SAVE_TOOLBAR_SECTION, 10, SAVE_GRAPH_IMAGE_TOOLBAR_URI);
	}

	@Override
	protected Action createAction() {
		return new SaveGraphImageAction(fileManager, menuManager, workbenchConfiguration);
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setWorkbenchConfiguration(WorkbenchConfiguration workbenchConfiguration) {
		this.workbenchConfiguration = workbenchConfiguration;
	}

}





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
package net.sf.taverna.t2.workbench.file.impl.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.impl.actions.OpenWorkflowAction;

public class FileOpenMenuAction extends AbstractMenuAction {

	private static final URI FILE_OPEN_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileOpen");
	private final FileManager fileManager;

	public FileOpenMenuAction(FileManager fileManager) {
		super(FileOpenMenuSection.FILE_OPEN_SECTION_URI, 20, FILE_OPEN_URI);
		this.fileManager = fileManager;
	}

	@Override
	protected Action createAction() {
		return new OpenWorkflowAction(fileManager);
	}

}

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
package net.sf.taverna.t2.workbench.views.graph.menu;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.ui.menu.DesignOrResultsAction;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;


public class ZoomOutAction extends AbstractAction implements DesignOrResultsAction {

	private static Action designAction = null;
	private static Action resultsAction = null;

	public static void setResultsAction(Action resultsAction) {
		ZoomOutAction.resultsAction = resultsAction;
	}

	public static void setDesignAction(Action designAction) {
		ZoomOutAction.designAction = designAction;
	}

	ZoomOutAction() {
		super("Zoom out", WorkbenchIcons.zoomOutIcon);
		putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	public void actionPerformed(ActionEvent e) {
//		if (isWorkflowPerspective() && (designAction != null)) {
			designAction.actionPerformed(e);
//		} else if (isResultsPerspective() && (resultsAction != null)) {
//			resultsAction.actionPerformed(e);
//		}
	}

}
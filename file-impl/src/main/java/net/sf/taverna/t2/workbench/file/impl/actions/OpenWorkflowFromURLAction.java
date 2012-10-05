/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester
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
package net.sf.taverna.t2.workbench.file.impl.actions;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;

/**
 * An action for opening a workflow from a url.
 *
 * @author David Withers
 *
 */
public class OpenWorkflowFromURLAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(OpenWorkflowFromURLAction.class);

	private static Preferences prefs = Preferences.userNodeForPackage(OpenWorkflowFromURLAction.class);

	private static final String ACTION_NAME = "Open workflow location...";

	private static final String ACTION_DESCRIPTION = "Open a workflow from the web into a new workflow";

	private Component component;

	private FileManager fileManager;

	public OpenWorkflowFromURLAction(final Component component, FileManager fileManager) {
		this.component = component;
		this.fileManager = fileManager;
		putValue(SMALL_ICON, WorkbenchIcons.openurlIcon);
		putValue(NAME, ACTION_NAME);
		putValue(SHORT_DESCRIPTION, ACTION_DESCRIPTION);
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
		putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_L,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	public void actionPerformed(ActionEvent e) {
		String currentUrl = prefs.get("currentUrl", "http://");

		final String url = (String) JOptionPane.showInputDialog(component,
				"Enter the URL of a workflow definition to load",
				"Workflow URL", JOptionPane.QUESTION_MESSAGE, null, null,
				currentUrl);
		if (url != null) {
			new Thread("OpenWorkflowFromURLAction") {
				public void run() {
					openFromURL(url);
				}
			}.start();
		}
	}

	private void openFromURL(String urlString) {
		try {
			URL url = new URL(urlString);

			Object canonicalSource = fileManager.getCanonical(url);
			WorkflowBundle alreadyOpen = fileManager.getDataflowBySource(canonicalSource);
			if (alreadyOpen != null) {
				// The workflow from the same source is already opened -
				// ask the user if they want to switch to it or open another copy;

				Object[] options = { "Switch to opened", "Open new copy",
						"Cancel" };
				int iSelected = JOptionPane
						.showOptionDialog(
								null,
								"The workflow from the same location is already opened.\n"
										+ "Do you want to switch to it or open a new copy?",
								"File Manager Alert",
								JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE, null,
								options, // the titles of buttons
								options[0]); // default button title

				if (iSelected == JOptionPane.YES_OPTION) {
					fileManager.setCurrentDataflow(alreadyOpen);
					return;
				} else if (iSelected == JOptionPane.CANCEL_OPTION) {
					// do nothing
					return;
				}
				// else open the workflow as usual
			}

			fileManager.openDataflow(null, url);
			prefs.put("currentUrl", urlString);
		} catch (Exception ex) {
			logger.warn("Failed to open the workflow from url " + urlString +" \n", ex);
			JOptionPane.showMessageDialog(component,
					"Failed to open the workflow from url " + urlString + " \n" + ex.getMessage(),
					"Error!", JOptionPane.ERROR_MESSAGE);
		}
	}

}

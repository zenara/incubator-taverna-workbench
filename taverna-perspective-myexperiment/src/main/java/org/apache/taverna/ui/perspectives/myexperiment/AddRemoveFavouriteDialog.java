package org.apache.taverna.ui.perspectives.myexperiment;
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

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.HttpURLConnection;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.taverna.ui.perspectives.myexperiment.model.MyExperimentClient;
import org.apache.taverna.ui.perspectives.myexperiment.model.Resource;
import org.apache.taverna.ui.perspectives.myexperiment.model.ServerResponse;
import org.apache.taverna.ui.perspectives.myexperiment.model.Util;
import org.apache.taverna.workbench.helper.HelpEnabledDialog;

import org.apache.log4j.Logger;

/**
 * @author Sergejs Aleksejevs
 */
public class AddRemoveFavouriteDialog extends HelpEnabledDialog implements ActionListener, ComponentListener {
  // CONSTANTS
  protected static final int OPERATION_SUCCESSFUL = 1;
  protected static final int OPERATION_CANCELLED = 0;
  protected static final int OPERATION_FAILED = -1;

  // components for accessing application's main elements
  private final MainComponent pluginMainComponent;
  private final MyExperimentClient myExperimentClient;
  private final Logger logger;

  // COMPONENTS
  private JButton bAddRemoveFavourite;
  private JButton bCancel;

  // STORAGE
  private final Resource resource; // a resource which is being favourited / removed from favourites
  private final boolean bIsFavouriteBeingAdded;
  private int iOperationStatus = OPERATION_CANCELLED;
  private ServerResponse response = null;

  public AddRemoveFavouriteDialog(JFrame owner, boolean isFavouriteAdded, Resource resource, MainComponent component, MyExperimentClient client, Logger logger) {
	super(owner, isFavouriteAdded ? "Add to" : "Remove from"
	+ " favourites - \"" + resource.getTitle() + "\" "
	+ resource.getItemTypeName(), true);

	// set main variables to ensure access to myExperiment, logger and the parent component
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;

	// parameters
	this.bIsFavouriteBeingAdded = isFavouriteAdded;
	this.resource = resource;

	// set options of the 'add/remove favourite' dialog box
	this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	//this.setIconImage(new ImageIcon(MyExperimentPerspective.getLocalResourceURL("myexp_icon")).getImage());

	this.initialiseUI();
  }

  private void initialiseUI() {
	// get content pane
	Container contentPane = this.getContentPane();

	// set up layout
	contentPane.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();

	// add all components
	JLabel lInfo = new JLabel("<html><center>You are about to "
		+ (this.bIsFavouriteBeingAdded ? "add" : "remove") + " \""
		+ resource.getTitle() + "\" " + resource.getItemTypeName()
		+ (this.bIsFavouriteBeingAdded ? " to" : " from")
		+ " your favourites.<br><br>"
		+ "Do you want to proceed?</center></html>");
	c.gridx = 0;
	c.gridy = 0;
	c.anchor = GridBagConstraints.WEST;
	c.gridwidth = 2;
	c.fill = GridBagConstraints.NONE;
	c.insets = new Insets(10, 10, 10, 10);
	contentPane.add(lInfo, c);

	if (this.bIsFavouriteBeingAdded) {
	  this.bAddRemoveFavourite = new JButton("Add to Favourites");
	} else {
	  this.bAddRemoveFavourite = new JButton("Remove from Favourites");
	}
	this.bAddRemoveFavourite.setDefaultCapable(true);
	this.bAddRemoveFavourite.addActionListener(this);

	c.gridy = 1;
	c.anchor = GridBagConstraints.EAST;
	c.gridwidth = 1;
	c.fill = GridBagConstraints.NONE;
	c.weightx = 0.5;
	c.insets = new Insets(5, 5, 10, 5);
	contentPane.add(this.bAddRemoveFavourite, c);

	this.bCancel = new JButton("Cancel");
	this.bCancel.setPreferredSize(this.bAddRemoveFavourite.getPreferredSize());
	this.bCancel.addActionListener(this);
	c.gridx = 1;
	c.anchor = GridBagConstraints.WEST;
	contentPane.add(bCancel, c);

	this.pack();
	this.getRootPane().setDefaultButton(this.bAddRemoveFavourite);
	this.setMinimumSize(this.getPreferredSize());
	this.setMaximumSize(this.getPreferredSize());
	this.addComponentListener(this);
  }

  /**
   * Makes the dialog for adding / removing an item from favourites visible.
   * Based on the user actions, it might execute the adding / removing
   * operation.
   * 
   * @return Returns an integer value which represents status of the operation:
   *         {@value #OPERATION_SUCCESSFUL} - favourite item was added / removed
   *         successfully; {@value #OPERATION_CANCELLED} - the user has
   *         cancelled operation; {@value #OPERATION_FAILED} - failed while
   *         adding / removing favourite item;
   */
  public int launchAddRemoveFavouriteDialogAndPerformNecessaryActionIfRequired() {
	this.setVisible(true);
	return (this.iOperationStatus);
  }

  // *** Callback for ActionListener interface ***
  public void actionPerformed(ActionEvent e) {

	if (e.getSource().equals(this.bAddRemoveFavourite)) {
	  // the window will stay visible, but should turn into 'waiting' state
	  final Container contentPane = this.getContentPane();
	  contentPane.removeAll();

	  final GridBagConstraints c = new GridBagConstraints();
	  c.gridx = 0;
	  c.gridy = 0;
	  c.gridwidth = 2;
	  c.anchor = GridBagConstraints.CENTER;
	  c.fill = GridBagConstraints.NONE;
	  c.insets = new Insets(10, 5, 10, 5);
	  JLabel lInfo = new JLabel(this.bIsFavouriteBeingAdded ? "Adding to favourites..." : "Removing from favourites...", new ImageIcon(MyExperimentPerspective.getLocalResourceURL("spinner")), SwingConstants.CENTER);
	  contentPane.add(lInfo, c);

	  // disable the (X) button (ideally, would need to remove it, but there's no way to do this)
	  this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

	  // revalidate the window
	  this.pack();
	  this.validate();
	  this.repaint();

	  new Thread("Execute add / remove favourite operation") {
		@Override
		public void run() {
		  // *** DO THE REQUIRED ACTION ***
		  response = (bIsFavouriteBeingAdded ? myExperimentClient.addFavourite(resource) : myExperimentClient.deleteFavourite(resource));
		  iOperationStatus = (response.getResponseCode() == HttpURLConnection.HTTP_OK) ? OPERATION_SUCCESSFUL : OPERATION_FAILED;

		  if (iOperationStatus == OPERATION_SUCCESSFUL) {
			// update local list of favourite items - no data sync with the API is required at this point
			if (bIsFavouriteBeingAdded) {
			  myExperimentClient.getCurrentUser().getFavourites().add(resource);
			} else {
			  myExperimentClient.getCurrentUser().getFavourites().remove(resource);
			}
		  } else {
			// operation has failed - this might have been due to outdated data which is stored locally;
			// sync favourite data with the API so that the UI can show more relevant data
			// (e.g. replace 'add to favourites' with 'remove from favourites' if this item is already
			//  added to favourites - probably via another interface: web or another instance of the plugin)
			myExperimentClient.updateUserFavourites(myExperimentClient.getCurrentUser());
		  }

		  SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			  // *** REACT TO RESULT ***
			  setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			  contentPane.removeAll();
			  c.insets = new Insets(10, 5, 5, 5);

			  if (iOperationStatus == OPERATION_SUCCESSFUL) {
				// favourite was added / removed successfully
				contentPane.add(new JLabel("Item has been successfully "
					+ (bIsFavouriteBeingAdded ? "added to" : "removed from")
					+ " your favourites", new ImageIcon(MyExperimentPerspective.getLocalResourceURL("success_icon")), SwingConstants.LEFT), c);
			  } else {
				// favourite wasn't added / removed - operation failed;
				// display error message
				contentPane.add(new JLabel("<html><center>Error occurred while "
					+ (bIsFavouriteBeingAdded ? "adding" : "removing")
					+ " the item "
					+ (bIsFavouriteBeingAdded ? "to" : "from")
					+ " your favourites:<br>"
					+ Util.retrieveReasonFromErrorXMLDocument(response.getResponseBody())
					+ "</center></html>", new ImageIcon(MyExperimentPerspective.getLocalResourceURL("failure_icon")), SwingConstants.LEFT), c);
			  }

			  bCancel.setText("OK");
			  bCancel.setPreferredSize(null); // resets preferred size to the automatic one
			  bCancel.setDefaultCapable(true);
			  c.insets = new Insets(5, 5, 10, 5);
			  c.gridy += 1;
			  contentPane.add(bCancel, c);

			  pack();
			  repaint();

			  bCancel.requestFocusInWindow();
			  getRootPane().setDefaultButton(bCancel);
			}
		  });
		}
	  }.start();

	} else if (e.getSource().equals(this.bCancel)) {
	  // simply close and destroy the window
	  this.dispose();
	}
  }

  // *** Callbacks for ComponentListener interface ***
  public void componentShown(ComponentEvent e) {
	// center this dialog box within the preview browser window
	Util.centerComponentWithinAnother(this.pluginMainComponent.getPreviewBrowser(), this);
  }

  public void componentHidden(ComponentEvent e) {
	// not in use
  }

  public void componentMoved(ComponentEvent e) {
	// not in use
  }

  public void componentResized(ComponentEvent e) {
	// not in use
  }

}

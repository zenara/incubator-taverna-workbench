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

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.apache.taverna.ui.perspectives.myexperiment.model.MyExperimentClient;
import org.apache.taverna.ui.perspectives.myexperiment.model.Resource;
import org.apache.taverna.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

/**
 * @author Sergejs Aleksejevs
 */
public class SearchResultsPanel extends JPanel
{
  // CONSTANTS
  public static final String NO_SEARCHES_STATUS = "No searches have been done so far";
  
  
  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;
  
  // COMPONENTS
  private JLabel lStatusLabel;
  public JButton bRefresh;
  public JButton bClear;
  private JPanel jpResultsBody;
  private ActionListener alClickHandler;
  
  // result data store
  boolean bNoSearchesMadeYet;
  String strCurrentSearchTerm;
  HashMap<Integer, ArrayList<Resource>> hmSearchResults;
  
  
  public SearchResultsPanel(ActionListener buttonClickHandler, MainComponent component, MyExperimentClient client, Logger logger)
  {
    super();
    
    // set main variables to ensure access to myExperiment, logger and the parent component
    this.pluginMainComponent = component;
    this.myExperimentClient = client;
    this.logger = logger;
    this.alClickHandler = buttonClickHandler;
    
    // initialise the result data collection to an empty one, just in case
    // someone calls refresh() before setting the real result data
    bNoSearchesMadeYet = true;
    hmSearchResults = new HashMap<Integer, ArrayList<Resource>>();
    
    this.initialiseUI();
  }

  
  
  private void initialiseUI()
  {
    // label to hold the status of search (e.g. result query + count, etc)
    this.lStatusLabel = new JLabel(NO_SEARCHES_STATUS);
    this.lStatusLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0)); // a bit of padding on the left
    
    // control buttons for the search results panel
    JPanel jpButtonsPanel = new JPanel();
    jpButtonsPanel.setLayout(new BoxLayout(jpButtonsPanel, BoxLayout.LINE_AXIS));
    this.bClear = new JButton("Clear", WorkbenchIcons.deleteIcon);
    this.bClear.addActionListener(this.alClickHandler);
    this.bClear.setToolTipText("Click this button to clear the search results");
    this.bClear.setEnabled(false);
    jpButtonsPanel.add(this.bClear);
    this.bRefresh = new JButton("Refresh", WorkbenchIcons.refreshIcon);
    this.bRefresh.addActionListener(this.alClickHandler);
    this.bRefresh.setToolTipText("Click this button to refresh the search results");
    this.bRefresh.setEnabled(false);
    jpButtonsPanel.add(this.bRefresh);
    
    // status panel containing the status label and control buttons
    JPanel jpStatusPanel = new JPanel(new BorderLayout());
    jpStatusPanel.setBorder(BorderFactory.createEtchedBorder());
    jpStatusPanel.add(this.lStatusLabel, BorderLayout.CENTER);
    jpStatusPanel.add(jpButtonsPanel, BorderLayout.EAST);
    
    // create empty panel for the main search result content
    this.jpResultsBody = new JPanel();
    jpResultsBody.setLayout(new BorderLayout());
    
    // PUT EVERYTHING TOGETHER
    this.setLayout(new BorderLayout());
    this.add(jpStatusPanel, BorderLayout.NORTH);
    this.add(this.jpResultsBody, BorderLayout.CENTER);
    
    // tabbed results view is missing from this method - this is
    // because if no results will be found in a particular category,
    // that tab will not be displayed (hence the results view will
    // need to be generated every time dynamically)
  }
  
  
  public void setCurrentSearchTerm(String strSearchTerm)
  {
    this.strCurrentSearchTerm = strSearchTerm;
  }
  
  public String getCurrentSearchTerm()
  {
    return (this.strCurrentSearchTerm);
  }
  
  
  public void setSearchResultsData(HashMap<Integer, ArrayList<Resource>> hmResults)
  {
    this.bNoSearchesMadeYet = false;
    this.hmSearchResults = hmResults;
  }
  
  
  public void setStatus(String status)
  {
    this.lStatusLabel.setText(status);
  }
  
  public void refresh()
  {
    // remove all items from the main results content panel
    this.jpResultsBody.removeAll();
    
    if (!this.bNoSearchesMadeYet) // this will be true if the result collection was never assigned to this result panel
    {
      ArrayList<Integer> alResourceTypes = new ArrayList<Integer>(this.hmSearchResults.keySet());
      Collections.sort(alResourceTypes);  // this will ensure that the tabs are always in the correct order
      if(alResourceTypes.isEmpty()) {
        this.jpResultsBody.add(new JLabel("No items to display"), BorderLayout.NORTH);
        this.repaint();
      }
      else {
        JTabbedPane tpResults = new JTabbedPane();
        
        for(int iType : alResourceTypes)
        {
          // HACK: for now all item types are suitably turned into plural by simply appending "s"
          String strTabLabel = Resource.getResourceTypeName(iType) + "s (" + this.hmSearchResults.get(iType).size() + ")";
          
          ResourceListPanel jpTabContents = new ResourceListPanel(pluginMainComponent, myExperimentClient, logger);
          jpTabContents.setListItems(this.hmSearchResults.get(iType));
          
          JScrollPane spTabContents = new JScrollPane(jpTabContents);
          spTabContents.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);
          
          tpResults.add(strTabLabel, spTabContents);
        }
        
        this.jpResultsBody.add(tpResults, BorderLayout.CENTER);
        this.bClear.setEnabled(true);
        this.bRefresh.setEnabled(true);
      }
    }
    
    this.revalidate();
  }
  
  
  public void clear()
  {
    // remove all items from the main results content panel
    this.jpResultsBody.removeAll();
    this.repaint();
    this.revalidate();
  }
  
}

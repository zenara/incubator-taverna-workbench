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
package org.apache.taverna.lang.ui.tabselector;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * LayoutManager for laying out tabs.
 * <p>
 * Tabs are made all the same width and prefer to be maximumTabWidth. If there is more than
 * preferred width available extra space is left blank. If there is not enough width for tabs to be
 * maximumTabWidth tab width is reduced until it reaches minimumTabWidth. If there is not enough
 * width to show tabs at minimumTabWidth scroll buttons are shows and tabs can be scrolled within
 * the available width.
 *
 * @author David Withers
 */
public class TabLayout implements LayoutManager {

	public static final int DEFAULT_MINIMUM_TAB_WIDTH = 100;
	public static final int DEFAULT_MAXIMUM_TAB_WIDTH = 250;
	public static final int DEFAULT_TAB_HEIGHT = 22;
	public static final int DEFAULT_SCROLL_BUTTON_WIDTH = 22;

	private final int minimumTabWidth, maximumTabWidth, tabHeight, scrollButtonWidth;
	private final ScrollController scrollController;

	public TabLayout(ScrollController scrollController) {
		this(scrollController, DEFAULT_MINIMUM_TAB_WIDTH, DEFAULT_MAXIMUM_TAB_WIDTH,
				DEFAULT_TAB_HEIGHT, DEFAULT_SCROLL_BUTTON_WIDTH);
	}

	public TabLayout(ScrollController scrollController, int minimumTabWidth, int maximumTabWidth,
			int tabHeight, int scrollButtonWidth) {
		this.scrollController = scrollController;
		this.minimumTabWidth = minimumTabWidth;
		this.maximumTabWidth = maximumTabWidth;
		this.tabHeight = tabHeight;
		this.scrollButtonWidth = scrollButtonWidth;
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public void layoutContainer(Container parent) {
		Component[] components = parent.getComponents();
		int tabs = components.length - 2;
		if (tabs > 0) {
			Insets insets = parent.getInsets();
			int x = insets.left;
			int y = insets.top;
			int availableWidth = parent.getWidth() - insets.left - insets.right;
			int tabWidth = availableWidth / tabs;
			boolean showScrollButtons = false;
			if (tabWidth < minimumTabWidth) {
				tabWidth = minimumTabWidth;
				showScrollButtons = true;
			} else if (tabWidth > maximumTabWidth) {
				tabWidth = maximumTabWidth;
			}
			if (showScrollButtons) {
				scrollController.getScrollLeft().setLocation(x, y);
				scrollController.getScrollLeft().setSize(
						new Dimension(scrollButtonWidth, tabHeight));
				scrollController.getScrollLeft().setVisible(true);
				scrollController.getScrollLeft().setEnabled(shouldScrollLeft(tabs, availableWidth));
				x = x + scrollButtonWidth - (scrollController.getPosition() * minimumTabWidth);
				scrollController.getScrollRight()
						.setLocation(availableWidth - scrollButtonWidth, y);
				scrollController.getScrollRight().setSize(
						new Dimension(scrollButtonWidth, tabHeight));
				scrollController.getScrollRight().setVisible(true);
				scrollController.getScrollRight().setEnabled(scrollController.getPosition() > 0);
			} else {
				scrollController.getScrollLeft().setVisible(false);
				scrollController.getScrollRight().setVisible(false);
				scrollController.reset();
			}
			for (int i = 2; i < components.length; i++) {
				components[i].setLocation(x, y);
				components[i].setSize(new Dimension(tabWidth, tabHeight));
				x = x + tabWidth;
			}
		}
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return calculateSize(parent.getInsets(), 2, minimumTabWidth);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return calculateSize(parent.getInsets(), parent.getComponents().length, maximumTabWidth);
	}

	private Dimension calculateSize(Insets insets, int tabs, int tabWidth) {
		int width = insets.left + insets.right + tabs * tabWidth;
		int height = insets.top + insets.bottom + tabHeight;
		return new Dimension(width, height);
	}

	private boolean shouldScrollLeft(int tabs, int availableWidth) {
		int tabsToShow = tabs - scrollController.getPosition();
		return (tabsToShow * minimumTabWidth) > (availableWidth - (scrollButtonWidth * 2));
	}
}

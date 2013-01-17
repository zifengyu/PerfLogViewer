/*
 * MainMenu.java
 *
 * This file is part of TDA - Thread Dump Analysis Tool.
 *
 * TDA is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * TDA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * TDA should have received a copy of the Lesser GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * $Id: MainMenu.java,v 1.38 2008-09-18 14:44:10 irockel Exp $
 */

package cn.alan.perflogviewer.ui;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

/**
 * provides instances of the tool bar
 *  
 */
public class MainMenu extends JMenuBar {

	private static final long serialVersionUID = 1L;
	
	private static final int TOOL_BAR_ICON_SIZE = 32;
	
	private PLV listener;
	private JToolBar toolBar;

	/** 
	 * Creates a new instance of the MainMenu 
	 */
	public MainMenu(PLV listener) {
		this.listener = listener;	
	}

	/**
	 * creates and returns a toolbar 
	 * @return toolbar instance, is created on demand.
	 */
	public JToolBar getToolBar() {
		if(toolBar == null) {
			createToolBar();
		}
		return toolBar;
	}

	/**
	 * create a toolbar showing the most important main menu entries.
	 */
	private void createToolBar() {
		toolBar = new JToolBar("PLV Toolbar");

		toolBar.add(createToolBarButton("Add files", "table-add-icon.png"));
		toolBar.add(createToolBarButton("Remove selected row", "table-remove-icon.png"));		
		toolBar.add(createToolBarButton("Clear all", "tables-icon.png"));
		toolBar.addSeparator();
		JButton button = createToolBarButton("Export to clipboard", "Clipboard-Manager-icon.png");
		//button.setEnabled(false);
		toolBar.add(button);
		toolBar.add(createToolBarButton("Export to image", "Line-Chart-icon.png"));
		toolBar.addSeparator();
		toolBar.add(createToolBarButton("Chart configuration", "app-settings-icon.png"));
		toolBar.add(createToolBarButton("Help", "Actions-help-about-icon.png"));
	}

	/**
	 * create a toolbar button with tooltip and given icon.
	 * @param text tooltip text
	 * @param fileName filename for the icon to load
	 * @return toolbar button
	 */
	private JButton createToolBarButton(String text, String fileName) {
		Image image = PLV.createImageIcon(fileName).getImage().getScaledInstance(TOOL_BAR_ICON_SIZE, TOOL_BAR_ICON_SIZE, java.awt.Image.SCALE_SMOOTH);
				
		JButton toolbarButton = new JButton(new ImageIcon(image));
		
		if(text != null) {
			toolbarButton.setToolTipText(text);
		}

		toolbarButton.addActionListener(listener);
		toolbarButton.setFocusable(false);
		return(toolbarButton);
	}
}

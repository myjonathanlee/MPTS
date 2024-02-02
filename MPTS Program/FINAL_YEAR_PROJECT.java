/*
 * @(#)FINAL_YEAR_PROJECT.java 1.0 03/02/11
 *
 * You can modify the template of this file in the
 * directory ..\JCreator\Templates\Template_1\Project_Name.java
 *
 * You can also create your own project template by making a new
 * folder in the directory ..\JCreator\Template\. Use the other
 * templates as examples.
 *
 */
package myprojects.final_year_project;

import java.awt.*;
import java.awt.event.*;

class FINAL_YEAR_PROJECT extends Frame {
	
	public FINAL_YEAR_PROJECT() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				System.exit(0);
			}
		});
	}

	public static void main(String args[]) {
		System.out.println("Starting FINAL_YEAR_PROJECT...");
		FINAL_YEAR_PROJECT mainFrame = new FINAL_YEAR_PROJECT();
		mainFrame.setSize(400, 400);
		mainFrame.setTitle("FINAL_YEAR_PROJECT");
		mainFrame.setVisible(true);
	}
}

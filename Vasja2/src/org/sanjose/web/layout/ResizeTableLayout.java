package org.sanjose.web.layout;

import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.ResizeEvent;
import com.vaadin.ui.Window.ResizeListener;

public class ResizeTableLayout extends VerticalLayout {
	
	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public void setResizeMargin(int margin) {
		this.margin = margin;
	}
	
	public int getResizeMargin() {
		return this.margin;
	}

	protected Table table = null;
	
	int margin = 200;
	
	protected Window mainWindow = null;
	
	public ResizeTableLayout(Window w) {		
		this.mainWindow = w;
		mainWindow.addListener(new ResizeListener() {
			@Override
			public void windowResized(ResizeEvent e) {
			//	System.out.println("window height: " + mainWindow.getHeight());
				if (table!=null)
					table.setPageLength(Math.round((mainWindow.getHeight()-margin)/20f));
			}			
		});
	}
	

}

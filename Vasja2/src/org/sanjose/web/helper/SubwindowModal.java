package org.sanjose.web.helper;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class SubwindowModal extends Window {

	public SubwindowModal(String title) {
    	super(title);

        // ...and make it modal
        setModal(true);
        setWidth("36em");
        setCloseShortcut(KeyCode.ESCAPE, null);
        //setPositionY(20);
        //setPositionX(-1);
        center();
        //setPositionX(UNITS_PERCENTAGE);
        // Configure the windws layout; by default a VerticalLayout
        VerticalLayout layout = (VerticalLayout) getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
    }

}

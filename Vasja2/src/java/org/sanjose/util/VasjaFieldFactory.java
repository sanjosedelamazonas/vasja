package org.sanjose.util;

import com.vaadin.ui.DateField;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;

@SuppressWarnings("serial")
public class VasjaFieldFactory extends DefaultFieldFactory {

	protected DateField createDateField(Object propertyId) {
		DateField pf = new DateField();
		pf.setResolution(DateField.RESOLUTION_MIN);
		pf.setDateFormat(ConfigurationUtil.get("DEFAULT_DATE_FORMAT"));
		pf.setCaption(DefaultFieldFactory.createCaptionByPropertyId(propertyId));
		return pf;
	}

	protected TextArea createTextArea(Object propertyId) {
		TextArea pf = new TextArea();
		pf.setWidth("16em");
		pf.setHeight("4em");
		pf.setWordwrap(true);
		pf.setCaption(DefaultFieldFactory.createCaptionByPropertyId(propertyId));
		return pf;
	}

	protected PasswordField createPasswordField(Object propertyId) {
		PasswordField pf = new PasswordField();
		pf.setCaption(DefaultFieldFactory.createCaptionByPropertyId(propertyId));
		return pf;
	}

}

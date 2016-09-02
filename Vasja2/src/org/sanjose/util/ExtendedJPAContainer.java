package org.sanjose.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.util.BeanItem;

public class ExtendedJPAContainer<T> extends JPAContainer<T> {

	static final Logger logger = Logger.getLogger(ExtendedJPAContainer.class.getName());

	public ExtendedJPAContainer(Class<T> entityClass) {
		super(entityClass);
	}

	/**
	 * Updates properties of the given item to the values of the beanItem. This method ignores nested objects
	 * since they are not available in the beanItem
	 * @param item
	 * @param beanItem
	 * @throws UnsupportedOperationException
	 * @throws IllegalStateException
	 */
	public void updateEntityItem(Item item, BeanItem<T> beanItem) throws UnsupportedOperationException,
			IllegalStateException {
		assert beanItem != null : "entity must not be null";
		assert item != null : "item must not be null";
		requireWritableContainer();

		Object itemId = beanItem.getItemProperty("id");
		if (itemId!=null && item!=null) {
			for (Object prop : beanItem.getItemPropertyIds()) {
				logger.info("Updating prop " + prop);
				logger.info("value: " + item.getItemProperty(prop).toString());
				if ((beanItem.getItemProperty(prop)!=null && 
						beanItem.getItemProperty(prop).getType().getCanonicalName().equals(Date.class.getName()))) {
					SimpleDateFormat format = new SimpleDateFormat(ConfigurationUtil.get("DEFAULT_DATE_FORMAT"));
					try {
						Date newDate = format.parse(beanItem.getItemProperty(prop).toString());
						item.getItemProperty(prop).setValue(newDate);
					} catch (ParseException e) {
						try {
							format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
							Date newDate = format.parse(beanItem.getItemProperty(prop).toString());
							item.getItemProperty(prop).setValue(newDate);
						} catch (ParseException ee) {
							// Tue Sep 06 19:31:45 PET 2011
							format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
							try {
								Date newDate = format.parse(beanItem.getItemProperty(prop).toString());
								item.getItemProperty(prop).setValue(newDate);
							} catch (ParseException eee) {
								eee.printStackTrace();	
							}

						}
					}
					
				} else		
				try {	
					if (item.getItemProperty(prop)!=null && item.getItemProperty(prop).toString()!=null &&
						!item.getItemProperty(prop).toString().contains("[") && 
							!item.getItemProperty(prop).toString().contains("{") ) {
					//	logger.info("Updating prop: " + beanItem.getItemProperty(prop) + " " + beanItem.getItemProperty(prop).getValue());
						item.getItemProperty(prop).setValue(beanItem.getItemProperty(prop));
					}
				} catch (ConversionException nsme) {
					logger.severe("Property: " + prop + " failed to update to value: " + beanItem.getItemProperty(prop).getValue());					
				}
			}		
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 142562363572743737L;

}

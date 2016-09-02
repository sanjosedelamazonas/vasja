package org.sanjose.web.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.sanjose.util.ConfigurationUtil;

import com.vaadin.data.Property;
import com.vaadin.ui.Table;

public class OperacionesTable extends Table {
	
	static final Logger logger = Logger.getLogger(OperacionesTable.class.getName());
	
	public OperacionesTable(String name) {
		super();
		setCaption(name);
		//super(name, container);
		//this.setFilterGenerator(new OperacionesFilterGenerator());
		//this.setFiltersVisible(true);
	}
	
	public OperacionesTable() {
		super();
	}
	
	@Override
	protected String formatPropertyValue(Object rowId, Object colId,
			Property property) {
		
		boolean isPen = (Boolean) (getContainerProperty(rowId, "isPen").getValue());
		
		if (colId.equals("pen") || colId.equals("usd") || colId.equals("abonoPen") 
				|| colId.equals("abonoUsd") || colId.equals("cargoPen") || colId.equals("cargoUsd")) {
			String pen = super.formatPropertyValue(rowId, colId, property);
			//pen = pen.format("###,##0.00", null);
/*			String iconName = "../icons/";
			boolean isCar = (Boolean) (getContainerProperty(rowId,
					"isCargo").getValue());
			boolean isCorreccion = (Boolean) (getContainerProperty(rowId,
					"isCorrection").getValue());
			iconName += (isPen ? "pen_" : "usd_");
			iconName += (isCorreccion ? "correccion" : (isCar ? "cargo"
					: "abono")) + ".png";
			setItemIcon(rowId, new ThemeResource(iconName)); 
			*/
			if (!pen.contains("."))
				pen+=".00";				
			if (pen.equals("0.00"))
				return "";			
			
			/*if ((isCar) && (!pen.equals("0.00"))) {
				if (pen.contains("-"))
					return pen.replace("-", "");
				else
					return "-" + pen;
			}*/
		}
		if (colId.equals("saldoPen") && (!isPen)) return "";
		if (colId.equals("saldoUsd") && (isPen)) return "";
	
		if (property.getType() == Date.class) {
			SimpleDateFormat df = new SimpleDateFormat(ConfigurationUtil.get("SHORT_DATE_FORMAT"));
			try {
				return df.format((Date) property.getValue());
			} catch (Exception e) {
				return (property.getValue() != null ? property.getValue()
						.toString() : "");
			}
		}
		return super.formatPropertyValue(rowId, colId, property);
	}
	
	
	/*private class OperacionesFilterGenerator implements FilterGenerator {

	    @Override
	    public Filter generateFilter(Object propertyId, Object value) {
	        if ("descripcion".equals(propertyId)) {
	            if (value != null && value instanceof String) {
	                try {
	                	Filters.like(propertyId, "*" +(String)value + "*", false);
	                	
	                	
	                	//return new Compare(propertyId.toString().contains((String)value);
	                    //return //new Compare.Equal(propertyId,
	                            //Integer.parseInt((String) value));
	                } catch (NumberFormatException ignored) {
	                    // If no integer was entered, just generate default filter
	                }
	            }
	        }
	        // For other properties, use the default filter
	        return null;
	    }

	} */
	
	
	
	
}


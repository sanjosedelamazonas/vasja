package org.sanjose.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.vaadin.data.Property;
import com.vaadin.data.util.PropertyFormatter;

public class DecimalPropertyFormatter extends PropertyFormatter {
	    private static final long serialVersionUID = -8487454652016030363L;
	   
	    NumberFormat df;
	
	    public DecimalPropertyFormatter() {
	        super();
	    }
	   
	    public DecimalPropertyFormatter(Property propertyDataSource, String formatString) {
	        // Must call the parameterless super-constructor, because otherwise
	        // the setPropertyDataSource() is called during this call, which
	        // calls format(), which would be a problem as this class (the 'df'
	        // variable) is not initialized yet at this point.
	        super();
	
	        df = DecimalFormat.getInstance(Locale.US);
	        if (df instanceof DecimalFormat) {
	             ((DecimalFormat) df).setDecimalSeparatorAlwaysShown(true);
	        }
	        
	        ((DecimalFormat)df).applyPattern(formatString);
	        // Must be set after the format is set, as this causes a format() call
	        if (propertyDataSource!=null) setPropertyDataSource(propertyDataSource);
	    }

	    @Override
	    public Object parse(String formattedValue) throws Exception {
	        return df.parse(formattedValue);
	    }
	   
	    @Override
	    public String format(Object value) {
	        if (value == null) {
	            System.out.println("Null value to format");
	            return "";
	        }
	        if (df == null) {
	            System.out.println("DecimalFormat not initialized yet in constructor - bug #4484!");
	            return "";
	        }
	        String result = df.format((BigDecimal) value);
	        return result;
	    }
	
	    @Override
	    public Class<?> getType() {
	        return String.class;
	    }
}
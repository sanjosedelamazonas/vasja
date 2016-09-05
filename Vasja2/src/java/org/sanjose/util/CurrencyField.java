package org.sanjose.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.vaadin.autoreplacefield.AutoReplaceField;
/**
 * TODO document, refine regexps
 *
 */
@SuppressWarnings("serial")
public class CurrencyField extends AutoReplaceField  {
	
	String decimalFormat = ConfigurationUtil.get("DECIMAL_FORMAT");
	
	public CurrencyField() {
			// allow only numbers, dots and commas
			addReplaceRule("[^0-9\\.\\,]+", "");
			// allow only one dot or comma
			addReplaceRule("(.+)[\\.\\,](.*)[\\.\\,]", "$1.$2");
			
			setNullRepresentation("");
	}
	
	public void setDecimalFormat(String decimalFormat) {
		this.decimalFormat = decimalFormat;
	}
	
	/**
	 * TODO could use NumberFormat to parse.
	 * 
	 * @return parses the text in the field into a Number instance.
	 */
	public BigDecimal getValue() {
		Object value2 = super.getValue();
		if(value2 != null) {
			String string = prepareStringForNumberParsing(value2.toString());
			string = string.replace(",", ".");
			try {
				long parseInt = Long.parseLong(string);
				return new BigDecimal(parseInt);
			} catch (NumberFormatException e) {
				try {
					DecimalFormat fmt = new DecimalFormat(decimalFormat);
					fmt.setParseBigDecimal(true);
					return (BigDecimal) fmt.parse((String)string);
				} catch (Exception e2) {
					return null;
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class getType() {
		return BigDecimal.class;
	}

	/**
	 * For subclasses so they can filter the string for number parsing.
	 * 
	 * @param string the string value in the text field
	 * @return the filtered string
	 */
	protected String prepareStringForNumberParsing(String string) {
		return string;
	}
}

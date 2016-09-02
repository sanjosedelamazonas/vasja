package org.sanjose.util;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.sanjose.model.Cuenta;

import com.vaadin.data.Validator;

public class CuentaNumeroValidator implements Validator {

			private static final long serialVersionUID = -9007565688398484768L;

			private String message;

		    public CuentaNumeroValidator(String message) {
		        this.message = message;
		    }

		    public boolean isValid(Object value) {
		        if (value == null || !(value instanceof String)) {
		            return false;
		        }		        		      
	        	String string = (String)value;
	        	Long num = Long.parseLong(string);
	        	Query q = ConfigurationUtil.getEntityManager().createNamedQuery("Cuenta.getCuentaPorNumero");
	   		 	q.setParameter(1, num);
	            try {
	            	Cuenta c = (Cuenta)q.getSingleResult();
	            	return false;
	            } catch (NoResultException e) {
	            	return true;
		        }
		    }

		    public void validate(Object value) throws InvalidValueException {
		        if (!isValid(value)) {
		            throw new InvalidValueException(message);
		        }
		    }

}

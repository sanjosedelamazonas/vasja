package org.sanjose.web.helper;

import org.sanjose.model.Cuenta;
import org.sanjose.model.Operacion;
import org.sanjose.util.ExtendedJPAContainer;

public interface IOperacionTable {

	ExtendedJPAContainer<Operacion> getOperacionContainer();
	
	void drawCuentaForm(final Cuenta cuenta, boolean isRefresh);
	
	void generateTotals();
}

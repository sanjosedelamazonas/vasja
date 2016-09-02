package org.sanjose.util;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.sanjose.model.Cuenta;
import org.sanjose.web.helper.ReportHelper;
import org.sanjose.web.layout.ReporteCuentaLayout;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

public class CuentaReporteButtonClickListener implements Button.ClickListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2188827173228547680L;
	Cuenta cuenta = null;
	HashMap<Object, CheckBox> selectedChBoxes = null;
	ReporteCuentaLayout repLayout = new ReporteCuentaLayout();
	Window window = null;
	Window sub = null;
	Table table = null;

	public CuentaReporteButtonClickListener(Cuenta cuenta, Window window, Window sub) {
		this.cuenta = cuenta;
		this.window = window;
		this.sub = sub;
	}
	
	public CuentaReporteButtonClickListener(Cuenta cuenta, Table table, Window window, Window sub) {
		this.selectedChBoxes = selectedChBoxes;
		this.cuenta = cuenta;
		this.window = window;
		this.sub = sub;
		this.table = table;
	}

	public void buttonClick(ClickEvent event) {
		//if ((window!=null && window.getChildWindows().size()>0 && (!window.getChildWindows().contains(sub))) {
			sub.getContent().removeAllComponents();
			final Set<Object> cuentaIds = new HashSet<Object>();
			if (cuenta!=null) { 
				sub.setCaption("Reporte Diario de Cuenta: " + cuenta.getNumero());
				repLayout.getReportLabel().setValue(" Cuenta: " + cuenta.getNumero() + " " + cuenta.getNombre());
			} else {
				sub.setCaption("Reporte Diario de Cuentas");
				Set<Object> selIds = (Set<Object>)table.getValue();
				for (Object id : selIds) {
					//if (!((Cuenta)container.getItem(id).getEntity()).getIsCaja())
					cuentaIds.add(id);
				}					
				repLayout.getReportLabel().setValue(cuentaIds.size() + " cuentas seleccionadas para reporte");
			}
				 
			repLayout.setPrintButton();
			repLayout.getPrintButton().addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					if (repLayout.getFechaMinField().getValue()!=null && repLayout.getFechaMaxField().getValue()!=null) 
					{
		/*				if (cuenta!=null)
							ReportHelper.generateReporteCuenta(
								(Date)repLayout.getFechaMinField().getValue(), 
								(Date)repLayout.getFechaMaxField().getValue(), 
								cuenta ,null, window);
						else { */
							ReportHelper.generateReporteCuenta(
								(Date)repLayout.getFechaMinField().getValue(), 
								(Date)repLayout.getFechaMaxField().getValue(), 
								null, cuentaIds, window);
						//}
						sub.getParent().removeWindow(sub);
					}						
				}
			});
			sub.getContent().addComponent(repLayout);
			window.addWindow(sub);
//		}
	}
}
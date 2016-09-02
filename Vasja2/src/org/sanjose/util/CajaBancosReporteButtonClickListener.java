package org.sanjose.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.sanjose.web.helper.ReportHelper;
import org.sanjose.web.layout.ReporteCajaBancosLayout;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window;

public class CajaBancosReporteButtonClickListener implements Button.ClickListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2188827173228547680L;
	static final Logger logger = Logger.getLogger(CajaBancosReporteButtonClickListener.class.getName());
	
	ReporteCajaBancosLayout repCBLayout = new ReporteCajaBancosLayout();
	Window window = null;
	Window sub = null;
	boolean isPen;
	boolean isCaja;
	Date minFecha;
	Date maxFecha;
	String moneda;
	

	public CajaBancosReporteButtonClickListener(boolean isCaja, boolean isPen, Date minFecha, Date maxFecha,Window window, Window sub) {
		this.isCaja=isCaja;
		this.isPen=isPen;
		this.minFecha=minFecha;
		if (maxFecha==null) maxFecha=minFecha;
		this.maxFecha=maxFecha;
		this.window = window;
		this.sub = sub;
		this.moneda=(isPen)?"Nuevos soles":"USD";
	}

	public void buttonClick(ClickEvent event) {
		//if ((window!=null && window.getChildWindows().size()>0 && (!window.getChildWindows().contains(sub))) {
			if (isCaja) { 
				sub.setCaption("Reporte de Caja en "+moneda);
				repCBLayout.getReportLabel().setValue(" Reporte de los movimientos de caja en "+moneda);
	//			repCBLayout.getReportTypeGroup().setVisible(false);
			} else {
				sub.setCaption("Reporte de Bancos en "+moneda);
				}
			repCBLayout.setPrintButton();
			logger.info("got params minFecha"+minFecha+" maxFecha "+ maxFecha +" isPen "+ isPen +" window "+window);
			repCBLayout.getPrintButton().addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					logger.info("got params "+minFecha+" "+ maxFecha +" "+ isPen +" "+window +" "+repCBLayout.getFormatOptionGroup().getValue().toString());
					//nie mam pojecia czemu sie nie przekazuja parametry
					// do testow ustawiam na sztywno:
					isCaja=false;
					try {
					minFecha=new SimpleDateFormat("yyyyMMdd").parse("20120101");
					maxFecha=new SimpleDateFormat("yyyyMMdd").parse("20120501");
					} catch (ParseException e) {};
					//wyciac ustawienie param
					if (isCaja) ReportHelper.generateDiarioCaja(
								minFecha,maxFecha, isPen, window, repCBLayout.getFormatOptionGroup().getValue().toString());
						else 
							ReportHelper.generateDiarioBanco(minFecha,maxFecha, isPen, window, 
									repCBLayout.getFormatOptionGroup().getValue().toString(),
									repCBLayout.getReportTypeGroup().getValue().toString());
						sub.getParent().removeWindow(sub);
					}						
			});
			sub.getContent().addComponent(repCBLayout);
			window.addWindow(sub);
//		}
	}
}

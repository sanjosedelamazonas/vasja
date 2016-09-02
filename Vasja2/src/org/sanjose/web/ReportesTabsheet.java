package org.sanjose.web;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.sanjose.model.CategoriaCuenta;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.web.helper.ReportHelper;
import org.sanjose.web.helper.SubwindowModal;
import org.sanjose.web.layout.ReportesLayout;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ReportesTabsheet extends VerticalLayout {
	
	ReportesLayout rl = new ReportesLayout();
	
	static final Logger logger = Logger
			.getLogger(ReportesTabsheet.class.getName());

	public ReportesTabsheet() {
		setCaption("Reportes");
		setSpacing(true);
		Panel panel = new Panel();
		panel.addComponent(rl);
		
	// Fill categorias 
		IndexedContainer ccc = new IndexedContainer();
        ccc.addContainerProperty("nombre", String.class, "");
    	JPAContainer jpaContainer = JPAContainerFactory.make(CategoriaCuenta.class, ConfigurationUtil.getEntityManager());
		Collection<Object> catIds = jpaContainer.getItemIds();        
		for (Object catId : catIds) {
        	CategoriaCuenta cat = (CategoriaCuenta)jpaContainer.getItem(catId).getEntity();//catEntityProvider.getEntity(catId);
            Item item = ccc.addItem(cat);
            item.getItemProperty("nombre").setValue(cat.getNombre());
        }
        rl.getCategoriaCombo().setContainerDataSource(ccc);
        rl.getCategoriaCombo().setItemCaptionPropertyId("nombre");
        rl.getCategoriaCombo().setFilteringMode(ComboBox.FILTERINGMODE_STARTSWITH);
        
        // Button Listeners
		
		rl.getPrintBancarioButton().addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Date fechaMin = (Date)rl.getFechaMinField().getValue();
				Date fechaMax = (Date)rl.getFechaMaxField().getValue();
				String grouping = (String)rl.getGroupingCombo().getValue();
			/*	boolean isPen = (((rl.getMonedaGroup().getValue()!=null) 
						&& (rl.getMonedaGroup().getValue().toString().equals("USD"))) ? false : true); */
				String format = (String)rl.getFormatOptionGroup().getValue();
				if (fechaMin!=null && fechaMax!=null && fechaMin.before(fechaMax)) {
					if (grouping==null || grouping.equals("Detallado por centro de costo"))
						ReportHelper.generateLugarGasto(fechaMin, fechaMax, getWindow(), format,"Detallado");							
					else if (grouping.equals("Entregas a rendir"))
						ReportHelper.generateLugarGasto(fechaMin, fechaMax, getWindow(), format,"Sin lugar");
					else if(grouping.equals("Informe resumido por centro de costo"))
						ReportHelper.generateLugarGasto(fechaMin, fechaMax, getWindow(), format,"Informe");	
					else if(grouping.equals("Detallado por cuenta contable"))
						ReportHelper.generateLugarGasto(fechaMin, fechaMax, getWindow(), format,"DetalladoCuentaContable");	
					else if(grouping.equals("Detallado por rubro institucional"))
						ReportHelper.generateLugarGasto(fechaMin, fechaMax, getWindow(), format,"Detallado por rubro institucional");	
					else if(grouping.equals("Informe resumido por cuenta contable"))
						ReportHelper.generateLugarGasto(fechaMin, fechaMax, getWindow(), format,"Informe resumido por cuenta contable");	
					else if(grouping.equals("Informe resumido por rubro institucional"))
						ReportHelper.generateLugarGasto(fechaMin, fechaMax, getWindow(), format,"Informe resumido por rubro presupuestal");	
				}				
			}
		});
	
	/*	rl.getPrintCajaButton().addListener(new Button.ClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void buttonClick(ClickEvent event) {
				Date fechaMin = (Date)rl.getCajaFechaMinField().getValue();
				boolean isPen = (((rl.getCajaOptionGroup().getValue()!=null) 
						&& (rl.getCajaOptionGroup().getValue().toString().equals("USD"))) ? false : true); 
				String format = (String)rl.getCajaFormatOptionGroup().getValue();
				if (fechaMin!=null)
					ReportHelper.generateDiarioCaja(
						ConfigurationUtil.getBeginningOfDay(fechaMin),null, isPen, getWindow(), format);
			}			
		});
	*/
		
		rl.getPrintMensualButton().addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Date fecha = (Date)rl.getMensualDateField().getValue();
				String format = (String)rl.getMensualOptionGroup().getValue();
				CategoriaCuenta cat = (CategoriaCuenta)rl.getCategoriaCombo().getValue();
				if (fecha!=null && cat!=null)
					ReportHelper.generateCuentasMensual(fecha, cat, getWindow(), format);								
			}
		});		

		rl.getPrintCCButton().addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Date minfecha = (Date)rl.getCCfechaMinField().getValue();
				Date maxfecha = (Date)rl.getCCfechaMaxField().getValue();
				if (maxfecha==null) maxfecha=minfecha;
				boolean isPen = (((rl.getMonedaCCGroup().getValue()!=null) 
						&& (rl.getMonedaCCGroup().getValue().toString().equals("USD"))) ? false : true); 
				String format = (String)rl.getFormatCCOptionGroup().getValue();
				String grouping = (String)rl.getGroupingCCCombo().getValue();
				if (minfecha!=null)
					ReportHelper.generateCC(minfecha, maxfecha,isPen, getWindow(), format,grouping);								
			}
		});	
		
/*		Button consistency = new Button("Consistency");
		consistency.addListener(new Button.ClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void buttonClick(ClickEvent event) {
				SubwindowModal sub = new SubwindowModal("Consistency");
				ConfigurationUtil.checkConsistency();
				if (!ConfigurationUtil.isConsistent())
					sub.addComponent(new Label("LA BASE NO ESTA CONSISTENTE!!!"));					
				String mes = ConfigurationUtil.getLastConsistency();
				Label l = new Label(mes);
				l.setContentMode(Label.CONTENT_PREFORMATTED);
				sub.addComponent(l);
				getWindow().addWindow(sub);
			}			
		});		
		rl.getMainLayout().addComponent(consistency);
		rl.getMainLayout().setComponentAlignment(consistency, Alignment.MIDDLE_CENTER); */
		panel.addComponent(rl);
		addComponent(panel);
		setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
	}
	

}

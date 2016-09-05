package org.sanjose.web.accountant;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.sanjose.model.Banco;
import org.sanjose.model.CategoriaCuenta;
import org.sanjose.model.Cuenta;
import org.sanjose.model.Operacion;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.util.DataFilterUtil;
import org.sanjose.util.DateValidator;
import org.sanjose.util.ExtendedJPAContainer;
import org.sanjose.web.OperacionForm;
import org.sanjose.web.VasjaApp;
import org.sanjose.web.helper.IOperacionTable;
import org.sanjose.web.helper.ReportHelper;
import org.sanjose.web.helper.SessionHandler;
import org.sanjose.web.helper.SubwindowModal;
import org.sanjose.web.layout.ReporteCajaBancosLayout;
import org.sanjose.web.layout.ResizeTableLayout;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.filter.Filters;
import com.vaadin.addon.jpacontainer.provider.CachingBatchableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class AccountantCajaTable extends ResizeTableLayout implements IOperacionTable {
	
	NumberFormat formatter = new DecimalFormat(ConfigurationUtil.get("DECIMAL_FORMAT"));
	private String valorTemp;
	static final Action ACTION_ABRIR_COMPROBANTE = new Action("Abrir comprobante");
	static final Action ACTION_IMPRIMIR_COMPROBANTE = new Action("Imprimir comprobante");
	static final Action ACTION_CORRECTION = new Action("Correction");
	static final Action VER_OPERACION = new Action("Ver operacion");
	static final Action EDIT_OPERACION = new Action("Edit operacion"); 
	static final Action[] ACTIONS = new Action[] { VER_OPERACION, ACTION_ABRIR_COMPROBANTE, ACTION_IMPRIMIR_COMPROBANTE, 
			ACTION_CORRECTION };
	static final Action[] ACTIONS_EDITABLE = new Action[] { VER_OPERACION, EDIT_OPERACION, ACTION_ABRIR_COMPROBANTE, ACTION_IMPRIMIR_COMPROBANTE, 
		ACTION_CORRECTION };	
	static final Logger logger = Logger.getLogger(AccountantCajaTable.class
			.getName());

	Panel mainPanel = new Panel();
	AccountantCajaComponent cajaComp = new AccountantCajaComponent();
	ExtendedJPAContainer<Operacion> operacionContainer;
	MutableLocalEntityProvider<Operacion> operacionEntityProvider;
	final ReporteCajaBancosLayout repCBLayout = new ReporteCajaBancosLayout();
	
	OperacionButtonClickListener egresoListener = null;
	OperacionButtonClickListener ingresoListener = null;
	
	HashMap<String, Filter> appliedFilters = new HashMap<String, Filter>();
	HashMap<String, Property.ValueChangeListener> filterListeners = new HashMap<String, Property.ValueChangeListener>();
	
	private Cuenta selectedCuenta = null;

	Button operacionesMuestraButton = null;
	boolean firstRun = true;
	boolean isPen=true;
	boolean isBanco=false;
	VerticalLayout buttonsLayout = null;

	//final Table operacionesTable = new OperacionesTable("Operaciones");
	
	public AccountantCajaTable(Window w) {
		super(w);
		this.table = cajaComp.getOpTable();
		setResizeMargin(460);
    	setSizeFull();    
		mainPanel.addComponent(cajaComp);
		mainPanel.setSizeFull();		
		mainPanel.setScrollable(true);
		if (SessionHandler.isContador()) setCaption("Caja/Bancos");
		if ((!SessionHandler.isContador())&&(!SessionHandler.isAdmin())) {
			cajaComp.getCajaBancoCombo().setVisible(false);
			cajaComp.getLabel_2().setValue("<b> Movimientos del </b>");

		}
		addComponent(mainPanel);
		setExpandRatio(mainPanel, 1f);
		cajaComp.getCajaDateField().setValue(new Date());
		cajaComp.getCajaDateField().setImmediate(true);
		redraw(null, isPen, false, isBanco);
		cajaComp.getImprimirButton().addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				
				//(Boolean)cajaComp.getMonedaCombo().getValue();
				String moneda=((Boolean)cajaComp.getMonedaCombo().getValue()) ? "Nuevos soles" : "USD";
				//if ((window!=null && window.getChildWindows().size()>0 && (!window.getChildWindows().contains(sub))) {
				if ((Boolean)cajaComp.getCajaBancoCombo().getValue()) { 
					sub.setCaption("Reporte de Caja en "+moneda);
					repCBLayout.getReportLabel().setValue(" Reporte de los movimientos de caja en "+moneda);
		//			repCBLayout.getReportTypeGroup().setVisible(false);
				} else {
					sub.setCaption("Reporte de Bancos en "+moneda);
				}
				repCBLayout.setPrintButton();
				//logger.info("got params minFecha" + (Date)cajaComp.getCajaDateField().getValue() + " maxFecha "+ maxFecha + " isPen "+ isPen +" window "+mainWindow);
				repCBLayout.getPrintButton().addListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						Date maxFecha = (Date)cajaComp.getCajaMaxDateField().getValue()!=null ? (Date)cajaComp.getCajaMaxDateField().getValue() : (Date)cajaComp.getCajaDateField().getValue();   
						//logger.info("got params "+minFecha+" "+ maxFecha +" "+ isPen +" "+window +" "+repCBLayout.getFormatOptionGroup().getValue().toString());
						if ((Boolean)cajaComp.getCajaBancoCombo().getValue()) ReportHelper.generateDiarioCaja(
								(Date)cajaComp.getCajaDateField().getValue() , maxFecha, (Boolean)cajaComp.getMonedaCombo().getValue(), mainWindow, repCBLayout.getFormatOptionGroup().getValue().toString());
						else 
							ReportHelper.generateDiarioBanco((Date)cajaComp.getCajaDateField().getValue(), maxFecha , (Boolean)cajaComp.getMonedaCombo().getValue(), mainWindow, 
										repCBLayout.getFormatOptionGroup().getValue().toString(),
										repCBLayout.getReportTypeGroup().getValue().toString());
						sub.getParent().removeWindow(sub);
					}						
				});
				sub.getContent().addComponent(repCBLayout);
				mainWindow.addWindow(sub);
			}
		});	
				
				
/*ab		new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				boolean isPen = (Boolean)cajaComp.getMonedaCombo().getValue();
				Date maxFecha = cajaComp.getCajaMaxDateField().getValue()!=null ? (Date)cajaComp.getCajaMaxDateField().getValue() : (Date)cajaComp.getCajaDateField().getValue();				
				if ((Boolean)cajaComp.getCajaBancoCombo().getValue()) ReportHelper.generateDiarioCaja((Date)cajaComp.getCajaDateField().getValue(),ConfigurationUtil.getEndOfDay(maxFecha), isPen, getWindow(), null);
				else 
					ReportHelper.generateDiarioBanco((Date)cajaComp.getCajaDateField().getValue(),ConfigurationUtil.getEndOfDay(maxFecha), isPen, getWindow(), null);
			}
		}		);		
		*/
	}
	
	public void redraw(Cuenta selCuenta, boolean isPen, boolean isRefresh, boolean isBanco) {
		this.selectedCuenta = selCuenta;
		final AccountantCajaTable aoTable = this;
		DataFilterUtil.bindBooleanComboBox(cajaComp.getCajaBancoCombo(), "isCaja", "CAJA/BANCO", 
				new String[] { "CAJA", "BANCOS"});
		cajaComp.getCajaBancoCombo().setValue(true);
		ValueChangeListener l = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if ((event.getProperty()!=null) && (event.getProperty().getValue()!=null)) {
					if (!(Boolean)cajaComp.getCajaBancoCombo().getValue()) {
						cajaComp.getBancosCombo().setEnabled(true);
						cajaComp.getBancosCombo().setVisible(true);
						cajaComp.getIngresoButton().setEnabled(false);
						cajaComp.getEgresoButton().setEnabled(false);
						selectedCuenta = null;
						drawDetails((Date)cajaComp.getCajaDateField().getValue(), (Boolean)cajaComp.getMonedaCombo().getValue(), true, true);			
					} else {
						cajaComp.getBancosCombo().setEnabled(false);
						cajaComp.getBancosCombo().setVisible(false);
						cajaComp.getIngresoButton().setEnabled(false);
						cajaComp.getEgresoButton().setEnabled(false);
						drawDetails((Date)cajaComp.getCajaDateField().getValue(), (Boolean)cajaComp.getMonedaCombo().getValue(), true, false);
					}					
				}				
			}
		};
		cajaComp.getCajaBancoCombo().setImmediate(true);
		cajaComp.getCajaBancoCombo().addListener(l);
		
		DataFilterUtil.bindComboBox(cajaComp.getBancosCombo(), "nombre", "filtrar banco", Banco.class,  Filters.not(Filters.eq("bancoCuenta", null)));
		l = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if ((event.getProperty()!=null) && (event.getProperty().getValue()!=null)) {
					cajaComp.getMonedaCombo().setEnabled(false);
					Banco bancoSel = (Banco)cajaComp.getBancosCombo().getValue();
					if (bancoSel!=null) { 
						cajaComp.getMonedaCombo().setValue(bancoSel.getMoneda().equals("PEN"));
						cajaComp.getEgresoButton().setEnabled(true);
						cajaComp.getIngresoButton().setEnabled(true);
						selectedCuenta = bancoSel.getBancoCuenta();
					}					 
				}	
				else {
					cajaComp.getMonedaCombo().setEnabled(true);
					cajaComp.getEgresoButton().setEnabled(false);
					cajaComp.getIngresoButton().setEnabled(false);
					drawDetails((Date)cajaComp.getCajaDateField().getValue(), (Boolean)cajaComp.getMonedaCombo().getValue(), true, true);
					selectedCuenta = null;
				}
			}
		};	
		cajaComp.getBancosCombo().addListener(l);
		DataFilterUtil.bindBooleanComboBox(cajaComp.getMonedaCombo(), "isPen", "Moneda", 
				new String[] { "Nuevos Soles", "Dolares"});
		//cajaComp.getMonedaCombo().select("Nuevos Soles");
		cajaComp.getMonedaCombo().setValue(isPen);
		if (filterListeners.containsKey("fecha")) filterListeners.remove("fecha");
		l = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if ((event.getProperty()!=null) && (event.getProperty().getValue()!=null)) 
					drawDetails((Date)event.getProperty().getValue(), (Boolean)cajaComp.getMonedaCombo().getValue(), true, !(Boolean)cajaComp.getCajaBancoCombo().getValue());				
			}
		};		
		cajaComp.getCajaDateField().addListener(l);
		filterListeners.put("fecha", l);
		try {
			Date minDate = new SimpleDateFormat("yyyyMMdd").parse("20100101");
			cajaComp.getCajaDateField().addValidator(
					new DateValidator("La fecha seleccionada no esta correcta", "dd/MM/yyyy", 
							minDate, ConfigurationUtil.getEndOfDay(new Date(System.currentTimeMillis()))));
		} catch (ParseException e) {
		}
		cajaComp.getCajaDateField().setDateFormat("dd/MM/yyyy");
		cajaComp.getCajaDateField().setResolution(DateField.RESOLUTION_DAY);
		
		if (filterListeners.containsKey("fechaMax")) filterListeners.remove("fechaMax");
		l = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if ((event.getProperty()!=null) && (event.getProperty().getValue()!=null)) 
					drawDetails((Date)event.getProperty().getValue(), (Boolean)cajaComp.getMonedaCombo().getValue(), true, !(Boolean)cajaComp.getCajaBancoCombo().getValue());				
			}
		};
		cajaComp.getCajaMaxDateField().addListener(l);
		filterListeners.put("fechaMax", l);
		
		
		drawDetails((Date)cajaComp.getCajaDateField().getValue(), (Boolean)cajaComp.getMonedaCombo().getValue(), isRefresh, !(Boolean)cajaComp.getCajaBancoCombo().getValue());
		if (selectedCuenta==null) {
			cajaComp.getIngresoButton().setEnabled(false);
			cajaComp.getEgresoButton().setEnabled(false);
		}		
	}
	
	public void drawDetails(Date cajaDate, boolean isPen, boolean isRefresh, boolean isBanco) {
		String moneda = (isPen ? "Nuevos Soles" : "Dolares");
		BigDecimal total = new BigDecimal(0.00);
		int num = 1;
		cajaComp.getCatIniLayouts().clear();
		
		if (!isBanco) {
			Filter fil = Filters.not(Filters.eq("nombre", "Caja y bancos"));			
			JPAContainer jpaContainer = JPAContainerFactory.make(CategoriaCuenta.class, ConfigurationUtil.getEntityManager());
			jpaContainer.addContainerFilter(fil);
			jpaContainer.applyFilters();
			Collection<Object> catIds = jpaContainer.getItemIds();
	
			//List<Object> catIds = ConfigurationUtil.getCategoriaCuentaEP().getAllEntityIdentifiers(fil, null);
			HashMap<Object, Operacion> saldoOpers = new HashMap<Object, Operacion>();
			for (Object catId : catIds) {
				CategoriaCuenta catCuenta = (CategoriaCuenta)jpaContainer.getItem(catId).getEntity();
				Operacion operSaldo = ConfigurationUtil.getSaldoPorFecha((Long)catId, 
						ConfigurationUtil.getBeginningOfDay((Date)cajaComp.getCajaDateField().getValue()));
				//logger.info("Got for cat " + catCuenta.getNombre() + " " + operSaldo);
				saldoOpers.put(catId, operSaldo);
				HorizontalLayout catDetailsLayout = cajaComp.buildCatIniHorizontalLayout(
						num, catCuenta.getNombre(), (isPen ? operSaldo.getSaldoPen() : operSaldo.getSaldoUsd()));
				total = total.add((isPen ? operSaldo.getSaldoPen() : operSaldo.getSaldoUsd()));
				cajaComp.getCatIniLayouts().put(num, catDetailsLayout);
				num++;
			}
			cajaComp.buildSaldosInicialVerticalLayout();
			valorTemp = formatter.format(total);
			cajaComp.getCatTotalLabel().setValue(valorTemp);
			if (((BigDecimal)total).compareTo(new BigDecimal(0))<0) cajaComp.getCatTotalLabel().setStyleName("red");
	
			//cuentaPanel.removeAllComponents();
			//BeanItem<Cuenta> cuentaItem = new BeanItem<Cuenta>(cuenta); // item
			total = new BigDecimal(0.00);
			num = 1;
			cajaComp.getCatCierLayouts().clear();
			for (Object catId : catIds) {
				CategoriaCuenta catCuenta = (CategoriaCuenta)jpaContainer.getItem(catId).getEntity();			
				Date maxFecha = cajaComp.getCajaMaxDateField().getValue()!=null ? (Date)cajaComp.getCajaMaxDateField().getValue() : (Date)cajaComp.getCajaDateField().getValue();			
				Operacion operMov = ConfigurationUtil.getCajaMovimientosPorFecha((Date)cajaComp.getCajaDateField().getValue(), maxFecha, (Long)catId);
				operMov.setSaldoPen(operMov.getSaldoPen().add(saldoOpers.get(catId).getSaldoPen()));
				operMov.setSaldoUsd(operMov.getSaldoUsd().add(saldoOpers.get(catId).getSaldoUsd()));
				HorizontalLayout catDetailsLayout = cajaComp.buildCatIniHorizontalLayout(
						num, catCuenta.getNombre(), (isPen ? operMov.getSaldoPen() : operMov.getSaldoUsd()));
				total = total.add(isPen ? operMov.getSaldoPen() : operMov.getSaldoUsd());
				cajaComp.getCatCierLayouts().put(num, catDetailsLayout);
				num++;
			}
			cajaComp.buildSaldosCierVerticalLayout();
			valorTemp=formatter.format(total);
			cajaComp.getTotalCierLabel().setValue(valorTemp);
			if (((BigDecimal)total).compareTo(new BigDecimal(0))<0) cajaComp.getTotalCierLabel().setStyleName("red");
			cajaComp.getMonedaInicioLabel().setValue(moneda);
			cajaComp.getMonedaSaldosLabel().setValue(moneda);
		} else {
			// BANCOS
			Filter fil = Filters.not(Filters.eq("moneda", (isPen ? "USD" : "PEN")));
			if (cajaComp.getBancosCombo().getValue()!=null)
				fil = Filters.and(fil, Filters.eq("id", ((Banco)cajaComp.getBancosCombo().getValue()).getId()));
			
			JPAContainer jpaContainer = JPAContainerFactory.make(Banco.class, ConfigurationUtil.getEntityManager());
			jpaContainer.addContainerFilter(fil);
			jpaContainer.applyFilters();
			Collection<Object> bancoIds = jpaContainer.getItemIds();
	
			//List<Object> bancoIds = ConfigurationUtil.getBancoEP().getAllEntityIdentifiers(fil, null);
			HashMap<Object, Operacion> saldoOpers = new HashMap<Object, Operacion>();
			for (Object bancoId : bancoIds) {
				Banco banco = (Banco)jpaContainer.getItem(bancoId).getEntity();
				Operacion operSaldo = ConfigurationUtil.getSaldoPorFecha(banco.getBancoCuenta(), 
						ConfigurationUtil.getBeginningOfDay((Date)cajaComp.getCajaDateField().getValue()));
				logger.info("banco saldo before for: " + banco.getBancoCuenta() + " " + operSaldo);
//				saldoOpers.put(bancoId, operSaldo);
				HorizontalLayout catDetailsLayout = cajaComp.buildCatIniHorizontalLayout(
						num, banco.getNombre(), (isPen ? operSaldo.getSaldoPen() : operSaldo.getSaldoUsd()));
				total = total.add((isPen ? operSaldo.getSaldoPen() : operSaldo.getSaldoUsd()));
				cajaComp.getCatIniLayouts().put(num, catDetailsLayout);
				num++;
			}
			cajaComp.buildSaldosInicialVerticalLayout();
			cajaComp.getCatTotalLabel().setValue(total);
			if (((BigDecimal)total).compareTo(new BigDecimal(0))<0) cajaComp.getCatTotalLabel().setStyleName("red");
	
			//cuentaPanel.removeAllComponents();
			//BeanItem<Cuenta> cuentaItem = new BeanItem<Cuenta>(cuenta); // item
			total = new BigDecimal(0.00);
			num = 1;
			cajaComp.getCatCierLayouts().clear();
			for (Object bancoId : bancoIds) {
				Banco banco = (Banco)jpaContainer.getItem(bancoId).getEntity();
				Date maxFecha = cajaComp.getCajaMaxDateField().getValue()!=null ? (Date)cajaComp.getCajaMaxDateField().getValue() : (Date)cajaComp.getCajaDateField().getValue();			
				Operacion operSaldo = ConfigurationUtil.getSaldoPorFecha(banco.getBancoCuenta(), 
						ConfigurationUtil.getEndOfDay(maxFecha));
				logger.info("banco saldo after for: " + banco.getBancoCuenta() + " " + operSaldo);
				HorizontalLayout catDetailsLayout = cajaComp.buildCatIniHorizontalLayout(
						num, banco.getNombre(), (isPen ? operSaldo.getSaldoPen() : operSaldo.getSaldoUsd()));
				total = total.add(isPen ? operSaldo.getSaldoPen() : operSaldo.getSaldoUsd());
				cajaComp.getCatCierLayouts().put(num, catDetailsLayout);
				num++;
			}
			cajaComp.buildSaldosCierVerticalLayout();
			cajaComp.getTotalCierLabel().setValue(total.toString());
			if (((BigDecimal)total).compareTo(new BigDecimal(0))<0) cajaComp.getTotalCierLabel().setStyleName("red");
			cajaComp.getMonedaInicioLabel().setValue(moneda);
			cajaComp.getMonedaSaldosLabel().setValue(moneda);
		}
		
		
		AccountantCajaTable ot = this;
		if (ingresoListener!=null) {
			cajaComp.getIngresoButton().removeListener(ingresoListener);			
			cajaComp.getEgresoButton().removeListener(egresoListener);									
		} else {
			cajaComp.getCerrarButton().addListener(new Button.ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					//
					removeAllComponents();
					addComponent(new AccountantOperacionTable(mainWindow));
					
				}
			});
			cajaComp.getIngresoButton().setIcon(new ThemeResource("../icons/sign_add.png"));
			cajaComp.getEgresoButton().setIcon(new ThemeResource("../icons/sign_remove.png"));
			cajaComp.getImprimirButton().setIcon(new ThemeResource("../icons/printer-icon_mini.png"));
			cajaComp.getCerrarButton().setIcon(new ThemeResource("../icons/back-icon.png"));
		}
		if (SessionHandler.isAdmin()) cajaComp.getCerrarButton().setVisible(false);
		ingresoListener = new OperacionButtonClickListener(
				selectedCuenta, ot, false);
		egresoListener = new OperacionButtonClickListener(
				selectedCuenta, ot, true);
		cajaComp.getEgresoButton().addListener(egresoListener);
		cajaComp.getIngresoButton().addListener(ingresoListener);
		
		prepareOperacionesTable(isPen, isRefresh, isBanco);
		drawOperacionesTable(isPen);
		generateTotals();
	}

	private void prepareOperacionesTable(boolean isPen, boolean isRefresh, boolean isBanco) {
		if (firstRun) {
			operacionContainer = new ExtendedJPAContainer<Operacion>(
				Operacion.class);
			MutableLocalEntityProvider<Operacion> operacionProvider = ConfigurationUtil.getOperacionEP();
	
			operacionContainer.setEntityProvider(operacionProvider);
			operacionContainer.addNestedContainerProperty("usuario.*");
			operacionContainer.addNestedContainerProperty("cuenta.*");
			operacionContainer.addNestedContainerProperty("banco.*");
			operacionContainer.addNestedContainerProperty("operacionCorrected.*");
			operacionContainer.addNestedContainerProperty("operacionDetalle.cuenta.*");
			operacionContainer.sort(new Object[] { "fecha", "id" },
					new boolean[] { false, false });
			cajaComp.getOpTable().setContainerDataSource(operacionContainer);
		} else if (isRefresh) {
			CachingBatchableLocalEntityProvider<Operacion> entProv = ((CachingBatchableLocalEntityProvider<Operacion>)operacionContainer.getEntityProvider());
			entProv.flush();
			entProv.refresh();
			operacionContainer.setEntityProvider(ConfigurationUtil.getOperacionEP());
			operacionContainer.sort(new Object[] { "fecha", "id" },
					new boolean[] { false, false });
		}
		for (Object filterName : appliedFilters.keySet())
			operacionContainer.removeContainerFilter(appliedFilters.get(filterName));
		
		Filter f = Filters.or(  
						Filters.and(Filters.eq("cuenta.isBanco", isBanco), Filters.eq("cuenta.isCaja", true))
						);
		operacionContainer.addContainerFilter(f);			
		appliedFilters.put("basic", f);
				
		Date maxFecha = cajaComp.getCajaMaxDateField().getValue()!=null ? (Date)cajaComp.getCajaMaxDateField().getValue() : (Date)cajaComp.getCajaDateField().getValue();			
		f = Filters.between("fecha", 
				ConfigurationUtil.getBeginningOfDay((Date)cajaComp.getCajaDateField().getValue()), 
				ConfigurationUtil.getEndOfDay(maxFecha), true, true);
		operacionContainer.addContainerFilter(f);
		appliedFilters.put("fecha", f);
		f = Filters.eq("isPen", isPen);
		operacionContainer.addContainerFilter(f);
		appliedFilters.put("isPen", f);
		if (cajaComp.getBancosCombo().getValue()!=null) {
			f = Filters.eq("cuenta.id", ((Banco)cajaComp.getBancosCombo().getValue()).getBancoCuenta().getId());
			operacionContainer.addContainerFilter(f);
			appliedFilters.put("cuenta.id", f);
		}
		operacionContainer.applyFilters();
		operacionContainer.sort(new Object[] { "fecha", "id" },
				new boolean[] { false, false });
		cajaComp.getOpTable().requestRepaint();			
	}

	public void drawOperacionesTable(final boolean isPen) {
		cajaComp.getOpTable().removeGeneratedColumn("ingreso"); 
		cajaComp.getOpTable().addGeneratedColumn("ingreso", new ColumnGenerator() {
		@Override
			public Component generateCell(Table source, Object itemId, Object columnId) {
				Item it = (Item)source.getItem(itemId);
				if (!(Boolean)it.getItemProperty("isCargo").getValue()) 
					return new Label((isPen ? source.getItem(itemId).getItemProperty("pen") : 
						source.getItem(itemId).getItemProperty("usd")));
				else
					return null;
			}
		});
		cajaComp.getOpTable().removeGeneratedColumn("egreso");
		cajaComp.getOpTable().addGeneratedColumn("egreso", new ColumnGenerator() {
			@Override
			public Component generateCell(Table source, Object itemId, Object columnId) {
				Item it = (Item)source.getItem(itemId);
				if ((Boolean)it.getItemProperty("isCargo").getValue()) 
					return new Label((isPen ? source.getItem(itemId).getItemProperty("pen") : 
						source.getItem(itemId).getItemProperty("usd")));
				else
					return null;
			}
		});

		if (firstRun) {
			cajaComp.getOpTable().setSelectable(true);
			cajaComp.getOpTable().setMultiSelect(false);
			cajaComp.getOpTable().setColumnReorderingAllowed(true);
			cajaComp.getOpTable().setColumnCollapsingAllowed(true);
			cajaComp.getOpTable().setPageLength(Math.round((mainWindow.getHeight()-460)/20f));
			
			
			
			cajaComp.getOpTable().setVisibleColumns(new String[] { "fecha","fechaDeCobro", "operacionDetalle.cuenta.numero", "descripcion", "cuenta.numero", "banco.nombre",  "id",
					"ingreso", "egreso", "firma",  "pen" , "usd", (isPen ? "saldoPen" : "saldoUsd"), "tipo", "chequeNumero", "dni", "usuario.usuario", "isCargo"});
			// set column headers
			cajaComp.getOpTable().setColumnHeaders(new String[] { "Fecha","FCobro", "Cuenta", "Descripcion", "Caja/Bco", "Banco", "Id",
					"Ingreso", "Egreso", "Nombre", "PEN", "USD", (isPen ? "SALDO PEN" : "SALDO USD"),
					"Tipo", "Detalle", "DNI", "Usuario", "ISCARGO" });
			
			// Column alignment
			cajaComp.getOpTable().setColumnAlignment("descripcion", Table.ALIGN_CENTER);
			// Column width
			cajaComp.getOpTable().setColumnWidth("fecha",60);
			cajaComp.getOpTable().setColumnWidth("fechaDeCobro",60);
			cajaComp.getOpTable().setColumnWidth("operacionDetalle.cuenta.numero",60);
			cajaComp.getOpTable().setColumnExpandRatio("descripcion", 1);
			cajaComp.getOpTable().setColumnWidth("descripcion", 400);
			cajaComp.getOpTable().setColumnWidth("chequeNumero", 100);
			cajaComp.getOpTable().setColumnWidth("banco", 100);
			cajaComp.getOpTable().setColumnWidth("ingreso", 80);
			cajaComp.getOpTable().setColumnWidth("egreso", 80);
			cajaComp.getOpTable().setColumnCollapsed("id", true);
			cajaComp.getOpTable().setColumnCollapsed("saldoPen", true);
			cajaComp.getOpTable().setColumnCollapsed("saldoUsd", true);
			cajaComp.getOpTable().setColumnCollapsed("pen", true);
			cajaComp.getOpTable().setColumnCollapsed("usd", true);
			cajaComp.getOpTable().setColumnCollapsed("firma", true);
			cajaComp.getOpTable().setColumnCollapsed("tipo", true);
			cajaComp.getOpTable().setColumnCollapsed("banco", true);
			cajaComp.getOpTable().setColumnCollapsed("dni", true);
			cajaComp.getOpTable().setColumnCollapsed("chequeNumero", true);
			cajaComp.getOpTable().setColumnCollapsed("usuario.usuario", true);
			cajaComp.getOpTable().setColumnCollapsed("isCargo", true);
			cajaComp.getOpTable().setColumnAlignment("ingreso", Table.ALIGN_RIGHT);
			cajaComp.getOpTable().setColumnAlignment("egreso", Table.ALIGN_RIGHT);
			cajaComp.getOpTable().setColumnAlignment("saldoUsd", Table.ALIGN_RIGHT);
			cajaComp.getOpTable().setColumnAlignment("saldoPen", Table.ALIGN_RIGHT);
			cajaComp.getOpTable().setColumnAlignment("usd", Table.ALIGN_RIGHT);
			cajaComp.getOpTable().setColumnAlignment("pen", Table.ALIGN_RIGHT);
			// Filters
			DataFilterUtil.bindBooleanComboBox(cajaComp.getIngrEgrCombo(),
					"isCargo", "Ingreso/Egreso", new String[] { "EGRESO", "INGRESO"}, operacionContainer, appliedFilters, filterListeners, this);
			// Filter by Moneda using the field above
			bindMonedaCombo();			
			// Filter Categoria 
			DataFilterUtil.bindComboBox(cajaComp.getCatCombo(), "nombre", "filtrar categoria", CategoriaCuenta.class,  Filters.not(Filters.eq("nombre", "Caja y bancos")));
			cajaComp.getCatCombo().removeListener(filterListeners.get("cuenta.categoriacuenta_id"));
			ValueChangeListener l = new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					if (event != null && event.getProperty() != null
							&& event.getProperty().getValue() != null) {
						CategoriaCuenta cat = (CategoriaCuenta)event.getProperty().getValue();
						// For CAJA
						if ((Boolean)cajaComp.getCajaBancoCombo().getValue()) {
							selectedCuenta = cat.getCajaCuenta();
							if (appliedFilters.containsKey("cuenta.categoriacuenta_id")) {
								operacionContainer.removeContainerFilter(appliedFilters.get("cuenta.categoriacuenta_id"));
								operacionContainer.applyFilters();
								appliedFilters.remove(appliedFilters.get("cuenta.categoriacuenta_id"));
							}
							CategoriaCuenta cajaCat = ((CategoriaCuenta)event.getProperty().getValue());
							if (cajaCat!=null) {									
								Filter f = Filters.eq("cuenta",cajaCat.getCajaCuenta());
								operacionContainer.addContainerFilter(f);
								operacionContainer.applyFilters();
								appliedFilters.put("cuenta.categoriacuenta_id", f);
							}
							cajaComp.getEgresoButton().setEnabled(true);
							cajaComp.getIngresoButton().setEnabled(true);
							
						} else {
							// For Banco
							if (appliedFilters.containsKey("cuenta.categoriacuenta_id")) {
								operacionContainer.removeContainerFilter(appliedFilters.get("cuenta.categoriacuenta_id"));
								operacionContainer.applyFilters();
								appliedFilters.remove(appliedFilters.get("cuenta.categoriacuenta_id"));
							}
							CategoriaCuenta cajaCat = ((CategoriaCuenta)event.getProperty().getValue());
							if (cajaCat!=null) {									
								Filter f = Filters.eq("operacionDetalle.cuenta.categoriaCuenta",cajaCat);
								operacionContainer.addContainerFilter(f);
								operacionContainer.applyFilters();
								appliedFilters.put("cuenta.categoriacuenta_id", f);
							}							
						}
						
					} else {
						selectedCuenta=null;
						cajaComp.getCatCombo().setInputPrompt("filtrar categoria");
						if (appliedFilters.containsKey("cuenta.categoriacuenta_id")) {
							operacionContainer.removeContainerFilter(appliedFilters.get("cuenta.categoriacuenta_id"));
							appliedFilters.remove(appliedFilters.get("cuenta.categoriacuenta_id"));
						}
						cajaComp.getEgresoButton().setEnabled(false);
						cajaComp.getIngresoButton().setEnabled(false);
					}
					generateTotals();
				}
			};
			cajaComp.getCatCombo().addListener(l);
			filterListeners.put("cuenta.categoriacuenta_id", l);
						
			final AccountantCajaTable ot = this;
			cajaComp.getOpTable().addActionHandler(new Action.Handler() {
				public Action[] getActions(Object target, Object sender) {
					Item item = cajaComp.getOpTable().getItem(target);
					if (item!=null) {
						Date fecha = (Date)item.getItemProperty("fecha").getValue();
						if (item.getItemProperty("operacionDetalle.cuenta.numero").getValue()==null && ConfigurationUtil.getUltimoMesCerrado().before(fecha)) {
 							return ACTIONS_EDITABLE;							
						} else {
							return ACTIONS;
						}
					}
					return ACTIONS;
				}
				
				public void handleAction(Action action, Object sender,
						Object target) {
					Item item = cajaComp.getOpTable().getItem(target);
					Long opId = (Long) item.getItemProperty("id")
							.getValue();					
					if (VER_OPERACION == action) {
						EntityItem<Operacion> eItem = operacionContainer
								.getItem(opId);
						final SubwindowModal sub = new SubwindowModal(
								"Operacion");
						sub.getContent().addComponent(
								new OperacionForm(eItem.getEntity()));
						getWindow().addWindow(sub);
					} else if (EDIT_OPERACION == action) {
						EntityItem<Operacion> eItem = operacionContainer
								.getItem(opId);
						final SubwindowModal sub = new SubwindowModal(
								"Edit Operacion");
						sub.getContent().addComponent(
								new OperacionPostEditForm(eItem.getEntity(), null, ot, isPen));
						//sub.getContent().addComponent(
						//		new OperacionPostEditForm(eItem.getEntity(), cajaComp.getOpTable()));
						getWindow().addWindow(sub);											
					} else if (ACTION_ABRIR_COMPROBANTE == action) {
						EntityItem<Operacion> eItem = operacionContainer
								.getItem(opId);
						ReportHelper.generateComprobante(eItem.getEntity(),
								getWindow());
					} else if (ACTION_IMPRIMIR_COMPROBANTE == action) {
						EntityItem<Operacion> eItem = operacionContainer
								.getItem(opId);
						try {
							JasperPrint jrPrint = ReportHelper.printComprobante(eItem.getEntity(), getWindow());
							boolean isPrinted = false;
							if (((VasjaApp)getApplication()).getPrintHelper()!=null)
								isPrinted = ((VasjaApp)getApplication()).getPrintHelper().print(jrPrint, true);
							if (!isPrinted)
								throw new JRException("Problema al conseguir un servicio de imprimir");
						} catch (JRException e) {
							e.printStackTrace();
							getWindow().showNotification("Problema al imprimir el comprobante ID: " + opId + " " + e.getMessage());
						}
					} else if (ACTION_CORRECTION == action) {
						EntityItem<Operacion> eItem = operacionContainer
								.getItem((Long) item.getItemProperty("id")
										.getValue());
						Operacion op = eItem.getEntity();
						if (!op.getIsCorrection()) {
							final SubwindowModal sub = new SubwindowModal(
									"Correccion");
							sub.getContent().addComponent(
									new OperacionForm(op.getCuenta(), ot, op.getIsCargo(), op));
							getWindow().addWindow(sub);
						} else 
							getWindow().showNotification("Problema con la correccion", "No se puede corregir una correccion", Notification.TYPE_HUMANIZED_MESSAGE);						
					}
				}
			});
			cajaComp.getOpTable().setFooterVisible(true);
			cajaComp.getOpTable().setColumnFooter("cuenta.nombre", "Total:");
			
			// style generator
			cajaComp.getOpTable().setCellStyleGenerator(new CellStyleGenerator() {
				public String getStyle(Object itemId, Object propertyId) {
					if ("pen".equals(propertyId) || "usd".equals(propertyId) 
							|| "saldoPen".equals(propertyId) || "saldoUsd".equals(propertyId)
							|| "ingreso".equals(propertyId) || "egreso".equals(propertyId) )  {
							
						if (cajaComp.getOpTable()
								.getContainerProperty(itemId, propertyId)!=null) {
									BigDecimal value = (BigDecimal)cajaComp.getOpTable()
											.getContainerProperty(itemId, propertyId)
											.getValue();
							if (value.compareTo(new BigDecimal(0))<0)									
								return "negativeNumber";					
						}
					}
					return null;
				}
			});
			firstRun = false;
		}
        final ThemeResource export = new ThemeResource("../icons/table-excel.png");
		cajaComp.getExportarButton().setIcon(export);
		cajaComp.getExportarButton().addListener(new ClickListener() {
            private static final long serialVersionUID = -73954695086117200L;
            private ExcelExport excelExport;

            @Override
            public void buttonClick(final ClickEvent event) {
            	try {
            		String type = "Banco";
            		if ((Boolean)cajaComp.getCajaBancoCombo().getValue())
            			type = "Caja";
            		Date fromDate = null;
            		Date toDate = null;
            		if (cajaComp.getCajaDateField().getValue()!=null) 
            			fromDate = (Date)cajaComp.getCajaDateField().getValue();
            		if (cajaComp.getCajaMaxDateField()!=null && cajaComp.getCajaMaxDateField().getValue()!=null) 
            			toDate = (Date)cajaComp.getCajaMaxDateField().getValue();            				            		
	                excelExport = new ExcelExport(cajaComp.getOpTable(), "Operaciones " + type);
	                //excelExport.excludeCollapsedColumns();                               
	                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
	                String dates = (fromDate!=null ? sdf.format(fromDate) : "") + 
							(toDate!=null ? " - "+sdf.format(toDate) : "");
	                excelExport.setReportTitle("Operaciones " + type + " - " + dates);
	                sdf = new SimpleDateFormat("yyyyMMdd");
	                dates = (fromDate!=null ? sdf.format(fromDate) : "") + 
							(toDate!=null ? "-"+sdf.format(toDate) : "");
	                sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
	                excelExport.setExportFileName("Export_" + type + "_" + dates + "_created_" + sdf.format(new Date()) + ".xls");
	                excelExport.setDisplayTotals(true);
	                excelExport.setRowHeaders(false);
	                excelExport.setDateDataFormat("yyyy.MM.dd");
	                excelExport.setExcelFormatOfProperty("id",  "##0");
	                excelExport.setExcelFormatOfProperty("operacionDetalle.cuenta.numero",  "##0");
	                excelExport.setExcelFormatOfProperty("cuenta.numero",  "##0");
	                excelExport.setExcelFormatOfProperty("pen",  ConfigurationUtil.get("DECIMAL_FORMAT"));
	                excelExport.setExcelFormatOfProperty("usd",  ConfigurationUtil.get("DECIMAL_FORMAT"));
	                excelExport.setExcelFormatOfProperty("saldoPen",  ConfigurationUtil.get("DECIMAL_FORMAT"));
	                excelExport.setExcelFormatOfProperty("saldoUsd",  ConfigurationUtil.get("DECIMAL_FORMAT"));
	                excelExport.setExcelFormatOfProperty("ingreso",  ConfigurationUtil.get("DECIMAL_FORMAT"));
	                excelExport.setExcelFormatOfProperty("egreso",  ConfigurationUtil.get("DECIMAL_FORMAT"));
	                excelExport.export();
                } catch (Exception e) {
            		getWindow().showNotification("Problema con el exporte de operaciones", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
            	}
                
            }
        });

		// Filter Cuenta
		Set<Cuenta> cuentasForFiltro = new HashSet<Cuenta>();
		for (Object id : operacionContainer.getItemIds()) {
			Operacion opp = (Operacion)operacionContainer.getItem(id).getEntity(); 
			if (opp.getOperacionDetalle()!=null)
				cuentasForFiltro.add(opp.getOperacionDetalle().getCuenta());
		}
		DataFilterUtil.bindCuentasBox(cajaComp.getCuentaCombo(), cuentasForFiltro);	
		cajaComp.getCuentaCombo().removeListener(filterListeners.get("cuentas"));
		ValueChangeListener l = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if (event != null && event.getProperty() != null
						&& event.getProperty().getValue() != null) {
					Cuenta cta = (Cuenta)event.getProperty().getValue();
					if (appliedFilters.containsKey("cuentas")) {
							operacionContainer.removeContainerFilter(appliedFilters.get("cuentas"));
							operacionContainer.applyFilters();
							appliedFilters.remove(appliedFilters.get("cuentas"));
					}
					if (cta!=null) {									
							Filter f = Filters.eq("operacionDetalle.cuenta",cta);
							operacionContainer.addContainerFilter(f);
							operacionContainer.applyFilters();
							appliedFilters.put("cuentas", f);
					}					
				} else {
					cajaComp.getCuentaCombo().setInputPrompt("filtrar cuenta");
					if (appliedFilters.containsKey("cuentas")) {
						operacionContainer.removeContainerFilter(appliedFilters.get("cuentas"));
						appliedFilters.remove(appliedFilters.get("cuentas"));
					}
				}
				generateTotals();
			}
		};
		cajaComp.getCuentaCombo().addListener(l);
		filterListeners.put("cuentas", l);

		
		// is Null Cta Detalle
		
		cajaComp.getIsNullCtaDetalle().addListener(new ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				if ((Boolean)cajaComp.getIsNullCtaDetalle().getValue()) {
					if (!appliedFilters.containsKey("operacionDetalle")) {
						Filter f = Filters.isNull("operacionDetalle");
						operacionContainer.addContainerFilter(f);
						appliedFilters.put("operacionDetalle", f);
						operacionContainer.applyFilters();
						generateTotals();
					}
				} else {
					if (appliedFilters.containsKey("operacionDetalle")) {
						operacionContainer.removeContainerFilter(appliedFilters.get("operacionDetalle"));
						appliedFilters.remove("operacionDetalle");
						operacionContainer.applyFilters();
						generateTotals();
					}					
				}
			}
		});
		
		
		
	}
	
	private void bindMonedaCombo() {
		cajaComp.getMonedaCombo().removeListener(filterListeners.get("isPen"));
		ValueChangeListener l = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if (event != null && event.getProperty() != null
						&& event.getProperty().getValue() != null && 
						!event.getProperty().getValue().toString().equals("")) {
					if (appliedFilters.containsKey("isPen")) {
						operacionContainer.removeContainerFilter(appliedFilters.get("isPen"));
						appliedFilters.remove(appliedFilters.get("isPen"));
					}
					Filter f = Filters.eq("isPen", event.getProperty().getValue());
					operacionContainer.addContainerFilter(f);
					operacionContainer.applyFilters();
					appliedFilters.put("isPen", f);
					
					drawDetails((Date)cajaComp.getCajaDateField().getValue(), (Boolean)event.getProperty().getValue(), true, !(Boolean)cajaComp.getCajaBancoCombo().getValue());
					generateTotals();
					
				} else {
					cajaComp.getMonedaCombo().setInputPrompt("Seleccione moneda");
					if (appliedFilters.containsKey("isPen")) {
						operacionContainer.removeContainerFilter(appliedFilters.get("isPen"));
						appliedFilters.remove(appliedFilters.get("isPen"));
					}
				}
			}
		};
		cajaComp.getMonedaCombo().addListener(l);
		filterListeners.put("isPen", l);		
	}

	public void generateTotals() {
		BigDecimal total = new BigDecimal(0.00);
		boolean isPen = (Boolean)cajaComp.getMonedaCombo().getValue();
		for (Object id : cajaComp.getOpTable().getItemIds()) {
			Item item = cajaComp.getOpTable().getItem(id);
			boolean isCargo = (Boolean)item.getItemProperty("isCargo").getValue();
			if (!isCargo) 
				total = total.add(isPen ?
						(BigDecimal)item.getItemProperty("pen").getValue() : 
							(BigDecimal)item.getItemProperty("usd").getValue());		
		}
		cajaComp.getOpTable().setColumnFooter("ingreso", (isPen ? "S./ " : "$ ") + total.toString());
		total = new BigDecimal(0.00);
		for (Object id : cajaComp.getOpTable().getItemIds()) {
			Item item = cajaComp.getOpTable().getItem(id);
			boolean isCargo = (Boolean)item.getItemProperty("isCargo").getValue();
			if (isCargo) 
				total = total.add(isPen ?
						(BigDecimal)item.getItemProperty("pen").getValue() : 
							(BigDecimal)item.getItemProperty("usd").getValue());		
		}
		cajaComp.getOpTable().setColumnFooter("egreso", (isPen ? "S./ " : "$ ") + total.toString());
	}
	
	
	SubwindowModal sub = new SubwindowModal("Comprobante");
	
	private class OperacionButtonClickListener implements Button.ClickListener {
		Cuenta cuenta;
		boolean isCargo = false;
		AccountantCajaTable ot;

		public OperacionButtonClickListener(Cuenta cuenta, AccountantCajaTable ot,
				boolean isCargo) {
			this.cuenta = cuenta;
			this.isCargo = isCargo;
			this.ot = ot;
		}

		public void buttonClick(ClickEvent event) {
			if (!getWindow().getChildWindows().contains(sub) && selectedCuenta!=null) {
				sub.getContent().removeAllComponents();
				sub.setCaption((isCargo ? "Egreso"
						: "Ingreso"));
				Boolean isPen = (Boolean)cajaComp.getMonedaCombo().getValue();
				logger.info("Button listener isPne: " + isPen);
				sub.getContent().addComponent(
						new OperacionForm(selectedCuenta, null, ot, null, isCargo, isPen));
				getWindow().addWindow(sub);
			}
		}
	}
	
	private class CajaCuentasFilter implements Container.Filter {
	    protected String propertyId;
	    protected List<Cuenta> cajaCuentas;
	    
	    public CajaCuentasFilter(String propertyId, List<Cuenta> cajaCuentas) {
	        this.propertyId = propertyId;
	        this.cajaCuentas = cajaCuentas;
	    }

	    /** Tells if this filter works on the given property. */
	    @Override
	    public boolean appliesToProperty(Object propertyId) {
	        return propertyId != null &&
	               propertyId.equals(this.propertyId);
	    }
	 
	    /** Apply the filter on an item to check if it passes. */
	    @Override
	    public boolean passesFilter(Object itemId, Item item)
	            throws UnsupportedOperationException {
	        // Acquire the relevant property from the item object
	        Property p = item.getItemProperty(propertyId);
	        
	        // Should always check validity
	        if (p == null || !p.getType().equals(Cuenta.class))
	            return false;
	        Cuenta cnta = (Cuenta) p.getValue();
	        
	        for (Cuenta cn : cajaCuentas)
	        	if (cnta.getId().equals(cn.getId())) return true;
	        return false;
	        // The actual filter logic
//	        return cnta.getId().equals
	    }
	}
			
	@Override
	public ExtendedJPAContainer<Operacion> getOperacionContainer() {
		return operacionContainer;
	}

	@Override
	public void drawCuentaForm(Cuenta cuenta, boolean isRefresh) {		
		
	}
}

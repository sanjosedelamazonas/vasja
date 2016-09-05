package org.sanjose.web.accountant;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.sanjose.model.Cuenta;
import org.sanjose.model.Operacion;
import org.sanjose.model.Propietario;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.util.CuentaReporteButtonClickListener;
import org.sanjose.util.DataFilterUtil;
import org.sanjose.util.ExtendedJPAContainer;
import org.sanjose.util.VasjaFieldFactory;
import org.sanjose.web.OperacionForm;
import org.sanjose.web.VasjaApp;
import org.sanjose.web.helper.IOperacionTable;
import org.sanjose.web.helper.ReportHelper;
import org.sanjose.web.helper.SessionHandler;
import org.sanjose.web.helper.SubwindowModal;
import org.sanjose.web.layout.ResizeTableLayout;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.filter.Filters;
import com.vaadin.addon.jpacontainer.provider.CachingBatchableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class AccountantOperacionTable extends ResizeTableLayout implements IOperacionTable {

	static final Action ACTION_ABRIR_COMPROBANTE = new Action("Abrir comprobante");
	static final Action ACTION_IMPRIMIR_COMPROBANTE = new Action("Imprimir comprobante");
	static final Action ACTION_CORRECTION = new Action("Correction");
	static final Action VER_OPERACION = new Action("Ver operacion");
	static final Action ACTION_EDIT_OPERACION = new Action("Editar operacion"); 
	static final Action[] ACTIONS = new Action[] { VER_OPERACION, ACTION_ABRIR_COMPROBANTE, ACTION_IMPRIMIR_COMPROBANTE, 
			ACTION_CORRECTION };
	static final Action[] ACTIONS_EDITABLE = new Action[] { VER_OPERACION, ACTION_ABRIR_COMPROBANTE, ACTION_IMPRIMIR_COMPROBANTE, 
		ACTION_EDIT_OPERACION, ACTION_CORRECTION };
	static final Logger logger = Logger.getLogger(AccountantOperacionTable.class
			.getName());

	Panel mainPanel = new Panel();
	AccountantOperacionComponent opComp = new AccountantOperacionComponent();
	ExtendedJPAContainer<Operacion> operacionContainer;
	MutableLocalEntityProvider<Operacion> operacionEntityProvider;
	IndexedContainer cuentasContainer;
	
	OperacionButtonClickListener cargoListener = null;
	OperacionButtonClickListener abonoListener = null;
	CuentaReporteButtonClickListener cuentaReporteListener = null;
	

	Button operacionesMuestraButton = null;
	boolean firstRun = true;
	VerticalLayout buttonsLayout = null;

	//final Table operacionesTable = new OperacionesTable("Operaciones");
	
	
	public AccountantOperacionTable(Window w) {
		super(w);
		this.table = opComp.getOperacionesTable();
		setResizeMargin(310);
    	setSizeFull();    
		mainPanel.setSizeFull();
		mainPanel.addComponent(opComp);
		mainPanel.setHeight("100.0%");
		mainPanel.setScrollable(true);
		addComponent(mainPanel);
		setExpandRatio(mainPanel, 1f);
		redraw();
	}

	public void redraw() {
		final AccountantOperacionTable aoTable = this;
		opComp.drawCuentaSelection();		
		opComp.getReportesButton().addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (!getWindow().getChildWindows().contains(sub)) {
					removeAllComponents();
					addComponent(new AccountantCajaTable(mainWindow));
					//sub.getContent().removeAllComponents();
				}
			}
		});
		ComboBox cuentas = opComp.getCuentas();
		if ((!SessionHandler.isContador())&&(!SessionHandler.isAdmin())){
		Filter filter = new Compare.Equal("closed", 0);
			DataFilterUtil.bindCuentasBox(filter, opComp.getCuentas());
		}
		else
		DataFilterUtil.bindCuentasBox(opComp.getCuentas());
		
		opComp.getCuentas().addListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if ((event.getProperty()!=null) && (event.getProperty().getValue()!=null)) 
					drawCuentaForm((Cuenta) event.getProperty().getValue(), false);				
			}
		});
		opComp.getCuentas().focus();
	}

	public void drawCuentaForm(Cuenta cuenta, boolean isRefresh) {
		opComp.drawCuentaLabels();
		// refresh cuenta from DB - needed after recalculation
		opComp.getCuentaNombreValue().setValue(cuenta.getNumero() + " " +cuenta.getNombre());
		opComp.getCuentaPropietarioValue().setValue(cuenta.getPropietario()!=null ? cuenta.getPropietario().getNombre() : "");
		opComp.getCuentaRubroValue().setValue(cuenta.getCategoriaCuenta().getNombre()!=null ? cuenta.getCategoriaCuenta().getNombre() : "");
		
		NumberFormat formatter = new DecimalFormat(ConfigurationUtil.get("DECIMAL_FORMAT"));
		String valor="S/. "+formatter.format(cuenta.getPen());
		opComp.getCuentaSolesValue().setValue(valor);
		
		if (cuenta.getPen().compareTo(new BigDecimal(0))<0) 
			opComp.getCuentaSolesValue().setStyleName(ConfigurationUtil.CSS_RED);
		
		valor="$ "+formatter.format(cuenta.getUsd());
		opComp.getCuentaDolaresValue().setValue(valor);
		
		if (cuenta.getUsd().compareTo(new BigDecimal(0))<0) 
			opComp.getCuentaDolaresValue().setStyleName(ConfigurationUtil.CSS_RED);
		AccountantOperacionTable ot = this;
		if (abonoListener!=null) {
			opComp.getAbonoButton().removeListener(abonoListener);			
			opComp.getCargoButton().removeListener(cargoListener);			
			opComp.getCuentaReporteButton().removeListener(cuentaReporteListener);			
		} else {
			opComp.getCerrarButton().addListener(new Button.ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					opComp.drawCuentaSelection();
				}
			});		
		}
		abonoListener = new OperacionButtonClickListener(
				cuenta, ot, false);
		cargoListener = new OperacionButtonClickListener(
				cuenta, ot, true);
		final Table cuentaIds = new Table();
		cuentaIds.addItem(cuenta.getId());
		cuentaReporteListener = new CuentaReporteButtonClickListener(cuenta,cuentaIds, getWindow(), new SubwindowModal("Diario de Cuenta"));
		opComp.getCargoButton().addListener(cargoListener);
		opComp.getAbonoButton().addListener(abonoListener);
		opComp.getCuentaReporteButton().addListener(cuentaReporteListener);

		prepareOperacionesTable(cuenta, isRefresh);
		drawOperacionesTable(cuenta);
	}

	private void prepareOperacionesTable(Cuenta cuenta, boolean isRefresh) {
		if (firstRun) {
			operacionContainer = new ExtendedJPAContainer<Operacion>(
					Operacion.class);
					MutableLocalEntityProvider<Operacion> operacionProvider = ConfigurationUtil.getOperacionEP();
	
			operacionContainer.setEntityProvider(operacionProvider);
			operacionContainer.addNestedContainerProperty("usuario.*");
			operacionContainer.addNestedContainerProperty("cuenta.*");
			operacionContainer.addNestedContainerProperty("banco.*");
			operacionContainer.addNestedContainerProperty("beneficiario.*");
			operacionContainer.addNestedContainerProperty("tipoDocumento.*");
			operacionContainer.addNestedContainerProperty("centroCosto.*");
			operacionContainer.addNestedContainerProperty("lugarGasto.*");
			operacionContainer.addNestedContainerProperty("operacionCorrected.*");
			operacionContainer.sort(new Object[] { "fecha", "id" },
					new boolean[] { false, false });
			opComp.getOperacionesTable().setContainerDataSource(operacionContainer);
		}
		if (isRefresh) {
			CachingBatchableLocalEntityProvider<Operacion> entProv = ((CachingBatchableLocalEntityProvider<Operacion>)operacionContainer.getEntityProvider());
			entProv.flush();
		}
		operacionContainer.removeAllContainerFilters();
		operacionContainer.addContainerFilter(Filters.joinFilter("cuenta",
				Filters.eq("id", cuenta.getId())));

		operacionContainer.applyFilters();
	}

	private void drawOperacionesTable(Cuenta cuenta) {
		//operacionesPanel.addComponent(operacionesTable);
		//cuentaPanel.addComponent(operacionesPanel);
		// turn on column reordering and collapsing
		opComp.getOperacionesTable().setPageLength(Math.round((mainWindow.getHeight()-getResizeMargin())/20f));
		if (firstRun) {
			opComp.getOperacionesTable().setColumnReorderingAllowed(true);
			opComp.getOperacionesTable().setColumnCollapsingAllowed(true);
			opComp.getOperacionesTable().setVisibleColumns(new String[] { "fecha", "id",
					"descripcion", "beneficiario.nombre", "pen", "saldoPen",
					"usd", "saldoUsd", "centroCosto.nombre", "lugarGasto.nombre", "tipo", "banco.nombre", "chequeNumero", "tipoDocumento.nombre", "dni", "usuario.usuario"});
			// set column headers
			opComp.getOperacionesTable().setColumnHeaders(new String[] { "Fecha", "Id",
					"Descripcion", "Firma", "Importe S/.", "SALDO PEN",
					"Importe US$", "SALDO USD", "Centro de costo", "Lugar de Gasto", "Tipo", "Banco", "Cheque Numero", "Documento", "DNI", "Usuario" });
						
			// Column alignment
			opComp.getOperacionesTable().setColumnAlignment("descripcion", Table.ALIGN_CENTER);
			// Column width
			opComp.getOperacionesTable().setColumnExpandRatio("descripcion", 1);
			// table.set
			opComp.getOperacionesTable().setColumnWidth("fecha", 40);
			opComp.getOperacionesTable().setColumnWidth("descripcion", 400);
			opComp.getOperacionesTable().setColumnWidth("firma", 180);
			opComp.getOperacionesTable().setColumnWidth(null, 32);
	
			opComp.getOperacionesTable().setColumnCollapsed("id", true);
			opComp.getOperacionesTable().setColumnCollapsed("saldoPen", true);
			opComp.getOperacionesTable().setColumnCollapsed("saldoUsd", true);
			opComp.getOperacionesTable().setColumnCollapsed("banco.nombre", true);
			opComp.getOperacionesTable().setColumnCollapsed("lugarGasto.nombre", true);
			opComp.getOperacionesTable().setColumnCollapsed("tipoDocumento.nombre", true);
			opComp.getOperacionesTable().setColumnCollapsed("dni", true);
			opComp.getOperacionesTable().setColumnCollapsed("chequeNumero", true);
			opComp.getOperacionesTable().setColumnCollapsed("usuario.usuario", true);
			
			opComp.getOperacionesTable().setColumnAlignment("pen", Table.ALIGN_RIGHT);
			opComp.getOperacionesTable().setColumnAlignment("descripcion", Table.ALIGN_LEFT);
			opComp.getOperacionesTable().setColumnAlignment("usd", Table.ALIGN_RIGHT);
			opComp.getOperacionesTable().setColumnAlignment("saldoUsd", Table.ALIGN_RIGHT);
			opComp.getOperacionesTable().setColumnAlignment("saldoPen", Table.ALIGN_RIGHT);
			
			final IOperacionTable ot = this;
			opComp.getOperacionesTable().addActionHandler(new Action.Handler() {
				public Action[] getActions(Object target, Object sender) {
					Item item = opComp.getOperacionesTable().getItem(target);
 					if (item!=null) {
 						Long id = (Long) item.getItemProperty("id").getValue();
 						Date fecha = (Date) item.getItemProperty("fecha").getValue();
						if (ConfigurationUtil.isOperacionEditable(id)) {
							return ACTIONS_EDITABLE;
						}
					}
					return ACTIONS;
				}

				public void handleAction(Action action, Object sender,
						Object target) {
					if (VER_OPERACION == action) {
						Item item = opComp.getOperacionesTable().getItem(target);
						Long opId = (Long) item.getItemProperty("id")
								.getValue();
						EntityItem<Operacion> eItem = operacionContainer
								.getItem(opId);
						final SubwindowModal sub = new SubwindowModal(
								"Operacion");
						sub.getContent().addComponent(
								new OperacionForm(eItem.getEntity()));
						getWindow().addWindow(sub);					
					} else if (ACTION_ABRIR_COMPROBANTE == action) {
						Item item = opComp.getOperacionesTable().getItem(target);
						Long opId = (Long) item.getItemProperty("id")
								.getValue();
						EntityItem<Operacion> eItem = operacionContainer
								.getItem(opId);
						ReportHelper.generateComprobante(eItem.getEntity(),
								getWindow());
					} else if (ACTION_IMPRIMIR_COMPROBANTE == action) {
						Item item = opComp.getOperacionesTable().getItem(target);
						Long opId = (Long) item.getItemProperty("id")
								.getValue();
						EntityItem<Operacion> eItem = operacionContainer
								.getItem(opId);
						try {
							JasperPrint jrPrint = ReportHelper.printComprobante(eItem.getEntity(), getWindow());
							boolean isPrinted = false;
							if (((VasjaApp)getApplication()).getPrintHelper()!=null)
								isPrinted = ((VasjaApp)getApplication()).getPrintHelper().print(jrPrint, true);
							if (!isPrinted)
								throw new JRException("Problema al consequir un servicio de imprimir");
						} catch (JRException e) {
							e.printStackTrace();
							getWindow().showNotification("Problema al imprimir el comprobante ID: " + opId + " " + e.getMessage());
						}
					} else if (ACTION_CORRECTION == action) {
						Item item = opComp.getOperacionesTable().getItem(target);						
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
					} else if (ACTION_EDIT_OPERACION == action) {
						Item item = opComp.getOperacionesTable().getItem(target);
						Long opId = (Long) item.getItemProperty("id")
								.getValue();
						EntityItem<Operacion> eItem = operacionContainer
								.getItem(opId);
						final SubwindowModal sub = new SubwindowModal(
								"Operacion");
						sub.getContent().addComponent(
								new OperacionPostEditForm(eItem.getEntity(), eItem.getEntity().getCuenta(), ot, eItem.getEntity().getIsPen()));
						getWindow().addWindow(sub);					

					} 
				}
			});
			// Double -click to view details
			opComp.getOperacionesTable().addListener(new ItemClickListener() {
				public void itemClick(ItemClickEvent event) {
					if (event.isDoubleClick()) {
						Object itemId = event.getItemId();
						Operacion op = operacionContainer.getItem(itemId).getEntity();
						SubwindowModal sub = new SubwindowModal("Operacion");
						sub.getContent().addComponent(new OperacionForm(op));
						getWindow().addWindow(sub);						
					}
				}
			});

			// style generator
			opComp.getOperacionesTable().setCellStyleGenerator(new CellStyleGenerator() {
				public String getStyle(Object itemId, Object propertyId) {
					if ("pen".equals(propertyId) || "usd".equals(propertyId)) {
						if ("pen".equals(propertyId) || "usd".equals(propertyId)) {
							BigDecimal value = (BigDecimal)opComp.getOperacionesTable()
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
		opComp.drawOperaciones();
	}

	SubwindowModal sub = new SubwindowModal("Comprobante");
	
	private class OperacionButtonClickListener implements Button.ClickListener {
		Cuenta cuenta;
		boolean isCargo = false;
		AccountantOperacionTable ot;

		public OperacionButtonClickListener(Cuenta cuenta, AccountantOperacionTable ot,
				boolean isCargo) {
			this.cuenta = cuenta;
			this.isCargo = isCargo;
			this.ot = ot;
		}

		public void buttonClick(ClickEvent event) {
			if (!getWindow().getChildWindows().contains(sub)) {
				sub.getContent().removeAllComponents();
				sub.setCaption((isCargo ? "Cargo"
						: "Abono"));
				sub.getContent().addComponent(
						new OperacionForm(cuenta, null, ot, null, isCargo));
				getWindow().addWindow(sub);
			}
		}
	}
	
	private class CuentaFieldFactory extends VasjaFieldFactory {

		final ComboBox proprietarios = new ComboBox("Propietario");

		public CuentaFieldFactory() {
			IndexedContainer c = new IndexedContainer();
			c.addContainerProperty("nombre", String.class, "");

			
			JPAContainer jpaContainer = JPAContainerFactory.make(Propietario.class, ConfigurationUtil.getEntityManager());
			//Collection<Object> bancoIds = 
			Collection<Object> proprietarioIds = jpaContainer.getItemIds();
			for (Object proprietarioId : proprietarioIds) {
				Propietario propietario = (Propietario)jpaContainer.getItem(proprietarioId).getEntity();
				Item item = c.addItem(propietario);
				item.getItemProperty("nombre").setValue(
						propietario.getNombre());
			}

			proprietarios.setContainerDataSource(c);
			proprietarios.setItemCaptionPropertyId("nombre");
			proprietarios.setRequired(true);
			proprietarios.setFilteringMode(ComboBox.FILTERINGMODE_STARTSWITH);
			proprietarios.setEnabled(false);
		}

		@Override
		public Field createField(Item item, Object propertyId,
				Component uiContext) {
			Field f;
			if ("proprietario".equals(propertyId)) {
				return proprietarios;
			} else if ("descripcion".equals(propertyId)) {
				f = createTextArea(propertyId);
				f.setEnabled(false);
			} else if ("fechaDeApertura".equals(propertyId)) {
				f = createDateField(propertyId);
				f.setEnabled(false);
				if (f.getValue() == null)
					f.setValue(new Date(System.currentTimeMillis()));
			} else {
				f = super.createField(item, propertyId, uiContext);
				f.setEnabled(false);
			}
			return f;
		}
	}
	
	@Override
	public ExtendedJPAContainer<Operacion> getOperacionContainer() {
		return operacionContainer;
	}

	// For compatibility with Interface
	@Override
	public void generateTotals() {		
	}
}

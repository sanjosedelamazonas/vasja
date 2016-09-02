package org.sanjose.web;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.sanjose.model.Banco;
import org.sanjose.model.Beneficiario;
import org.sanjose.model.CategoriaCuenta;
import org.sanjose.model.CentroCosto;
import org.sanjose.model.Cuenta;
import org.sanjose.model.CuentaContable;
import org.sanjose.model.LugarGasto;
import org.sanjose.model.Operacion;
import org.sanjose.model.Operacion.Tipo;
import org.sanjose.model.RubroInstitucional;
import org.sanjose.model.RubroProyecto;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.util.CuentaReporteButtonClickListener;
import org.sanjose.util.DataFilterUtil;
import org.sanjose.util.ExtendedJPAContainer;
import org.sanjose.web.accountant.OperacionPostEditForm;
import org.sanjose.web.helper.IOperacionTable;
import org.sanjose.web.helper.ReportHelper;
import org.sanjose.web.helper.SessionHandler;
import org.sanjose.web.helper.SubwindowModal;
import org.sanjose.web.layout.OperacionComponent;
import org.sanjose.web.layout.OperacionFilterLayout;
import org.sanjose.web.layout.ResizeTableLayout;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.filter.Filters;
import com.vaadin.addon.jpacontainer.provider.BatchableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class OperacionTable extends ResizeTableLayout implements IOperacionTable {

	static final Action ACTION_ABRIR_COMPROBANTE = new Action("Ver comprobante");
	static final Action ACTION_IMPRIMIR_COMPROBANTE = new Action("Imprimir comprobante");
	static final Action ACTION_CORRECTION = new Action("Correction");
	static final Action ACTION_VER_OPERACION = new Action("Ver operacion");
	static final Action ACTION_EDIT_OPERACION = new Action("Editar operacion"); 
	static final Action ACTION_DELETE_OPERACION = new Action("Borrar operacion"); 
	static final Action[] ACTIONS = new Action[] { ACTION_VER_OPERACION, ACTION_ABRIR_COMPROBANTE, ACTION_IMPRIMIR_COMPROBANTE, 
		ACTION_CORRECTION };
	static final Action[] ACTIONS_EDITABLE = new Action[] { ACTION_VER_OPERACION, ACTION_EDIT_OPERACION, ACTION_DELETE_OPERACION, ACTION_ABRIR_COMPROBANTE, ACTION_IMPRIMIR_COMPROBANTE, 
		ACTION_CORRECTION };
	static final Logger logger = Logger.getLogger(OperacionTable.class
			.getName());

	//final Form cuentaForm = new Form();
	//Panel operacionesPanel = new Panel();
	ExtendedJPAContainer<Operacion> operacionContainer = null;
	MutableLocalEntityProvider<Operacion> operacionEntityProvider;
	
	OperacionButtonClickListener cargoListener = null;
	OperacionButtonClickListener abonoListener = null;
	CuentaReporteButtonClickListener reporteListener = null;
	
	// Filters
	OperacionFilterLayout operacionFilters = new OperacionFilterLayout();
	HashMap<String, Filter> appliedFilters = new HashMap<String, Filter>();
	HashMap<String, Property.ValueChangeListener> filterListeners = new HashMap<String, Property.ValueChangeListener>();
	
	

	//HorizontalLayout hLayout = new HorizontalLayout();
	boolean firstRun = true;
	OperacionComponent opComp = null;

	public OperacionTable(Window w) {
		super(w);
    	setResizeMargin(320);
    	setSizeFull();
		redraw();
	}

	public void redraw() {
		if (opComp==null) {
			opComp = new OperacionComponent();		//opComp.removeAllComponents();
			this.table = opComp.getOperacionesTable();
			setMargin(true, true, false, true);
			opComp.setCuentas(opComp.getCuentas());
			addComponent(opComp);
			setExpandRatio(opComp, 1);
	        setWidth("100.0%");	        
		}
		setCaption("Operaciones");
		DataFilterUtil.bindComboBox(opComp.getCategorias(), "nombre", "filtrar por categoria" ,CategoriaCuenta.class, null);
		opComp.getCategorias().setNullSelectionAllowed(true);
		opComp.getCategorias().addListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if ((event.getProperty()!=null) && (event.getProperty().getValue()!=null)) {
					if (appliedFilters.containsKey("categoriaCuenta")) { 
						operacionContainer.removeContainerFilter(appliedFilters.get("categoriaCuenta"));
						operacionContainer.applyFilters();
						appliedFilters.remove("categoriaCuenta");
					}
					CategoriaCuenta cc = (CategoriaCuenta) event.getProperty().getValue();
					DataFilterUtil.bindCuentasBox(Filters.eq("categoriaCuenta", cc), opComp.getCuentas());
					
					Filter f = Filters.eq("cuenta.categoriaCuenta",cc);
					operacionContainer.addContainerFilter(f);
					appliedFilters.put("categoriaCuenta", f);
					operacionContainer.applyFilters();
					generateTotals();
				} else {
					//((JPAContainer)opComp.getCuentas().getContainerDataSource()).removeAllContainerFilters();
					DataFilterUtil.bindCuentasBox(opComp.getCuentas());
					operacionContainer.removeContainerFilter(appliedFilters.get("categoriaCuenta"));
					if (appliedFilters.containsKey("categoriaCuenta")) 
						appliedFilters.remove("categoriaCuenta");
					operacionContainer.applyFilters();
					generateTotals();
				}
			}
		});
		
		DataFilterUtil.bindCuentasBox(opComp.getCuentas());
		opComp.getCuentas().setNullSelectionAllowed(true);
		opComp.getCuentas().addListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if ((event.getProperty()!=null) && (event.getProperty().getValue()!=null)) {
					Cuenta selCuenta = (Cuenta) event.getProperty().getValue();
					logger.fine("Filters before: " + appliedFilters + " " + operacionContainer.getAppliedFilters());
					appliedFilters.remove("rubroProyecto");
					drawCuentaForm(selCuenta, false);
					if (appliedFilters.containsKey("cuenta")) {
						operacionContainer.removeContainerFilter(appliedFilters.get("cuenta"));
						operacionContainer.removeContainerFilters("cuenta");
						appliedFilters.remove("cuenta");
						// Remove rubroProyecto cuenta filter
						DataFilterUtil.bindRubroProyectoBox(opComp.getOpFilterLayout().getRubroProyectoFilter(), 
								operacionContainer, appliedFilters, filterListeners, null);
					}
					if (selCuenta!=null) {
						Filter f = Filters.joinFilter("cuenta", Filters.eq("id", selCuenta.getId()));
						operacionContainer.addContainerFilter(f);
						appliedFilters.put("cuenta", f);
						operacionContainer.applyFilters();
						
						// Adding filter to rubroProyecto so that only Rubros for selected cuenta are displayed
						DataFilterUtil.bindRubroProyectoBox(opComp.getOpFilterLayout().getRubroProyectoFilter(), 
								operacionContainer, appliedFilters, filterListeners, selCuenta);
						
					}
					logger.fine("Filters after: " + appliedFilters + " " + operacionContainer.getAppliedFilters());
					//resetFilters((Cuenta) event.getProperty().getValue());											
				} else {
					operacionContainer.removeContainerFilters("cuenta");
					appliedFilters.remove("cuenta");
					logger.info("Redraw cuenta form, no cuenta selected");
					drawCuentaForm(null, false);
				}
			}
		});
		//if (operacionContainer!=null) resetFilters(null);
		drawCuentaForm(null, false);
	}

	public void drawCuentaForm(Cuenta cuenta, boolean refresh) {
		if (cuenta!=null) {
			opComp.getCuentaLabel().setValue("Listado de operaciones de cuenta: " + cuenta.getNumero() + " " + cuenta.getNombre() +  
					"  Saldo actual en Soles:  s/. " + cuenta.getPen() + "    Dolares: $ " + cuenta.getUsd());
			//opComp.getCuentaLabel().setStyleName("h2");
			opComp.getSolesLabel().setCaption("Soles: ");
			opComp.getSolesLabel().setValue(cuenta.getPen());
			if (cuenta.getPen().compareTo(new BigDecimal(0))<0) 
				opComp.getSolesLabel().setStyleName(ConfigurationUtil.CSS_RED);
			opComp.getDolaresLabel().setCaption("Dolares: ");
			opComp.getDolaresLabel().setValue(cuenta.getUsd());
			if (cuenta.getUsd().compareTo(new BigDecimal(0))<0) 
				opComp.getDolaresLabel().setStyleName(ConfigurationUtil.CSS_RED);
		} else
			opComp.getCuentaLabel().setValue("");
		OperacionTable ot = this;
		if (abonoListener!=null) {
			opComp.getAbonoButton().removeListener(abonoListener);			
			opComp.getCargoButton().removeListener(cargoListener);	
			opComp.getReporteButton().removeListener(reporteListener);	
		} else {
			opComp.getAbonoButton().setIcon(new ThemeResource("../icons/sign_add.png"));
			opComp.getCargoButton().setIcon(new ThemeResource("../icons/sign_remove.png"));
			opComp.getReporteButton().setIcon(new ThemeResource("../icons/window_text.png"));
		}
		abonoListener = new OperacionButtonClickListener(
				cuenta, ot, false);
		cargoListener = new OperacionButtonClickListener(				
				cuenta, ot, true);
		reporteListener = new CuentaReporteButtonClickListener(cuenta, getWindow(), sub);
		opComp.getCargoButton().addListener(cargoListener);
		opComp.getAbonoButton().addListener(abonoListener);
		opComp.getReporteButton().addListener(reporteListener);
		if (cuenta==null) {
			opComp.getCargoButton().setVisible(false);
			opComp.getAbonoButton().setVisible(false);
			opComp.getReporteButton().setVisible(false);
		} else {
			opComp.getCargoButton().setVisible(true);
			opComp.getAbonoButton().setVisible(true);
			opComp.getReporteButton().setVisible(true);
			if (cuenta.getIsCaja()) {
				opComp.getCargoButton().setEnabled(false);
				opComp.getAbonoButton().setEnabled(false);
			} else {
				opComp.getCargoButton().setEnabled(true);
				opComp.getAbonoButton().setEnabled(true);
			}
		}
		drawOperacionesTable(cuenta, refresh);
	}

	private void drawOperacionesTable(final Cuenta cuenta, boolean refresh) {
		if (firstRun || (refresh && SessionHandler.getVasjaApp().getOperacionContainer()==null)) {
			opComp.getOperacionesTable().setLocale(ConfigurationUtil.LOCALE);
			opComp.getOperacionesTable().setStyleName(ConfigurationUtil.get("CSS_STYLE"));
			// size
			opComp.getOperacionesTable().setWidth("100%");
		    //int shownRowsCount = opComp.getOperacionesTable().getVisibleItemIds().size();
		    opComp.getOperacionesTable().setPageLength(Math.round((mainWindow.getHeight()-320)/20.6f));
			//opComp.getOperacionesTable().setHeight("-1px");
			opComp.getOperacionesTable().setSelectable(true);
			opComp.getOperacionesTable().setImmediate(true); // react at once when something is
			operacionContainer = new ExtendedJPAContainer<Operacion>(
					Operacion.class);
			MutableLocalEntityProvider<Operacion> operacionProvider = ConfigurationUtil.getOperacionEP();

			operacionContainer.setEntityProvider(operacionProvider);
			operacionContainer.addNestedContainerProperty("usuario.usuario");
			operacionContainer.addNestedContainerProperty("beneficiario.nombre");
			operacionContainer.addNestedContainerProperty("tipoDocumento.abreviacion");
			operacionContainer.addNestedContainerProperty("centroCosto.isLeaf");
			operacionContainer.addNestedContainerProperty("centroCosto.codigo");
			operacionContainer.addNestedContainerProperty("lugarGasto.isLeaf");
			operacionContainer.addNestedContainerProperty("lugarGasto.codigo");
			operacionContainer.addNestedContainerProperty("cuentaContable.isLeaf");
			operacionContainer.addNestedContainerProperty("cuentaContable.codigo");
			operacionContainer.addNestedContainerProperty("rubroInstitucional.isLeaf");
			operacionContainer.addNestedContainerProperty("rubroInstitucional.codigo");
			operacionContainer.addNestedContainerProperty("rubroProyecto.isLeaf");
			operacionContainer.addNestedContainerProperty("rubroProyecto.codigo");
			operacionContainer.addNestedContainerProperty("cuenta.id");
			operacionContainer.addNestedContainerProperty("cuenta.categoriaCuenta.*");
			operacionContainer.addNestedContainerProperty("cuenta.numero");
			operacionContainer.addNestedContainerProperty("banco.nombre");
			operacionContainer.addNestedContainerProperty("operacionCorrected.id");
			operacionContainer.addNestedContainerProperty("operacionDetalle.id");
			operacionContainer.sort(new Object[] { "fecha", "id" },
					new boolean[] { false, false });
			opComp.getOperacionesTable().setContainerDataSource(operacionContainer);
			
			opComp.getOpFilterLayout().getRemoveFilter().addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					resetFilters((Cuenta)opComp.getCuentas().getValue());
				}
			});
			DataFilterUtil.bindBooleanComboBox(opComp.getOpFilterLayout().getIsCargoFilter(),
					"isCargo", "Cargo/Abono", new String[] { "CARGO", "ABONO"}, operacionContainer, appliedFilters, filterListeners, this);
			DataFilterUtil.bindBooleanComboBox(opComp.getOpFilterLayout().getIsPenFilter(),
					"isPen", "PEN/USD", new String[] { "PEN", "USD"}, operacionContainer, appliedFilters, filterListeners, this);
			DataFilterUtil.bindDateField(opComp.getOpFilterLayout().getFechaMin(), 1,
					"fecha", "fecha min", operacionContainer, appliedFilters, filterListeners, this);
			DataFilterUtil.bindDateField(opComp.getOpFilterLayout().getFechaMax(), -1,
					"fecha", "fecha max", operacionContainer, appliedFilters, filterListeners, this);
			DataFilterUtil.bindComboBox(opComp.getOpFilterLayout().getBancoFilter(), "nombre",
					"filtro banco", Banco.class,
					operacionContainer, "banco", appliedFilters, filterListeners, this);
			DataFilterUtil.bindComboBox(opComp.getOpFilterLayout().getTipoFilter(), "tipo",
					"filtro tipo", Tipo.values(),
					operacionContainer, appliedFilters, filterListeners, this);
			DataFilterUtil.bindSearchTextField(opComp.getOpFilterLayout().getDescripcionFilter(),
					"descripcion", "busca descripcion", operacionContainer, appliedFilters, filterListeners, this);
			DataFilterUtil.bindSearchTextField(opComp.getOpFilterLayout().getDetalleFilter(),
					"chequeNumero", "busca detalle", operacionContainer, appliedFilters, filterListeners, this);
			DataFilterUtil.bindComboBox(opComp.getOpFilterLayout().getBeneficiarioFilter(), "nombre",
					"filtro firma", Beneficiario.class,
					operacionContainer, "beneficiario", appliedFilters, filterListeners, this);
			
			DataFilterUtil.bindComboBox(opComp.getOpFilterLayout().getCentroCostoFilter(), "codigo",
					"filtro centro costo", CentroCosto.class,
					operacionContainer, "centroCosto", appliedFilters, filterListeners, this);
			
			DataFilterUtil.bindComboBox(opComp.getOpFilterLayout().getLugarGastoFilter(), "codigo",
					"filtro lugar gasto", LugarGasto.class,
					operacionContainer, "lugarGasto", appliedFilters, filterListeners, this);	
			
			DataFilterUtil.bindComboBox(opComp.getOpFilterLayout().getCuentaContableFilter(), "codigo",
					"filtro cuenta contable", CuentaContable.class,
					operacionContainer, "cuentaContable", appliedFilters, filterListeners, this);
			
			DataFilterUtil.bindComboBox(opComp.getOpFilterLayout().getRubroInstitucionalFilter(), "codigo",
					"filtro rubro institucional", RubroInstitucional.class,
					operacionContainer, "rubroInstitucional", appliedFilters, filterListeners, this);
			
			DataFilterUtil.bindRubroProyectoBox(opComp.getOpFilterLayout().getRubroProyectoFilter(), 
					operacionContainer, appliedFilters, filterListeners, cuenta);
			
			DataFilterUtil.bindRubroInstitucionalBox(opComp.getOpFilterLayout().getRubroInstitucionalFilter(),
					operacionContainer, appliedFilters, filterListeners);
			
			SessionHandler.getVasjaApp().setOperacionContainer(operacionContainer);
	
		} else if (SessionHandler.getVasjaApp().getOperacionContainer()!=null) {
			operacionContainer = SessionHandler.getVasjaApp().getOperacionContainer();
		}
		
		Date fechaMin = new Date();
		if (firstRun && !appliedFilters.containsKey("fecha1")) {		
			fechaMin = ConfigurationUtil.getBeginningOfMonth(fechaMin);
			//fechaMin = ConfigurationUtil.dateAddDays(fechaMin, -30);
			Filter f = Filters.gteq("fecha", ConfigurationUtil.getBeginningOfDay(fechaMin));
			if (!operacionContainer.getAppliedFilters().contains(f)) operacionContainer.addContainerFilter(f);
			operacionContainer.applyFilters();
			opComp.getOpFilterLayout().getFechaMin().setValue(fechaMin);
			appliedFilters.put("fecha1", f);
		}

		
		Filter fCuenta = null;
		if (cuenta!=null) fCuenta = Filters.joinFilter("cuenta",
				Filters.eq("id", cuenta.getId()));
		if (cuenta!=null && operacionContainer.getAppliedFilters().contains(fCuenta)) {
			operacionContainer.addContainerFilter(fCuenta);
		}
		if (!appliedFilters.isEmpty()) {
			for (Filter f : appliedFilters.values()) 
				if (!operacionContainer.getAppliedFilters().contains(f)) operacionContainer.addContainerFilter(f);
		}				
		
		opComp.getOperacionesTable().removeGeneratedColumn("abonoPen"); 
		opComp.getOperacionesTable().addGeneratedColumn("abonoPen", new ColumnGenerator() {
		@Override
			public Component generateCell(Table source, Object itemId, Object columnId) {
				Item it = (Item)source.getItem(itemId);
				if (!(Boolean)it.getItemProperty("isCargo").getValue()) 
					return new Label(source.getItem(itemId).getItemProperty("pen"));
				else
					return null;
			}
		});
		opComp.getOperacionesTable().removeGeneratedColumn("cargoPen"); 
		opComp.getOperacionesTable().addGeneratedColumn("cargoPen", new ColumnGenerator() {
		@Override
			public Component generateCell(Table source, Object itemId, Object columnId) {
				Item it = (Item)source.getItem(itemId);
				if ((Boolean)it.getItemProperty("isCargo").getValue()) 
					return new Label(source.getItem(itemId).getItemProperty("pen"));
				else
					return null;
			}
		});
		opComp.getOperacionesTable().removeGeneratedColumn("abonoUsd"); 
		opComp.getOperacionesTable().addGeneratedColumn("abonoUsd", new ColumnGenerator() {
		@Override
			public Component generateCell(Table source, Object itemId, Object columnId) {
				Item it = (Item)source.getItem(itemId);
				if (!(Boolean)it.getItemProperty("isCargo").getValue()) 
					return new Label(source.getItem(itemId).getItemProperty("usd"));
				else
					return null;
			}
		});
		opComp.getOperacionesTable().removeGeneratedColumn("cargoUsd"); 
		opComp.getOperacionesTable().addGeneratedColumn("cargoUsd", new ColumnGenerator() {
		@Override
			public Component generateCell(Table source, Object itemId, Object columnId) {
				Item it = (Item)source.getItem(itemId);
				if ((Boolean)it.getItemProperty("isCargo").getValue()) 
					return new Label(source.getItem(itemId).getItemProperty("usd"));
				else
					return null;
			}
		});
		/*operacionContainer.addFilter(f);
		appliedFilters.put("fecha1", f);
		operacionContainer.applyFilters();*/
		
		//buttonsLayout.removeComponent(operacionesMuestraButton);
	//	operacionesMuestraButton = new Button("< Operaciones",
//				new OperacionesMuestraButtonListener(false, cuenta));
	//	operacionesMuestraButton.setWidth("8em");
		// operacionesMuestraButton.setHeight("6em");
		//buttonsLayout.addComponent(operacionesMuestraButton);
		opComp.drawOperacionesTable();
		//operacionFilters.getFechaMin().setValue(ConfigurationUtil.getBeginningOfDay(fechaMin));
		operacionFilters.getFechaMin().requestRepaint();
		// turn on column reordering and collapsing

		opComp.getOperacionesTable().setColumnReorderingAllowed(true);
		opComp.getOperacionesTable().setColumnCollapsingAllowed(true);
		
		opComp.getOperacionesTable().setVisibleColumns(new String[] { "id","cuenta.numero", "fecha", "fechaDeCobro",
				"descripcion", "centroCosto.codigo", "lugarGasto.codigo", "cuentaContable.codigo","rubroInstitucional.codigo","rubroProyecto.codigo","beneficiario.nombre", "tipoDocumento.abreviacion", "abonoPen", "cargoPen", "saldoPen", 
				"abonoUsd", "cargoUsd", "saldoUsd", "tipo", "banco.nombre", "chequeNumero", "usuario.usuario", "pen", "usd", "operacionDetalle.id" });
		
		// set column headers
		opComp.getOperacionesTable().setColumnHeaders(new String[] { "ID", "CTA", "Fecha", "Fecha Cobro",
				"Descripcion", "CCosto", "LGasto","CContable","PstInstitucional","PstProyecto", "Firma", "Documento", "Abono PEN", "Cargo PEN", "Saldo PEN",
 				"Abono USD", "Cargo USD", "Saldo USD", "Tipo", "Banco", "Detalle", "Usuario", "PEN", "USD", "Detalle ID" });
		
		// Column alignment
		opComp.getOperacionesTable().setColumnCollapsed("id",true);
		opComp.getOperacionesTable().setColumnWidth("cuenta.numero", 35);
		opComp.getOperacionesTable().setColumnAlignment("cuenta.numero",Table.ALIGN_RIGHT);
		opComp.getOperacionesTable().setColumnWidth("fecha",35);
		opComp.getOperacionesTable().setColumnAlignment("descripcion", Table.ALIGN_LEFT);
		opComp.getOperacionesTable().setColumnExpandRatio("descripcion", 1);
		opComp.getOperacionesTable().setColumnWidth("descripcion", 350);
		opComp.getOperacionesTable().setColumnWidth("centroCosto.codigo",50);
		opComp.getOperacionesTable().setColumnWidth("lugarGasto.codigo",50);
		opComp.getOperacionesTable().setColumnWidth("cuentaContable.codigo",50);
		opComp.getOperacionesTable().setColumnWidth("rubroInstitucional.codigo",50);
		opComp.getOperacionesTable().setColumnWidth("rubroProyecto.codigo",50);
		opComp.getOperacionesTable().setColumnWidth("beneficiario.nombre",100);
		opComp.getOperacionesTable().setColumnCollapsed("tipoDocumento.abreviacion",true);
		opComp.getOperacionesTable().setColumnWidth("pen", 55);
		opComp.getOperacionesTable().setColumnWidth("usd", 55);
		opComp.getOperacionesTable().setColumnWidth("cargoPen", 70);
		opComp.getOperacionesTable().setColumnWidth("abonoPen", 70);
		opComp.getOperacionesTable().setColumnWidth("cargoUsd", 70);
		opComp.getOperacionesTable().setColumnWidth("abonoUsd", 70);
		opComp.getOperacionesTable().setColumnAlignment("pen", Table.ALIGN_RIGHT);
		opComp.getOperacionesTable().setColumnAlignment("usd", Table.ALIGN_RIGHT);
		opComp.getOperacionesTable().setColumnAlignment("cargoPen", Table.ALIGN_RIGHT);
		opComp.getOperacionesTable().setColumnAlignment("cargoUsd", Table.ALIGN_RIGHT);
		opComp.getOperacionesTable().setColumnAlignment("abonoPen", Table.ALIGN_RIGHT);
		opComp.getOperacionesTable().setColumnAlignment("abonoUsd", Table.ALIGN_RIGHT);
		opComp.getOperacionesTable().setColumnCollapsed("fechaDeCobro", true);
		opComp.getOperacionesTable().setColumnCollapsed("usuario.usuario", true);		
		opComp.getOperacionesTable().setColumnCollapsed("pen", true);		
		opComp.getOperacionesTable().setColumnCollapsed("usd", true);
		opComp.getOperacionesTable().setColumnCollapsed("operacionDetalle.id", true);		
		opComp.getOperacionesTable().setFooterVisible(true);
		generateTotals();
		
		if (firstRun) {
			final OperacionTable ot = this;
			opComp.getOperacionesTable().addActionHandler(new Action.Handler() {
					public Action[] getActions(Object target, Object sender) {
						Item item = opComp.getOperacionesTable().getItem(target);
						if (item!=null) {
							Date fecha = (Date)item.getItemProperty("fecha").getValue();
							Long detalleId = (Long)item.getItemProperty("operacionDetalle.id").getValue();
							if (detalleId==null && ConfigurationUtil.getUltimoMesCerrado().before(fecha)) {
								return ACTIONS_EDITABLE;							
							} else 
								return ACTIONS;									
						}
						return ACTIONS;
					}
	
					public void handleAction(Action action, Object sender,
							Object target) {
						if (ACTION_VER_OPERACION == action) {
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
						}  else if (ACTION_EDIT_OPERACION == action) {
							Item item = opComp.getOperacionesTable().getItem(target);
							Long opId = (Long) item.getItemProperty("id")
									.getValue();
							EntityItem<Operacion> eItem = operacionContainer
									.getItem(opId);
							final SubwindowModal sub = new SubwindowModal(
									"Operacion");
							sub.getContent().addComponent(
									new OperacionPostEditForm(eItem.getEntity(), cuenta, ot, eItem.getEntity().getIsPen()));
							getWindow().addWindow(sub);
						} else if (ACTION_DELETE_OPERACION == action) {
							Item item = opComp.getOperacionesTable().getItem(target);
							Long opId = (Long) item.getItemProperty("id")
									.getValue();
							final EntityItem<Operacion> eItem = operacionContainer
									.getItem(opId);
							final SubwindowModal sub = new SubwindowModal(
									"Eliminar operacion");
							Label message = new Label("?Eliminar la operacion: (ID) "
									+ (Long) item.getItemProperty("id").getValue()
									+ ", " + item.getItemProperty("descripcion").getValue()
									+ "?");

							sub.getContent().addComponent(message);
							((VerticalLayout) sub.getContent()).setComponentAlignment(
									message, Alignment.MIDDLE_CENTER);
							HorizontalLayout hLayout = new HorizontalLayout();
							hLayout.setMargin(true);
							hLayout.setSpacing(true);

							Button yes = new Button("Si", new Button.ClickListener() {
								public void buttonClick(ClickEvent event) {
									deleteOperacion(eItem);
									//container.removeItem(eItem.getItemId());
									//container.commit();
									(sub.getParent()).removeWindow(sub);
								}
							});
							Button no = new Button("No", new Button.ClickListener() {
								public void buttonClick(ClickEvent event) {
									(sub.getParent()).removeWindow(sub);
								}
							});
							hLayout.addComponent(yes);
							hLayout.addComponent(no);
							sub.getContent().addComponent(hLayout);
							((VerticalLayout) sub.getContent()).setComponentAlignment(
									hLayout, Alignment.MIDDLE_CENTER);
							getWindow().addWindow(sub);
						} else if (ACTION_ABRIR_COMPROBANTE == action) {
							Item item = opComp.getOperacionesTable().getItem(target);
							Long opId = (Long) item.getItemProperty("id")
									.getValue();
							EntityItem<Operacion> eItem = operacionContainer
									.getItem(opId);
							ReportHelper.generateComprobante(eItem.getEntity(),
									getWindow());
						} if (ACTION_IMPRIMIR_COMPROBANTE == action) {
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
										new OperacionForm(op.getCuenta(), ot, op
												.getIsCargo(), op));
								getWindow().addWindow(sub);
							}
							else 
								getWindow().showNotification("Problema con la correccion", "No se puede corregir una correccion", Notification.TYPE_HUMANIZED_MESSAGE);
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
							BigDecimal value = (BigDecimal)opComp.getOperacionesTable()
									.getContainerProperty(itemId, propertyId)
									.getValue();
							if (value.compareTo(new BigDecimal(0))<0)									
								return "negativeNumber";							
						}
						return null;
					}
			});
			// create the export buttons
	        final ThemeResource export = new ThemeResource("../icons/table-excel.png");
	        //final Button regularExportButton = new Button("Exportar");
	        opComp.getRegularExportButton().setIcon(export);
	        opComp.getRegularExportButton().addListener(new ClickListener() {
	            private static final long serialVersionUID = -73954695086117200L;
	            private ExcelExport excelExport;

	            @Override
	            public void buttonClick(final ClickEvent event) {
	            	try {
	                excelExport = new ExcelExport(opComp.getOperacionesTable(), 
	                		(cuenta!=null ? "Operaciones cuenta " + cuenta.getNumero() : "Operaciones"));
	                //excelExport.excludeCollapsedColumns();
	                excelExport.setReportTitle((cuenta!=null ? "Operaciones cuenta " + cuenta.getNumero() :
	                	"Operaciones para todos cuentas"));
	                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
	                excelExport.setExportFileName("Export_Operaciones_" + sdf.format(new Date()) + ".xls");
                    excelExport.setDisplayTotals(true);
	                excelExport.setRowHeaders(false);
	                excelExport.setDateDataFormat("yyyy.MM.dd");
	                excelExport.setExcelFormatOfProperty("id",  "##0");
	                excelExport.setExcelFormatOfProperty("cuenta.numero",  "##0");
	                excelExport.setExcelFormatOfProperty("pen",  ConfigurationUtil.get("DECIMAL_FORMAT"));
	                excelExport.setExcelFormatOfProperty("usd",  ConfigurationUtil.get("DECIMAL_FORMAT"));
	                excelExport.setExcelFormatOfProperty("saldopen", ConfigurationUtil.get("DECIMAL_FORMAT"));
	                excelExport.setExcelFormatOfProperty("saldousd", ConfigurationUtil.get("DECIMAL_FORMAT"));
	                excelExport.export();
	            	} catch (Exception e) {
	            		getWindow().showNotification("Problema con el exporte de operaciones", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
	            	}
	            }
	        });
			//opComp.getOperacionesPanel().addComponent(regularExportButton);
			firstRun = false;
		}
	}

	public void resetFilters(Cuenta cuenta) {
		logger.info("Resetting filters");
		opComp.getOpFilterLayout().getFechaMax().setValue(null);
		opComp.getOpFilterLayout().getFechaMin().setValue(null);
		opComp.getOpFilterLayout().getDescripcionFilter().setValue("");
		opComp.getOpFilterLayout().getIsCargoFilter().setValue(null);					
		opComp.getOpFilterLayout().getIsPenFilter().setValue(null);
		opComp.getOpFilterLayout().getBancoFilter().setValue(null);
		opComp.getOpFilterLayout().getTipoFilter().setValue(null);
		//opComp.getOpFilterLayout().getCentroCostoFilter().
		opComp.getOpFilterLayout().getCentroCostoFilter().setValue(null);
		opComp.getOpFilterLayout().getLugarGastoFilter().setValue(null);
		opComp.getOpFilterLayout().getRubroProyectoFilter().setValue(null);
		opComp.getOpFilterLayout().getRubroInstitucionalFilter().setValue(null);
		opComp.getOpFilterLayout().getCuentaContableFilter().setValue(null);
		//opComp.getOpFilterLayout().getDetalleFilter().setValue("");
		opComp.getOpFilterLayout().getBeneficiarioFilter().setValue(null);
		
		operacionContainer.removeAllContainerFilters();
		appliedFilters.clear();
		if (cuenta!=null) {
			Filter f = Filters.joinFilter("cuenta", Filters.eq("id", cuenta.getId()));
			operacionContainer.addContainerFilter(f);
			appliedFilters.put("cuenta", f);
		}
		if (opComp.getCategorias().getValue()!=null) {
			CategoriaCuenta cc = (CategoriaCuenta)opComp.getCategorias().getValue();		
			DataFilterUtil.bindCuentasBox(Filters.eq("categoriaCuenta", cc), opComp.getCuentas());		
			Filter f = Filters.eq("cuenta.categoriaCuenta",cc);
			operacionContainer.addContainerFilter(f);
			appliedFilters.put("categoriaCuenta", f);
		}		
		
		
		operacionContainer.applyFilters();
		generateTotals();
	}
	
	
	private boolean deleteOperacion(EntityItem<Operacion> oItem) {
		// Actually write the operation to the database
		EntityTransaction trans = null;
		EntityManager em = null;
		try {
			em = ConfigurationUtil.getNewEntityManager();
			MutableLocalEntityProvider<Operacion> operacionEP = new BatchableLocalEntityProvider<Operacion>(
					Operacion.class, em);
			MutableLocalEntityProvider<Cuenta> cuentaEP = new BatchableLocalEntityProvider<Cuenta>(
					Cuenta.class, em);			
			operacionEP.setTransactionsHandledByProvider(false);
			cuentaEP.setTransactionsHandledByProvider(false);
			trans = em.getTransaction();
			trans.begin();
			Operacion oper = (Operacion)oItem.getEntity();
			logger.info("DELETING: " + oper);
			Date fechaToRecalculate = oper.getFecha();
			Operacion operCajaBanco = ConfigurationUtil.getOperacionByOperacionDetalle(oper.getId(), em);
			logger.info("OPERACION C/B TO DELETE: " + operCajaBanco);
			//
			Long cuentaId = oper.getCuenta().getId();
			Long cuentaDetalleId = null;
			if (operCajaBanco!=null) cuentaDetalleId = operCajaBanco.getCuenta().getId();
			logger.info("OLD Saldo on cuenta: " + oper.getCuenta().getNumero() + ": " + oper.getCuenta().getPen() + " " + oper.getCuenta().getUsd());
			if (operCajaBanco!=null) logger.info("OLD Saldo on cuentaC/B: "+ operCajaBanco.getCuenta().getNumero() + ": " + operCajaBanco.getCuenta().getPen() + " " + operCajaBanco.getCuenta().getUsd());
		
			oper = operacionEP.getEntityManager().find(Operacion.class, oper.getId());
			if (operCajaBanco!=null) operCajaBanco = operacionEP.getEntityManager().find(Operacion.class, operCajaBanco.getId());
			operacionEP.getEntityManager().remove(oper);
			if (operCajaBanco!=null) operacionEP.getEntityManager().remove(operCajaBanco);
			// Recalculate the actual account
			OperacionForm.recalculateSaldos(cuentaId, fechaToRecalculate, operacionEP.getEntityManager());
			// Recalculate the cuenta/banco account
			if (operCajaBanco!=null) OperacionForm.recalculateSaldos(cuentaDetalleId, fechaToRecalculate, operacionEP.getEntityManager());
			Cuenta cuentaAfter = cuentaEP.getEntityManager().find(Cuenta.class, cuentaId);
			Cuenta cuentaDetalleAfter = null;
			if (cuentaDetalleId!=null) 
				cuentaDetalleAfter = cuentaEP.getEntityManager().find(Cuenta.class, cuentaDetalleId);
			logger.info("NEW Saldo on cuenta: " + cuentaAfter.getNumero() + ": " + cuentaAfter.getPen() + " " + cuentaAfter.getUsd());
			if (operCajaBanco!=null) logger.info("NEW Saldo on cuentaC/B: " + cuentaDetalleAfter.getNumero() + ": " + cuentaDetalleAfter.getPen() + " " + cuentaDetalleAfter.getUsd());
			trans.commit();
			logger.info("committed");
			if (operCajaBanco!=null) opComp.getOperacionesTable().removeItem(operCajaBanco.getId());
			logger.info("removed operCajaBanco");
			opComp.getOperacionesTable().removeItem(oper.getId());
			logger.info("removed oper");
			redraw();
		} catch (Exception e) {
			if ((trans!=null) && (trans.isActive())) {
				logger.info("Trans is active rolling back...");
				trans.rollback();
				logger.info("Rolled back");
				em.close();
			}
			e.printStackTrace();					
			getWindow().showNotification("Problema con la operacion ", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
		}
		return true;
	}
	
	
	public void generateTotals() {
		BigDecimal totalAbono = new BigDecimal(0.00);
		BigDecimal totalCargo = new BigDecimal(0.00);
		for (Object id : opComp.getOperacionesTable().getItemIds()) {
			Item item = opComp.getOperacionesTable().getItem(id);
			boolean isCargo = (Boolean)item.getItemProperty("isCargo").getValue();
			if (!isCargo) 
				totalAbono = totalAbono.add((BigDecimal)item.getItemProperty("pen").getValue());
			else
				totalCargo = totalCargo.add((BigDecimal)item.getItemProperty("pen").getValue());
		}
		opComp.getOperacionesTable().setColumnFooter("abonoPen", "S./ " + totalAbono.toString());
		opComp.getOperacionesTable().setColumnFooter("cargoPen", "S./ " + totalCargo.toString());
		totalAbono = new BigDecimal(0.00);
		totalCargo = new BigDecimal(0.00);
		for (Object id : opComp.getOperacionesTable().getItemIds()) {
			Item item = opComp.getOperacionesTable().getItem(id);
			boolean isCargo = (Boolean)item.getItemProperty("isCargo").getValue();
			if (!isCargo) 
				totalAbono = totalAbono.add((BigDecimal)item.getItemProperty("usd").getValue());
			else
				totalCargo = totalCargo.add((BigDecimal)item.getItemProperty("usd").getValue());
		}
		opComp.getOperacionesTable().setColumnFooter("abonoUsd", "$ " + totalAbono.toString());
		opComp.getOperacionesTable().setColumnFooter("cargoUsd", "$ " + totalCargo.toString());			
	}
	
	
	
	SubwindowModal sub = new SubwindowModal("Comprobante");
	
	private class OperacionButtonClickListener implements Button.ClickListener {
		Cuenta cuenta;
		boolean isCargo = false;
		OperacionTable ot;

		public OperacionButtonClickListener(Cuenta cuenta, OperacionTable ot,
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

	@Override
	public ExtendedJPAContainer<Operacion> getOperacionContainer() {
		return operacionContainer;
	}
}

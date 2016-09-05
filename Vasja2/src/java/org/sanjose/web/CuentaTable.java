package org.sanjose.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.sanjose.model.CategoriaCuenta;
import org.sanjose.model.Cuenta;
import org.sanjose.model.Propietario;
import org.sanjose.model.Usuario;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.util.CuentaReporteButtonClickListener;
import org.sanjose.util.DataFilterUtil;
import org.sanjose.util.ExtendedJPAContainer;
import org.sanjose.web.helper.SessionHandler;
import org.sanjose.web.helper.SubwindowModal;
import org.sanjose.web.layout.CuentaFilterLayout;
import org.sanjose.web.layout.ResizeTableLayout;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class CuentaTable extends ResizeTableLayout {

	static final Logger logger = Logger.getLogger(CuentaTable.class
			.getName());

	CuentaFilterLayout cuentaFilters = new CuentaFilterLayout();
	HashMap<String, Filter> appliedFilters = new HashMap<String, Filter>();
	HashMap<String, Property.ValueChangeListener> filterListeners = new HashMap<String, Property.ValueChangeListener>();
	HashMap<Object, CheckBox> selectionCheckBoxes = new HashMap<Object, CheckBox>();
	ExtendedJPAContainer<Cuenta> container;
	HashSet<Object> markedRows = new HashSet<Object>();
	boolean firstRun = true;

	static final Action ACTION_EDIT = new Action("Editar");
	static final Action ACTION_DEL = new Action("Eliminar");
	static final Action ACTION_NEW = new Action("Nueva");
	static final Action[] ACTIONS = new Action[] { ACTION_EDIT, ACTION_NEW,
			ACTION_DEL };

	public CuentaTable(Window mainWin) {
		super(mainWin);		
		table = new Table("Cuentas") {
			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				if (property.getType() == Date.class) {
					SimpleDateFormat df = new SimpleDateFormat(
							ConfigurationUtil.get("DEFAULT_DATE_FORMAT"));
					return df.format((Date) property.getValue());
				}
				if (colId.equals("isCaja")) {
					boolean isCaja = (Boolean) (getContainerProperty(rowId, "isCaja")
							.getValue());
					return (isCaja ? "CAJA" : "");				
				}
				if (colId.equals("isBanco")) {
					boolean isBanco = (Boolean) (getContainerProperty(rowId, "isBanco")
							.getValue());
					return (isBanco ? "BANCO" : "");				
				}
				/*if (property.getType() == BigDecimal.class) {
					DecimalFormat fmt = new DecimalFormat(ConfigurationUtil.get("DECIMAL_FORMAT"));
					return fmt.format((BigDecimal) property.getValue());				
				}*/
				

				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		setCaption("Cuentas");
		setImmediate(true);
		setWidth("100%");
		setMargin(true);
		setSpacing(true);
		setSizeFull();
		setResizeMargin(285);
		table.setLocale(ConfigurationUtil.LOCALE);		
		
		// set a style name, so we can style rows and cells
		table.setStyleName(ConfigurationUtil.get("CSS_STYLE"));
	
		// size
		table.setWidth("100%");
	   // int shownRowsCount = table.getVisibleItemIds().size();
		table.setPageLength(Math.round((mainWindow.getHeight()-getResizeMargin())/20f));
		//table.se
		// selectable
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.setImmediate(true); // react at once when something is selected
		cuentaFilters.getRemoveFilter().addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				cuentaFilters.getCategoriaFilter().setValue(null);
				cuentaFilters.getPropietarioFilter().setValue(null);
				cuentaFilters.getUsuarioFilter().setValue(null);
				cuentaFilters.getNombreFilter().setValue("");
				cuentaFilters.getNumeroFilter().setValue("");
				container.removeAllContainerFilters();
				appliedFilters.clear();
				container.applyFilters();
				cuentaFilters.getCategoriaFilter().setValue(null);
			}
		});		
		cuentaFilters.getSelectAll().addListener(new ClickListener() {
			boolean isSelected = false;
			@Override
			public void buttonClick(ClickEvent event) {
				if (!isSelected) {
					for (Object id: table.getItemIds()) {
						table.select(id);
					}
					isSelected = true;
				}
				else {
					for (Object id: table.getItemIds()) {
						table.unselect(id);
					}
					isSelected = false;
				}
				table.requestRepaint(); 
			}
		});
			
		redraw();
		firstRun = false;
		
		final CuentaTable ut = this;

		table.addActionHandler(new Action.Handler() {
			public Action[] getActions(Object target, Object sender) {
				return ACTIONS;
			}
			public void handleAction(Action action, Object sender, Object target) {
				if (ACTION_DEL == action) {
					Item item = table.getItem(target);
					final SubwindowModal sub = new SubwindowModal(
							"Eliminar cuenta");
					final EntityItem<Cuenta> eItem = container
							.getItem((Long) item.getItemProperty("id")
									.getValue());
					Label message = new Label("?Eliminar la cuenta numero "
							+ (Long) item.getItemProperty("numero").getValue()
							+ "?");

					sub.getContent().addComponent(message);
					((VerticalLayout) sub.getContent()).setComponentAlignment(
							message, Alignment.MIDDLE_CENTER);
					HorizontalLayout hLayout = new HorizontalLayout();
					hLayout.setMargin(true);
					hLayout.setSpacing(true);

					Button yes = new Button("Si", new Button.ClickListener() {
						public void buttonClick(ClickEvent event) {
							container.removeItem(eItem.getItemId());
							container.commit();
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

				} else if (ACTION_EDIT == action) {
					Item item = table.getItem(target);
					SubwindowModal sub = new SubwindowModal("Editar cuenta");
					EntityItem<Cuenta> eItem = container.getItem((Long) item
							.getItemProperty("id").getValue());
					sub.getContent().addComponent(
							new CuentaForm(eItem.getEntity(), ut));
					getWindow().addWindow(sub);
				} else if (ACTION_NEW == action) {
					SubwindowModal sub = new SubwindowModal("Nueva cuenta");
					sub.getContent().addComponent(new CuentaForm(ut));
					getWindow().addWindow(sub);
				}

			}

		});
		// Disabled double -click editing of Cuenta
		table.addListener(new ItemClickListener() {
			public void itemClick(ItemClickEvent event) {
				if (event.isDoubleClick()) {
					Object itemId = event.getItemId();
					Cuenta us = container.getItem(itemId).getEntity();
					SubwindowModal sub = new SubwindowModal("Editar cuenta");
					sub.getContent().addComponent(new CuentaForm(us, ut));
					getWindow().addWindow(sub);
					table.requestRepaint();
				}
			}
		});

		// style generator
		table.setCellStyleGenerator(new CellStyleGenerator() {
			public String getStyle(Object itemId, Object propertyId) {
				if (propertyId == null) {
					// no propertyId, styling row
					return (markedRows.contains(itemId) ? "marked" : null);
				} else if ("login".equals(propertyId)) {
					return "bold";
				} else {
					return null;
				}

			}
		});
	}
	
	public void redraw() {
		removeAllComponents();
		addComponent(cuentaFilters);
		addComponent(table);
		setExpandRatio(table, 1f);
	
		if (SessionHandler.getVasjaApp().getCuentaContainer()!=null) {
			container = SessionHandler.getVasjaApp().getCuentaContainer();
		}
		else { 				
			container = new ExtendedJPAContainer<Cuenta>(Cuenta.class);
			container.setEntityProvider(ConfigurationUtil
					.getCuentaEP());
			container.addNestedContainerProperty("creadoPorUsuario.*");
			container.addNestedContainerProperty("propietario.*");
			container.addNestedContainerProperty("categoriaCuenta.*");
	
			DataFilterUtil.bindSearchTextField(cuentaFilters.getNumeroFilter(),
					"numero", "filtrar numero", container, appliedFilters, filterListeners);
			DataFilterUtil.bindComboBox(cuentaFilters.getCategoriaFilter(),
					"nombre", "filtrar categoria",
					CategoriaCuenta.class, container,
					"categoriaCuenta", appliedFilters, filterListeners);
			DataFilterUtil.bindComboBox(cuentaFilters.getPropietarioFilter(),
					"nombre", "filtrar propietario",
					Propietario.class, container,
					"propietario", appliedFilters, filterListeners);
			DataFilterUtil.bindComboBox(cuentaFilters.getUsuarioFilter(), "nombre",
					"filtrar usuario", Usuario.class,
					container, "creadoPorUsuario", appliedFilters, filterListeners);
			DataFilterUtil.bindSearchTextField(cuentaFilters.getNombreFilter(),
					"nombre", "busca nombres", container, appliedFilters, filterListeners);
			
			SessionHandler.getVasjaApp().setCuentaContainer(container);
		}
		
		table.setContainerDataSource(container);
		table.removeGeneratedColumn("SEL");
		// Set Data for table
		/*table.addGeneratedColumn("SEL", new ColumnGenerator() {
			@Override
			public Component generateCell(final Table source, final Object itemId, Object columnId) {
				CheckBox ch = new CheckBox();
				ch.addListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						logger.info("Got SEL event: " + event.getButton().getValue() + " " + source.getCaption());
						if ((Boolean)event.getButton().getValue())
							source.select(itemId);
						else
							source.unselect(itemId);
					}
				});
				selectionCheckBoxes.put(itemId, ch);
				return ch;
			}
		});
		table.setColumnWidth("SEL", 25);*/
		// turn on column reordering and collapsing
		table.setColumnReorderingAllowed(true);
		table.setColumnCollapsingAllowed(true);
		table.setVisibleColumns(new String[] { "id", "numero", "nombre",
				"categoriaCuenta.nombre", "isCaja", "isBanco", "pen", "usd", "propietario.nombre", 
				"fechaDeApertura", "creadoPorUsuario.usuario", "descripcion" });
		// set column headers
		table.setColumnHeaders(new String[] { "Id", "Numero", "Nombre",
				"Categoria de Cuentas", "Caja", "Banco", "PEN", "USD", "Coordinador", 
				"Fecha de apertura", "Creado por usuario", "Descripcion" });
		// Column alignment
		table.setColumnAlignment("descripcion", Table.ALIGN_CENTER);
		table.setColumnCollapsed("id", true);
		table.setColumnCollapsed("descripcion", true);
		table.setColumnCollapsed("fechaDeApertura", true);
		table.setColumnCollapsed("creadoPorUsuario.usuario", true);
		table.setSortContainerPropertyId("numero");
		
		// Column width
		table.setColumnExpandRatio("descripcion", 1);
		table.setColumnWidth("descripcion", 70);
		table.setColumnWidth("creadoPorUsuario", 40);
		table.setColumnWidth("numero", 35);
		
		buildBottomButtons();
	}
	
	public void buildBottomButtons() {

		// create the export buttons
        final ThemeResource export = new ThemeResource("../icons/table-excel.png");
        final Button regularExportButton = new Button("Exportar");
        regularExportButton.setIcon(export);
        regularExportButton.addListener(new ClickListener() {
            private static final long serialVersionUID = -73954695086117200L;
            private ExcelExport excelExport;

            @Override
            public void buttonClick(final ClickEvent event) {
                excelExport = new ExcelExport(table, "Cuentas");
                //excelExport.excludeCollapsedColumns();
                excelExport.setReportTitle("Cuentas");
                excelExport.setExportFileName("Cuentas.xls");
                excelExport.setDisplayTotals(true);
                excelExport.setRowHeaders(false);
                excelExport.setDateDataFormat("yyyy.MM.dd");
                //excelExport.setExcelFormatOfProperty("pen",);
                //excelExport.setExcelFormatOfProperty("usd", "number");
                excelExport.setExcelFormatOfProperty("id",  "##0");
                excelExport.setExcelFormatOfProperty("numero",  "##0");
                excelExport.setExcelFormatOfProperty("pen",  ConfigurationUtil.get("DECIMAL_FORMAT"));
                excelExport.setExcelFormatOfProperty("usd",  ConfigurationUtil.get("DECIMAL_FORMAT"));
                excelExport.setDoubleDataFormat(ConfigurationUtil.get("DECIMAL_FORMAT"));
                excelExport.export();
            }
        });
        HorizontalLayout bottomButtonsLayout = new HorizontalLayout();
        bottomButtonsLayout.addComponent(regularExportButton);
		
		Button reporteButton = new Button("Reporte cuenta");
		logger.info("window: " +getWindow());
		reporteButton.setIcon(new ThemeResource("../icons/window_text.png"));
		reporteButton.addListener(
				new CuentaReporteButtonClickListener(null, table,
						mainWindow, new SubwindowModal("Reporte Cuenta")));
				
		bottomButtonsLayout.addComponent(reporteButton);
		
		Button abonoButton = new Button("Multi abono");
		Button cargoButton = new Button("Multi cargo");
		
		abonoButton.setIcon(new ThemeResource("../icons/sign_add.png"));
		cargoButton.setIcon(new ThemeResource("../icons/sign_remove.png"));
		abonoButton.addListener(new OperacionButtonClickListener(false, this));
		cargoButton.addListener(new OperacionButtonClickListener(true, this));
		bottomButtonsLayout.addComponent(abonoButton);
		bottomButtonsLayout.addComponent(cargoButton);
		
		addComponent(bottomButtonsLayout);
	}
	
	SubwindowModal sub = new SubwindowModal("Comprobante de Multi operacion");
	
	private class OperacionButtonClickListener implements Button.ClickListener {
		boolean isCargo = false;
		CuentaTable ct = null;

		public OperacionButtonClickListener(boolean isCargo, CuentaTable ct) {
			this.isCargo = isCargo;
			this.ct = ct;
		}

		public void buttonClick(ClickEvent event) {
			if (!getWindow().getChildWindows().contains(sub)) {
				sub.getContent().removeAllComponents();
				sub.setCaption((isCargo ? "MultiCargo"
						: "MultiAbono"));
				Set<Cuenta> cuentas  = new HashSet<Cuenta>();
				Set<Object> selIds = (Set<Object>)table.getValue();
				for (Object id : selIds) {
					if (!((Cuenta)container.getItem(id).getEntity()).getIsCaja())
						cuentas.add((Cuenta)container.getItem(id).getEntity());
				}
				sub.getContent().addComponent(
						new OperacionForm(null, cuentas, null, ct, isCargo));
				getWindow().addWindow(sub);
			}
		}
	}
}

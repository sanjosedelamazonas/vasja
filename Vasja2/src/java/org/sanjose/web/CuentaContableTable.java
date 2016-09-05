package org.sanjose.web;

import java.util.HashSet;

import org.sanjose.model.CuentaContable;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.util.DataFilterUtil;
import org.sanjose.util.ExtendedJPAContainer;
import org.sanjose.web.helper.SubwindowModal;
import org.sanjose.web.layout.ResizeTableLayout;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;
import com.vaadin.addon.jpacontainer.filter.Filters;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class CuentaContableTable extends ResizeTableLayout  {

    	ExtendedJPAContainer<CuentaContable> container;
	    MutableEntityProvider<CuentaContable> myEntityProvider;

	    HashSet<Object> markedRows = new HashSet<Object>();

	    static final Action ACTION_EDIT = new Action("Editar");
	    static final Action ACTION_DEL = new Action("Eliminar");
	    static final Action ACTION_NEW = new Action("Nuevo");
		static final Action[] ACTIONS = new Action[] { ACTION_EDIT, ACTION_NEW,
			ACTION_DEL };
		
	    public CuentaContableTable(Window w) {
	    	super(w);
	    	table = new Table("Cuentas contables") {
    			@Override
    			protected String formatPropertyValue(Object rowId, Object colId,
    					Property property) {
    				if (colId.equals("isLeaf")) {
    					boolean isLeaf = (Boolean) (getContainerProperty(rowId, "isLeaf")
    							.getValue());
    					return (isLeaf ? "HOJA" : "");				
    				}    				
    				return super.formatPropertyValue(rowId, colId, property);
    			}    		
	    	};
	    	addComponent(table);
		    setExpandRatio(table, 1f);
		    setResizeMargin(150);
		    setSizeFull();
	    	setCaption("Cuenta contable");
	        // set a style name, so we can style rows and cells
	        table.setStyleName(ConfigurationUtil.get("CSS_STYLE"));
	        // size
	        table.setWidth("100%");
	        // selectable
	        table.setWriteThrough(true);
	        table.setSelectable(true);
	        table.setMultiSelect(true);
	        table.setImmediate(true); // react at once when something is selected
	        

	    	redraw();
	    	final CuentaContableTable ut = this;
	        table.addActionHandler(new Action.Handler() {
	            public Action[] getActions(Object target, Object sender) {
	            	return ACTIONS;
	            }

	            public void handleAction(Action action, Object sender, Object target) {
	            	if (ACTION_DEL == action) {
	            		Item item = table.getItem(target);
	                    final SubwindowModal sub = new SubwindowModal("Elminar cuenta contable");
	                    final EntityItem<CuentaContable> eItem = container.getItem((Long)item.getItemProperty("id").getValue());
	                    Label message = new Label("?Eliminar cuenta contable " + item.getItemProperty("nombre").getValue() + "?");
	                    
	                    sub.getContent().addComponent(message);
	                    ((VerticalLayout)sub.getContent()).setComponentAlignment(message, Alignment.MIDDLE_CENTER);
	                    HorizontalLayout hLayout = new HorizontalLayout();
	                    hLayout.setMargin(true);
	                    hLayout.setSpacing(true);
	                    
	                    Button yes = new Button("Si", new Button.ClickListener() {
	                        public void buttonClick(ClickEvent event) {
	                        	container.removeItem(eItem.getItemId());
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
	                    ((VerticalLayout)sub.getContent()).setComponentAlignment(hLayout, Alignment.MIDDLE_CENTER);
	                    getWindow().addWindow(sub);	  
	                   
	                } else if (ACTION_EDIT == action) {
	                	Item item = table.getItem(target);
						SubwindowModal sub = new SubwindowModal("Editar cuenta contable");
						EntityItem<CuentaContable> eItem = container.getItem((Long) item
								.getItemProperty("id").getValue());
						sub.getContent().addComponent(
								new CuentaContableForm(eItem.getEntity(), ut));
						getWindow().addWindow(sub);
					} else if (ACTION_NEW == action) {
						SubwindowModal sub = new SubwindowModal("Nueva cuenta contable");
						sub.getContent().addComponent(new CuentaContableForm(ut));
						getWindow().addWindow(sub);
					}
	            }
	        });
	        
	        table.setTableFieldFactory(new TableFieldFactory() {
				@Override
				public Field createField(Container container, Object itemId,
						Object propertyId, Component uiContext) {
					TextField tf = new TextField((String) propertyId);
					if ("id".equals(propertyId)) {
						tf.setReadOnly(true);
					} else {
						tf.setData(itemId);
					}
					tf.setImmediate(true);
					if ("categoriaCuentaContable".equals(propertyId)) {
						ComboBox cmb = new ComboBox();
						Filter f = Filters.eq("isLeaf", false);
						DataFilterUtil.bindComboBox(cmb, "nombre",
								"Seleccione categoria de Cuentas contables", CuentaContable.class, f);
			            cmb.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			            cmb.setData(itemId);
						return cmb;
					}		
					return tf;
				}
	        });

	    }
	    
	    public void redraw() {
	    	table.setContainerDataSource(null);
	        container = new ExtendedJPAContainer<CuentaContable>(
	        		CuentaContable.class);
            MutableLocalEntityProvider<CuentaContable> myEntityProvider = ConfigurationUtil.getCuentaContableEP();
            container.setEntityProvider(myEntityProvider);
    		container.addNestedContainerProperty("categoriaCuentaContable.*");
    		//container.addNestedContainerProperty("cuentaCentroCosto.*");
            table.setContainerDataSource(container);

	        // turn on column reordering and collapsing
	        table.setColumnReorderingAllowed(true);
	        table.setColumnCollapsingAllowed(true);
	        table.setVisibleColumns(new String[] { "id", "codigo", "isLeaf", "level", "nombre", "categoriaCuentaContable.codigo", "categoriaCuentaContable.nombre", "descripcion" });
	        // set column headers
	        table.setColumnHeaders(new String[] { "Id", "Codigo", "Hoja", "Nivel", "Nombre", "Cadigo Cat", "Categoria", "Descripcion"});
        	table.setColumnWidth("id", 50);
        	table.setColumnWidth("codigo", 50);
        	table.setColumnWidth("isLeaf", 50);
        	table.setColumnWidth("nombre", 120);
    		table.setColumnExpandRatio("descripcion", 1);
    		table.setColumnWidth("categoriaCuentaContable.nombre", 120);
	    }
}
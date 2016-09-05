package org.sanjose.web;

import java.util.HashSet;

import org.sanjose.model.Propiedad;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.web.helper.SubwindowModal;
import org.sanjose.web.layout.ResizeTableLayout;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
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
public class PropiedadTable extends ResizeTableLayout {

	    EntityContainer<Propiedad> container;
	    MutableEntityProvider<Propiedad> myEntityProvider;

	    HashSet<Object> markedRows = new HashSet<Object>();

	    static final Action ACTION_DEL = new Action("Eliminar");
	    static final Action ACTION_NEW = new Action("Nuevo");
	    static final Action[] ACTIONS = new Action[] { ACTION_NEW, ACTION_DEL };
	    
	    public PropiedadTable(Window w) {
	    	super(w);
	    	table = new Table("Configuracion");
		    addComponent(table);
		    setExpandRatio(table, 1f);
		    setResizeMargin(200);
		    setSizeFull();
	    	setCaption("Config");
	        // set a style name, so we can style rows and cells
	        table.setStyleName(ConfigurationUtil.get("CSS_STYLE"));
	        // size
	        table.setWidth("100%");
	        // selectable
	        table.setWriteThrough(true);
	        table.setSelectable(true);
	        table.setMultiSelect(true);
	        table.setImmediate(true); // react at once when something is selected

	    	table.setContainerDataSource(null);
	        container = new JPAContainer<Propiedad>(
            		Propiedad.class);
            MutableLocalEntityProvider<Propiedad> myEntityProvider = ConfigurationUtil.getPropiedadEP();
            container.setEntityProvider(myEntityProvider);
            table.setContainerDataSource(container);
            table.setSortContainerPropertyId("nombre");

	        // turn on column reordering and collapsing
	        table.setColumnReorderingAllowed(true);
	        table.setColumnCollapsingAllowed(true);
	        table.setVisibleColumns(new String[] { "nombre", "value"});
	        // set column headers
	        table.setColumnHeaders(new String[] { "Nombre", "Value"});
        	
	        final Button editButton = new Button("Editar");
	        addComponent(editButton);
	        editButton.addListener(new Button.ClickListener() {
	            public void buttonClick(ClickEvent event) {
	                table.setEditable(!table.isEditable());
	                editButton.setCaption((table.isEditable() ? "Guardar" : "Editar"));
	                ConfigurationUtil.resetConfiguration();
	            }
	        });
	        setComponentAlignment(editButton, Alignment.TOP_RIGHT);

	        table.addActionHandler(new Action.Handler() {
	            public Action[] getActions(Object target, Object sender) {
	            	return ACTIONS;
	            }

	            public void handleAction(Action action, Object sender, Object target) {
	            	if (ACTION_DEL == action) {
	            		Item item = table.getItem(target);
	                    final SubwindowModal sub = new SubwindowModal("Elminar propiedad");
	                    final EntityItem<Propiedad> eItem = container.getItem((Long)item.getItemProperty("id").getValue());
	                    Label message = new Label("?Eliminar la propiedad " + item.getItemProperty("nombre").getValue() + "?");
	                    
	                    sub.getContent().addComponent(message);
	                    ((VerticalLayout)sub.getContent()).setComponentAlignment(message, Alignment.MIDDLE_CENTER);
	                    HorizontalLayout hLayout = new HorizontalLayout();
	                    hLayout.setMargin(true);
	                    hLayout.setSpacing(true);
	                    
	                    Button yes = new Button("Si", new Button.ClickListener() {
	                        public void buttonClick(ClickEvent event) {
	                        	container.removeItem(eItem.getItemId());
	                        	ConfigurationUtil.resetConfiguration();
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
	                   
	                } else if (ACTION_NEW == action) {
	                	EntityItem<Propiedad> eItem = container.createEntityItem(new Propiedad());
	                	container.addEntity(eItem.getEntity());
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
					return tf;
				}
	        });

	    }
}


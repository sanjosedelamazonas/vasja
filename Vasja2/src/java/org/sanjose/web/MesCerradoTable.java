package org.sanjose.web;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.sanjose.model.MesCerrado;
import org.sanjose.model.Usuario;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.util.DataFilterUtil;
import org.sanjose.web.helper.SessionHandler;
import org.sanjose.web.layout.MesCerradoLayout;
import org.sanjose.web.layout.ResizeTableLayout;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction.KeyCode;
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
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class MesCerradoTable extends ResizeTableLayout {

		SimpleDateFormat sdfNormal = new SimpleDateFormat(ConfigurationUtil.get("DEFAULT_DATE_FORMAT"), ConfigurationUtil.LOCALE);
    	EntityContainer<MesCerrado> container;
	    MutableEntityProvider<MesCerrado> myEntityProvider = ConfigurationUtil.getMesCerradoEP();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", ConfigurationUtil.LOCALE);
	    MesCerradoLayout mesCerradoLayout = new MesCerradoLayout();

	    public MesCerradoTable(Window mainW) {
	    	super(mainW);
	    	setResizeMargin(150);
	    	setSizeFull();
	    	table = new Table("MesCerrados") {
	            @Override
	            protected String formatPropertyValue(Object rowId, Object colId,
	                    Property property) {
	                Object v = property.getValue();
	                if (v instanceof Date) {
	                    Date dateValue = (Date) v;                    
	                    Calendar c1 = Calendar.getInstance();
	                    c1.setTime(dateValue);
	                    c1.add(Calendar.MILLISECOND, -1);
	                    return (colId.equals("fecha") ? formatMes(c1.getTime()) : sdfNormal.format(dateValue));
	                }
	                return super.formatPropertyValue(rowId, colId, property);
	            }

	        };
	    	setCaption("Meses cerrados");
	    	addComponent(mesCerradoLayout);
	    	
	        addComponent(table);
	        setExpandRatio(table, 1f);
	        // set a style name, so we can style rows and cells
	        table.setStyleName(ConfigurationUtil.get("CSS_STYLE"));
	        // size
	        table.setWidth("100%");
	        // selectable
	        table.setWriteThrough(true);
	        table.setImmediate(true); // react at once when something is selected
	        table.setPageLength(Math.round((mainWindow.getHeight()-getResizeMargin())/20f));

	    	table.setContainerDataSource(null);
	        container = new JPAContainer<MesCerrado>(
            		MesCerrado.class);
            container.setEntityProvider(myEntityProvider);
    		container.addNestedContainerProperty("usuario.*");
    		container.sort(new Object[] { "fecha"},
					new boolean[] { false });
            table.setContainerDataSource(container);

	        // turn on column reordering and collapsing
	        table.setColumnReorderingAllowed(true);
	        table.setColumnCollapsingAllowed(true);
	        table.setVisibleColumns(new String[] { "id", "fecha", "fechaCerrado", "usuario.usuario"});
	        // set column headers
	        table.setColumnHeaders(new String[] { "Id", "Mes", "Fecha Cerrado", "Usuario" });
	        table.setColumnWidth("id", 45);
	        table.setColumnCollapsed("id", true);
	        
	        mesCerradoLayout.getMesAcerrarField().setValue(getUltimoMes());
	        
	        mesCerradoLayout.getMesCerradoButton().addListener(new Button.ClickListener() {
				
				@Override
				public void buttonClick(ClickEvent event) {
					openConfirmationWindow();
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
					if ("fecha".equals(propertyId)) {
						tf.setFormat(sdf);
					}
					if ("usuario".equals(propertyId)) {
						ComboBox cmb = new ComboBox();
						DataFilterUtil.bindComboBox(cmb, "usuario",
								"Usuario", Usuario.class);
			            cmb.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			            cmb.setData(itemId);
						return cmb;
					}					
					return tf;
				}
	        });
	    }
	    
	    void openConfirmationWindow() {
			Window logout = new Window("Confirmacion");
			logout.setModal(true);
			logout.setStyleName(Reindeer.WINDOW_BLACK);
			logout.setWidth("260px");
			logout.setResizable(false);
			logout.setClosable(false);
			logout.setDraggable(false);
			logout.setCloseShortcut(KeyCode.ESCAPE, null);

			Label helpText = new Label(
					"Estas seguro que quieres cerrar el mes: " + getUltimoMes(),
					Label.CONTENT_XHTML);
			logout.addComponent(helpText);

			HorizontalLayout buttons = new HorizontalLayout();
			buttons.setSpacing(true);
			Button yes = new Button("Cerrar", new Button.ClickListener() {
				public void buttonClick(ClickEvent event) {
					Date fecha = ConfigurationUtil.dateAddMonths(ConfigurationUtil.getUltimoMesCerrado(), 1);
					MesCerrado mc = new MesCerrado();
					mc.setFecha(fecha);
					mc.setUsuario(ConfigurationUtil.getEntityManager().find(Usuario.class, (SessionHandler.get().getId())));
					myEntityProvider.addEntity(mc);
					mesCerradoLayout.getMesAcerrarField().setValue(getUltimoMes());			        
					mainWindow.removeWindow(event.getButton().getWindow());					
				}
			});
			yes.setStyleName(Reindeer.BUTTON_DEFAULT);
			yes.focus();
			buttons.addComponent(yes);
			Button no = new Button("Regresar", new Button.ClickListener() {
				public void buttonClick(ClickEvent event) {
					mainWindow.removeWindow(event.getButton().getWindow());
				}
			});
			buttons.addComponent(no);
			logout.addComponent(buttons);
			((VerticalLayout) logout.getContent()).setComponentAlignment(buttons,
					Alignment.TOP_CENTER);
			((VerticalLayout) logout.getContent()).setSpacing(true);
			mainWindow.addWindow(logout);
		}
	    
	    private String formatMes(Date fecha) {
	    	return sdf.format(fecha).toUpperCase();
	    }
	    
	    private String getUltimoMes() {
	    	return formatMes(ConfigurationUtil.getUltimoMesCerrado());
	    }	    
	    
}


package org.sanjose.web;

import java.util.Arrays;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.sanjose.model.Cuenta;
import org.sanjose.model.RubroProyecto;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.util.DataFilterUtil;
import org.sanjose.util.VasjaFieldFactory;

import com.vaadin.addon.jpacontainer.filter.Filters;
import com.vaadin.addon.jpacontainer.provider.BatchableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class RubroProyectoForm extends VerticalLayout {

		RubroProyecto RubroProyecto;
		RubroProyectoTable RubroProyectoTable;
		final Form RubroProyectoForm = new Form();
		boolean isNew = false;
		static final Logger logger = Logger.getLogger(RubroProyectoForm.class
				.getName());
        
	    public RubroProyectoForm(RubroProyecto account, RubroProyectoTable RubroProyectoTable) {
	    	this.RubroProyecto = account;
	    	this.RubroProyectoTable = RubroProyectoTable;
	        RubroProyectoForm.setCaption("Editar los detalles del Rubro presupuestal de Proyecto");
	    	buildForm();
	    }
	    
	    public RubroProyectoForm() {
	    	isNew = true;
	    	RubroProyecto = new RubroProyecto(); // a account POJO
	        RubroProyectoForm.setCaption("Nuevo Rubro presupuestal");
	        buildForm();
	    }
	    
	    public RubroProyectoForm(RubroProyectoTable RubroProyectoTable) {
	    	this();
	    	this.RubroProyectoTable = RubroProyectoTable;
	    }
	        
	    private void buildForm() {    
	    	setCaption("Rubro presupuestal");		        
	        final BeanItem<RubroProyecto> accountItem = new BeanItem<RubroProyecto>(RubroProyecto); // item fromus;
	                                                                    // POJO
	        // Create the Form
	        RubroProyectoForm.setWriteThrough(false); // we want explicit 'apply'
	        RubroProyectoForm.setInvalidCommitted(false); // no invalid values in datamodel

	        // FieldFactory for customizing the fields and adding validators
	        RubroProyectoForm.setFormFieldFactory(new RubroProyectoFieldFactory());
	        RubroProyectoForm.setItemDataSource(accountItem); // bind to POJO via BeanItem

	        // Determines which properties are shown, and in which order:
	        RubroProyectoForm.setVisibleItemProperties(Arrays.asList(new String[] {
	        		"codigo", "isLeaf", "level", "nombre", "descripcion", "cuentaRubroProyecto", "categoriaRubroProyecto" }));

	        // Add form to layout
	        addComponent(RubroProyectoForm);

	        // The cancel / apply buttons
	        HorizontalLayout buttons = new HorizontalLayout();
	        buttons.setSpacing(true);

	        Button apply = new Button("Guardar", new Button.ClickListener() {
	            public void buttonClick(ClickEvent event) {
	            	EntityTransaction trans = null;
                    try {
	                    RubroProyectoForm.commit();
	                    if (RubroProyecto.getId()!=null) {
	                    	logger.info("Got RubroProyecto to save: " + RubroProyecto.toString());
	                    	Item item = RubroProyectoTable.container.getItem(RubroProyecto.getId());
	                    	logger.info("Got item to save: " + item);	                    	
	                    	RubroProyectoTable.container.updateEntityItem(item, accountItem);
	                    	item.getItemProperty("categoriaRubroProyecto").setValue(RubroProyecto.getCategoriaRubroProyecto());
	                    	item.getItemProperty("cuentaRubroProyecto").setValue(RubroProyecto.getCuentaRubroProyecto());	                    	
	                    }
	                    else { 
	                    	
	                    	EntityManager em = ConfigurationUtil.getNewEntityManager();
	            			MutableLocalEntityProvider<RubroProyecto> RubroProyectoEP = new BatchableLocalEntityProvider<RubroProyecto>(
	            					RubroProyecto.class, em);
	            			RubroProyectoEP.setTransactionsHandledByProvider(false);
	            						
	            			trans = em.getTransaction();
	            			trans.begin();	            			
	                    	//Object catAdded = catCuentaEP.addEntity(RubroProyecto);
	                    	//Object cuentaAdded = cuentaEP.addEntity(cajaCuenta);
	                    	//RubroProyecto.setCategoriaRubroProyecto(categoriaRubroProyecto);
	                    	RubroProyectoEP.addEntity(RubroProyecto);
	                    	//catCuentaEP.updateEntity(RubroProyecto);
	                    	trans.commit();
	                    }
	                    removeAllComponents();
	                    getWindow().getParent().removeWindow(getWindow());
	            		//
	        			if (RubroProyectoTable!=null) RubroProyectoTable.redraw();	        			
	                } catch (Exception e) {
	                	e.printStackTrace();
	                	if (trans!=null) trans.rollback();
	                	getWindow().showNotification("Problema al anadir Rubro presupuestal: ", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
	                }
	            }
	        });
	        buttons.addComponent(apply);
	        RubroProyectoForm.getFooter().addComponent(buttons);
	        RubroProyectoForm.getFooter().setMargin(false, false, true, true);

	    }

	    private class RubroProyectoFieldFactory extends VasjaFieldFactory {

	        final ComboBox categoriaRubroProyectoCombo = new ComboBox();
	        final ComboBox cuentaRubroProyectoCombo = new ComboBox();
			
			public RubroProyectoFieldFactory() {
	        	Filter f = Filters.eq("isLeaf", false);
				DataFilterUtil.bindComboBox(categoriaRubroProyectoCombo, "nombre",
						"Seleccione categoria del rubro presupuestal del proyecto", RubroProyecto.class, f);
				categoriaRubroProyectoCombo.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));	     
				categoriaRubroProyectoCombo.setVisible(true);
				f = Filters.eq("isCaja", false);
				DataFilterUtil.bindComboBox(cuentaRubroProyectoCombo, "numero",
						"Seleccione cuenta", Cuenta.class, f, "nombre");
	        }

	        @Override
	        public Field createField(Item item, Object propertyId,
	                Component uiContext) {
	            Field f;
	            if ("categoriaRubroProyecto".equals(propertyId)) {
	                return categoriaRubroProyectoCombo;
	            } else if ("cuentaRubroProyecto".equals(propertyId)) {
	                return cuentaRubroProyectoCombo;
	            } else if ("descripcion".equals(propertyId)) {
	                f = createTextArea(propertyId);
	                f.setHeight("6em");
	                f.setWidth("16em");
	            } 
	            else {
	                f = super.createField(item, propertyId, uiContext);
	            }
	            if ("nombre".equals(propertyId)) {
	                TextField tf = (TextField) f;
	                tf.setRequired(false);
	                tf.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
	            }	            
	            return f;
	        }

	    }
	}


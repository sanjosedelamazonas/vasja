package org.sanjose.web;

import java.util.Arrays;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.sanjose.model.LugarGasto;
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
public class LugarGastoForm extends VerticalLayout {

		LugarGasto lugarGasto;
		LugarGastoTable lugarGastoTable;
		final Form lugarGastoForm = new Form();
		boolean isNew = false;
		static final Logger logger = Logger.getLogger(LugarGastoForm.class
				.getName());
        
	    public LugarGastoForm(LugarGasto account, LugarGastoTable lugarGastoTable) {
	    	this.lugarGasto = account;
	    	this.lugarGastoTable = lugarGastoTable;
	        lugarGastoForm.setCaption("Editar los detalles del Lugar de Gasto");
	    	buildForm();
	    }
	    
	    public LugarGastoForm() {
	    	isNew = true;
	    	lugarGasto = new LugarGasto(); // a account POJO
	        lugarGastoForm.setCaption("Nuevo Lugar de Gasto");
	        buildForm();
	    }
	    
	    public LugarGastoForm(LugarGastoTable lugarGastoTable) {
	    	this();
	    	this.lugarGastoTable = lugarGastoTable;
	    }
	        
	    private void buildForm() {    
	    	setCaption("Lugar de Gasto");		        
	        final BeanItem<LugarGasto> accountItem = new BeanItem<LugarGasto>(lugarGasto); // item fromus;
	                                                                    // POJO
	        // Create the Form
	        lugarGastoForm.setWriteThrough(false); // we want explicit 'apply'
	        lugarGastoForm.setInvalidCommitted(false); // no invalid values in datamodel

	        // FieldFactory for customizing the fields and adding validators
	        lugarGastoForm.setFormFieldFactory(new LugarGastoFieldFactory());
	        lugarGastoForm.setItemDataSource(accountItem); // bind to POJO via BeanItem

	        // Determines which properties are shown, and in which order:
	        lugarGastoForm.setVisibleItemProperties(Arrays.asList(new String[] {
	        		"codigo", "isLeaf", "level", "nombre", "descripcion", "categoriaLugarGasto" }));

	        // Add form to layout
	        addComponent(lugarGastoForm);

	        // The cancel / apply buttons
	        HorizontalLayout buttons = new HorizontalLayout();
	        buttons.setSpacing(true);

	        Button apply = new Button("Guardar", new Button.ClickListener() {
	            public void buttonClick(ClickEvent event) {
	            	EntityTransaction trans = null;
                    try {
	                    lugarGastoForm.commit();
	                    if (lugarGasto.getId()!=null) {
	                    	logger.info("Got lugarGasto to save: " + lugarGasto.toString());
	                    	Item item = lugarGastoTable.container.getItem(lugarGasto.getId());
	                    	logger.info("Got item to save: " + item);	                    	
	                    	lugarGastoTable.container.updateEntityItem(item, accountItem);
	                    	item.getItemProperty("categoriaLugarGasto").setValue(lugarGasto.getCategoriaLugarGasto());
	                    	//item.getItemProperty("cuentaLugarGasto").setValue(lugarGasto.getCuentaLugarGasto());	                    	
	                    }
	                    else { 
	                    	
	                    	EntityManager em = ConfigurationUtil.getNewEntityManager();
	            			MutableLocalEntityProvider<LugarGasto> lugarGastoEP = new BatchableLocalEntityProvider<LugarGasto>(
	            					LugarGasto.class, em);
	            			lugarGastoEP.setTransactionsHandledByProvider(false);
	            						
	            			trans = em.getTransaction();
	            			trans.begin();	            			
	                    	//Object catAdded = catCuentaEP.addEntity(lugarGasto);
	                    	//Object cuentaAdded = cuentaEP.addEntity(cajaCuenta);
	                    	//lugarGasto.setCategoriaLugarGasto(categoriaLugarGasto);
	                    	lugarGastoEP.addEntity(lugarGasto);
	                    	//catCuentaEP.updateEntity(lugarGasto);
	                    	trans.commit();
	                    }
	                    removeAllComponents();
	                    getWindow().getParent().removeWindow(getWindow());
	            		//
	        			if (lugarGastoTable!=null) lugarGastoTable.redraw();	        			
	                } catch (Exception e) {
	                	e.printStackTrace();
	                	if (trans!=null) trans.rollback();
	                	getWindow().showNotification("Problema al anadir Lugar de Gasto: ", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
	                }
	            }
	        });
	        buttons.addComponent(apply);
	        lugarGastoForm.getFooter().addComponent(buttons);
	        lugarGastoForm.getFooter().setMargin(false, false, true, true);

	    }

	    private class LugarGastoFieldFactory extends VasjaFieldFactory {

	        final ComboBox categoriaLugarGastoCombo = new ComboBox();
	        final ComboBox cuentaLugarGastoCombo = new ComboBox();
			
			public LugarGastoFieldFactory() {
	        	Filter f = Filters.eq("isLeaf", false);
				DataFilterUtil.bindComboBox(categoriaLugarGastoCombo, "nombre",
						"Seleccione categoria del lugar de gasto", LugarGasto.class, f);
				categoriaLugarGastoCombo.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));	     
				categoriaLugarGastoCombo.setVisible(true);
				//f = Filters.eq("isCaja", false);
				//DataFilterUtil.bindComboBox(cuentaLugarGastoCombo, "numero",
				//		"Seleccione cuenta", Cuenta.class, f, "nombre");
	        }

	        @Override
	        public Field createField(Item item, Object propertyId,
	                Component uiContext) {
	            Field f;
	            if ("categoriaLugarGasto".equals(propertyId)) {
	                return categoriaLugarGastoCombo;
	            //} else if ("cuentaLugarGasto".equals(propertyId)) {
	              //  return cuentaLugarGastoCombo;
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


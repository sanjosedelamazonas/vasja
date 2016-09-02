package org.sanjose.web;

import java.util.Arrays;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.sanjose.model.CentroCosto;
import org.sanjose.model.Cuenta;
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
public class CentroCostoForm extends VerticalLayout {

		CentroCosto centroCosto;
		CentroCostoTable centroCostoTable;
		final Form centroCostoForm = new Form();
		boolean isNew = false;
		static final Logger logger = Logger.getLogger(CentroCostoForm.class
				.getName());
        
	    public CentroCostoForm(CentroCosto account, CentroCostoTable centroCostoTable) {
	    	this.centroCosto = account;
	    	this.centroCostoTable = centroCostoTable;
	        centroCostoForm.setCaption("Editar los detalles del Centro de Costo");
	    	buildForm();
	    }
	    
	    public CentroCostoForm() {
	    	isNew = true;
	    	centroCosto = new CentroCosto(); // a account POJO
	        centroCostoForm.setCaption("Nuevo Centro de Costo");
	        buildForm();
	    }
	    
	    public CentroCostoForm(CentroCostoTable centroCostoTable) {
	    	this();
	    	this.centroCostoTable = centroCostoTable;
	    }
	        
	    private void buildForm() {    
	    	setCaption("Centro de Costo");		        
	        final BeanItem<CentroCosto> accountItem = new BeanItem<CentroCosto>(centroCosto); // item fromus;
	                                                                    // POJO
	        // Create the Form
	        centroCostoForm.setWriteThrough(false); // we want explicit 'apply'
	        centroCostoForm.setInvalidCommitted(false); // no invalid values in datamodel

	        // FieldFactory for customizing the fields and adding validators
	        centroCostoForm.setFormFieldFactory(new CentroCostoFieldFactory());
	        centroCostoForm.setItemDataSource(accountItem); // bind to POJO via BeanItem

	        // Determines which properties are shown, and in which order:
	        centroCostoForm.setVisibleItemProperties(Arrays.asList(new String[] {
	        		"codigo", "isLeaf", "level", "nombre", "descripcion", "cuentaCentroCosto", "categoriaCentroCosto" }));

	        // Add form to layout
	        addComponent(centroCostoForm);

	        // The cancel / apply buttons
	        HorizontalLayout buttons = new HorizontalLayout();
	        buttons.setSpacing(true);

	        Button apply = new Button("Guardar", new Button.ClickListener() {
	            public void buttonClick(ClickEvent event) {
	            	EntityTransaction trans = null;
                    try {
	                    centroCostoForm.commit();
	                    if (centroCosto.getId()!=null) {
	                    	logger.info("Got centroCosto to save: " + centroCosto.toString());
	                    	Item item = centroCostoTable.container.getItem(centroCosto.getId());
	                    	logger.info("Got item to save: " + item);	                    	
	                    	centroCostoTable.container.updateEntityItem(item, accountItem);
	                    	item.getItemProperty("categoriaCentroCosto").setValue(centroCosto.getCategoriaCentroCosto());
	                    	item.getItemProperty("cuentaCentroCosto").setValue(centroCosto.getCuentaCentroCosto());	                    	
	                    }
	                    else { 
	                    	
	                    	EntityManager em = ConfigurationUtil.getNewEntityManager();
	            			MutableLocalEntityProvider<CentroCosto> centroCostoEP = new BatchableLocalEntityProvider<CentroCosto>(
	            					CentroCosto.class, em);
	            			centroCostoEP.setTransactionsHandledByProvider(false);
	            						
	            			trans = em.getTransaction();
	            			trans.begin();	            			
	                    	//Object catAdded = catCuentaEP.addEntity(centroCosto);
	                    	//Object cuentaAdded = cuentaEP.addEntity(cajaCuenta);
	                    	//centroCosto.setCategoriaCentroCosto(categoriaCentroCosto);
	                    	centroCostoEP.addEntity(centroCosto);
	                    	//catCuentaEP.updateEntity(centroCosto);
	                    	trans.commit();
	                    }
	                    removeAllComponents();
	                    getWindow().getParent().removeWindow(getWindow());
	            		//
	        			if (centroCostoTable!=null) centroCostoTable.redraw();	        			
	                } catch (Exception e) {
	                	e.printStackTrace();
	                	if (trans!=null) trans.rollback();
	                	getWindow().showNotification("Problema al anadir Centro de costo: ", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
	                }
	            }
	        });
	        buttons.addComponent(apply);
	        centroCostoForm.getFooter().addComponent(buttons);
	        centroCostoForm.getFooter().setMargin(false, false, true, true);

	    }

	    private class CentroCostoFieldFactory extends VasjaFieldFactory {

	        final ComboBox categoriaCentroCostoCombo = new ComboBox();
	        final ComboBox cuentaCentroCostoCombo = new ComboBox();
			
			public CentroCostoFieldFactory() {
	        	Filter f = Filters.eq("isLeaf", false);
				DataFilterUtil.bindComboBox(categoriaCentroCostoCombo, "nombre",
						"Seleccione categoria del centro de costo", CentroCosto.class, f);
				categoriaCentroCostoCombo.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));	     
				categoriaCentroCostoCombo.setVisible(true);
				f = Filters.eq("isCaja", false);
				DataFilterUtil.bindComboBox(cuentaCentroCostoCombo, "numero",
						"Seleccione cuenta", Cuenta.class, f, "nombre");
	        }

	        @Override
	        public Field createField(Item item, Object propertyId,
	                Component uiContext) {
	            Field f;
	            if ("categoriaCentroCosto".equals(propertyId)) {
	                return categoriaCentroCostoCombo;
	            } else if ("cuentaCentroCosto".equals(propertyId)) {
	                return cuentaCentroCostoCombo;
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


package org.sanjose.web;

import java.util.Arrays;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.sanjose.model.CuentaContable;
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
public class CuentaContableForm extends VerticalLayout {

		CuentaContable cuentaContable;
		CuentaContableTable cuentaContableTable;
		final Form cuentaContableForm = new Form();
		boolean isNew = false;
		static final Logger logger = Logger.getLogger(CuentaContableForm.class
				.getName());
        
	    public CuentaContableForm(CuentaContable account, CuentaContableTable cuentaContableTable) {
	    	this.cuentaContable = account;
	    	this.cuentaContableTable = cuentaContableTable;
	        cuentaContableForm.setCaption("Editar los detalles del Cuenta Contable");
	    	buildForm();
	    }
	    
	    public CuentaContableForm() {
	    	isNew = true;
	    	cuentaContable = new CuentaContable(); // a account POJO
	        cuentaContableForm.setCaption("Nuevo Cuenta Contable");
	        buildForm();
	    }
	    
	    public CuentaContableForm(CuentaContableTable cuentaContableTable) {
	    	this();
	    	this.cuentaContableTable = cuentaContableTable;
	    }
	        
	    private void buildForm() {    
	    	setCaption("Cuentas contables");		        
	        final BeanItem<CuentaContable> accountItem = new BeanItem<CuentaContable>(cuentaContable); // item fromus;
	                                                                    // POJO
	        // Create the Form
	        cuentaContableForm.setWriteThrough(false); // we want explicit 'apply'
	        cuentaContableForm.setInvalidCommitted(false); // no invalid values in datamodel

	        // FieldFactory for customizing the fields and adding validators
	        cuentaContableForm.setFormFieldFactory(new CuentaContableFieldFactory());
	        cuentaContableForm.setItemDataSource(accountItem); // bind to POJO via BeanItem

	        // Determines which properties are shown, and in which order:
	        cuentaContableForm.setVisibleItemProperties(Arrays.asList(new String[] {
	        		"codigo", "isLeaf", "level", "nombre", "descripcion", "categoriaCuentaContable" }));

	        // Add form to layout
	        addComponent(cuentaContableForm);

	        // The cancel / apply buttons
	        HorizontalLayout buttons = new HorizontalLayout();
	        buttons.setSpacing(true);

	        Button apply = new Button("Guardar", new Button.ClickListener() {
	            public void buttonClick(ClickEvent event) {
	            	EntityTransaction trans = null;
                    try {
	                    cuentaContableForm.commit();
	                    if (cuentaContable.getId()!=null) {
	                    	logger.info("Got cuentaContable to save: " + cuentaContable.toString());
	                    	Item item = cuentaContableTable.container.getItem(cuentaContable.getId());
	                    	logger.info("Got item to save: " + item);	                    	
	                    	cuentaContableTable.container.updateEntityItem(item, accountItem);
	                    	item.getItemProperty("categoriaCuentaContable").setValue(cuentaContable.getCategoriaCuentaContable());
	                    	//item.getItemProperty("cuentaCuentaContable").setValue(cuentaContable.getCuentaCuentaContable());	                    	
	                    }
	                    else { 
	                    	
	                    	EntityManager em = ConfigurationUtil.getNewEntityManager();
	            			MutableLocalEntityProvider<CuentaContable> cuentaContableEP = new BatchableLocalEntityProvider<CuentaContable>(
	            					CuentaContable.class, em);
	            			cuentaContableEP.setTransactionsHandledByProvider(false);
	            						
	            			trans = em.getTransaction();
	            			trans.begin();	            			
	                    	//Object catAdded = catCuentaEP.addEntity(cuentaContable);
	                    	//Object cuentaAdded = cuentaEP.addEntity(cajaCuenta);
	                    	//cuentaContable.setCategoriaCuentaContable(categoriaCuentaContable);
	                    	cuentaContableEP.addEntity(cuentaContable);
	                    	//catCuentaEP.updateEntity(cuentaContable);
	                    	trans.commit();
	                    }
	                    removeAllComponents();
	                    getWindow().getParent().removeWindow(getWindow());
	            		//
	        			if (cuentaContableTable!=null) cuentaContableTable.redraw();	        			
	                } catch (Exception e) {
	                	e.printStackTrace();
	                	if (trans!=null) trans.rollback();
	                	getWindow().showNotification("Problema al anadir Cuenta contable: ", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
	                }
	            }
	        });
	        buttons.addComponent(apply);
	        cuentaContableForm.getFooter().addComponent(buttons);
	        cuentaContableForm.getFooter().setMargin(false, false, true, true);

	    }

	    private class CuentaContableFieldFactory extends VasjaFieldFactory {

	        final ComboBox categoriaCuentaContableCombo = new ComboBox();
	        final ComboBox cuentaCuentaContableCombo = new ComboBox();
			
			public CuentaContableFieldFactory() {
	        	Filter f = Filters.eq("isLeaf", false);
				DataFilterUtil.bindComboBox(categoriaCuentaContableCombo, "nombre",
						"Seleccione categoria de cuenta contable", CuentaContable.class, f);
				categoriaCuentaContableCombo.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));	     
				categoriaCuentaContableCombo.setVisible(true);
				//f = Filters.eq("isCaja", false);
				//DataFilterUtil.bindComboBox(cuentaCuentaContableCombo, "numero",
				//		"Seleccione cuenta", Cuenta.class, f, "nombre");
	        }

	        @Override
	        public Field createField(Item item, Object propertyId,
	                Component uiContext) {
	            Field f;
	            if ("categoriaCuentaContable".equals(propertyId)) {
	                return categoriaCuentaContableCombo;
	            //} else if ("cuentaCuentaContable".equals(propertyId)) {
	              //  return cuentaCuentaContableCombo;
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


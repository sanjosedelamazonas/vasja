package org.sanjose.web;

import java.util.Arrays;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.sanjose.model.RubroInstitucional;
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
public class RubroInstitucionalForm extends VerticalLayout {

		RubroInstitucional rubroInstitucional;
		RubroInstitucionalTable rubroInstitucionalTable;
		final Form rubroInstitucionalForm = new Form();
		boolean isNew = false;
		static final Logger logger = Logger.getLogger(RubroInstitucionalForm.class
				.getName());
        
	    public RubroInstitucionalForm(RubroInstitucional account, RubroInstitucionalTable rubroInstitucionalTable) {
	    	this.rubroInstitucional = account;
	    	this.rubroInstitucionalTable = rubroInstitucionalTable;
	        rubroInstitucionalForm.setCaption("Editar los detalles de Rubro Institucional");
	    	buildForm();
	    }
	    
	    public RubroInstitucionalForm() {
	    	isNew = true;
	    	rubroInstitucional = new RubroInstitucional(); // a account POJO
	        rubroInstitucionalForm.setCaption("Nuevo Rubro Institucional");
	        buildForm();
	    }
	    
	    public RubroInstitucionalForm(RubroInstitucionalTable rubroInstitucionalTable) {
	    	this();
	    	this.rubroInstitucionalTable = rubroInstitucionalTable;
	    }
	        
	    private void buildForm() {    
	    	setCaption("Rubros Institucionales");		        
	        final BeanItem<RubroInstitucional> accountItem = new BeanItem<RubroInstitucional>(rubroInstitucional); // item fromus;
	                                                                    // POJO
	        // Create the Form
	        rubroInstitucionalForm.setWriteThrough(false); // we want explicit 'apply'
	        rubroInstitucionalForm.setInvalidCommitted(false); // no invalid values in datamodel

	        // FieldFactory for customizing the fields and adding validators
	        rubroInstitucionalForm.setFormFieldFactory(new RubroInstitucionalFieldFactory());
	        rubroInstitucionalForm.setItemDataSource(accountItem); // bind to POJO via BeanItem

	        // Determines which properties are shown, and in which order:
	        rubroInstitucionalForm.setVisibleItemProperties(Arrays.asList(new String[] {
	        		"codigo", "isLeaf", "level", "nombre", "descripcion", "categoriaRubroInstitucional" }));

	        // Add form to layout
	        addComponent(rubroInstitucionalForm);

	        // The cancel / apply buttons
	        HorizontalLayout buttons = new HorizontalLayout();
	        buttons.setSpacing(true);

	        Button apply = new Button("Guardar", new Button.ClickListener() {
	            public void buttonClick(ClickEvent event) {
	            	EntityTransaction trans = null;
                    try {
	                    rubroInstitucionalForm.commit();
	                    if (rubroInstitucional.getId()!=null) {
	                    	logger.info("Got rubroInstitucional to save: " + rubroInstitucional.toString());
	                    	Item item = rubroInstitucionalTable.container.getItem(rubroInstitucional.getId());
	                    	logger.info("Got item to save: " + item);	                    	
	                    	rubroInstitucionalTable.container.updateEntityItem(item, accountItem);
	                    	item.getItemProperty("categoriaRubroInstitucional").setValue(rubroInstitucional.getCategoriaRubroInstitucional());
	                    	//item.getItemProperty("cuentaRubroInstitucional").setValue(rubroInstitucional.getCuentaRubroInstitucional());	                    	
	                    }
	                    else { 
	                    	
	                    	EntityManager em = ConfigurationUtil.getNewEntityManager();
	            			MutableLocalEntityProvider<RubroInstitucional> rubroInstitucionalEP = new BatchableLocalEntityProvider<RubroInstitucional>(
	            					RubroInstitucional.class, em);
	            			rubroInstitucionalEP.setTransactionsHandledByProvider(false);
	            						
	            			trans = em.getTransaction();
	            			trans.begin();	            			
	                    	//Object catAdded = catCuentaEP.addEntity(rubroInstitucional);
	                    	//Object cuentaAdded = cuentaEP.addEntity(cajaCuenta);
	                    	//rubroInstitucional.setCategoriaRubroInstitucional(categoriaRubroInstitucional);
	                    	rubroInstitucionalEP.addEntity(rubroInstitucional);
	                    	//catCuentaEP.updateEntity(rubroInstitucional);
	                    	trans.commit();
	                    }
	                    removeAllComponents();
	                    getWindow().getParent().removeWindow(getWindow());
	            		//
	        			if (rubroInstitucionalTable!=null) rubroInstitucionalTable.redraw();	        			
	                } catch (Exception e) {
	                	e.printStackTrace();
	                	if (trans!=null) trans.rollback();
	                	getWindow().showNotification("Problema al anadir Rubro Institucional: ", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
	                }
	            }
	        });
	        buttons.addComponent(apply);
	        rubroInstitucionalForm.getFooter().addComponent(buttons);
	        rubroInstitucionalForm.getFooter().setMargin(false, false, true, true);

	    }

	    private class RubroInstitucionalFieldFactory extends VasjaFieldFactory {

	        final ComboBox categoriaRubroInstitucionalCombo = new ComboBox();
	        final ComboBox cuentaRubroInstitucionalCombo = new ComboBox();
			
			public RubroInstitucionalFieldFactory() {
	        	Filter f = Filters.eq("isLeaf", false);
				DataFilterUtil.bindComboBox(categoriaRubroInstitucionalCombo, "nombre",
						"Seleccione categoria del rubro institucional", RubroInstitucional.class, f);
				categoriaRubroInstitucionalCombo.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));	     
				categoriaRubroInstitucionalCombo.setVisible(true);
				//f = Filters.eq("isCaja", false);
				//DataFilterUtil.bindComboBox(cuentaRubroInstitucionalCombo, "numero",
				//		"Seleccione cuenta", Cuenta.class, f, "nombre");
	        }

	        @Override
	        public Field createField(Item item, Object propertyId,
	                Component uiContext) {
	            Field f;
	            if ("categoriaRubroInstitucional".equals(propertyId)) {
	                return categoriaRubroInstitucionalCombo;
	            //} else if ("cuentaRubroInstitucional".equals(propertyId)) {
	              //  return cuentaRubroInstitucionalCombo;
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


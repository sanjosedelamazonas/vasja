package org.sanjose.web;

import java.util.Arrays;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.sanjose.model.Banco;
import org.sanjose.model.CategoriaCuenta;
import org.sanjose.model.Cuenta;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.util.DataFilterUtil;
import org.sanjose.util.VasjaFieldFactory;
import org.sanjose.web.helper.SessionHandler;

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
public class CategoriaCuentaForm extends VerticalLayout {

		CategoriaCuenta categoriaCuenta;
		CategoriaCuentaTable categoriaCuentaTable;
		final Form categoriaCuentaForm = new Form();
		boolean isNew = false;
		static final Logger logger = Logger.getLogger(CategoriaCuentaForm.class
				.getName());
        
	    public CategoriaCuentaForm(CategoriaCuenta account, CategoriaCuentaTable categoriaCuentaTable) {
	    	this.categoriaCuenta = account;
	    	this.categoriaCuentaTable = categoriaCuentaTable;
	        categoriaCuentaForm.setCaption("Editar los detalles de la Categoria de Cuentas");
	    	buildForm();
	    }
	    
	    public CategoriaCuentaForm() {
	    	isNew = true;
	    	categoriaCuenta = new CategoriaCuenta(); // a account POJO
	        categoriaCuentaForm.setCaption("Nueva Categoria de Cuentas");
	        buildForm();
	    }
	    
	    public CategoriaCuentaForm(CategoriaCuentaTable categoriaCuentaTable) {
	    	this();
	    	this.categoriaCuentaTable = categoriaCuentaTable;
	    }
	        
	    private void buildForm() {    
	    	setCaption("Categoria de Cuentas");		        
	        final BeanItem<CategoriaCuenta> accountItem = new BeanItem<CategoriaCuenta>(categoriaCuenta); // item fromus;
	                                                                    // POJO
	        // Create the Form
	        categoriaCuentaForm.setWriteThrough(false); // we want explicit 'apply'
	        categoriaCuentaForm.setInvalidCommitted(false); // no invalid values in datamodel

	        // FieldFactory for customizing the fields and adding validators
	        categoriaCuentaForm.setFormFieldFactory(new CategoriaCuentaFieldFactory());
	        categoriaCuentaForm.setItemDataSource(accountItem); // bind to POJO via BeanItem

	        // Determines which properties are shown, and in which order:
	        categoriaCuentaForm.setVisibleItemProperties(Arrays.asList(new String[] {
	        		"nombre", "descripcion", "cajaCuenta", "bancoPen", "bancoUsd" }));
	        if (isNew) {
	        	TextField cajaNumeroField = new TextField("Caja Numero");
	        	cajaNumeroField.setValue(ConfigurationUtil.getNewCuentaNumber());		        
	        	categoriaCuentaForm.addField("cajaNumero", cajaNumeroField);
	        }

	        // Add form to layout
	        addComponent(categoriaCuentaForm);

	        // The cancel / apply buttons
	        HorizontalLayout buttons = new HorizontalLayout();
	        buttons.setSpacing(true);

	        Button apply = new Button("Guardar", new Button.ClickListener() {
	            public void buttonClick(ClickEvent event) {
	            	EntityTransaction trans = null;
                    try {
	                    categoriaCuentaForm.commit();
	                    if (categoriaCuenta.getId()!=null) {
	            		/*	Object bancoNombre = categoriaCuentaForm.getItemProperty("bancoPen").getValue();
	            			logger.info("Got bancoNombre PEN: " + bancoNombre);
	            			if (bancoNombre!=null) {
	            				categoriaCuenta.setBancoPen(ConfigurationUtil.getBancoPorNombre((String)bancoNombre, categoriaCuentaTable.container.getEntityProvider().getEntityManager()));
	            			}
	            			bancoNombre = categoriaCuentaForm.getItemProperty("bancoUsd").getValue();
	            			logger.info("Got bancoNombre USD: " + bancoNombre);
	            			if (bancoNombre!=null) {
	            				categoriaCuenta.setBancoUsd(ConfigurationUtil.getBancoPorNombre((String)bancoNombre, categoriaCuentaTable.container.getEntityProvider().getEntityManager()));
	            			}*/
	                    	logger.info("Got categoriaCuenta to save: " + categoriaCuenta.toString());
	                    	Item item = categoriaCuentaTable.container.getItem(categoriaCuenta.getId());	                    	
	                    	logger.info("Got item to save: " + item);	                    	
	                    	item.getItemProperty("bancoPen").setValue(categoriaCuenta.getBancoPen());
	                    	item.getItemProperty("bancoUsd").setValue(categoriaCuenta.getBancoUsd());
	                    	item.getItemProperty("cajaCuenta").setValue(categoriaCuenta.getCajaCuenta());
	                    	categoriaCuentaTable.container.updateEntityItem(item, accountItem);
	                    }
	                    else { 
	                    	
	                    	EntityManager em = ConfigurationUtil.getNewEntityManager();
	            			MutableLocalEntityProvider<Cuenta> cuentaEP =  new BatchableLocalEntityProvider<Cuenta>(
	            					Cuenta.class, em);
	            			cuentaEP.setTransactionsHandledByProvider(false);
	            			MutableLocalEntityProvider<CategoriaCuenta> catCuentaEP = new BatchableLocalEntityProvider<CategoriaCuenta>(
	            					CategoriaCuenta.class, em);
	            			catCuentaEP.setTransactionsHandledByProvider(false);
	            						
	            			trans = em.getTransaction();
	            			trans.begin();
	            			Long cajaNum = null;
	            			Object val = categoriaCuentaForm.getItemProperty("cajaNumero").getValue();
	            			if (val instanceof Long) cajaNum = (Long)val;
	            			if (val instanceof String) cajaNum = Long.parseLong((String)val);
	                    	// Create CAJA cuenta automatically
	                    	Cuenta cajaCuenta = new Cuenta();	            			
	            			Cuenta cnta = ConfigurationUtil.getCuentaPorNumero(cajaNum, em);
	            			if (cnta!=null) {
	            				if (!cnta.getIsCaja()) throw new Exception("Cuenta numero: " + cajaNum + " existe, pero no es de tipo caja!");
	            				cajaCuenta = cnta;
	            			} else {
	            				cajaCuenta.setCreadoPorUsuario(SessionHandler.get());
		                    	cajaCuenta.setDescripcion("CAJA cuenta para: " + categoriaCuenta.getDescripcion());
		                    	cajaCuenta.setNombre("CAJA_" + categoriaCuenta.getNombre());
		                    	cajaCuenta.setIsCaja(true);
		                    	cajaCuenta.setNumero(cajaNum);
	            			}
	                    	//cajaCuenta.setNumero(ConfigurationUtil.getNewCuentaNumber(em));
	            			Object bancoNombre = categoriaCuentaForm.getItemProperty("bancoPen").getValue();
	            			logger.info("Got bancoNombre PEN: " + bancoNombre);
	            			if (bancoNombre!=null) {
	            				categoriaCuenta.setBancoPen(ConfigurationUtil.getBancoPorNombre((String)bancoNombre, em));
	            			}
	            			bancoNombre = categoriaCuentaForm.getItemProperty("bancoUsd").getValue();
	            			logger.info("Got bancoNombre USD: " + bancoNombre);
	            			if (bancoNombre!=null) {
	            				categoriaCuenta.setBancoUsd(ConfigurationUtil.getBancoPorNombre((String)bancoNombre, em));
	            			}
	            			CategoriaCuenta catCajaBancos = ConfigurationUtil.getCategoriaCuentaPorNombre("Caja y bancos", em);
	                    	// if caja y bancos does not exist add it
	                    	if (catCajaBancos==null) {
	                    		CategoriaCuenta catCaja = new CategoriaCuenta();
	                    		catCaja.setNombre("Caja y bancos");
	                    		catCajaBancos = catCuentaEP.addEntity(catCaja);
	                    	}
	                    	//Object catAdded = catCuentaEP.addEntity(categoriaCuenta);
	                    	cajaCuenta.setCategoriaCuenta(catCajaBancos);
	                    	//Object cuentaAdded = cuentaEP.addEntity(cajaCuenta);
	                    	categoriaCuenta.setCajaCuenta(cajaCuenta);
	                    	catCuentaEP.addEntity(categoriaCuenta);
	                    	//catCuentaEP.updateEntity(categoriaCuenta);
	                    	trans.commit();
	                    }
	                    removeAllComponents();
	                    getWindow().getParent().removeWindow(getWindow());
	            		//
	        			if (categoriaCuentaTable!=null) categoriaCuentaTable.redraw();	        			
	                } catch (Exception e) {
	                	e.printStackTrace();
	                	if (trans!=null) trans.rollback();
	                	getWindow().showNotification("Problema al anadir Categoria de cuentas: ", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
	                }
	            }
	        });
	        buttons.addComponent(apply);
	        categoriaCuentaForm.getFooter().addComponent(buttons);
	        categoriaCuentaForm.getFooter().setMargin(false, false, true, true);

	    }

	    private class CategoriaCuentaFieldFactory extends VasjaFieldFactory {

	        final ComboBox cajaCombo = new ComboBox("Cuenta de caja");
	        final ComboBox bancoPenCombo = new ComboBox("Banco PEN");
	        final ComboBox bancoUsdCombo = new ComboBox("Banco USD");
			
			public CategoriaCuentaFieldFactory() {
	        	Filter f = Filters.eq("isCaja", true);
				DataFilterUtil.bindComboBox(cajaCombo, "numero",
						"Seleccione cuenta de Caja", Cuenta.class, f, "nombre");
				cajaCombo.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));	     
				f = Filters.and(Filters.eq("bancoCuenta.isCaja", true), Filters.eq("moneda", "USD"));
				DataFilterUtil.bindComboBox(bancoUsdCombo, "nombre",
							"Seleccione Banco USD", Banco.class, f);
				bancoUsdCombo.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));	     				
				f = Filters.and(Filters.eq("bancoCuenta.isCaja", true), Filters.eq("moneda", "PEN"));
				DataFilterUtil.bindComboBox(bancoPenCombo, "nombre",
							"Seleccione Banco PEN", Banco.class, f);
				bancoPenCombo.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));	     				
				if (isNew) {
					bancoPenCombo.setVisible(false);
					bancoUsdCombo.setVisible(false);					
					cajaCombo.setVisible(false);
				}
				
				
				
				
				
	        }

	        @Override
	        public Field createField(Item item, Object propertyId,
	                Component uiContext) {
	            Field f;
	            if ("cajaCuenta".equals(propertyId)) {	            	
	                return cajaCombo;
	            } else if ("bancoPen".equals(propertyId)) {	            	
	                return bancoPenCombo;
	            } else if ("bancoUsd".equals(propertyId)) {	            	
	            	return bancoUsdCombo;
	            }else if ("descripcion".equals(propertyId)) {
	                f = createTextArea(propertyId);
	                f.setHeight("6em");
	                f.setWidth("16em");
	            } else if ("cajaNumero".equals(propertyId)) {
	            	f = super.createField(item, propertyId, uiContext);
					f.setValue(ConfigurationUtil.getNewCuentaNumber());
					if (isNew) f.setVisible(true);
					else f.setVisible(false);
	                return f;
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


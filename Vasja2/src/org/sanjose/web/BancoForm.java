package org.sanjose.web;

import java.util.Arrays;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.sanjose.model.Banco;
import org.sanjose.model.Banco.Moneda;
import org.sanjose.model.CategoriaCuenta;
import org.sanjose.model.Cuenta;
import org.sanjose.model.Operacion.Tipo;
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
import com.vaadin.data.util.IndexedContainer;
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
public class BancoForm extends VerticalLayout {

		Banco banco;
		BancoTable bancoTable;
		final Form bancoForm = new Form();
		boolean isNew = false;
		static final Logger logger = Logger.getLogger(BancoForm.class
				.getName());
        
	    public BancoForm(Banco account, BancoTable bancoTable) {
	    	this.banco = account;
	    	this.bancoTable = bancoTable;
	        bancoForm.setCaption("Editar los detalles del Banco");
	    	buildForm();
	    }
	    
	    public BancoForm() {
	    	isNew = true;
	    	banco = new Banco(); // a account POJO
	        bancoForm.setCaption("Nuevo Banco");
	        buildForm();
	    }
	    
	    public BancoForm(BancoTable bancoTable) {
	    	this();
	    	this.bancoTable = bancoTable;
	    }
	        
	    private void buildForm() {    
	    	setCaption("Banco");		        
	        final BeanItem<Banco> accountItem = new BeanItem<Banco>(banco); // item fromus;
	                                                                    // POJO
	        // Create the Form
	        bancoForm.setWriteThrough(false); // we want explicit 'apply'
	        bancoForm.setInvalidCommitted(false); // no invalid values in datamodel

	        // FieldFactory for customizing the fields and adding validators
	        bancoForm.setFormFieldFactory(new BancoFieldFactory());
	        bancoForm.setItemDataSource(accountItem); // bind to POJO via BeanItem

	        // Determines which properties are shown, and in which order:
	        bancoForm.setVisibleItemProperties(Arrays.asList(new String[] {
	        		"banco", "moneda", "nombre", "numero", "descripcion", "bancoCuenta" }));
	        if (isNew) {
	        	TextField cajaNumeroField = new TextField("Banco cuenta numero");
	        	cajaNumeroField.setValue(ConfigurationUtil.getNewCuentaNumber());		        
	        	bancoForm.addField("bancoCuentaNumero", cajaNumeroField);
	        }

	        // Add form to layout
	        addComponent(bancoForm);

	        // The cancel / apply buttons
	        HorizontalLayout buttons = new HorizontalLayout();
	        buttons.setSpacing(true);

	        Button apply = new Button("Guardar", new Button.ClickListener() {
	            public void buttonClick(ClickEvent event) {
	            	EntityTransaction trans = null;
                    try {
	                    bancoForm.commit();
	                    if (banco.getId()!=null) {
	                    	logger.info("Got banco to save: " + banco.toString());
	                    	Item item = bancoTable.container.getItem(banco.getId());
	                    	logger.info("Got item to save: " + item);                		
	                    	bancoTable.container.updateEntityItem(item, accountItem);
	                    	//item.getItemProperty("moneda").setValue(banco.getMoneda());

	                    	item.getItemProperty("bancoCuenta").setValue(banco.getBancoCuenta());
	                    }
	                    else { 
	                    	
	                    	EntityManager em = ConfigurationUtil.getNewEntityManager();
	            			MutableLocalEntityProvider<Cuenta> cuentaEP =  new BatchableLocalEntityProvider<Cuenta>(
	            					Cuenta.class, em);
	            			cuentaEP.setTransactionsHandledByProvider(false);
	            			MutableLocalEntityProvider<Banco> bancoEP = new BatchableLocalEntityProvider<Banco>(
	            					Banco.class, em);
	            			MutableLocalEntityProvider<CategoriaCuenta> catCuentaEP = new BatchableLocalEntityProvider<CategoriaCuenta>(
	            					CategoriaCuenta.class, em);
	            			catCuentaEP.setTransactionsHandledByProvider(false);
	            						
	            			trans = em.getTransaction();
	            			trans.begin();
	            			Long cajaNum = null;
	            			Object val = bancoForm.getItemProperty("bancoCuentaNumero").getValue();
	            			if (val instanceof Long) cajaNum = (Long)val;
	            			if (val instanceof String) cajaNum = Long.parseLong((String)val);
	                    	// Create CAJA cuenta automatically
	                    	Cuenta cajaCuenta = new Cuenta();	            			
	            			Cuenta cnta = ConfigurationUtil.getCuentaPorNumero(cajaNum, em);
	            			if (cnta!=null) {
	            				if (!cnta.getIsCaja()) throw new Exception("Cuenta numero: " + cajaNum + " existe, pero no es de tipo caja/banco!");
	            				cajaCuenta = cnta;
	            			} else {
	            				cajaCuenta.setCreadoPorUsuario(SessionHandler.get());
		                    	cajaCuenta.setDescripcion("BANCO cuenta en " + bancoForm.getItemProperty("moneda").getValue() + " para: " + banco.getDescripcion());
		                    	cajaCuenta.setNombre("BANCO_" +  bancoForm.getItemProperty("moneda").getValue() + "_" + banco.getNombre());
		                    	cajaCuenta.setIsCaja(true);
		                    	cajaCuenta.setIsBanco(true);
		                    	cajaCuenta.setNumero(cajaNum);
	            			}
	                    	//cajaCuenta.setNumero(ConfigurationUtil.getNewCuentaNumber(em));
	                		
	                    	CategoriaCuenta cat = ConfigurationUtil.getCategoriaCuentaPorNombre("Caja y bancos", em);
	                    	// if caja y bancos does not exist add it
	                    	if (cat==null) {
	                    		CategoriaCuenta catCaja = new CategoriaCuenta();
	                    		catCaja.setNombre("Caja y bancos");
	                    		cat = catCuentaEP.addEntity(catCaja);
	                    	}
	            			
	                    	//Object catAdded = catCuentaEP.addEntity(banco);
	                    	cajaCuenta.setCategoriaCuenta(cat);
	                    	//Object cuentaAdded = cuentaEP.addEntity(cajaCuenta);
	                    	banco.setBancoCuenta(cajaCuenta);
	                    	bancoEP.addEntity(banco);
	                    	//catCuentaEP.updateEntity(banco);
	                    	trans.commit();
	                    }
	                    removeAllComponents();
	                    getWindow().getParent().removeWindow(getWindow());
	            		//
	        			if (bancoTable!=null) bancoTable.redraw();	        			
	                } catch (Exception e) {
	                	e.printStackTrace();
	                	if (trans!=null) trans.rollback();
	                	getWindow().showNotification("Problema al anadir Banco: ", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
	                }
	            }
	        });
	        buttons.addComponent(apply);
	        bancoForm.getFooter().addComponent(buttons);
	        bancoForm.getFooter().setMargin(false, false, true, true);

	    }

	    private class BancoFieldFactory extends VasjaFieldFactory {

	        final ComboBox cajaCombo = new ComboBox("Banco Cuenta");
	        final ComboBox monedaCombo = new ComboBox("Moneda");
			
			public BancoFieldFactory() {
				monedaCombo.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
				IndexedContainer c = new IndexedContainer();
				c.addContainerProperty("nombre", String.class, "");
				Item item = c.addItem(Moneda.PEN.toString());
				item.getItemProperty("nombre").setValue(Moneda.PEN.toString());
				item = c.addItem(Moneda.USD.toString());
				item.getItemProperty("nombre").setValue(Moneda.USD.toString());
				monedaCombo.setContainerDataSource(c);
				monedaCombo.setItemCaptionPropertyId("nombre");
				monedaCombo.setRequired(true);
				monedaCombo.setImmediate(true);
				
				Filter f = Filters.eq("isBanco", true);				
				DataFilterUtil.bindComboBox(cajaCombo, "numero",
						"Seleccione cuenta de Banco", Cuenta.class, f, "nombre");
				cajaCombo.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));	     
				if (isNew) {
					cajaCombo.setVisible(false);
				}
	        }

	        @Override
	        public Field createField(Item item, Object propertyId,
	                Component uiContext) {
	            Field f;
	            if ("bancoCuenta".equals(propertyId)) {	            	
	                return cajaCombo;
	            } else if ("moneda".equals(propertyId)) {	            	
	                return monedaCombo;
	            } else if ("descripcion".equals(propertyId)) {
	                f = createTextArea(propertyId);
	                f.setHeight("6em");
	                f.setWidth("16em");
	            } else if ("bancoCuentaNumero".equals(propertyId)) {
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


package org.sanjose.web;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.sanjose.model.CategoriaCuenta;
import org.sanjose.model.Cuenta;
import org.sanjose.model.Propietario;
import org.sanjose.model.Usuario;
import org.sanjose.model.Operacion.Tipo;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.util.CuentaNumeroValidator;
import org.sanjose.util.IntegerValidator;
import org.sanjose.util.VasjaFieldFactory;
import org.sanjose.web.helper.SessionHandler;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CuentaForm extends VerticalLayout {

		Cuenta cuenta;
		CuentaTable cuentaTable;
		final Form cuentaForm = new Form();
		boolean isNew = false;
		static final Logger logger = Logger.getLogger(CuentaForm.class
				.getName());
        
	    public CuentaForm(Cuenta account, CuentaTable cuentaTable) {
	    	this.cuenta = account;
	    	this.cuentaTable = cuentaTable;
	        cuentaForm.setCaption("Editar los detalles de la cuenta");
	    	buildForm();
	    }
	    
	    public CuentaForm() {
	    	isNew = true;
	    	cuenta = new Cuenta(); // a account POJO
	    	cuenta.setNumero(ConfigurationUtil.getNewCuentaNumber(ConfigurationUtil.getEntityManager()));
	        cuentaForm.setCaption("Nueva cuenta");
	        buildForm();
	    }
	    
	    public CuentaForm(CuentaTable cuentaTable) {
	    	this();
            cuenta.setCreadoPorUsuario(SessionHandler.get());
	    	this.cuentaTable = cuentaTable;
	    }
	        
	    private void buildForm() {    
	    	setCaption("Cuenta");		        
	        final BeanItem<Cuenta> accountItem = new BeanItem<Cuenta>(cuenta); // item fromus;
	                                                                    // POJO
	        // Create the Form
	        cuentaForm.setWriteThrough(false); // we want explicit 'apply'
	        cuentaForm.setInvalidCommitted(false); // no invalid values in datamodel

	        // FieldFactory for customizing the fields and adding validators
	        cuentaForm.setFormFieldFactory(new AccountFieldFactory());
	        cuentaForm.setItemDataSource(accountItem); // bind to POJO via BeanItem

	        // Determines which properties are shown, and in which order:
	        cuentaForm.setVisibleItemProperties(Arrays.asList(new String[] {
	        		"numero", "nombre", "categoriaCuenta", "descripcion", "propietario", "isCaja", "isBanco", "fechaDeApertura", "creadoPorUsuario", "pen", "usd","closed" }));

	        // Add form to layout
	        addComponent(cuentaForm);

	        // The cancel / apply buttons
	        HorizontalLayout buttons = new HorizontalLayout();
	        buttons.setSpacing(true);

	        Button apply = new Button("Guardar", new Button.ClickListener() {
	            public void buttonClick(ClickEvent event) {
	                try {
	                    cuentaForm.commit();
	                    if (cuenta.getId()!=null) {
	                    	logger.info("Got cuenta to save: " + cuenta.toString());
	                    	Item item = cuentaTable.container.getItem(cuenta.getId());
	                    	logger.info("Got item to save: " + item);	                    	
	                    	cuentaTable.container.updateEntityItem(item, accountItem);
	                    	item.getItemProperty("propietario").setValue(cuenta.getPropietario());
	                    	item.getItemProperty("creadoPorUsuario").setValue(cuenta.getCreadoPorUsuario());
	                    	if (cuenta.getIsCaja()) {
		                    	CategoriaCuenta cat = ConfigurationUtil.getCategoriaCuentaPorNombre("Caja y bancos", ConfigurationUtil.getEntityManager());
		                    	// if caja y bancos does not exist add it
		                    	if (cat==null) {
		                    		CategoriaCuenta catCaja = new CategoriaCuenta();
		                    		catCaja.setNombre("Caja y bancos");
		                    		cat = ConfigurationUtil.getCategoriaCuentaEP().addEntity(catCaja);
		                    	}
		                    	item.getItemProperty("categoriaCuenta").setValue(cat);
	                    	} else
	                    		item.getItemProperty("categoriaCuenta").setValue(cuenta.getCategoriaCuenta());	                    
	                    	cuentaTable.container.commit();
	                    //	cuentaTable.container.getEntityProvider().getEntityManager().flush();
	                    }
	                    else {
	                    	EntityItem<Cuenta> myNewItem = cuentaTable.container.createEntityItem(accountItem.getBean());
	                    	cuentaTable.container.addEntity(myNewItem.getEntity());
	                    	cuentaTable.container.commit();
	                    }
	                    getWindow().getParent().removeWindow(getWindow());                   
	                } catch (Exception e) {
	                    // Ignored, we'll let the Form handle the errors
	                }
	            }
	        });
	        buttons.addComponent(apply);
	        cuentaForm.getFooter().addComponent(buttons);
	        cuentaForm.getFooter().setMargin(false, false, true, true);

	    }

	    private class AccountFieldFactory extends VasjaFieldFactory {

	        final ComboBox propietarios = new ComboBox("Coordinador");
	        final ComboBox creadoPorUsuario = new ComboBox("Creado por usuario");
	        final ComboBox categorias = new ComboBox("Categoria");
	        final CheckBox esCajaChkBox = new CheckBox("Es Caja");
	        final CheckBox esBancoChkBox = new CheckBox("Es Banco");
	        final CheckBox esCerradaChkBox = new CheckBox("Esta Cerrada");
	        
	        public AccountFieldFactory() {
	            propietarios.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
	            IndexedContainer c = new IndexedContainer();
	            c.addContainerProperty("nombre", String.class, "");

	            JPAContainer jpaContainer = JPAContainerFactory.make(Propietario.class, ConfigurationUtil.getEntityManager());
        		Collection<Object> proprietarioIds = jpaContainer.getItemIds();

	            //MutableLocalEntityProvider<Propietario> proprietarioEntityProvider = ConfigurationUtil.getProprietarioEP();
	            //List<Object> proprietarioIds = proprietarioEntityProvider.getAllEntityIdentifiers(null, null);
	            for (Object proprietarioId : proprietarioIds) {
	            	Propietario propietario = (Propietario)jpaContainer.getItem(proprietarioId).getEntity();//proprietarioEntityProvider.getEntity(proprietarioId);
	                Item item = c.addItem(propietario);
	                item.getItemProperty("nombre").setValue(propietario.getNombre());
	            }
        
	            propietarios.setContainerDataSource(c);
	            propietarios.setItemCaptionPropertyId("nombre");
	            propietarios.setRequired(true);
	            propietarios.setFilteringMode(ComboBox.FILTERINGMODE_STARTSWITH);
	            
	            creadoPorUsuario.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
	            IndexedContainer cc = new IndexedContainer();
	            cc.addContainerProperty("usuario", String.class, "");
	            
//	            MutableLocalEntityProvider<Usuario> userEntityProvider = ConfigurationUtil.getUsuarioEP();
//	            List<Object> userIds = userEntityProvider.getAllEntityIdentifiers(null, null);
	            JPAContainer jpaContainerUsuario = JPAContainerFactory.make(Usuario.class, ConfigurationUtil.getEntityManager());
        		Collection<Object> userIds = jpaContainerUsuario.getItemIds();

	            for (Object userId : userIds) {
	            	Usuario user = (Usuario)jpaContainerUsuario.getItem(userId).getEntity(); 
	            			//userEntityProvider.getEntity(userId);
	                Item item = cc.addItem(user);
	                item.getItemProperty("usuario").setValue(user.getUsuario());
	            }
        
	            creadoPorUsuario.setContainerDataSource(cc);
	            creadoPorUsuario.setItemCaptionPropertyId("usuario");
	            creadoPorUsuario.setFilteringMode(ComboBox.FILTERINGMODE_STARTSWITH);
	            creadoPorUsuario.setEnabled(false);

	            categorias.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
	            IndexedContainer ccc = new IndexedContainer();
	            ccc.addContainerProperty("nombre", String.class, "");
	            
	            //MutableLocalEntityProvider<CategoriaCuenta> catEntityProvider = 
	            //		ConfigurationUtil.getCategoriaCuentaEP();
	            JPAContainer jpaContainerCatCuenta = JPAContainerFactory.make(CategoriaCuenta.class, ConfigurationUtil.getEntityManager());
        		Collection<Object> catIds = jpaContainerCatCuenta.getItemIds();
	            //List<Object> catIds = catEntityProvider.getAllEntityIdentifiers(null, null);
	            for (Object catId : catIds) {
	            	CategoriaCuenta cat = (CategoriaCuenta)jpaContainerCatCuenta.getItem(catId).getEntity();
	            			//catEntityProvider.getEntity(catId);
	                Item item = ccc.addItem(cat);
	                item.getItemProperty("nombre").setValue(cat.getNombre());
	            }
        
	            categorias.setContainerDataSource(ccc);
	            categorias.setItemCaptionPropertyId("nombre");
	            categorias.setFilteringMode(ComboBox.FILTERINGMODE_STARTSWITH);
	            categorias.setRequired(true);
	            categorias.setRequiredError("Por favor rellena la categoria");	      
	            esCerradaChkBox.setImmediate(true);
	            esCajaChkBox.setImmediate(true);
	            esCajaChkBox.addListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						 logger.info("Got a change: " + event.getProperty());
						if ((Boolean)event.getProperty().getValue()) {
							Field f = cuentaForm.getField("isBanco");
							f.setValue(false);
							f = cuentaForm.getField("categoriaCuenta");
							CategoriaCuenta cat = ConfigurationUtil.getCategoriaCuentaPorNombre("Caja y bancos", ConfigurationUtil.getEntityManager());
	                    	// if caja y bancos does not exist add it
	                    	if (cat==null) {
	                    		CategoriaCuenta catCaja = new CategoriaCuenta();
	                    		catCaja.setNombre("Caja y bancos");
	                    		cat = ConfigurationUtil.getCategoriaCuentaEP().addEntity(catCaja);
	                    	}
							f.setValue(cat);
							f.setEnabled(false);
						} else {
							Field f = cuentaForm.getField("categoriaCuenta");
							f.setValue(null);
							f.setEnabled(true);
						}
					}
	            });
	            esBancoChkBox.setImmediate(true);
	            esBancoChkBox.addListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						 logger.info("Got a change: " + event.getProperty());
						if ((Boolean)event.getProperty().getValue()) {
							Field f = cuentaForm.getField("isCaja");
							if (f!=null) {
								f.setValue(true);
								f.setEnabled(false);
							}
							f = cuentaForm.getField("categoriaCuenta");
							CategoriaCuenta cat = ConfigurationUtil.getCategoriaCuentaPorNombre("Caja y bancos", ConfigurationUtil.getEntityManager());
	                    	// if caja y bancos does not exist add it
	                    	if (cat==null) {
	                    		CategoriaCuenta catCaja = new CategoriaCuenta();
	                    		catCaja.setNombre("Caja y bancos");
	                    		cat = ConfigurationUtil.getCategoriaCuentaEP().addEntity(catCaja);
	                    	}	                    	
							f.setValue(cat);
							f.setEnabled(false);
						} else {
							Field f = cuentaForm.getField("isCaja");
							f.setEnabled(true);
							f = cuentaForm.getField("categoriaCuenta");
							f.setValue(null);
							f.setEnabled(true);
						}
					}
	            });
	          
	            }

	        @Override
	        public Field createField(Item item, Object propertyId,
	                Component uiContext) {
	            Field f;
	            if ("propietario".equals(propertyId)) {
	                return propietarios;
	            } else if ("creadoPorUsuario".equals(propertyId)) {
	            	return creadoPorUsuario;
	            } else if ("categoriaCuenta".equals(propertyId)) {
	            	return categorias;
	            } else if ("descripcion".equals(propertyId)) {
	                f = createTextArea(propertyId);
	                f.setHeight("6em");
	                f.setWidth("16em");
	            } else if ("isCaja".equals(propertyId)) {
	            	logger.info("customize for isCaja, checkbox");
	                f = esCajaChkBox;	                
	            } else if ("isBanco".equals(propertyId)) {
	            	logger.info("customize for isBanco, checkbox");
	                f = esBancoChkBox;	
	            } else if ("closed".equals(propertyId)) {
	            	logger.info("customize for Closed checkbox");
	                f = esCerradaChkBox;	
	            } else if ("fechaDeApertura".equals(propertyId)) {
	            	f = createDateField(propertyId);
	            	f.setEnabled(false);
	                if (f.getValue()==null) f.setValue(new Date(System.currentTimeMillis()));
	            }
	            else {
	                f = super.createField(item, propertyId, uiContext);
	            }
	            if ("numero".equals(propertyId)) {
	                TextField tf = (TextField) f;
	                if (!isNew) tf.setEnabled(false);
	                tf.setRequired(true);
	                tf.setRequiredError("Por favor rellena el numero de cuenta");
	                tf.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
	                tf.addValidator(new IntegerValidator("El numero de cuenta no esta correcto"));
	                if (isNew) tf.addValidator(new CuentaNumeroValidator("El numero ya existe"));
	            } else if ("nombre".equals(propertyId)) {
	                TextField tf = (TextField) f;
	                tf.setRequired(false);
	                tf.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
	            }
	            else if ("pen".equals(propertyId) || "usd".equals(propertyId)) {
	            	TextField tf = (TextField) f;
	            	tf.setEnabled(false);
	                if (tf.getValue()==null) tf.setValue(new BigDecimal(0));
		        } 
	            return f;
	        }

	    }
	}


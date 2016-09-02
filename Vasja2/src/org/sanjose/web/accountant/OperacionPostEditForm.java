package org.sanjose.web.accountant;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.sanjose.model.Banco;
import org.sanjose.model.Beneficiario;
import org.sanjose.model.Cuenta;
import org.sanjose.model.Operacion;
import org.sanjose.model.Operacion.Tipo;
import org.sanjose.model.TipoDocumento;
import org.sanjose.util.AmountValidator;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.util.DataFilterUtil;
import org.sanjose.util.DecimalPropertyFormatter;
import org.sanjose.util.VasjaFieldFactory;
import org.sanjose.web.OperacionForm;
import org.sanjose.web.VasjaApp;
import org.sanjose.web.helper.IOperacionTable;
import org.sanjose.web.helper.SessionHandler;
import org.vaadin.csvalidation.CSValidatedTextField;

import com.vaadin.addon.jpacontainer.filter.Filters;
import com.vaadin.addon.jpacontainer.provider.BatchableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

public class OperacionPostEditForm extends VerticalLayout {
	Operacion operacion = null;
	final Form operacionEditForm = new Form();
	//Cuenta cuenta;
	//Set<Cuenta> cuentasSet = null;
	IOperacionTable opTable;
	boolean isACT_PEN = false;
	//CuentaTable cuentaTable = null;
	Label warning = null;
	boolean isNew = true;
	Cuenta cuenta = null;
	Cuenta opCuenta = null;
	Operacion oldOper = null;
	SimpleDateFormat sdf = new SimpleDateFormat(ConfigurationUtil.get("DEFAULT_DATE_FORMAT"), ConfigurationUtil.LOCALE);

	static final Logger logger = Logger
			.getLogger(OperacionForm.class.getName());
	
	public OperacionPostEditForm(Operacion operacion, Cuenta cuenta, IOperacionTable opTable, boolean isACT_Pen) {
		setCaption("Editar operacion " + (operacion.getIsCargo() ? "CARGO" : "ABONO") + " numero: " + operacion.getId() + ", cuenta numero: " + operacion.getCuenta().getNumero());
		this.opTable = opTable;
		this.isACT_PEN = isACT_Pen;
		this.opCuenta = operacion.cuenta;
		this.cuenta = cuenta;
		this.oldOper = operacion.clone();
		if (ConfigurationUtil.getUltimoMesCerrado().before(operacion.getFecha())) 
			buildEditForm(new BeanItem<Operacion>(operacion));
	}

	private void buildEditForm(final BeanItem<Operacion> oItem) {

		Operacion op = (Operacion) oItem.getBean();
		operacionEditForm.setWriteThrough(false); // we want explicit 'apply'
		operacionEditForm.setInvalidCommitted(false); // no invalid values in
		operacionEditForm.setItemDataSource(oItem); 
		operacionEditForm
				.setFormFieldFactory(new OperacionFieldFactory());

		operacionEditForm.setVisibleItemProperties(Arrays.asList(new String[] {"fecha", 
				 "pen", "usd", "descripcion", "tipo", "banco", "chequeNumero", "centroCosto",
				 "cuentaContable","rubroInstitucional","rubroProyecto",
				 "lugarGasto", "beneficiario",/* "tipoDocumento","docNumero",*/ "fechaDeCobro"
				}));
		// Add form to layout
		addComponent(operacionEditForm);

		Field pen = operacionEditForm.getField("pen");
		if (pen != null)
			pen.setPropertyDataSource(new DecimalPropertyFormatter(pen
					.getPropertyDataSource(), ConfigurationUtil.get("DECIMAL_FORMAT")));
		Field usd = operacionEditForm.getField("usd");
		if (usd != null)
			usd.setPropertyDataSource(new DecimalPropertyFormatter(usd
					.getPropertyDataSource(), ConfigurationUtil.get("DECIMAL_FORMAT")));
		
		if (op.getTipo().equals(Tipo.CHEQUE.toString())) {
			operacionEditForm.getField("tipo").setValue(Tipo.CHEQUE);
		}
		else if (op.getTipo().equals(Tipo.TRANSFERENCIA.toString())) {
			operacionEditForm.getField("tipo").setValue(Tipo.TRANSFERENCIA);
		}
		else {
			operacionEditForm.getField("tipo").setValue(Tipo.EFECTIVO);
		}

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		
		Button apply = new Button("Guardar");
		apply.setIcon(new ThemeResource("../icons/forward-icon.png"));
		apply.addListener(new Button.ClickListener() {
			boolean isProcessing = false;
			// Actually write the operation to the database
			public void buttonClick(ClickEvent event) {
				if (!isProcessing) {
					operacionEditForm.commit();
					isProcessing = saveOperacion(oItem, operacionEditForm);
					if (isProcessing) getWindow().getParent().removeWindow(getWindow());					
				}
			}
		});
		buttons.addComponent(apply);
		Button close = new Button("Anular", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				try {
					operacionEditForm.discard();
					removeComponent(operacionEditForm);
					getWindow().getParent().removeWindow(getWindow());
				} catch (Exception e) {
					// Ignored, we'll let the Form handle the errors
				}
			}
		});
		close.setIcon(new ThemeResource("../icons/go-back-icon.png"));
		buttons.addComponent(close);
		operacionEditForm.getFooter().addComponent(buttons);
		operacionEditForm.getFooter().setMargin(false, false, true, true);
	}

	private boolean saveOperacion(BeanItem<Operacion> oItem, Form operacionConfirmForm) {
		// Actually write the operation to the database
		EntityTransaction trans = null;
		EntityManager em = null;
		Operacion operDetalle = null;
		try {
			em = ConfigurationUtil.getNewEntityManager();
			MutableLocalEntityProvider<Operacion> operacionEP = new BatchableLocalEntityProvider<Operacion>(
					Operacion.class, em);
			MutableLocalEntityProvider<Cuenta> cuentaEP = new BatchableLocalEntityProvider<Cuenta>(
					Cuenta.class, em);			
			operacionEP.setTransactionsHandledByProvider(false);
			cuentaEP.setTransactionsHandledByProvider(false);
			trans = em.getTransaction();
			trans.begin();
			Operacion oper = (Operacion)oItem.getBean();
			oper.setIsPen((oper.getPen().compareTo(new BigDecimal(0)) > 0 
					|| oper.getPen().compareTo(new BigDecimal(0))< 0) ? true : false);
			logger.info("OLD: " + oldOper);			
			logger.info("NEW: " + oper);
			Date fechaToRecalculate = (oper.getFecha().before(oldOper.getFecha()) ? oper.getFecha() : oldOper.getFecha());
			Operacion operCajaBanco = ConfigurationUtil.getOperacionByOperacionDetalle(oper.getId(), em);
			Cuenta oldCajaBancoCuenta = null;
			if (operCajaBanco!=null) {
				oldCajaBancoCuenta = operCajaBanco.getCuenta();
				logger.info("OLD C/B: " + operCajaBanco);
				if (!oper.getTipo().equals(oldOper.getTipo()) && 
						(oper.getTipo().toString().equals(Tipo.EFECTIVO.toString()))) {
							operCajaBanco.setBanco(null);
							oper.setBanco(null);
							operCajaBanco.setCuenta(oper.getCuenta().getCategoriaCuenta().getCajaCuenta());					
				}
				if (oper.getBanco()!=null && (!oper.getBanco().equals(oldOper.getBanco()))){
					operCajaBanco.setBanco(oper.getBanco());
					operCajaBanco.setCuenta(oper.getBanco().getBancoCuenta());				
				} 
				logger.info("NEW C/B: " + operCajaBanco);
				operCajaBanco.setPen(oper.getPen());
				operCajaBanco.setUsd(oper.getUsd());
				operCajaBanco.setDescripcion(oper.getDescripcion());
				operCajaBanco.setBeneficiario(oper.getBeneficiario());
				operCajaBanco.setChequeNumero(oper.getChequeNumero());
				operCajaBanco.setCentroCosto(oper.getCentroCosto());
				operCajaBanco.setLugarGasto(oper.getLugarGasto());
				operCajaBanco.setCuentaContable(oper.getCuentaContable());
				operCajaBanco.setRubroInstitucional(oper.getRubroInstitucional());
				operCajaBanco.setRubroProyecto(oper.getRubroProyecto());
				operCajaBanco.setTipo(oper.getTipo());
				operCajaBanco.setTipoDocumento(oper.getTipoDocumento());
				operCajaBanco.setDni(oper.getDni());
				operCajaBanco.setDocNumero(oper.getDocNumero());
				operCajaBanco.setUsuario(oper.getUsuario());
				operCajaBanco.setFecha(oper.getFecha());
				operCajaBanco.setFechaDeCobro(oper.getFechaDeCobro());
				operCajaBanco.setIsCargo(oper.getIsCargo());
				operCajaBanco.setIsPen(oper.getIsPen());
				operDetalle = operacionEP.getEntityManager().merge(oper);
				operCajaBanco.setOperacionDetalle(operDetalle);
			}			
			//Operacion operDetalle = operacionEP.updateEntity(oper);
			operDetalle = operacionEP.getEntityManager().merge(oper);
			//
			Operacion operEdited = null;
			if (operCajaBanco!=null) operEdited = operacionEP.getEntityManager().merge(operCajaBanco);
			//Operacion operEdited= operacionEP.updateEntity(operCajaBanco);
			
			logger.info("OLD Saldo on cuenta: " + oper.getCuenta().getNumero() + ": " + oper.getCuenta().getPen() + " " + oper.getCuenta().getUsd());
			if (oldCajaBancoCuenta!=null) logger.info("OLD Saldo on oldCuentaC/B: "+ oldCajaBancoCuenta.getNumero() + ": " + oldCajaBancoCuenta.getPen() + " " + oldCajaBancoCuenta.getUsd());
			if (operCajaBanco!=null) logger.info("OLD Saldo on cuentaC/B: "+ operCajaBanco.getCuenta().getNumero() + ": " + operCajaBanco.getCuenta().getPen() + " " + operCajaBanco.getCuenta().getUsd());
			
			// Recalculate the actual account
			OperacionForm.recalculateSaldos(oper.getCuenta().getId(), fechaToRecalculate, operacionEP.getEntityManager());
			// Recalculate the cuenta/banco account
			if (operCajaBanco!=null) OperacionForm.recalculateSaldos(operCajaBanco.getCuenta().getId(), fechaToRecalculate, operacionEP.getEntityManager());
			// If the type was changed the previous Cuenta must be recalculated
			//if (!oper.getTipo().equals(oldOper.getTipo())) 
			if (oldCajaBancoCuenta!=null) OperacionForm.recalculateSaldos(oldCajaBancoCuenta.getId(), fechaToRecalculate, operacionEP.getEntityManager());
			
			if (oldCajaBancoCuenta!=null) oldCajaBancoCuenta = cuentaEP.refreshEntity(oldCajaBancoCuenta);
			operDetalle.setCuenta(cuentaEP.refreshEntity(operDetalle.getCuenta()));
			operDetalle = operacionEP.getEntityManager().merge(operDetalle);
			if (operCajaBanco!=null) {
				operEdited.setCuenta(cuentaEP.refreshEntity(operEdited.getCuenta()));
				operEdited = operacionEP.getEntityManager().merge(operEdited);
			}
			logger.info("Edited OperacionDetalle: " + operDetalle);
			if (operCajaBanco!=null) logger.info("Edited OperacionC/B: " + operEdited);
			logger.info("NEW Saldo on cuenta: " + operDetalle.getCuenta().getNumero() + ": " + operDetalle.getCuenta().getPen() + " " + operDetalle.getCuenta().getUsd());
			if (oldCajaBancoCuenta!=null) logger.info("NEW Saldo on oldCuentaC/B: " + oldCajaBancoCuenta.getNumero() + ": " + oldCajaBancoCuenta.getPen() + " " + oldCajaBancoCuenta.getUsd());
			if (operCajaBanco!=null) logger.info("NEW Saldo on cuentaC/B: " + operEdited.getCuenta().getNumero() + ": " + operEdited.getCuenta().getPen() + " " + operEdited.getCuenta().getUsd());
			
			//trans.rollback();
			trans.commit();
		} catch (Exception e) {
			if ((trans!=null) && (trans.isActive())) {
				logger.info("Trans is active rolling back...");
				trans.rollback();
				logger.info("Rolled back");
				em.close();
			}
			e.printStackTrace();					
			getWindow().showNotification("Problema con la operacion ", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
		}
		VasjaApp vapp = (VasjaApp)getApplication();
		try {
			if (vapp!=null) {
				if (vapp.getOperacionContainer()!=null)
					vapp.getOperacionContainer().refresh();
				if (vapp.getCuentaContainer()!=null) {
					vapp.getCuentaContainer().refresh();
				}
					
			}
		} catch (Exception e) {
			logger.info("Exception refreshing operacion and cuentas");
		}
		if ((opTable!=null) && (opTable instanceof AccountantCajaTable)) {
			((AccountantCajaTable)opTable).redraw(((operDetalle!=null) ? operDetalle.getCuenta() : null), 
					oItem.getBean().getIsPen(), true, false);
			SessionHandler.getVasjaApp().setOperacionContainer(null);
			SessionHandler.getVasjaApp().setCuentaContainer(null);
		}
		else if (opTable!=null) opTable.drawCuentaForm(operDetalle.getCuenta(), true);
		return true;
	}
	
		
		private class OperacionFieldFactory extends VasjaFieldFactory implements AbstractSelect.NewItemHandler {

			final ComboBox tipos = new ComboBox("Tipo");
			final ComboBox bancos = new ComboBox("Banco");
			final ComboBox beneficiarios = new ComboBox("Entregado a/por");
			final ComboBox tiposDocumento = new ComboBox("Tipo de documento");
			final ComboBox centrosCosto = new ComboBox("Centro de costo");
			final ComboBox lugarGasto = new ComboBox("Lugar de gasto");
			final ComboBox cuentaContable = new ComboBox("Cuenta contable");
			final ComboBox rubroInstitucional = new ComboBox("Rubro institucional");
			final ComboBox rubroProyecto = new ComboBox("Rubro proyecto");
			Field chequeNumero = null;
			Field fechaDeCobro = null;
			boolean isPen = true;
			
			public void createTipos(Item itemTipo) {
				String tipoValue = itemTipo.getItemProperty("tipo").getValue().toString();
				logger.info("OperacionFieldFact got tipoVal: " + tipoValue);
				tipos.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
				IndexedContainer c = new IndexedContainer();
				c.addContainerProperty("nombre", String.class, "");
				logger.info("OperacionFieldFact got cuenta: " + opCuenta);
				Item item;
				if (opCuenta==null || (opCuenta!=null && (!opCuenta.getIsBanco()))) {
					item = c.addItem(Tipo.EFECTIVO);
					item.getItemProperty("nombre").setValue(Tipo.EFECTIVO);
				}
				if ((opCuenta==null) || (opCuenta!=null && (!opCuenta.getIsCaja() || opCuenta.getIsBanco()))) {
					item = c.addItem(Tipo.CHEQUE);
					item.getItemProperty("nombre").setValue(Tipo.CHEQUE);
					item = c.addItem(Tipo.TRANSFERENCIA);
					item.getItemProperty("nombre").setValue(Tipo.TRANSFERENCIA);
				}
				tipos.setContainerDataSource(c);
				tipos.setItemCaptionPropertyId("nombre");
				tipos.setRequired(true);
				tipos.setImmediate(true);
				tipos.setNullSelectionAllowed(false);
				tipos.setFilteringMode(ComboBox.FILTERINGMODE_STARTSWITH);
				tipos.addListener(new ValueChangeListener() { 
					@Override
					public void valueChange(ValueChangeEvent event) {
						boolean isTransf = event.getProperty() != null
								&& !event.getProperty().equals("")
								&& event.getProperty().toString()
										.equals(Tipo.TRANSFERENCIA.toString());
						boolean isCheque = event.getProperty() != null
								&& !event.getProperty().equals("")
								&& event.getProperty().toString()
										.equals(Tipo.CHEQUE.toString());

						if (isCheque || isTransf) {						
							Field f = bancos;
							f.setRequired(true);
							f.setRequiredError("Por favor rellena banco");
							f.setVisible(true);
							f.setEnabled(true);
							if (chequeNumero!=null) chequeNumero.setVisible(true);
							if (fechaDeCobro!=null) {
								if (isCheque) {
									fechaDeCobro.setVisible(true);
									fechaDeCobro.setValue(null);
								}
								else fechaDeCobro.setVisible(false);
							}
							if (chequeNumero!=null) { 
								if (isTransf)
									chequeNumero.setCaption("Transferencia Numero");
								else
									chequeNumero.setCaption("Cheque Numero");
							}										
						} else {
							if (chequeNumero!=null) 
								chequeNumero.setVisible(false);
							if (bancos!=null)
								bancos.setVisible(false);
							if (fechaDeCobro!=null)
								fechaDeCobro.setVisible(false);
						}
					}
				});				
			}
			
			public OperacionFieldFactory() {
				DataFilterUtil.bindComboBox(bancos, "nombre",
						"Seleccione banco", Banco.class, null);
	            bancos.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
				bancos.setVisible(false);
				bancos.setImmediate(true);
				bancos.setEnabled(true);
				
				DataFilterUtil.bindComboBox(beneficiarios, "nombre",
						"Seleccione beneficiario", Beneficiario.class, null);
				beneficiarios.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
				beneficiarios.setNewItemsAllowed(true);
				beneficiarios.setNewItemHandler(this);
				beneficiarios.setImmediate(true);
				beneficiarios.setRequired(true);
				beneficiarios.setRequiredError("Por favor rellena el beneficiario");
				
				DataFilterUtil.bindComboBox(tiposDocumento, "nombre",
						"Seleccione tipo de documento", TipoDocumento.class, null);
				tiposDocumento.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
				
				DataFilterUtil.bindCentroCostoBox(centrosCosto);
				centrosCosto.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));

				DataFilterUtil.bindLugarGastoBox(lugarGasto);
				lugarGasto.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			
				DataFilterUtil.bindCuentaContableBox(cuentaContable);
				cuentaContable.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
				
				DataFilterUtil.bindRubroProyectoBox(rubroProyecto, null, null, null, null);
				rubroProyecto.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));

				DataFilterUtil.bindRubroInstitucionalBox(rubroInstitucional, null, null, null);
				rubroInstitucional.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			}
		
			public void addNewItem(String newItemCaption) {
		        if (!beneficiarios.containsId(newItemCaption)) {
		        	if (ConfigurationUtil.getBeneficiarioPorNombre(newItemCaption)==null) {
			        	Beneficiario b = new Beneficiario();
			        	b.setNombre(newItemCaption);
			        	ConfigurationUtil.getBeneficiarioEP().addEntity(b);
			        	DataFilterUtil.bindComboBox(beneficiarios, "nombre",
								"Seleccione firma", Beneficiario.class, null);	        	
			        }
		            beneficiarios.setValue(ConfigurationUtil.getBeneficiarioPorNombre(newItemCaption));
		        }
		    }
		
			@Override
			public Field createField(Item item, Object propertyId,
					Component uiContext) {
				Field f;
				if ("fecha".equals(propertyId)) {
					f = createDateField(propertyId);
					((DateField)f).setResolution(DateField.RESOLUTION_DAY);
					f.setEnabled(true);
				} else if ("fechaDeCobro".equals(propertyId)) {
					f = createDateField(propertyId);
					((DateField)f).setResolution(DateField.RESOLUTION_DAY);
					f.setEnabled(true);
					if ((item.getItemProperty("tipo")!=null) && (item.getItemProperty("tipo").getValue().equals(Tipo.CHEQUE.toString())))
						f.setVisible(true);
					else
						f.setVisible(false);
				} else if ("descripcion".equals(propertyId)) {
					f = createTextArea(propertyId);
					f.setEnabled(true);
				} else if ("docNumero".equals(propertyId)) {
					f = super.createField(item, propertyId, uiContext);
					f.setEnabled(true);
				} else {
					f = super.createField(item, propertyId, uiContext);
					f.setEnabled(true);
				}
				if ("beneficiario".equals(propertyId)) {
					return beneficiarios;
				} else if ("tipoDocumento".equals(propertyId)) {
					return tiposDocumento;							
				} else if ("centroCosto".equals(propertyId)) {
					return centrosCosto;				
				} else if ("lugarGasto".equals(propertyId)) {
					return lugarGasto;
				} 
				else if ("cuentaContable".equals(propertyId)) {
					return cuentaContable;
				}
				else if ("rubroProyecto".equals(propertyId)) {
					return rubroProyecto;
				}
				else if ("rubroInstitucional".equals(propertyId)) {
					return rubroInstitucional;
				}
				else if ("banco".equals(propertyId)) {
					if (item.getItemProperty("banco").getValue()!=null && 
							!item.getItemProperty("tipo").toString().equals(Tipo.EFECTIVO)) {
						if (item.getItemProperty("pen").getValue()!=null && 
								(!item.getItemProperty("pen").getValue().toString().equals("0.00"))) {
							DataFilterUtil.bindComboBox(bancos, "nombre",
									"Seleccione banco", Banco.class, Filters.eq("moneda", Banco.Moneda.PEN));
							isPen = true;
						}
						else {
							DataFilterUtil.bindComboBox(bancos, "nombre",
									"Seleccione banco", Banco.class, Filters.eq("moneda", Banco.Moneda.USD));
							isPen = false;
						}
						bancos.setVisible(true);
					}
					else 
						bancos.setVisible(false);
					return bancos;
				} else if ("chequeNumero".equals(propertyId)) {
					chequeNumero = super.createField(item, propertyId, uiContext);
					if ((Tipo.TRANSFERENCIA.toString().equals(item
								.getItemProperty("tipo").getValue().toString()))) {
						chequeNumero.setCaption("Transferencia numero");
					}
					if (Tipo.EFECTIVO.toString().equals(item
								.getItemProperty("tipo").getValue().toString())) {
						chequeNumero.setVisible(false);
						chequeNumero.setEnabled(false);
						bancos.setVisible(false);
						bancos.setEnabled(false);
					} else {
						chequeNumero.setVisible(true);	
						chequeNumero.setEnabled(true);
						bancos.setVisible(true);
						bancos.setEnabled(true);
					}
					return chequeNumero;
				} else if ("tipo".equals(propertyId)) {
					createTipos(item);
					logger.info("OperacionFieldFact tipos value: " + tipos.getValue());
					return tipos;
				} else if ("firma".equals(propertyId)) 
					f.setCaption("Nombre");
				else if ("pen".equals(propertyId) || "usd".equals(propertyId)) {
					//String regexp = "^[+-]?[0-9]{3}+\\.?([0-9]{2})?";		
					String regexp = "^[+-]?((\\d+)|(\\d{1,3})(\\,\\d{3})*)(\\.\\d{0,2})?$";
					CSValidatedTextField tf = new CSValidatedTextField(
							DefaultFieldFactory
									.createCaptionByPropertyId(propertyId));
					tf.setRequired(true);
					tf.setRegExp(regexp);
					tf.setErrorMessage("El monton no esta correcto");
					tf.setImmediate(true);
					f = tf;
					f.setRequired(true);
					f.setRequiredError("El monton no esta correcto");
					f.addValidator(new RegexpValidator(regexp,
							"El monton no esta correcto"));
					if (opCuenta!=null && !ConfigurationUtil.is("ALLOW_OVERDRAW") 
							&& (Boolean) item.getItemProperty("isCargo").getValue()) {
						BigDecimal maxCargo = ("pen".equals(propertyId) ? opCuenta.getPen()
								: opCuenta.getUsd());
						logger.info("MaxCargo: " + maxCargo.toString());
						if ((Boolean)item.getItemProperty("isCorrection").getValue()) {
							Operacion oldOper = (Operacion)item.getItemProperty("operacionCorrected").getValue();
							logger.info("OldOper: " + oldOper);
							BigDecimal oldAmount = (oldOper!=null ? 
									("pen".equals(propertyId) ? oldOper.getPen() : oldOper.getUsd()) : null);
							logger.info("OldAmount: " + oldAmount);							
							maxCargo = maxCargo.add(oldAmount);						
						} 
						f.addValidator(new AmountValidator(
								"El monton de cargo no puede exceder el monton disponible",
								null, maxCargo));
					}
				}
				if ("pen".equals(propertyId)) {
					TextField tf = (TextField) f;
					tf.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
					tf.addListener(new FocusListener() {
						@Override
						public void focus(FocusEvent event) {
							if (operacionEditForm.getField("usd")!=null) 
								operacionEditForm.getField("usd").setValue("0.00");
							operacionEditForm.getField("pen").setEnabled(true);
							// Filter bancos for only PEN 
							if (!isPen) {
								DataFilterUtil.bindComboBox(bancos, "nombre",
										"Seleccione banco", Banco.class, Filters.eq("moneda", Banco.Moneda.PEN));
								isPen = true;
							}
						}
					});
				}
				if ("usd".equals(propertyId)) {
					TextField tf = (TextField) f;
					tf.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
					tf.addListener(new FocusListener() {
						@Override
						public void focus(FocusEvent event) {
							if (operacionEditForm.getField("pen")!=null) 
								operacionEditForm.getField("pen").setValue("0.00");
							// Filter bancos for only USD
							if (isPen) {
								DataFilterUtil.bindComboBox(bancos, "nombre",
										"Seleccione banco", Banco.class, Filters.eq("moneda", Banco.Moneda.USD));
								isPen = false;
							}
						}
					});
				}
				return f;
			}
		}
}

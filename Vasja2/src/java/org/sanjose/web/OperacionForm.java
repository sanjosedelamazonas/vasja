package org.sanjose.web;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.sanjose.model.Banco;
import org.sanjose.model.Beneficiario;
import org.sanjose.model.CentroCosto;
import org.sanjose.model.Cuenta;
import org.sanjose.model.CuentaContable;
import org.sanjose.model.LugarGasto;
import org.sanjose.model.Operacion;
import org.sanjose.model.Operacion.Tipo;
import org.sanjose.model.RubroInstitucional;
import org.sanjose.model.RubroProyecto;
import org.sanjose.model.Saldo;
import org.sanjose.model.TipoDocumento;
import org.sanjose.model.Usuario;
import org.sanjose.util.AmountValidator;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.util.DataFilterUtil;
import org.sanjose.util.DateValidator;
import org.sanjose.util.DecimalPropertyFormatter;
import org.sanjose.util.VasjaFieldFactory;
import org.sanjose.web.accountant.AccountantCajaTable;
import org.sanjose.web.helper.IOperacionTable;
import org.sanjose.web.helper.ReportHelper;
import org.sanjose.web.helper.SessionHandler;
import org.vaadin.csvalidation.CSValidatedTextField;
import org.vaadin.delayedbutton.DelayedButton;

import com.vaadin.addon.jpacontainer.filter.Filters;
import com.vaadin.addon.jpacontainer.provider.BatchableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
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
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class OperacionForm extends VerticalLayout {

	Operacion operacion = null;
	Cuenta cuenta;
	Set<Cuenta> cuentasSet = null;
	IOperacionTable operacionTable;
	CuentaTable cuentaTable = null;
	Form operacionForm;
	Label warning = null;
	boolean isNew = true;
	SimpleDateFormat sdf = new SimpleDateFormat(ConfigurationUtil.get("DEFAULT_DATE_FORMAT"), ConfigurationUtil.LOCALE);
	private ValueChangeListener tiposChangeValueListener = null;

	static final Logger logger = Logger
			.getLogger(OperacionForm.class.getName());

	public OperacionForm(Cuenta cuenta, Set<Cuenta> cuentasSet,  IOperacionTable operacionTable, CuentaTable ct,
			boolean isCargo) {
		this(cuenta, cuentasSet, operacionTable, ct, isCargo, true);
	}
	
	
	public OperacionForm(Cuenta cuenta, Set<Cuenta> cuentasSet,  IOperacionTable operacionTable, CuentaTable ct,
			boolean isCargo, boolean isPen) {
		
		operacion = new Operacion();
		
		operacion.setIsPen(isPen);
		logger.info("Got Pen in OpForm: " + isPen);
		
		this.cuenta = cuenta;
		this.cuentasSet = cuentasSet;
		this.operacionTable = operacionTable;
		this.cuentaTable = ct;
		operacion.setIsCargo(isCargo);
		
		if (cuenta!=null) {
			if (cuenta.getIsCaja()) 
				setCaption((operacion.getIsCorrection() ? "Correccion de " : "")		
						+ (isCargo ? "Egreso" : "Ingreso") + 
						(cuenta.getIsBanco() ? " a cuenta de banco: " : " a cuenta de caja : " )
						+cuenta.getNombre());
			else
				setCaption((operacion.getIsCorrection() ? "Correccion de " : "")		
				+ (isCargo ? "Cargo" : "Abono") + " de cuenta numero: "
				+ cuenta.getNumero() + " " + cuenta.getNombre());
			
		}
		else setCaption("Operacion de Multi " + 
				(isCargo ? "Cargo" : "Abono") + " para " + cuentasSet.size() + " cuentas");

		operacion.setUsuario(SessionHandler.get());
		if (cuenta!=null) operacion.setCuenta(cuenta);
		buildForm(new BeanItem<Operacion>(operacion));
	}

	public OperacionForm(Operacion op) {
		// Only preview of existing operacion
		if (cuenta!=null) setCaption((operacion.getIsCorrection() ? "Correccion de " : "")
				+ (op.getIsCargo() ? "Cargo" : "Abono") + " de cuenta numero: "
				+ op.getCuenta().getNumero() + " " + op.getCuenta().getNombre());		
		buildConfirmationForm(new BeanItem<Operacion>(op), true);
	}

	
	// Correccion
	public OperacionForm(Cuenta cuenta, IOperacionTable operacionTable,
			boolean isCargo, Operacion op) {
		operacion = new Operacion();
		operacion.setIsCorrection(true);
		operacion.setOperacionCorrected(op);
		
		// Set default date for correccion
		
		if (ConfigurationUtil.getUltimoMesCerrado().before(op.getFecha()))
			operacion.setFecha(ConfigurationUtil.dateAddSeconds(op.getFecha(), 1));
		else
			operacion.setFecha(ConfigurationUtil.dateAddSeconds(ConfigurationUtil.getUltimoMesCerrado(), 1));

		operacion.setDescripcion("CORRECCION operacion: "
				+ op.getId()
				+ " fecha: "
				+ new SimpleDateFormat(ConfigurationUtil.get("DEFAULT_DATE_FORMAT"))
						.format(op.getFecha()));
		operacion.setChequeNumero(op.getChequeNumero());
		operacion.setBanco(op.getBanco());
		operacion.setDni(op.getDni());
		operacion.setFirma(op.getFirma());
		operacion.setBeneficiario(op.getBeneficiario());
		operacion.setTipoDocumento(op.getTipoDocumento());
		operacion.setDocNumero(op.getDocNumero());
	//	operacion.setIsPen(op.getIsPen());
		operacion.setPen((isCargo) ? op.getPen().multiply(new BigDecimal(-1)) : op.getPen());
		operacion.setTipo(op.getTipo());
		operacion.setUsd((isCargo) ? op.getUsd().multiply(new BigDecimal(-1)) : op.getUsd());
		this.cuenta = cuenta;
		this.operacionTable = operacionTable;
		operacion.setIsCargo(isCargo);
		setCaption((operacion.getIsCorrection() ? "Correccion de " : "")
				+ (isCargo ? "Cargo" : "Abono") + " de cuenta numero: "
				+ cuenta.getNumero() + " " + cuenta.getNombre());
		
		operacion.setUsuario(SessionHandler.get());
		operacion.setCuenta(cuenta);
		buildForm(new BeanItem<Operacion>(operacion));
	}

	private void buildForm(final BeanItem<Operacion> oItem) {
		if (warning!=null) removeComponent(warning);
		operacionForm = new Form();

		operacionForm.setWriteThrough(false); // we want explicit 'apply'
		operacionForm.setInvalidCommitted(false); // no invalid values in
													// datamodel
		operacionForm.setFormFieldFactory(new OperacionFieldFactory());
		operacionForm.setItemDataSource(oItem); 

		if (cuenta!=null && cuenta.getIsCaja()) 
			operacionForm.setVisibleItemProperties(Arrays.asList(new String[] 
					{
					// This is Caja - it must be shown  					 
					 (operacion.getIsPen() ? "pen" : "usd"), "descripcion", "tipo", "banco", "chequeNumero", "centroCosto", 
					 "lugarGasto", "beneficiario","cuentaContable","rubroInstitucional","rubroProyecto", /*"tipoDocumento", "docNumero", */ 
					 "fecha", "fechaDeCobro"
					}));
		else 
		// Determines which properties are shown, and in which order:
			operacionForm.setVisibleItemProperties(Arrays.asList(new String[] {
				/*ab "isPen", */ "pen", "usd", "descripcion", "tipo", "banco", "chequeNumero", "centroCosto", 
				"lugarGasto", "beneficiario","cuentaContable","rubroInstitucional","rubroProyecto", /*"tipoDocumento", "docNumero", */ 
				"fecha", "fechaDeCobro"
				}));

		// Add form to layout
		addComponent(operacionForm);
		setComponentAlignment(operacionForm, Alignment.MIDDLE_CENTER);

		Field pen = operacionForm.getField("pen");
		if (pen != null)
			pen.setPropertyDataSource(new DecimalPropertyFormatter(pen
					.getPropertyDataSource(), ConfigurationUtil.get("DECIMAL_FORMAT")));
		Field usd = operacionForm.getField("usd");
		if (usd != null)
			usd.setPropertyDataSource(new DecimalPropertyFormatter(usd
					.getPropertyDataSource(), ConfigurationUtil.get("DECIMAL_FORMAT")));
		
		// For Banco Cuenta
		if (cuenta!=null && cuenta.getIsBanco()) {
			Banco selBanco = ConfigurationUtil.getBancoPorCuenta(cuenta.getId(), ConfigurationUtil.getEntityManager());
			operacionForm.getField("tipo").setValue(Tipo.TRANSFERENCIA.toString());
			operacionForm.getField("banco").setVisible(true);
			operacionForm.getField("banco").setValue(selBanco);
			operacionForm.getField("banco").setEnabled(true);
		}
		
		if ((!SessionHandler.isContador())&&(!SessionHandler.isAdmin()))
		{
			operacionForm.getField("tipo").setValue(Tipo.EFECTIVO.toString());
		}
		else
			operacionForm.getField("tipo").setValue(Tipo.TRANSFERENCIA.toString());

		// The cancel / apply buttons
		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);

		Button apply = new Button("Guardar", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				try {
					//logger.info("commiting operacionForm " + operacionForm);
					operacionForm.getField("tipo").removeListener(tiposChangeValueListener);
					operacionForm.commit();
					logger.info("Got oper" + oItem.getBean());
					removeComponent(operacionForm);
					Operacion oper = (Operacion) oItem.getBean();

					if (oper.getPen().compareTo(new BigDecimal(0)) > 0)
						oper.setIsPen(true);
					else
						oper.setIsPen(false);
					buildConfirmationForm(oItem, false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		apply.setIcon(new ThemeResource("../icons/forward-icon.png"));
		buttons.addComponent(apply);
		Button close = new Button("Anular", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				try {
					operacionForm.discard();
					removeComponent(operacionForm);
					getWindow().getParent().removeWindow(getWindow());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		close.setIcon(new ThemeResource("../icons/go-back-icon.png"));
		buttons.addComponent(close);
		operacionForm.getFooter().addComponent(buttons);
		operacionForm.getFooter().setMargin(false, false, true, true);

	}

	private void buildConfirmationForm(final BeanItem<Operacion> oItem, boolean isPreview) {

		if (warning!=null) removeComponent(warning);
		Operacion op = (Operacion) oItem.getBean();
		final Form operacionConfirmForm = new Form();
		
		if (isPreview) {
			Label l = new Label(
					(op.getIsCargo() ? "CARGO" : "ABONO") + (op.getIsCorrection() ? " - CORRECCION" : "")
					);
			l.setStyleName(Reindeer.LABEL_H2);
			addComponent(l);
		}
		
		// Warning if OVERDRAW
		if (cuenta!=null && op.getIsCargo()) {
			boolean isOverDraw = (!op.getIsCorrection() ?  
					((op.getPen().compareTo(cuenta.getPen())>0) || (op.getUsd().compareTo(cuenta.getUsd())>0)) : false);
			boolean isCorrOverDraw = (op.getIsCorrection() ? 
					((op.getPen().add(op.getOperacionCorrected().getPen()).compareTo(cuenta.getPen())>0) 
					|| (op.getUsd().add(op.getOperacionCorrected().getUsd()).compareTo(cuenta.getUsd())>0)) : false);
			if ((isOverDraw) || (isCorrOverDraw)) {
				warning = new Label("¡El monton del cargo excede el monton disponible!");
				warning.setStyleName(ConfigurationUtil.CSS_RED);
				addComponent(warning);
			}
		}
		operacionConfirmForm.setWriteThrough(false); // we want explicit 'apply'
		operacionConfirmForm.setInvalidCommitted(false); // no invalid values in
		operacionConfirmForm.setItemDataSource(oItem); 
		operacionConfirmForm
				.setFormFieldFactory(new OperacionConfirmationFieldFactory());

		String[] cols;

		if (op.getTipo().equals(Tipo.EFECTIVO.toString())) {
			if (op.getIsPen())
				cols = new String[] { "descripcion", "fecha", "fechaDeCobro", "centroCosto", 
					"lugarGasto",  "beneficiario", "cuentaContable","rubroInstitucional","rubroProyecto",//"tipoDocumento", "docNumero",
						"tipo", "pen" };
			else
				cols = new String[] { "descripcion", "fecha", "fechaDeCobro", "centroCosto", 
					"lugarGasto", "beneficiario","cuentaContable","rubroInstitucional","rubroProyecto", //"tipoDocumento", "docNumero",
						"tipo", "usd" };
		} else {
			if (op.getIsPen())
				cols = new String[] { "descripcion", "fecha", "fechaDeCobro", "centroCosto",
					"lugarGasto", "beneficiario", "cuentaContable","rubroInstitucional","rubroProyecto",//"tipoDocumento", "docNumero",
						"tipo", "banco", "chequeNumero", "pen" };
			else
				cols = new String[] { "descripcion", "fecha", "fechaDeCobro", "centroCosto", 
					"lugarGasto", "beneficiario", "cuentaContable","rubroInstitucional","rubroProyecto",//"tipoDocumento", "docNumero",
						"tipo", "banco", "chequeNumero", "usd" };
		}
		operacionConfirmForm.setVisibleItemProperties(Arrays.asList(cols));

		// Add form to layout
		addComponent(operacionConfirmForm);

		Field pen = operacionConfirmForm.getField("pen");
		if (pen != null)
			pen.setPropertyDataSource(new DecimalPropertyFormatter(pen
					.getPropertyDataSource(), ConfigurationUtil.get("DECIMAL_FORMAT")));
		Field usd = operacionConfirmForm.getField("usd");
		if (usd != null)
			usd.setPropertyDataSource(new DecimalPropertyFormatter(usd
					.getPropertyDataSource(), ConfigurationUtil.get("DECIMAL_FORMAT")));

		// The cancel / apply buttons
		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		Button apply = new DelayedButton("Correcto", 1);
		apply.setIcon(new ThemeResource("../icons/forward-icon.png"));
		apply.addListener(new Button.ClickListener() {
			boolean isProcessing = false;
			// Actually write the operation to the database
			public void buttonClick(ClickEvent event) {
				if (!isProcessing) {
					isProcessing = saveOperacion(oItem, operacionConfirmForm);
				}
			}
		});
		Button corrige = new Button("Atras", new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				removeComponent(operacionConfirmForm);
				buildForm(oItem);
			}
		});
		corrige.setIcon(new ThemeResource("../icons/go-back-icon.png"));
		Button close = new Button("Cerrar", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				try {
					removeComponent(operacionConfirmForm);
					getWindow().getParent().removeWindow(getWindow());
				} catch (Exception e) {
				}
			}
		});		
		close.setIcon(new ThemeResource("../icons/back-icon.png"));
		Button imprimir = new Button("Imprimir comprobante", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				try {
					JasperPrint jrPrint = ReportHelper.printComprobante(oItem.getBean(), getWindow());
					boolean isPrinted = false;
					if (((VasjaApp)getApplication()).getPrintHelper()!=null)
						isPrinted = ((VasjaApp)getApplication()).getPrintHelper().print(jrPrint, true);
					if (!isPrinted)
						throw new JRException("Problema al consequir un servicio de imprimir");
				} catch (JRException e) {
					e.printStackTrace();
					getWindow().showNotification("Problema al imprimir el comprobante ID: " + oItem.getBean().getId() + " " + e.getMessage());
				}
			}
		});
		if (!isPreview) {
			buttons.addComponent(corrige);
			buttons.addComponent(apply);
		} else {
			buttons.addComponent(close);
			buttons.addComponent(imprimir);
		}		
		
		operacionConfirmForm.getFooter().addComponent(buttons);
		operacionConfirmForm.getFooter().setMargin(false, false, true, true);
	}

	private boolean saveOperacion(BeanItem<Operacion> oItem, Form operacionConfirmForm) {
		// Actually write the operation to the database
		boolean isMulti = (cuentasSet!=null);
		EntityTransaction trans = null;
		EntityManager em = null;
		try {
			em = ConfigurationUtil.getNewEntityManager();
			MutableLocalEntityProvider<Operacion> operacionEP = new BatchableLocalEntityProvider<Operacion>(
					Operacion.class, em);
			operacionEP.setTransactionsHandledByProvider(false);
			MutableLocalEntityProvider<Cuenta> cuentaEP =  new BatchableLocalEntityProvider<Cuenta>(
					Cuenta.class, em);
			MutableLocalEntityProvider<Usuario> usuarioEP =  new BatchableLocalEntityProvider<Usuario>(
					Usuario.class, em);
			cuentaEP.setTransactionsHandledByProvider(false);
			MutableLocalEntityProvider<Saldo> saldoEP = new BatchableLocalEntityProvider<Saldo>(
					Saldo.class, em);
			saldoEP.setTransactionsHandledByProvider(false);
				
			String logCaja = null;
			
			trans = em.getTransaction();
			trans.begin();
			
			if (!isMulti) {
				cuentasSet = new HashSet<Cuenta>();
				cuentasSet.add(cuenta);
			}
			Operacion oper = null;
			for (Cuenta cn : cuentasSet) {
				oper = (isMulti ? ((Operacion)oItem.getBean()).clone() : (Operacion)oItem.getBean());
				oper.setUsuario(em.find(Usuario.class, (SessionHandler.get().getId())));
				if (oper.getFechaDeCobro()==null && (!oper.getTipo().equals(Tipo.CHEQUE.toString()))) oper.setFechaDeCobro(oper.getFecha());
				cn = cuentaEP.getEntityManager().find(Cuenta.class, cn.getId(), LockModeType.PESSIMISTIC_WRITE);
				assert cn!=null;
				
				if (oper.getIsCargo()) {
					oper.setPen(oper.getPen().multiply(new BigDecimal(-1)).setScale(2));
					oper.setUsd(oper.getUsd().multiply(new BigDecimal(-1)).setScale(2));
				}
	
				Boolean isEfectivo = oper.getTipo().equals(Tipo.EFECTIVO.toString()); 
				//logger.info("Got operacion to save: " + oper);
	
				oper.setIsPen((oper.getPen().compareTo(new BigDecimal(0)) > 0 
						|| oper.getPen().compareTo(new BigDecimal(0))< 0) ? true : false);
	
				Boolean isCorEfectivo = oper.getIsCorrection() ? 
						oper.getOperacionCorrected().getTipo().equals(Tipo.EFECTIVO.toString()) : isEfectivo;
				// In case of correction and same type (EFECTIVO/BANCO) or is of type Caja/Banco
				// Only add the difference
				if (oper.getIsCorrection() && (isEfectivo.equals(isCorEfectivo) || cn.getIsCaja())) {
					Operacion oldOperacion = oper.getOperacionCorrected();
					
					BigDecimal diffPen = oper.getPen().subtract(
							oldOperacion.getPen());
					oper.setPen(diffPen);
					BigDecimal diffUsd = oper.getUsd().subtract(
							oldOperacion.getUsd());
					oper.setUsd(diffUsd);
				} else if (oper.getIsCorrection() && (!cn.getIsCaja()) && ((!isEfectivo.equals(isCorEfectivo)) || 
						((!isEfectivo) && (!isCorEfectivo) && (!oper.getBanco().equals(oper.getOperacionCorrected().getBanco()))))) {
					// If isCorrection and there are differences of types we have to move between Efectivo and Bancario
					// make a reverse operation
					Operacion op = oper.getOperacionCorrected().clone();
					op.setIsCorrection(true);
					op.setDescripcion(oper.getDescripcion());
					op.setOperacionCorrected(oper.getOperacionCorrected());
					op.setBeneficiario(oper.getBeneficiario());
					op.setCentroCosto(oper.getCentroCosto());
					op.setCuentaContable(oper.getCuentaContable());
					op.setRubroInstitucional(oper.getRubroInstitucional());
					op.setRubroProyecto(oper.getRubroProyecto());
					op.setLugarGasto(oper.getLugarGasto());
					op.setBanco(oper.getBanco());
					op.setChequeNumero(oper.getChequeNumero());
					op.setDocNumero(oper.getDocNumero());
					op.setDni(oper.getDni());
					op.setTipoDocumento(oper.getTipoDocumento());
					if (ConfigurationUtil.getUltimoMesCerrado().before(oper.getOperacionCorrected().getFecha()))
						op.setFecha(ConfigurationUtil.dateAddSeconds(oper.getOperacionCorrected().getFecha(), 1));
					else
						op.setFecha(ConfigurationUtil.dateAddSeconds(ConfigurationUtil.getUltimoMesCerrado(), 1));
					
					op.setPen(oper.getOperacionCorrected().getPen().multiply(new BigDecimal(-1)));
					op.setUsd(oper.getOperacionCorrected().getUsd().multiply(new BigDecimal(-1)));
					
					cn.setPen(cn.getPen().add(op.getPen()));
					cn.setUsd(cn.getUsd().add(op.getUsd()));				
					cn = cuentaEP.updateEntity(cn);
					op.setSaldoPen(cn.getPen());
					op.setSaldoUsd(cn.getUsd());		
					Object operAdded = operacionEP.addEntity(op);
					Object newOperacionId = ((Operacion)operAdded).getId();
					op.setId((Long)newOperacionId);
					
					// NEW CODE REVERSE the Caja/Banco
					// NEW CODE - added operacion to caja Cuenta
					logger.info(processCajaBancoOperacion(cn, (Operacion)operAdded, em, cuentaEP, operacionEP));
					if (newOperacionId!=null) recalculateSaldos((Operacion)operAdded, em);
				}
				String logCuenta = "Cuenta numero: " + cn.getNumero()
						+ " , antes PEN: " + cn.getPen()
						+ ", USD: " + cn.getUsd();
				// Set the BancoCuenta beacause otherwise it gets lost
				if (oper.getBanco()!=null) {
					Cuenta bancoCuenta = em.find(Cuenta.class, oper.getBanco().getBancoCuenta().getId());
					Banco banco = em.find(Banco.class, oper.getBanco().getId());
					banco.setBancoCuenta(bancoCuenta);
					oper.setBanco(banco);
				}
				Object operAdded = operacionEP.addEntity(oper);
				Object newOperacionId = ((Operacion)operAdded).getId();
				cn = recalculateSaldos((Operacion)operAdded, em);
				
				
				logger.info("new oper id: " + newOperacionId);
				oper.setId((Long) newOperacionId);
				if (operAdded!=null) 
					logger.info("Added new operacion: " + operAdded.toString());
				logger.info("Updated cn: " + cn.toString());
				// PROCESS CAJA/BANCO 
				if (!cn.getIsCaja()) {
					logger.info("OPERACION en CAJA/BANCO processing normally for non Caja");			
					logger.info(processCajaBancoOperacion(cn, (Operacion)operAdded, em, cuentaEP, operacionEP));							
				}
				logger.info("Nueva operacion "
						+ (oper.getIsCorrection() ? " CORRECCION" : "")
						+ ": " + oper);
				logCuenta += ", ahora PEN: " + cn.getPen() + ", USD: "
						+ cn.getUsd();
				logger.info(logCuenta);				
				cuenta = cn;
			}
			if (trans!=null && trans.isActive()) {
				logger.info("trans active commiting...");
				trans.commit();
				logger.info("commit done");
				em.close();
			}
			logger.info("Cuenta for redrawing: " + cuenta);
			removeComponent(operacionConfirmForm);
			if (operacionTable instanceof AccountantCajaTable) 
				((AccountantCajaTable)operacionTable).redraw(cuenta, oper.getIsPen(), true, false);
			else if (operacionTable!=null) operacionTable.drawCuentaForm(cuenta, true);
			if (cuentaTable!=null) cuentaTable.redraw();
			Window par = getWindow().getParent();
			par.removeWindow(getWindow());
			if (!isMulti && ConfigurationUtil.is("REPORTS_COMPROBANTE_OPEN")) ReportHelper.generateComprobante(oper, par);
			if (!isMulti && ConfigurationUtil.is("REPORTS_COMPROBANTE_PRINT")&&(!SessionHandler.isAdmin())&&(!SessionHandler.isContador())) {
				JasperPrint jrPrint = ReportHelper.printComprobante(oper, par);
				boolean isPrinted = false;
				if (((VasjaApp)par.getApplication()).getPrintHelper()!=null)
					isPrinted = ((VasjaApp)par.getApplication()).getPrintHelper().print(jrPrint, true);
				if (!isPrinted)
					throw new Exception("No se podia imprimir comprobante");							
			}
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
		return true;
	}
	
	public static String processCajaBancoOperacion(Cuenta cn, Operacion oper, EntityManager em, 
			MutableLocalEntityProvider<Cuenta> cuentaEP, MutableLocalEntityProvider<Operacion> operacionEP) throws Exception {
		//logger.info("OPERACION en CAJA/BANCO processing normally for non Caja");
		String logCaja = "";
		Boolean isEfectivo = oper.getTipo().toString().equals(Tipo.EFECTIVO.toString()); 
		Cuenta cajaCuenta;
		if (isEfectivo) {
			if (cn.getCategoriaCuenta().getCajaCuenta()==null) throw new Exception("La categoria de esta Cuenta no tiene una cuenta de CAJA!!!"); 
			cajaCuenta = cuentaEP.getEntityManager().find(Cuenta.class, cn.getCategoriaCuenta().getCajaCuenta().getId(), LockModeType.PESSIMISTIC_WRITE);
		} else {
			if (oper.getBanco().getBancoCuenta()==null) throw new Exception("El banco seleccionado no tiene una cuenta!!!\nBanco seleccionado: " + oper.getBanco());
			cajaCuenta = cuentaEP.getEntityManager().find(Cuenta.class, oper.getBanco().getBancoCuenta().getId(), LockModeType.PESSIMISTIC_WRITE);
		}
		logCaja = " Caja antes, PEN: " + cajaCuenta.getPen() + " USD: " + cajaCuenta.getUsd();
		cajaCuenta.setPen(cajaCuenta.getPen().add(oper.getPen()));
		cajaCuenta.setUsd(cajaCuenta.getUsd().add(oper.getUsd()));
		cajaCuenta = cuentaEP.updateEntity(cajaCuenta);
		logCaja += ", CAJA ahora PEN: " + cajaCuenta.getPen() + ", USD: "
				+ cajaCuenta.getUsd();					
		// NEW CODE - added operacion to caja Cuenta
		Operacion cajaOperacion = oper.giveCajaOperacion(cajaCuenta);
		Object cajaOperacionAdded = operacionEP.addEntity(cajaOperacion);
		Object newCajaOperacionId = ((Operacion)cajaOperacionAdded).getId();
		recalculateSaldos((Operacion)cajaOperacionAdded, em);
		logCaja += "new caja oper id: " + newCajaOperacionId;
		logger.info(logCaja);
		return logCaja;
	}
	
	private class OperacionFieldFactory extends VasjaFieldFactory implements AbstractSelect.NewItemHandler {

		final ComboBox tipos = new ComboBox("Tipo");
		final ComboBox bancos = new ComboBox("Banco");
		final ComboBox beneficiarios = new ComboBox("Entregado a/por");
		final ComboBox tiposDocumento = new ComboBox("Tipo de documento");
		final ComboBox centrosCosto = new ComboBox("Centro de costo");
		final ComboBox lugarGasto = new ComboBox("Lugar de gasto");
		final ComboBox cuentaContable = new ComboBox("Cuenta contable");
		final ComboBox rubroProyecto = new ComboBox("Rubro presupuestal del proyecto");
		final ComboBox rubroInstitucional = new ComboBox("Rubro institucional");

		public OperacionFieldFactory() {
			
			tipos.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			IndexedContainer c = new IndexedContainer();
			c.addContainerProperty("nombre", String.class, "");
			logger.info("OperacionFieldFact got cuenta: " + cuenta);
			Item item;
			if (cuenta==null || (cuenta!=null && (!cuenta.getIsBanco()))) {
				item = c.addItem(Tipo.EFECTIVO);
				item.getItemProperty("nombre").setValue(Tipo.EFECTIVO);
			}
			if ((cuenta==null) || (cuenta!=null && (!cuenta.getIsCaja() || cuenta.getIsBanco()))) {
				item = c.addItem(Tipo.CHEQUE);
				item.getItemProperty("nombre").setValue(Tipo.CHEQUE);
				item = c.addItem(Tipo.TRANSFERENCIA);
				item.getItemProperty("nombre").setValue(Tipo.TRANSFERENCIA);
			}			
			tipos.setContainerDataSource(c);
			tipos.setItemCaptionPropertyId("nombre");
			tipos.setRequired(true);
			tipos.setNullSelectionAllowed(false);
			tipos.setImmediate(true);
			tipos.setFilteringMode(ComboBox.FILTERINGMODE_STARTSWITH);
			tiposChangeValueListener = new ValueChangeListener() 
			{
				@Override
				public void valueChange(ValueChangeEvent event) {
					logger.info("changing value of tipo: " + event.getProperty().getValue());
					boolean isTransf = event.getProperty() != null
							&& !event.getProperty().equals("")
							&& event.getProperty().toString()
									.equals(Tipo.TRANSFERENCIA.toString());
					boolean isCheque = event.getProperty() != null
							&& !event.getProperty().equals("")
							&& event.getProperty().toString()
									.equals(Tipo.CHEQUE.toString());

					if (isCheque || isTransf) {						
						Field f = operacionForm.getField("banco");
						f.setRequired(true);
						f.setRequiredError("Por favor rellena banco");
						f.setVisible(true);
						operacionForm.getField("chequeNumero").setVisible(true);
						if (isCheque) {
							operacionForm.getField("fechaDeCobro").setVisible(true);
							operacionForm.getField("fechaDeCobro").setValue(null);
						}
						else operacionForm.getField("fechaDeCobro").setVisible(false);
						if (isTransf)
							operacionForm.getField("chequeNumero").setCaption(
									"Transferencia Numero");
						else
							operacionForm.getField("chequeNumero").setCaption(
									"Cheque Numero");
						
						if (operacionForm.getField("pen")!=null && operacionForm.getField("pen").getValue()!=null
								&& !operacionForm.getField("pen").getValue().equals("") &&
								(new BigDecimal(((String)operacionForm.getField("pen").getValue()).replace(",","")).compareTo(new BigDecimal(0.00))!=0 && 
								cuenta.getCategoriaCuenta().getBancoPen()!=null)) 


							((ComboBox)f).select(cuenta.getCategoriaCuenta().getBancoPen());//f.setValue(cuenta.getCategoriaCuenta().getBancoPen());
						else if (operacionForm.getField("usd")!=null && operacionForm.getField("usd").getValue()!=null 
								&& !operacionForm.getField("usd").getValue().equals("") && 
								(new BigDecimal(((String)operacionForm.getField("usd").getValue()).replace(",","")).compareTo(new BigDecimal(0.00))!=0 && 
								cuenta.getCategoriaCuenta().getBancoUsd()!=null))
							//f.setValue(cuenta.getCategoriaCuenta().getBancoUsd());
							((ComboBox)f).select(cuenta.getCategoriaCuenta().getBancoUsd());
					} else {
						operacionForm.getField("chequeNumero")
								.setVisible(false);
						operacionForm.getField("fechaDeCobro").setVisible(false);
						Field f = operacionForm.getField("banco");
						operacionForm.getField("banco").setValue(null);
						f.setRequired(false);
						f.setVisible(false);

					}
				}
			};
			tipos.addListener(tiposChangeValueListener);
			DataFilterUtil.bindComboBox(bancos, "nombre",
					"Seleccione banco", Banco.class, null);
            bancos.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			bancos.setVisible(false);
			bancos.setImmediate(true);
		
			DataFilterUtil.bindComboBox(beneficiarios, "nombre",
					"Seleccione firma", Beneficiario.class, null);
			beneficiarios.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			beneficiarios.setNewItemsAllowed(true);
			beneficiarios.setNewItemHandler(this);
			beneficiarios.setImmediate(true);
			beneficiarios.setRequired(false);
			//beneficiarios.setRequiredError("Por favor rellena el beneficiario");
			
			DataFilterUtil.bindComboBox(tiposDocumento, "nombre",
					"Seleccione tipo de documento", TipoDocumento.class, null);
			tiposDocumento.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			
			DataFilterUtil.bindCentroCostoBox(centrosCosto);
			centrosCosto.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			
			DataFilterUtil.bindLugarGastoBox(lugarGasto);
			lugarGasto.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			lugarGasto.setVisible(true);
			
			DataFilterUtil.bindCuentaContableBox(cuentaContable);
			cuentaContable.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			cuentaContable.setVisible(true);
			
			DataFilterUtil.bindRubroProyectoBox(rubroProyecto, null, null, null, null);
			rubroProyecto.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			rubroProyecto.setVisible(true);

			DataFilterUtil.bindRubroInstitucionalBox(rubroInstitucional, null, null, null);
			rubroInstitucional.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			rubroInstitucional.setVisible(true);


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
			
			if ("tipo".equals(propertyId)) {
				return tipos;
			} else 
				if ("banco".equals(propertyId)) {
				return bancos;
			} else 
				if ("beneficiario".equals(propertyId)) {
				return beneficiarios;
			} else 
				if ("tipoDocumento".equals(propertyId)) {
				return tiposDocumento;							
			} else 
				if ("centroCosto".equals(propertyId)) {
				return centrosCosto;				
			} else 
				if ("lugarGasto".equals(propertyId)) {
				return lugarGasto;				
			} else 
				if ("cuentaContable".equals(propertyId)) {
				return cuentaContable;	
			} else
				if ("rubroProyecto".equals(propertyId)) {
				return rubroProyecto;	
			} else
				if ("rubroInstitucional".equals(propertyId)) {
				return rubroInstitucional;	
			} else			
				if ("descripcion".equals(propertyId)) {
				f = createTextArea(propertyId);
			} else 
				if ("fecha".equals(propertyId)) {
				f = createDateField(propertyId);
				f.setRequired(true);
				boolean isCor = (item.getItemProperty("isCorrection")!=null ? (Boolean)item.getItemProperty("isCorrection").getValue() : false);
				f.setEnabled(!isCor);
				((DateField)f).setImmediate(true);
				f.addValidator(new DateValidator("La feche debe ser entre: " + 
						sdf.format(ConfigurationUtil.getUltimoMesCerrado()) + " y ahora", 
						ConfigurationUtil.get("DEFAULT_DATE_FORMAT"), 
						ConfigurationUtil.getUltimoMesCerrado(), new Date()));
			} else 
				if ("fechaDeCobro".equals(propertyId)) {
				f = createDateField(propertyId);
				f.setVisible(false);
			} else 
				if ("pen".equals(propertyId) || "usd".equals(propertyId)) {
				String regexp = "((\\d+)|(\\d{1,3})(\\,\\d{3})*)(\\.\\d{0,2})?$";
				CSValidatedTextField tf = new CSValidatedTextField(
						DefaultFieldFactory
								.createCaptionByPropertyId(propertyId));
				tf.setRequired(true);
				tf.setRegExp(regexp);
				tf.setErrorMessage("El monto no esta correcto");
				tf.setImmediate(true);
				f = tf;
				f.setRequired(true);
				f.setRequiredError("El monto no esta correcto");
				f.addValidator(new RegexpValidator(regexp,
						"El monto no esta correcto"));
				if (cuenta!=null && !ConfigurationUtil.is("ALLOW_OVERDRAW") 
						&& (Boolean) item.getItemProperty("isCargo").getValue()) {
					BigDecimal maxCargo = ("pen".equals(propertyId) ? cuenta.getPen()
							: cuenta.getUsd());
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
			} else {
				f = super.createField(item, propertyId, uiContext);
			}
			if ("firma".equals(propertyId)) {
				TextField tf = (TextField) f;
				tf.setCaption("Nombre");
				tf.setRequired(true);
				tf.setRequiredError("Por favor rellena la firma");
				tf.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
				tf.addValidator(new StringLengthValidator(
						"El nombre debe tener 3-50 letras", 3, 50, false));
			}
			if ("chequeNumero".equals(propertyId)) {
				TextField tf = (TextField) f;
				tf.setVisible(false);
				tf.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			}
			if ("pen".equals(propertyId)) {
				TextField tf = (TextField) f;
				tf.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
				tf.addListener(new FocusListener() {
					@Override
					public void focus(FocusEvent event) {
						logger.info("Focus listener: " + event);
						if (operacionForm.getField("usd")!=null) 
							operacionForm.getField("usd").setValue("0.00");
						operacionForm.getField("pen").setValue("");
						operacionForm.getField("pen").setEnabled(true);
						// Filter bancos for only PEN 
						if (operacionForm.getField("banco").getValue()==null)
						{
							DataFilterUtil.bindComboBox(bancos, "nombre",
								"Seleccione banco", Banco.class, Filters.eq("moneda", Banco.Moneda.PEN));}
					}
				});
			}

			if ("usd".equals(propertyId)) {
				TextField tf = (TextField) f;
				tf.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
				tf.addListener(new FocusListener() {
					@Override
					public void focus(FocusEvent event) {
						if (operacionForm.getField("pen")!=null) 
							operacionForm.getField("pen").setValue("0.00");
						operacionForm.getField("usd").setValue("");
						// Filter bancos for only PEN 
						if (operacionForm.getField("banco").getValue()==null)
						{
						DataFilterUtil.bindComboBox(bancos, "nombre",
								"Seleccione banco", Banco.class, Filters.eq("moneda", Banco.Moneda.USD));
						}
					}
				});
			}
			return f;
		}
	}

	private class OperacionConfirmationFieldFactory extends VasjaFieldFactory {
	
		final ComboBox bancos = new ComboBox("Banco");
		final ComboBox beneficiarios = new ComboBox("Entregado a/por");
		final ComboBox tiposDocumento = new ComboBox("Tipo de documento");
		final ComboBox centrosCosto = new ComboBox("Centro de costo");
		final ComboBox lugarGasto = new ComboBox("Lugar de gasto");
		final ComboBox rubroProyecto = new ComboBox("Rubro presupuestal de proyecto");
		final ComboBox cuentaContable = new ComboBox("Cuenta contable");
		final ComboBox rubroInstitucional = new ComboBox("Rubro institucional");
		
		public OperacionConfirmationFieldFactory() {
			DataFilterUtil.bindComboBox(bancos, "nombre",
					"Seleccione banco", Banco.class, null);
            bancos.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			bancos.setVisible(false);
			bancos.setEnabled(false);
			DataFilterUtil.bindComboBox(beneficiarios, "nombre",
					"Seleccione firmante", Beneficiario.class, null);
			beneficiarios.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			beneficiarios.setEnabled(false);
			
			DataFilterUtil.bindComboBox(tiposDocumento, "nombre",
					"Seleccione tipo de documento", TipoDocumento.class, null);
			tiposDocumento.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			tiposDocumento.setEnabled(false);
			DataFilterUtil.bindComboBox(centrosCosto, "nombre",
					"Seleccione centro de costo", CentroCosto.class, Filters.eq("isLeaf", new Boolean(true)));
			centrosCosto.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			centrosCosto.setEnabled(false);

			DataFilterUtil.bindComboBox(lugarGasto, "nombre",
					"Seleccione lugarGasto", LugarGasto.class, Filters.eq("isLeaf", new Boolean(true)));
			lugarGasto.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			lugarGasto.setEnabled(false);
		
			DataFilterUtil.bindComboBox(cuentaContable, "nombre",
					"Seleccione cuentaContable", CuentaContable.class, Filters.eq("isLeaf", new Boolean(true)));
			cuentaContable.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			cuentaContable.setEnabled(false);
			
			DataFilterUtil.bindComboBox(rubroProyecto, "nombre",
					"Seleccione rubroProyecto", RubroProyecto.class, Filters.eq("isLeaf", new Boolean(true)));
			rubroProyecto.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			rubroProyecto.setEnabled(false);
			
			DataFilterUtil.bindComboBox(rubroInstitucional, "nombre",
					"Seleccione rubroInstitucional", RubroInstitucional.class, Filters.eq("isLeaf", new Boolean(true)));
			rubroInstitucional.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			rubroInstitucional.setEnabled(false);
			
			
		}
		
		@Override
		public Field createField(Item item, Object propertyId,
				Component uiContext) {
			Field f;
			if ("fecha".equals(propertyId)) {
				f = createDateField(propertyId);
				f.setEnabled(false);
			} else if ("fechaDeCobro".equals(propertyId)) {
				f = createDateField(propertyId);
				f.setEnabled(false);
				if ((item.getItemProperty("tipo")!=null) && (item.getItemProperty("tipo").getValue().equals(Tipo.CHEQUE.toString())))
					f.setVisible(true);
				else
					f.setVisible(false);
			} else if ("descripcion".equals(propertyId)) {
				f = createTextArea(propertyId);
				f.setEnabled(false);
			} else {
				f = super.createField(item, propertyId, uiContext);
				f.setEnabled(false);
			}
			if ("beneficiario".equals(propertyId)) {
				return beneficiarios;
			} else if ("tipoDocumento".equals(propertyId)) {
				return tiposDocumento;							
			} else if ("centroCosto".equals(propertyId)) {
				return centrosCosto;				
			} else if ("lugarGasto".equals(propertyId)) {
				return lugarGasto;				
			} else if ("cuentaContable".equals(propertyId)) {
				return cuentaContable;				
			} else if ("rubroInstitucional".equals(propertyId)) {
				return rubroInstitucional;				
			} else if ("rubroProyecto".equals(propertyId)) {
				return rubroProyecto;				
			}
			if ("banco".equals(propertyId)) {
				logger.info("Got Banco: " + item.getItemProperty("banco").getValue());
				if (item.getItemProperty("banco").getValue()!=null) 
					bancos.setVisible(true);
				else 
					bancos.setVisible(false);
				return bancos;
			}			
			if ("chequeNumero".equals(propertyId)
					&& (Tipo.TRANSFERENCIA.toString().equals(item
							.getItemProperty("tipo").getValue().toString()))) {
				logger.fine(" Tipo "
						+ item.getItemProperty("tipo").getValue().toString());
				f.setCaption("Transferencia numero");
			}
			if ("firma".equals(propertyId)) 
				f.setCaption("Nombre");
			return f;
		}
	}
	
	public static Cuenta recalculateSaldos(Long cuentaId, Date fechaToRecalculate, EntityManager em) {
		logger.info("+++++++ RecalculateSaldos: " + cuentaId + " " + fechaToRecalculate + "--------");
		BigDecimal saldoPen = new BigDecimal(0.00);
		BigDecimal saldoUsd = new BigDecimal(0.00);
		List<Operacion> newerOpers = ConfigurationUtil.getOperacionesNewerForCuenta(cuentaId,fechaToRecalculate, em);
		Operacion lastOper = ConfigurationUtil.getOperacionLastForCuenta(cuentaId, fechaToRecalculate, em);
		logger.info("Last oper: " + lastOper);
		if (lastOper!=null) {
			saldoPen = saldoPen.add(lastOper.getSaldoPen());
			saldoUsd = saldoUsd.add(lastOper.getSaldoUsd());
		}
		MutableLocalEntityProvider<Cuenta> cuentaEP =  new BatchableLocalEntityProvider<Cuenta>(
				Cuenta.class, em);
		cuentaEP.setTransactionsHandledByProvider(false);
		if (!newerOpers.isEmpty()) {
			MutableLocalEntityProvider<Operacion> operacionEP = new BatchableLocalEntityProvider<Operacion>(
					Operacion.class, em);
			operacionEP.setTransactionsHandledByProvider(false);
			for (Operacion oper : newerOpers) {
				logger.info("Newer oper: " + oper.getId() + " pen: " + oper.getPen() + " saldopen: " + oper.getSaldoPen());
				saldoPen = saldoPen.add(oper.getPen());
				saldoUsd = saldoUsd.add(oper.getUsd());
				oper.setSaldoPen(saldoPen);
				oper.setSaldoUsd(saldoUsd);
				operacionEP.updateEntity(oper);				
				logger.info("----- oper: " + oper.getId() + " pen: " + oper.getPen() + " saldopen: " + oper.getSaldoPen());
			}
		}
		Cuenta cnta = cuentaEP.refreshEntity(cuentaEP.getEntityManager().find(Cuenta.class, cuentaId));
		cnta.setPen(saldoPen);
		cnta.setUsd(saldoUsd);
		logger.info("----- updated cuenta: " + cnta.getNumero() + " " + cnta.getPen() + " " + cnta.getUsd());
		return cuentaEP.updateEntity(cnta);			
	}
	
	public static Cuenta recalculateSaldos(Operacion newOper, EntityManager em) {
		return recalculateSaldos(newOper.getCuenta().getId(), newOper.getFecha(), em);
	}
}

package org.sanjose.web;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.sanjose.model.Banco;
import org.sanjose.model.Beneficiario;
import org.sanjose.model.CategoriaCuenta;
import org.sanjose.model.CentroCosto;
import org.sanjose.model.Cuenta;
import org.sanjose.model.LugarGasto;
import org.sanjose.model.Operacion;
import org.sanjose.model.Operacion.Tipo;
import org.sanjose.model.Propietario;
import org.sanjose.model.Saldo;
import org.sanjose.model.TipoDocumento;
import org.sanjose.model.Usuario;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.web.helper.SessionHandler;
import org.sanjose.web.layout.ImportLayout;

import com.vaadin.addon.jpacontainer.provider.BatchableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ImportTabsheet extends VerticalLayout {
	
	ImportLayout il = new ImportLayout();
	
	CuentasReceiver cuentasRec = new CuentasReceiver();
	CuentasReceiver operRec = new CuentasReceiver();
	
	static final Logger logger = Logger
			.getLogger(ImportTabsheet.class.getName());

	public ImportTabsheet() {
		setCaption("Importar");
		setSpacing(true);
		Panel panel = new Panel();
		panel.addComponent(il);
		
        // Button Listeners
		
		il.getUploadCuentas().setReceiver(cuentasRec);		
		il.getUploadCuentas().addListener(new Upload.FinishedListener() {
            public void uploadFinished(FinishedEvent event) {
            	il.getLogTextArea().setValue(importCuentas(cuentasRec.getText(), cuentasRec.getMtype()));
            	logger.info("Got mtype: " + cuentasRec.getMtype());
            	logger.info("Text: " + cuentasRec.getText());            	            	
            	il.getUploadCuentas().setReceiver(null);
            	cuentasRec = new CuentasReceiver();
            	il.getUploadCuentas().setReceiver(cuentasRec);
            }
        });
		
		il.getUploadOperaciones().setReceiver(operRec);		
		il.getUploadOperaciones().addListener(new Upload.FinishedListener() {
            public void uploadFinished(FinishedEvent event) {
            	//importTable(cuentasRec.getText());
            	il.getLogTextArea().setValue(importOperaciones(operRec.getText(), operRec.getMtype()));
            	il.getUploadOperaciones().setReceiver(null);
            	operRec = new CuentasReceiver();
            	il.getUploadOperaciones().setReceiver(operRec);
            }
        });

		
		il.getClearLogButton().addListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				il.getLogTextArea().setValue("");	
				cuentasRec = new CuentasReceiver();
				operRec = new CuentasReceiver();
			}
		});
		panel.addComponent(il);
		addComponent(panel);
		setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
	}
	
	public String importCuentas(String txt, String mType) {
		EntityManager em = ConfigurationUtil.getNewEntityManager();
		MutableLocalEntityProvider<Operacion> operacionEP = new BatchableLocalEntityProvider<Operacion>(
				Operacion.class, em);
		operacionEP.setTransactionsHandledByProvider(false);
		MutableLocalEntityProvider<Cuenta> cuentaEP =  new BatchableLocalEntityProvider<Cuenta>(
				Cuenta.class, em);
		MutableLocalEntityProvider<Propietario> propietarioEP =  new BatchableLocalEntityProvider<Propietario>(
				Propietario.class, em);
		MutableLocalEntityProvider<Usuario> usuarioEP =  new BatchableLocalEntityProvider<Usuario>(
				Usuario.class, em);
		EntityTransaction trans = em.getTransaction();
		if (trans.isActive()) trans.rollback();
		trans.begin();
		String separator = (mType.equals("text/plain") ? "\t" : ";");
		int lineNum = 0;
		String logTxt = "";
		String[] lines = (txt.contains("\r\n") ? txt.split("\r\n") : txt.split("\n"));
		String[] tableCols = lines[0].split(separator);
		if (tableCols.length!=8) {
			if (trans.isActive()) trans.rollback();
			return "ERROR Wrong number of columns! There should be 8 cols for Cuentas and there are: " + tableCols.length;
		}
		try {
			for (int i=1;i<lines.length;i++) {
				lineNum = i+1;
				String[] tableColsValues = lines[i].split(separator);
				logger.info("Cols: " + i + " " + tableColsValues.length);
				if (tableColsValues.length==8) {
					logger.info("Table of Cols: " + Arrays.toString(tableColsValues));					
					Propietario prop = ConfigurationUtil.getPropietarioPorNombre(tableColsValues[7], em);
					logger.info("Prop: " + prop);
					// if not exists create
					if (prop==null) {						
						prop = propietarioEP.addEntity(new Propietario(tableColsValues[7]));						
					}
					CategoriaCuenta cat = ConfigurationUtil.getCategoriaCuentaPorNombre(tableColsValues[6], em);
					if (cat==null) {
						if (trans.isActive()) trans.rollback();								
						return "ERROR CategoriaCuenta: " +  tableColsValues[6] + " no existe!";
					}
					Cuenta cuenta = new Cuenta();
					cuenta.setCategoriaCuenta(cat);
					logger.info("Categoria:"+cat);
					cuenta.setCreadoPorUsuario(em.find(Usuario.class, (SessionHandler.get().getId())));
					cuenta.setDescripcion(tableColsValues[2]);
					cuenta.setFechaDeApertura(getDateFromText(tableColsValues[3]));
					logger.info("fecha: "+getDateFromText(tableColsValues[3]));
					cuenta.setIsCaja(false);
					cuenta.setIsBanco(false);
					cuenta.setNombre(tableColsValues[1]);
					logger.info("nombre: "+getDateFromText(tableColsValues[3]));
					cuenta.setNumero(Long.parseLong(tableColsValues[0]));		
					logger.info("numero: "+Long.parseLong(tableColsValues[0]));		
					//cuenta.setUsd(getBigDecimalFromText(tableColsValues[5]));
					//cuenta.setPen(getBigDecimalFromText(tableColsValues[4]));
					logger.info("pen: "+getBigDecimalFromText(tableColsValues[4]));
					cuenta.setPropietario(prop);
					
					Cuenta cn = ConfigurationUtil.getCuentaPorNumero(cuenta.getNumero(), em);
					if (cn!=null) {
						logTxt += "CUENTA num: " + cuenta.getNumero() + " ya existe, ignoramos!\n";
						continue;
					}
					
					logTxt += "\n" + cuenta.toString() + "\n";
					
					cuenta = cuentaEP.addEntity(cuenta);
					logger.info("cuenta "+cuenta);
					Operacion oper = new Operacion();
					// PEN
					oper.setCuenta(cuenta);
					oper.setDescripcion("Saldo Inicial PEN: " + tableColsValues[3]);
					oper.setTipo(Tipo.SALDO_INICIAL);
					oper.setPen(getBigDecimalFromText(tableColsValues[4]));
					oper.setIsCargo(getBigDecimalFromText(tableColsValues[4]).compareTo(new BigDecimal(0))<0);
					oper.setIsPen(true);
					//oper.setSaldoPen(getBigDecimalFromText(tableColsValues[4]));
					oper.setFecha(cuenta.getFechaDeApertura());
					oper.setIsCorrection(false);
					oper.setUsuario(em.find(Usuario.class, (SessionHandler.get().getId())));
					
					// Updating cn Saldo
					cuenta.setPen(cuenta.getPen().add(oper.getPen()));
					cuenta.setUsd(cuenta.getUsd().add(oper.getUsd()));
					oper.setSaldoPen(cuenta.getPen());
					oper.setSaldoUsd(cuenta.getUsd());
					cuenta = cuentaEP.updateEntity(cuenta);
					oper.setCuenta(cuenta);
					// Save operacion
					oper = operacionEP.addEntity(oper);
					logger.info("Cuenta after PEN: " + cuenta);
					logTxt += "PEN:\n" + oper.toString() + "\n";
					// Process Caja/Banco
					///OperacionForm.processCajaBancoOperacion(cn, (Operacion)oper, em, cuentaEP, operacionEP);
					
					
					oper = new Operacion();
					// USD
					oper.setCuenta(cuenta);
					oper.setDescripcion("Saldo Inicial USD : " + tableColsValues[3]);
					oper.setTipo(Tipo.SALDO_INICIAL);
					oper.setUsd(getBigDecimalFromText(tableColsValues[4]));
					oper.setIsCargo(getBigDecimalFromText(tableColsValues[5]).compareTo(new BigDecimal(0))<0);
					oper.setIsPen(false);
					//oper.setSaldoUsd(getBigDecimalFromText(tableColsValues[4]));
					oper.setFecha(ConfigurationUtil.dateAddSeconds(cuenta.getFechaDeApertura(),1));
					oper.setIsCorrection(false);
					oper.setUsd(getBigDecimalFromText(tableColsValues[5]));
					//oper.setSaldoUsd(getBigDecimalFromText(tableColsValues[5]));
					oper.setUsuario(em.find(Usuario.class, (SessionHandler.get().getId())));
					
					// Updating cn Saldo
					cuenta.setPen(cuenta.getPen().add(oper.getPen()));
					cuenta.setUsd(cuenta.getUsd().add(oper.getUsd()));				
					oper.setSaldoPen(cuenta.getPen());
					oper.setSaldoUsd(cuenta.getUsd());
					cuenta = cuentaEP.updateEntity(cuenta);
					oper.setCuenta(cuenta);
					// Save operacion
					oper = operacionEP.addEntity(oper);
					// Process Caja/Banco
					///OperacionForm.processCajaBancoOperacion(cn, (Operacion)oper, em, cuentaEP, operacionEP);
					logTxt += "USD:\n" + oper.toString() + "\n";
				}
				else {
					logger.info("Cols: " + i + " " + tableColsValues.length);
					logTxt = "ERROR\nEl formato del archivo es incorrecto - linia:  " + i + " tiene "+tableColsValues.length+"columnas. Se necesita 8 columnas por linia.";
					throw new Exception(logTxt);
					}
					// Do insert
				//em.createNativeQuery(insStr);
			}						
		} catch (ParseException e) {
			if (trans.isActive()) trans.rollback();
			return logTxt + "\nERROR: " + e.getClass().getName() + " " + e.getMessage() + " at: " + lineNum;
		} catch (NumberFormatException e) {
			if (trans.isActive()) trans.rollback();
			return logTxt + "\nERROR: " + e.getClass().getName() + " " + e.getMessage() + " at: " + lineNum;
		} catch (Exception e) {
			if (trans.isActive()) trans.rollback();
			//il.getLogTextArea().setValue("Error: " + e.getMessage());
			logger.severe("Error: " + e.getMessage());
			//e.printStackTrace();
			return logTxt + "\nERROR: " + e.getClass().getName() + " " + e.getMessage() + " at: " + lineNum;
		}
		if (trans.isActive()) trans.commit();		
		return logTxt + "\n\n" +(lineNum -1) + " cuentas importado!"; 
	}
	
	
	
	public String importOperaciones(String txt, String mType) {
		int COL_LENGTH = 15;
		EntityManager em;		
		logger.info("EM props: " + ConfigurationUtil.getEntityManager().getProperties().toString());
		em = ConfigurationUtil.getNewEntityManager();
		MutableLocalEntityProvider<Operacion> operacionEP = new BatchableLocalEntityProvider<Operacion>(
				Operacion.class, em);
		operacionEP.setTransactionsHandledByProvider(false);
		MutableLocalEntityProvider<Cuenta> cuentaEP =  new BatchableLocalEntityProvider<Cuenta>(
				Cuenta.class, em);
		MutableLocalEntityProvider<Beneficiario> beneficiarioEP =  new BatchableLocalEntityProvider<Beneficiario>(
				Beneficiario.class, em);
		MutableLocalEntityProvider<Usuario> usuarioEP =  new BatchableLocalEntityProvider<Usuario>(
				Usuario.class, em);
		cuentaEP.setTransactionsHandledByProvider(false);
		MutableLocalEntityProvider<Saldo> saldoEP = new BatchableLocalEntityProvider<Saldo>(
				Saldo.class, em);
		saldoEP.setTransactionsHandledByProvider(false);					
		EntityTransaction trans = em.getTransaction();		
		if (trans.isActive()) trans.rollback();
		trans.begin();
		String separator = (mType.equals("text/plain") ? "\t" : ";");
		int lineNum = 0;
		String logTxt = "";
		String logCaja = null;
		
		String[] lines = (txt.contains("\r\n") ? txt.split("\r\n") : txt.split("\n"));
		String[] tableCols = lines[0].split(separator);				
		//logger.info("Lines: " + tableCols);
		if (tableCols.length!=COL_LENGTH) {
			if (trans.isActive()) trans.rollback();
			return "ERROR Wrong number of columns! There should be " + COL_LENGTH + " cols for Operaciones and there are: " + tableCols.length;
		}
		try {
			Date ultimoMes = ConfigurationUtil.getUltimoMesCerrado(em);
			for (int i=1;i<lines.length;i++) {
				lineNum = i+1;
				String[] tableColsValues = lines[i].split(separator);
				logger.info("Cols: " + i + " " + tableColsValues.length);
				if (tableColsValues.length==COL_LENGTH) {
					Object newSaldoEntity = null;
					Operacion oper = new Operacion();
					// Usuario
					oper.setUsuario(em.find(Usuario.class, (SessionHandler.get().getId())));
					// Fecha de emision
					String fechaEmision=tableColsValues[0];
					if (fechaEmision.length()>0)
						oper.setFecha(getDateFromText(fechaEmision));
					else 
						if (trans.isActive()) {
							trans.rollback();								
					return "ERROR Operacion fecha: " +  tableColsValues[0] + " !";
						}
						// cannot import if month already closed!!!
					if (oper.getFecha().before(ultimoMes)) {
						if (trans.isActive()) trans.rollback();								
						return "ERROR Operacion fecha: " +  tableColsValues[0] + " es mas temprano que el ultimo mes cerrado!";
					}						
					// Fecha de cobro
					String fechaCobro=tableColsValues[1];
					if (fechaCobro.length()>0)
						oper.setFechaDeCobro((getDateFromText(fechaCobro)));
					//cta
					String colCuenta = tableColsValues[2];
					// Moneda
					oper.setIsPen(!tableColsValues[3].equals("USD") && !tableColsValues[3].equals("usd"));
					// Descripcion
					oper.setDescripcion(tableColsValues[4]);
					String colAbono = tableColsValues[5];
					String colCargo = tableColsValues[6];
					String colBanco = tableColsValues[7];
					// Cheque numero
					oper.setChequeNumero(tableColsValues[8]);					

					String colBenef = tableColsValues[9];
					String colTipoDoc = tableColsValues[10];
					// Documento numero
					oper.setDocNumero(tableColsValues[11]);
					String colCentroCosto = tableColsValues[12];
					// Lugar de Gasto
					String colLugarGasto = tableColsValues[13];
					String colTipo = tableColsValues[14];
					
					// Tipo
					if (colTipo.equals("BANCO") || colTipo.equals("TRANSFERENCIA"))
						oper.setTipo(Tipo.TRANSFERENCIA);
					else if (colTipo.equals("CHEQUE") || colTipo.equals("cheque"))
						oper.setTipo(Tipo.CHEQUE);
					else
						oper.setTipo(Tipo.EFECTIVO);	
					Boolean isEfectivo = oper.getTipo().equals(Tipo.EFECTIVO.toString()); 					
					// Banco
					if (colBanco!=null && colBanco.length()>0) {
						Banco banco = ConfigurationUtil.getBancoPorNombre(colBanco.trim() , em);
						logger.info("Banco: " + banco);						
						if (banco==null) {
							if (trans.isActive()) trans.rollback();								
							return "ERROR Banco: " +  colBanco.trim() + " no existe!";
						}
						oper.setBanco(banco);
					}
					// Beneficiario
					if (colBenef!=null && colBenef.length()>0) {
						Beneficiario benef = ConfigurationUtil.getBeneficiarioPorNombre(colBenef.trim(), em);
						if (benef==null) {						
							benef = beneficiarioEP.addEntity(new Beneficiario(colBenef.trim()));						
						}
						logger.info("Benef: " + benef);						
						oper.setBeneficiario(benef);
					}
					// TipoDocumento
					if (colTipoDoc!=null && colTipoDoc.length()>0) {
						TipoDocumento tipoDoc = ConfigurationUtil.getTipoDocumentoPorAbrev(colTipoDoc, em);
						logger.info("TipoDocumento: " + tipoDoc);						
						if (tipoDoc==null) {
							if (trans.isActive()) trans.rollback();								
							return "ERROR TipoDocumento: " +  colTipoDoc + " no existe!";
						}
						oper.setTipoDocumento(tipoDoc);
					}
					// CentroCosto
					if (colCentroCosto!=null && colCentroCosto.length()>0) {
		//ab - tutaj trzeba poprawic zeby szukalo po codigo  ZROBIONE
		//				CentroCosto cenCosto = ConfigurationUtil.getCentroCostoPorNombre(colCentroCosto, em);
						CentroCosto cenCosto = ConfigurationUtil.getCentroCostoPorCodigo(colCentroCosto, em);
		//				CentroCosto cenCosto = em.find(CentroCosto.class, new Long(colCentroCosto));
						logger.info("CentroCosto: " + cenCosto);						
						if (cenCosto==null) {
							if (trans.isActive()) trans.rollback();								
							return "ERROR CentroCosto: " +  colCentroCosto + " no existe!";
						}
						oper.setCentroCosto(cenCosto);
					}
					// LugarGasto
					if (colLugarGasto!=null && colLugarGasto.length()>0) {
						LugarGasto lugarGasto= ConfigurationUtil.getLugarGastoPorCodigo(colLugarGasto, em);
						logger.info("LugarGasto: " + lugarGasto);						
						if (lugarGasto==null) {
							if (trans.isActive()) trans.rollback();								
							return "ERROR LugarGasto: " +  colLugarGasto + " no existe!";
						}
						oper.setLugarGasto(lugarGasto);
					}
					// Cuenta
					if (colCuenta==null || colCuenta.compareTo("")==0) {
						if (trans.isActive()) trans.rollback();								
						return "ERROR Cuenta no esta especificada!";
					} 
					Cuenta cn = ConfigurationUtil.getCuentaPorNumero(Long.parseLong(colCuenta), em);
					logger.info("Cuenta existe: " + cn);						
					if (cn==null) {
						if (trans.isActive()) trans.rollback();								
						return "ERROR Cuenta: " +  colCuenta + " no existe!";
					}
					oper.setCuenta(cn);
					// Monto y isCargo
					if (colAbono.length()>0 && colCargo.length()>0) {
						if (trans.isActive()) trans.rollback();								
						return "ERROR Abono (" + colAbono + ") y Cargo (" + colCargo + ") - los dos no pueden tener valores!";
					}
					BigDecimal monto = new BigDecimal(0);
					if (colAbono.length()>0) {
						oper.setIsCargo(false);
						monto = getBigDecimalFromText(colAbono).setScale(2);
					} else {
						oper.setIsCargo(true);
						monto = getBigDecimalFromText(colCargo).multiply(new BigDecimal(-1)).setScale(2);
					}
					if (oper.getIsPen())
						oper.setPen(monto);
					else 
						oper.setUsd(monto);
					// ******************************************************************************************
					
					// Adding Operacion logic - copied partly from OperacionForm saveOperacion...
					
					String logCuenta = "Cuenta numero: " + cn.getNumero()
							+ " , antes PEN: " + cn.getPen()
							+ ", USD: " + cn.getUsd();
					logger.info(logCuenta);
					// Updating cn Saldo
					cn.setPen(cn.getPen().add(oper.getPen()));
					cn.setUsd(cn.getUsd().add(oper.getUsd()));				
					cn = cuentaEP.updateEntity(cn);
					oper.setCuenta(cn);
					
					oper.setSaldoPen(cn.getPen());
					oper.setSaldoUsd(cn.getUsd());
					logger.info("saldo pen: "+cn.getPen());
					logger.info("saldo usd: "+cn.getUsd());
					
				//	Saldo lastSaldo = ConfigurationUtil.getSaldoPorFecha(cn.getCategoriaCuenta().getId(), oper.getFecha(), em);
					
					Object operAdded = operacionEP.addEntity(oper);
					Object newOperacionId = ((Operacion)operAdded).getId();
					
					// Process Saldo recalculation for operations in the past
					OperacionForm.recalculateSaldos((Operacion)operAdded, em);
					
					logger.info("new oper id: " + newOperacionId);
					oper.setId((Long) newOperacionId);
					if (operAdded!=null) 
						logger.info("Added new operacion: " + operAdded.toString());
					
					logger.info("Updated cn: " + cn.toString());
					if (!cn.getIsCaja()) {
						OperacionForm.processCajaBancoOperacion(cn, (Operacion)operAdded, em, cuentaEP, operacionEP);
						/*if (cn.getCategoriaCuenta().getCajaCuenta()==null) throw new Exception("La categoria de esta Cuenta no tiene una cuenta de CAJA!!!"); 
						Cuenta cajaCuenta = em.find(Cuenta.class, cn.getCategoriaCuenta().getCajaCuenta().getId(), LockModeType.PESSIMISTIC_WRITE);
						logCaja = " Caja antes, PEN: " + cajaCuenta.getPen() + " USD: " + cajaCuenta.getUsd();
						cajaCuenta.setPen(cajaCuenta.getPen().add(oper.getPen()));
						cajaCuenta.setUsd(cajaCuenta.getUsd().add(oper.getUsd()));
						cajaCuenta = cuentaEP.updateEntity(cajaCuenta);
						logCaja = ", CAJA ahora PEN: " + cajaCuenta.getPen() + ", USD: " + cajaCuenta.getUsd();
						logger.info(logCaja);*/
						/*Saldo newSaldo = new Saldo(lastSaldo, oper);
						newSaldoEntity = saldoEP.addEntity(newSaldo);
						OperacionForm.recalculateSaldosInSaldos((Saldo)newSaldoEntity, em);
						logger.info("Added new saldo: " + newSaldoEntity.toString());*/
					}
/*					if (cn.getIsCaja()) {
						Tipo t = oper.getTipo();
						oper.setTipo(Tipo.EFECTIVO);
						Saldo newSaldo = new Saldo(lastSaldo, oper);
						newSaldoEntity = saldoEP.addEntity(newSaldo);
						// Recalculate Saldos!!!
						OperacionForm.recalculateSaldosInSaldos((Saldo)newSaldoEntity, em);
						oper.setTipo(t);
					}*/
					//logger.info("Nueva operacion "
					//		+ (oper.getIsCorrection() ? " CORRECCION" : "")
					//		+ ": " + oper);
					logCuenta += ", ahora PEN: " + cn.getPen() + ", USD: "
							+ cn.getUsd();
					logger.info(logCuenta);									
				
					logTxt += "ADDED\n" + oper.toString() + "\n";					
				}
				else {
					logger.info("Cols: " + (i+1) + " " + tableColsValues.length);
					return "ERROR\nEl formato del archivo es incorrecto - linia:  " + i + " tiene "+tableColsValues.length+"columnas. Se necesita " + COL_LENGTH + " columnas por linia.";
				}				
			}						
		} catch (ParseException e) {
			if (trans.isActive()) trans.rollback();
			e.printStackTrace();
			return logTxt + "\nERROR: " + e.getClass().getName() + " " + e.getMessage() + " at: " + lineNum;
		} catch (NumberFormatException e) {
			if (trans.isActive()) trans.rollback();
			return logTxt + "\nERROR: " + e.getClass().getName() + " " + e.getMessage() + " at: " + lineNum;
		} catch (Exception e) {
			if (trans.isActive()) trans.rollback();
			il.getLogTextArea().setValue("Error: " + e.getMessage());
			logger.severe("Error: " + e.getMessage());
			e.printStackTrace();
		}
		if (trans.isActive()) trans.commit();		
		return logTxt + "\n\n" +(lineNum -1) + " operaciones importado!"; 			
	}
	
	
	public static class CuentasReceiver implements Receiver {

        private String fileName;
        private String mtype;
        private int counter;
        private List<Byte> bytes = new ArrayList<Byte>();
        private String text = "";
        
        public String getMtype() {
			return mtype;
		}


		public String getText() {
        	byte[] bb = new byte[bytes.size()];
        	int i=0;
        	for (Byte o : bytes) {
        		bb[i] = o;
        		i++;
        	}        	
        	String encoding = ConfigurationUtil.get("IMPORTS_ENCODING");
        	if (mtype.equals("text/plain")) encoding = "UTF-16"; 
        	text = new String(bb, Charset.forName(encoding));
        	logger.info("Encoding: " + encoding); 
        	logger.info("Text to import: " + text);
			return text;
		}


		/**
         * return an OutputStream that simply counts lineends
         */
        public OutputStream receiveUpload(String filename, String MIMEType) {
            fileName = filename;
            mtype = MIMEType;            
            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                	bytes.add(new Integer(b).byteValue());
                }
            };
        }
	}		
	
	public static Date getDateFromText(String dateString) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return format.parse(dateString);			
		} catch (ParseException pe) {
			try {
				return format.parse(dateString + ":00");			
			} catch (ParseException ppe) {
				return format.parse(dateString + " 00:00:00");
			}							
		}
	}

	public static BigDecimal getBigDecimalFromText(String str) throws ParseException {
		BigDecimal bg = null;
		str = str.replace(" ","");
		//logger.info("Trying to parse " + str.trim());
		if (str==null || str.equals("") || str.trim().equals("-")) return new BigDecimal("0.00");
		try {
			return new BigDecimal(str.trim());
		} catch (NumberFormatException e) {
			//logger.severe("Problem parsing: " + str.trim().replace(",","."));
			return new BigDecimal(str.trim().replace(",","."));			
		}
	}

	
}

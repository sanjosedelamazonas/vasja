package org.sanjose.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.jfree.util.Log;
import org.sanjose.model.Banco;
import org.sanjose.model.Beneficiario;
import org.sanjose.model.CategoriaCuenta;
import org.sanjose.model.CentroCosto;
import org.sanjose.model.Cuenta;
import org.sanjose.model.CuentaContable;
import org.sanjose.model.LugarGasto;
import org.sanjose.model.MesCerrado;
import org.sanjose.model.Operacion;
import org.sanjose.model.Propiedad;
import org.sanjose.model.Propietario;
import org.sanjose.model.Role;
import org.sanjose.model.RubroInstitucional;
import org.sanjose.model.RubroProyecto;
import org.sanjose.model.Saldo;
import org.sanjose.model.TipoDocumento;
import org.sanjose.model.Usuario;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.provider.BatchableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.CachingBatchableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class ConfigurationUtil {

	public final static HashMap<String, String> defaultParamMap = new HashMap<String, String>();
	private static HashMap<String, String> paramMap = new HashMap<String, String>();

	public final static Locale LOCALE = new Locale("es", "PE");
	public final static String CSS_RED = "red";

	/* Persistence */
	final static String PERSISTENCE_UNIT = "VasjaEJBPU-nonJTA";
	static EntityManager em = null;
	static MutableLocalEntityProvider<Banco> bancoEP = null;
	static MutableLocalEntityProvider<Cuenta> cuentaEP = null;
	static MutableLocalEntityProvider<CategoriaCuenta> categoriaCuentaEP = null;
	static MutableLocalEntityProvider<Operacion> operacionEP = null;
	static MutableLocalEntityProvider<Propiedad> propiedadEP = null;
	static MutableLocalEntityProvider<Propietario> proprietarioEP = null;
	static MutableLocalEntityProvider<Role> roleEP = null;
	static MutableLocalEntityProvider<Saldo> saldoEP = null;
	static MutableLocalEntityProvider<Usuario> usuarioEP = null;
	static MutableLocalEntityProvider<CentroCosto> centroCostoEP = null;
	static MutableLocalEntityProvider<Beneficiario> beneficiarioEP = null;
	static MutableLocalEntityProvider<TipoDocumento> tipoDocumentoEP = null;
	static MutableLocalEntityProvider<MesCerrado> mesCerradoEP = null;
	static MutableLocalEntityProvider<LugarGasto> lugarGastoEP = null;
	static MutableLocalEntityProvider<CuentaContable> cuentaContableEP = null;
	static MutableLocalEntityProvider<RubroProyecto> rubroProyectoEP = null;
	static MutableLocalEntityProvider<RubroInstitucional> rubroInstitucionalEP = null;
	public static void init() {
		defaultParamMap.put("DECIMAL_FORMAT", "#,##0.00");
		defaultParamMap.put("SHORT_DATE_FORMAT", "MM/dd");
		defaultParamMap.put("DEFAULT_DATE_FORMAT", "yyyy/MM/dd");
		defaultParamMap.put("COMMON_FIELD_WIDTH", "12em");
		defaultParamMap.put("CSS_STYLE", "iso3166");
		defaultParamMap.put("THEME", "reindeer");
		defaultParamMap.put("DEV_MODE", "0");
		defaultParamMap.put("ALLOW_OVERDRAW", "TRUE");
		defaultParamMap.put("IMPORTS_ENCODING", "UTF-8");
		
		/* Reports */
		defaultParamMap.put("REPORTS_SOURCE_URL", "reports/");
//		defaultParamMap.put("REPORTS_SOURCE_FOLDER", "/d/java/workspaces/workspace_vasja/VasjaEXT/Reports/");
		defaultParamMap.put("REPORTS_SOURCE_FOLDER", "C:\\Users\\ab\\workspace\\VasjaEXT\\Reports\\");
		defaultParamMap.put("REPORTS_IMAGE_SERVLET",
				"../../servlets/image?image=");
		defaultParamMap.put("REPORTS_DIARIO_CAJA_TYPE", "PDF");
		defaultParamMap.put("REPORTS_DIARIO_BANCARIA_TYPE", "PDF");
		defaultParamMap.put("REPORTS_cuenta_TYPE", "PDF");
		defaultParamMap.put("REPORTS_COMPROBANTE_TYPE", "TXT");
		defaultParamMap.put("REPORTS_COMPROBANTE_OPEN", "TRUE");
		defaultParamMap.put("REPORTS_COMPROBANTE_PRINT", "FALSE");
		defaultParamMap.put("REPORTE_CAJA_PREPARADO_POR", "Gilmer G�mez Ochoa");
		defaultParamMap.put("REPORTE_CAJA_REVISADOR_POR", "Claudia Urrunaga R�os");
		defaultParamMap.put("REPORTE_BANCOS_PREPARADO_POR", "Cinthia del Castillo Segovia");
		defaultParamMap.put("REPORTE_BANCOS_REVISADOR_POR", "Claudia Urrunaga R�os");
		defaultParamMap.put("PRINTER_LIST_SHOW", "TRUE");
	}

	public static String getDefaultValue(String name) {
		if (defaultParamMap.isEmpty())
			init();
		return defaultParamMap.get(name);
	}

	public static Saldo getLastSaldo(Long catId) {
		return getLastSaldo(catId, getEntityManager());
	}

	public static Saldo getLastSaldo(Long catId, EntityManager em) {
		String qry = "Saldo.getLast";
		Query q = em.createNamedQuery(qry);
		q.setParameter(1, catId);
		q.setMaxResults(1);
		Saldo saldo = null;
		try {
			saldo = (Saldo) q.getSingleResult();
			return saldo;
		} catch (NoResultException e) {
			e.printStackTrace();			
			Saldo s = new Saldo();
			s.setFecha(new Date());
			s.setCategoriaCuenta(em.find(CategoriaCuenta.class, catId));
			return s;
		}
	}
	
	public static Long getNewCuentaNumber(EntityManager em) {
		Query q = em.createNativeQuery("SELECT MAX(cuenta.NUMERO) FROM cuenta");
		q.setMaxResults(1);
		Long res = (Long) q.getSingleResult();
		if (res==null) res = new Long(0);
		return (Long)res + 10; 		
	}

	public static Long getNewCuentaNumber() {
		return getNewCuentaNumber(getEntityManager());  		
	}
	
	
	public static Operacion getSaldoPorFecha(Cuenta cuenta, Date fecha) {
		return getSaldoPorFecha(cuenta, fecha, getEntityManager());
	}
	
	public static Operacion getSaldoPorFecha(Long catId, Date fecha) {
		return getSaldoPorFecha(catId, fecha, getEntityManager());		
	}
	
	public static Operacion getSaldoPorFecha(Long catId, Date fecha, EntityManager em) {
		CategoriaCuenta cat = em.find(CategoriaCuenta.class, catId);
		return getSaldoPorFecha(cat.getCajaCuenta(), fecha, em);
	}
	
	public static Operacion getOperacionByOperacionDetalle(Long opDetId, EntityManager em) {
		String qry = "Operacion.getByOperacionDetalle";
		Query q = em.createNamedQuery(qry);
		q.setParameter(1, opDetId);
		q.setMaxResults(1);
		Operacion oper = null;
		try {
			oper = (Operacion) q.getSingleResult();
			return oper;
		} catch (NoResultException e) {
			return null;
		}
	}
		
	public static Operacion getSaldoPorFecha(Cuenta cuenta, Date fecha, EntityManager em) {
		String qry = "Operacion.getLastForCuenta";
		//String qry = "Saldo.getSaldoPorFecha";
		Query q = em.createNamedQuery(qry);
		// For Caja y Banco which doesn't have a CajaCuenta
		if (cuenta==null) return new Operacion();
		q.setParameter(1, cuenta.getId());
		q.setParameter(2, fecha);
		q.setMaxResults(1);
		Operacion oper = null;
		try {
			oper = (Operacion) q.getSingleResult();
			//System.out.println("Got Operacion with saldo: " + oper.toString()
			//		+ " for fecha: " + fecha.toLocaleString());
			return oper;
		} catch (NoResultException e) {
			//Saldo sd = new Saldo();
			//sd.setCategoriaCuenta(getCategoriaCuentaEP(em).getEntity(catId));
			return new Operacion();
		}
	}
	
	public static Operacion getSaldoTotalPorFecha(Date fecha) {
		
		JPAContainer jpaContainer = JPAContainerFactory.make(CategoriaCuenta.class, ConfigurationUtil.getEntityManager());
		Collection<Object> catIds = jpaContainer.getItemIds();
		//List<Object> catIds = ConfigurationUtil.getCategoriaCuentaEP().getAllEntityIdentifiers();
		//getAllEntityIdentifiers(null, null);
		Operacion oper = new Operacion();
		oper.setFecha(fecha);
		for (Object catId : catIds) {
			Operacion catSaldo = getSaldoPorFecha((Long)catId, fecha);
			oper.setSaldoPen(oper.getSaldoPen().add(catSaldo.getSaldoPen()));
			oper.setSaldoUsd(oper.getSaldoUsd().add(catSaldo.getSaldoUsd()));
		}
		return oper;
	}

	public static Operacion getCajaMovimientosPorFecha(Date fechaMin, Date fechaMax, Long catId) {
		Operacion oper = new Operacion();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		Query q = em.createNativeQuery("SELECT SUM(operacion.PEN), SUM(operacion.USD) FROM operacion, cuenta, categoriacuenta " +
				"WHERE operacion.FECHA >= ? AND operacion.FECHA <= ? AND operacion.cuenta_ID = cuenta.ID" +
				" AND categoriacuenta.cajacuenta_ID = cuenta.ID AND categoriacuenta.ID = ?");
		q.setParameter(1, sdf.format(ConfigurationUtil.getBeginningOfDay(fechaMin)));
		q.setParameter(2, sdf.format(ConfigurationUtil.getEndOfDay(fechaMax)));
		q.setParameter(3, catId);
		q.setMaxResults(1);
		Object[] res = (Object[]) q.getSingleResult();
		if (res!=null && res.length>0) {
			oper.setSaldoPen(res[0]!=null ? (BigDecimal)res[0] : new BigDecimal(0.00));
			oper.setSaldoUsd(res[1]!=null ? (BigDecimal)res[1] : new BigDecimal(0.00));
		}				
		return oper;
	}
	
	public static List<Operacion> getOperacionesNewerForCuenta(Long cuentaId, Date fecha, EntityManager em) {
		String qry = "Operacion.getNewerForCuenta";
		if (em==null) em = getEntityManager();
		Query q = em.createNamedQuery(qry);
		q.setParameter(1, cuentaId);
		q.setParameter(2, fecha);
		//q.setMaxResults(1);
		try {
			return q.getResultList();			
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public static List<Saldo> getSaldosNewerForCategoria(Long catId, Date fecha, EntityManager em) {
		String qry = "Saldo.getSaldosNewerForCategoria";
		if (em==null) em = getEntityManager();
		Query q = em.createNamedQuery(qry);
		q.setParameter(1, catId);
		q.setParameter(2, fecha);
		//q.setMaxResults(1);
		try {
			return q.getResultList();			
		} catch (NoResultException e) {
			return null;
		}
	}
	
	

	public static Operacion getOperacionLastForCuenta(Long cuentaId, Date fecha, EntityManager em) {
		String qry = "Operacion.getLastForCuenta";
		if (em==null) em = getEntityManager();
		Query q = em.createNamedQuery(qry);
		q.setParameter(1, cuentaId);
		q.setParameter(2, fecha);
		q.setMaxResults(1);
		try {
			return (Operacion)q.getSingleResult();			
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public static Saldo getSaldoLastForCategoria(Long catId, Date fecha, EntityManager em) {
		String qry = "Saldo.getSaldoLastForCategoria";
		if (em==null) em = getEntityManager();
		Query q = em.createNamedQuery(qry);
		q.setParameter(1, catId);
		q.setParameter(2, fecha);
		q.setMaxResults(1);
		try {
			return (Saldo)q.getSingleResult();			
		} catch (NoResultException e) {
			return null;
		}
	}
	
	
	public static Propietario getPropietarioPorNombre(String nombre, EntityManager em) {
		String qry = "Propietario.getPropietarioPorNombre";
		if (em==null) em = getEntityManager();
		Query q = em.createNamedQuery(qry);
		q.setParameter(1, nombre);
		q.setMaxResults(1);
		try {
			return (Propietario)q.getSingleResult();			
		} catch (NoResultException e) {
			return null;
		}
	}

	public static CategoriaCuenta getCategoriaCuentaPorNombre(String nombre, EntityManager em) {
		String qry = "CategoriaCuenta.getCategoriaCuentaPorNombre";
		if (em==null) em = getEntityManager();
		Query q = em.createNamedQuery(qry);
		q.setParameter(1, nombre);
		q.setMaxResults(1);
		try {
			return (CategoriaCuenta)q.getSingleResult();			
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public static boolean isConsistent() {
		EntityManager emm = getNewEntityManager();
		Query q = emm
				.createNativeQuery("SELECT * FROM consistency ORDER BY FECHA DESC");
		q.setMaxResults(1);
		Object[] r = (Object[]) q.getSingleResult();
		emm.close();
		boolean isPen = r[1].equals(r[3]) && r[3].equals(r[5]);
		boolean isUsd = r[2].equals(r[4]) && r[4].equals(r[6]);
		return isPen && isUsd;
	}

	public static void checkConsistency() {
		EntityManager emm = getNewEntityManager();
		EntityTransaction etx = emm.getTransaction();
		etx.begin();
		Query q = emm.createNativeQuery("call check_consistency()");
		Object res = q.executeUpdate();
		if (etx.isActive())
			etx.commit();
		// q.executeUpdate(); SOBRA, el getSingleResult lo hace por el
		emm.close();
	}

	public static String getLastConsistency() {
		EntityManager emm = getNewEntityManager();
		Query q = emm
				.createNativeQuery("SELECT * FROM consistency ORDER BY FECHA DESC");
		q.setMaxResults(1);
		Object[] r = (Object[]) q.getSingleResult();

		String mes = ("Consistency check at: " + r[0].toString());
		//mes += "\n Saldo     PEN: " + r[1].toString();
		mes += "\n Cuentas   PEN: " + r[3].toString();
		mes += "\n Operacion PEN: " + r[5].toString();
		//mes += "\n\n Saldo     USD: " + r[2].toString();
		mes += "\n\n Cuentas   USD: " + r[4].toString();
		mes += "\n Operacion USD: " + r[6].toString();
		emm.close();
		System.out.println("Consistency " + mes);
		return mes;
	}

	public static void cuentas_mensual(Date fecha, Long catId) {
		EntityManager emm = getNewEntityManager();
		EntityTransaction etx = emm.getTransaction();
		etx.begin();
		Query q = emm.createNativeQuery("call cuentas_mensual(?,?,?)");
		q.setParameter(1, getBeginningOfMonth(fecha));
		q.setParameter(2, getBeginningOfMonth(dateAddMonths(fecha, 1)));
		q.setParameter(3, catId);
		System.out.println("Executing for: " + getBeginningOfMonth(fecha) + " " + getBeginningOfMonth(dateAddMonths(fecha, 1)));
		Object res = q.executeUpdate();
		if (etx.isActive())
			etx.commit();
		emm.close();
	}	
	
	public static void calculateSaldosDiario(Date fechaMin, Date fechaMax) {
		EntityManager emm = getNewEntityManager();
		EntityTransaction etx = emm.getTransaction();
		etx.begin();
		Query q = emm.createNativeQuery("call saldosForFecha(?,?);");
		q.setParameter(1, fechaMin);
		q.setParameter(2, fechaMax);
		Object res = q.executeUpdate();
		if (etx.isActive())
			etx.commit();
		emm.close();
	}		
	
	public static EntityManager getEntityManager() {
		if (em == null || !em.isOpen()) {
			EntityManagerFactory emf = Persistence
					.createEntityManagerFactory(PERSISTENCE_UNIT);
			em = emf.createEntityManager();
		}
		return em;
	}

	public static EntityManager getNewEntityManager() {
		EntityManagerFactory emf = Persistence
				.createEntityManagerFactory(PERSISTENCE_UNIT);
		return emf.createEntityManager();
	}

	public static MutableLocalEntityProvider<Banco> getBancoEP() {
		// if (bancoEP == null)
		bancoEP = new MutableLocalEntityProvider<Banco>(Banco.class,
				getNewEntityManager());
		return bancoEP;
	}

	public static MutableLocalEntityProvider<Cuenta> getCuentaEP(EntityManager em) {
		cuentaEP = new CachingBatchableLocalEntityProvider<Cuenta>(Cuenta.class, em);
		return cuentaEP;
	}

	public static MutableLocalEntityProvider<Cuenta> getCuentaEP() {
		return getCuentaEP(getNewEntityManager());
	}

	public static MutableLocalEntityProvider<CategoriaCuenta> getCategoriaCuentaEP() {
		return getCategoriaCuentaEP(getNewEntityManager());
	}

	public static MutableLocalEntityProvider<CategoriaCuenta> getCategoriaCuentaEP(EntityManager em) {
		// if (categoriaCuentaEP == null)
		categoriaCuentaEP = new MutableLocalEntityProvider<CategoriaCuenta>(
				CategoriaCuenta.class, em);
		return categoriaCuentaEP;
	}

	public static MutableLocalEntityProvider<Operacion> getOperacionEP(EntityManager em) {
		operacionEP = new CachingBatchableLocalEntityProvider<Operacion>(Operacion.class, em);
		return operacionEP;
	}

	public static MutableLocalEntityProvider<Operacion> getOperacionEP() {
		return getOperacionEP(getNewEntityManager());
	}

	
	public static MutableLocalEntityProvider<Propiedad> getPropiedadEP() {
		// if (propiedadEP == null)
		propiedadEP = new MutableLocalEntityProvider<Propiedad>(
				Propiedad.class, getNewEntityManager());
		return propiedadEP;
	}

	public static MutableLocalEntityProvider<Propietario> getProprietarioEP(EntityManager em) {
		// if (proprietarioEP == null)
		proprietarioEP = new MutableLocalEntityProvider<Propietario>(
				Propietario.class, em);
		return proprietarioEP;
	}

	public static MutableLocalEntityProvider<Propietario> getProprietarioEP() {
		return getProprietarioEP(getNewEntityManager());		
	}
	
	public static MutableLocalEntityProvider<Role> getRoleEP() {
		// if (roleEP == null)
		roleEP = new MutableLocalEntityProvider<Role>(Role.class,
				getNewEntityManager());
		return roleEP;
	}

	public static MutableLocalEntityProvider<Saldo> getSaldoEP() {
		return getSaldoEP(getNewEntityManager());
	}
	
	public static MutableLocalEntityProvider<Saldo> getSaldoEP(EntityManager em) {
		saldoEP = new BatchableLocalEntityProvider<Saldo>(Saldo.class, em);
		return saldoEP;
	}

	public static MutableLocalEntityProvider<Usuario> getUsuarioEP(EntityManager em) {
		// if (usuarioEP == null)
		usuarioEP = new MutableLocalEntityProvider<Usuario>(Usuario.class,em);
		return usuarioEP;
	}

	public static MutableLocalEntityProvider<Usuario> getUsuarioEP() {
		return getUsuarioEP(getNewEntityManager());
	}


	public static MutableLocalEntityProvider<CentroCosto> getCentroCostoEP() {
		// if (usuarioEP == null)
		centroCostoEP = new MutableLocalEntityProvider<CentroCosto>(CentroCosto.class,
				getNewEntityManager());
		return centroCostoEP;
	}
	
	public static MutableLocalEntityProvider<Beneficiario> getBeneficiarioEP() {				
		return getBeneficiarioEP(getNewEntityManager());
	}
	
	public static MutableLocalEntityProvider<Beneficiario> getBeneficiarioEP(EntityManager em) {
		// if (usuarioEP == null)
		beneficiarioEP = new MutableLocalEntityProvider<Beneficiario>(Beneficiario.class, em);
		return beneficiarioEP;
	}
	

	public static MutableLocalEntityProvider<TipoDocumento> getTipoDocumentoEP() {
		// if (usuarioEP == null)
		tipoDocumentoEP = new MutableLocalEntityProvider<TipoDocumento>(TipoDocumento.class,
				getNewEntityManager());
		return tipoDocumentoEP;
	}

	public static MutableLocalEntityProvider<MesCerrado> getMesCerradoEP() {
		// if (usuarioEP == null)
		mesCerradoEP = new MutableLocalEntityProvider<MesCerrado>(MesCerrado.class,
				getNewEntityManager());
		return mesCerradoEP;
	}
	
	public static MutableLocalEntityProvider<LugarGasto> getLugarGastoEP() {
		// if (usuarioEP == null)
		lugarGastoEP = new MutableLocalEntityProvider<LugarGasto>(LugarGasto.class,
				getNewEntityManager());
		return lugarGastoEP;
	}
	
	public static MutableLocalEntityProvider<CuentaContable> getCuentaContableEP() {
		// if (usuarioEP == null)
		cuentaContableEP = new MutableLocalEntityProvider<CuentaContable>(CuentaContable.class,
				getNewEntityManager());
		return cuentaContableEP;
	}
	
	public static MutableLocalEntityProvider<RubroInstitucional> getRubroInstitucionalEP() {
		// if (usuarioEP == null)
		rubroInstitucionalEP = new MutableLocalEntityProvider<RubroInstitucional>(RubroInstitucional.class,
				getNewEntityManager());
		return rubroInstitucionalEP;
	}
	public static MutableLocalEntityProvider<RubroProyecto> getRubroProyectoEP() {
		// if (usuarioEP == null)
		rubroProyectoEP = new MutableLocalEntityProvider<RubroProyecto>(RubroProyecto.class,
				getNewEntityManager());
		return rubroProyectoEP;
	}
	
	public static Beneficiario getBeneficiarioPorNombre(String name) {
		return getBeneficiarioPorNombre(name, getEntityManager());
	}
		
	
	public static Beneficiario getBeneficiarioPorNombre(String name, EntityManager em) {
		Query q = em.createNamedQuery("Beneficiario.getBeneficiarioPorNombre");
		q.setParameter(1, name);
		q.setMaxResults(1);
		try {
			return (Beneficiario) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public static Banco getBancoPorNombre(String name, EntityManager em) {
		Query q = em.createNamedQuery("Banco.getBancoPorNombre");
		q.setParameter(1, name);
		q.setMaxResults(1);
		try {
			return (Banco) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public static Banco getBancoPorCuenta(Long cntaId, EntityManager em) {
		Query q = em.createNamedQuery("Banco.getBancoPorCuenta");
		q.setParameter(1, cntaId);
		q.setMaxResults(1);
		try {
			return (Banco) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	
	
	public static CentroCosto getCentroCostoPorNombre(String name, EntityManager em) {
		Query q = em.createNamedQuery("CentroCosto.getCentroCostoPorNombre");
		q.setParameter(1, name);
		q.setMaxResults(1);
		try {
			return (CentroCosto) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public static CentroCosto getCentroCostoPorCodigo(String codigo, EntityManager em) {
		Query q = em.createNamedQuery("CentroCosto.getCentroCostoPorCodigo");
		q.setParameter(1, codigo);
		q.setMaxResults(1);
		try {
			return (CentroCosto) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public static LugarGasto getLugarGastoPorCodigo(String codigo, EntityManager em) {
		Query q = em.createNamedQuery("LugarGasto.getLugarGastoPorCodigo");
		q.setParameter(1, codigo);
		q.setMaxResults(1);
		try {
			return (LugarGasto) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public static TipoDocumento getTipoDocumentoPorAbrev(String name, EntityManager em) {
		Query q = em.createNamedQuery("TipoDocumento.getTipoDocumentoPorAbrev");
		q.setParameter(1, name);
		q.setMaxResults(1);
		try {
			return (TipoDocumento) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public static TipoDocumento getTipoDocumentoPorNombre(String name, EntityManager em) {
		Query q = em.createNamedQuery("TipoDocumento.getTipoDocumentoPorNombre");
		q.setParameter(1, name);
		q.setMaxResults(1);
		try {
			return (TipoDocumento) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public static Cuenta getCuentaPorNumero(Long numero, EntityManager em) {
		Query q = em.createNamedQuery("Cuenta.getCuentaPorNumero");
		q.setParameter(1, numero);
		q.setMaxResults(1);
		try {
			return (Cuenta) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public static Date getUltimoMesCerrado() {
		return getUltimoMesCerrado(getEntityManager());
	}
	
	public static boolean isOperacionEditable(Long id) {
		if (ConfigurationUtil.getEntityManager().find(Operacion.class, id)!=null)
			return (getUltimoMesCerrado().before(ConfigurationUtil.getEntityManager().find(Operacion.class, id).getFecha()));
		else return true;
	}

	public static Date getUltimoMesCerrado(EntityManager em) {
		MesCerrado mc = null;
		Query q = em.createNamedQuery("MesCerrado.getMesCerradoUltimo");
		q.setMaxResults(1);
		try {
			mc = (MesCerrado)q.getSingleResult();
			return mc.getFecha();
		} catch (NoResultException e) {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HH:mm:ss:SSS");
			try {
				return format.parse("20100101-00:00:00:000");
								
			} catch (ParseException pe) {
				Log.info("ERROR getUltimoMesCerrado " + pe.getMessage());
				return null;
			}
		}
	}
	
	public static String getProperty(String name, Window window) {
		String value = getProperty(name);
		if (value == null)
			window.showNotification(
					"Problema con la configuracion. Falta propiedad: " + name,
					Notification.TYPE_ERROR_MESSAGE);
		return value;
	}

	public static String getProperty(String name) {
		Propiedad prop = null;
		Query q = getEntityManager().createNamedQuery("Propiedad.getProperty");
		q.setParameter(1, name);
		try {
			prop = (Propiedad) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
		return prop.getValue();
	}
	
	public static void storeDefaultProperties() {
		for (String key : defaultParamMap.keySet()) {
			getPropiedadEP().addEntity(new Propiedad(key, defaultParamMap.get(key)));
		}		
	}

	public static void resetConfiguration() {
		paramMap.clear();
	}

	public static String get(String name) {
		if (paramMap.containsKey(name))
			return paramMap.get(name);
		else {
			String prop = getProperty(name);
			String param = (prop != null ? prop : getDefaultValue(name));
			paramMap.put(name, param);
			return param;
		}
	}

	public static Boolean is(String name) {
		if (get(name)!=null)
			return (get(name).equalsIgnoreCase("TRUE") || get(name)
				.equalsIgnoreCase("1"));
		else
			return false;
	}

	public static Date getBeginningOfMonth(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM-ddHH:mm:ss");
		try {
			String d = format.format(date);
			d = d.substring(0, 6);
			d += "-" + "0100:00:00";
			return format.parse(d);			
		} catch (ParseException pe) {
			pe.printStackTrace();
			return date;
		}
	}

	public static Date getBeginningOfDay(Date date) {
		return getTimeOfDay(date, "00:00:00");
	}

	public static Date getEndOfDay(Date date) {
		return getTimeOfDay(date, "23:59:59");
	}

	public static Date getTimeOfDay(Date date, String hourMinutes) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
		try {
			String d = format.format(date);
			d = d.substring(0, 8);
			d += "-" + hourMinutes;
			return format.parse(d);
		} catch (ParseException pe) {
			pe.printStackTrace();
			return date;
		}
	}

	public static Date dateAddDays(Date d, int days) {
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d);
		c1.add(Calendar.DAY_OF_YEAR, days);
		return c1.getTime();
	}

	public static Date dateAddSeconds(Date d, int secs) {
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d);
		c1.add(Calendar.SECOND, secs);
		return c1.getTime();
	}
	
	public static Date dateAddMonths(Date d, int months) {
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d);
		c1.add(Calendar.MONTH, months);
		return c1.getTime();
	}
}

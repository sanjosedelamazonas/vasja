package org.sanjose.web.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.jasperreports.engine.util.JRLoader;

import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.sessions.UnitOfWorkImpl;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.sanjose.model.CategoriaCuenta;
import org.sanjose.model.Cuenta;
import org.sanjose.model.Operacion;
import org.sanjose.model.Saldo;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.util.DecimalPropertyFormatter;

import com.vaadin.Application;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Window;

public class ReportHelper {

	static Connection sqlConnection = null;
	static final Logger logger = Logger.getLogger(ReportHelper.class.getName());

	@SuppressWarnings("serial")
	public static void generateComprobante(final Operacion op, Window window) {
		final boolean isPdf = ConfigurationUtil.get("REPORTS_COMPROBANTE_TYPE")
				.equalsIgnoreCase("PDF");
		final boolean isTxt = ConfigurationUtil.get("REPORTS_COMPROBANTE_TYPE")
				.equalsIgnoreCase("TXT");
		final String REPORT = (isTxt ? "ComprobanteTxt" : "Comprobante");
		final Application app = window.getApplication();
		StreamResource.StreamSource source = new StreamResource.StreamSource() {
			@SuppressWarnings("unchecked")
			public InputStream getStream() {
				byte[] b = null;
				try {
					InputStream rep = loadReport(REPORT, app);
					if (rep != null) {
						JasperReport report = (JasperReport) JRLoader
								.loadObject(rep);
						report.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
						@SuppressWarnings("rawtypes")
						HashMap paramMap = new HashMap();
						paramMap.put("REPORT_LOCALE", ConfigurationUtil.LOCALE);
						paramMap.put("OP_ID", op.getId());
						DecimalPropertyFormatter dpf = new DecimalPropertyFormatter(
								null, ConfigurationUtil.get("DECIMAL_FORMAT"));
						paramMap.put(
								"OP_AMOUNT",
								(op.getIsPen() ? dpf.format(op.getPen()) : dpf
										.format(op.getUsd())));
						if (isPdf)
							b = JasperRunManager.runReportToPdf(report,
									paramMap, getSqlConnection());
						else if (isTxt) {
							return exportToTxt(REPORT, paramMap);
						}
						else {
							return exportToHtml(REPORT, paramMap);
						}
					} else {
						app.getMainWindow().showNotification(
								"There is no report file!");
					}
				} catch (JRException ex) {
					logger.log(Level.SEVERE, null, ex);

				}
				return new ByteArrayInputStream(b);
			}
		};
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		StreamResource resource = new StreamResource(source,
				(op.getIsCargo() ? "Cargo_" : "Abono_")
						+ op.getCuenta().getId() + "_" + op.getId() + "_"
						+ df.format(new Date(System.currentTimeMillis()))
						+ (isPdf ? ".pdf" : (isTxt ? ".txt" : ".html")), app);
		resource.setMIMEType((isPdf ? "application/pdf" : (isTxt ? "text/plain" : "text/html")));

		logger.info("Resource: " + resource.getFilename() + " "
				+ resource.toString());

		window.open(resource, // URL
				"_blank", // window name
				500, // width
				650, // weight
				Window.BORDER_DEFAULT // decorations
		);
		//Set<Window> windows = window.getChildWindows();
		/*for (Window win : windows) {
			logger.info("URL: " + win.getURL());
			win.executeJavaScript("window.onload = function() { window.print(); } ");
		}*/
	}
	
	@SuppressWarnings({ "serial", "unchecked" })
	public static JasperPrint printComprobante(final Operacion op, Window window) {
		final boolean isTxt = ConfigurationUtil.get("REPORTS_COMPROBANTE_TYPE")
				.equalsIgnoreCase("TXT");
		final String REPORT = (isTxt ? "ComprobanteTxt" : "Comprobante");
			try {
					InputStream rep = loadReport(REPORT, null);
					if (rep != null) {
						JasperReport report = (JasperReport) JRLoader
								.loadObject(rep);
						report.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
						@SuppressWarnings("rawtypes")
						HashMap paramMap = new HashMap();
						paramMap.put("REPORT_LOCALE", ConfigurationUtil.LOCALE);
						paramMap.put("OP_ID", op.getId());
						
						DecimalPropertyFormatter dpf = new DecimalPropertyFormatter(
								null, ConfigurationUtil.get("DECIMAL_FORMAT"));
						paramMap.put(
								"OP_AMOUNT",
								(op.getIsPen() ? dpf.format(op.getPen()) : dpf
										.format(op.getUsd())));
						return prepareToPrint(REPORT, paramMap);
					} else {
						logger.warning("There is no report file!");
					}
				} catch (JRException ex) {
					logger.log(Level.SEVERE, null, ex);
				}
			return null;
	}

	@SuppressWarnings("serial")
	public static void generateReporteCuenta(final Date fechaMin, final Date fechaMax, 
			Cuenta cuenta, Set<Object> cuentaIds, Window window) {
		HashMap paramMap = new HashMap();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
		paramMap.put("REPORT_LOCALE", ConfigurationUtil.LOCALE);
		paramMap.put("STR_FECHA_MIN", sdf.format(ConfigurationUtil.getBeginningOfDay(fechaMin)));
		paramMap.put("STR_FECHA_MAX", sdf.format(ConfigurationUtil.getEndOfDay(fechaMax)));
		paramMap.put("FECHA_MIN", fechaMin);
		paramMap.put("FECHA_MAX", fechaMax);
		if (cuenta!=null) {
			paramMap.put("CUENTA_ID", cuenta.getId());
			paramMap.put("CUENTA_NUMERO", cuenta.getNumero());
			paramMap.put("CUENTA_NOMBRE", cuenta.getNombre());
			paramMap.put("CATEGORIA_NOMBRE", cuenta.getCategoriaCuenta().getNombre());
			generateReport("ReporteCuentaMultiHorizontal", "REPORTS_CUENTA_TYPE", paramMap, window, null);	
		}
		if (cuentaIds!=null) {
			paramMap.put("CUENTA_IDS", cuentaIds);		
			generateReport("ReporteCuentaMultiHorizontal", "REPORTS_CUENTA_TYPE", paramMap, window, null);
		}
	}
	
	public static void generateDiarioBanco(final Date fechaMin, final Date fechaMax,
			boolean isPen, Window window, String format, String type) {
		if (type.equals("Detallado")) 
		generateDiario("ReporteLibroDeBancosDetallado", ConfigurationUtil.getBeginningOfDay(fechaMin), 
				ConfigurationUtil.getEndOfDay(fechaMax), isPen, window, format, 
				ConfigurationUtil.get("REPORTE_BANCOS_PREPARADO_POR"),
				ConfigurationUtil.get("REPORTE_BANCOS_REVISADOR_POR"));
		else 
			generateDiario("ReporteLibroDeBancos", ConfigurationUtil.getBeginningOfDay(fechaMin), 
					ConfigurationUtil.getEndOfDay(fechaMax), isPen, window, format, 
					ConfigurationUtil.get("REPORTE_BANCOS_PREPARADO_POR"),
					ConfigurationUtil.get("REPORTE_BANCOS_REVISADOR_POR"));
	}

	
	public static void generateLugarGasto(final Date fechaMin, final Date fechaMax,
			 Window window, String format, String type) {
		
		String reportName="ReporteLugarYFuenteDeFinancDetallado";
		
		if (type.equals("Detallado")) reportName="ReporteLugarYFuenteDeFinancDetallado";						
		else 
			if (type.equals("Sin lugar")) reportName="ReporteLugarYFuenteDeFinancNoTienen";
		else 
			if (type.equals("Informe")) reportName="ReporteLugarYFuenteDeFinancInforme";
		else 
			if (type.equals("DetalladoCuentaContable")) reportName="ReporteDetalladoCuentaContable";
		else 
			if (type.equals("Detallado por rubro institucional")) reportName="ReporteDetalladoRubroInstitucional";
		else 
			if (type.equals("Informe resumido por cuenta contable")) reportName="ReporteResumidoCuentaContable";
		else 
			if (type.equals("Informe resumido por rubro institucional")) reportName="ReporteResumidoRubroInstitucional";
		
		generateLG(fechaMin, fechaMax, window,format, reportName);
	}
	
	public static void generateDiarioBancoCategoria(final Date fechaMin, final Date fechaMax,
			boolean isPen, Window window, String format) {
		generateDiario("ReporteBancoCategoria", ConfigurationUtil.getBeginningOfDay(fechaMin), 
				ConfigurationUtil.getEndOfDay(fechaMax), isPen, window, format,ConfigurationUtil.get("REPORTE_BANCOS_PREPARADO_POR"),ConfigurationUtil.get("REPORTE_BANCOS_REVISADOR_POR"));
	}
	
	public static void generateDiarioBancoBancos(final Date fechaMin, final Date fechaMax,
			boolean isPen, Window window, String format) {
		generateDiario("ReporteBancoBancos", ConfigurationUtil.getBeginningOfDay(fechaMin), 
				ConfigurationUtil.getEndOfDay(fechaMax), isPen, window, format,ConfigurationUtil.get("REPORTE_BANCOS_PREPARADO_POR"),ConfigurationUtil.get("REPORTE_BANCOS_REVISADOR_POR"));
	}
	
	public static void generateDiarioCaja(final Date fechaMin, Date fechaMax,
			boolean isPen, Window window, String format) {
		if (fechaMax==null) fechaMax = fechaMin;
		generateDiario("ReporteCajaDiario", 
/*ab*/			ConfigurationUtil.getBeginningOfDay(fechaMin),
				ConfigurationUtil.getEndOfDay(fechaMax), 
				isPen, window, format,
				ConfigurationUtil.get("REPORTE_CAJA_PREPARADO_POR"),
				ConfigurationUtil.get("REPORTE_CAJA_REVISADOR_POR"));
	}

	public static void generateDiario(String reportName, final Date fechaMin, final Date fechaMax,
			boolean isPen, Window window, String format, String preparado, String revisado) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
		HashMap paramMap = new HashMap();
		Operacion operSaldoTotal = ConfigurationUtil.getSaldoTotalPorFecha(fechaMin);
		if (fechaMax==null) ConfigurationUtil.calculateSaldosDiario(fechaMin, ConfigurationUtil.getEndOfDay(fechaMin));
		else ConfigurationUtil.calculateSaldosDiario(fechaMin, fechaMax);
		paramMap.put("REPORT_LOCALE", ConfigurationUtil.LOCALE);
		paramMap.put("SALDO_INICIAL", (isPen ? operSaldoTotal.getSaldoPen() : operSaldoTotal.getSaldoUsd()));
		paramMap.put("DIARIO_FECHA_MIN", fechaMin);
		paramMap.put("DIARIO_FECHA_MAX", fechaMax);
		paramMap.put("DIARIO_ISPEN", isPen);
		paramMap.put("SUBREPORT_DIR", ConfigurationUtil.get("REPORTS_SOURCE_FOLDER"));
		paramMap.put("STR_FECHA_MIN", sdf.format(ConfigurationUtil.getBeginningOfDay(fechaMin)));
		if (fechaMax!=null) paramMap.put("STR_FECHA_MAX", sdf.format(ConfigurationUtil.getEndOfDay(fechaMax)));
		paramMap.put("REPORTE_PREPARADO_POR", preparado);
		paramMap.put("REPORTE_REVISADOR_POR", revisado);
		logger.info("ParamMap: " + paramMap.toString());
		generateReport(reportName, "REPORTS_DIARIO_CAJA_TYPE", paramMap, window, format);
	}
	public static void generateCC(final Date minfecha, final Date maxfecha,boolean isPen, Window window, String format, String grouping) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
		logger.info("jestem w generateRep. params: "+minfecha+" maxfecha "+maxfecha+" isPen "+isPen);
		String reportName;
		HashMap paramMap = new HashMap();
		paramMap.put("REPORT_LOCALE", ConfigurationUtil.LOCALE);
		paramMap.put("in_isPen", isPen);
		paramMap.put("str_fecha_min", sdf.format(ConfigurationUtil.getBeginningOfDay(minfecha)));
		paramMap.put("SUBREPORT_DIR", ConfigurationUtil.get("REPORTS_SOURCE_FOLDER"));
		if (maxfecha!=null) paramMap.put("str_fecha_max", sdf.format(ConfigurationUtil.getEndOfDay(maxfecha)));
		if (grouping==null || grouping.equals("NO GROUPING")) 	reportName="ReporteCentroDeCostosNoGrouping";						
		else if (grouping.equals("POR CATEGORIA")) reportName="ReporteCentroDeCostosPorCategoria";
				else reportName="ReporteCentroDeCostosPorCuenta";
		logger.info("ParamMap: " + paramMap.toString());
		generateReport(reportName, "REPORTS_DIARIO_CAJA_TYPE", paramMap, window, format);
//		generateReport("ReporteCentroDeCostosStructure", "REPORTS_DIARIO_CAJA_TYPE", paramMap, window, format);
	}
	public static void generateLG(final Date minfecha, final Date maxfecha, Window window, String format, String reportName) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
		logger.info("jestem w generateLG. params: "+minfecha+" maxfecha "+maxfecha);
		HashMap paramMap = new HashMap();
		paramMap.put("REPORT_LOCALE", ConfigurationUtil.LOCALE);
		paramMap.put("str_fecha_min", sdf.format(ConfigurationUtil.getBeginningOfDay(minfecha)));
		paramMap.put("SUBREPORT_DIR", ConfigurationUtil.get("REPORTS_SOURCE_FOLDER"));
		if (maxfecha!=null) 
			paramMap.put("str_fecha_max", sdf.format(ConfigurationUtil.getEndOfDay(maxfecha)));
		else 
			paramMap.put("str_fecha_max", sdf.format(ConfigurationUtil.getEndOfDay(minfecha)));
		logger.info("ParamMap: " + paramMap.toString());
		generateReport(reportName, "REPORTS_DIARIO_CAJA_TYPE", paramMap, window, format);
//		generateReport("ReporteCentroDeCostosStructure", "REPORTS_DIARIO_CAJA_TYPE", paramMap, window, format);
	}
	
	public static void generateCuentasMensual(final Date fecha, CategoriaCuenta categoria, Window window, String format) {
		logger.info("Fecha: " + fecha);
		ConfigurationUtil.cuentas_mensual(fecha, categoria.getId());
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", ConfigurationUtil.LOCALE);
		HashMap paramMap = new HashMap();
		paramMap.put("REPORT_LOCALE", ConfigurationUtil.LOCALE);
		paramMap.put("STR_FECHA", sdf.format(fecha).toUpperCase());
		paramMap.put("CATEGORIA_NOMBRE", categoria.getNombre());
		logger.info("ParamMap: " + paramMap.toString());				
		generateReport("ReporteCuentasMensualHorizontal", "REPORTS_DIARIO_CAJA_TYPE", paramMap, window, format);
	}
	
	
	@SuppressWarnings({ "serial", "unchecked" })
	public static JasperPrint printDiario(final Date fechaMin, final Date fechaMax,
			boolean isPen) {
		final String REPORT = "ReporteDiario" + (isPen ? "PEN" : "USD");
			try {
					InputStream rep = loadReport(REPORT, null);
					if (rep != null) {
						JasperReport report = (JasperReport) JRLoader
								.loadObject(rep);
						report.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
						@SuppressWarnings("rawtypes")
						HashMap paramMap = new HashMap();
						paramMap.put("REPORT_LOCALE", ConfigurationUtil.LOCALE);
						paramMap.put("DIARIO_FECHA_MIN", fechaMin);
						if (fechaMax != null)
							paramMap.put("DIARIO_FECHA_MAX", fechaMax);
						return prepareToPrint(REPORT, paramMap);
					} else {
						logger.warning("There is no report file!");
					}
				} catch (JRException ex) {
					logger.log(Level.SEVERE, null, ex);
				}
			return null;
	}

	

	private static void generateReport(final String reportName, String typeParamName, 
			final HashMap paramMap, Window window, final String inFormat) {
		final String format;
		if (inFormat==null) format = ConfigurationUtil.get(typeParamName);
		else format = inFormat;
		final Application app = (window!=null ? window.getApplication() : null);
		StreamResource.StreamSource source = new StreamResource.StreamSource() {
			@SuppressWarnings("unchecked")
			public InputStream getStream() {
				byte[] b = null;
				try {
					InputStream rep = loadReport(reportName, app);
					if (rep != null) {
						JasperReport report = (JasperReport) JRLoader
								.loadObject(rep);
						report.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
						if (format.equalsIgnoreCase("pdf"))
							b = JasperRunManager.runReportToPdf(report,
									paramMap, getSqlConnection());
						else if (format.equalsIgnoreCase("html"))
							return exportToHtml(reportName, paramMap);
						else 
							return exportToXls(reportName, paramMap);
					} else {
						app.getMainWindow().showNotification(
								"There is no report file!");
					}
				} catch (JRException ex) {
					logger.severe(ex.getMessage());
					ex.printStackTrace();
				}
				return new ByteArrayInputStream(b);
			}
		};
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
		StreamResource resource = new StreamResource(source, reportName + "_"
				+ df.format(new Date()) + "." + format.toLowerCase(), app);
		if (format.equalsIgnoreCase("PDF"))
			resource.setMIMEType("application/pdf");
		else if (format.equalsIgnoreCase("XLS"))
			resource.setMIMEType("application/xls");
		else 
			resource.setMIMEType("text/html");
		window.open(resource, // URL
				"_blank", // window name
				700, // width
				600, // weight
				Window.BORDER_MINIMAL // decorations
		);
		window.setScrollable(true);
	}	
	
	
	private static InputStream loadReport(String reportName, Application app) {
		InputStream rep = null;
		if (app != null)
			rep = (app.getClass()).getResourceAsStream(ConfigurationUtil
					.get("REPORTS_SOURCE_URL") + reportName + ".jasper");
		if (rep == null) {
			logger.info("Loading report " + reportName + " from file");
			try {
				logger.info("Reports folder: " + ConfigurationUtil.get("REPORTS_SOURCE_FOLDER"));
				rep = new FileInputStream(
						ConfigurationUtil.get("REPORTS_SOURCE_FOLDER")
								+ reportName + ".jasper");
			} catch (FileNotFoundException e) {
				app.getMainWindow().showNotification("Report file not found!");
			}
		}
		return rep;
	}

	@SuppressWarnings("rawtypes")
	private static ByteArrayInputStream exportToHtml(String reportName,
			HashMap paramMap) throws JRException {
		@SuppressWarnings("unchecked")
		JasperPrint jasperPrint = JasperFillManager.fillReport(
				ConfigurationUtil.get("REPORTS_SOURCE_FOLDER") + reportName
						+ ".jasper", paramMap, getSqlConnection());
		JRHtmlExporter htmlExporter = new JRHtmlExporter();
		ByteArrayOutputStream oStream = new ByteArrayOutputStream();

		htmlExporter
				.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		htmlExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, oStream);

		htmlExporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,
				ConfigurationUtil.get("REPORTS_IMAGE_SERVLET"));
		htmlExporter.exportReport();

		return new ByteArrayInputStream(oStream.toByteArray());
	}

	@SuppressWarnings("rawtypes")
	private static ByteArrayInputStream exportToXls(String reportName,
			HashMap paramMap) throws JRException {
		@SuppressWarnings("unchecked")
		JasperPrint jasperPrint = JasperFillManager.fillReport(
				ConfigurationUtil.get("REPORTS_SOURCE_FOLDER") + reportName
						+ ".jasper", paramMap, getSqlConnection());
		JRXlsExporter xlsExporter = new JRXlsExporter();
		ByteArrayOutputStream oStream = new ByteArrayOutputStream();

		xlsExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, oStream);
		xlsExporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint); 
		xlsExporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, oStream); 
		xlsExporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE); 
		xlsExporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE); 
		xlsExporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE); 
		xlsExporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE); 
		xlsExporter.exportReport();

		return new ByteArrayInputStream(oStream.toByteArray());
	}
	
	@SuppressWarnings("rawtypes")
	private static ByteArrayInputStream exportToTxt(String reportName,
			HashMap paramMap) throws JRException {
		@SuppressWarnings("unchecked")
		JasperPrint jasperPrint = JasperFillManager.fillReport(
				ConfigurationUtil.get("REPORTS_SOURCE_FOLDER") + reportName
						+ ".jasper", paramMap, getSqlConnection());
		JRTextExporter txtExporter = new JRTextExporter();
		ByteArrayOutputStream oStream = new ByteArrayOutputStream();

		txtExporter
				.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		txtExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, oStream);
		txtExporter.setParameter(JRTextExporterParameter.CHARACTER_ENCODING, "ISO-8859-1");

		txtExporter.exportReport();

		return new ByteArrayInputStream(oStream.toByteArray());
	}
	
	@SuppressWarnings({ "rawtypes" })
	private static JasperPrint prepareToPrint(String reportName,
			HashMap paramMap) throws JRException {
		return JasperFillManager.fillReport(
				ConfigurationUtil.get("REPORTS_SOURCE_FOLDER") + reportName
						+ ".jasper", paramMap, getSqlConnection());
	}

	public static Connection getSqlConnection() {
		try {
			if (sqlConnection != null && sqlConnection.isValid(100))
				return sqlConnection;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		UnitOfWork unitOfWork = (UnitOfWork) ((JpaEntityManager) ConfigurationUtil
				.getEntityManager().getDelegate()).getActiveSession();
		unitOfWork.beginEarlyTransaction();
		Accessor accessor = ((UnitOfWorkImpl) unitOfWork).getAccessor();
		accessor.incrementCallCount(unitOfWork.getParent());
		accessor.decrementCallCount();
		sqlConnection = accessor.getConnection();
		return sqlConnection;
	}
}

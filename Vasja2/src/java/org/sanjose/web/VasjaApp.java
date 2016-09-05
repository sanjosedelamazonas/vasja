package org.sanjose.web;

import java.util.Date;
import java.util.logging.Logger;

import org.sanjose.model.Cuenta;
import org.sanjose.model.Operacion;
import org.sanjose.model.Usuario;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.util.ExtendedJPAContainer;
import org.sanjose.util.LogoutEvent;
import org.sanjose.util.LogoutListener;
import org.sanjose.web.accountant.AccountantCajaTable;
import org.sanjose.web.accountant.AccountantOperacionTable;
import org.sanjose.web.helper.PrintHelper;
import org.sanjose.web.helper.SessionHandler;
import org.sanjose.web.layout.LoginLayout;

import com.vaadin.Application;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

public class VasjaApp extends Application implements TabSheet.SelectedTabChangeListener, LogoutListener {

	private LoginLayout loginLayout;
	@SuppressWarnings("deprecation")
	private static final Date DATE = new Date(2009 - 1900, 6 - 1, 2);

	private Window main;
	private VerticalLayout mainLayout;
	private TabSheet tabs;
	private HorizontalLayout printTitleLayout;
	private ProgressIndicator printerLoading;
	private Label printerIcon = new Label("");
	
	private PrintHelper printHelper = null;
	static final Logger logger = Logger
			.getLogger(VasjaApp.class.getName());
	
	private ExtendedJPAContainer<Cuenta> cuentaContainer;

	private ExtendedJPAContainer<Operacion> operacionContainer;
	
	public ExtendedJPAContainer<Cuenta> getCuentaContainer() {
		return cuentaContainer;
	}

	public void setCuentaContainer(ExtendedJPAContainer<Cuenta> cuentaContainer) {
		this.cuentaContainer = cuentaContainer;
	}

	public ExtendedJPAContainer<Operacion> getOperacionContainer() {
		return operacionContainer;
	}

	public void setOperacionContainer(
			ExtendedJPAContainer<Operacion> operacionContainer) {
		this.operacionContainer = operacionContainer;
	}

	public PrintHelper getPrintHelper() {
		return printHelper;
	}

	public void printerLoaded() {
		if (printerLoading!=null) printerLoading.setEnabled(false);
		if (printerIcon!=null) {
			printerIcon.setVisible(true);
			//printerIcon.setValue(null);
			printerIcon.setHeight("16px");
			printerIcon.setWidth("16px");
		}
		printTitleLayout.requestRepaint();
	}
	
	@Override
	public void init() {
		setTheme(ConfigurationUtil.get("THEME"));
		main = new Window("Vicariato San Jose Del Amazonas");
		mainLayout = (VerticalLayout) main.getContent();
		mainLayout.setMargin(false);
		setMainWindow(main);
		setLocale(ConfigurationUtil.LOCALE);
		SessionHandler.initialize(this);
		SessionHandler.addListener(this);
		Long userId = Long.parseLong(ConfigurationUtil.get("DEV_MODE"));		
		if (userId!=null && userId >0) {
			Usuario loggedUser = ConfigurationUtil.getEntityManager().find(Usuario.class, userId);        
			SessionHandler.setUsuario(loggedUser);
			buildMainView();
		}
		else 
			loginForm();
			
	}

	private void loginForm() {
		getMainWindow().removeAllComponents();
		loginLayout = new LoginLayout();
		loginLayout.getVasjaLoginForm_1().setVasjaApp(this); 
		getMainWindow().addComponent(loginLayout);		
	}

	public void homeWindow() {
		getMainWindow().removeComponent(loginLayout);
		buildMainView();
	}

	void buildMainView() {
		mainLayout.setSizeFull();
		//mainLayout.addComponent(getTopMenu());
		printHelper = new PrintHelper(this);
		mainLayout.addComponent(getHeader());

		CssLayout margin = new CssLayout();
		margin.setMargin(false, true, true, true);
		margin.setSizeFull();
		
				

		if (SessionHandler.isContador()) {
			tabs = new TabSheet();
			tabs.setSizeFull();
			margin.addComponent(tabs);
			tabs.addComponent(new OperacionTable(getMainWindow()));
			tabs.addComponent(new AccountantCajaTable(getMainWindow()));
			tabs.addComponent(new ReportesTabsheet());
//			tabs.addComponent(new MesCerradoTable(getMainWindow()));
			tabs.addComponent(new BeneficiarioTable(getMainWindow()));
			tabs.addComponent(new CuentaTable(getMainWindow()));			
			tabs.addComponent(new PropietarioTable(getMainWindow()));
			tabs.addComponent(new ImportTabsheet());
		}	
		if (SessionHandler.isAdmin()) {
			tabs.addComponent(new CentroCostoTable(getMainWindow()));
			tabs.addComponent(new LugarGastoTable(getMainWindow()));
			tabs.addComponent(new CuentaContableTable(getMainWindow()));
			tabs.addComponent(new RubroProyectoTable(getMainWindow()));
			tabs.addComponent(new RubroInstitucionalTable(getMainWindow()));						
			tabs.addComponent(new CategoriaCuentaTable(getMainWindow()));
			tabs.addComponent(new BancoTable(getMainWindow()));
			tabs.addComponent(new UsuarioTable(getMainWindow()));
			tabs.addComponent(new TipoDocumentoTable(getMainWindow()));
			if (ConfigurationUtil.is("PRINTER_LIST_SHOW")) tabs.addComponent(printHelper);
			tabs.addComponent(new PropiedadTable(getMainWindow()));
			tabs.addListener(this);
		} 
		if (!SessionHandler.isContador()) {
			margin.addComponent(new AccountantOperacionTable(getMainWindow()));			
		}
		mainLayout.addComponent(margin);
		mainLayout.setExpandRatio(margin, 1);
		
	}
			
	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {
		String c = tabs.getTab(event.getTabSheet().getSelectedTab()).getCaption();
		if (c.equals("Operaciones")) {
			logger.info("Selected operaciones");
			OperacionTable ot = ((OperacionTable)(tabs.getTab(event.getTabSheet().getSelectedTab()).getComponent()));
			ot.redraw();
		} else if (c.equals("Cuentas")) {
			logger.info("Selected cuentas");
			CuentaTable ct = ((CuentaTable)(tabs.getTab(event.getTabSheet().getSelectedTab()).getComponent()));
			ct.redraw();
		} else if (c.equals("Caja")) {
			logger.info("Selected caja");
			AccountantCajaTable at = ((AccountantCajaTable)(tabs.getTab(event.getTabSheet().getSelectedTab()).getComponent()));
			at.redraw(null, true, false, false);
		} 
        		
	}
	
	Layout getHeader() {
		HorizontalLayout header = new HorizontalLayout();
		header.setWidth("100%");
		header.setMargin(true);
		header.setSpacing(true);
		// header.setStyleName(Reindeer.LAYOUT_BLACK);

		CssLayout titleLayout = new CssLayout();
		H2 title = new H2("Vicariato San Jose Del Amazonas");
		titleLayout.addComponent(title);
		SmallText description = new SmallText(
				"Financas de Terceros, VASJA y Proyectos");
		description.setSizeUndefined();
		//titleLayout.addComponent(description);

		header.addComponent(titleLayout);

		HorizontalLayout toggles = new HorizontalLayout();
		toggles.setSpacing(true);
		header.addComponent(toggles);
		header.setComponentAlignment(toggles, Alignment.MIDDLE_LEFT);

		printTitleLayout = new HorizontalLayout();
		printTitleLayout.setSpacing(true);
		Label user = new Label("Bienvenido " + SessionHandler.get().getNombre() + " " + SessionHandler.get().getApellido());
		user.setSizeUndefined();
		printTitleLayout.addComponent(user);
		//printTitleLayout.addComponent(new Label(""));

		//HorizontalLayout buttons = new HorizontalLayout();
		//printTitleLayout.setSpacing(true);
		Button help = new Button("Ayuda", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				openHelpWindow();
			}
		});
		//help.setStyleName(Reindeer.BUTTON_SMALL);
		// Add PrintHelper so that the applet is initialized
		if (ConfigurationUtil.is("REPORTS_COMPROBANTE_PRINT")) {
			if (!ConfigurationUtil.is("PRINTER_LIST_SHOW")) printTitleLayout.addComponent(printHelper);
			Label l = new Label("");
			printerLoading = new ProgressIndicator();
			printerLoading.setIndeterminate(true);
			printerLoading.setPollingInterval(3000);
			printerLoading.setEnabled(true);
			printerIcon.setIcon(new ThemeResource("../icons/printer-icon.png"));
			printerIcon.setVisible(false);
			printerIcon.setImmediate(true);
			//printerIcon.setValue("");
			printTitleLayout.addComponent(printerLoading);
			printTitleLayout.addComponent(printerIcon);
		}

		Button logout = new Button("Logout", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				openLogoutWindow();
			}
		});
		logout.setStyleName(Reindeer.BUTTON_SMALL);
		printTitleLayout.addComponent(logout);

		header.addComponent(printTitleLayout);
		header.setComponentAlignment(printTitleLayout, Alignment.TOP_RIGHT);

		return header;
	}

	Window help = new Window("Ayuda");

	void openHelpWindow() {
		if (!"initialized".equals(help.getData())) {
			help.setData("initialized");
			help.setCloseShortcut(KeyCode.ESCAPE, null);

			help.center();
			// help.setStyleName(Reindeer.WINDOW_LIGHT);
			help.setWidth("400px");
			help.setResizable(false);

			Label helpText = new Label(
					"<strong>How To Use This Application</strong><p>Click around, explore. The purpose of this app is to show you what is possible to achieve with the Reindeer theme and its different styles.</p><p>Most of the UI controls that are visible in this application don't actually do anything. They are purely for show, like the menu items and the components that demostrate the different style names assosiated with the components.</p><strong>So, What Then?</strong><p>Go and use the styles you see here in your own application and make them beautiful!",
					Label.CONTENT_XHTML);
			help.addComponent(helpText);

		}
		if (!getMainWindow().getChildWindows().contains(help)) {
			getMainWindow().addWindow(help);
		}
	}

	void openLogoutWindow() {
		Window logout = new Window("Logout");
		logout.setModal(true);
		logout.setStyleName(Reindeer.WINDOW_BLACK);
		logout.setWidth("260px");
		logout.setResizable(false);
		logout.setClosable(false);
		logout.setDraggable(false);
		logout.setCloseShortcut(KeyCode.ESCAPE, null);

		Label helpText = new Label(
				"Estas seguro que qieres abandonar el programa",
				Label.CONTENT_XHTML);
		logout.addComponent(helpText);

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		Button yes = new Button("Logout", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				getMainWindow().removeWindow(event.getButton().getWindow());	
				setOperacionContainer(null);
				setCuentaContainer(null);
				SessionHandler.logout();
			}
		});
		yes.setStyleName(Reindeer.BUTTON_DEFAULT);
		yes.focus();
		buttons.addComponent(yes);
		Button no = new Button("Regresar", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				getMainWindow().removeWindow(event.getButton().getWindow());
			}
		});
		buttons.addComponent(no);

		logout.addComponent(buttons);
		((VerticalLayout) logout.getContent()).setComponentAlignment(buttons,
				Alignment.TOP_CENTER);
		((VerticalLayout) logout.getContent()).setSpacing(true);

		getMainWindow().addWindow(logout);
	}
	
	@Override
	public void logout(LogoutEvent event) {
		getMainWindow().removeAllComponents();
		loginForm();
	}	

	public class H1 extends Label {
		public H1(String caption) {
			super(caption);
			setSizeUndefined();
			setStyleName(Reindeer.LABEL_H1);
		}
	}

	class H2 extends Label {
		public H2(String caption) {
			super(caption);
			setSizeUndefined();
			setStyleName(Reindeer.LABEL_H2);
		}
	}

	class SmallText extends Label {
		public SmallText(String caption) {
			super(caption);
			setStyleName(Reindeer.LABEL_SMALL);
		}
	}

	class Ruler extends Label {
		public Ruler() {
			super("<hr />", Label.CONTENT_XHTML);
		}
	}
}

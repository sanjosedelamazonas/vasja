package org.sanjose.web.accountant;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.sanjose.util.ConfigurationUtil;
import org.sanjose.web.helper.ReportHelper;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.InlineDateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ReporteDiarioLayout extends CustomComponent {

	@AutoGenerated
	private VerticalLayout mainLayout;
	@AutoGenerated
	private Button diarioButton;
	@AutoGenerated
	private HorizontalLayout horizontalLayout;
	@AutoGenerated
	private InlineDateField fechaMinDateField;
	@AutoGenerated
	private Label label;
	@AutoGenerated
	private Button diarioTodayButton;
	@AutoGenerated
	private OptionGroup monedaGroup;



	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	public Button getDiarioButton() {
		return diarioButton;
	}



	public void setDiarioButton(Button diarioButton) {
		this.diarioButton = diarioButton;
	}



	public InlineDateField getFechaMinDateField() {
		return fechaMinDateField;
	}



	public void setFechaMinDateField(InlineDateField fechaMinDateField) {
		this.fechaMinDateField = fechaMinDateField;
	}



	public Button getDiarioTodayButton() {
		return diarioTodayButton;
	}



	public void setDiarioTodayButton(Button diarioTodayButton) {
		this.diarioTodayButton = diarioTodayButton;
	}



	public OptionGroup getMonedaGroup() {
		return monedaGroup;
	}



	public void setMonedaGroup(OptionGroup monedaGroup) {
		this.monedaGroup = monedaGroup;
	}



	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public ReporteDiarioLayout() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// TODO add user code here
	}

	

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		
		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");
		
		// monedaGroup
		List<String> currencies = Arrays
				.asList(new String[] { "PEN", "USD" });
		monedaGroup = new OptionGroup("Seleccione la moneda", currencies);
		monedaGroup.setImmediate(false);
		monedaGroup.setWidth("-1px");
		monedaGroup.setHeight("-1px");
		monedaGroup.setRequired(true);
		monedaGroup.select("PEN");
		monedaGroup.setNullSelectionAllowed(false); // user can not 'unselect'
		mainLayout.addComponent(monedaGroup);
		mainLayout.setComponentAlignment(monedaGroup, new Alignment(48));
		
		// diarioTodayButton
		diarioTodayButton = new Button();
		diarioTodayButton.setCaption("Diario de Caja: hoy dia");
		diarioTodayButton.setImmediate(true);
		diarioTodayButton.setWidth("20.0em");
		diarioTodayButton.setHeight("-1px");
		diarioTodayButton.setIcon(new ThemeResource("../icons/clock_go.png"));
		diarioTodayButton.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				boolean isPen = (monedaGroup.getValue().toString().equalsIgnoreCase("PEN"));
				ReportHelper.generateDiarioCaja(new Date(), new Date(), isPen, getWindow().getParent(), null);
				getWindow().getParent().removeWindow(getWindow());
			}
		});		
		mainLayout.addComponent(diarioTodayButton);
		mainLayout.setComponentAlignment(diarioTodayButton, new Alignment(48));
		
		// label
		label = new Label();
		label.setImmediate(false);
		label.setWidth("-1px");
		label.setHeight("2.0em");
		label.setValue(" ");
		mainLayout.addComponent(label);
		mainLayout.setComponentAlignment(label, new Alignment(48));
		
		// horizontalLayout
		horizontalLayout = buildHorizontalLayout();
		mainLayout.addComponent(horizontalLayout);
		mainLayout.setComponentAlignment(horizontalLayout, new Alignment(48));
		
		// diarioButton
		diarioButton = new Button();
		diarioButton.setCaption("Diario de Caja: dia seleccionado");
		diarioButton.setImmediate(true);
		diarioButton.setWidth("20.0em");
		diarioButton.setHeight("-1px");
		diarioButton.setIcon(new ThemeResource("../icons/clock_ringing.png"));
		diarioButton.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (fechaMinDateField.getValue()!=null) {
					Date fecha = (Date)fechaMinDateField.getValue();
					boolean isPen = (monedaGroup.getValue().toString().equalsIgnoreCase("PEN"));			
					ReportHelper.generateDiarioCaja(fecha, fecha, isPen, getWindow().getParent(), null);
					getWindow().getParent().removeWindow(getWindow());
				}
			}
		});
		mainLayout.addComponent(diarioButton);
		mainLayout.setComponentAlignment(diarioButton, new Alignment(48));
		
		return mainLayout;
	}



	@AutoGenerated
	private HorizontalLayout buildHorizontalLayout() {
		// common part: create layout
		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setImmediate(false);
		horizontalLayout.setWidth("-1px");
		horizontalLayout.setHeight("-1px");
		horizontalLayout.setMargin(false);
		
		// fechaMinDateField
		fechaMinDateField = new InlineDateField();
		fechaMinDateField.setImmediate(false);
		fechaMinDateField.setWidth("-1px");
		fechaMinDateField.setHeight("-1px");
		fechaMinDateField.setInvalidAllowed(false);
		fechaMinDateField.setResolution(4);
		horizontalLayout.addComponent(fechaMinDateField);
		
		return horizontalLayout;
	}

}

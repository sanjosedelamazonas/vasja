package org.sanjose.web;

import java.util.HashSet;
import java.util.Set;

import org.sanjose.model.Usuario;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.util.ExtendedJPAContainer;
import org.sanjose.web.helper.SubwindowModal;
import org.sanjose.web.layout.ResizeTableLayout;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class UsuarioTable extends ResizeTableLayout {

	ExtendedJPAContainer<Usuario> container;
	MutableEntityProvider<Usuario> myEntityProvider;
	HashSet<Object> markedRows = new HashSet<Object>();

	static final Action ACTION_EDIT = new Action("Editar");
	static final Action ACTION_DEL = new Action("Eliminar");
	static final Action ACTION_NEW = new Action("Nuevo");
	static final Action[] ACTIONS = new Action[] { ACTION_EDIT, ACTION_NEW,
			ACTION_DEL };

	public UsuarioTable(Window w) {
    	super(w);
    	table = new Table("Usuarios");
    	addComponent(table);
	    setExpandRatio(table, 1f);
	    setResizeMargin(150);
	    setSizeFull();
	    setWidth("100.0%");
		setCaption("Usuario");
		// Label to indicate current selection
		final Label selected = new Label("No seleccion");
		// addComponent(selected);
		// set a style name, so we can style rows and cells
		table.setStyleName(ConfigurationUtil.get("CSS_STYLE"));
		// size
		table.setWidth("100%");
		// table.setHeight("500px");
		// selectable
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.setImmediate(true); // react at once when something is selected

		table.setContainerDataSource(null);
		container = new ExtendedJPAContainer<Usuario>(Usuario.class);
		MutableLocalEntityProvider<Usuario> myEntityProvider = ConfigurationUtil
				.getUsuarioEP();

		container.setEntityProvider(myEntityProvider);
		table.setContainerDataSource(container);
		container.addNestedContainerProperty("role.*");

		// turn on column reordering and collapsing
		table.setColumnReorderingAllowed(true);
		table.setColumnCollapsingAllowed(true);
		table.setVisibleColumns(new String[] { "id", "usuario", "nombre",
				"apellido", "correoElectronico", "role.nombre" });
		// set column headers
		table.setColumnHeaders(new String[] { "Id", "Usuario", "Nombre",
				"Apellido", "Correo Electronico", "Role" });

		// Column alignment
		table.setColumnAlignment("correoElectronico", Table.ALIGN_CENTER);

		final UsuarioTable ut = this;

		table.addActionHandler(new Action.Handler() {
			public Action[] getActions(Object target, Object sender) {
				return ACTIONS;
			}

			public void handleAction(Action action, Object sender, Object target) {
				if (ACTION_DEL == action) {
					Item item = table.getItem(target);
					final SubwindowModal sub = new SubwindowModal(
							"Eliminar Usuario");
					final EntityItem<Usuario> eItem = container
							.getItem((Long) item.getItemProperty("id")
									.getValue());
					Label message = new Label("?Eliminar el usuario "
							+ item.getItemProperty("usuario").getValue()
							+ "?");

					sub.getContent().addComponent(message);
					((VerticalLayout) sub.getContent()).setComponentAlignment(
							message, Alignment.MIDDLE_CENTER);
					HorizontalLayout hLayout = new HorizontalLayout();
					hLayout.setMargin(true);
					hLayout.setSpacing(true);

					Button yes = new Button("Si", new Button.ClickListener() {
						public void buttonClick(ClickEvent event) {
							container.removeItem(eItem.getItemId());
							(sub.getParent()).removeWindow(sub);
						}
					});
					Button no = new Button("No", new Button.ClickListener() {
						public void buttonClick(ClickEvent event) {
							(sub.getParent()).removeWindow(sub);
						}
					});
					hLayout.addComponent(yes);
					hLayout.addComponent(no);
					sub.getContent().addComponent(hLayout);
					((VerticalLayout) sub.getContent()).setComponentAlignment(
							hLayout, Alignment.MIDDLE_CENTER);
					getWindow().addWindow(sub);

				} else if (ACTION_EDIT == action) {
					Item item = table.getItem(target);
					SubwindowModal sub = new SubwindowModal("Editar usuario");
					EntityItem<Usuario> eItem = container.getItem((Long) item
							.getItemProperty("id").getValue());
					sub.getContent().addComponent(
							new UsuarioForm(eItem.getEntity(), ut));
					getWindow().addWindow(sub);
				} else if (ACTION_NEW == action) {
					SubwindowModal sub = new SubwindowModal("Nuevo usuario");
					sub.getContent().addComponent(new UsuarioForm(ut));
					getWindow().addWindow(sub);
				}
			}
		});

		table.addListener(new ItemClickListener() {
			public void itemClick(ItemClickEvent event) {
				if (event.isDoubleClick()) {
					Object itemId = event.getItemId();
					Usuario us = container.getItem(itemId).getEntity();
					SubwindowModal sub = new SubwindowModal("Editar usuario");
					sub.getContent().addComponent(new UsuarioForm(us, ut));
					getWindow().addWindow(sub);
					table.requestRepaint();
				}
			}
		});

		// listen for valueChange, a.k.a 'select' and update the label
		table.addListener(new Table.ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				// in multiselect mode, a Set of itemIds is returned,
				// in singleselect mode the itemId is returned directly
				// if (!(event.getProperty().getValue() instanceof User)) {
				Set<?> value = (Set<?>) event.getProperty().getValue();
				if (null == value || value.size() == 0) {
					selected.setValue("No seleccion");
				} else {
					selected.setValue("Selectado: " + table.getValue());
				}
			}
		});
	}
}

package org.sanjose.web;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.sanjose.model.Role;
import org.sanjose.model.Usuario;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.util.VasjaFieldFactory;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class UsuarioForm extends VerticalLayout {

	Usuario usuario;
	UsuarioTable usuarioTable;
	final Form usuarioForm = new Form();

	public UsuarioForm(Usuario user, UsuarioTable usuarioTable) {
		this.usuario = user;
		this.usuarioTable = usuarioTable;
		usuarioForm.setCaption("Editar detalles del usuario");
		buildForm();
	}

	public UsuarioForm() {
		usuario = new Usuario();
		usuarioForm.setCaption("Creacion del usuario");
		buildForm();
	}

	public UsuarioForm(UsuarioTable usuarioTable) {
		this();
		this.usuarioTable = usuarioTable;
	}

	private void buildForm() {
		setCaption("Usuario");
		final BeanItem<Usuario> userItem = new BeanItem<Usuario>(usuario); // item
																			// fromus;
		// Create the Form
		usuarioForm.setWriteThrough(false); // we want explicit 'apply'
		usuarioForm.setInvalidCommitted(false); // no invalid values in
												// datamodel
		// FieldFactory for customizing the fields and adding validators
		usuarioForm.setFormFieldFactory(new UserFieldFactory());
		usuarioForm.setItemDataSource(userItem); // bind to POJO via BeanItem
		// Determines which properties are shown, and in which order:
		usuarioForm.setVisibleItemProperties(Arrays.asList(new String[] {
				"usuario", "clave", "nombre", "apellido", "correoElectronico",
				"role" }));
		// Add form to layout
		addComponent(usuarioForm);
		// The cancel / apply buttons
		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);

		Button apply = new Button("Guardar", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				try {
					usuarioForm.commit();
					if (usuario.getId() != null) {
						Item item = usuarioTable.container.getItem(usuario
								.getId());
						usuarioTable.container.updateEntityItem(item, userItem);
						item.getItemProperty("role")
								.setValue(usuario.getRole());
					} else {
						EntityItem<Usuario> myNewItem = usuarioTable.container
								.createEntityItem(userItem.getBean());
						usuarioTable.container.addEntity(myNewItem.getEntity());
					}
					getWindow().getParent().removeWindow(getWindow());
				} catch (Exception e) {
					// Ignored, we'll let the Form handle the errors
				}
			}
		});
		buttons.addComponent(apply);
		usuarioForm.getFooter().addComponent(buttons);
		usuarioForm.getFooter().setMargin(false, false, true, true);

	}

	private class UserFieldFactory extends VasjaFieldFactory {

		final ComboBox roles = new ComboBox("Role");

		public UserFieldFactory() {
			roles.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
			IndexedContainer c = new IndexedContainer();
			c.addContainerProperty("nombre", String.class, "");

			//MutableLocalEntityProvider<Role> roleEntityProvider = ConfigurationUtil
			//		.getRoleEP();
    		JPAContainer jpaContainer = JPAContainerFactory.make(Role.class, ConfigurationUtil.getEntityManager());
    		Collection<Object> roleIds = jpaContainer.getItemIds();

			
			//List<Object> roleIds = roleEntityProvider.getAllEntityIdentifiers(
			//		null, null);
			for (Object roleId : roleIds) {
				Role role = (Role)jpaContainer.getItem(roleId).getEntity();
				Item item = c.addItem(role);
				item.getItemProperty("nombre").setValue(role.getNombre());
			}
			roles.setContainerDataSource(c);
			roles.setItemCaptionPropertyId("nombre");
			roles.setRequired(true);
			roles.setFilteringMode(ComboBox.FILTERINGMODE_STARTSWITH);
		}

		@Override
		public Field createField(Item item, Object propertyId,
				Component uiContext) {
			Field f;
			if ("role".equals(propertyId)) {
				return roles;
			} else if ("clave".equals(propertyId)) {
				f = createPasswordField(propertyId);
			} else {
				f = super.createField(item, propertyId, uiContext);
			}
			if ("nombre".equals(propertyId)) {
				TextField tf = (TextField) f;
				tf.setRequired(true);
				tf.setRequiredError("Por favor rellena el nombre");
				tf.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
				tf.addValidator(new StringLengthValidator(
						"El nombre debe tener 3-25 letras", 3, 25, false));
			} else if ("apellido".equals(propertyId)) {
				TextField tf = (TextField) f;
				tf.setRequired(true);
				tf.setRequiredError("Por favor rellena el apellido");
				tf.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
				tf.setImmediate(false);
				tf.addValidator(new StringLengthValidator(
						"El apellido debe tener 3-50 letras", 3, 50, false));
			} else if ("clave".equals(propertyId)) {
				PasswordField pf = (PasswordField) f;
				pf.setRequired(true);
				pf.setRequiredError("Por favor rellena la clave");
				pf.setWidth("10em");
				pf.addValidator(new StringLengthValidator(
						"La clave debe tener 6-20 letras", 6, 20, false));
			} else if ("usuario".equals(propertyId)) {
				TextField tf = (TextField) f;
				tf.setRequired(true);
				tf.setRequiredError("Por favor rellena el usuario");
				tf.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
				tf.addValidator(new StringLengthValidator(
						"El usuario debe tener 3-10 letras", 3, 10, false));
			} else if ("correoElectronico".equals(propertyId)) {
				TextField tf = (TextField) f;
				tf.setRequired(true);
				tf.setRequiredError("Por favor rellena el correo electronico");
				tf.setWidth(ConfigurationUtil.get("COMMON_FIELD_WIDTH"));
				tf.addValidator(new EmailValidator(
						"No es un correo electronico correcto"));
			}
			return f;
		}
	}
}
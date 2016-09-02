/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sanjose.web;

import java.util.Collection;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.sanjose.model.Role;
import org.sanjose.model.Usuario;
import org.sanjose.util.ConfigurationUtil;
import org.sanjose.web.helper.SessionHandler;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.LoginForm.LoginListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class VasjaLoginForm extends VerticalLayout {

	VasjaApp vapp;
	
	public VasjaLoginForm() {
		setSizeFull();
		setWidth("100%");
		LoginForm login = new LoginForm();
        login.setWidth("100%");
        login.setHeight("300px");
        login.setLocale(ConfigurationUtil.LOCALE);
        login.setLoginButtonCaption("Entrar");
        login.setPasswordCaption("Clave");
        login.setUsernameCaption("Usuario");
        login.addListener(new LoginListener() {
            public void onLogin(LoginEvent event) {
            	
                MutableLocalEntityProvider<Usuario> myEntityProvider = ConfigurationUtil.getUsuarioEP();
                Usuario usuario = null;
                Query q = myEntityProvider.getEntityManager().createNamedQuery("Usuario.getUsuarioPorUsuarioClave");
                q.setParameter(1, event.getLoginParameter("username"));
                q.setParameter(2,  event.getLoginParameter("password"));
                try {
                    usuario = (Usuario) q.getSingleResult();
            		SessionHandler.setUsuario(usuario);
            		vapp.homeWindow();
                } catch (NoResultException e) {
                    usuario = null;
                	// Check if first login (DB is empty)
                    
            		JPAContainer jpaContainer = JPAContainerFactory.make(Usuario.class, ConfigurationUtil.getEntityManager());
            		Collection<Object> userIds = jpaContainer.getItemIds();
            		// If it is empty create a default admin account.
                	if (userIds.isEmpty()) {
                		Role roleAdmin = ConfigurationUtil.getRoleEP().addEntity(new Role("Administrador"));
                		Role role = ConfigurationUtil.getRoleEP().addEntity(new Role("Contador"));
                		role = ConfigurationUtil.getRoleEP().addEntity(new Role("Cajero"));
                		Usuario us = new Usuario();
                		us.setUsuario("admin");
                		us.setClave("admin123");
                		us.setNombre("Default");
                		us.setApellido("Administrator");
                		us.setRole(roleAdmin);
                		myEntityProvider.addEntity(us);
                		// Initialize configuration properties
                		ConfigurationUtil.storeDefaultProperties();
                		getWindow().showNotification("La nueva base esta ahora inicializada, usa por favor el usuario/clave para entrar: admin/admin123", Notification.TYPE_HUMANIZED_MESSAGE);
                	} else {
                		getWindow().showNotification("Usuario o clave no existe o no esta correcto", Notification.TYPE_HUMANIZED_MESSAGE);	                		
                	}
                }                
            }
        });
        addComponent(login);    
        setComponentAlignment(login, Alignment.MIDDLE_CENTER);
      }
	
	public void setVasjaApp(VasjaApp ap) {
		this.vapp = ap;
	}
}

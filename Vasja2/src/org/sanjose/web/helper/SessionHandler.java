package org.sanjose.web.helper;

import java.util.ArrayList;
import java.util.List;

import org.sanjose.model.Usuario;
import org.sanjose.util.LogoutEvent;
import org.sanjose.util.LogoutListener;
import org.sanjose.web.VasjaApp;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext.TransactionListener;

/**
 * A utility class for handling usuario sessions
 * 
 * @author Kim
 * Usuario
 */
public class SessionHandler implements TransactionListener {

    private static final long serialVersionUID = 4142938996955537395L;

    private final Application application;

    private Usuario usuario;

    private List<LogoutListener> listeners = new ArrayList<LogoutListener>();

    // Store the usuario object of the currently inlogged usuario
    private static ThreadLocal<SessionHandler> instance = new ThreadLocal<SessionHandler>();

    /**
     * Constructor
     * 
     * @param application
     *            Current application instance
     */
    public SessionHandler(Application application) {
        this.application = application;
        instance.set(this);
    }

    /**
     * {@inheritDoc}
     */
    public void transactionEnd(Application application, Object transactionData) {
        // Clear the currentApplication field
        if (this.application == application) {
            instance.set(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void transactionStart(Application application, Object transactionData) {
        // Check if the application instance we got as parameter is actually
        // this application instance. If it is, then we should define the thread
        // local variable for this request.
        if (this.application == application) {
            // Set the current usuario
            instance.set(this);
        }
    }

    /**
     * Set the Usuario object for the currently inlogged usuario for this application
     * instance
     * 
     * @param usuario
     */
    public static void setUsuario(Usuario usuario) {
        instance.get().usuario = usuario;
    }

    /**
     * Get the Usuario object of the currently inlogged usuario for this application
     * instance.
     * 
     * @return The currently inlogged usuario
     */
    public static Usuario get() {
        return instance.get().usuario;
    }

    public static VasjaApp getVasjaApp() {
    	return (VasjaApp)instance.get().application;
    }
    
    /**
     * Check if user has adminstrative privileges
     * @return
     */
    public static boolean isAdmin() {
    	return (instance.get().usuario!=null ? 
    			instance.get().usuario.getRole().getNombre().equals("Administrador") : false);
    			
    }
    
    public static boolean isContador() {
    	return isAdmin() || (instance.get().usuario!=null ? 
    			instance.get().usuario.getRole().getNombre().equals("Contador") : false);
    			
    }
    
    /**
     * Method for logging out a usuario
     */
    public static void logout() {
        LogoutEvent event = new LogoutEvent(instance.get().usuario);
        setUsuario(null);
        dispatchLogoutEvent(event);
    }

    /**
     * Dispatches the {@link LogoutEvent} to all registered logout listeners.
     * 
     * @param event
     *            The LogoutEvent
     */
    private static void dispatchLogoutEvent(LogoutEvent event) {
        for (LogoutListener listener : instance.get().listeners) {
            listener.logout(event);
        }
    }

    /**
     * Initializes the {@link SessionHandler} for the given {@link Application}
     * 
     * @param application
     */
    public static void initialize(Application application) {
        if (application == null) {
            throw new IllegalArgumentException("Application may not be null");
        }
        SessionHandler handler = new SessionHandler(application);
        application.getContext().addTransactionListener(handler);
    }

    /**
     * Add a logout listener.
     * 
     * @param listener
     */
    public static void addListener(LogoutListener listener) {
        instance.get().listeners.add(listener);
    }

    /**
     * Remove the given listener from the active logout listeners.
     * 
     * @param listener
     */
    public static void removeListener(LogoutListener listener) {
        instance.get().listeners.remove(listener);
    }

}

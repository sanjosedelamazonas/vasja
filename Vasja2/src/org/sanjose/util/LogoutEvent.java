package org.sanjose.util;

import java.io.Serializable;

import org.sanjose.model.Usuario;

/**
 * Event which is dispatched when a usuario logs out.
 * 
 * @author Kim
 * 
 */
public class LogoutEvent implements Serializable {

    private static final long serialVersionUID = -5497435495187618081L;

    private Usuario usuario;

    /**
     * 
     * @param usuario
     *            The usuario who is been logged out
     */
    public LogoutEvent(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Get the usuario who has been logged out
     * 
     * @return The usuario object of the logged out usuario
     */
    public Usuario getUsuario() {
        return usuario;
    }

}

package org.sanjose.views;

import com.vaadin.data.fieldgroup.FieldGroup.CommitEvent;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.FieldGroup.CommitHandler;
import com.vaadin.data.util.BeanItem;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.server.Page;
import org.sanjose.MainUI;
import org.sanjose.model.VsjPropiedad;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides an interface for the logical operations between the CRUD
 * view, its parts like the product editor form and the data source, including
 * fetching and saving products.
 *
 * Having this separate from the view makes it easier to test various parts of
 * the system separately, and to e.g. provide alternative views for the same
 * data.
 */
public class PropiedadLogic implements Serializable {


	private static final Logger log = LoggerFactory.getLogger(PropiedadLogic.class);

    private PropiedadView view;

    public PropiedadLogic(PropiedadView propiedadView) {
        view = propiedadView;
    }

    public void init() {
        view.btnNuevaPropiedad.addClickListener(e -> newPropiedad());
        // register save listener
        view.gridPropiedad.getEditorFieldGroup().addCommitHandler(new CommitHandler() {
            @Override
            public void preCommit(CommitEvent commitEvent) throws CommitException {
            	//Notification.show("Item " + view.gridPropiedad.getEditedItemId() + " was edited PRE.");                
            }
            @Override
            public void postCommit(CommitEvent commitEvent) throws CommitException {
                // You can persist your data here
                //Notification.show("Item " + view.gridPropiedad.getEditedItemId() + " was edited.");
                Object item = view.gridPropiedad.getContainerDataSource().getItem(view.gridPropiedad.getEditedItemId());
                if (item!=null) 
                	view.repo.save((VsjPropiedad)((BeanItem)item).getBean());
            }
        });
               
    }

    public void cancelProduct() {
        setFragmentParameter("");
        view.clearSelection();
//        view.editProduct(null);
    }

    /**
     * Update the fragment without causing navigator to change view
     */
    private void setFragmentParameter(String productId) {
        String fragmentParameter;
        if (productId == null || productId.isEmpty()) {
            fragmentParameter = "";
        } else {
            fragmentParameter = productId;
        }

        Page page = MainUI.get().getPage();
  /*      page.setUriFragment("!" + SampleCrudView.VIEW_NAME + "/"
                + fragmentParameter, false);
  */  }

    public void enter(String productId) {
        if (productId != null && !productId.isEmpty()) {
        	log.info("Configuracion Logic getting: " + productId);
            if (productId.equals("new")) {
     //       	newConfiguracion();
            } else {
                // Ensure this is selected even if coming directly here from
                // login
                try {
                    int pid = Integer.parseInt(productId);
  //                  Product product = findProduct(pid);
    //                view.selectRow(product);
                } catch (NumberFormatException e) {
                }
            }
        }
    }

    public void newPropiedad() {
        view.clearSelection();
        setFragmentParameter("new");
        VsjPropiedad vcb = new VsjPropiedad();
        view.gridPropiedad.getContainerDataSource().addItem(vcb);
    }


    public void deleteConfiguracion() {
        List<VsjPropiedad> rows = new ArrayList<VsjPropiedad>();
    	
        for (Object vsj : view.getSelectedRow()) {
        	log.info("Got selected: " + vsj);
        	if (vsj instanceof VsjPropiedad)
        		rows.add((VsjPropiedad)vsj);
        }
        view.clearSelection();
        //setFragmentParameter("new");
        for (VsjPropiedad vsj : rows) {
        	log.info("Removing: " + vsj.getCodPropiedad());
        	view.removeRow(vsj);
        }
        //view.gridPropiedad.getContainerDataSource().removeItem(itemId)
    }    
}

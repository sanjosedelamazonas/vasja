package org.sanjose.config;

import com.sun.javafx.css.converters.BooleanConverter;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.vaadin.data.util.converter.StringToBooleanConverter;
import com.vaadin.ui.renderers.ImageRenderer;
import com.vaadin.ui.renderers.Renderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.vaadin.annotations.Theme;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import pl.waw.sgh.bank.*;

import java.util.ArrayList;

//@SpringUI
//@Theme("valo")
public class VaadinUI extends UI {

	private final CustomerRepository repo;

	private final AccountRepository accountRepo;

	private final CustomerEditor editor;

	private final AccountEditor accountEditor;

	private final Grid grid;

	private final Grid accountGrid;

	private final TextField filter;

	private final Button addNewBtn;

	private final Button addNewDebitAccountBtn;

	private final Button addNewSavingsAccountBtn;

	private HorizontalLayout newAccountsLayout;

	@Autowired
	public VaadinUI(CustomerRepository repo, CustomerEditor editor, AccountRepository accountRepo, AccountEditor accountEditor) {
		this.repo = repo;
		this.editor = editor;
		this.accountRepo = accountRepo;
		this.accountEditor = accountEditor;
		this.grid = new Grid();
		this.accountGrid = new Grid();
		this.filter = new TextField();
		this.addNewBtn = new Button("New customer", FontAwesome.PLUS);
		this.addNewDebitAccountBtn = new Button("New debit account", FontAwesome.PLUS);
		this.addNewSavingsAccountBtn = new Button("New savings account", FontAwesome.PLUS);
	}

	@Override
	protected void init(VaadinRequest request) {
		// build layout
		HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);

		VerticalLayout customerLayout = new VerticalLayout(grid, editor);

		newAccountsLayout = new HorizontalLayout(addNewDebitAccountBtn, addNewSavingsAccountBtn);

		VerticalLayout accountLayout = new VerticalLayout(accountGrid, newAccountsLayout, accountEditor);

		HorizontalLayout grids = new HorizontalLayout(customerLayout, accountLayout);

		VerticalLayout mainLayout = new VerticalLayout(actions, grids);

		setContent(mainLayout);

		newAccountsLayout.setVisible(false);

		// Configure layouts and components
		actions.setSpacing(true);
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		grid.setHeight(300, Unit.PIXELS);
		grid.setColumns("customerID", "firstName", "lastName");

		accountGrid.setHeight(300, Unit.PIXELS);
		accountGrid.setColumns("accountID", "savings", "balance");

		//accountGrid.getColumn("savings").setRenderer(new ImageRenderer());
		
		filter.setInputPrompt("Filter by last name");

		// Hook logic to components

		// Replace listing with filtered content when user changes filter
		filter.addTextChangeListener(e -> listCustomers(e.getText()));

		// Connect selected Customer to editor or hide if none is selected
		grid.addSelectionListener(e -> {
			if (e.getSelected().isEmpty()) {
				editor.setVisible(false);
				listAccounts(null);
			}
			else {
				editor.editCustomer((Customer) grid.getSelectedRow());
				listAccounts((Customer) grid.getSelectedRow());
			}
		});


		accountGrid.addSelectionListener(e -> {
			if (e.getSelected().isEmpty()) {
				accountEditor.setVisible(false);
			}
			else {
				accountEditor.editAccount((Account) accountGrid.getSelectedRow());
			}
		});

		// Instantiate and edit new Customer the new button is clicked
		addNewBtn.addClickListener(e -> editor.editCustomer(new Customer("", "")));

		addNewDebitAccountBtn.addClickListener(e -> accountEditor.editAccount(
				new DebitAccount((Customer) grid.getSelectedRow())));

		addNewSavingsAccountBtn.addClickListener(e -> accountEditor.editAccount(
				new SavingsAccount((Customer) grid.getSelectedRow())));

		// Listen changes made by the editor, refresh data from backend
		editor.setChangeHandler(() -> {
			editor.setVisible(false);
			listCustomers(filter.getValue());
		});

		// Listen changes made by the editor, refresh data from backend
		accountEditor.setChangeHandler(() -> {
			accountEditor.setVisible(false);
			listAccounts((Customer) grid.getSelectedRow());
			//listCustomers(filter.getValue());
		});


		// Initialize listing
		listCustomers(null);
		listAccounts(null);
	}

	// tag::listCustomers[]
	private void listCustomers(String text) {
		if (StringUtils.isEmpty(text)) {
			grid.setContainerDataSource(
					new BeanItemContainer(Customer.class, repo.findAll()));
			newAccountsLayout.setVisible(false);
		}
		else {
			grid.setContainerDataSource(new BeanItemContainer(Customer.class,
					repo.findByLastNameStartsWithIgnoreCase(text)));
			newAccountsLayout.setVisible(false);
		}
	}
	// end::listCustomers[]


	// tag::listAccounts[]
	private void listAccounts(Customer owner) {
		if (owner==null) {
			accountGrid.setContainerDataSource(
					new BeanItemContainer(Account.class, new ArrayList<Account>()));
		}
		else {
			accountGrid.setContainerDataSource(new BeanItemContainer(Account.class,
					accountRepo.findByCustomer(owner)));
			newAccountsLayout.setVisible(true);
		}
	}
	// end::listAccounts[]

}

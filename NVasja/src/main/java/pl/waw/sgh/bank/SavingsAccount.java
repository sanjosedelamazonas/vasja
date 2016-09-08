package pl.waw.sgh.bank;

import javax.persistence.Entity;

/**
 * Created by prubac on 4/15/2016.
 */
//@Entity
public class SavingsAccount extends Account {

    public SavingsAccount() {
        super();
    }

    public SavingsAccount(Long accountID, Customer customer) {
        super(accountID, customer);
    }

    public SavingsAccount(Customer customer) {
        super(customer, true);
    }

}

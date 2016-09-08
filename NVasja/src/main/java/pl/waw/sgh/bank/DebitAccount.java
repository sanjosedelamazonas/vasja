package pl.waw.sgh.bank;

import javax.persistence.Entity;

/**
 * Created by prubac on 4/15/2016.
 */
//@Entity
public class DebitAccount extends Account {

    public DebitAccount() {
        super();
    }

    public DebitAccount(Long accountID, Customer customer) {
        super(accountID, customer);
    }


    public DebitAccount(Customer customer) {
        super(customer, false);
    }
}

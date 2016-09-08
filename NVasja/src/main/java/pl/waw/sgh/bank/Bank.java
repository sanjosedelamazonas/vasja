package pl.waw.sgh.bank;

import pl.waw.sgh.bank.exceptions.AccountNotFoundException;
import pl.waw.sgh.bank.exceptions.IllegalDataException;
import pl.waw.sgh.bank.exceptions.NotEnoughMoneyException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by prubac on 4/15/2016.
 */
public class Bank {

    List<Account> accountList = new ArrayList<>();

    List<Customer> customerList = new ArrayList<>();

    Long lastCustomerId = 1L;

    Long lastAccountId = 1L;

    public List<Customer> getCustomerList() {
        return customerList;
    }

    public List<Account> getAccountList() {
        return accountList;
    }

    public List<Account> getAccountListForCustomer(Customer customer) {
        List<Account> customerAccountList = new ArrayList<>();
        for (Account a : accountList) {
            if (a.getCustomer().equals(customer))
                customerAccountList.add(a);
        }
        return customerAccountList;
    }

    public void deleteAccount(Account acc) {
        accountList.remove(acc);
    }

    public Customer createCustomer(String firstName,
                                   String lastName) {
        Customer c1 = new Customer(lastCustomerId,
                firstName, lastName);
        lastCustomerId++;
        customerList.add(c1);
        return c1;
    }

    public Account createAccount(Customer owner,
                                 boolean isSavings) {
        Account acc;
        //= null;
        if (isSavings) {
            acc = new SavingsAccount(lastAccountId, owner);
        }
        else
            acc = new DebitAccount(lastAccountId, owner);
        lastAccountId++;
        accountList.add(acc);
        return acc;
    }

    public void transfer(Long fromID,
                         Long toID, Double amount) throws AccountNotFoundException,
            NotEnoughMoneyException, IllegalDataException {
        //if (amount > 10000)
        //    throw new RuntimeException("can't transfer more than 10000");

        BigDecimal bgAmount = new BigDecimal(amount);
        Account fromAcc = getAccountById(fromID);
        Account toAcc = getAccountById(toID);

        if (fromAcc.getBalance().compareTo(bgAmount)>0) {
            fromAcc.charge(bgAmount);
            toAcc.deposit(bgAmount);
        } else {
            throw new NotEnoughMoneyException("Can't transfer - not enough money");
            //System.out.println("Can't transfer - not enough money");
        }
    }

    public Account getAccountById(Long accId) throws AccountNotFoundException {

        for (Account acc : accountList) {
            if (acc.getAccountID().equals(accId))
                return acc;
        }
        throw new AccountNotFoundException("Account with ID: " + accId + " not found!!!");
        //return null;
    }

    public Customer getCustomerById(Long customerId) {
        for (Customer customer : customerList) {
            if (customer.getCustomerID().equals(customerId))
                return customer;
        }
        return null;
    }


    @Override
    public String toString() {
        return "Bank{" +
                "\n accs=" + accountList +
                "\n custs=" + customerList +
                "\n lastCustId=" + lastCustomerId +
                "\n lastAccId=" + lastAccountId +
                "\n}";
    }
}

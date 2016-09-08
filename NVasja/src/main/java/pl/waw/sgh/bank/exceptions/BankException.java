package pl.waw.sgh.bank.exceptions;

/**
 * Created by prubac on 4/22/2016.
 */
public class BankException extends Exception{
/*
    public BankException() {
        super();
    }*/

    public BankException(String message) {
        super(message);
    }
}

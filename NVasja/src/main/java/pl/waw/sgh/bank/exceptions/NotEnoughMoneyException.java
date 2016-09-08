package pl.waw.sgh.bank.exceptions;

/**
 * Created by prubac on 4/22/2016.
 */
public class NotEnoughMoneyException extends BankException {

    public NotEnoughMoneyException(String message) {
        super(message);
    }
}

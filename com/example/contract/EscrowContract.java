package com.example.contract;

import com.credits.scapi.v3.SmartContract;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EscrowContract extends SmartContract {

    final private String administrator;
    private String depositor = "";
    private String beneficiary = "";

    private BigDecimal sum = null;
    private Date date = null;
    private boolean is_closed= false;

    public EscrowContract() {
        administrator = initiator;
    }

    @Override
    public String payable(BigDecimal amount, byte[] userData) {
        if(isClosed()) {
            throw new RuntimeException("contract is closed");
        }
        if(!depositor.isEmpty()) {
            throw new RuntimeException("Depositor may be set only once");
        }
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit sum must be positive");
        }
        depositor = initiator;
        sum = amount;
        return "accepted deposit " + String.valueOf(sum.doubleValue()) + " from " + depositor;
    }

    public String setBeneficiary(String wallet_key) {
        if(isClosed()) {
            return "contract is closed";
        }
        if(!isAD()) {
            return "only admin and depositor can set beneficiary (not " + initiator + ")";
        }
        if(!beneficiary.isEmpty()) {
            return "Beneficiary may be set only once";
        }
        beneficiary = wallet_key;
        return "beneficiary set";
    }

    public String setReleaseDate(String text) {
        if(isClosed()) {
            return "contract is closed";
        }
        if(!isAD()) {
            return "only admin and depositor can set release date (not " + initiator + ")";
        }
        if(date != null) {
            return "release date may be set only once";
        }
        Date new_date;
        try {
            new_date = parseDate(new String(text));
        }
        catch(ParseException x) {
            return "release date must be set in format dd.mm.yyyy (f.i. 04.07.1970)";
        }
        date = new_date;
        return "release date set to " + printDate(date);
    }

    public String tryWithdraw() {
        if(isClosed()) {
            return "contract is closed";
        }
        if(beneficiary.isEmpty()) {
            return "beneficiary is not set";
        }
        if(!isB()) {
            return "only beneficiary can wihtdraw (not " + initiator + ")";
        }
        if(date == null) {
            return "date to release deposit is not set";
        }
        if(sum == null) {
            return "deposit sum is not set";
        }
        Date now = new Date(getBlockchainTimeMills());
        if(date.before(now)) {
            BigDecimal balance = getBalance(contractAddress);
            if(sum.compareTo(balance) <= 0) {
                sum = balance;
                if(sum.doubleValue() < 0.001) {
                    return "deposit is empty";
                }
            }
            sendTransaction(contractAddress, beneficiary, sum.doubleValue(), null);
            sum = BigDecimal.ZERO;
            return "deposit released";
        }
        return "deposit is hold until " + printDate(date);
    }

    public String getDepositor() {
        if(depositor.isEmpty()) {
            return "not set yet";
        }
        return depositor;
    }

    public String getBeneficiary() {
        if(beneficiary.isEmpty()) {
            return "not set yet";
        }
        return beneficiary;
    }

    public String getDepositSum() {
        if(sum == null) {
            return "not set yet";
        }
        return String.valueOf(sum.doubleValue());
    }

    public String getReleaseDate() {
        if(date == null) {
            return "not set yet";
        }
        return printDate(date);
    }

    private Date parseDate(String dob) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Date date = formatter.parse(dob);
        return date;
    }

    private String printDate(Date d) {
        SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");
        return fmt.format(d);
    }

    private boolean isAB() {
        if(isA() || isB()) {
            return true;
        }
        return false;
    }

    private boolean isAD() {
        if(isA() || isD()) {
            return true;
        }
        return false;
    }

    private boolean isB() {
        if(initiator.equals(beneficiary)) {
            return true;
        }
        return false;
    }

    private boolean isA() {
        if(initiator.equals(administrator)) {
            return true;
        }
        return false;
    }

    private boolean isD() {
        if(initiator.equals(depositor)) {
            return true;
        }
        return false;
    }

    public boolean isClosed() {
        return is_closed;
    }

    public String close() {
        if(!isB()) {
            return "only beneficiary can close contract";
        }
        is_closed = true;
        return "contract is closed now";
    }
}

package com.example.contract;

import java.math.BigDecimal;
import java.math.RoundingMode;
import static java.math.BigDecimal.ZERO;
import java.util.Date;
import java.util.HashSet;
import com.credits.scapi.v3.SmartContract;

public class ICOContract extends SmartContract {
    
    //#region testing

    private ICOTokenContract token_instance = null;

    public void mock_setToken(ICOTokenContract inst) {
        if(token_instance != null) {
            throw new RuntimeException("token update is disabled");
        }
        token_instance = inst;
    }

    // mock method
    protected java.lang.Object invokeExternalContract(String contract_address, String method, Object... params) {
        if(method.equals("getDecimal")) {
            return token_instance.getDecimal();
        }
        // String balanceOf(String owner)
        else if(method.equals("balanceOf")) {
            return token_instance.balanceOf((String) params[0]);
        }
        // boolean transfer(String to, String amount)
        else if(method.equals("transfer")) {
            token_instance.mock_pushInitiator();
            token_instance.mock_setInitiator(contractAddress);
            boolean tmp = token_instance.transfer((String) params[0], (String) params[1]);
            token_instance.mock_popInitiator();
            return tmp;
        }
        return null;
    }

    //#endregion testing

    private String owner;
    protected String token;
    protected BigDecimal cost;
    protected BigDecimal minPayment;
    protected BigDecimal maxPayment;
    protected int decimal = -1;
    private long expiration;
    private boolean expired;

    protected HashSet<String> whiteList;

    public ICOContract() {
        super();

        // deploy time settings
        token = "39wm6VNcXZMj9y9BC13UQ1ZkWsjAAQYZDQ1uwGaE3qHF";
        cost = new BigDecimal(2.0);
        minPayment = BigDecimal.ONE;
        maxPayment = BigDecimal.valueOf(Long.MAX_VALUE);

        whiteList = new HashSet<String>();
        owner = initiator;
        expiration = 0;
        expired = false;
    }

    public void addToWhiteList(String wallet) {
        testOwner();

        if(!wallet.isBlank()) {
            whiteList.add(wallet);
        }
    }

    public boolean isWhite(String wallet) {
        if(whiteList.isEmpty()) {
            return true;
        }
        return whiteList.contains(wallet);
    }

    public void removeWhiteList() {
        testOwner();
        whiteList.clear();
    }

    public void startNewRound(String min, String max, long exp_unix_sec) {
        testOwner();
        if(!expired) {
            stopRound();
        }

        BigDecimal vmin = toBigDecimal(min);
        BigDecimal vmax = toBigDecimal(max);
        if(vmin == null || vmax == null) {
            String smin = "";
            String smax = "";
            if(vmin == null) {
                smin = " min";
            }
            if(vmax == null) {
                smax = " max";
            }
            throw new RuntimeException("incorrect parameters:" + smin + smax);
        }
        expiration = exp_unix_sec * 1000;
        expired = false;
        minPayment = vmin;
        maxPayment = vmax;
    }

    public String withdrawMax(BigDecimal sum) {
        BigDecimal balance = getBalance(contractAddress);
        if(balance.compareTo(sum) < 0) {
            sum = balance;
        }
        sendTransaction(contractAddress, owner, sum.doubleValue(), 0.0, null);
        return "transferred " + sum + " to owner";
    }

    public String getAvailTokens() {
        return (String) invokeExternalContract(token, "balanceOf", contractAddress);
    }

    public String getAvailTokensTotalCost() {
        testCost();
        BigDecimal total = toBigDecimal((String) invokeExternalContract(token, "balanceOf", contractAddress));
        return total.multiply(cost).toString();
    }

    protected String payable(BigDecimal amount, byte[] userData) {
        checkExpirationDate();
        testExpired();
        testCost();
        testWhiteList();

        BigDecimal requested = amount.divide(cost);
        BigDecimal total = toBigDecimal((String) invokeExternalContract(token, "balanceOf", contractAddress));
        if(total.compareTo(requested) < 0) {
            BigDecimal avail_sum = cost.multiply(total);
            throw new RuntimeException("not enough tokens, max avail " + total + ", sum " + avail_sum);
        }
        if(! (boolean) invokeExternalContract(token, "transfer", initiator, requested.toString()) ) {
            throw new RuntimeException("failed to transfer " + requested + " tokens to " + initiator);
        }
        return "transferred " + requested + " tokens to " + initiator;
    }


    private BigDecimal toBigDecimal(String stringValue) {
        BigDecimal val = null;
        try {
            if(decimal < 0) {
                decimal = (Integer) invokeExternalContract(token, "getDecimal");
            }
        } catch(ClassCastException x) {
            throw new RuntimeException(x.getMessage());
        } catch(NullPointerException x) {
            throw new RuntimeException(x.getMessage());
        }
        try {
            val = new BigDecimal(stringValue).setScale(decimal, RoundingMode.FLOOR);
        } catch(NumberFormatException x) {
            throw new RuntimeException(x.getMessage());
        }
        return val;
    }

    private void testCost() {
        if(cost.compareTo(ZERO) <= 0) {
            throw new RuntimeException("contract error: incorrect token cost " + cost);
        }
    }

    private void testOwner() {
        if(!initiator.equals(owner)) {
            throw new RuntimeException("only owner allowed to operate");
        }
    }

    private void testWhiteList() {
        if(!isWhite(initiator)) {
            throw new RuntimeException("only whitelisted accounts are welcome");
        }
    }

    private void testExpired() {
        if(expired) {
            throw new RuntimeException("round is expired");
        }
    }

    private boolean checkExpirationDate() {
        if(!expired) {
            if(expiration != 0) {
                if(getBlockchainTimeMills() >= expiration) {
                    stopRound();
                }
            }
        }
        return expired;
    }

    private void stopRound() {
        if(expired) {
            return;
        }
        String avail = getAvailTokens();
        if(!avail.isBlank() && !avail.equals("0")) {
            if(! (boolean) invokeExternalContract(token, "transfer", owner, avail) ) {
                throw new RuntimeException("failed to transfer " + avail + " tokens to " + owner);
            }
        }
        expired = true;
    }
}


package com.credits.scapi.v0;

import java.math.BigDecimal;

public class SmartContract {
    public static void test_setDeployer(String key_base58) {
        deployer = key_base58;
    }

    private static String deployer = "";

    //#region testing support

    protected String initiator = "";
    protected String contractAddress = "";
    private BigDecimal balance = BigDecimal.ZERO;

    public void test_setInitiator(String key_base58) {
        initiator = key_base58;
    }

    public void test_setBalance(BigDecimal sum) {
        balance = sum;
    }

    public void test_setAddress(String key_base58) {
        contractAddress = key_base58;
    }

    public String test_replenish(String from_base58, BigDecimal sum, byte[] userData) {
        test_setInitiator(from_base58);
        System.out.println("replenish contract by " + sum.toString());
        String result = payable(sum, userData);
        balance = balance.add(sum);
        return result;
    }

    //#endregion testing support

    public SmartContract() {
        initiator = SmartContract.deployer;
    }

    protected String payable(BigDecimal amount, byte[] userData) {
        throw new RuntimeException("illegal contract::payable() call");
    }
    
    protected BigDecimal getBalance(String addr_base58) {
        return balance;
    }

    protected void sendTransaction(String from_base_58, String to_base58, double sum, byte[] data) {
        if(balance.doubleValue() < sum) {
            throw new RuntimeException("sendTransaction(): insufficient balance");
        }
        balance = balance.add((new BigDecimal(sum)).negate());
        String data_desc = "";
        if(data != null && data.length > 0 ) {
            data_desc = ", data length = " + Integer.toString(data.length);
        }
        System.out.println("sendTransaction(): " + Double.toString(sum) + " send from " + from_base_58 + " to " + to_base58 + data_desc);
    }

    protected void sendTransaction(String b58_from, String b58_to, double sum, double fee, byte[] data) {
        System.out.println("sendTransaction(): obsolete call, set fee " + Double.toString(fee));
        sendTransaction(b58_from, b58_to, sum, data);
    }

}

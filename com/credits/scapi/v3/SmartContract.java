package com.credits.scapi.v3;

import java.math.BigDecimal;

public class SmartContract extends com.credits.scapi.v0.SmartContract {

    private long blockchainTimeMillis = 0;

    public SmartContract() {
    }

    public void test_setBlockchainTimeMills(long unix_msec) {
        blockchainTimeMillis = unix_msec;
    }

    public long getBlockchainTimeMills() {
        return blockchainTimeMillis;
    }

    protected void sendTransaction(String from, String to, double amount, double fee, byte... userData) {
    }

    protected BigDecimal getBalance(String addressBase58) {
        return new BigDecimal(0.0);
    }

    protected byte[] getSeed() {
        return null;
    }

    protected String payable(BigDecimal amount, byte[] userData) {
        return "";
    }

}

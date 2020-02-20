package com.credits.scapi.v3;

import java.math.BigDecimal;

public class SmartContract extends com.credits.scapi.v0.SmartContract {

    public SmartContract() {
    }

    //#region testing support

    private long blockchainTimeMillis = 0;

    public void test_setBlockchainTimeMills(long unix_msec) {
        blockchainTimeMillis = unix_msec;
    }

    public long getBlockchainTimeMills() {
        return blockchainTimeMillis;
    }

    //#endregion testing support

    protected void sendTransaction(String from, String to, double amount, byte[] data) {
    }

    protected java.lang.Object invokeExternalContract(String contractAddress, String method, Object... params) {
        return null;
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

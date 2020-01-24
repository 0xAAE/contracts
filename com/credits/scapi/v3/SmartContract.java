package com.credits.scapi.v3;

public class SmartContract extends com.credits.scapi.v0.SmartContract {

    private long blockchainTimeMillis = 0;

    public SmartContract() {
    }

    public void test_setBlockchainTimeMills(long unix_msec) {
        blockchainTimeMillis = unix_msec;
    }

    protected long getBlockchainTimeMills() {
        return blockchainTimeMillis;
    }

}

package com.credits.scapi.v2;

import com.credits.scapi.internal.v0.SmartContractApi;

import java.math.BigDecimal;

public abstract class SmartContract extends SmartContractApi {
    private static final long serialVersionUID = -7107388825339899265L;

    protected final transient long accessId = 0;
    protected final transient String initiator = null;
    protected final String contractAddress = null;

    protected void sendTransaction(String from, String to, double amount, byte... userData) {
    }

    protected Object invokeExternalContract(String contractAddress, String method, Object... params) {
        return null;
    }

    protected byte[] getSeed() {
        return null;
    }

    protected BigDecimal getBalance(String addressBase58) {
        return null;
    }
}

package com.example.contract;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.credits.scapi.v3.SmartContract;

public class TestContracts
{
    public static void main(String args[]) {
        testEscrowContract();
    }

    public static void testEscrowContract() {

        final String pk_admin = "5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe";
        final String pk_deponent = "JfFyPGxxN7ygUNfM5if5TfGmjGuJ1BaZqrGsTKPsWnZ";
        final String pk_beneficiary = "HL99dwfM3YPQnauN1djBvVLZNbC3b1FHwe5vPv8pDZ1y";
        final String pk_address = "39wm6VNcXZMj9y9BC13UQ1ZkWsjAAQYZDQ1uwGaE3qHF";

        final int unix_sec = 1579871500;
        final BigDecimal deposit_sum = new BigDecimal(1000.0);

        SmartContract.test_setDeployer(pk_admin);
        EscrowContract contract = new EscrowContract();
        contract.test_setAddress(pk_address);
        //contract.test_setInitiator(pk_deponent);
        System.out.println(contract.test_replenish(pk_deponent, deposit_sum, null));
        System.out.println(contract.setBeneficiary(pk_beneficiary));
        System.out.println(contract.setReleaseDate(Integer.toString(unix_sec + 1000)));

        contract.test_setInitiator(pk_beneficiary);
        contract.test_setBlockchainTimeMills((long)(unix_sec + 500) * 1000);
        System.out.println(contract.tryWithdraw());
        contract.test_setBlockchainTimeMills((long)(unix_sec + 1001) * 1000);
        System.out.println(contract.tryWithdraw());

        System.out.println("ok");
    }
}

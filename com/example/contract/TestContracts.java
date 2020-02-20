package com.example.contract;

import java.math.BigDecimal;
import java.util.Date;

import com.credits.scapi.v3.SmartContract;

public class TestContracts
{
    final static String pk_owner = "owner_address";
    final static String pk_from = "from_address";
    final static String pk_to = "to_address";
    final static String pk_address = "contract_address";
    final static String pk_service = "service_address";
    final static long tomorrow = 1582285710;
    final static long yesterday = 1582112910;
    final static long now_ms = new Date().getTime();

    public static void main(String args[]) {

        System.out.println("\ntest EscrowContract\n");
        testEscrowContract();
        System.out.println("\nok\n");

        System.out.println("\ntest ICOTokenContract\n");
        testOCIContract();
        System.out.println("\nok\n");

        System.out.println("\ntest ICOContract\n");
        testICOContract();
        System.out.println("\nok\n");
    }

    private static void testICOContract() {
        ICOTokenContract.test_setDeployer(pk_owner);
        ICOTokenContract token = new ICOTokenContract();
        token.test_setAddress(pk_address);
        token.test_setBlockchainTimeMills(now_ms);

        ICOContract.test_setDeployer(pk_owner);
        ICOContract ico = new ICOContract();
        ico.test_setAddress(pk_service);
        ico.test_setToken(token);
        ico.test_setBlockchainTimeMills(now_ms);

        // scenario-1
        token.test_setInitiator(pk_owner);
        ico.test_setInitiator(pk_owner);
        token.transfer(pk_service, "50000");
        System.out.println(ico.getAvailTokens());
        System.out.println(ico.getAvailTokensTotalCost());
        ico.updatePaymentLimits("2", "10000");
        ico.test_setInitiator(pk_from);
        System.out.println(ico.payable(new BigDecimal(5000.0), null));
        System.out.println(ico.getAvailTokens());
        System.out.println(ico.getAvailTokensTotalCost());
        System.out.println(token.balanceOf(pk_owner));
        System.out.println(token.balanceOf(pk_from));
        System.out.println(token.balanceOf(pk_service));
    }

    private static void testOCIContract() {
        ICOTokenContract.test_setDeployer(pk_owner);
        ICOTokenContract token = new ICOTokenContract();
        token.test_setAddress(pk_address);
        token.test_setBlockchainTimeMills(new Date().getTime());

        // isAccountFrozen() / getAccountDefrostDate() / defrostAccount() / freezeAccount()
        // owner == initiator
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));
        token.freezeAccount(pk_service, tomorrow);
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));
        token.defrostAccount(pk_service);
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));
        // autodefrost
        token.freezeAccount(pk_service, yesterday);
        System.out.println(token.getAccountDefrostDate(pk_service));
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));

        // initiator != owner
        token.test_setInitiator(pk_from);
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));
        try{
            token.freezeAccount(pk_service, tomorrow);
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));
        try {
            token.defrostAccount(pk_service);
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));
        // autodefrost
        token.test_setInitiator(pk_owner);
        token.freezeAccount(pk_service, yesterday);
        token.test_setInitiator(pk_from);
        System.out.println(token.getAccountDefrostDate(pk_service));
        try {
            System.out.println(token.isAccountFrozen(pk_service));
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        System.out.println(token.getAccountDefrostDate(pk_service));

        // pk_service still is frozen
        token.test_setInitiator(pk_owner);
        // now is not
        System.out.println(token.isAccountFrozen(pk_service));

        // disabled methods
        try {
            token.payable("1.0", "1");
        }
        catch(RuntimeException x) {
            System.out.println(x);
        }
        try {
            token.buyTokens("2");
        }
        catch(RuntimeException x) {
            System.out.println(x);
        }

        // getters
        System.out.println(token.getDecimal());
        System.out.println(token.getName());
        System.out.println(token.getSymbol());
        System.out.println(token.totalSupply());
        System.out.println(token.balanceOf(pk_owner));
        System.out.println(token.balanceOf(pk_from));
        System.out.println(token.balanceOf(pk_service));
        System.out.println(token.allowance(pk_owner, pk_service));

        // scenario-1 (transfers)
        token.test_setInitiator(pk_owner);
        token.transfer(pk_owner, "1000");
        System.out.println(token.balanceOf(pk_owner));
        token.transfer(pk_from, "1000");
        System.out.println(token.balanceOf(pk_owner));
        System.out.println(token.balanceOf(pk_from));
        token.test_setInitiator(pk_from);
        token.approve(pk_service, "50");
        token.test_setInitiator(pk_service);
        try {
            token.transferFrom(pk_owner, pk_to, "40");
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        try {
            token.transferFrom(pk_from, pk_to, "60");
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        token.transferFrom(pk_from, pk_to, "49");
        try {
            token.transferFrom(pk_from, pk_to, "2");
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        token.transferFrom(pk_from, pk_to, "1");
        token.test_setInitiator(pk_to);
        token.transfer(pk_from, "50");
        token.test_setInitiator(pk_from);
        token.transfer(pk_owner, "1000");
        System.out.println(token.balanceOf(pk_owner));
        System.out.println(token.balanceOf(pk_from));
        System.out.println(token.balanceOf(pk_service));
        System.out.println(token.balanceOf(pk_to));

        // scenario-2 (burns)
        token.test_setInitiator(pk_owner);
        token.transfer(pk_from, "1000");
        System.out.println(token.totalSupply());
        System.out.println(token.balanceOf(pk_owner));
        System.out.println(token.balanceOf(pk_from));
        try {
            token.burn(token.totalSupply());
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        token.burn("1000");
        System.out.println(token.totalSupply());
        System.out.println(token.balanceOf(pk_owner));
        System.out.println(token.balanceOf(pk_from));
        token.burn("980000");
        System.out.println(token.totalSupply());
        System.out.println(token.balanceOf(pk_owner));
        System.out.println(token.balanceOf(pk_from));
        token.burn("2000");
        System.out.println(token.totalSupply());
        System.out.println(token.balanceOf(pk_owner));
        System.out.println(token.balanceOf(pk_from));
        token.burn(token.getBurnAvail());
        System.out.println(token.totalSupply());
        System.out.println(token.balanceOf(pk_owner));
        System.out.println(token.balanceOf(pk_from));
    }

    private static void testEscrowContract() {
        final String pk_admin = pk_owner;
        final String pk_deponent = "deponent_address";
        final String pk_beneficiary = "beneficiary_address";

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
    }
}

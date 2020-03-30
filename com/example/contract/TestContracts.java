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
    final static long feb19 = 1582112910;
    final static long feb20 = 1582187074;
    final static long feb21 = 1582285710;
    final static long feb22 = 1582359874;
    final static long feb23 = 1582446274;
    final static long feb20_ms = feb20 * 1000;

    public static void main(String args[]) {

        System.out.println("\nskip EscrowContract\n");
        // testEscrowContract();
        // System.out.println("\nok\n");

        System.out.println("\nskip ICOTokenContract\n");
        // testICOTokenContract();
        // System.out.println("\nok\n");

        System.out.println("\nskip ICOContract\n");
        // testICOContract();
        // System.out.println("\nok\n");

        System.out.println("\nskip ICOTokenEntireContract\n");
        //testICOTokenEntireContract();
        //System.out.println("\nok\n");

        System.out.println("\ntest StableCoinTokenContract\n");
        testStableCoinTokenContract();
        System.out.println("\nok\n");
    }

    private static void testICOTokenEntireContract() {
        ICOTokenEntireContract.mock_setDeployer(pk_owner);
        ICOTokenEntireContract ico_token = new ICOTokenEntireContract();
        ico_token.mock_setAddress(pk_address);
        ico_token.mock_setBlockchainTimeMills(new Date().getTime());

        //#region ICO testing

        ico_token.mock_setBlockchainTimeMills(feb20 * 1000);
        // round-1: min 2CS, max 10000CS, expire feb21, 50000ICOT
        ico_token.mock_setInitiator(pk_owner);
        System.out.println("no round");
        System.out.println("avail " + ico_token.getAvailTokens() + " ICOT = " + ico_token.getAvailTokensTotalCost() + " CS");
        System.out.println("start round-1: 2..10'000 until 21 feb, avail 50K");
        ico_token.startNewRound("50000", "2", "10000", feb21);
        System.out.println("avail " + ico_token.getAvailTokens() + " ICOT = " + ico_token.getAvailTokensTotalCost() + " CS");
        ico_token.mock_setInitiator(pk_from);
        try {
            System.out.println(ico_token.payable(new BigDecimal(1.0), null));
        } catch (RuntimeException x) {
            System.out.println(x);
        }
        try {
            System.out.println(ico_token.payable(new BigDecimal(11000.0), null));
        } catch (RuntimeException x) {
            System.out.println(x);
        }
        try {
            System.out.println(ico_token.payable(new BigDecimal(5000.0), null));
        } catch (RuntimeException x) {
            System.out.println(x);
        }
        System.out.println(ico_token.payable(new BigDecimal(50.0), null));
        System.out.println("avail " + ico_token.getAvailTokens() + " ICOT = " + ico_token.getAvailTokensTotalCost() + " CS");
        System.out.println(pk_owner + ": " + ico_token.balanceOf(pk_owner));
        System.out.println(pk_from + ": " + ico_token.balanceOf(pk_from));
        System.out.println(pk_service + ": " + ico_token.balanceOf(pk_service));
        ico_token.mock_setBlockchainTimeMills(feb21 * 1000);
        try {
            System.out.println(ico_token.payable(new BigDecimal(5000.0), null));
        } catch (RuntimeException x) {
            System.out.println(x);
        }
        System.out.println("avail " + ico_token.getAvailTokens() + " ICOT = " + ico_token.getAvailTokensTotalCost() + " CS");
        System.out.println(pk_owner + ": " + ico_token.balanceOf(pk_owner));
        System.out.println(pk_from + ": " + ico_token.balanceOf(pk_from));
        System.out.println(pk_service + ": " + ico_token.balanceOf(pk_service));

        // round-2: min 5000CS max 40000CS, expire 22 feb, 100000 ICOT
        System.out.println("start round-2: 5'000..40'000 until 22 feb, avail 50K");
        try {
            ico_token.startNewRound("50000", "5000", "40000", feb22);
        } catch (RuntimeException x) {
            System.out.println(x);
        }
        ico_token.mock_pushInitiator();
        ico_token.mock_setInitiator(pk_owner);
        ico_token.startNewRound("50000", "5000", "40000", feb22);
        ico_token.mock_popInitiator();
        System.out.println("avail " + ico_token.getAvailTokens() + " ICOT = " + ico_token.getAvailTokensTotalCost() + " CS");
        try {
            System.out.println(ico_token.payable(new BigDecimal(4000.0), null));
        } catch (RuntimeException x) {
            System.out.println(x);
        }
        try {
            System.out.println(ico_token.payable(new BigDecimal(50000.0), null));
        } catch (RuntimeException x) {
            System.out.println(x);
        }
        System.out.println(ico_token.payable(new BigDecimal(5001.0), null));
        System.out.println("avail " + ico_token.getAvailTokens() + " ICOT = " + ico_token.getAvailTokensTotalCost() + " CS");
        System.out.println(pk_owner + ": " + ico_token.balanceOf(pk_owner));
        System.out.println(pk_from + ": " + ico_token.balanceOf(pk_from));
        System.out.println(pk_service + ": " + ico_token.balanceOf(pk_service));
        ico_token.mock_setBlockchainTimeMills(feb22 * 1000);
        try {
            System.out.println(ico_token.payable(new BigDecimal(10000.0), null));
        } catch (RuntimeException x) {
            System.out.println(x);
        }
        System.out.println("avail " + ico_token.getAvailTokens() + " ICOT = " + ico_token.getAvailTokensTotalCost() + " CS");
        System.out.println(pk_owner + ": " + ico_token.balanceOf(pk_owner));
        System.out.println(pk_from + ": " + ico_token.balanceOf(pk_from));
        System.out.println(pk_service + ": " + ico_token.balanceOf(pk_service));

        //#endregion ICO testing

        //#region Token testing

        // isAccountFrozen() / getAccountDefrostDate() / defrostAccount() / freezeAccount()
        // owner == initiator
        ico_token.mock_setInitiator(pk_owner);
        System.out.println(ico_token.isAccountFrozen(pk_service));
        System.out.println(ico_token.getAccountDefrostDate(pk_service));
        ico_token.freezeAccount(pk_service, feb21);
        System.out.println(ico_token.isAccountFrozen(pk_service));
        System.out.println(ico_token.getAccountDefrostDate(pk_service));
        ico_token.defrostAccount(pk_service);
        System.out.println(ico_token.isAccountFrozen(pk_service));
        System.out.println(ico_token.getAccountDefrostDate(pk_service));
        // autodefrost
        ico_token.freezeAccount(pk_service, feb19);
        System.out.println(ico_token.getAccountDefrostDate(pk_service));
        System.out.println(ico_token.isAccountFrozen(pk_service));
        System.out.println(ico_token.getAccountDefrostDate(pk_service));

        // initiator != owner
        ico_token.mock_setInitiator(pk_from);
        System.out.println(ico_token.isAccountFrozen(pk_service));
        System.out.println(ico_token.getAccountDefrostDate(pk_service));
        try{
            ico_token.freezeAccount(pk_service, feb21);
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        System.out.println(ico_token.isAccountFrozen(pk_service));
        System.out.println(ico_token.getAccountDefrostDate(pk_service));
        try {
            ico_token.defrostAccount(pk_service);
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        System.out.println(ico_token.isAccountFrozen(pk_service));
        System.out.println(ico_token.getAccountDefrostDate(pk_service));
        // autodefrost
        ico_token.mock_setInitiator(pk_owner);
        ico_token.freezeAccount(pk_service, feb19);
        ico_token.mock_setInitiator(pk_from);
        System.out.println(ico_token.getAccountDefrostDate(pk_service));
        try {
            System.out.println(ico_token.isAccountFrozen(pk_service));
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        System.out.println(ico_token.getAccountDefrostDate(pk_service));

        // pk_service still is frozen
        ico_token.mock_setInitiator(pk_owner);
        // now is not
        System.out.println(ico_token.isAccountFrozen(pk_service));

        // disabled methods
        try {
            ico_token.payable(new BigDecimal(1.0), null);
        }
        catch(RuntimeException x) {
            System.out.println(x);
        }
        try {
            ico_token.buyTokens("2");
        }
        catch(RuntimeException x) {
            System.out.println(x);
        }

        // getters
        System.out.println(ico_token.getDecimal());
        System.out.println(ico_token.getName());
        System.out.println(ico_token.getSymbol());
        System.out.println(ico_token.totalSupply());
        System.out.println(ico_token.balanceOf(pk_owner));
        System.out.println(ico_token.balanceOf(pk_from));
        System.out.println(ico_token.balanceOf(pk_service));
        System.out.println(ico_token.allowance(pk_owner, pk_service));

        // scenario-1 (transfers)
        ico_token.mock_setInitiator(pk_owner);
        ico_token.transfer(pk_owner, "1000");
        System.out.println(ico_token.balanceOf(pk_owner));
        ico_token.transfer(pk_from, "1000");
        System.out.println(ico_token.balanceOf(pk_owner));
        System.out.println(ico_token.balanceOf(pk_from));
        ico_token.mock_setInitiator(pk_from);
        ico_token.approve(pk_service, "50");
        ico_token.mock_setInitiator(pk_service);
        try {
            ico_token.transferFrom(pk_owner, pk_to, "40");
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        try {
            ico_token.transferFrom(pk_from, pk_to, "60");
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        ico_token.transferFrom(pk_from, pk_to, "49");
        try {
            ico_token.transferFrom(pk_from, pk_to, "2");
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        ico_token.transferFrom(pk_from, pk_to, "1");
        ico_token.mock_setInitiator(pk_to);
        ico_token.transfer(pk_from, "50");
        ico_token.mock_setInitiator(pk_from);
        ico_token.transfer(pk_owner, "1000");
        System.out.println(ico_token.balanceOf(pk_owner));
        System.out.println(ico_token.balanceOf(pk_from));
        System.out.println(ico_token.balanceOf(pk_service));
        System.out.println(ico_token.balanceOf(pk_to));

        // scenario-2 (burns)
        ico_token.mock_setInitiator(pk_owner);
        ico_token.transfer(pk_from, "1000");
        System.out.println(ico_token.totalSupply());
        System.out.println(ico_token.balanceOf(pk_owner));
        System.out.println(ico_token.balanceOf(pk_from));
        try {
            ico_token.burn(ico_token.totalSupply());
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        ico_token.burn("1000");
        System.out.println(ico_token.totalSupply());
        System.out.println(ico_token.balanceOf(pk_owner));
        System.out.println(ico_token.balanceOf(pk_from));
        ico_token.burn("980000");
        System.out.println(ico_token.totalSupply());
        System.out.println(ico_token.balanceOf(pk_owner));
        System.out.println(ico_token.balanceOf(pk_from));
        ico_token.burn("2000");
        System.out.println(ico_token.totalSupply());
        System.out.println(ico_token.balanceOf(pk_owner));
        System.out.println(ico_token.balanceOf(pk_from));
        ico_token.burn(ico_token.getBurnAvail());
        System.out.println(ico_token.totalSupply());
        System.out.println(ico_token.balanceOf(pk_owner));
        System.out.println(ico_token.balanceOf(pk_from));

        //#endregion Token testing
    }

    private static void testICOContract() {
        ICOTokenContract.mock_setDeployer(pk_owner);
        ICOTokenContract token = new ICOTokenContract();
        token.mock_setAddress(pk_address);
        token.mock_setBlockchainTimeMills(feb20_ms);

        ICOContract.mock_setDeployer(pk_owner);
        ICOContract ico = new ICOContract();
        ico.mock_setAddress(pk_service);
        ico.mock_setToken(token);
        ico.mock_setBlockchainTimeMills(feb20_ms);

        // round-1: min 2CS, max 10000CS, expire feb21, 50000ICOT
        token.mock_setInitiator(pk_owner);
        ico.mock_setInitiator(pk_owner);
        System.out.println("no round");
        System.out.println("avail " + ico.getAvailTokens() + " ICOT = " + ico.getAvailTokensTotalCost() + " CS");
        System.out.println("start round-1: 2..10'000 until 21 feb");
        ico.startNewRound("2", "10000", feb21);
        token.transfer(pk_service, "50000");
        System.out.println("avail " + ico.getAvailTokens() + " ICOT = " + ico.getAvailTokensTotalCost() + " CS");
        ico.mock_setInitiator(pk_from);
        try {
            System.out.println(ico.payable(new BigDecimal(1.0), null));
        } catch (RuntimeException x) {
            System.out.println(x);
        }
        try {
            System.out.println(ico.payable(new BigDecimal(11000.0), null));
        } catch (RuntimeException x) {
            System.out.println(x);
        }
        System.out.println(ico.payable(new BigDecimal(5000.0), null));
        System.out.println("avail " + ico.getAvailTokens() + " ICOT = " + ico.getAvailTokensTotalCost() + " CS");
        System.out.println(pk_owner + ": " + token.balanceOf(pk_owner));
        System.out.println(pk_from + ": " + token.balanceOf(pk_from));
        System.out.println(pk_service + ": " + token.balanceOf(pk_service));
        ico.mock_setBlockchainTimeMills(feb21 * 1000);
        try {
            System.out.println(ico.payable(new BigDecimal(5000.0), null));
        } catch (RuntimeException x) {
            System.out.println(x);
        }
        System.out.println("avail " + ico.getAvailTokens() + " ICOT = " + ico.getAvailTokensTotalCost() + " CS");
        System.out.println(pk_owner + ": " + token.balanceOf(pk_owner));
        System.out.println(pk_from + ": " + token.balanceOf(pk_from));
        System.out.println(pk_service + ": " + token.balanceOf(pk_service));

        // round-2: min 5000CS max 40000CS, expire 22 feb, 100000 ICOT
        System.out.println("start round-2: 5'000..40'000 until 22 feb");
        try {
            ico.startNewRound("5000", "40000", feb22);
        } catch (RuntimeException x) {
            System.out.println(x);
        }
        ico.mock_pushInitiator();
        ico.mock_setInitiator(pk_owner);
        ico.startNewRound("5000", "40000", feb22);
        token.transfer(pk_service, "50000");
        ico.mock_popInitiator();
        System.out.println("avail " + ico.getAvailTokens() + " ICOT = " + ico.getAvailTokensTotalCost() + " CS");
        try {
            System.out.println(ico.payable(new BigDecimal(4000.0), null));
        } catch (RuntimeException x) {
            System.out.println(x);
        }
        try {
            System.out.println(ico.payable(new BigDecimal(50000.0), null));
        } catch (RuntimeException x) {
            System.out.println(x);
        }
        System.out.println(ico.payable(new BigDecimal(10000.0), null));
        System.out.println("avail " + ico.getAvailTokens() + " ICOT = " + ico.getAvailTokensTotalCost() + " CS");
        System.out.println(pk_owner + ": " + token.balanceOf(pk_owner));
        System.out.println(pk_from + ": " + token.balanceOf(pk_from));
        System.out.println(pk_service + ": " + token.balanceOf(pk_service));
        ico.mock_setBlockchainTimeMills(feb22 * 1000);
        try {
            System.out.println(ico.payable(new BigDecimal(10000.0), null));
        } catch (RuntimeException x) {
            System.out.println(x);
        }
        System.out.println("avail " + ico.getAvailTokens() + " ICOT = " + ico.getAvailTokensTotalCost() + " CS");
        System.out.println(pk_owner + ": " + token.balanceOf(pk_owner));
        System.out.println(pk_from + ": " + token.balanceOf(pk_from));
        System.out.println(pk_service + ": " + token.balanceOf(pk_service));
    }

    private static void testICOTokenContract() {
        ICOTokenContract.mock_setDeployer(pk_owner);
        ICOTokenContract token = new ICOTokenContract();
        token.mock_setAddress(pk_address);
        token.mock_setBlockchainTimeMills(new Date().getTime());

        // isAccountFrozen() / getAccountDefrostDate() / defrostAccount() / freezeAccount()
        // owner == initiator
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));
        token.freezeAccount(pk_service, feb21);
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));
        token.defrostAccount(pk_service);
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));
        // autodefrost
        token.freezeAccount(pk_service, feb19);
        System.out.println(token.getAccountDefrostDate(pk_service));
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));

        // initiator != owner
        token.mock_setInitiator(pk_from);
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));
        try{
            token.freezeAccount(pk_service, feb21);
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
        token.mock_setInitiator(pk_owner);
        token.freezeAccount(pk_service, feb19);
        token.mock_setInitiator(pk_from);
        System.out.println(token.getAccountDefrostDate(pk_service));
        try {
            System.out.println(token.isAccountFrozen(pk_service));
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        System.out.println(token.getAccountDefrostDate(pk_service));

        // pk_service still is frozen
        token.mock_setInitiator(pk_owner);
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
        token.mock_setInitiator(pk_owner);
        token.transfer(pk_owner, "1000");
        System.out.println(token.balanceOf(pk_owner));
        token.transfer(pk_from, "1000");
        System.out.println(token.balanceOf(pk_owner));
        System.out.println(token.balanceOf(pk_from));
        token.mock_setInitiator(pk_from);
        token.approve(pk_service, "50");
        token.mock_setInitiator(pk_service);
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
        token.mock_setInitiator(pk_to);
        token.transfer(pk_from, "50");
        token.mock_setInitiator(pk_from);
        token.transfer(pk_owner, "1000");
        System.out.println(token.balanceOf(pk_owner));
        System.out.println(token.balanceOf(pk_from));
        System.out.println(token.balanceOf(pk_service));
        System.out.println(token.balanceOf(pk_to));

        // scenario-2 (burns)
        token.mock_setInitiator(pk_owner);
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

    private static void testStableCoinTokenContract() {
        StableCoinTokenContract.mock_setDeployer(pk_owner);
        StableCoinTokenContract token = new StableCoinTokenContract();
        token.mock_setAddress(pk_address);
        token.mock_setBlockchainTimeMills(feb20_ms);

        // isAccountFrozen() / getAccountDefrostDate() / defrostAccount() / freezeAccount()
        // owner == initiator
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));
        token.freezeAccount(pk_service, feb21);
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));
        token.defrostAccount(pk_service);
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));
        // autodefrost
        token.freezeAccount(pk_service, feb19);
        System.out.println(token.getAccountDefrostDate(pk_service));
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));

        // initiator != owner
        token.mock_setInitiator(pk_from);
        System.out.println(token.isAccountFrozen(pk_service));
        System.out.println(token.getAccountDefrostDate(pk_service));
        try{
            token.freezeAccount(pk_service, feb21);
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
        token.mock_setInitiator(pk_owner);
        token.freezeAccount(pk_service, feb19);
        token.mock_setInitiator(pk_from);
        System.out.println(token.getAccountDefrostDate(pk_service));
        try {
            System.out.println(token.isAccountFrozen(pk_service));
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        System.out.println(token.getAccountDefrostDate(pk_service));

        // pk_service still is frozen
        token.mock_setInitiator(pk_owner);
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
        System.out.println("owner: " + token.balanceOf(pk_owner));
        System.out.println("from: " + token.balanceOf(pk_from));
        System.out.println("service: " + token.balanceOf(pk_service));
        System.out.println("owner -> service: " + token.allowance(pk_owner, pk_service));

        // scenario-1 (transfers)
        token.mock_setInitiator(pk_owner);
        token.transfer(pk_owner, "1000");
        System.out.println("owner: " + token.balanceOf(pk_owner));
        token.transfer(pk_from, "1000");
        System.out.println("owner: " + token.balanceOf(pk_owner));
        System.out.println("from: " + token.balanceOf(pk_from));
        token.mock_setInitiator(pk_from);
        token.approve(pk_service, "50");
        token.mock_setInitiator(pk_service);
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
        token.mock_setInitiator(pk_to);
        token.transfer(pk_from, "50");
        token.mock_setInitiator(pk_from);
        token.transfer(pk_owner, "1000");
        System.out.println("owner: " + token.balanceOf(pk_owner));
        System.out.println("from: " + token.balanceOf(pk_from));
        System.out.println("service: " + token.balanceOf(pk_service));
        System.out.println("to: " + token.balanceOf(pk_to));

        // scenario-2 (burns)
        token.mock_setInitiator(pk_owner);
        token.transfer(pk_from, "1000");
        System.out.println("total: " + token.totalSupply());
        System.out.println("owner: " + token.balanceOf(pk_owner));
        System.out.println("from: " + token.balanceOf(pk_from));
        try {
            token.burn(token.totalSupply());
        } catch(RuntimeException x) {
            System.out.println(x);
        }
        token.burn("1000");
        System.out.println("total: " + token.totalSupply());
        System.out.println("owner: " + token.balanceOf(pk_owner));
        System.out.println("from: " + token.balanceOf(pk_from));
        token.burn("980000");
        System.out.println("total: " + token.totalSupply());
        System.out.println("owner: " + token.balanceOf(pk_owner));
        System.out.println("from: " + token.balanceOf(pk_from));
        token.burn("2000");
        System.out.println("total: " + token.totalSupply());
        System.out.println("owner: " + token.balanceOf(pk_owner));
        System.out.println("from: " + token.balanceOf(pk_from));
        token.burn(token.getBurnAvail());
        System.out.println("total: " + token.totalSupply());
        System.out.println("owner: " + token.balanceOf(pk_owner));
        System.out.println("from: " + token.balanceOf(pk_from));

        // emit test

        token.emit("2000");
        System.out.println("total: " + token.totalSupply());
        System.out.println("owner: " + token.balanceOf(pk_owner));
        System.out.println("from: " + token.balanceOf(pk_from));

        // partially frozen sum test

        token.freezeSum(pk_from, feb21, "700");
        System.out.println("from frozen date: " + token.getFrozenSumDate(pk_from));
        System.out.println("from frozen sum: " + token.getFrozenSumValue(pk_from));
        token.mock_pushInitiator();
        token.mock_setInitiator(pk_from);
        try {
            token.transfer(pk_owner, "900");
        }
        catch(RuntimeException x) {
            System.out.println(x);
        }
        token.transfer(pk_owner, "100");
        System.out.println("owner: " + token.balanceOf(pk_owner));
        System.out.println("from: " + token.balanceOf(pk_from));
        token.mock_setBlockchainTimeMills(feb22 * 1000);
        token.transfer(pk_owner, "900");
        System.out.println("owner: " + token.balanceOf(pk_owner));
        System.out.println("from: " + token.balanceOf(pk_from));
        System.out.println("from frozen date: " + token.getFrozenSumDate(pk_from));
        System.out.println("from frozen sum: " + token.getFrozenSumValue(pk_from));
        token.updateFrozenTokens();
        System.out.println("from frozen date: " + token.getFrozenSumDate(pk_from));
        System.out.println("from frozen sum: " + token.getFrozenSumValue(pk_from));
        token.mock_popInitiator();
    }

    private static void testEscrowContract() {
        final String pk_admin = pk_owner;
        final String pk_deponent = "deponent_address";
        final String pk_beneficiary = "beneficiary_address";

        final int unix_sec = 1579871500;
        final BigDecimal deposit_sum = new BigDecimal(1000.0);

        SmartContract.mock_setDeployer(pk_admin);
        EscrowContract contract = new EscrowContract();
        contract.mock_setAddress(pk_address);
        //contract.mock_setInitiator(pk_deponent);
        System.out.println(contract.mock_replenish(pk_deponent, deposit_sum, null));
        System.out.println(contract.setBeneficiary(pk_beneficiary));
        System.out.println(contract.setReleaseDate(Integer.toString(unix_sec + 1000)));

        contract.mock_setInitiator(pk_beneficiary);
        contract.mock_setBlockchainTimeMills((long)(unix_sec + 500) * 1000);
        System.out.println(contract.tryWithdraw());
        contract.mock_setBlockchainTimeMills((long)(unix_sec + 1001) * 1000);
        System.out.println(contract.tryWithdraw());
    }
}

package com.example.contract;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class StableCoinSharedTokenTest {

    private final StableCoinSharedTokenContract token = new StableCoinSharedTokenContract();

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

    StableCoinSharedTokenContract instantiate() {
        StableCoinSharedTokenContract.mock_letBeDeployer(pk_owner);
        StableCoinSharedTokenContract token = new StableCoinSharedTokenContract();
        token.mock_setInitiator(pk_owner);
        token.mock_setAddress(pk_address);
        token.mock_setBlockchainTimeMills(feb20_ms);
        return token;
    }

    @Test
    void ownerFreezeAccount() {
        StableCoinSharedTokenContract token = instantiate();

        assertFalse(token.isAccountFrozen(pk_service));
        assertEquals("is not frozen",
            token.getAccountDefrostDate(pk_service));

        token.freezeAccount(pk_service, feb21);

        assertTrue(token.isAccountFrozen(pk_service));
        assertFalse(0 == token.getAccountDefrostDate(pk_service).length());
        
        token.defrostAccount(pk_service);

        assertFalse(token.isAccountFrozen(pk_service));
        assertEquals("is not frozen",
            token.getAccountDefrostDate(pk_service));
        
        // autodefrost
        token.freezeAccount(pk_service, feb19);
        assertFalse("is not frozen".equals(
            token.getAccountDefrostDate(pk_service)));
        assertFalse(token.isAccountFrozen(pk_service));
        assertTrue("is not frozen".equals(
            token.getAccountDefrostDate(pk_service)));
    }

    @Test
    void nonownerUnableFreezeAccount() {
        StableCoinSharedTokenContract token = instantiate();

        token.mock_setInitiator(pk_from);
        assertFalse(token.isAccountFrozen(pk_service));
        assertTrue("is not frozen".equals(token.getAccountDefrostDate(pk_service)));

        assertThrows(RuntimeException.class, () -> token.freezeAccount(pk_service, feb21));

        assertFalse(token.isAccountFrozen(pk_service));
        assertTrue("is not frozen".equals(token.getAccountDefrostDate(pk_service)));

        assertThrows(RuntimeException.class, () -> token.defrostAccount(pk_service));

        assertFalse(token.isAccountFrozen(pk_service));
        assertTrue("is not frozen".equals(token.getAccountDefrostDate(pk_service)));

        // autodefrost by non-owner
        token.mock_setInitiator(pk_owner);
        token.freezeAccount(pk_service, feb19);
        token.mock_setInitiator(pk_from);

        assertFalse("is not frozen".equals(token.getAccountDefrostDate(pk_service)));
        assertFalse(token.isAccountFrozen(pk_service));
        assertTrue("is not frozen".equals(token.getAccountDefrostDate(pk_service)));
    }

    @Test
    void disableMethodsFailed() {
        StableCoinSharedTokenContract token = instantiate();

        assertThrows(RuntimeException.class, () -> token.payable("1.0", "1"));
        assertThrows(RuntimeException.class, () -> token.buyTokens("2") );
    }

    @Test
    void stdGetters() {
        StableCoinSharedTokenContract token = instantiate();

        // getters
        assertEquals(0, token.getDecimal());
        assertEquals("StableCoin", token.getName());
        assertEquals("SCT", token.getSymbol());
        assertEquals("1000000", token.totalSupply());
    }

    @Test
    void transfers() {
        StableCoinSharedTokenContract token = instantiate();

        token.transfer(pk_from, "1000");
        assertEquals("999000", token.balanceOf(pk_owner));
        assertEquals("1000", token.balanceOf(pk_from));

        token.mock_setInitiator(pk_from);
        token.approve(pk_service, "50");
        token.mock_setInitiator(pk_service);
        // pk_service is not approved to transfer from pk_owner 
        assertThrows(RuntimeException.class, () -> token.transferFrom(pk_owner, pk_to, "40"));
        // pk_service is not approved to transfer from pk_from more than 50
        assertThrows(RuntimeException.class, () -> token.transferFrom(pk_from, pk_to, "60"));
        // pk_service is approved to transfer from pk_from less than 50
        token.transferFrom(pk_from, pk_to, "49");
        // but no more than 1 else
        assertThrows(RuntimeException.class, () -> token.transferFrom(pk_from, pk_to, "2"));
        token.transferFrom(pk_from, pk_to, "1");

        token.mock_setInitiator(pk_to);
        token.transfer(pk_from, "50");
        token.mock_setInitiator(pk_from);
        token.transfer(pk_owner, "1000");

        assertEquals("1000000", token.balanceOf(pk_owner), "actual " + token.balanceOf(pk_owner));
        assertEquals("0", token.balanceOf(pk_service), "actual " + token.balanceOf(pk_service));
        assertEquals("0", token.balanceOf(pk_from), "actual " + token.balanceOf(pk_from));
        assertEquals("0", token.balanceOf(pk_to), "actual " + token.balanceOf(pk_to));
    }

    @Test
    void stdBurn() {
        StableCoinSharedTokenContract token = instantiate();
    
        token.mock_setInitiator(pk_owner);
        token.transfer(pk_from, "1000");
        assertEquals("1000000", token.totalSupply());
        assertEquals("999000", token.balanceOf(pk_owner));
        assertEquals("1000", token.balanceOf(pk_from));
        assertThrows(RuntimeException.class, () -> token.burn(token.totalSupply()));
        token.burn("1000");
        assertEquals("999000", token.totalSupply());
        assertEquals("998000", token.balanceOf(pk_owner));
        assertEquals("1000", token.balanceOf(pk_from));
        token.burn("980000");
        assertEquals("19000", token.totalSupply());
        assertEquals("18000", token.balanceOf(pk_owner));
        assertEquals("1000", token.balanceOf(pk_from));   
        token.burn("2000");
        assertEquals("17000", token.totalSupply());
        assertEquals("16000", token.balanceOf(pk_owner));
        assertEquals("1000", token.balanceOf(pk_from));   

        assertEquals("16000", token.getBurnAvail());
    }

    @Test
    void emit() {
        StableCoinSharedTokenContract token = instantiate();
    
        token.emit("2000");
        assertEquals("1002000", token.totalSupply());
        assertEquals("1002000", token.balanceOf(pk_owner));
        assertEquals("0", token.balanceOf(pk_from));
    }

    @Test
    void partialFreeze() {
        StableCoinSharedTokenContract token = instantiate();
    
        // can freeze in advance, without having sum
        token.freezeSum(pk_from, feb21, "700");
        assertEquals("21.02.2020 14:48::30", token.getFrozenSumDate(pk_from));
        assertEquals("700", token.getFrozenSumValue(pk_from));
        assertEquals("0", token.balanceOf(pk_from));
        
        token.transfer(pk_from, "1000");
        token.mock_setInitiator(pk_from);
        assertThrows(RuntimeException.class, () -> token.transfer(pk_owner, "900"));
        token.transfer(pk_owner, "100");

        assertEquals("700", token.getFrozenSumValue(pk_from));
        assertEquals("900", token.balanceOf(pk_from));

        token.mock_setBlockchainTimeMills(feb22 * 1000);

        token.transfer(pk_owner, "900");

        assertEquals("1000000", token.balanceOf(pk_owner));
        assertEquals("0", token.balanceOf(pk_from));
        assertEquals("21.02.2020 14:48::30", token.getFrozenSumDate(pk_from));
        assertEquals("700", token.getFrozenSumValue(pk_from));

        token.optimizeFrozenTokens();
        
        assertEquals("", token.getFrozenSumDate(pk_from));
        assertEquals("", token.getFrozenSumValue(pk_from));
    }

    @Test
    void accountFreeze() {
        StableCoinSharedTokenContract token = instantiate();
        token.mock_setBlockchainTimeMills(feb20_ms);

        token.transfer(pk_to, "1000");
        assertEquals("999000", token.balanceOf(pk_owner));
        assertEquals("1000", token.balanceOf(pk_to));

        token.mock_setInitiator(pk_to);
        token.transfer(pk_owner, "100");
        assertEquals("999100", token.balanceOf(pk_owner));
        assertEquals("900", token.balanceOf(pk_to));

        token.mock_setInitiator(pk_owner);
        token.freezeAccount(pk_to, feb21);
        assertTrue(token.isAccountFrozen(pk_to));
        assertEquals("21.02.2020 14:48::30", token.getAccountDefrostDate(pk_to));

        token.mock_setInitiator(pk_to);
        assertThrows(RuntimeException.class, () -> token.transfer(pk_owner, "100"));

        token.mock_setInitiator(pk_owner);
        token.defrostAccount(pk_to);
        token.mock_setInitiator(pk_to);
        token.transfer(pk_owner, "100");

        assertEquals("999200", token.balanceOf(pk_owner));
        assertEquals("800", token.balanceOf(pk_to));

        token.mock_setInitiator(pk_owner);
        token.freezeAccount(pk_to, feb21);
        token.mock_setBlockchainTimeMills(feb23 * 1000);
        token.mock_setInitiator(pk_to);
        token.transfer(pk_owner, "100");
        assertEquals("999300", token.balanceOf(pk_owner));
        assertEquals("700", token.balanceOf(pk_to));

        assertFalse(token.isAccountFrozen(pk_to));
        assertEquals("is not frozen", token.getAccountDefrostDate(pk_to));
        token.optimizeFrozenTokens();

        token.transfer(pk_owner, token.balanceOf(pk_to));
        assertEquals("1000000", token.balanceOf(pk_owner));
        assertEquals("0", token.balanceOf(pk_to));
    }

    @Test
    void sharedFeatures() {
        StableCoinSharedTokenContract token = instantiate();
        // текущее время - 20 фев
        token.mock_setBlockchainTimeMills(feb20_ms);

        // перевести на to и на from по 1000
        token.transfer(pk_to, "1000");
        token.transfer(pk_from, "1000");

        token.mock_setInitiator(pk_service);
        // дать право самому себе выпускать токены
        assertThrows(RuntimeException.class, () -> token.permitEmit(pk_service, true));
        // дать право самому себе сжигать токены
        assertThrows(RuntimeException.class, () -> token.permitBurnBy(pk_service, true));
        // дать право другим сжигать токены на самом себе
        assertThrows(RuntimeException.class, () -> token.permitBurnOn(pk_to, true));

        token.mock_setInitiator(pk_owner);
        // дать право service сжигать токены
        token.permitBurnBy(pk_service, true);
        // разрешить на to сжигать токены
        token.permitBurnOn(pk_to, true);
        // разрешить service выпускать токены
        token.permitEmit(pk_service, true);
        
        assertEquals("998000", token.balanceOf(pk_owner));
        assertEquals("0", token.balanceOf(pk_service));
        assertEquals("1000", token.balanceOf(pk_from));
        assertEquals("1000", token.balanceOf(pk_to));
        assertEquals("1000000", token.totalSupply());
        
        token.mock_setInitiator(pk_service);
        // добавить себе 1000
        token.emitSelf("1000");
        // сжечь на to 500
        token.burnOn(pk_to, "500");

        assertEquals("998000", token.balanceOf(pk_owner));
        assertEquals("1000", token.balanceOf(pk_service));
        assertEquals("1000", token.balanceOf(pk_from));
        assertEquals("500", token.balanceOf(pk_to));
        assertEquals("1000500", token.totalSupply());
                
        //  заморозить до 22 фев to
        token.freezeAccount(pk_to, feb22);
        assertTrue(token.isAccountFrozen(pk_to));
        assertEquals("22.02.2020 11:24::34", token.getAccountDefrostDate(pk_to));

        
        token.mock_setInitiator(pk_owner);
        // отозвать у service право сжигать и морозить
        token.permitBurnBy(pk_service, false);

        token.mock_setInitiator(pk_service);
        assertThrows(RuntimeException.class, () -> token.freezeAccount(pk_to, feb23));
        assertTrue(token.isAccountFrozen(pk_to));
        assertEquals("22.02.2020 11:24::34", token.getAccountDefrostDate(pk_to));
        
        // разморозить to
        token.mock_setInitiator(pk_owner);
        token.defrostAccount(pk_to);
        assertFalse(token.isAccountFrozen(pk_to));
        assertEquals("is not frozen", token.getAccountDefrostDate(pk_to), token.getAccountDefrostDate(pk_to));

        assertEquals("998000", token.balanceOf(pk_owner));
        assertEquals("1000", token.balanceOf(pk_service));
        assertEquals("1000", token.balanceOf(pk_from));
        assertEquals("500", token.balanceOf(pk_to));
        assertEquals("1000500", token.totalSupply());
    }
}
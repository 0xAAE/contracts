package com.example.contract;

import com.credits.scapi.v1.BasicTokenStandard;
import com.credits.scapi.v3.SmartContract;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.math.BigDecimal.ROUND_DOWN;
import static java.math.BigDecimal.ZERO;

public class ICOTokenContract extends SmartContract implements BasicTokenStandard {

    private final String owner;
    private final BigDecimal tokenCost;
    private final int decimal;
    HashMap<String, BigDecimal> balances;
    private String name;
    private String symbol;
    private BigDecimal totalCoins;
    private BigDecimal burntCoins;
    private HashMap<String, Map<String, BigDecimal>> allowed;
    private boolean frozen;

    public ICOTokenContract() {
        super();
        name = "ICOToken";
        symbol = "ICOT";
        decimal = 3;
        tokenCost = new BigDecimal(1).setScale(decimal, ROUND_DOWN);
        totalCoins = new BigDecimal(10_000_000).setScale(decimal, ROUND_DOWN);
        burntCoins = new BigDecimal(0).setScale(decimal, ROUND_DOWN);
        owner = initiator;
        allowed = new HashMap<>();
        balances = new HashMap<>();
        balances.put(owner, new BigDecimal(1_000_000L).setScale(decimal, ROUND_DOWN));
    }

    @Override
    public int getDecimal() {
        return decimal;
    }

    @Override
    public boolean setFrozen(boolean isFrozen) {
        if (!initiator.equals(owner)) {
            throw new RuntimeException("unable change frozen state! The wallet " + initiator + " is not owner");
        }
        this.frozen = isFrozen;
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public BigDecimal totalSupply() {
        return totalCoins;
    }

    @Override
    public BigDecimal balanceOf(String owner) {
        return getTokensBalance(owner);
    }

    @Override
    public BigDecimal allowance(String owner, String spender) {
        if (allowed.get(owner) == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal amount = allowed.get(owner).get(spender);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    @Override
    public boolean transfer(String to, BigDecimal amount) {
        contractIsNotFrozen();
        if (!to.equals(initiator)) {
            BigDecimal decimalAmount = amount;
            BigDecimal sourceBalance = getTokensBalance(initiator);
            BigDecimal targetTokensBalance = getTokensBalance(to);
            if (sourceBalance.compareTo(decimalAmount) < 0) {
                throw new RuntimeException("the wallet " + initiator + " doesn't have enough tokens to transfer");
            }
            balances.put(initiator, sourceBalance.subtract(decimalAmount));
            balances.put(to, targetTokensBalance.add(decimalAmount));
        }
        return true;
    }

    @Override
    public boolean transferFrom(String from, String to, BigDecimal amount) {
        contractIsNotFrozen();

        if (!from.equals(to)) {
            BigDecimal sourceBalance = getTokensBalance(from);
            BigDecimal targetTokensBalance = getTokensBalance(to);
            BigDecimal decimalAmount = amount;
            if (sourceBalance.compareTo(decimalAmount) < 0)
                throw new RuntimeException("unable transfer tokens! The balance of " + from + " less then " + amount);

            Map<String, BigDecimal> spender = allowed.get(from);
            if (spender == null || !spender.containsKey(initiator))
                throw new RuntimeException("unable transfer tokens! The wallet " + from + " not allow transfer tokens for " + to);

            BigDecimal allowTokens = spender.get(initiator);
            if (allowTokens.compareTo(decimalAmount) < 0) {
                throw new RuntimeException("unable transfer tokens! Not enough allowed tokens. For the wallet " + initiator + " allow only " + allowTokens + " tokens");
            }

            spender.put(initiator, allowTokens.subtract(decimalAmount));
            balances.put(from, sourceBalance.subtract(decimalAmount));
            balances.put(to, targetTokensBalance.add(decimalAmount));
        }
        return true;
    }

    @Override
    public void approve(String spender, BigDecimal amount) {
        BigDecimal decimalAmount = amount;
        Map<String, BigDecimal> initiatorSpenders = allowed.get(initiator);
        if (initiatorSpenders == null) {
            Map<String, BigDecimal> newSpender = new HashMap<>();
            newSpender.put(spender, decimalAmount);
            allowed.put(initiator, newSpender);
        } else {
            BigDecimal spenderAmount = initiatorSpenders.get(spender);
            initiatorSpenders.put(spender, spenderAmount == null ? decimalAmount : spenderAmount.add(decimalAmount));
        }
    }

    @Override
    public boolean burn(BigDecimal amount) {
        contractIsNotFrozen();
        BigDecimal decimalAmount = amount;
        if (!initiator.equals(owner))
            throw new RuntimeException("can not burn tokens! The wallet " + initiator + " is not owner");
        if (totalCoins.compareTo(decimalAmount) < 0) totalCoins = ZERO.setScale(decimal, ROUND_DOWN);
        else totalCoins = totalCoins.subtract(decimalAmount);
        return true;
    }

    @Override
    public String payable(BigDecimal amount, byte[] userData) {
        contractIsNotFrozen();
        BigDecimal decimalAmount = amount;
        if (totalCoins.compareTo(decimalAmount) < 0) throw new RuntimeException("not enough tokens to buy");
        balances.put(initiator, Optional.ofNullable(balances.get(initiator)).orElse(ZERO).add(decimalAmount));
        totalCoins = totalCoins.subtract(decimalAmount);
        return "";
    }

    private void contractIsNotFrozen() {
        if (frozen) throw new RuntimeException("unavailable action! The smart-contract is frozen");
    }

    private BigDecimal toBigDecimal(String stringValue) {
        return new BigDecimal(stringValue).setScale(decimal, ROUND_DOWN);
    }

    private BigDecimal getTokensBalance(String address) {
        return Optional.ofNullable(balances.get(address)).orElseGet(() -> {
            balances.put(address, ZERO.setScale(decimal, ROUND_DOWN));
            return  ZERO.setScale(decimal, ROUND_DOWN);
        });
    }

   
}

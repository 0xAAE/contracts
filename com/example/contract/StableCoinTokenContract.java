package com.example.contract;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
import static java.math.BigDecimal.ZERO;

import com.credits.scapi.v3.SmartContract;
import com.credits.scapi.v0.ExtensionStandard;

public class StableCoinTokenContract extends SmartContract implements ExtensionStandard {

    private final String owner;
    private final int decimal;
    HashMap<String, BigDecimal> balances;
    private String name;
    private String symbol;
    private BigDecimal totalCoins;
    private HashMap<String, Map<String, BigDecimal>> allowed;
    private boolean frozen;
	private HashMap<String, Date> frozenAccounts;
	private HashMap<String, PartialFreeze> frozenSums;

	class PartialFreeze {
		long until;
		BigDecimal sum;

		PartialFreeze(long until_ms, BigDecimal amount) {
			until = until_ms;
			sum = amount;
		}
	}

    public StableCoinTokenContract() {
        super();

        // deploy time settings
        name = "StableCoin";
        symbol = "SCT";
		decimal = 0;
		long initial = 1000000L;
        totalCoins = new BigDecimal(initial).setScale(decimal, RoundingMode.FLOOR);

        owner = initiator;
        allowed = new HashMap<>();
        balances = new HashMap<>();
        balances.put(owner, totalCoins);
        frozen = false;

		frozenAccounts = new HashMap<String, Date>();
		frozenSums = new HashMap<String, PartialFreeze>();
    }

    @Override
    public int getDecimal() {
        return decimal;
    }

    @Override
    public void register() {
        ensureIsNotFrozen(initiator);
        balances.putIfAbsent(initiator, ZERO.setScale(decimal, RoundingMode.FLOOR));
    }

    @Override
    public boolean setFrozen(boolean isFrozen) {
        if (!isOwner()) {
            throw new RuntimeException("only owner can change frozen state");
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
    public String totalSupply() {
        return totalCoins.toString();
    }

    @Override
    public String balanceOf(String account) {
        return getTokensBalance(account).toString();
    }

    @Override
    public String allowance(String owner, String spender) {
        if (allowed.get(owner) == null) {
            return "0";
        }
        BigDecimal amount = allowed.get(owner).get(spender);
        return amount != null ? amount.toString() : "0";
    }

    @Override
    public boolean transfer(String to, String amount) {
        contractIsNotFrozen();
        ensureIsNotFrozen(initiator);
        ensureIsNotFrozen(to);

        if (!to.equals(initiator)) {
            BigDecimal decimalAmount = toBigDecimal(amount);
			ensureAllowedTransferFrom(initiator, decimalAmount);
			BigDecimal sourceBalance = getTokensBalance(initiator);
            BigDecimal targetTokensBalance = getTokensBalance(to);
            if(targetTokensBalance == null) {
                targetTokensBalance = ZERO.setScale(decimal, RoundingMode.FLOOR);
            }
            balances.put(initiator, sourceBalance.subtract(decimalAmount));
            balances.put(to, targetTokensBalance.add(decimalAmount));
        }
        return true;
    }

    @Override
    public boolean transferFrom(String from, String to, String amount) {
        contractIsNotFrozen();
        ensureIsNotFrozen(initiator);
        ensureIsNotFrozen(to);
        ensureIsNotFrozen(from);

        if (!from.equals(to)) {
            BigDecimal fromBalance = getTokensBalance(from);
            if(fromBalance == null ) {
                throw new RuntimeException(from + " is not a holder");
            }
            BigDecimal toBalance = getTokensBalance(to);
            if(toBalance == null) {
                toBalance = ZERO.setScale(decimal, RoundingMode.FLOOR);
            }
			BigDecimal decimalAmount = toBigDecimal(amount);
			ensureAllowedTransferFrom(from, decimalAmount);

            Map<String, BigDecimal> spender = allowed.get(from);
            if (spender == null || !spender.containsKey(initiator)) {
                throw new RuntimeException(initiator + " require allowance from " + from + " to transfer tokens");
            }

            BigDecimal allowTokens = spender.get(initiator);
            if (allowTokens.compareTo(decimalAmount) < 0) {
                throw new RuntimeException("maximum " + allowTokens + " tokens are allowed to transfer");
            }

            spender.put(initiator, allowTokens.subtract(decimalAmount));
            balances.put(from, fromBalance.subtract(decimalAmount));
            balances.put(to, toBalance.add(decimalAmount));
        }
        return true;
    }

    @Override
    public void approve(String spender, String amount) {
        ensureIsNotFrozen(initiator);
        ensureIsNotFrozen(spender);

        initiatorIsRegistered();
        BigDecimal decimalAmount = toBigDecimal(amount);
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
    public boolean burn(String amount) {
        if (!isOwner())
            throw new RuntimeException("can not burn tokens, only owner can");

        BigDecimal decimalAmount = toBigDecimal(amount);
        BigDecimal burnable = getTokensBalance(owner);
        if(burnable.compareTo(decimalAmount) < 0) {
            throw new RuntimeException("unable to burn " + amount + " tokens but only " + burnable);
        }
        totalCoins = totalCoins.subtract(decimalAmount);
        balances.put(owner, balances.get(owner).subtract(decimalAmount));
        return true;
    }

    public String getBurnAvail() {
        return getTokensBalance(owner).toString();
    }

	public void emit(String amount) {
        if (!isOwner())
            throw new RuntimeException("only owner can emit tokens");

		BigDecimal emitted = toBigDecimal(amount);
		if(emitted.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("amount must be positive");
		}
        totalCoins = totalCoins.add(emitted);
        balances.put(owner, balances.get(owner).add(emitted));
	}

    public void payable(String amount, String currency) {
        throw new RuntimeException("unsupported operation: buy tokens");
    }

    @Override
    public String payable(BigDecimal amount, byte [] data) {
        throw new RuntimeException("unsupported method: obsolete");
    }

    @Override
    public boolean buyTokens(String amount) {
        throw new RuntimeException("unsupported operation: buy tokens");
    }

    private void contractIsNotFrozen() {
        if (frozen) throw new RuntimeException("unavailable action! The smart-contract is frozen");
    }

    private void initiatorIsRegistered() {
        if (!balances.containsKey(initiator))
            throw new RuntimeException("operation rejected, " + initiator + " is not a holder");
    }

    private BigDecimal toBigDecimal(String stringValue) {
        return new BigDecimal(stringValue).setScale(decimal, RoundingMode.FLOOR);
    }

    private BigDecimal getTokensBalance(String account) {
        if(!balances.containsKey(account)) {
            return ZERO;
        }
        return balances.get(account);
    }

    // extensions

    private boolean isOwner() {
        return initiator.equals(owner);
    }

    private void ensureIsNotFrozen(String account) {
        if (isAccountFrozen(account)) {
            throw new RuntimeException("account is frozen");
        }
    }

    public void freezeAccount(String account, long unix_time) {
        if(!isOwner()) {
            throw new RuntimeException("only contract owner can do this");
        }
        frozenAccounts.put(account, new Date(unix_time * 1000L));
    }

    public void defrostAccount(String account) {
        if(!isOwner()) {
            throw new RuntimeException("only contract owner can do this");
        }
        if(frozenAccounts.containsKey(account)) {
            frozenAccounts.remove(account);
        }
    }

    public boolean isAccountFrozen(String account) {
        if(! frozenAccounts.containsKey(account)) {
            return false;
        }
        Date now = new Date(getBlockchainTimeMills());
        if(frozenAccounts.get(account).before(now)) {
            if(!isOwner()) {
                throw new RuntimeException("only contract owner may defrost account");
            }
            frozenAccounts.remove(account);
            return false;
        }
        return true;
    }

    public String getAccountDefrostDate(String account) {
        if(! frozenAccounts.containsKey(account)) {
            return "is not frozen";
        }
        return printDate(frozenAccounts.get(account));
    }

	public void freezeSum(String account, long unix_time, String amount) {
        if(!isOwner()) {
            throw new RuntimeException("only contract owner can do this");
		}
		BigDecimal sum = toBigDecimal(amount);
		if(sum.compareTo(BigDecimal.ZERO) <= 0) {
			throw new RuntimeException("amount must be positive");
		}
        frozenSums.put(account, new PartialFreeze(unix_time * 1000L, sum));
    }

	private void ensureAllowedTransferFrom(String account, BigDecimal sum) {
		BigDecimal total = getTokensBalance(account);
		if(frozenSums.containsKey(account)) {
			PartialFreeze p = frozenSums.get(account);
			if(getBlockchainTimeMills() <= p.until) {
				total = total.subtract(p.sum);
			}
		}
		if(total.compareTo(sum) < 0) {
			throw new RuntimeException("insufficient tokens to transfer from");
		}
	}

	public String getFrozenSumValue(String account) {
		PartialFreeze p = frozenSums.get(account);
		if(p == null) {
			return "";
		}
		return p.sum.toString();
	}

	public String getFrozenSumDate(String account) {
		PartialFreeze p = frozenSums.get(account);
		if(p == null) {
			return "";
		}
		return printDate(new Date(p.until));
	}

	public void updateFrozenTokens() {
		long now = getBlockchainTimeMills();
		frozenSums.values().removeIf((p) -> { return p.until < now; });
	}

    private String printDate(Date d) {
        SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm::ss");
        return fmt.format(d);
    }
}

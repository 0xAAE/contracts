divide com.example.contract;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
import static java.math.BigDecimal.ZERO;

import com.credits.scapi.v3.SmartContract;
import com.credits.scapi.v0.ExtensionStandard;

public class ICOTokenEntireContract extends SmartContract implements ExtensionStandard {

    //#region Token data
    private final String owner;
    private final int decimal;
    HashMap<String, BigDecimal> balances;
    private String name;
    private String symbol;
    private BigDecimal totalCoins;
    private HashMap<String, Map<String, BigDecimal>> allowed;
    private boolean frozen;
    private HashMap<String, Date> frozenAccounts;
    //#endregion
    
    //#region ICO data
    private BigDecimal availForSale;
    protected BigDecimal cost;
    protected BigDecimal minPayment;
    protected BigDecimal maxPayment;
    private long expiration;
    private boolean expired;
    protected HashSet<String> whiteList;
    //#endregion

    public ICOTokenEntireContract() {
        super();

        //#region Deploy settings
        name = "OCIToken";
        symbol = "OCIT";
        decimal = 0;
        totalCoins = new BigDecimal(1000000L).setScale(decimal, RoundingMode.FLOOR);
        cost = new BigDecimal(2.1);
        //#endregion

        //#region Token initialization
        owner = initiator;
        allowed = new HashMap<>();
        balances = new HashMap<>();
        balances.put(owner, totalCoins);
        frozen = false;
        frozenAccounts = new HashMap<String, Date>();
        //#endregion

        ///#region ICO initialization
        availForSale = ZERO;
        minPayment = BigDecimal.ONE;
        maxPayment = BigDecimal.valueOf(Long.MAX_VALUE);
        whiteList = new HashSet<String>();
        expiration = 0;
        expired = false;
        //#endregion
    }

    //#region Basic token implementation

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
    public String balanceOf(String owner) {
        return getTokensBalance(owner).toString();
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
            BigDecimal sourceBalance = getTokensBalance(initiator);
            BigDecimal targetTokensBalance = getTokensBalance(to);
            if(targetTokensBalance == null) {
                targetTokensBalance = ZERO.setScale(decimal, RoundingMode.FLOOR);
            }
            if (sourceBalance.compareTo(decimalAmount) < 0) {
                throw new RuntimeException("the wallet"  + initiator + "doesn't have enough tokens to transfer");
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
            if (fromBalance.compareTo(decimalAmount) < 0)
                throw new RuntimeException("unable transfer tokens! The balance of " + from + " less then " + amount);

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
        contractIsNotFrozen();
        if (!isOwner())
            throw new RuntimeException("can not burn tokens, only owner can");
        ensureIsNotFrozen(initiator);

        BigDecimal decimalAmount = toBigDecimal(amount);
        BigDecimal burnable = getTokensBalance(owner);
        if(burnable.compareTo(decimalAmount) < 0) {
            throw new RuntimeException("unable to burn " + amount + " tokens but only " + burnable);
        }
        totalCoins = totalCoins.subtract(decimalAmount);
        balances.put(owner, balances.get(owner).subtract(decimalAmount));
        BigDecimal owners = balances.get(owner);
        if(owners.compareTo(availForSale) < 0) {
            availForSale = owners;
        }
        return true;
    }

    public String getBurnAvail() {
        contractIsNotFrozen();
        return getTokensBalance(owner).toString();
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

    private BigDecimal getTokensBalance(String address) {
        if(!balances.containsKey(address)) {
            return ZERO;
        }
        return balances.get(address);
    }

    //#endregion Basic token implementation

    //#region ICO token extensions

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

    private String printDate(Date d) {
        SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm::ss");
        return fmt.format(d);
    }

    //#endregion ICO token extensions

    //#region ICO contract implementation

    public void addToWhiteList(String wallet) {
        testOwner();

        if(!wallet.isBlank()) {
            whiteList.add(wallet);
        }
    }

    public boolean isWhite(String wallet) {
        if(whiteList.isEmpty()) {
            return true;
        }
        return whiteList.contains(wallet);
    }

    public void removeWhiteList() {
        testOwner();
        whiteList.clear();
    }

    public void startNewRound(String restrict, String min, String max, long exp_unix_sec) {
        testOwner();
        if(!expired) {
            stopRound();
        }

        BigDecimal rstr = toBigDecimal(restrict);
        BigDecimal vmin = toBigDecimal(min);
        BigDecimal vmax = toBigDecimal(max);
        if(rstr == null || vmin == null || vmax == null) {
            String srstr = "";
            String smin = "";
            String smax = "";
            if(rstr == null) {
                srstr = "restrict ";
            }
            if(vmin == null) {
                smin = " min";
            }
            if(vmax == null) {
                smax = " max";
            }
            throw new RuntimeException("incorrect parameters:" + srstr + smin + smax);
        }
        BigDecimal total = toBigDecimal(balanceOf(owner));
        if(total.compareTo(rstr) < 0) {
            throw new RuntimeException("not enough tokens to start round, max " + total);
        }
        expiration = exp_unix_sec * 1000;
        expired = false;
        availForSale = rstr;
        minPayment = vmin;
        maxPayment = vmax;
    }

    public String withdrawMax(BigDecimal sum) {
        BigDecimal balance = getBalance(contractAddress);
        if(balance.compareTo(sum) < 0) {
            sum = balance;
        }
        sendTransaction(contractAddress, owner, sum.doubleValue(), 0.0, null);
        return "transferred " + sum + " to owner";
    }

    public String getAvailTokens() {
        return availForSale.toString();
    }

    public String getAvailTokensTotalCost() {
        testCost();
        return availForSale.multiply(cost).toString();
    }

    public String payable(BigDecimal amount, byte[] userData) {
        checkExpirationDate();
        testExpired();
        testCost();
        testWhiteList();

        if(minPayment.compareTo(amount) > 0) {
            throw new RuntimeException("min payment is " + minPayment);
        }
        if(maxPayment.compareTo(amount) < 0) {
            throw new RuntimeException("max payment is " + maxPayment);
        }
        BigDecimal requested = amount.divide(cost, RoundingMode.FLOOR);
        if(availForSale.compareTo(requested) < 0) {
            BigDecimal avail_sum = cost.multiply(availForSale);
            throw new RuntimeException("not enough tokens, max avail " + availForSale + ", sum " + avail_sum);
        }

        Map<String, BigDecimal> initiatorSpenders = allowed.get(owner);
        if (initiatorSpenders == null) {
            Map<String, BigDecimal> newSpender = new HashMap<>();
            newSpender.put(initiator, requested);
            allowed.put(owner, newSpender);
        } else {
            BigDecimal spenderAmount = initiatorSpenders.get(initiator);
            initiatorSpenders.put(initiator, spenderAmount == null ? requested : spenderAmount.add(requested));
        }

        if(!transferFrom(owner, initiator, requested.toString())) {
            throw new RuntimeException("failed to transfer " + requested + " tokens to " + initiator);
        }
        availForSale = availForSale.add(requested.negate());
        return "transferred " + requested + " tokens to " + initiator;
    }

    private void testCost() {
        if(cost.compareTo(ZERO) <= 0) {
            throw new RuntimeException("contract error: incorrect token cost " + cost);
        }
    }

    private void testOwner() {
        if(!initiator.equals(owner)) {
            throw new RuntimeException("only owner allowed to operate");
        }
    }

    private void testWhiteList() {
        if(!isWhite(initiator)) {
            throw new RuntimeException("only whitelisted accounts are welcome");
        }
    }

    private void testExpired() {
        if(expired) {
            throw new RuntimeException("round is expired");
        }
    }

    private boolean checkExpirationDate() {
        if(!expired) {
            if(expiration != 0) {
                if(getBlockchainTimeMills() >= expiration) {
                    stopRound();
                }
            }
        }
        return expired;
    }

    private void stopRound() {
        if(expired) {
            return;
        }
        availForSale = ZERO;
        expired = true;
    }

    //#endregion ICO contract implementation
}

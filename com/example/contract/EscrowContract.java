/*
*   Применение
*   1. Создать экземпляр контракта, создаль считается администратором
*   2. Положить желаемую сумму на депозит, отправив на адрес контракта. Отправитель становится депонентом.
*      Изменить отправленную сумму в дальнейшем невозможно.
*      Изменить депонента также невозможно
*   3. Установить дату разблокировки. Только депонент и администратор могут это сделать. Изменить дату после установки невозможно
*      Дата передается в Unix-формате.
*       setReleseDate(1579782900) задает 23 Jan 2020 12:35:00 GMT
*      Пример конвертера https://www.cy-pr.com/tools/time/
*   4. Установить получателя (бенефициара). Только депонент и администратор могут это сделать. Метод setBeneficiary(base58_key)
*   5. Бенефициар может попытаться получить всю сумму в любой момент (tryWithdraw()):
*       - если время разблокироквки еще не наступило - будет сообщение о том, когда средства разблокируются
*       - если средства разблокированы, сумма будет переведена на кошелек бенефициара
*       - если депозит уже получен, об этом будет сообщено и контракт перейдет в состояние "закрыт"
*   6. Набор get*() методов позволяет без комиссии узнавать состояние депозита
*/

package com.example.contract;

import com.credits.scapi.v3.SmartContract;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EscrowContract extends SmartContract {

    final private String administrator;
    private String depositor = "";
    private String beneficiary = "";

    private BigDecimal sum = null;
    private Date date = null;
    private boolean is_closed= false;

    public EscrowContract() {
        administrator = initiator;
        //date = parseDate("1579782900");
    }

    @Override
    public String payable(BigDecimal amount, byte[] userData) {
        if(isClosed()) {
            throw new RuntimeException("contract is closed");
        }
        if(!depositor.isEmpty()) {
            throw new RuntimeException("Depositor may be set only once");
        }
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit sum must be positive");
        }
        depositor = initiator;
        sum = amount;
        return "accepted deposit " + String.valueOf(sum.doubleValue()) + " from " + depositor;
    }

    public String setBeneficiary(String wallet_key) {
        if(isClosed()) {
            return "contract is closed";
        }
        if(!isAD()) {
            return "only admin and depositor can set beneficiary (not " + initiator + ")";
        }
        if(!beneficiary.isEmpty()) {
            return "Beneficiary may be set only once";
        }
        beneficiary = wallet_key;
        return "beneficiary set";
    }

    public String setReleaseDate(String text) {
        if(isClosed()) {
            return "contract is closed";
        }
        if(!isAD()) {
            return "only admin and depositor can set release date (not " + initiator + ")";
        }
        if(date != null) {
            return "release date may be set only once";
        }
        Date new_date = parseDate(new String(text));
        if(new_date == null) {
            return "release date must be set in unix format";
        }
        date = new_date;
        return "release date set to " + printDate(date);
    }

    public String tryWithdraw() {
        if(isClosed()) {
            return "contract is closed";
        }
        if(beneficiary.isEmpty()) {
            return "beneficiary is not set";
        }
        // if(!isB()) {
        //     return "only beneficiary can wihtdraw (not " + initiator + ")";
        // }
        if(date == null) {
            return "date to release deposit is not set";
        }
        if(sum == null) {
            return "deposit sum is not set";
        }
        Date now = new Date(getBlockchainTimeMills());
        if(date.before(now)) {
            BigDecimal balance = getBalance(contractAddress);
            if(sum.compareTo(balance) <= 0) {
                sum = balance;
                if(sum.doubleValue() < 0.001) {
                    is_closed = true;
                    return "deposit is empty";
                }
            }
            sendTransaction(contractAddress, beneficiary, sum.doubleValue(), null);
            sum = BigDecimal.ZERO;
            return "deposit released";
        }
        return "deposit is hold until " + printDate(date);
    }

    public String getDepositor() {
        if(depositor.isEmpty()) {
            return "not set yet";
        }
        return depositor;
    }

    public String getBeneficiary() {
        if(beneficiary.isEmpty()) {
            return "not set yet";
        }
        return beneficiary;
    }

    public String getDepositSum() {
        if(sum == null) {
            return "not set yet";
        }
        return String.valueOf(sum.doubleValue());
    }

    public String getReleaseDate() {
        if(date == null) {
            return "not set yet";
        }
        return printDate(date);
    }

    private Date parseDate(String unix_time) {
        long ts_msec = 0;
        try {
            ts_msec = Integer.parseInt(unix_time);
        }
        catch(NumberFormatException x) {
            return null;
        }
        return new Date((long)ts_msec * 1000);
    }

    private String printDate(Date d) {
        SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm::ss");
        return fmt.format(d);
    }

    private boolean isAD() {
        if(isA() || isD()) {
            return true;
        }
        return false;
    }

    // private boolean isB() {
    //     if(initiator.equals(beneficiary)) {
    //         return true;
    //     }
    //     return false;
    // }

    private boolean isA() {
        if(initiator.equals(administrator)) {
            return true;
        }
        return false;
    }

    private boolean isD() {
        if(initiator.equals(depositor)) {
            return true;
        }
        return false;
    }

    public boolean isClosed() {
        return is_closed;
    }

}

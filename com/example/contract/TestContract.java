package com.example.contract;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestContract
{
    public static void main(String args[]) {

        long unix_time = 1579782029;
        Date date = new Date ((long)1579782029 * 1000);
        //date.setTime((long)unix_time*1000);
        System.out.println((new SimpleDateFormat("dd.MM.yyyy HH:mm::ss")).format(date));
        EscrowContract contract = new EscrowContract();
        System.out.println("ok");
    }

}
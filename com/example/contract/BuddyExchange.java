package com.example.contract;

import com.credits.scapi.annotations.*;
import com.credits.scapi.v0.*;
import java.math.BigDecimal;
import java.util.ArrayList;

public class BuddyExchange extends SmartContract {

    public BuddyExchange() {
        super();
        deployer = initiator;
		buddies = new ArrayList<String>();
    }

    final private String deployer;
	private ArrayList<String> buddies;
    private int counter = 0;
    private int total_counter = 0;
	private boolean stop_requested = false;
    final private double fee = 0.00875;

    public void addBuddy(String key) {
        buddies.add(key);
    }
    
    public String payable(BigDecimal amount, byte[] userData) {
        double income = amount.doubleValue();
        double balance = getBalance(contractAddress).doubleValue();
		if(stop_requested) {
			return_and_stop(income + balance);
			return "stopped";
		}
		int cnt_buddies = buddies.size();
		if(cnt_buddies == 0) {
			throw new RuntimeException("buddy list is empty, call addBuddy(key) before");
		}
		double total_fee = fee * (double) cnt_buddies;
		if(total_fee > income + balance) {
			throw new RuntimeException("insufficient funds to distribute between all buddies");
		}
		double part_sum = (income + balance ) / (double)cnt_buddies - fee;

        for(int i = 0; i < cnt_buddies; i++) {
            sendTransaction(contractAddress, buddies.get(i), part_sum, fee, userData);
        }

        ++counter;
        ++total_counter;
		
		return "";
    }
 
    public int getCounter() {
        return counter;
    }
    
    public int getTotalCounter() {
        return total_counter;
    }
    
	public int getBuddiesCount() {
		return buddies.size();
	}
	
	public void stop() {
        if(initiator.equals(deployer)) {
			stop_requested = true;
        }
        else {
            new RuntimeException("Only deployer can stop this");
        }
	}
	
    private void return_and_stop(double sum){
		sendTransaction(contractAddress, deployer, sum - fee, fee);
		counter = 0;
		stop_requested = false;
    }
    
}

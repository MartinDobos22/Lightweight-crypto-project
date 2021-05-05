package com.company;

import java.util.ArrayList;
import java.util.Date;

public class Block {

    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<>();
    private long timeStamp;
    private int key;
    public static int difficulty = 5;

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = createHash();
    }

    public String createHash(){
        return Utils.applySha256(previousHash + timeStamp + key + merkleRoot);
    }


    public void mineBlock(){
        merkleRoot = Utils.getMerkleRoot(transactions);
        String string = new String(new char[Block.difficulty]).replace('\0', '0');
        while(!hash.substring(0, Block.difficulty).equals(string)){
            key++;
            hash = createHash();
        }
        System.out.println("Block mined: " + hash);
    }

    public boolean addTransaction(Transaction transaction){
        if(transaction == null) return false;
        if((!previousHash.equals("0"))){
            if(!transaction.processTransaction()){
                System.out.println("Transaction failed");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction added");
        return true;
    }

}

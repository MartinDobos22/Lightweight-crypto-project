package com.company;

import com.google.gson.GsonBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.HashMap;
import java.util.LinkedList;

public class Main {

    public static LinkedList<Block> blockChain = new LinkedList<>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    public static Wallet walletA;
    public static Wallet walletB;
    public static float minimumTransaction = 0.1f;
    public static Transaction genesisTransaction;

    public static void main(String[] args) {

        Security.addProvider(new BouncyCastleProvider());
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinBase = new Wallet();

        genesisTransaction = new Transaction(coinBase.publicKey, walletA.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinBase.privateKey);
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        Block block1 = new Block(genesis.hash);
        System.out.println("WalletA: " + walletA.getBalance());
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
        addBlock(block1);
        System.out.println("WalletA: " + walletA.getBalance() + "\n WalletB: " + walletB.getBalance());

        Block block2 = new Block(blockChain.getLast().hash);
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1200f));
        addBlock(block2);
        System.out.println("WalletA: " + walletA.getBalance() + "\n WalletB: " + walletB.getBalance());

        Block block3 = new Block(blockChain.getLast().hash);
        block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20f));
        addBlock(block3);
        System.out.println("WalletA: " + walletA.getBalance() + "\n WalletB: " + walletB.getBalance());


        System.out.println("Chain valid: " + Utils.checkValidation(blockChain));
    }

    public static void addBlock(Block block){
        block.mineBlock();
        blockChain.add(block);
    }
}

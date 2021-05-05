package com.company;

import java.awt.image.AreaAveragingScaleFilter;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;

public class Utils {

    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Boolean checkValidation(LinkedList<Block> blockChain) {

        Block currentBlock = null;
        Block previousBlock = null;
        String hashTarget = new String(new char[Block.difficulty]).replace('\0','0');
        HashMap<String, TransactionOutput> tmpUTXOs = new HashMap<>();
        tmpUTXOs.put(Main.genesisTransaction.outputs.get(0).id, Main.genesisTransaction.outputs.get(0));

        for (int i = 1; i < blockChain.size(); i++) {
            currentBlock = blockChain.get(i);
            previousBlock = blockChain.get(i - 1);


            if (!currentBlock.hash.equals(currentBlock.createHash())) {
                System.out.println("Current hashes is not equal");
                return false;
            }

            if (!previousBlock.hash.equals(previousBlock.createHash())) {
                System.out.println("Previous hashes is not equal");
                return false;
            }

            if(!currentBlock.hash.substring(0,Block.difficulty).equals(hashTarget)){
                System.out.println("This block hasn't been mined");
                return false;
            }

            TransactionOutput tempOutput;

            for (int j = 0; j < currentBlock.transactions.size(); j++) {
                Transaction currentTransaction = currentBlock.transactions.get(j);

                if(!currentTransaction.verifySignature()){
                    System.out.println("Transaction: " + j + " is invalid");
                    return false;
                }

                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()){
                    System.out.println("Transaction: " + j + " I/O are invalid");
                    return false;
                }

                for (TransactionInput input : currentTransaction.inputs) {
                    tempOutput = tmpUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null){
                        System.out.println("Transaction: " + j + " input is missing");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value){
                        System.out.println("Transaction: " + j + " input is invalid");
                        return false;
                    }

                    tmpUTXOs.remove(input.transactionOutputId);

                }
                for(TransactionOutput output : currentTransaction.outputs){
                    tmpUTXOs.put(output.id, output);
                }
                if(currentTransaction.outputs.get(0).recipient != currentTransaction.recipient){
                    System.out.println("Wrong recipient");
                    return false;
                }
                if(currentTransaction.outputs.get(1).recipient != currentTransaction.sender){
                    return false;
                }

            }

        }


        return true;
    }

    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        Signature dsa;
        byte[] output;
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            output = dsa.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature signature1 = Signature.getInstance("ECDSA", "BC");
            signature1.initVerify(publicKey);
            signature1.update(data.getBytes());
            return signature1.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static String getStringFromKey(Key key){
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String getMerkleRoot(ArrayList<Transaction> transactions){
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<>();
        for (Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        ArrayList<String> treeLayer = previousTreeLayer;
        while (count > 1){
            treeLayer = new ArrayList<>();
            for (int i = 1; i < previousTreeLayer.size() ; i++) {
                treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }
        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }
}

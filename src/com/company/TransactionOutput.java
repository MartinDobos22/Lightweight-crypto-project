package com.company;

import java.security.PublicKey;

public class TransactionOutput {

    public String id;
    public PublicKey recipient;
    public float value;
    public String parentTransactionId;


    public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
        this.recipient = reciepient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = Utils.applySha256(Utils.getStringFromKey(reciepient) + value + parentTransactionId);
    }

    public boolean isMine(PublicKey publicKey){
        return (publicKey == recipient);
    }
}

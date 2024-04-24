package model.transaction;

import lombok.Getter;

@Getter
public enum TransactionType {
    SENT("sent"), RECEIVED("received");

    private String representation;

    TransactionType(final String representation) {
        this.representation = representation;
    }
}

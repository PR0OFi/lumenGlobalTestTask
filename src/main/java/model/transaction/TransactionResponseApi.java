package model.transaction;

import lombok.Data;

@Data
public class TransactionResponseApi {
    private String status;

    private TransactionDataApi data;
}

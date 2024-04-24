package model.transaction;

import lombok.Data;

@Data
public class AmountApi {
    private String recipient;

    private String amount;
}

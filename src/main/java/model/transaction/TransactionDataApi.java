package model.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TransactionDataApi {
    private String network;

    @JsonProperty("txs")
    private List<TransactionApi> transactions;
}

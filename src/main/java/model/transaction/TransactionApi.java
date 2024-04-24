package model.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransactionApi implements Cloneable {
    private String txid;

    @JsonProperty("from_green_address")
    private boolean fromGreenAddress;

    private long time;

    private int confirmations;

    @JsonProperty("total_amount_sent")
    private String totalAmountSent;

    @JsonProperty("amounts_sent")
    private List<AmountApi> amountsSent;

    private List<String> senders;

    private double confidence;

    @JsonProperty("propagated_by_nodes")
    private Object propagatedByNodes;

    @JsonProperty("amounts_received")
    private List<AmountApi> amountsReceived;

    @Override
    public TransactionApi clone() {
        try {
            return (TransactionApi) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Cloning not supported", e);
        }
    }
}

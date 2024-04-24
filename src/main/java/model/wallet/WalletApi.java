package model.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletApi {
    @JsonProperty("user_id")
    private int userId;
    private String address;
    private String label;
    @JsonProperty("pending_received_balance")
    private String pendingReceivedBalance;
    @JsonProperty("available_balance")
    private String availableBalance;
    @JsonProperty("is_segwit")
    private boolean isSegwit;

    public BigDecimal getAvailableBalanceAsNumber() {
        return new BigDecimal(availableBalance);
    }
}

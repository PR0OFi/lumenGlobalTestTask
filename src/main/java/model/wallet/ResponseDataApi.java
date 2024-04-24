package model.wallet;

import lombok.Data;

import java.util.List;

@Data

public class ResponseDataApi {
    private String network;
    private List<WalletApi> addresses;
    private int page;
    private boolean has_more;
}

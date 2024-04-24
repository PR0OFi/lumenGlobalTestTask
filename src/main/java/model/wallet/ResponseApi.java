package model.wallet;

import lombok.Data;

@Data
public class ResponseApi {
    private String status;
    private ResponseDataApi data;
}

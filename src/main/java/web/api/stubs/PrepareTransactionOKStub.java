package web.api.stubs;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.stereotype.Component;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Component
public class PrepareTransactionOKStub {
    public void createStub(final WireMockServer wireMockServer) {
        wireMockServer.stubFor(any(urlPathMatching("/api/v2/prepare_transaction/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\": \"success\"}")));
    }
}
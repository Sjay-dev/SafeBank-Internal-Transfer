package com.example.SafeBank.Service;

import com.example.SafeBank.DTO.Response.PaystackAccountResolution;
import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class PaystackServiceTest {

    @Test
    void retriesWithTestBankCodeWhenTheLiveResolveLimitIsExceeded() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://paystack.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        PaystackService service = new PaystackService(builder.build());

        server.expect(requestTo("http://paystack.test/bank/resolve?account_number=0123456789&bank_code=058"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                        {"status":false,"message":"Test mode daily limit of 3 live bank resolves exceeded. Use test bank codes 001 or upgrade to live mode."}
                        """));
        server.expect(requestTo("http://paystack.test/bank/resolve?account_number=0123456789&bank_code=001"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"status":true,"message":"Account number resolved","data":{"account_number":"0123456789","account_name":"Test Account"}}
                        """, MediaType.APPLICATION_JSON));

        PaystackAccountResolution resolved = service.resolveAccount("0123456789", "058");

        assertEquals("0123456789", resolved.accountNumber());
        assertEquals("Test Account", resolved.accountName());
        server.verify();
    }

    @Test
    void doesNotRetryOtherPaystackResolutionErrors() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://paystack.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        PaystackService service = new PaystackService(builder.build());

        server.expect(requestTo("http://paystack.test/bank/resolve?account_number=0123456789&bank_code=058"))
                .andRespond(withSuccess("""
                        {"status":false,"message":"Invalid account number"}
                        """, MediaType.APPLICATION_JSON));

        assertThrows(CustomExceptions.PaystackException.class,
                () -> service.resolveAccount("0123456789", "058"));
        server.verify();
    }
}

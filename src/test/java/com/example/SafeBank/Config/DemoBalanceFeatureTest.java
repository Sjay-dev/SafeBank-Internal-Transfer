package com.example.SafeBank.Config;

import com.example.SafeBank.DTO.Request.AuthRequest;
import com.example.SafeBank.DTO.Request.TransferRequest;
import com.example.SafeBank.DTO.Request.UserRequest;
import com.example.SafeBank.DTO.Response.AuthResponse;
import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import com.example.SafeBank.Repository.UserRepository;
import com.example.SafeBank.Service.AuthService;
import com.example.SafeBank.Service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class DemoBalanceFeatureTest {

    private static final String JOSEPH = "joseph@demo.com";
    private static final String MARY = "mary@demo.com";
    private static final String CHRIS = "chris@demo.com";

    @Autowired
    private AuthService authService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DemoBalanceProperties demoBalanceProperties;

    private DemoAccountSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new DemoAccountSeeder(authService, demoBalanceProperties);
    }

    @Test
    void registrationCreatesTheStartingBalanceExactlyOnce() {
        String email = "new-user-" + UUID.randomUUID() + "@safebank.test";

        AuthResponse response = authService.register(new UserRequest("New User", email, "password"));

        assertEquals(new BigDecimal("30000.00"), new BigDecimal(response.balance()));
        assertEquals(new BigDecimal("30000.00"), userRepository.findByEmail(email).orElseThrow().getBalance());
    }

    @Test
    void seederPersistsTheRequestedUsersWithHashedPasswordsAndDoesNotResetBalances() throws Exception {
        seeder.run(new DefaultApplicationArguments());

        var joseph = userRepository.findByEmail(JOSEPH).orElseThrow();
        var mary = userRepository.findByEmail(MARY).orElseThrow();
        var chris = userRepository.findByEmail(CHRIS).orElseThrow();
        assertEquals("Joseph Sanusi", joseph.getName());
        assertEquals("Mary", mary.getName());
        assertEquals("Chris", chris.getName());
        assertTrue(passwordEncoder.matches("Password123", joseph.getPassword()));
        assertFalse("Password123".equals(joseph.getPassword()));
        assertEquals(new BigDecimal("30000.00"), joseph.getBalance());
        assertEquals(new BigDecimal("30000.00"), mary.getBalance());
        assertEquals(new BigDecimal("30000.00"), chris.getBalance());

        joseph.setBalance(new BigDecimal("25000.00"));
        userRepository.saveAndFlush(joseph);
        seeder.run(new DefaultApplicationArguments());

        assertEquals(new BigDecimal("25000.00"), userRepository.findByEmail(JOSEPH).orElseThrow().getBalance());
        assertEquals(3, userRepository.findAll().stream()
                .filter(user -> user.getEmail().equals(JOSEPH) || user.getEmail().equals(MARY) || user.getEmail().equals(CHRIS))
                .count());
        assertEquals(JOSEPH, authService.login(new AuthRequest(JOSEPH, "Password123")).email());
    }

    @Test
    void demoAccountsUseTheNormalTransferService() throws Exception {
        seeder.run(new DefaultApplicationArguments());
        setBalance(JOSEPH, "30000.00");
        setBalance(MARY, "30000.00");
        String maryAccountNumber = userRepository.findByEmail(MARY).orElseThrow().getAccountNumber();

        transferService.performTransfer(JOSEPH,
                new TransferRequest(maryAccountNumber, new BigDecimal("5000.00"), "Demo transfer"));

        assertEquals(new BigDecimal("25000.00"), userRepository.findByEmail(JOSEPH).orElseThrow().getBalance());
        assertEquals(new BigDecimal("35000.00"), userRepository.findByEmail(MARY).orElseThrow().getBalance());
    }

    @Test
    void transferValidationKeepsBalancesUnchanged() throws Exception {
        seeder.run(new DefaultApplicationArguments());
        setBalance(JOSEPH, "30000.00");
        String josephAccountNumber = userRepository.findByEmail(JOSEPH).orElseThrow().getAccountNumber();
        String maryAccountNumber = userRepository.findByEmail(MARY).orElseThrow().getAccountNumber();

        assertThrows(CustomExceptions.InsufficientBalanceException.class, () -> transferService.performTransfer(
                JOSEPH, new TransferRequest(maryAccountNumber, new BigDecimal("30000.01"), "Too much")));
        assertThrows(CustomExceptions.InvalidTransferException.class, () -> transferService.performTransfer(
                JOSEPH, new TransferRequest(josephAccountNumber, new BigDecimal("1.00"), "Self transfer")));

        assertEquals(new BigDecimal("30000.00"), userRepository.findByEmail(JOSEPH).orElseThrow().getBalance());
    }

    private void setBalance(String email, String amount) {
        var user = userRepository.findByEmail(email).orElseThrow();
        user.setBalance(new BigDecimal(amount));
        userRepository.saveAndFlush(user);
    }
}

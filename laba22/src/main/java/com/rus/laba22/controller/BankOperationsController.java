package com.rus.laba22.controller;

import com.rus.laba22.model.Account;
import com.rus.laba22.model.Card;
import com.rus.laba22.model.Transaction;
import com.rus.laba22.service.BankService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/operations")
public class BankOperationsController {

    private final BankService bankService;

    public BankOperationsController(BankService bankService) {
        this.bankService = bankService;
    }

    public record OpenAccountRequest(Long customerId, String accountNumber, BigDecimal initialBalance) {}
    public record IssueCardRequest(Long accountId, String cardNumber) {}
    public record AmountRequest(BigDecimal amount) {}
    public record TransferRequest(Long fromAccountId, Long toAccountId, BigDecimal amount) {}

    @PostMapping("/open-account")
    public Account openAccount(@RequestBody OpenAccountRequest request) {
        return bankService.openAccountForCustomer(request.customerId(), request.accountNumber(), request.initialBalance());
    }

    @PostMapping("/issue-card")
    public Card issueCard(@RequestBody IssueCardRequest request) {
        return bankService.issueCardForAccount(request.accountId(), request.cardNumber());
    }

    @PostMapping("/deposit/{accountId}")
    public Transaction deposit(@PathVariable Long accountId, @RequestBody AmountRequest request) {
        return bankService.deposit(accountId, request.amount());
    }

    @PostMapping("/withdraw/{accountId}")
    public Transaction withdraw(@PathVariable Long accountId, @RequestBody AmountRequest request) {
        return bankService.withdraw(accountId, request.amount());
    }

    @PostMapping("/transfer")
    public Transaction transfer(@RequestBody TransferRequest request) {
        return bankService.transfer(request.fromAccountId(), request.toAccountId(), request.amount());
    }
}


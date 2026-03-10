package com.rus.laba22.service;

import com.rus.laba22.model.Account;
import com.rus.laba22.model.Card;
import com.rus.laba22.model.Customer;
import com.rus.laba22.model.Transaction;
import com.rus.laba22.repository.AccountRepository;
import com.rus.laba22.repository.CardRepository;
import com.rus.laba22.repository.CustomerRepository;
import com.rus.laba22.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BankService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;

    public BankService(CustomerRepository customerRepository,
                       AccountRepository accountRepository,
                       CardRepository cardRepository,
                       TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Account openAccountForCustomer(Long customerId, String accountNumber, BigDecimal initialBalance) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        Account account = new Account();
        account.setCustomer(customer);
        account.setNumber(accountNumber);
        account.setBalance(initialBalance != null ? initialBalance : BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    @Transactional
    public Card issueCardForAccount(Long accountId, String cardNumber) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        Card card = new Card();
        card.setAccount(account);
        card.setNumber(cardNumber);
        card.setBlocked(false);
        return cardRepository.save(card);
    }

    @Transactional
    public Transaction deposit(Long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setFromAccount(account);
        tx.setToAccount(account);
        tx.setAmount(amount);
        tx.setCreatedAt(LocalDateTime.now());
        return transactionRepository.save(tx);
    }

    @Transactional
    public Transaction withdraw(Long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setFromAccount(account);
        tx.setToAccount(account);
        tx.setAmount(amount.negate());
        tx.setCreatedAt(LocalDateTime.now());
        return transactionRepository.save(tx);
    }

    @Transactional
    public Transaction transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }
        if (fromAccountId.equals(toAccountId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Accounts must be different");
        }
        Account from = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found"));
        Account to = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target account not found"));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        accountRepository.save(from);
        accountRepository.save(to);

        Transaction tx = new Transaction();
        tx.setFromAccount(from);
        tx.setToAccount(to);
        tx.setAmount(amount);
        tx.setCreatedAt(LocalDateTime.now());
        return transactionRepository.save(tx);
    }

    @Transactional
    public Card blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        card.setBlocked(true);
        return cardRepository.save(card);
    }
}


package pardalis.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

@Entity(name = "Account")
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private BigInteger accountId;

    @Column(name = "balance")
    private BigDecimal balance;

    @Version
    @Column(name = "version")
    private Long version;

    public Account() {
        super();
    }

    public Account(BigDecimal balance) {
        super();
        this.balance = balance;
    }

    public Account(BigInteger accountId, BigDecimal balance) {
        super();
        this.accountId = accountId;
        this.balance = balance;
    }

    public Account(BigInteger accountId, BigDecimal balance, Long version) {
        this.accountId =  accountId;
        this.balance = balance;
        this.version = version;
    }

    public BigInteger getAccountId() {
        return accountId;
    }

    public void setAccountId(BigInteger accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Account account = (Account) o;

        return accountId.equals(account.accountId) &&
                balance.equals(account.balance) &&
                version.equals(account.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, balance, version);
    }
}
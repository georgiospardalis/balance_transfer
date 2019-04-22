package pardalis.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class TransferOrderDTO {
    private final BigInteger sourceAccount;
    private final BigInteger targetAccount;
    private final BigDecimal amount;

    @JsonCreator
    public TransferOrderDTO(
            @JsonProperty("source-account") BigInteger sourceAccount,
            @JsonProperty("target-account") BigInteger targetAccount,
            @JsonProperty("amount")BigDecimal amount) {
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
    }

    public BigInteger getSourceAccount() {
        return sourceAccount;
    }

    public BigInteger getTargetAccount() {
        return targetAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
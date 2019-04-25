package pardalis.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

@Entity(name = "TransferOrder")
@Table(name = "transfer_order")
public class TransferOrder {
    @Id
    @Column(name = "transfer_id")
    private String transferId;

    @Column(name = "ts")
    private Long timestamp;

    @Column(name = "source_account_id", updatable = false, insertable = true)
    private BigInteger sourceAccountId;

    @Column(name = "target_account_id", updatable = false, insertable = true)
    private BigInteger targetAccountId;

    @Column(name = "transfer_amount")
    private BigDecimal transferAmount;

    public TransferOrder() {
        super();
    }

    public TransferOrder(String transferId,
                         Long timestamp,
                         BigInteger sourceAccountId,
                         BigInteger targetAccountId,
                         BigDecimal transferAmount) {
        super();
        this.transferId = transferId;
        this.timestamp = timestamp;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.transferAmount = transferAmount;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String balanceTransferId) {
        this.transferId = balanceTransferId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public BigInteger getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(BigInteger sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public BigInteger getTargetAccountId() {
        return targetAccountId;
    }

    public void setTargetAccountId(BigInteger targetAccountId) {
        this.targetAccountId = targetAccountId;
    }

    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TransferOrder that = (TransferOrder) o;

        return transferId.equals(that.transferId) &&
                timestamp.equals(that.timestamp) &&
                sourceAccountId.equals(that.sourceAccountId) &&
                targetAccountId.equals(that.targetAccountId) &&
                transferAmount.equals(that.transferAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transferId, timestamp, sourceAccountId, targetAccountId, transferAmount);
    }
}
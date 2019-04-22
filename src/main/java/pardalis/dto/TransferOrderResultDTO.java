package pardalis.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public final class TransferOrderResultDTO {
    @JsonProperty("transfer-status")
    private final String transferStatus;

    @JsonProperty("timestamp")
    private final Long timestamp;

    @JsonProperty("transferId")
    private final String transferId;

    public TransferOrderResultDTO(String transferStatus) {
        this.transferStatus = transferStatus;
        timestamp = null;
        transferId = null;
    }

    public TransferOrderResultDTO(String transferStatus, Long timestamp, String transferId) {
        this.transferStatus = transferStatus;
        this.timestamp = timestamp;
        this.transferId = transferId;
    }

    public String getTransferStatus() {
        return transferStatus;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getTransferId() {
        return transferId;
    }
}
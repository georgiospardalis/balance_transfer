package pardalis.unit;

import org.junit.Before;
import org.junit.Test;
import pardalis.dto.TransferOrderDTO;
import pardalis.helper.enumeration.RequestValidationError;
import pardalis.helper.exception.RequestValidationException;
import pardalis.validator.RequestValidator;
import pardalis.validator.RequestValidatorImpl;

import java.math.BigDecimal;
import java.math.BigInteger;

public class RequestValidatorImplTest {
    private RequestValidator requestValidator;

    @Before
    public void init() {
        requestValidator = new RequestValidatorImpl();
    }

    @Test
    public void validDTOShouldNotThrowException() {
        TransferOrderDTO transferOrderDTO = new TransferOrderDTO(
                new BigInteger("1"),
                new BigInteger("2"),
                new BigDecimal("25.50"));

        this.requestValidator.validateTransferOrderDTO(transferOrderDTO);
    }

    @Test
    public void invalidDTOShouldThrowForNullSourceAccount() {
        TransferOrderDTO transferOrderDTO = new TransferOrderDTO(
                null,
                new BigInteger("2"),
                new BigDecimal("25.50"));

        try {
            this.requestValidator.validateTransferOrderDTO(transferOrderDTO);
        } catch (RequestValidationException e) {
            assert (e.getMessage().equals(RequestValidationError.TRANSFER_ACCOUNT_MISSING.toString()));
        }
    }

    @Test
    public void invalidDTOShouldThrowForNullTargetAccount() {
        TransferOrderDTO transferOrderDTO = new TransferOrderDTO(
                new BigInteger("1"),
                null,
                new BigDecimal("25.50"));

        try {
            this.requestValidator.validateTransferOrderDTO(transferOrderDTO);
        } catch (RequestValidationException e) {
            assert(e.getMessage().equals(RequestValidationError.TRANSFER_ACCOUNT_MISSING.toString()));
        }
    }

    @Test
    public void invalidDTOShouldThrowForNullBothAccounts() {
        TransferOrderDTO transferOrderDTO = new TransferOrderDTO(
                null,
                null,
                new BigDecimal("25.50"));

        try {
            this.requestValidator.validateTransferOrderDTO(transferOrderDTO);
        } catch (RequestValidationException e) {
            assert(e.getMessage().equals(RequestValidationError.TRANSFER_ACCOUNT_MISSING.toString()));
        }
    }

    @Test
    public void invalidDTOShouldThrowForInvalidSourceAccount() {
        TransferOrderDTO transferOrderDTO = new TransferOrderDTO(
                new BigInteger("-1"),
                new BigInteger("2"),
                new BigDecimal("25.50"));

        try {
            this.requestValidator.validateTransferOrderDTO(transferOrderDTO);
        } catch (RequestValidationException e) {
            assert(e.getMessage().equals(RequestValidationError.TRANSFER_ACCOUNT_INVALID.toString()));
        }
    }

    @Test
    public void invalidDTOShouldThrowForInvalidTargetAccount() {
        TransferOrderDTO transferOrderDTO = new TransferOrderDTO(
                new BigInteger("1"),
                new BigInteger("-2"),
                new BigDecimal("25.50"));

        try {
            this.requestValidator.validateTransferOrderDTO(transferOrderDTO);
        } catch (RequestValidationException e) {
            assert(e.getMessage().equals(RequestValidationError.TRANSFER_ACCOUNT_INVALID.toString()));
        }
    }

    @Test
    public void invalidDTOShouldThrowForInvalidBothAccounts() {
        TransferOrderDTO transferOrderDTO = new TransferOrderDTO(
                new BigInteger("-1"),
                new BigInteger("-2"),
                new BigDecimal("25.50"));

        try {
            this.requestValidator.validateTransferOrderDTO(transferOrderDTO);
        } catch (RequestValidationException e) {
            assert(e.getMessage().equals(RequestValidationError.TRANSFER_ACCOUNT_INVALID.toString()));
        }
    }

    @Test
    public void invalidDTOShouldThrowForSameSourceTargetAccount() {
        TransferOrderDTO transferOrderDTO = new TransferOrderDTO(
                new BigInteger("1"),
                new BigInteger("1"),
                new BigDecimal("25.50"));

        try {
            this.requestValidator.validateTransferOrderDTO(transferOrderDTO);
        } catch (RequestValidationException e) {
            assert(e.getMessage().equals(RequestValidationError.TRANSFER_SAME_ACCOUNT.toString()));
        }
    }

    @Test
    public void invalidDTOShouldThrowForTryingToTransferNegativeAmount() {
        TransferOrderDTO transferOrderDTO = new TransferOrderDTO(
                new BigInteger("1"),
                new BigInteger("5"),
                new BigDecimal("-25.50"));

        try {
            this.requestValidator.validateTransferOrderDTO(transferOrderDTO);
        } catch (RequestValidationException e) {
            assert(e.getMessage().equals(RequestValidationError.TRANSFER_LESS_THAN_OR_ZERO.toString()));
        }
    }

    @Test
    public void invalidDTOShouldThrowForTryingToTransferZeroAmount() {
        TransferOrderDTO transferOrderDTO = new TransferOrderDTO(
                new BigInteger("1"),
                new BigInteger("5"),
                new BigDecimal("0.0"));

        try {
            this.requestValidator.validateTransferOrderDTO(transferOrderDTO);
        } catch (RequestValidationException e) {
            assert(e.getMessage().equals(RequestValidationError.TRANSFER_LESS_THAN_OR_ZERO.toString()));
        }
    }
}

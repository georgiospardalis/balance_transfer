package pardalis.validator;

import pardalis.dto.TransferOrderDTO;
import pardalis.helper.enumeration.RequestValidationError;
import pardalis.helper.exception.RequestValidationException;

import java.math.BigDecimal;
import java.math.BigInteger;

public class RequestValidatorImpl implements RequestValidator {
    @Override
    public void validateTransferOrderDTO(TransferOrderDTO transferOrderDTO) throws RequestValidationException {
        if (transferOrderDTO.getSourceAccount() == null || transferOrderDTO.getTargetAccount() == null) {
            throw new RequestValidationException(RequestValidationError.TRANSFER_ACCOUNT_MISSING.toString());
        }

        if (transferOrderDTO.getSourceAccount().compareTo(BigInteger.ZERO) < 0 ||
                transferOrderDTO.getTargetAccount().compareTo(BigInteger.ZERO) < 0) {
            throw new RequestValidationException(RequestValidationError.TRANSFER_ACCOUNT_INVALID.toString());
        }

        if (transferOrderDTO.getSourceAccount().equals(transferOrderDTO.getTargetAccount())) {
            throw new RequestValidationException(RequestValidationError.TRANSFER_SAME_ACCOUNT.toString());
        }

        if (transferOrderDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RequestValidationException(RequestValidationError.TRANSFER_LESS_THAN_OR_ZERO.toString());
        }
    }
}
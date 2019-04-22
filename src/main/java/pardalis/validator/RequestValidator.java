package pardalis.validator;

import pardalis.dto.TransferOrderDTO;
import pardalis.helper.exception.RequestValidationException;

public interface RequestValidator {
    void validateTransferOrderDTO(TransferOrderDTO transferOrderDTO) throws RequestValidationException;
}
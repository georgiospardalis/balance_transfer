package pardalis.service;

import pardalis.dto.TransferOrderDTO;
import pardalis.dto.TransferOrderResultDTO;

public interface AccountService {
    TransferOrderResultDTO transferBalance(TransferOrderDTO transferOrderDTO);
}
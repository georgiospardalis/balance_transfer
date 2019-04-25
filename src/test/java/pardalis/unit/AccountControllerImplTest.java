package pardalis.unit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pardalis.controller.AccountControllerImpl;
import pardalis.dto.TransferOrderDTO;
import pardalis.dto.TransferOrderResultDTO;
import pardalis.helper.enumeration.RequestValidationError;
import pardalis.helper.exception.RequestValidationException;
import pardalis.service.AccountService;
import pardalis.validator.RequestValidatorImpl;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccountControllerImplTest {
    @Mock
    RequestValidatorImpl requestValidatorImpl;

    @Mock
    AccountService accountService;

    @InjectMocks
    AccountControllerImpl accountControllerImpl;

    @Test
    public void getDTOWithMsgForSuccess() {
        TransferOrderDTO validTransferOrderDTO = new TransferOrderDTO(
                new BigInteger("1"),
                new BigInteger("2"),
                new BigDecimal("50.0"));

        TransferOrderResultDTO expectedTransferOrderResultDTO = new TransferOrderResultDTO(
                "Successful Transaction",
                1234567890L,
                "n/a");

        doNothing().when(requestValidatorImpl).validateTransferOrderDTO(any());
        when(accountService.transferBalance(any())).thenReturn(expectedTransferOrderResultDTO);

        TransferOrderResultDTO returnedValue = accountControllerImpl.transferMoney(validTransferOrderDTO);

        Assert.assertEquals(returnedValue.getTimestamp(), new Long(1234567890L));
        assert(returnedValue.getTransferStatus().equals("Successful Transaction"));
    }

    @Test
    public void getDTOWithMsgForFailedTransfer() {
        TransferOrderDTO validTransferOrderDTO = new TransferOrderDTO(
                new BigInteger("1"),
                new BigInteger("2"),
                new BigDecimal("50.0"));

        TransferOrderResultDTO expectedTransferOrderResultDTO = new TransferOrderResultDTO(
                "Insufficient Balance");

        doNothing().when(requestValidatorImpl).validateTransferOrderDTO(any());
        when(accountService.transferBalance(any())).thenReturn(expectedTransferOrderResultDTO);

        TransferOrderResultDTO returnedValue = accountControllerImpl.transferMoney(validTransferOrderDTO);

        Assert.assertEquals(returnedValue.getTransferStatus(), "Insufficient Balance");
    }

    @Test
    public void getDTOWithMsgForFailedValidation() {
        TransferOrderDTO invalidTransferOrderDTO = new TransferOrderDTO(
                new BigInteger("1"),
                new BigInteger("1"),
                new BigDecimal("50.0"));

        doThrow(new RequestValidationException(RequestValidationError.TRANSFER_SAME_ACCOUNT.toString()))
                .when(requestValidatorImpl).validateTransferOrderDTO(any());

        TransferOrderResultDTO returnedValue = accountControllerImpl.transferMoney(invalidTransferOrderDTO);

        Assert.assertEquals(returnedValue.getTransferStatus(), "Sender and Recipient is the same Account");
    }
}

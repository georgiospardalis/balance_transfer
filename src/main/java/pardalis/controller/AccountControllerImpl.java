package pardalis.controller;

import com.google.inject.Inject;
import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.jboss.logging.Logger;
import pardalis.dto.TransferOrderDTO;
import pardalis.dto.TransferOrderResultDTO;
import pardalis.helper.exception.RequestValidationException;
import pardalis.service.AccountService;
import pardalis.validator.RequestValidator;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("transaction")
public class AccountControllerImpl implements AccountController {
    private static final Logger LOGGER = LoggerFactory.logger(AccountControllerImpl.class);

    @Inject
    RequestValidator requestValidator;

    @Inject
    AccountService accountService;

    @PUT
    @Path("/balance_transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TransferOrderResultDTO transferMoney(TransferOrderDTO transferOrderDTO) {
        try {
            requestValidator.validateTransferOrderDTO(transferOrderDTO);
        } catch (RequestValidationException e) {
            LOGGER.error(e.getMessage(), e);
            return new TransferOrderResultDTO(e.getMessage(), null, null);
        }

        return accountService.transferBalance(transferOrderDTO);
    }
}
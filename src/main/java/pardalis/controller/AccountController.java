package pardalis.controller;

import pardalis.dto.TransferOrderDTO;
import pardalis.dto.TransferOrderResultDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("transaction")
public interface AccountController {
    @PUT
    @Path("/balance_transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    TransferOrderResultDTO transferMoney(TransferOrderDTO transferOrderDTO);
}
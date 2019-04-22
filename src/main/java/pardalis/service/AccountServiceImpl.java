package pardalis.service;

import com.google.inject.Inject;
import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.jboss.logging.Logger;
import pardalis.dto.TransferOrderDTO;
import pardalis.dto.TransferOrderResultDTO;
import pardalis.entity.Account;
import pardalis.entity.TransferOrder;
import pardalis.helper.enumeration.AccountOperationStatus;
import pardalis.helper.exception.AccountManipulationException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

public class AccountServiceImpl implements AccountService {
    private static final Logger LOGGER = LoggerFactory.logger(AccountServiceImpl.class);

    private ThreadLocal<EntityManager> threadLocalEM;

    @Inject
    private EntityManagerFactory entityManagerFactory;

    @Override
    public TransferOrderResultDTO transferBalance(TransferOrderDTO transferOrderDTO) {
        threadLocalEM = new ThreadLocal<>();
        threadLocalEM.set(entityManagerFactory.createEntityManager());

        String status = AccountOperationStatus.SUCCESSFUL_TRANSACTION.toString();
        TransferOrder transferOrder = new TransferOrder();

        try {
            threadLocalEM.get().getTransaction().begin();

            updateAccounts(
                    transferOrderDTO.getSourceAccount(),
                    transferOrderDTO.getTargetAccount(),
                    transferOrderDTO.getAmount());

            transferOrder = logSuccessfulTransfer(
                    transferOrderDTO.getSourceAccount(),
                    transferOrderDTO.getTargetAccount(),
                    transferOrderDTO.getAmount());

            threadLocalEM.get().getTransaction().commit();
        } catch (Exception e) {
            threadLocalEM.get().getTransaction().rollback();
            LOGGER.error(e.getMessage(), e);

            if (e.getClass().equals(AccountManipulationException.class)) {
                status = e.getMessage();
            } else {
                status = AccountOperationStatus.INTERNAL_ERROR.toString();
            }
        } finally {
            threadLocalEM.get().close();
            threadLocalEM.remove();
        }

        return mapResultToDTO(status, transferOrder);
    }

    private void updateAccounts(BigInteger sourceAccountId, BigInteger targetAccountId, BigDecimal transferAount) {
        Account sourceAccount = threadLocalEM.get().find(
                Account.class,
                sourceAccountId,
                LockModeType.OPTIMISTIC);

        Account targetAccount = threadLocalEM.get().find(
                Account.class,
                targetAccountId,
                LockModeType.OPTIMISTIC);

        if (sourceAccount == null || targetAccount == null) {
            throw new AccountManipulationException(AccountOperationStatus.ACCOUNT_NOT_FOUND.toString());
        }

        if (sourceAccount.getBalance().subtract(transferAount).compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountManipulationException(AccountOperationStatus.INSUFFICIENT_BALANCE.toString());
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(transferAount));
        targetAccount.setBalance(targetAccount.getBalance().add(transferAount));

        threadLocalEM.get().merge(sourceAccount);
        threadLocalEM.get().merge(targetAccount);
    }

    private TransferOrder logSuccessfulTransfer(BigInteger sourceAccountId,
                                                BigInteger targetAccountId,
                                                BigDecimal transferAmount) {
        Long ts = System.currentTimeMillis();
        String transferId = UUID.randomUUID().toString();


        TransferOrder transferOrder = new TransferOrder(
                transferId,
                ts,
                sourceAccountId,
                targetAccountId,
                transferAmount);

        threadLocalEM.get().persist(transferOrder);

        return transferOrder;
    }

    private TransferOrderResultDTO mapResultToDTO(String status, TransferOrder transferOrder) {
        TransferOrderResultDTO transferOrderResultDTO;

        if (status.equals(AccountOperationStatus.SUCCESSFUL_TRANSACTION.toString())) {
            transferOrderResultDTO = new TransferOrderResultDTO(
                    status,
                    transferOrder.getTimestamp(),
                    transferOrder.getTransferId());
        } else {
            transferOrderResultDTO = new TransferOrderResultDTO(status);
        }

        return transferOrderResultDTO;
    }
}
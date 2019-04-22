package pardalis.helper.enumeration;

public enum AccountOperationStatus {
    SUCCESSFUL_TRANSACTION("Successful Transaction"),
    INSUFFICIENT_BALANCE("Insufficient Balance"),
    ACCOUNT_NOT_FOUND("Could not find Account(s)"),
    INTERNAL_ERROR("Internal Error");

    private final String value;

    AccountOperationStatus(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
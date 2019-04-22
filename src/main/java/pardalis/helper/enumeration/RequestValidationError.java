package pardalis.helper.enumeration;

public enum RequestValidationError {
    TRANSFER_LESS_THAN_OR_ZERO("Transfer Amount cannot be negative"),
    TRANSFER_ACCOUNT_MISSING("Must provide both Accounts"),
    TRANSFER_ACCOUNT_INVALID("Invalid source and/or target accounnt"),
    TRANSFER_SAME_ACCOUNT("Sender and Recipient is the same Account");

    private final String value;

    RequestValidationError(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
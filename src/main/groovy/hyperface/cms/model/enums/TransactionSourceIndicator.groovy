package hyperface.cms.model.enums

enum TransactionSourceIndicator {
    C("CUSTOMER_INITIATED"),
    S("SYSTEM_GENERATED");

    private String name;

    TransactionSourceIndicator(String name) {
        this.name = name;
    }

    String getName() {
        return this.name;
    }
}
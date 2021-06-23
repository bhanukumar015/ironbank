package hyperface.cms.model

class Amount {
    int coefficient;
    int exponent;
    String currency;

    Double getAmountInDecimal() {
        return new Double(String.format("%.2f", coefficient * Math.pow(10, exponent)));
    }
}

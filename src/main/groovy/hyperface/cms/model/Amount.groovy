package hyperface.cms.model;

import lombok.Data;

@Data
class Amount {
    private int coefficient;
    private int exponent;
    private String currency;

    Double getAmountInDecimal() {
        return new Double(String.format("%.2f" ,coefficient * Math.pow(10, exponent)));
    }
}

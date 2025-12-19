package com.magiclook.dto;

import java.math.BigDecimal;

public class RefundInfoDTO {
    private int percent;
    private BigDecimal amount;

    public RefundInfoDTO() {}

    public RefundInfoDTO(int percent, BigDecimal amount) {
        this.percent = percent;
        this.amount = amount;
    }

    public int getPercent() { return percent; }
    public void setPercent(int percent) { this.percent = percent; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

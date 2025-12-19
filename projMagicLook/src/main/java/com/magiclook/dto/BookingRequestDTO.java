package com.magiclook.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

public class BookingRequestDTO {
    
    @NotNull(message = "Item ID é obrigatório")
    private Integer itemId;
    
    @NotNull(message = "Data de início de uso é obrigatória")
    @Future(message = "A data deve ser futura")
    private Date startUseDate;
    
    @NotNull(message = "Data de fim de uso é obrigatória")
    @Future(message = "A data deve ser futura")
    private Date endUseDate;

    private String size;
    
    public BookingRequestDTO() {}
    
    public BookingRequestDTO(Integer itemId, Date startUseDate, Date endUseDate) {
        this.itemId = itemId;
        this.startUseDate = startUseDate;
        this.endUseDate = endUseDate;
    }
    
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    
    public Date getStartUseDate() { return startUseDate; }
    public void setStartUseDate(Date startUseDate) { this.startUseDate = startUseDate; }
    
    public Date getEndUseDate() { return endUseDate; }
    public void setEndUseDate(Date endUseDate) { this.endUseDate = endUseDate; }
    
    public boolean isValidDates() {
        if (startUseDate == null || endUseDate == null) return false;
        return !endUseDate.before(startUseDate);
    }
    
    public long getUseDays() {
        if (!isValidDates()) return 0;
        long diff = endUseDate.getTime() - startUseDate.getTime();
        return (diff / (1000 * 60 * 60 * 24)) + 1;
    }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
}
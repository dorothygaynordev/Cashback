package com.cashback.models.loyalty.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para información de los atendientes
 */
public class Attendants {
    
    @JsonProperty("cashier")
    private Cashier cashier;
    
    @JsonProperty("salesperson")
    private Salesperson salesperson;

    // Constructores
    public Attendants() {
    }
    
    public Attendants(String cashierId, String salespersonId) {
        this.cashier = new Cashier(cashierId);
        this.salesperson = new Salesperson(salespersonId);
    }

    // Getters y Setters
    public Cashier getCashier() {
        return cashier;
    }

    public void setCashier(Cashier cashier) {
        this.cashier = cashier;
    }

    public Salesperson getSalesperson() {
        return salesperson;
    }

    public void setSalesperson(Salesperson salesperson) {
        this.salesperson = salesperson;
    }

    // Clases internas
    public static class Cashier {
        @JsonProperty("id")
        private String id;
        
        public Cashier() {
        }
        
        public Cashier(String id) {
            this.id = id;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
    }
    
    public static class Salesperson {
        @JsonProperty("id")
        private String id;
        
        public Salesperson() {
        }
        
        public Salesperson(String id) {
            this.id = id;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Attendants{cashier=%s, salesperson=%s}",
                cashier != null ? cashier.getId() : "null",
                salesperson != null ? salesperson.getId() : "null"
        );
    }
}
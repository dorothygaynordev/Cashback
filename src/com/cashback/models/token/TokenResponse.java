package com.cashback.models.token;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DTO para la respuesta del token de autenticación
 */
public class TokenResponse {
    
    @JsonProperty("accessToken")
    private String accessToken;
    
    @JsonProperty("expiresIn")
    private String expiresIn;
    
    @JsonProperty("dateTime")
    private String dateTime;
    
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("access_token")
    private String access_token;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("expires_in")
    private Integer expiresInSeconds;

    // Constructor sin argumentos (requerido por Jackson)
    public TokenResponse() {
    }

    // Getters y Setters
    public String getAccessToken() {
        // Intentar diferentes nombres de propiedad
        if (accessToken != null) return accessToken;
        if (token != null) return token;
        if (access_token != null) return access_token;
        return null;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Integer getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(Integer expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    /**
     * Convierte la fecha de expiración a objeto Date
     */
    public Date getExpirationDate() throws ParseException {
        if (expiresIn == null) {
            // Si no hay fecha específica, calcular basado en expires_in
            if (expiresInSeconds != null) {
                Date ahora = new Date();
                return new Date(ahora.getTime() + (expiresInSeconds * 1000L));
            }
            throw new ParseException("La fecha de expiración es nula", 0);
        }
        
        String fechaSinZona = expiresIn.substring(0, Math.min(expiresIn.length(), 19));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return sdf.parse(fechaSinZona);
    }

    /**
     * Convierte la fecha del servidor a objeto Date
     */
    public Date getServerDate() throws ParseException {
        if (dateTime == null) {
            return new Date(); // Fallback a fecha actual
        }
        
        String fechaSinZona = dateTime.substring(0, Math.min(dateTime.length(), 19));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return sdf.parse(fechaSinZona);
    }

    /**
     * Calcula cuánto tiempo falta para que expire (en minutos)
     */
    public int getMinutosRestantes() {
        try {
            Date expiracion = getExpirationDate();
            Date ahora = new Date();
            long diferencia = expiracion.getTime() - ahora.getTime();
            return (int) (diferencia / (60 * 1000));
        } catch (Exception e) {
            System.err.println("Error calculando minutos restantes: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Verifica si el token es válido (falta más de 5 minutos)
     */
    public boolean isValid() {
        return getMinutosRestantes() > 5;
    }

    @Override
    public String toString() {
        String tokenPreview = getAccessToken() != null ? 
            getAccessToken().substring(0, Math.min(20, getAccessToken().length())) + "..." : "null";
            
        return String.format(
                "TokenResponse{token='%s', expira='%s', servidor='%s', minutosRestantes=%d, valido=%s}",
                tokenPreview,
                expiresIn,
                dateTime,
                getMinutosRestantes(),
                isValid()
        );
    }
}
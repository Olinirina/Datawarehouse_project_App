package com.Laborex.Application.Service.ALERTE.Configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.alertes")
public class AlerteConfiguration {
    
    private int joursSeuilCommandeBloquee = 7;
    private int moisSeuilClientInactif = 3;
    private double seuilAnomalieVentePourcent = 50.0; // 50% d'écart
    private boolean activerNotifications = true;
    
    // Getters et Setters
    public int getJoursSeuilCommandeBloquee() { return joursSeuilCommandeBloquee; }
    public void setJoursSeuilCommandeBloquee(int joursSeuilCommandeBloquee) { 
        this.joursSeuilCommandeBloquee = joursSeuilCommandeBloquee; 
    }
    
    public int getMoisSeuilClientInactif() { return moisSeuilClientInactif; }
    public void setMoisSeuilClientInactif(int moisSeuilClientInactif) { 
        this.moisSeuilClientInactif = moisSeuilClientInactif; 
    }
    
    public double getSeuilAnomalieVentePourcent() { return seuilAnomalieVentePourcent; }
    public void setSeuilAnomalieVentePourcent(double seuilAnomalieVentePourcent) { 
        this.seuilAnomalieVentePourcent = seuilAnomalieVentePourcent; 
    }
    
    public boolean isActiverNotifications() { return activerNotifications; }
    public void setActiverNotifications(boolean activerNotifications) { 
        this.activerNotifications = activerNotifications; 
    }
}

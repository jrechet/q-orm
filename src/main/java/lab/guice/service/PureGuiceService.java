package lab.guice.service;

import org.jboss.logging.Logger;

/**
 * Service purement Guice (sans aucune dépendance Quarkus).
 * Démontre un composant Guice natif qui peut être injecté dans Quarkus CDI.
 */
public class PureGuiceService {
    
    private static final Logger LOG = Logger.getLogger(PureGuiceService.class);
    
    private final String serviceId;
    
    public PureGuiceService() {
        this.serviceId = "GUICE-" + System.currentTimeMillis();
        LOG.info("PureGuiceService created with ID: " + serviceId);
    }
    
    /**
     * Service de calcul simple
     */
    public double calculate(double a, double b, String operation) {
        LOG.info("Calculating: " + a + " " + operation + " " + b);
        
        switch (operation.toLowerCase()) {
            case "add":
            case "+":
                return a + b;
            case "subtract":
            case "-":
                return a - b;
            case "multiply":
            case "*":
                return a * b;
            case "divide":
            case "/":
                if (b == 0) {
                    throw new IllegalArgumentException("Division by zero");
                }
                return a / b;
            case "power":
            case "^":
                return Math.pow(a, b);
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }
    
    /**
     * Service de formatage
     */
    public String formatResult(double result) {
        String formatted = String.format("%.2f", result);
        LOG.info("Formatted result: " + formatted);
        return formatted;
    }
    
    /**
     * Service de validation
     */
    public boolean isValidNumber(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            LOG.warn("Invalid number: " + input);
            return false;
        }
    }
    
    /**
     * Service métier complexe
     */
    public String processBusinessLogic(String input, double multiplier) {
        LOG.info("Processing business logic for: " + input + " with multiplier: " + multiplier);
        
        if (!isValidNumber(input)) {
            return "ERROR: Invalid input";
        }
        
        double value = Double.parseDouble(input);
        double result = calculate(value, multiplier, "multiply");
        String formatted = formatResult(result);
        
        return "Processed[" + serviceId + "]: " + input + " * " + multiplier + " = " + formatted;
    }
    
    /**
     * Retourne l'ID unique du service
     */
    public String getServiceId() {
        return serviceId;
    }
    
    /**
     * Service de statistiques simple
     */
    public String getStats(double[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return "No data";
        }
        
        double sum = 0;
        double min = numbers[0];
        double max = numbers[0];
        
        for (double num : numbers) {
            sum += num;
            if (num < min) min = num;
            if (num > max) max = num;
        }
        
        double average = sum / numbers.length;
        
        return String.format("Stats[%s]: Count=%d, Sum=%.2f, Avg=%.2f, Min=%.2f, Max=%.2f", 
                           serviceId, numbers.length, sum, average, min, max);
    }
}
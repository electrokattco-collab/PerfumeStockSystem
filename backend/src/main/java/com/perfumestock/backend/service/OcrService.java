package com.perfumestock.backend.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OcrService {
    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    /**
     * Process a receipt image and extract line items using OCR + heuristic parsing.
     * Returns a map with extracted data: supplier, items, total, etc.
     */
    public Map<String, Object> processReceipt(MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        // Save temp file for Tesseract
        Path tempFile = Files.createTempFile("receipt_", ".tmp");
        Files.write(tempFile, file.getBytes());
        
        try {
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
            tesseract.setLanguage("eng");
            tesseract.setPageSegMode(6); // Assume uniform block of text
            tesseract.setTessVariable("preserve_interword_spaces", "1");
            
            // Enhance image before OCR
            BufferedImage original = ImageIO.read(tempFile.toFile());
            BufferedImage enhanced = enhanceImage(original);
            
            String rawText = tesseract.doOCR(enhanced);
            result.put("rawText", rawText);
            
            // Parse the OCR text
            result.putAll(parseReceiptText(rawText));
            
        } catch (TesseractException e) {
            log.error("OCR processing failed", e);
            result.put("error", "OCR processing failed: " + e.getMessage());
            result.put("rawText", "");
        } finally {
            Files.deleteIfExists(tempFile);
        }
        
        return result;
    }

    private BufferedImage enhanceImage(BufferedImage image) {
        // Convert to grayscale and increase contrast
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage enhanced = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                // Apply simple threshold for better OCR
                int value = gray < 140 ? 0 : 255;
                enhanced.setRGB(x, y, (value << 16) | (value << 8) | value);
            }
        }
        return enhanced;
    }

    private Map<String, Object> parseReceiptText(String text) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        
        String[] lines = text.split("\n");
        String supplier = null;
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        String date = null;
        
        Pattern pricePattern = Pattern.compile("R?\\s*([\\d,]+\\.\\d{2})");
        Pattern qtyPattern = Pattern.compile("(\\d+)\\s*[xX@*]\\s*");
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            
            // Try to detect supplier name (usually first non-empty line)
            if (supplier == null && !trimmed.matches(".*\\d{2}[/-]\\d{2}[/-]\\d{2,4}.*") && trimmed.length() > 3) {
                supplier = trimmed;
            }
            
            // Detect date
            Pattern datePattern = Pattern.compile("(\\d{2}[/-]\\d{2}[/-]\\d{2,4})");
            Matcher dateMatcher = datePattern.matcher(trimmed);
            if (dateMatcher.find() && date == null) {
                date = dateMatcher.group(1);
            }
            
            // Detect total/tax/subtotal
            String lower = trimmed.toLowerCase();
            if (lower.contains("total") && !lower.contains("subtotal")) {
                Matcher m = pricePattern.matcher(trimmed);
                if (m.find()) total = new BigDecimal(m.group(1).replace(",", ""));
            } else if (lower.contains("subtotal") || lower.contains("sub total")) {
                Matcher m = pricePattern.matcher(trimmed);
                if (m.find()) subtotal = new BigDecimal(m.group(1).replace(",", ""));
            } else if (lower.contains("vat") || lower.contains("tax")) {
                Matcher m = pricePattern.matcher(trimmed);
                if (m.find()) tax = new BigDecimal(m.group(1).replace(",", ""));
            }
            
            // Try to extract line items (product + quantity + price)
            Matcher priceMatcher = pricePattern.matcher(trimmed);
            Matcher qtyMatcher = qtyPattern.matcher(trimmed);
            
            if (priceMatcher.find()) {
                String priceStr = priceMatcher.group(1).replace(",", "");
                BigDecimal linePrice = new BigDecimal(priceStr);
                
                // Extract quantity if present
                int qty = 1;
                if (qtyMatcher.find()) {
                    qty = Integer.parseInt(qtyMatcher.group(1));
                }
                
                // Extract product name (everything before the price)
                String productName = trimmed.substring(0, priceMatcher.start()).trim();
                productName = productName.replaceAll("\\d+\\s*[xX@*]\\s*", "").trim();
                
                if (!productName.isEmpty() && productName.length() > 1) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("productName", productName);
                    item.put("quantity", qty);
                    item.put("unitCost", qty > 0 ? linePrice.divide(BigDecimal.valueOf(qty), 2, java.math.RoundingMode.HALF_UP) : linePrice);
                    item.put("totalCost", linePrice);
                    items.add(item);
                }
            }
        }
        
        if (subtotal.compareTo(BigDecimal.ZERO) == 0) subtotal = total.subtract(tax);
        
        result.put("supplier", supplier);
        result.put("date", date);
        result.put("items", items);
        result.put("totalAmount", total);
        result.put("subtotal", subtotal);
        result.put("taxAmount", tax);
        result.put("itemCount", items.size());
        
        return result;
    }
}

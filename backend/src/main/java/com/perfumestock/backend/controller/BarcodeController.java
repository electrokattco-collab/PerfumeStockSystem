package com.perfumestock.backend.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/barcodes")
@Tag(name = "Barcodes", description = "QR code and barcode generation for products")
public class BarcodeController {

    @GetMapping(value = "/qr/{productId}", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Generate QR code", description = "Generates a PNG QR code image for a product ID.")
    public ResponseEntity<byte[]> generateQRCode(
            @PathVariable String productId,
            @RequestParam(defaultValue = "250") int size) {

        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.MARGIN, 1,
                    EncodeHintType.CHARACTER_SET, "UTF-8"
            );

            BitMatrix matrix = writer.encode(productId, BarcodeFormat.QR_CODE, size, size, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            // Add product ID text below the QR code
            BufferedImage finalImage = new BufferedImage(size, size + 30, BufferedImage.TYPE_INT_RGB);
            var g = finalImage.createGraphics();
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, size, size + 30);
            g.drawImage(image, 0, 0, null);
            g.setColor(java.awt.Color.BLACK);
            g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
            var fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(productId);
            g.drawString(productId, (size - textWidth) / 2, size + 20);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(finalImage, "PNG", baos);
            byte[] qrBytes = baos.toByteArray();

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                    .body(qrBytes);

        } catch (WriterException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/code128/{productId}", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Generate Code128 barcode", description = "Generates a Code128 barcode image for a product ID.")
    public ResponseEntity<byte[]> generateCode128(
            @PathVariable String productId,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "80") int height) {

        try {
            com.google.zxing.oned.Code128Writer barcodeWriter =
                    new com.google.zxing.oned.Code128Writer();

            BitMatrix matrix = barcodeWriter.encode(
                    productId, BarcodeFormat.CODE_128, width, height);

            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            // Add product ID text below the barcode
            BufferedImage finalImage = new BufferedImage(width, height + 25, BufferedImage.TYPE_INT_RGB);
            var g = finalImage.createGraphics();
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, width, height + 25);
            g.drawImage(image, 0, 0, null);
            g.setColor(java.awt.Color.BLACK);
            g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
            var fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(productId);
            g.drawString(productId, (width - textWidth) / 2, height + 15);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(finalImage, "PNG", baos);
            byte[] barcodeBytes = baos.toByteArray();

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                    .body(barcodeBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

package com.bookrecommend.book_recommend_be.service.file;

import lombok.extern.slf4j.Slf4j;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

@Component
@Slf4j
public class PdfToEpubConverter {

    public ConversionResult convert(byte[] pdfBytes, String bookTitle) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper textStripper = new PDFTextStripper();
            String extractedText = textStripper.getText(document);

            Book epubBook = new Book();
            Metadata metadata = epubBook.getMetadata();
            metadata.addTitle(bookTitle);

            String safeTitle = sanitizeTitle(bookTitle);
            String chapterFileName = safeTitle + "-chapter-1.xhtml";
            Resource resource = new Resource(extractedText.getBytes(StandardCharsets.UTF_8), chapterFileName);
            epubBook.addSection("Chapter 1", resource);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                new EpubWriter().write(epubBook, outputStream);
                byte[] epubBytes = outputStream.toByteArray();
                return new ConversionResult(epubBytes, safeTitle + ".epub", document.getNumberOfPages());
            }
        } catch (IOException e) {
            log.error("Failed to convert PDF to EPUB", e);
            throw new RuntimeException("Failed to convert PDF to EPUB", e);
        }
    }

    private String sanitizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return "book";
        }
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized
                .replaceAll("[^a-zA-Z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .toLowerCase();
    }

    public record ConversionResult(byte[] epubBytes, String fileName, int totalPages) {
        public ByteArrayInputStream toInputStream() {
            return new ByteArrayInputStream(epubBytes);
        }

        public long size() {
            return epubBytes.length;
        }
    }
}

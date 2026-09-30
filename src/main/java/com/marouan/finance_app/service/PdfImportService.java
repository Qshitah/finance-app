package com.marouan.finance_app.service;

import com.marouan.finance_app.service.dto.ProposedEvent;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfImportService {

    // dd/mm/yyyy or dd-mm-yyyy, most bank statements use this
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{2})[/-](\\d{2})[/-](\\d{4})");
    // an amount like 1,234.56 or -45.00, comma as thousands separator, dot as decimal
    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("(?<!\\d)(-?\\d+(?:,\\d{3})*\\.\\d{2})(?!\\d)");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public List<ProposedEvent> extract(MultipartFile file) {
        String text;
        try {
            var doc = Loader.loadPDF(file.getBytes());
            text = new PDFTextStripper().getText(doc);
            doc.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't read this PDF: " + e.getMessage());
        }

        List<ProposedEvent> proposals = new ArrayList<>();

        for (String line : text.split("\\r?\\n")) {
            if (line.isBlank()) continue;

            var dateMatcher = DATE_PATTERN.matcher(line);
            var amountMatcher = AMOUNT_PATTERN.matcher(line);

            // a real transaction line needs both a date and an amount, skip anything else
            // (headers, page numbers, "Statement for account...", etc)
            if (!dateMatcher.find() || !amountMatcher.find()) continue;

            LocalDate date;
            try {
                date = LocalDate.parse(dateMatcher.group().replace("-", "/"), DATE_FORMAT);
            } catch (DateTimeParseException e) {
                continue; // looked like a date but didn't actually parse, skip rather than guess
            }

            BigDecimal amount = new BigDecimal(amountMatcher.group().replace(",", ""));

            // whatever's left on the line after pulling out the date and amount is the description
            String description = line
                    .replace(dateMatcher.group(), "")
                    .replace(amountMatcher.group(), "")
                    .trim()
                    .replaceAll("\\s{2,}", " ");

            proposals.add(new ProposedEvent(amount, date, description, line.trim()));
        }

        return proposals;
    }
}
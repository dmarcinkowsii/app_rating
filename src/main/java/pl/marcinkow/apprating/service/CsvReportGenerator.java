package pl.marcinkow.apprating.service;

import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvReportGenerator {

    private final Logger LOG = LoggerFactory.getLogger(CsvReportGenerator.class);

    public void generateReport(final String fileName, String[] headers, String reportsPath, final List<String[]> reportRows) {
        LOG.info("Creating report " + fileName + ".");
        createReportDirectory(reportsPath);
        File file = new File(reportsPath + fileName);

        try (CSVWriter writer =
                 new CSVWriter(new FileWriter(file), ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

            List<String[]> csvRows = new ArrayList<>();
            csvRows.add(headers);
            csvRows.addAll(reportRows);
            writer.writeAll(csvRows);

        } catch (IOException ioe) {
            LOG.error("Couldn't generate report." + ioe);
        }
        LOG.info("Report " + fileName + " has been created.");
    }

    private void createReportDirectory(String reportsPath) {
        try {
            Files.createDirectories(Paths.get(reportsPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

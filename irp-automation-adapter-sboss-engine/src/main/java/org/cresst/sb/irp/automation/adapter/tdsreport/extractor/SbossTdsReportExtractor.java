package org.cresst.sb.irp.automation.adapter.tdsreport.extractor;

import org.cresst.sb.irp.automation.adapter.tdsreport.dao.DocumentXmlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryOperations;

import java.util.*;

/**
 * Extracts TDS Reports using the DocumentXmlRepository DAO
 */
public class SbossTdsReportExtractor implements TdsReportExtractor {
    private final static Logger logger = LoggerFactory.getLogger(SbossTdsReportExtractor.class);

    private final DocumentXmlRepository documentXmlRepository;
    private final RetryOperations retryTemplate;

    public SbossTdsReportExtractor(DocumentXmlRepository documentXmlRepository,
                                   RetryOperations retryTemplate) {
        this.documentXmlRepository = documentXmlRepository;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public String getTdsReport(int tdsReportId) {
        return documentXmlRepository.getTdsReport(tdsReportId);
    }

    @Override
    public Collection<Integer> getTdsReports(final Date startTimeOfSimulation, Map<Integer, Integer> studentTestCount) {

        int expectedTdsReportCount = 0;
        for (Map.Entry<Integer, Integer> entry : studentTestCount.entrySet()) {
            if (entry.getValue() > 1) {
                // When there is more than one record per student then expect a possible COMBO test to be generated
                // The plus one represents the COMBO test
                expectedTdsReportCount += entry.getValue() + 1;
            } else {
                expectedTdsReportCount++;
            }
        }

        final int expectedCount = expectedTdsReportCount;

        try {
            List<Integer> reports = retryTemplate.execute(new RetryCallback<List<Integer>, Exception>() {
                @Override
                public List<Integer> doWithRetry(RetryContext retryContext) throws Exception {
                    List<Integer> reports = documentXmlRepository.getXmlRepositoryDataIdentifiers(startTimeOfSimulation);
                    logger.info("Found {} records in TIS DB. Expecting {} records.", reports != null ? reports.size() : 0, expectedCount);

                    if (reports.size() < expectedCount) {
                        throw new RepositoryException(reports, "XML Repository doesn't contain enough records");
                    }

                    return reports;
                }
            });

            return reports;
        } catch (RepositoryException e) {
            return e.getRecords() != null ? e.getRecords() : new ArrayList<Integer>();
        } catch (Exception e) {
            logger.error("Unable to get TDS Reports", e);
            return new ArrayList<>();
        }
    }

    class RepositoryException extends Exception {

        List<Integer> records;

        public RepositoryException(List<Integer> records, String message) {
            super(message);
            this.records = records;
        }

        public List<Integer> getRecords() {
            return records;
        }
    }
}

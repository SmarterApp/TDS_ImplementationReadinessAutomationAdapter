package org.cresst.sb.irp.automation.adapter.student;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.retry.RetryOperations;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public abstract class SbossStudentFactory implements StudentFactory {
    private final static Logger logger = LoggerFactory.getLogger(SbossStudentFactory.class);

    private final URL studentUrl;
    private final StudentResponseGenerator studentResponseGenerator;
    private final RetryOperations retryTemplate;
    private final Resource testStudentMappingsFile;

    private List<AutomationStudent> automationStudents;

    public SbossStudentFactory(URL studentUrl,
                               StudentResponseGenerator studentResponseGenerator,
                               RetryOperations retryTemplate,
                               Resource testStudentMappingsFile) {

        this.studentUrl = studentUrl;
        this.studentResponseGenerator = studentResponseGenerator;
        this.retryTemplate = retryTemplate;
        this.testStudentMappingsFile = testStudentMappingsFile;
    }

    public void postContruct() throws IOException {
        this.automationStudents = loadStudentsFromMapping(testStudentMappingsFile.getInputStream());
    }

    private List<TestStudentMapping> parseTestStudentMapping(InputStream inputStream) {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(TestStudentMapping.class).withHeader();

        try {
            MappingIterator<TestStudentMapping> it = mapper.readerFor(TestStudentMapping.class)
                    .with(schema)
                    .readValues(inputStream);
            return it.readAll();
        } catch (IOException e) {
            logger.error("Error parsing csv for TestStudentMapping, {}", e.getMessage());
        }
        return null;
    }

    private List<AutomationStudent> loadStudentsFromMapping(InputStream csvInputStream) {
        List<AutomationStudent> students = new ArrayList<>();

        List<TestStudentMapping> testStudentMappings = parseTestStudentMapping(csvInputStream);
        Map<String, List<TestStudentMapping>> groupedStudents = new TreeMap<>();
        for (TestStudentMapping t : testStudentMappings) {
            String studentName = t.getStudentName();
            if (groupedStudents.containsKey(studentName)) {
                groupedStudents.get(studentName).add(t);
            } else {
                List<TestStudentMapping> newGroup = new ArrayList<>();
                newGroup.add(t);
                groupedStudents.put(studentName, newGroup);
            }
        }

        // Create SbossStudents for each of the tests a student needs to take
        for (String studentName : groupedStudents.keySet()) {
            List<TestStudentMapping> studentTests = groupedStudents.get(studentName);
            Map<StudentTestTypeEnum, String> testNames = new HashMap<>();
            String altId = null;
            for (TestStudentMapping m : studentTests) {
                if (m.getTestType().toLowerCase().equals("combined")) break;

                altId = m.getAlternateSSID();
                if (m.isCat()) {
                    testNames.put(StudentTestTypeEnum.CAT, m.getTest());
                } else {
                    testNames.put(StudentTestTypeEnum.FIXED, m.getTest());
                }
            }

            SbossStudent student = new SbossStudent(
                    createStudentRestTemplate(),
                    retryTemplate,
                    studentUrl,
                    testNames,
                    studentResponseGenerator,
                    altId,
                    studentName);

            students.add(student);
        }

        return students;
    }

    @Override
    public List<AutomationStudent> createStudents() {
        return automationStudents;
    }

    /**
     * Created using Spring's "lookup method injection"
     * @return An AutomationRestTemplate to be used by the Student to communicate with Student application
     */
    protected abstract AutomationRestTemplate createStudentRestTemplate();
}

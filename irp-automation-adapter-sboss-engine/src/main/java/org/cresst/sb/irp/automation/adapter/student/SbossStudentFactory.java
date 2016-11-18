package org.cresst.sb.irp.automation.adapter.student;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.Lists;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.retry.RetryOperations;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SbossStudentFactory implements StudentFactory {
    private final static Logger logger = LoggerFactory.getLogger(SbossStudentFactory.class);
    
    private final AutomationRestTemplate studentRestTemplate;
    private final URL studentUrl;
    private final StudentResponseGenerator studentResponseGenerator;
    private final RetryOperations retryTemplate;

    public SbossStudentFactory(AutomationRestTemplate studentRestTemplate,
                               URL studentUrl,
                               StudentResponseGenerator studentResponseGenerator,
                               RetryOperations retryTemplate) {

        this.studentRestTemplate = studentRestTemplate;
        this.studentUrl = studentUrl;
        this.studentResponseGenerator = studentResponseGenerator;
        this.retryTemplate = retryTemplate;
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
		Map<String, List<TestStudentMapping>> groupedStudents = new HashMap<>();
		for(TestStudentMapping t : testStudentMappings) {
			String studentName = t.getStudentName();
			if(groupedStudents.containsKey(studentName)) {
				groupedStudents.get(studentName).add(t);
			} else {
				List<TestStudentMapping> newGroup = new ArrayList<>();
				newGroup.add(t);
				groupedStudents.put(studentName, newGroup);
			}
		}
		
		// Create SbossStudents for each of the tests a student needs to take  
		for(String studentName : groupedStudents.keySet()) {
			List<TestStudentMapping> studentTests = groupedStudents.get(studentName);
			Map<StudentTestTypeEnum, String> testNames = new HashMap<>();
			 String altId = null;
			 for(TestStudentMapping m : studentTests) {
				 if(m.getTestType().toLowerCase().equals("combined")) break;
				 
				 altId = m.getAlternateSSID();
				 if(m.isCat()) {
					 testNames.put(StudentTestTypeEnum.CAT, m.getTest());
				 } else {
					 testNames.put(StudentTestTypeEnum.FIXED, m.getTest());
				 }
			 }
			 
			 SbossStudent student =  new SbossStudent(
		                studentRestTemplate,
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
    	List<AutomationStudent> students = new ArrayList<>();
    	
        Map<StudentTestTypeEnum, String> testNames = new HashMap<>();
        testNames.put(StudentTestTypeEnum.FIXED, "(SBAC_PT)IRP-Perf-ELA-3-Summer-2015-2016");
        testNames.put(StudentTestTypeEnum.CAT, "(SBAC_PT)SBAC-IRP-CAT-ELA-3-Summer-2015-2016");

        SbossStudent student = new SbossStudent(
                studentRestTemplate,
                retryTemplate,
                studentUrl,
                testNames,
                studentResponseGenerator,
                "AIRP39990001",
                "IRPStudentA");
        
        students.add(student);
				
		return students;
    }
}

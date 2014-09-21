package edu.csus.ecs.pc2.validator;

import java.io.File;
import java.util.Hashtable;
import java.util.UUID;

import junit.framework.TestCase;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.execute.IResultsParser;
import edu.csus.ecs.pc2.core.execute.XMLResultsParser;

/**
 * JUnit test cases for Validator.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class ValidatorTest extends TestCase {

    private boolean unitDebug = true;

    private String testDataDirectoryName = "testdata" + File.separator + "validator";

    private String tempOutputDirectoryName = testDataDirectoryName + File.separator + "output";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        // Project path for JUnit with Cruise Control
        String projectPath = "projects" + File.separator + "pc2-9.1";
        
        // Directory where test data is
        String testDir = "testdata";
        
        File dir = new File(testDir);
        if (!dir.exists()) {
            
            String testDirectoryName = projectPath + File.separator + testDir;

            // TODO, try to find this path in the environment
            dir = new File(testDirectoryName);
            
            if (dir.exists()) {
                testDataDirectoryName = testDirectoryName + File.separator + "validator";
                tempOutputDirectoryName = testDataDirectoryName + File.separator + "output";
                
                // Create test output directory
                Utilities.insureDir(tempOutputDirectoryName);
            } else {
                throw new Exception ("Could not find "+testDir+" under "+testDirectoryName);
            }
        } else {
            // Create test output directory
            Utilities.insureDir(tempOutputDirectoryName);
        }
    }
    
    /**
     * Return full path and filename to test data directory.
     * 
     * @param filename
     * @return
     */
    private String getFullPathName(String filename) {
        return testDataDirectoryName + File.separator + filename;
    }

    /**
     * return a random filename (no path).
     * 
     * @param prefix
     *            an optional string prefixed to the filename.
     * @return
     */
    protected String randomFileName(String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        UUID uuid = UUID.randomUUID();

        return prefix + uuid.toString();
    }

    protected String randomOutputFileName(String prefix) {
        return tempOutputDirectoryName + File.separator + randomFileName(prefix);
    }

    public void testAll() {

        // -pc2 test options
        // 1 - diff
        // 2 - ignore whitespace at start of file
        // 3 - ignore leading whitespace on lines
        // 4 - ignore all whitespace on lines
        // 5 - ignore empty lines
        // 6 2 & 3
        // 7 2 & 4
        // 8 2 & 5
        // 9 3 & 5
        // 10 4 & 5

        int[] pc2TestNumbers = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

        for (int i = 0; i < pc2TestNumbers.length; i++) {
            int pc2TestNumber = pc2TestNumbers[i];

            String outcome = Validator.JUDGEMENT_YES;

            validateOneLineIdentical(pc2TestNumber, outcome);
            validateTwoEmptyFiles(pc2TestNumber, outcome);
            validateFileSame(pc2TestNumber, outcome);

            outcome = Validator.JUDGEMENT_NO_WRONG_ANSWER;

            validateOneLineDifferent(pc2TestNumber, outcome);
            validateOneLineEmpty(pc2TestNumber, outcome);
            validateOneLineBlank(pc2TestNumber, outcome);
            validateFileDiff1stChar(pc2TestNumber, outcome);
            validateFileDiffMissLine(pc2TestNumber, outcome);
            validateFileDiff(pc2TestNumber, outcome);
        }
    }

    /**
     * Test two identical files, one line per file.
     */
    public void validateOneLineIdentical(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("oneline1.txt");

        runValidatorTest(outputFilename, outputFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test two files, single line, different contents.
     */
    public void validateOneLineDifferent(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("oneline1.txt");

        String answerFilename = getFullPathName("oneline2.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test two files, both empty (0 byte) files.
     */
    public void validateTwoEmptyFiles(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("emptyfile");

        String answerFilename = getFullPathName("emptyfile");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test two files, first file empty, second file one line.
     */
    public void validateOneLineEmpty(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("emptyfile");

        String answerFilename = getFullPathName("oneline1.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test two files, firs file one empty line, second file one line.
     */
    public void validateOneLineBlank(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("oneline0.txt");

        String answerFilename = getFullPathName("oneline1.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test two identical multi line files.
     */
    public void validateFileSame(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("testfile.txt");

        String answerFilename = getFullPathName("testfile.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test a multi line output and answer, same contents
     */
    public void validateFileDiff(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("testfile.txt");

        String answerFilename = getFullPathName("testdatain.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test only the first character is different.
     */
    public void validateFileDiff1stChar(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("testfile.txt");

        String answerFilename = getFullPathName("testfile2.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test only the last character is different.
     */
    public void validateFileDiffLastChar(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("testfile.txt");

        String answerFilename = getFullPathName("testfileLastCh.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test if a single line is missing.
     */
    public void validateFileDiffMissLine(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("testfile.txt");

        String answerFilename = getFullPathName("testfileML.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Runs the Validator comparing contents of two files.
     * 
     * Runs the validator against outputFileName and answerFileName with expected outcome/judgement of judgementString.
     * 
     * @param outputFileName
     * @param answerFileName
     * @param judgementString
     *            expected judgement text
     */
    protected void runValidatorTest(String outputFileName, String answerFileName, String judgementString, int pc2TestNumber) {

        String inFilename = getFullPathName("testdatain.txt");

        String resultFilename = randomOutputFileName("result");

        String results = validatorResultXML(inFilename, outputFileName, answerFileName, resultFilename, pc2TestNumber, false);

        if (isUnitDebug() && !judgementString.equals(results)) {
            System.err.println("Failed " + judgementString + " vs (validator) " + results);
            System.err.println("  Out: " + outputFileName);
            System.err.println("  Val: " + answerFileName);
            System.err.println("  Out: " + resultFilename);
            System.err.println("  Cmd: validator " + inFilename + " " + outputFileName + " " + answerFileName + " " + resultFilename + " -pc2 " + pc2TestNumber + " " + false);
        }

        assertEquals("Failed -pc2 " + pc2TestNumber, results, judgementString);
    }

    /**
     * Run Validator and return results.
     * 
     * 
     * @param inName
     * @param outName
     * @param ansName
     * @param resFile
     * @param pc2TestNumber
     * @param ignoreCase
     * @return null or outcome string from XML generated by validator.
     */
    protected String validatorResultXML(String inputFileName, String outputFileName, String answerFileName, String resultsFileName, int pc2TestNumber, boolean ignoreCase) {

        /*
         * Validator command line syntax: Validator <inputfile name> <outputfile name> <answerfile name> <resultfile name> <-pc2> [options] icflag
         */

        try {

            String[] args = { inputFileName, outputFileName, answerFileName, resultsFileName, "-pc2", "" + pc2TestNumber, "" + ignoreCase };
            Validator.main(args);

            IResultsParser parser = new XMLResultsParser();
            // parser.setLog(log);
            boolean done = parser.parseValidatorResultsFile(resultsFileName);
            Hashtable<String, String> results = parser.getResults();

            if (done && results != null && results.containsKey("outcome")) {
                return results.get("outcome");
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isUnitDebug() {
        return unitDebug;
    }

    public void setUnitDebug(boolean unitDebug) {
        this.unitDebug = unitDebug;
    }
}

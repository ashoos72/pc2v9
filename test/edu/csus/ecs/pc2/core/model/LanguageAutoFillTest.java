package edu.csus.ecs.pc2.core.model;

import junit.framework.TestCase;

/**
 * Prints language definitions.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class LanguageAutoFillTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testVariableNames() throws Exception {
        String[] keys = { LanguageAutoFill.JAVATITLE, LanguageAutoFill.DEFAULTTITLE, LanguageAutoFill.GNUCPPTITLE, LanguageAutoFill.GNUCTITLE, LanguageAutoFill.PERLTITLE, LanguageAutoFill.MSCTITLE,
                LanguageAutoFill.KYLIXTITLE, LanguageAutoFill.KYLIXCPPTITLE, LanguageAutoFill.FPCTITLE };

        for (String key : keys) {

            String[] values = LanguageAutoFill.getAutoFillValues(key);

//            String theKey = values[0];
            String compileCommandLine = values[1];
//            String executableFilename = values[2];
            String programExecutionCommandLine = values[3];
//            String displayName = values[4];
            
            assertTrue("Expected {: in "+compileCommandLine + " for "+key, compileCommandLine.indexOf("{:") > -1);
            assertTrue("Expected {: in "+programExecutionCommandLine + " for "+key, programExecutionCommandLine.indexOf("{:") > -1);

        }
    }

    public static void printDefs() {

        String[] keys = { LanguageAutoFill.JAVATITLE, LanguageAutoFill.DEFAULTTITLE, LanguageAutoFill.GNUCPPTITLE, LanguageAutoFill.GNUCTITLE, LanguageAutoFill.PERLTITLE, LanguageAutoFill.MSCTITLE,
                LanguageAutoFill.KYLIXTITLE, LanguageAutoFill.KYLIXCPPTITLE, LanguageAutoFill.FPCTITLE };

        for (String key : keys) {

            String[] fields = LanguageAutoFill.getAutoFillValues(key);

            System.out.println("Language: " + fields[4]);
            System.out.println("             " + fields[0]);
            System.out.println("             " + fields[1]);
            System.out.println("             " + fields[2]);
            System.out.println("             " + fields[3]);
            System.out.println("             " + fields[4]);
            System.out.println();
        }
    }

    public static void main(String[] args) {

        printDefs();

    }

}
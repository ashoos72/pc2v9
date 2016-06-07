package edu.csus.ecs.pc2.core.report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.GregorianCalendar;

import edu.csus.ecs.pc2.VersionInfo;
import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.Category;
import edu.csus.ecs.pc2.core.model.ContestInformation;
import edu.csus.ecs.pc2.core.model.Filter;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.ProblemDataFiles;
import edu.csus.ecs.pc2.core.model.SerializedFile;

/**
 * Print all problems info.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class ProblemsReport implements IReport {

    /**
     * 
     */
    private static final long serialVersionUID = 977130815676827828L;

    private IInternalContest contest;

    private IInternalController controller;

    private Log log;

    private Filter filter;

    private void writeContestTime(PrintWriter printWriter) {
        printWriter.println();
        GregorianCalendar resumeTime = contest.getContestTime().getResumeTime();
        if (resumeTime == null) {
            printWriter.println("Contest date/time: never started");
        } else {
            printWriter.println("Contest date/time: " + resumeTime.getTime());

        }
    }

    public void writeRow(PrintWriter printWriter, Problem problem, ProblemDataFiles problemDataFiles) {

        String deletedText = "";
        if (!problem.isActive()) {
            deletedText = " [HIDDEN] ";
        }

        printWriter.println("  Problem '" + problem + deletedText + "' ver=" + problem.getElementId().getVersionNumber() + " id=" + problem.getElementId());
        printWriter.println("       Short name       : " + problem.getShortName());
        printWriter.println("       Letter           : " + problem.getLetter());
        printWriter.println("       Data file name   : " + problem.getDataFileName());
        printWriter.println("       Answer file name : " + problem.getAnswerFileName());
        printWriter.println("       Read from stdin  : " + Utilities.yesNoString(problem.isReadInputDataFromSTDIN()));
        printWriter.println("       Number test cases: " + problem.getNumberTestCases());
        printWriter.println();

        printWriter.print("   Execution time limit : " + problem.getTimeOutInSeconds() + " seconds");
        if (problem.getTimeOutInSeconds() == 0) {
            printWriter.print(" (no time limit when zero seconds)");
        }
        printWriter.println();

        printWriter.println("        Computer Judged : " + problem.isComputerJudged());

        if (problem.isComputerJudged()) {
            printWriter.println("         Manual Review  : " + problem.isManualReview());
        } else {
            printWriter.println("         Manual Review  : true");
        }

        printWriter.println("  Show Prelim Judgement : " + problem.isPrelimaryNotification());

        printWriter.println();

        printWriter.println("        Using validator : " + problem.isValidatedProblem());
        printWriter.println("         Validator name : " + problem.getValidatorProgramName());

        printWriter.println("     Validator cmd line : " + problem.getValidatorCommandLine());
        printWriter.println("     Validator option # : " + problem.getWhichPC2Validator());
        printWriter.println("    Using pc2 validator : " + problem.isUsingPC2Validator());

        printWriter.println("   Using external files : " + problem.isUsingExternalDataFiles() + " path = " + problem.getExternalDataFileLocation());
        printWriter.println("               CCS mode : " + problem.isCcsMode());

        if (problem.getAnswerFileName() != null) {
            if (problemDataFiles != null) {
                if (problemDataFiles.getJudgesAnswerFiles().length == 0) {
                    printWriter.println("                          Warning - no answer files defined (no contents) ");
                }
            } else {
                printWriter.println("                          Warning - no data/answer files defined (null problemDataFiles) ");
            }
        }

        if (problem.getAnswerFileName() != null) {
            if (problemDataFiles != null) {
                if (problemDataFiles.getJudgesDataFiles().length == 0) {
                    printWriter.println("                          Warning - no judges data files defined (no contents) ");
                }
            } else {
                printWriter.println("                          Warning - no data/judge files defined (null problemDataFiles) ");
            }
        }

        if (problem.getNumberTestCases() > 1) {
            for (int i = 0; i < problem.getNumberTestCases(); i++) {
                int testCaseNumber = i + 1;
                String datafile = problem.getDataFileName(testCaseNumber);
                String answerfile = problem.getAnswerFileName(testCaseNumber);

                printWriter.println("       Data File name " + testCaseNumber + " : " + datafile);
                printWriter.println("     Answer File name " + testCaseNumber + " : " + answerfile);
            }
        }

        writeProblemDataFiles(printWriter, problemDataFiles);
    }

    public void writeProblemDataFiles(PrintWriter printWriter, ProblemDataFiles problemDataFiles) {

        if (problemDataFiles != null) {
            SerializedFile[] judgesDataFiles = problemDataFiles.getJudgesDataFiles();
            SerializedFile[] judgesAnswerFiles = problemDataFiles.getJudgesAnswerFiles();

            if (judgesDataFiles != null) {

                printWriter.println("                  " + judgesDataFiles.length + " judge data files");

                if (judgesDataFiles.length > 0) {
                    for (SerializedFile serializedFile : judgesDataFiles) {
                        Integer bytes = null;
                        String name = null;
                        String shaSum = null;
                        if (serializedFile != null && serializedFile.getBuffer() != null) {
                            bytes = serializedFile.getBuffer().length;
                            name = serializedFile.getName();
                            shaSum = serializedFile.getSHA1sum();
                        }
                        printWriter.println("                    judge data file '" + name + "' " + bytes + " bytes, " + internExternDesc(serializedFile) + " SHA1 = " + shaSum);
                    }
                }
            } else {
                printWriter.println("                  * No judge's data files *");
            }

            if (judgesAnswerFiles != null) {

                printWriter.println("                  " + judgesAnswerFiles.length + " judge answer files");
                if (judgesAnswerFiles.length > 0) {
                    for (SerializedFile serializedFile : judgesAnswerFiles) {
                        Integer bytes = null;
                        String name = null;
                        String shaSum = null;
                        if (serializedFile != null && serializedFile.getBuffer() != null) {
                            bytes = serializedFile.getBuffer().length;
                            name = serializedFile.getName();
                            shaSum = serializedFile.getSHA1sum();
                        }
                        printWriter.println("                    judge ans. file '" + name + "' " + bytes + " bytes, " + internExternDesc(serializedFile) + " SHA1 = " + shaSum);
                    }
                }
            } else {
                printWriter.println("                  * No judge's answer files *");
            }
        } else {
            printWriter.println("                  * No judge's data or answer files *");
        }

        if (problemDataFiles != null && problemDataFiles.getValidatorFile() != null) {
            SerializedFile validatorFile = problemDataFiles.getValidatorFile();
            printWriter.println("                  " + 1 + " validator file");
            int bytes = 0;
            if (validatorFile.getBuffer() != null) {
                bytes = validatorFile.getBuffer().length;
            }
            printWriter.println("                    validator file '" + validatorFile.getName() + "' " + bytes + " bytes " + internExternDesc(validatorFile));
        } else {
            printWriter.println("                  * No validator files *");
        }
    }

    /**
     * Returns External or Internal (storage) word.
     * 
     * @param serializedFile
     * @return External or Internal (storage) word.
     */
    private String internExternDesc(SerializedFile serializedFile) {
        if (serializedFile.isExternalFile()) {
            return "External";
        } else {
            return "Internal";
        }
    }

    public void writeReport(PrintWriter printWriter) {

        // Problem
        printWriter.println();
        printWriter.println("-- " + contest.getProblems().length + " problems --");

        ContestInformation info = contest.getContestInformation();
        if (info != null) {

            String judgeCDPBasePath = info.getJudgeCDPBasePath();
            if (judgeCDPBasePath == null) {
                judgeCDPBasePath = "";
            }

            printWriter.println();
            printWriter.println("  Location for Judges CDP / problem config : '" + judgeCDPBasePath + "'");

            String adminCDPPath = info.getAdminCDPBasePath();
            if (adminCDPPath == null) {
                adminCDPPath = "";
            }
            printWriter.println("  Location for  Admin CDP / problem config : '" + adminCDPPath + "'");

        }

        for (Problem problem : contest.getProblems()) {
            printWriter.println();
            ProblemDataFiles problemDataFiles = contest.getProblemDataFile(problem);
            writeRow(printWriter, problem, problemDataFiles);
        }

        // ProblemDataFiles
        printWriter.println();
        printWriter.println("-- " + contest.getProblemDataFiles().length + " problem data file sets --");
        for (ProblemDataFiles problemDataFile : contest.getProblemDataFiles()) {
            printWriter.println();
            Problem problem = contest.getProblem(problemDataFile.getProblemId());
            printWriter.println("  Problem Data File set for " + problem + " id=" + problemDataFile.getProblemId());
            writeProblemDataFiles(printWriter, problemDataFile);
        }

        Category[] categories = contest.getCategories();
        if (categories.length > 0) {
            printWriter.println(" Categories:");
            for (int i = 0; i < categories.length; i++) {
                Category category = categories[i];
                printWriter.println("  " + category);
            }
        } else {
            printWriter.println(" Categories: (not defined)");
        }
    }

    public void printHeader(PrintWriter printWriter) {
        printWriter.println(new VersionInfo().getSystemName());
        printWriter.println("Date: " + Utilities.getL10nDateTime());
        printWriter.println(new VersionInfo().getSystemVersionInfo());
        printWriter.println();
        printWriter.println(getReportTitle() + " Report");

        writeContestTime(printWriter);
    }

    public void printFooter(PrintWriter printWriter) {
        printWriter.println();
        printWriter.println("end report");
    }

    public void createReportFile(String filename, Filter inFilter) throws IOException {

        PrintWriter printWriter = new PrintWriter(new FileOutputStream(filename, false), true);

        try {

            try {
                printHeader(printWriter);

                writeReport(printWriter);

                printFooter(printWriter);

            } catch (Exception e) {
                printWriter.println("Exception in report: " + e.getMessage());
                e.printStackTrace(printWriter);
            }

            printWriter.close();
            printWriter = null;

        } catch (Exception e) {
            log.log(Log.INFO, "Exception writing report", e);
            printWriter.println("Exception generating report " + e.getMessage());
        }
    }

    public String[] createReport(Filter inFilter) {
        throw new SecurityException("Not implemented");
    }

    public String createReportXML(Filter inFilter) throws IOException {
        return Reports.notImplementedXML(this);
    }

    public String getReportTitle() {
        return "Problems";
    }

    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        this.contest = inContest;
        this.controller = inController;
        log = controller.getLog();
    }

    public String getPluginTitle() {
        return "Problems Report";
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}

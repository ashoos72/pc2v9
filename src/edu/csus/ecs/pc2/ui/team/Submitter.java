package edu.csus.ecs.pc2.ui.team;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import edu.csus.ecs.pc2.api.IClient;
import edu.csus.ecs.pc2.api.IContest;
import edu.csus.ecs.pc2.api.ILanguage;
import edu.csus.ecs.pc2.api.IProblem;
import edu.csus.ecs.pc2.api.IRun;
import edu.csus.ecs.pc2.api.IRunComparator;
import edu.csus.ecs.pc2.api.ServerConnection;
import edu.csus.ecs.pc2.api.exceptions.LoginFailureException;
import edu.csus.ecs.pc2.api.listener.IRunEventListener;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.ParseArguments;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.exception.CommandLineErrorException;
import edu.csus.ecs.pc2.core.model.ClientId;

/**
 * Command line submit run.
 * 
 * Uses the API.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class Submitter {

    private ServerConnection serverConnection = null;

    private String login;

    private String password;

    private String languageTitle;

    private String problemTitle;

    private IContest contest;

    private IProblem submittedProblem;

    private ILanguage submittedLanguage;

    private IClient submittedUser;

    private String[] otherFiles = new String[0];

    private String mainSubmissionFileName;
    
    public static final String[] CCS_REQUIRED_OPTIONS_LIST = { "-p", "-u", "-w", "-m", "-d", "-l" };

    private String[] allCCSOptions = new String[0];
    
    /**
     * print all missing options if command line error.
     */
    private boolean showAllMissingOptions = false;

    /**
     * --check option.
     * 
     */
    private boolean checkArg = false;

    /**
     * Filename for source to be submitted.
     */
    private String submittedFileName;

    private boolean debugMode = false;

    /**
     * Override elapsed time for run (contest time for run), only used in test mode
     */
    private int timeStamp;

    protected Submitter() {

    }

    public Submitter(String login) {
        super();
        setLoginPassword(login, null);
    }

    /**
     * Login with login and password.
     * 
     * @param login
     * @param password
     */
    public Submitter(String login, String password) {
        super();
        setLoginPassword(login, password);
    }

    public Submitter(String[] args) throws CommandLineErrorException {
        loadVariables(args);
    }

    /**
     * Expand shortcut names.
     * 
     * @param loginName
     */
    private void setLoginPassword(String loginName, String inPassword) {

        ClientId id = InternalController.loginShortcutExpansion(1, loginName);
        if (id != null) {

            login = id.getName();
            password = inPassword;

            if (password == null) {
                password = login;
            }

        }
    }

    protected void loadVariables(String[] args) throws CommandLineErrorException {

        if (args.length == 0 || args[0].equals("--help")) {
            usage();
            System.exit(4);
        }
        
        if (args.length == 0 || args[0].equals("--helpCCS")) {
            usageCCS();
            System.exit(4);
        }
        
        /**
         * If any of the CCS options are present then validate that all
         * CCS options are present.
         */
        
        if (hasAnyCCSArguments (args, getAllCCSOptions())){
            int numberMissingCCSArguments = numberMissingArguments(args, CCS_REQUIRED_OPTIONS_LIST);
            
            if (numberMissingCCSArguments > 0){
                
                if (showAllMissingOptions) {
                    printMissingArguments(args,CCS_REQUIRED_OPTIONS_LIST);
                }
                throw new CommandLineErrorException("Missing required command line argument(s)");
            }
            
            loadCCSVariables(args, getAllCCSOptions());
            
        } else {
            
            String[] opts = { "--login", "--password"};
            loadPC2Variables(args, opts);
        }
    }



    private void loadCCSVariables(String[] args, String[] opts) throws CommandLineErrorException {

        ParseArguments arguments = new ParseArguments(args, opts);

        timeStamp = 0;
        
        debugMode = arguments.isOptPresent("--debug");

        checkArg = arguments.isOptPresent("--check");

        // -u <team id>
        String cmdLineLogin = arguments.getOptValue("-u");

        // -w <team password>
        String cmdLinePassword = arguments.getOptValue("-w");

        setLoginPassword(cmdLineLogin, cmdLinePassword);

        // -p <problem short-name>
        problemTitle = arguments.getOptValue("-p");

        if (arguments.isOptPresent("--list")) {
            listInfo();
            System.exit(0);
        } else if (arguments.isOptPresent("--listruns")) {
            listRuns();
            System.exit(0);
        } else {

            // -p <problem short-name>
            problemTitle = arguments.getOptValue("-p");

            // -t <contest-time for submission>
          
            try {
                if (arguments.isOptPresent("-t")){
                    timeStamp = Integer.parseInt(arguments.getOptValue("-t"));
                }
            } catch (Exception e) {
                throw new CommandLineErrorException("Invalid number after -t", e);
            }

            // -m <main source filename>
            mainSubmissionFileName = arguments.getOptValue("-m");

            // -d <directory for main and other files>
            String sourceDirectory = arguments.getOptValue("-d");

            requireDirectory(sourceDirectory, "source file directory");

            if (!sourceDirectory.endsWith(File.separator)) {
                sourceDirectory += File.separator;
            }

            submittedFileName = sourceDirectory + mainSubmissionFileName;

            requireFile(submittedFileName, "main source filename");

            otherFiles = getAllOtherFileNames(sourceDirectory, mainSubmissionFileName);
        }

        if (password == null) {
            password = login;
        }
    }

    private String[] getAllOtherFileNames(String sourceDirectory, String mainfileName) {
        
        ArrayList<String> list = new ArrayList<String>();

        File[] listOfFiles = new File(sourceDirectory).listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                if (!file.getName().equals(mainfileName)) {
                    list.add(sourceDirectory + File.separator + file.getName());
                }
            }
        }
        
        return (String[]) list.toArray(new String[list.size()]);
    }

    private void requireFile(String sourceFile, String description) throws CommandLineErrorException {
        if (! new File(sourceFile).isFile()){
            throw new CommandLineErrorException(description+" missing ("+sourceFile+")");
        } 
    }

    private void requireDirectory(String sourceDirectory, String description) throws CommandLineErrorException {
        if (! new File(sourceDirectory).isDirectory()){
            throw new CommandLineErrorException(description+" missing ("+sourceDirectory+")");
        }
    }

    private void loadPC2Variables(String[] args, String[] opts) {
        
        ParseArguments arguments = new ParseArguments(args, opts);
        
        debugMode = arguments.isOptPresent("--debug");

        checkArg = arguments.isOptPresent("--check");
        
        String cmdLineLogin = null;
        
        String cmdLinePassword = null;

        if (arguments.isOptPresent("--login")) {
            cmdLineLogin = arguments.getOptValue("--login");
        }

        if (arguments.isOptPresent("--password")) {
            cmdLinePassword = arguments.getOptValue("--password");
        }

        setLoginPassword(cmdLineLogin, cmdLinePassword);

        if (arguments.isOptPresent("--list")) {
            listInfo();
            System.exit(0);
        } else if (arguments.isOptPresent("--listruns")) {
            listRuns();
            System.exit(0);
        } else {

            submittedFileName = arguments.getArg(0);
            if (submittedFileName == null) {
                System.err.println("Error - missing filename");
                System.exit(4);
            }

            if (arguments.getArgCount() > 1) {
                problemTitle = arguments.getArg(1);
            }

            if (arguments.getArgCount() > 2) {
                languageTitle = arguments.getArg(2);
            }
        }

    }

    protected boolean hasAnyCCSArguments(String[] args, String[] requiredOpts) {

        ParseArguments parseArguments = new ParseArguments(args, requiredOpts);

        for (String s : requiredOpts) {
            if (parseArguments.isOptPresent(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Scans command line, if missing options returns count of missing arguments.
     * 
     * If any one of the required opts is present and any other option is
     * missing then will return the number of missing required options
     * and values for those options.
     * 
     * @param args
     * @param requiredOpts
     * @return if any option is present, and any other is not present return count.
     */
    protected int numberMissingArguments(String[] args, String[] requiredOpts) {
        
        int count = 0;
        
        ParseArguments parseArguments = new ParseArguments(args, requiredOpts);
        
        for (String s : requiredOpts){
            if (! parseArguments.isOptPresent(s)){
                count ++;
            } else if (! parseArguments.optHasValue(s)){
                count ++;
            }
        }
        return count;
    }
    
    private void printMissingArguments(String[] args, String[] requiredOpts) {

        ParseArguments parseArguments = new ParseArguments(args, requiredOpts);

        for (String s : requiredOpts) {
            if (!parseArguments.isOptPresent(s)) {
                System.err.println("Missing required command line parameter " + s);
            } else if (!parseArguments.optHasValue(s)) {
                System.err.println("Missing required value after command line parameter " + s);
            }
        }
    }

    private String getLanguageFromFilename(String filename2) {

        if (filename2.endsWith(".java")) {
            return findLanguageName("Java");
        } else if (filename2.endsWith(".cpp")) {
            return findLanguageName("C++");
        } else if (filename2.endsWith(".c")) {
            return findLanguageName("C");
        } else {
            return languageTitle;
        }
    }

    protected String findLanguageName(String string) {

        for (ILanguage language : contest.getLanguages()) {
            if (language.getName().equalsIgnoreCase(string)) {
                return language.getName();
            } else if (language.getName().indexOf(string) > -1) {
                return language.getName();
            }
        }
        return string;
    }

    protected String getProblemNameFromFilename(String filename) {

        String baseName = Utilities.basename(filename);

        // Strip extension
        int lastIndex = baseName.lastIndexOf('.');
        if (lastIndex > 1) {
            baseName = baseName.substring(0, lastIndex - 1);
        }

        IProblem problem = matchProblem(baseName);
        if (problem != null) {
            return problem.getName();
        } else {
            return baseName;
        }
    }
    
    private void usageCCS() {
        String[] usage = { //
                "", //
                "Usage Submitter [--help|--list|--listruns|--check] options", //
                "Usage Submitter [-t timestamp] -u loginname -w password -p problem -l language -d directory -m mainfile", //
                "Usage Submitter [-F propfile] [--help|--list|--listruns|--check] ", //
                "", //
                "Submit filename for problem and language.  ", //
                "", //
                "--helpCCS      - this listing", //
                "", //
                "-p problem     - contest problem letter or name", //
                "", //
                "-l language    - contest language", //
                "", //
                "-u loginname   - user login ", //
                "", //
                "-w password    - user password", //
                "", //
                "-m filename    - main source file name in directory specified by -d option", //
                "", //
                "-d directory   - for main source and other source files", //
                "", //
                "-t timestamp   - (optional)  contest-time for submission  ", //
                "", //
                "--list         - list problem and languages", //
                "", //
                "--listruns     - list run info for the user", //
                "", //
                "On success exit code will be 0", //
                "On failure exit code will be non-zero", //
                "", //
                "$Id$", //
        };

        for (String s : usage) {
            System.out.println(s);
        }
    }

    private void usage() {
        String[] usage = { //
        "Usage Submitter [--help|--list|--listruns|--check] --login loginname [--password password] filename [problem [language]]", //
                "Usage Submitter [-F propfile] [--help|--list|--listruns|--check] filename [problem [language]]", //
                "Usage Submitter [options] filename1[,filename2[,filename3[,...]]] [problem [language]]", //
                  "", //
                "Submit filename for problem and language.  If problem or language", //
                "not specified the program will guess which problem and language", //
                "based on the file name.", //
                "", //
                "--help   this listing", //
                "", //      
                "--helpCCS  CCS testing usage info", //
                "", //
                "--check  login and check parameters: list problem, language and files that would be submitted.", //
                "", //
                "--list   list problem and languages", //
                "", //
                "--listruns list run info for the user", //
                "", //
                "On success exit code will be 0", //
                "On failure exit code will be non-zero", //
                "", //
                "$Id$", //
        };

        for (String s : usage) {
            System.out.println(s);
        }
    }

    public void submitRun(String mainfilename, String problemName, String languageName) {
        submittedFileName = mainfilename;
        problemTitle = problemName;
        languageTitle = languageName;
        submitRun();
    }

    private String getProblemNameFromLetter(char letter) {
        try {
            letter = Character.toUpperCase(letter);
            int idx = letter - 'A';
            IProblem[] problems = contest.getProblems();
            return problems[idx].getName();
        } catch (Exception e) {
            return new Character(letter).toString();
        }
    }

    /**
     * Submit a run.
     */
    public void submitRun() {

        boolean success = false;

        try {

            checkRequiredParams();

            serverConnection = new ServerConnection();

            contest = serverConnection.login(login, password);
            
            System.out.println("For: " + contest.getMyClient().getDisplayName() + " (" + contest.getMyClient().getLoginName() + ")");
            System.out.println();

            try {

                // Register for run event.

                RunEventListener runliEventListener = new RunEventListener();
                contest.addRunListener(runliEventListener);

                submitTheRun(problemTitle, languageTitle, mainSubmissionFileName, otherFiles);

                waitForRunSubmissionConfirmation(runliEventListener, 3);

                IRun run = runliEventListener.getRun();

                if (runliEventListener.getRun() != null) {
                    // got a run
                    success = true;

                    System.out.println("Submission confirmation: Run " + run.getNumber() + ", problem " + run.getProblem().getName() + //
                            ", for team " + run.getTeam().getDisplayName() + //
                            " (" + run.getTeam().getLoginName() + ")");
                } 
                // no else
                
                serverConnection.logoff();

            } catch (Exception e) {
                System.err.println("Unable to submit run: " + e.getMessage());
                if (debugMode){
                    e.printStackTrace();
                }
            }
  
        } catch (LoginFailureException e) {
            System.out.println("Unable to login: " + e.getMessage());
            if (debugMode){
                e.printStackTrace();
            }
        }

        if (success) {
            System.exit(0);
        } else {
            System.exit(4);
        }
    }

    /**
     * Check that they have supplied required parameters.
     * 
     * @throws LoginFailureException
     */
    private void checkRequiredParams() throws LoginFailureException {

        if (login == null) {
            throw new LoginFailureException("No login specified");
        }
        if (password == null) {
            throw new LoginFailureException("No password specified");
        }
    }

    /**
     * Waits for run confirmation.
     * 
     * @param listener
     * @throws Exception
     */
    private void waitForRunSubmissionConfirmation(RunEventListener listener, int seconds) throws Exception {

        boolean done = false;

        long waittime = seconds * 1000;

        long startTime = new Date().getTime();

        long timeLimit = startTime + waittime;

        while (!done) {

            if (listener.getRun() != null) {
                done = true;
            }

            if (new Date().getTime() > timeLimit) {
                Thread.sleep(500);
                break;
            }
        }

        long totalTime = new Date().getTime() - startTime;

        System.out.println(totalTime + " ms");
        System.out.println();

        if (!done) {
            throw new Exception("Timed out waiting for run submission confirm ");
        }
    }

    /**
     * List who logged in, problems and languages.
     */
    public void listInfo() {

        try {

            checkRequiredParams();

            serverConnection = new ServerConnection();

            contest = serverConnection.login(login, password);

            try {
                listInfo(contest);
                
                serverConnection.logoff();

            } catch (Exception e) {
                e.printStackTrace();
                if (debugMode){
                    e.printStackTrace();
                }

            }

        } catch (LoginFailureException e1) {
            System.out.println("Unable to login: " + e1.getMessage());
            if (debugMode){
                e1.printStackTrace();
            }
        }

    }

    /**
     * Login and output runs for login.
     * 
     */
    public void listRuns() {

        try {
            checkRequiredParams();

            serverConnection = new ServerConnection();

            contest = serverConnection.login(login, password);
            
            System.out.println();
            System.out.println(contest.getContestTitle());
            System.out.println();
            System.out.println("For: "+contest.getMyClient().getDisplayName()+" ("+contest.getMyClient().getLoginName()+")");
            System.out.println();

            IRun[] runs = contest.getRuns();
            if (runs.length == 0) {
                System.out.println("No runs submitted");
            } else {
                System.out.println(runs.length + " runs for " + contest.getMyClient().getDisplayName() + " (" + contest.getMyClient().getLoginName() + ")");
                System.out.println();
                Arrays.sort(runs, new IRunComparator());
                for (IRun run : runs) {
                    System.out.println("Run " + run.getNumber() + " at " + run.getSubmissionTime() + " by " + contest.getMyClient().getLoginName() + //
                            " " + run.getJudgementName() + //
                            " " + run.getProblem().getName() + " " + run.getLanguage().getName());
                }
            }
        } catch (LoginFailureException e1) {
            System.out.println("Unable to login: " + e1.getMessage());
            if (debugMode){
                e1.printStackTrace();
            }
        }

    }

    /**
     * List who logged in, problems and languages.
     * 
     * @param contest2
     */
    private void listInfo(IContest contest2) {

        System.out.println("Logged in as: " + contest2.getMyClient().getDisplayName());

        System.out.println();

        char let = 'A';

        System.out.println("Problems");
        for (IProblem problem : contest.getProblems()) {
            System.out.println(let + " - " + problem.getName());
            let++;
        }

        System.out.println();

        System.out.println("Languages");
        for (ILanguage language : contest.getLanguages()) {
            System.out.println(language.getName());
        }

        System.out.println();
    }

    /**
     * Submit run.
     * 
     * @param problemTitle2
     * @param languageTitle2
     * @param fileNames filename or comma delimited file list
     * @throws Exception
     */
    private void submitTheRun(String problemTitle2, String languageTitle2, String mainFileName, String [] additionalFilenames) throws Exception {

        submittedProblem = null;

        submittedLanguage = null;
        
        if (languageTitle2 == null) {
            languageTitle2 = getLanguageFromFilename(mainFileName);
        }

        ILanguage language = matchLanguage(languageTitle2);

        if (languageTitle2 == null) {
            throw new Exception("Could not determine Language based on filename '" + mainFileName + "'");
        }

        if (language == null) {
            throw new Exception("Could not match language '" + languageTitle2 + "'");
        }

        if (problemTitle2 == null) {
            problemTitle2 = getProblemNameFromFilename(mainFileName);
        }
        if (problemTitle2 != null && problemTitle2.length() == 1) {
            problemTitle2 = getProblemNameFromLetter(problemTitle2.charAt(0));
        }

        IProblem problem = matchProblem(problemTitle2);

        if (problemTitle2 == null) {
            throw new Exception("Could not determine Problem based on filename '" + mainFileName + "'");
        }

        if (problem == null) {
            throw new Exception("Could not match problem '" + problemTitle2 + "'");
        }

        if (checkArg) {

            System.out.println("For   : " + contest.getMyClient().getLoginName() + " - " + contest.getMyClient().getDisplayName());
            System.out.println("File  : " + mainFileName);

            for (String name : additionalFilenames) {
                System.out.println(" file : " + name);
            }
            System.out.println("Prob  : " + problem.getName());
            System.out.println("Lang  : " + language.getName());
            System.out.println();
            
            boolean success = true;
            
            if (! new File(mainFileName).isFile()){
                System.err.println("Error - file does not exist '"+mainFileName+"'");
                success = false;
            }
            
            for (String name : additionalFilenames) {
                if (! new File(name).isFile()){
                    System.err.println("Error - file does not exist '"+name+"'");
                    success = false;
                } 
            }
            
            if (success){
                System.exit(0);
            } else {
                System.exit(3);
            }

        } else {

            serverConnection.submitRun(problem, language, mainFileName, additionalFilenames, timeStamp);

            submittedProblem = problem;
            submittedLanguage = language;
            submittedUser = contest.getMyClient();
        }
    }

    /**
     * Find IProblem that matches the title.
     * 
     * Will look for an exact match, then look for a single letter used for problem, then looks for a problem title that starts with the input problem title.
     * 
     * @param problemTitle2
     *            title, letter or partial title.
     * @return
     */
    private IProblem matchProblem(String problemTitle2) {

        // check full name
        
        for (IProblem problem : contest.getProblems()) {
            if (problem.getName().equalsIgnoreCase(problemTitle2)) {
                return problem;
            }
        }

        char let = 'A';

        // check letter
        
        for (IProblem problem : contest.getProblems()) {
            if (problem.getName().equalsIgnoreCase(Character.toString(let))) {
                return problem;
            }
            let++;
        }
        
        // check start name

        for (IProblem problem : contest.getProblems()) {
            
            if (problem.getName().toLowerCase().startsWith(problemTitle2.toLowerCase())) {
                return problem;
            }
        }

        return null;
    }

    private ILanguage matchLanguage(String languageTitle2) {
        for (ILanguage language : contest.getLanguages()) {
            if (language.getName().equalsIgnoreCase(languageTitle2)) {
                return language;
            }
        }
        return null;
    }

    public ILanguage getSubmittedLanguage() {
        return submittedLanguage;
    }

    public IClient getSubmittedUser() {
        return submittedUser;
    }

    public IProblem getSubmittedProblem() {
        return submittedProblem;
    }

    /**
     * Listen for run events.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */

    // $HeadURL$
    protected class RunEventListener implements IRunEventListener, Runnable {

        private IRun submittedRun = null;

        public void runSubmitted(IRun run) {
            this.submittedRun = run;
        }

        public void runDeleted(IRun run) {
            // ignore

        }

        public void runCheckedOut(IRun run, boolean isFinal) {
            // ignore

        }

        public void runJudged(IRun run, boolean isFinal) {
            // ignore

        }

        public void runUpdated(IRun run, boolean isFinal) {
            // ignore

        }

        public void runCompiling(IRun run, boolean isFinal) {
            // ignore
        }

        public void runExecuting(IRun run, boolean isFinal) {
            // ignore
        }

        public void runValidating(IRun run, boolean isFinal) {
            // ignore
        }

        public void runJudgingCanceled(IRun run, boolean isFinal) {
            // ignore
        }

        public void run() {
            // ignore
        }

        public IRun getRun() {
            return submittedRun;
        }
    }

    public static void main(String[] args) {
        
        try {
            Submitter submitter = new Submitter(args);
            submitter.submitRun();
        } catch (CommandLineErrorException e) {
            System.err.println("Error on command line: "+e.getMessage());
        } catch (Exception e){
            System.err.println("Error submitting run "+e.getMessage());
            e.printStackTrace(System.err);
        }
        
    }
    
    public String[] getAllCCSOptions() {
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(CCS_REQUIRED_OPTIONS_LIST));
        list.add("-t");
        allCCSOptions = (String[]) list.toArray(new String[list.size()]);
        return allCCSOptions;
    }

    /**
     * 
     * @param showAllMissingOptions true means when exception show messages
     */
    public void setShowAllMissingOptions(boolean showAllMissingOptions) {
        this.showAllMissingOptions = showAllMissingOptions;
    }
    /**
     * 
     * @see #setShowAllMissingOptions(boolean).
     * @return true if show missing options
     */
    public boolean isShowAllMissingOptions() {
        return showAllMissingOptions;
    }
}
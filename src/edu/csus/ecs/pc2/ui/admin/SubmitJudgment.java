package edu.csus.ecs.pc2.ui.admin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import edu.csus.ecs.pc2.api.IClient;
import edu.csus.ecs.pc2.api.IContest;
import edu.csus.ecs.pc2.api.IJudgement;
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
 * Command line submit judgment.
 * 
 * Allows an external program, with proper Admin credentials, to submit a Judgment for a run.
 * 
 * Uses the API.
 * 
 * @author pc2@ecs.csus.edu
 */

// $HeadURL$
public class SubmitJudgment {

    private ServerConnection serverConnection = null;

    private String login;

    private String password;

    private IContest contest;

    private ILanguage submittedLanguage;

    private IClient submittingUser;

    public static final String[] CCS_REQUIRED_OPTIONS_LIST = {"-l", "-m", "-p", "-u", "-w" };
    
    private RunEventListener runliEventListener = new RunEventListener();

    /**
     * Successful run exit code.
     *
     * Using a non-zero exit code because if there is a problem in the JVM
     * or elsewhere a zero exit code could be returned.
     */
    private static final int SUCCESS_EXIT_CODE = 5;

    // TODO move to Constants
    private static final String FILE_OPTION_STRING = "-F";

//    private static final String NL = System.getProperty("line.separator");

    private String[] allCCSOptions = new String[0];
    
    /**
     * print all missing options if command line error.
     */
    private boolean showAllMissingOptions = true;

    /**
     * --check option.
     * 
     */
    @SuppressWarnings("unused")
    private boolean checkArg = false; // TODO implement

    private boolean debugMode = false;

    private long runId;
    
    private String judgementAcronym;

    public SubmitJudgment(String[] args) throws CommandLineErrorException {
        loadProgramVariables(args, getAllCCSOptions());
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

    /**
     * Load program variables from command line arguments.
     * 
     * @param args
     * @param opts
     * @throws CommandLineErrorException
     */
    private void loadProgramVariables(String[] args, String[] opts) throws CommandLineErrorException {

        ParseArguments arguments = new ParseArguments(args, opts);
        
        if (args.length == 0) {
            usage();
            System.exit(4);
        }

        if (arguments.isOptPresent(FILE_OPTION_STRING)) {
            String propertiesFileName = arguments.getOptValue(FILE_OPTION_STRING);
            
            if (propertiesFileName == null){
                arguments.dumpArgs(System.err);
                fatalError("No file specified after -F option ");
            }
            
            System.out.println("debug 22 "+propertiesFileName);

            if (!(new File(propertiesFileName).exists())) {
                fatalError(propertiesFileName + " does not exist (pwd: " + Utilities.getCurrentDirectory() + ")", null);
            }

            try {
                arguments.overRideOptions(propertiesFileName);
            } catch (IOException e) {
                fatalError("Unable to read file " + propertiesFileName, e);
            }
        }

        debugMode = arguments.isOptPresent("--debug");

        if (debugMode) {
            arguments.dumpArgs(System.err);
        }
        
        System.out.println("debug 22 here ");
        
        arguments.dumpArgs(System.err); // debug 22
        
        
//        timeStamp = 0;
        checkArg = arguments.isOptPresent("--check");

        // -u <team id>
        String cmdLineLogin = arguments.getOptValue("-u");

        // -w <team password>
        String cmdLinePassword = arguments.getOptValue("-w");

        setLoginPassword(cmdLineLogin, cmdLinePassword);

        if (arguments.isOptPresent("--list")) {
            listInfo();
            System.exit(SUCCESS_EXIT_CODE);
        } else if (arguments.isOptPresent("--listruns")) {
            listRuns();
            System.exit(SUCCESS_EXIT_CODE);
        } else {
            
            // -i runid       -  run id for submission
            
            String runIdString = arguments.getOptValue("-i");
            try {
                if (arguments.isOptPresent("-i")){
                    runId = Long.parseLong(runIdString);
                }
            } catch (Exception e) {
                throw new CommandLineErrorException("Invalid number after -i '"+runIdString+"'", e);
            }

            // -j acro         - judgement for run, (judgement acronym)
            
            if (arguments.isOptPresent("-j")) {
                judgementAcronym = arguments.getOptValue("-j");
            }

            if (password == null) {
                password = login;
            }
        }
    }

    protected boolean hasAnyCCSArguments(String[] args, String[] requiredOpts) {

        ParseArguments parseArguments = new ParseArguments(args, requiredOpts);
        
        for (String s : args) {
            if (parseArguments.isRequiredOptPresent(s)) {
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
     * @param requiredOptions 
     * @return if any option is present, and any other is not present return count.
     */
    protected int numberMissingArguments(String[] args, String[] allOptions, String[] requiredOptions) {
        
        int count = 0;
        
        ParseArguments parseArguments = new ParseArguments(args, allOptions);
        
        for (String s : requiredOptions){
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
//
//    private String getLanguageFromFilename(String filename2) {
//
//        if (filename2.endsWith(".java")) {
//            return findLanguageName("Java");
//        } else if (filename2.endsWith(".cpp")) {
//            return findLanguageName("C++");
//        } else if (filename2.endsWith(".c")) {
//            return findLanguageName("C");
//        } else {
//            return languageTitle;
//        }
//    }

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
    
    private static void usage() {
        String[] usageMessage = { //
                "", //
                "Usage SubmitJudgement [-F propfile] -i runid -u loginname -w password -j judgement_acronym", //
                "Usage SubmitJudgement [--help|--list|--listruns|--check] options", //
                "", //
                "Submit judgement acronym for run.  ", //
                "", //
                "--help         - this listing", //
                "", //
                "-u loginname   - user login ", //
                "", //
                "-w password    - user password", //
                "", //
                "-i runid       - run id for run to be updated ", //
                "", //
                "-j acro         - judgement for run, (judgement acronym)", //
                "", //
                "--listruns     - list run info for the user", //
                "", //
                "filelist       - list of files including main file", //
                "", //
                "On success exit code will be " + SUCCESS_EXIT_CODE, //
                "Any other exit code is an error.", //
                "", //
        };

        for (String s : usageMessage) {
            System.out.println(s);
        }
    }
    
    private IJudgement findJudgement(IContest contest, String ja) {
        IJudgement[] judgements = contest.getJudgements();
        for (IJudgement iJudgement : judgements) {
            if (iJudgement.getAcronym().equals(ja)) {
                return iJudgement;
            }
        }
        return null;
    }

    private IRun findRun(IContest contest, long runId) {
        IRun[] runs = contest.getRuns();
        for (IRun iRun : runs) {
            if (iRun.getNumber() == runId)
            {
                return iRun;
            }
        }

        return null;
    }

    /**
     * Submit a run.
     * @param args 
     * @throws CommandLineErrorException 
     */
    public void submitJudgement(String[] args) throws CommandLineErrorException {

        boolean success = false;

        try {
            
            

            checkRequiredParams();

            serverConnection = new ServerConnection();

            contest = serverConnection.login(login, password);
            contest.addRunListener(runliEventListener);
            
            System.out.println("For: " + contest.getMyClient().getDisplayName() + " (" + contest.getMyClient().getLoginName() + ")");
            System.out.println();

            try {
                
                IRun run = findRun(contest, runId);
                if (run == null) {
                    throw new Exception("No run " + runId + " exists in contest.   No such run.");
                }

                IJudgement judgement = findJudgement(contest, judgementAcronym);
                
                if (judgement == null) {
                    throw new Exception("No judgement acronym found in contest for '" + judgementAcronym + "'");
                }
                
                serverConnection.submitRunJudgement(run, judgement);
                
                waitForRunJudgementConfirmation(runliEventListener, 2);

                run = runliEventListener.getRun();

                if (runliEventListener.getRun() != null) {
                    // got a run
                    success = true;
                    
                    // TODO revise confirmation message

                    System.out.println("Submission confirmation: Run " + run.getNumber() + ", problem " + run.getProblem().getName() + //
                            ", for team " + run.getTeam().getDisplayName() + //
                            " (" + run.getTeam().getLoginName() + ")");
                    if (debugMode){
                        System.out.println("Run "+run.getNumber()+" submitted at "+run.getSubmissionTime()+" minutes");
                    }
                } 
                // no else
                
                serverConnection.logoff();

            } catch (Exception e) {
                System.err.println("Unable to change run judgement: " + e.getMessage());
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
            System.exit(SUCCESS_EXIT_CODE);
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
        
        if (runId == 0) {
            throw new LoginFailureException("No run id specified");
        }
        
        if (judgementAcronym == null || judgementAcronym.length() == 0) {
            throw new LoginFailureException("No judgement acronym specified");
        }
    }

    /**
     * Waits for run judgement confirmation.
     * 
     * @param listener
     * @param seconds seconds to wait for response
     * @throws Exception
     */
    private void waitForRunJudgementConfirmation(RunEventListener listener, int seconds) throws Exception {

        boolean done = false;

        long waittime = seconds * 1000;

        long startTime = new Date().getTime();

        long timeLimit = startTime + waittime;

        while (!done) {

            if (listener.getRun() != null) {
                done = true;
            }

            if (! done && (new Date().getTime() > timeLimit)) {
                break;
            }
            System.out.print("");
        }

        long totalTime = new Date().getTime() - startTime;

        if (debugMode){
            System.out.println(totalTime + " ms");
            System.out.println();
        }

        if (!done) {
            throw new Exception("Timed out ("+totalTime+" ms) waiting for run submission confirm - contact staff ");
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
            if (problem.getShortName().equalsIgnoreCase(problemTitle2)) {
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

    public ILanguage getSubmittedLanguage() {
        return submittedLanguage;
    }

    public IClient getSubmittingUser() {
        return submittingUser;
    }
    
    /**
     * Listen for run events.
     * 
     * @author pc2@ecs.csus.edu
     */
    protected class RunEventListener implements IRunEventListener, Runnable {

        private IRun submittedRun = null;

        public void runSubmitted(IRun run) {
            // ignore
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
            // TODO check/handle if judgement done for run

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
        
        if (args.length == 0 || args[0].equals("--help")) {
            usage();;
            System.exit(4);
        }
        
        try {
            SubmitJudgment submitter = new SubmitJudgment(args);
            
            submitter.submitJudgement(args);
            
        } catch (CommandLineErrorException e) {
            System.err.println("Error on command line: "+e.getMessage());
        } catch (Exception e){
            System.err.println("Error submitting run "+e.getMessage());
            e.printStackTrace(System.err);
        }
        
    }
    

    /**
     * Return all optional and required CCS options.
     * 
     * @return list of -t, -i, -w, etc.
     */
    public String[] getAllCCSOptions() {
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(CCS_REQUIRED_OPTIONS_LIST));
        list.add("-t");
        list.add("-i");
        list.add("-j");
        list.add("-F");
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
    
    /**
     * Fatal error - log error and show user message before exiting.
     * 
     * @param message
     * @param ex
     */
    protected void fatalError(String message, Exception ex) {

        if (ex != null) {
            ex.printStackTrace(System.err);
        }
        System.err.println(message);

        System.exit(4);

    }

    /**
     * 
     * @see #fatalError(String, Exception)
     * @param message
     */
    protected void fatalError(String message) {
        fatalError(message, null);
    }

}

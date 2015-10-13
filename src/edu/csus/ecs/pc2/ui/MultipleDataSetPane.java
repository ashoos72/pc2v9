package edu.csus.ecs.pc2.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.model.ElementId;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.ProblemDataFiles;
import edu.csus.ecs.pc2.core.model.SerializedFile;

/**
 * Multiple Test Data set UI.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// TODO 917 make font larger

// $HeadURL$
public class MultipleDataSetPane extends JPanePlugin {

    /**
     * 
     */
    private static final long serialVersionUID = -5975163495479418935L;

    private JPanel centerPane = null;

    private TestCaseTableModel tableModel = new TestCaseTableModel();

    private JTable testDataSetsListBox = null;

    private ProblemDataFiles problemDataFiles;
    private JButton btnDelete;
    private JButton btnReload;
    private JButton btnLoad;

    private EditProblemPane editProblemPane = null;

    /**
     * This method initializes
     * 
     */
    public MultipleDataSetPane() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(new Dimension(440, 229));
        this.add(getCenterPane(), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
        flowLayout.setHgap(45);
        buttonPanel.setPreferredSize(new Dimension(35, 35));
        add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.add(getBtnLoad());
        buttonPanel.add(getBtnDelete());
        buttonPanel.add(getBtnReload());
    }

    @Override
    public String getPluginTitle() {
        return "Mulitple Data Set Pane";
    }

    /**
     * Clone data files and populate in pane.
     * 
     * @param problemDataFiles
     * @throws CloneNotSupportedException
     */
    public void setProblemDataFiles(Problem problem, ProblemDataFiles problemDataFiles) throws CloneNotSupportedException {
        if (problem != null){
            setProblemDataFiles(problemDataFiles.copy(problem));
        } else {
            clearDataFiles();
        }
    }

    public void setProblemDataFiles(ProblemDataFiles datafiles) {
        this.problemDataFiles = datafiles;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                populateUI();
            }
        });
    }
    
    protected void populateUI() {

        tableModel.setFiles(problemDataFiles);
        tableModel.fireTableDataChanged();
     
        // TODO 917 remove debugging
        if (EditProblemPane.debug22EditProblem)
        {
            System.out.println("debug 22 fire data changed ");
            dump(problemDataFiles, "populateUI debug 22 ");
            dump(getProblemDataFiles(), "populateUI debug 22 B");
        }

        // TODO 917 re-add auto size columns
//        testDataSetsListBox.autoSizeAllColumns();
    }


    private void dump(ProblemDataFiles problemDataFiles2, String string) {
        Utilities.dump(problemDataFiles2, string);
    }

    /**
     * This method initializes centerPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getCenterPane() {
        if (centerPane == null) {
            centerPane = new JPanel();
            centerPane.setLayout(new BorderLayout());
            centerPane.add(getTestDataSetsListBox(), BorderLayout.CENTER);
        }
        return centerPane;
    }


    /**
     * This method initializes testDataSetsListBox
     * 
     * @return edu.csus.ecs.pc2.ui.MCLB
     */
    private JTable getTestDataSetsListBox() {
        if (testDataSetsListBox == null) {
            testDataSetsListBox = new JTable(tableModel);

        }
        return testDataSetsListBox;
    }
    
    public ProblemDataFiles getProblemDataFiles () {
        dump(tableModel.getFiles(), "populateUI debug 22 C");
        return tableModel.getFiles();
    }

    public void clearDataFiles() {
        this.problemDataFiles = null;
        
        tableModel.setFiles(null);
        tableModel.fireTableDataChanged();
    }
    
    /**
     * Compares current set of data sets to input datafiles.
     * 
     */
    boolean hasChanged(ProblemDataFiles originalFiles) {

        if (originalFiles == null && problemDataFiles == null) {
            return true;
        }

        if (originalFiles == null || problemDataFiles == null) {
            return false;
        }

        int comp = compare(originalFiles.getJudgesDataFiles(), problemDataFiles.getJudgesDataFiles());
        if (comp != 0) {
            return false;
        }

        comp = compare(originalFiles.getJudgesAnswerFiles(), problemDataFiles.getJudgesAnswerFiles());
        if (comp != 0) {
            return false;
        }
        
        System.out.println("debug 22 Are problemId's identical ?" + //
                problemDataFiles.getProblemId().equals(originalFiles.getProblemId()));

        return true;

    }

    /**
     * Compare serializedfile arrays.
     * 
     * @param listOne
     * @param listTwo
     * @return 0 if identical, non-zero if different.
     */
    private int compare(SerializedFile[] listOne, SerializedFile[] listTwo) {

        if (listOne.length != listTwo.length) {
            return listTwo.length - listOne.length;
        } else {
            for (int i = 0; i < listTwo.length; i++) {
                if (!listOne[i].equals(listTwo[i])) {
                    return 1;
                }
            }

        }
        return 0;
    }

    
    private JButton getBtnDelete() {
        if (btnDelete == null) {
            btnDelete = new JButton("Delete");
            btnDelete.setToolTipText("Delete selected data sets");
            btnDelete.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int rowNumber = testDataSetsListBox.getSelectedRow();
                    if (rowNumber != -1){
                        removeRow (rowNumber);
                    }
                }
            });
        }
        return btnDelete;
    }

    protected void removeRow(int rowNumber) {

        if (tableModel.getRowCount() == 1)
        {
            editProblemPane.setJudgingTestSetOne(tableModel.getFiles());
        }

        tableModel.removeRow(rowNumber);
        
        // TODO 917 if row one is deleted, update the data and answer file on the General Tab 
        // Warn if they delete row one ??
    }

    private JButton getBtnReload() {
        if (btnReload == null) {
            btnReload = new JButton("Re-Load");
            btnReload.setToolTipText("Refresh/Reload data sets from disk");
            btnReload.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    reloadDataFiles();
                }
            });
        }
        return btnReload;
    }

    protected void reloadDataFiles() {
        
        boolean externalFiles = true;
        
        String baseDirectoryName = ".";
        
        // TODO 917 let them pick the directory ?
        
        /**
         * short name or base directory
         */
        String shortProblemName = "unset";
        
        String problemFilesDirectory = shortProblemName;
        
        Problem problem = null;
        
        // loop through
        
        if (problemDataFiles != null){
            
            SerializedFile file = problemDataFiles.getJudgesDataFile();
            if (file != null){
                baseDirectoryName = Utilities.findDataBasePath (file.getFile().getParent());
            }
            
            // find problem
            ElementId problemId = problemDataFiles.getProblemId();
            problem = getContest().getProblem(problemId);
            
            if (problem != null){
                externalFiles = problem.isUsingExternalDataFiles();
                problemFilesDirectory = Utilities.getSecretDataPath(baseDirectoryName, problem);
            }
        }
        
        // check for answer files
        String secretDirPath = Utilities.getSecretDataPath(problemFilesDirectory, shortProblemName);
        String[] inputFileNames = Utilities.getFileNames(secretDirPath, ".ans");
        
        if (inputFileNames.length == 0){
            System.out.println("debug 22 "+"No .ans files found in "+secretDirPath);
            showMessage(this, "No answer files found", "No .ans files found in "+secretDirPath);
            return;
        }
        
        loadDataFiles(problem, problemDataFiles, secretDirPath, ".in", ".ans", externalFiles);
        
        // TODO 917 Populate general data and answer files too
        
        // TODO 917 trigger refresh of Update button on edit problem frame 
        
        populateUI();
    }

    private JButton getBtnLoad() {
        if (btnLoad == null) {
            btnLoad = new JButton("Load");
            btnLoad.setToolTipText("Load data sets from directory");

            btnLoad.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loadDataFiles();
                }
            });
        }
        return btnLoad;
    }

    protected void loadDataFiles() {
        showMessage(this, "Not implemented Yet", "loadDataFiles not implemented, yet");
        // TODO Auto-generated method stub
        
        // TODO load the data and answer file on the General Tab
        
        editProblemPane.setJudgingTestSetOne(tableModel.getFiles());

    }

    public void setParentPane(EditProblemPane pane) {
        editProblemPane = pane;
    }
    
    
    public ProblemDataFiles loadDataFiles(Problem problem, ProblemDataFiles files, String dataFileBaseDirectory, String dataExtension, String answerExtension, boolean externalDataFiles) {

        if (files == null) {
            files = new ProblemDataFiles(problem);
        } else {
            /**
             * A check. It makes no sense to update an existing ProblemDataFiles for a different Problem.
             */
            if (problem != null && !files.getProblemId().equals(problem.getElementId())) {
                throw new RuntimeException("problem and data files are not for the same problem " + problem.getElementId() + " vs " + files.getProblemId());
            }
        }

        String[] inputFileNames = Utilities.getFileNames(dataFileBaseDirectory, dataExtension);

        String[] answerFileNames = Utilities.getFileNames(dataFileBaseDirectory, answerExtension);

        if (inputFileNames.length == 0) {
            throw new RuntimeException("No input files with extension " + dataExtension + " in "+dataFileBaseDirectory);
        }

        if (answerFileNames.length == 0) {
            throw new RuntimeException("No answer  files with extension " + answerExtension+ " in "+dataFileBaseDirectory);
        }

        if (answerFileNames.length != inputFileNames.length) {
            throw new RuntimeException("Miss match expecting same " + dataExtension + " and " + answerExtension + " files (" + inputFileNames.length + " vs " + answerFileNames.length);
        }

        SerializedFile[] inputFiles = Utilities.createSerializedFiles(dataFileBaseDirectory, inputFileNames, externalDataFiles);
        SerializedFile[] answertFiles = Utilities.createSerializedFiles(dataFileBaseDirectory, answerFileNames, externalDataFiles);
        files.setJudgesDataFiles(inputFiles);
        files.setJudgesAnswerFiles(answertFiles);

        return files;
    }
    
} // @jve:decl-index=0:visual-constraint="10,10"

package edu.csus.ecs.pc2.core.scoring;

import java.util.Properties;

import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.Run;

/**
 * Calculate score methods.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 * @deprecated Use {@link INewScoringAlgorithm}
 */

public interface ICalculateScore {

    /**
     * Calculate scoring record per problem.
     * 
     * @param runs
     * @param problem
     * @param properties
     */
    ProblemScoreRecord createProblemScoreRecord(Run[] runs, Problem problem, Properties properties);

}

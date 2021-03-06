// Copyright (C) 1989-2019 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
package edu.csus.ecs.pc2.core.model;

import edu.csus.ecs.pc2.core.util.AbstractTestCase;

/**
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class JudgementTest extends AbstractTestCase {
    
    public void testAcronym() throws Exception {

        // bug 704
        Judgement judgement = new Judgement("Accepted");
        assertNotNull(judgement.getAcronym());
        
        judgement = new Judgement("Accepted", "AC");
        assertEquals("AC", judgement.getAcronym());
        
    }

}

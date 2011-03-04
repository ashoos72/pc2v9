package edu.csus.ecs.pc2.core.model;

import java.io.Serializable;

/**
 * Judgement Name/Title.
 * 
 * This contains the judgements, like Yes, No, Contact Staff.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class Judgement implements IElementObject, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8252588018410124478L;

    private ElementId elementId = null;

    /**
     * Title for the Judgement.
     */
    private String displayName = null;

    private boolean active = true;
    
    private String acronym;

    public Judgement(String displayName) {
        this(displayName, displayName.toUpperCase().substring(0,2));
    }
    
    public Judgement(String displayName, String acronym) {
        super();
        this.displayName = displayName;
        this.acronym = acronym;
        elementId = new ElementId(displayName);
    }


    /**
     * @return Returns the displayName.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return Returns the elementId.
     */
    public ElementId getElementId() {
        return elementId;
    }

    /**
     * @param displayName
     *            The displayName to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * return title/text for judgement.
     * 
     * Returns the phrases that the judge selects as a judgement.
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return displayName;
    }

    /**
     * @return Returns the active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active
     *            The active to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    public int versionNumber() {
        return elementId.getVersionNumber();
    }

    public int getSiteNumber() {
        return elementId.getSiteNumber();
    }

    public void setSiteNumber(int siteNumber) {
        elementId.setSiteNumber(siteNumber);
    }

    public boolean isSameAs(Judgement judgement) {
        try {
            if (!getDisplayName().equals(judgement.getDisplayName())) {
                return false;
            }
            if (!getAcronym().equals(judgement.getAcronym())) {
                return false;
            }
            if (isActive() != judgement.isActive()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            // TODO log to static Exception log
            return false;
        }
    }
    
    public String getAcronym() {
        return acronym;
    }
    
    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }
    
}

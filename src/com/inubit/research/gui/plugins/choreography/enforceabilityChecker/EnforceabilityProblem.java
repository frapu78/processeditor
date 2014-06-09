/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.enforceabilityChecker;

import java.util.Collection;
import java.util.HashSet;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

/**
 * Data-structure for carrying a problem, that was detected during
 * Enforceability-Check. This structure is able to carry a problem type (Error or
 * Warning), a description and ProcessObjects, that are involved in the problem.
 *
 * @author tmi
 */
public class EnforceabilityProblem {

  /**
   * Describes the type of problem
   */
  public static enum ProblemType {
    /**
     * a problem, that strongly inhibits enforceability and therefore generating
     * behavioral interfaces.
     */
    Error,
    /**
     * a problem, that requires generating timeouts in order to enforce the
     * choreography
     */
    TimeoutWarning;
  }

  /**
   * the problem-description
   */
  private String description;

  /**
   * the main-object related with the problem. This is the ProcessObject that is
   * selected to be most significant for this problem.
   */
  private ProcessObject mainObject;

  /**
   * a collection of ProcessObjects related with the problem. This collection
   * must not include the mainObject. It may be empty, but not null
   */
  private Collection<ProcessObject> relatedObjects;

  /**
   * the type of problem
   * @see ProblemType
   */
  private ProblemType problemType;


  public EnforceabilityProblem(
          String description, ProcessObject mainObject, ProblemType problemType) {
    this.description = description;
    this.mainObject = mainObject;
    relatedObjects = new HashSet<ProcessObject>();
    this.problemType = problemType;
  }

  public EnforceabilityProblem(
          String description, ProcessObject mainObject) {
    this(description, mainObject, ProblemType.Error);
  }

  /**
   * creates a new instance of EnforceabilityProblem with all possible parameters
   * @param description the problem-description
   * @param mainObject the main-object related with the problem. This is the
   * ProcessObject that is selected to be most significant for this problem.
   * @param relatedObjects a collection of ProcessObjects related with the
   * problem. This collection must not include the mainObject.
   * It may be empty, but not null.
   * @param problemType the type of problem (Error or specific Warning)
   */
  public EnforceabilityProblem(String description, ProcessObject mainObject,
          Collection<ProcessObject> relatedObjects, ProblemType problemType) {
    this.description = description;
    this.mainObject = mainObject;
    this.relatedObjects = relatedObjects;
    this.problemType = problemType;
  }

  public EnforceabilityProblem(String description, ProcessObject mainObject,
          Collection<ProcessObject> relatedObjects) {
    this(description, mainObject, relatedObjects, ProblemType.Error);
  }

  public EnforceabilityProblem(String description, ProcessNode mainObject,
          Collection<ProcessNode> relatedObjects, ProblemType problemType) {
    this.description = description;
    this.mainObject = mainObject;
    this.relatedObjects = new HashSet<ProcessObject>();
    for(ProcessNode node : relatedObjects) {
      this.relatedObjects.add(node);
    }
    this.problemType = problemType;
  }

  public EnforceabilityProblem(String description, ProcessNode mainObject,
          Collection<ProcessNode> relatedObjects) {
    this(description, mainObject, relatedObjects, ProblemType.Error);
  }

  public EnforceabilityProblem(String description, ProcessObject mainObject,
          ProcessObject relatedObject, ProblemType problemType) {
    this.description = description;
    this.mainObject = mainObject;
    relatedObjects = new HashSet<ProcessObject>();
    relatedObjects.add(relatedObject);
    this.problemType = problemType;
  }

  public EnforceabilityProblem(String description, ProcessObject mainObject,
          ProcessObject relatedObject) {
    this(description, mainObject, relatedObject, ProblemType.Error);
  }

  //sorry for tourning around the parameter order - type erasure makes this necessary
  public EnforceabilityProblem(ProcessObject mainObject,
          Collection<ProcessEdge> relatedObjects, 
          String description,
          ProblemType problemType) {
    this.description = description;
    this.mainObject = mainObject;
    this.relatedObjects = new HashSet<ProcessObject>();
    for(ProcessEdge edge : relatedObjects) {
      this.relatedObjects.add(edge);
    }
    this.problemType = problemType;
  }

  public EnforceabilityProblem(ProcessObject mainObject,
          Collection<ProcessEdge> relatedObjects,
          String description) {
    this(mainObject, relatedObjects, description, ProblemType.Error);
  }

  /**
   * get this Problem as a one-element Collection of Problems
   * @return a new Collection containing just this EnforceabilityProblem
   */
  public Collection<EnforceabilityProblem> inCollection() {
    Collection<EnforceabilityProblem> collection =
            new HashSet<EnforceabilityProblem>();
    collection.add(this);
    return collection;
  }

  /**
   * @return the problem-description (a String to be displayed to the user)
   */
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ProcessObject getMainObject() {
    return mainObject;
  }

  public void setMainObject(ProcessObject mainObject) {
    this.mainObject = mainObject;
  }

  public Collection<ProcessObject> getRelatedObjects() {
    return relatedObjects;
  }

  public void addRelatedObject(ProcessObject object) {
    relatedObjects.add(object);
  }

  public ProblemType getProblemType() {
    return problemType;
  }

  public boolean isError() {
    return problemType.equals(ProblemType.Error);
  }

  public boolean isWarning() {
    return ! isError();
  }

  public void setProblemType(ProblemType problemType) {
    this.problemType = problemType;
  }
}

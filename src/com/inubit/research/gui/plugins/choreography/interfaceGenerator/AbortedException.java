/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.interfaceGenerator;

/**
 * Thrown when generating a behavioral interface was aborted due to some reason.
 * This exception is used internally only
 * @author tmi
 */
class AbortedException extends Exception {
  public AbortedException(String message) {
    super(message);
  }

}

package net.frapu.code.visualization;

import net.frapu.code.visualization.editors.ReferenceChooserRestriction;

/**
 * Interface which must be implemented by objects which may be linked with other objects.
 * 
 * @author tg
 *
 */
public interface Linkable {

    /**
     * @return a {@link ReferenceChooserRestriction}.
     */
    public ReferenceChooserRestriction getReferenceRestrictions();
}

/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.manager;

import com.inubit.research.server.user.SingleUser;

/**
 *
 * @author fel
 */
public interface UserHomeable {
    public String getUserHome( SingleUser user );
    public void createUserHome ( SingleUser user );
}

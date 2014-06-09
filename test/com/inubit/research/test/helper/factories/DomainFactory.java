/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inubit.research.test.helper.factories;

import com.inubit.research.server.domainStorage.db4o.DB4oDomain;
import com.inubit.research.server.domainStorage.data.Domain;
import com.inubit.research.server.domainStorage.data.Storage;

/**
 *
 * @author Uwe
 */
public class DomainFactory {

    public static Domain build(Storage store) {
        Domain domain = store.createDomain("AppleAppStore", DomainModelFactory.build());
        return domain;
    }
}

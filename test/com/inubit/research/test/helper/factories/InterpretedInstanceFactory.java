/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inubit.research.test.helper.factories;

import com.inubit.research.server.domainStorage.data.Domain;
import com.inubit.research.server.domainStorage.data.InterpretedInstance;
import com.inubit.research.server.domainStorage.data.Storage;
import java.util.Iterator;

/**
 *
 * @author Uwe
 */
public class InterpretedInstanceFactory {

    public static InterpretedInstance build(Storage store) {
        Domain domain = DomainFactory.build(store);
        Integer classID = Integer.valueOf(domain.getModel().getNodes().get(0).getId());
        InterpretedInstance instance = domain.getClassData(classID).createNewInstance();
        Iterator<Integer> it = domain.getClassData(classID).getInstanceIndexes().iterator();
        Integer instanceID = it.next();
        InterpretedInstance classInstance = domain.getClassData(classID).getInstanceByIndex(instanceID);
        return classInstance;
    }
}

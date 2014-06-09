/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.test.helper.factories;

import net.frapu.code.visualization.domainModel.DomainModel;

/**
 *
 * @author Uwe
 */
public class DomainModelFactory {
    
    public static DomainModel build() {
        DomainModel domainModel = new DomainModel("AppStore");
        domainModel.addObject(DomainClassFactory.build()); 
        domainModel.addObject(DomainClassFactory.build()); 
        return domainModel;
    }

}

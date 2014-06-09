/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inubit.research.test.helper.factories;

import net.frapu.code.visualization.domainModel.DomainClass;

/**
 *
 * @author Uwe
 */
public class DomainClassFactory {
    
    private static int counter = 0;

    public static DomainClass build() {
        DomainClass domainClass = new DomainClass("App" + counter);
        domainClass.addAttribute("Price" + counter);
        domainClass.addAttribute("Name" + counter);
        counter++;
        return domainClass;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inubit.research.test.helper.factories;

import com.inubit.research.server.domainStorage.data.Storage;
import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.manager.ModelManager;
import com.inubit.research.server.user.SingleUser;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.converter.ProcessEditorExporter;
import net.frapu.code.converter.ProcessEditorImporter;
import net.frapu.code.visualization.domainModel.DomainClass;
import net.frapu.code.visualization.domainModel.DomainModel;

/**
 *
 * @author Uwe
 */
public class SolutionCenterRequestHandlerFixture {

    public static void build() {
        try {
            //create some example data
            DomainModel domainModel = (DomainModel) ProcessEditorImporter.getInstance().load("models\\Merger-Tests\\edgeDocker1.model"/*"models" + File.separator + "SC-Backend-Tests" + File.separator + "AppStoreDomainModel.model"*/);
            DomainClass appClass = new DomainClass("App");
            appClass.addAttribute("Price");
            appClass.addAttribute("Name");
            appClass.addAttribute("Active");
            domainModel.addObject(appClass);
            ProcessEditorExporter exporter = new ProcessEditorExporter();
            File f = new File(ProcessEditorServerHelper.TMP_DIR + "/" + domainModel.getId() + ".model");
            exporter.serialize(f, domainModel);
            String key = ModelManager.getInstance().addPersistentModel(f, "nothing", "/", new SingleUser("domainModels", "password"));
            f.delete();
            assert ModelManager.getInstance().getPersistentModel(domainModel.getId(), -1) != null;
            appClass.addAttribute("Rank");
            appClass.addAttribute("Tags");
            appClass.addAttribute("Currency");
            HashSet<String> prevVersions = new HashSet<String>();
            prevVersions.add("0");
            ModelManager.getInstance().saveModel(domainModel, domainModel.getId(), 0, "comment", "/", prevVersions, new SingleUser("domainModels", "password"));
            LinkedList<String> tags = new LinkedList<String>();
            tags.add("Game");
            tags.add("Birds");
            tags.add("Rovio");
            Storage.getCurrent().createDomain("AppleAppStore", domainModel)
                    .getClassDataByName(appClass.getId())
                    .createNewInstance()
                    .setAttribute("Name", "Angry Birds")
                    .setAttribute("Price", 0.99)
                    .setAttribute("Rank", 4)
                    .setAttribute("Tags", tags)
                    .setAttribute("Active", true)
                    .setAttribute("Currency", "$");
            Storage.getCurrent().getDomain("AppleAppStore")
                    .getClassDataByName(appClass.getId())
                    .createNewInstance()
                    .setAttribute("Name", "Twitter")
                    .setAttribute("Price", 0.00)
                    .setAttribute("Rank", 12)
                    .setAttribute("Active", true)
                    .setAttribute("Currency", "$");
            Storage.getCurrent().createDomain("AndroidMarket", domainModel)
                    .getClassDataByName(appClass.getId())
                    .createNewInstance()
                    .setAttribute("Name", "Task Manager")
                    .setAttribute("Price", 1.99)
                    .setAttribute("Rank", 2)
                    .setAttribute("Active", false)
                    .setAttribute("Currency", "€");
        } catch (Exception ex) {
            Logger.getLogger(SolutionCenterRequestHandlerFixture.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

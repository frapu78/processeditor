/**
 *
 * Process Editor - UML Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.uml;

import net.frapu.code.visualization.DefaultRoutingPointLayouter;
import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.gridLayouter.GridLayouter;
import com.inubit.research.layouter.sugiyama.SugiyamaLayoutAlgorithm;

import java.net.URI;
import net.frapu.code.converter.XSDImporter;
import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.RoutingPointLayouter;

/**
 * @author ff
 *
 */
public class UMLUtils extends ProcessUtils {
    
    private ArrayList<ProcessLayouter> layouters = null;
    private DefaultRoutingPointLayouter rpLayouter = new DefaultRoutingPointLayouter();

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {

        System.out.println("Creating edge: "+source+"->"+target);

        // UMLClass --> EdgeDocker
        if (source instanceof UMLClass && target instanceof EdgeDocker) {
            
            Association a = new Association(source, target);
            a.setProperty(Association.PROP_DIRECTION, Association.DIRECTION_NONE);
            return a;
        }

        // Return null of no UMLClass
        if (!(source instanceof UMLClass || source instanceof UMLClassReference) ||
                !(target instanceof UMLClass || target instanceof UMLClassReference)) return null;

        Association a = new Association(source, target);
        a.setProperty(Association.PROP_DIRECTION, Association.DIRECTION_NONE);
        return a;
    }

    @Override
    public List<ProcessLayouter> getLayouters() {
        if (layouters == null) {
            layouters = new ArrayList<ProcessLayouter>();
            layouters.add(new SugiyamaLayoutAlgorithm(Configuration.getProperties()));
            layouters.add(new SugiyamaLayoutAlgorithm(false,Configuration.getProperties()));
            layouters.add(new GridLayouter(Configuration.getProperties()));
            //layouters.add(new UMLlayouter());            
        }
        return layouters;
    }

        /**
     * Fetches a Schema from an Uri, that either represents an XML Schema
     * or a ClassDiagram.
     * @param uriString
     * @return
     */
    public static ProcessModel fetchSchemaModel(String uriString) throws Exception {
            URI uri = new URI(uriString);
            ProcessModel model = null;
            try {
                // Try to import as XSD
                 model = new XSDImporter().parseSource(uri).get(0);
            } catch (Exception ex) {
                // Try to import as ProcessModel
                model = ProcessUtils.parseProcessModelSerialization(uri);
                if (!(model instanceof ClassModel)) return null;
            }
            return model;
    }

    @Override
    public RoutingPointLayouter getRoutingPointLayouter() {
       return rpLayouter;
    }

    @Override
    public List<Class<? extends ProcessNode>> getNextNodesRecommendation(ProcessModel model, ProcessNode node) {
        List<Class<? extends ProcessNode>> rec =  super.getNextNodesRecommendation(model, node);

        if ( node instanceof UMLClass ) {
            rec.add( UMLClass.class );
        }

        return rec;
    }

    public static List<String> getAttributeTypes() {
        List<String> result = new ArrayList<String>();
        result.add(UMLAttribute.TYPE_BOOLEAN);
        result.add(UMLAttribute.TYPE_DATE);
        result.add(UMLAttribute.TYPE_DATETIME);
        result.add(UMLAttribute.TYPE_DOUBLE);
        result.add(UMLAttribute.TYPE_ENUM);
        result.add(UMLAttribute.TYPE_FLOAT);
        result.add(UMLAttribute.TYPE_INT);
        result.add(UMLAttribute.TYPE_LONG);
        result.add(UMLAttribute.TYPE_MLTEXT);
        result.add(UMLAttribute.TYPE_TEXT);
        
        return result;
    }
}

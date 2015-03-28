/**
 *
 * Process Editor - Domain Package
 *
 * (C) 2010 inubit AG
 * (C) 2014 the authors
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.domainModel;

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

import java.util.LinkedList;
import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.RoutingPointLayouter;

/**
 * @author ff
 *
 */
public class DomainUtils extends ProcessUtils {

    private ArrayList<ProcessLayouter> layouters = null;
    private DefaultRoutingPointLayouter rpLayouter = new DefaultRoutingPointLayouter();

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {

        System.out.println("Creating edge: " + source + "->" + target);

        // DomainClass --> EdgeDocker
        if (source instanceof DomainClass && target instanceof EdgeDocker) {

            Association a = new Association(source, target);
            a.setProperty(Association.PROP_DIRECTION, Association.DIRECTION_NONE);
            return a;
        }

        // Return null of no DomainClass
        if (!(source instanceof DomainClass || source instanceof DomainClassReference)
                || !(target instanceof DomainClass || target instanceof DomainClassReference)) {
            return null;
        }

        Aggregation a = new Aggregation(source, target);
        return a;
    }

    @Override
    public List<ProcessLayouter> getLayouters() {
        if (layouters == null) {
            layouters = new ArrayList<ProcessLayouter>();
            layouters.add(new SugiyamaLayoutAlgorithm(Configuration.getProperties()));
            layouters.add(new SugiyamaLayoutAlgorithm(false, Configuration.getProperties()));
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
//    public static ProcessModel fetchSchemaModel(String uriString) throws Exception {
//            URI uri = new URI(uriString);
//            ProcessModel model = null;
//            try {
//                // Try to import as XSD
//                 model = new XSDImporter().parseSource(uri).get(0);
//            } catch (Exception ex) {
//                // Try to import as ProcessModel
//                model = ProcessUtils.parseProcessModelSerialization(uri);
//                if (!(model instanceof ClassModel)) return null;
//            }
//            return model;
//    }
    @Override
    public RoutingPointLayouter getRoutingPointLayouter() {
        return rpLayouter;
    }

    @Override
    public List<Class<? extends ProcessNode>> getNextNodesRecommendation(ProcessModel model, ProcessNode node) {
        List<Class<? extends ProcessNode>> rec = super.getNextNodesRecommendation(model, node);


        if (node instanceof DomainClass) {
            rec.add(DomainClass.class);
        }

        return rec;
    }

    /**
     * Returns a list of all outgoing Aggregations, incl. those from super classes
     * @param dc
     * @deprecated 
     * @return
     */
    public static List<Aggregation> getAggregations(DomainClass dc, DomainModel model) {
        List<Aggregation> result = new LinkedList<Aggregation>();
        List<DomainClass> superClasses = getParents(dc, model);
        for (ProcessEdge edge : model.getEdges()) {
            if (edge instanceof Aggregation) {
                // Check if source is one of superClasses
                if (superClasses.contains(edge.getSource())) {
                    result.add((Aggregation) edge);
                }
            }
        }
        return result;
    }

    /**
     * Returns a list of all outgoing Associations, incl. those from super classes.
     *
     * The list contains all Associations attached to one of dc's classes that
     * have either DIRECTION_NONE, DIRECTION_BOTH, OR DIRECTION_TARGET as DIRECTION.
     *
     * DIRECTION.SOURCE is not included, since it only points to dc.
     *
     * @param dc
     * @deprecated 
     * @return
     */
    public static List<Association> getAssociations(DomainClass dc, DomainModel model) {
        List<Association> result = new LinkedList<Association>();
        List<DomainClass> superClasses = getParents(dc, model);
        for (ProcessEdge edge : model.getEdges()) {
            if (edge instanceof Association) {
                Association assoc = (Association) edge;
                // Undirected: Check if source is one of superClasses
                if (assoc.getProperty(Association.PROP_DIRECTION).equalsIgnoreCase(Association.DIRECTION_BOTH) |
                        assoc.getProperty(Association.PROP_DIRECTION).equalsIgnoreCase(Association.DIRECTION_NONE)) {
                    if (superClasses.contains(edge.getSource()) | superClasses.contains(edge.getTarget())) {
                        result.add(assoc);
                        continue;
                    }
                } else if (assoc.getProperty(Association.PROP_DIRECTION).equalsIgnoreCase(Association.DIRECTION_TARGET)) {
                    if (superClasses.contains(edge.getSource())) {
                        result.add(assoc);
                        continue;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns a list of all parent classes, incl. the recent one.
     * @param dc
     * @param model
     * @deprecated 
     * @return
     */
    public static List<DomainClass> getParents(DomainClass dc, DomainModel model) {
        return getParents(dc, model, new LinkedList<DomainClass>());
    }
    /**
     * 
     * @param dc
     * @param model
     * @param visited
     * @deprecated
     * @return 
     */
     
    private static List<DomainClass> getParents(DomainClass dc, DomainModel model, List<DomainClass> visited) {
        List<DomainClass> result = new LinkedList<DomainClass>();
        if (visited.contains(dc)) {
            return result;
        }
        visited.add(dc);
        // Iterate over all parents
        for (ProcessEdge edge : model.getEdges()) {
            if ((edge.getTarget() instanceof DomainClass)
                    && (edge instanceof Inheritance)
                    && (edge.getSource() == dc)) {
                DomainClass parent = (DomainClass) edge.getTarget();
                result.addAll(getParents(parent, model, visited));
            }
        }
        result.add(dc);
        return result;
    }

    /**
     * Returns a list of all child classes, incl. the recent one.
     * @param dc
     * @param model
     * @deprecated
     * @return
     */
    public static List<DomainClass> getChildren(DomainClass dc, DomainModel model) {
        return getChildren(dc, model, new LinkedList<DomainClass>());
    }
    
    /**
     * 
     * @param dc
     * @param model
     * @param visited
     * @deprecated
     * @return 
     */
    private static List<DomainClass> getChildren(DomainClass dc, DomainModel model, List<DomainClass> visited) {
        List<DomainClass> result = new LinkedList<DomainClass>();
        if (visited.contains(dc)) {
            return result;
        }
        visited.add(dc);
        result.add(dc);
        // Iterate over all childs
        for (ProcessEdge edge : model.getEdges()) {
            if ((edge.getSource() instanceof DomainClass)
                    && (edge instanceof Inheritance)
                    && (edge.getTarget() == dc)) {
                DomainClass child = (DomainClass) edge.getSource();
                result.addAll(getChildren(child, model, visited));
            }
        }
        return result;
    }
    
    public static List<String> getAttributeTypes() {
        List<String> result = new ArrayList<String>();
        result.add(Attribute.TYPE_BOOLEAN);
        result.add(Attribute.TYPE_DATE);
        result.add(Attribute.TYPE_DATETIME);
        result.add(Attribute.TYPE_DOUBLE);
        result.add(Attribute.TYPE_ENUM);
        result.add(Attribute.TYPE_FLOAT);
        result.add(Attribute.TYPE_INT);
        result.add(Attribute.TYPE_LONG);
        result.add(Attribute.TYPE_MLTEXT);
        result.add(Attribute.TYPE_TEXT);
        
        return result;
    }
}

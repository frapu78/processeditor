/*
 * Process Editor
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.converter;

import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.request.XMLHelper;
import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.domainModel.Association;
import net.frapu.code.visualization.domainModel.Aggregation;
import net.frapu.code.visualization.domainModel.Attribute;
import net.frapu.code.visualization.domainModel.DomainClass;
import net.frapu.code.visualization.domainModel.DomainClassReference;
import net.frapu.code.visualization.domainModel.DomainModel;
import net.frapu.code.visualization.domainModel.Inheritance;
import net.frapu.code.visualization.helper.ReferenceHelper;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author fel
 */
public class XSDCreator implements Exporter {
    public static final String ATTRIBUTE_FIXED = "fixed";
    public static final String ATTRIBUTE_MAX_OCCURS = "maxOccurs";
    public static final String ATTRIBUTE_MIN_OCCURS = "minOccurs";
    public static final String ATTRIBUTE_TYPE = "type";
    public static final String ATTRIBUTE_USE = "use";
    public static final String ATTRIBUTE_ABSTRACT = "abstract";
    public static final String STEREOTYPE_AGGREGATION = "aggregation";
    public static final String TYPE_CHOICE = "xs:choice";
    public static final String TYPE_COMPLEX_CONTENT = "xs:complexContent";
    public static final String TYPE_COMPLEX_TYPE = "xs:complexType";
    public static final String TYPE_ATTRIBUTE = "xs:attribute";
    public static final String TYPE_ELEMENT = "xs:element";
    public static final String TYPE_EXTENSION = "xs:extension";
    public static final String TYPE_SEQUENCE = "xs:sequence";
    public static final String CLASS_ATTRIBUTE_TITLE = "title";
    public static final String CLASS_ATTRIBUTE_DESCRIPTION = "description";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ML_TEXT_TYPE_NAME = "mltext";
    private static final String SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
    private static final Set<String> xmlTypes;

    static {
        String[] types = {"int", "string", "date", "float", "boolean", "dateTime" };
        xmlTypes = new HashSet<String>( Arrays.asList(types) );
    }

    public class Hierarchy {
        private Map<ProcessNode, HierarchyNode> nodes = new HashMap<ProcessNode, HierarchyNode>();
        private Set<HierarchyNode> rawHNodes = new HashSet<HierarchyNode>();
        private HierarchyNode root;

        void insert( ProcessEdge e ) {
            ProcessEdge i = e;

            if ( e instanceof Aggregation )
                i = new Aggregation( e.getTarget(), e.getSource() );
            if ( this.contains( i.getTarget() ) ) {
                //we have to insert a new subclass
                HierarchyNode sourceElement = new HierarchyNode( i.getSource() );
                HierarchyNode targetElement = nodes.get( i.getTarget() );
                
                nodes.put( i.getSource(), sourceElement );
                rawHNodes.add( sourceElement );

                targetElement.addChild(  sourceElement );
                sourceElement.setParent( targetElement );
            } else  if ( this.contains( i.getSource() ) ) {
                //we have to insert a new superclass
                HierarchyNode sourceElement = nodes.get( i.getSource() );
                HierarchyNode targetElement = new HierarchyNode( i.getTarget() );
                nodes.put( i.getTarget(), targetElement );
                rawHNodes.add( targetElement );

                targetElement.addChild( sourceElement );
                root = targetElement;
                sourceElement.setParent( targetElement );
            } else {
                HierarchyNode sourceElement = new HierarchyNode( i.getSource() );
                HierarchyNode targetElement = new HierarchyNode( i.getTarget() );
                nodes.put( i.getTarget(), targetElement );
                rawHNodes.add( targetElement );
                nodes.put( i.getSource(), sourceElement );
                rawHNodes.add( sourceElement );

                targetElement.addChild(sourceElement);
                sourceElement.setParent( targetElement );
                root = targetElement;
            }
        }

        boolean contains( ProcessNode n ) {
            return nodes.containsKey(n);
        }

        public List<List<ProcessObject>> detectCircles() {
            List<List<ProcessObject>> circles = new ArrayList<List<ProcessObject>>();
            Set<ProcessNode> circling = new HashSet<ProcessNode>();

            for ( HierarchyNode hn : rawHNodes ) {
                if ( hn.hasDescendant( hn.node ) && !circling.contains(hn.node) ) {
                    List<ProcessObject> circle = new LinkedList<ProcessObject>();
                    circles.add(circle);
                    circling.add( hn.node );
                    hn.findPathTo(hn.node, circle);
                }
            }

            return circles;
        }

        public Map<ProcessNode, List<ProcessObject>> detectMultiAggregation() {
            Map<ProcessNode, List<ProcessObject>> multi = new HashMap<ProcessNode, List<ProcessObject>>();
            Set<ProcessNode> multiNodes = new HashSet<ProcessNode>();

            for ( HierarchyNode hn : rawHNodes ) {
                if ( hn != nodes.get( hn.node ) )
                    multiNodes.add( hn.node );
            }

            for ( ProcessNode pn : multiNodes ) {
                List<ProcessObject> parents = new ArrayList<ProcessObject>();

                for ( HierarchyNode hn : rawHNodes ) {
                    if ( hn.node == pn ) {
                        parents.add( hn.parent.node );
                    }
                }

                parents.add( pn );
                multi.put( pn, parents );
            }


            return multi;
        }
        
        @Override
        public String toString() {
            return this.root.toString();
        }
    }

    private class HierarchyNode {
        private ProcessNode node;
        private HierarchyNode parent;
        private List<HierarchyNode> children = new ArrayList<HierarchyNode>();

        public HierarchyNode(ProcessNode node) {
            this.node = node;
        }

        void addChild( HierarchyNode n ) {
            int i;
            for ( i = 0; i < children.size(); i++ ) {
                if ( n.node.getId().hashCode() > children.get(i).node.getId().hashCode() )
                    break;
            }
            children.add(i, n);
        }

        void setChildren( List<HierarchyNode> children ) {
            this.children = children;
        }

        List<HierarchyNode> getChildren() {
            return children;
        }

        HierarchyNode getParent() {
            return parent;
        }

        void setParent(HierarchyNode parent) {
            this.parent = parent;
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append(node.toString());
            b.append(" --> ");
            b.append(children.toString());
            return b.toString();
        }

        public boolean hasDescendant( ProcessNode n ) {
            for ( HierarchyNode h : children ) {
                if ( ( h.node == n && this.node != n )|| h.hasDescendant(n) )
                    return true;
            }

            return false;
        }

        public void findPathTo( ProcessNode n, List<ProcessObject> path ) {
            if ( this.hasDescendant(n) ) {
                path.add( this.node );

                for ( HierarchyNode hn : this.children ) {
                    if ( hn.node == n ) {
                        path.add( hn.node );
                        return;
                    } else if ( hn.hasDescendant(n) ) {
                        hn.findPathTo(n, path);
                    }
                }
            }
        }
    }

    private static final String[] fileTypes = { "xsd" };

    private DomainModel model;
    private Document schema;
    private Map<String, String> importedNamespaces = new HashMap<String, String>();
    private Map<String, String> typeNames = new HashMap<String, String>();
    private Set<ProcessNode> unmappedNodes;
    boolean refTypeAdded;

    public void serialize(File f, ProcessModel m) throws Exception {
        if (!(m instanceof DomainModel)) throw new Exception("Unsupported model type for XSD creation.");

        model = ( DomainModel ) m;
        unmappedNodes = new HashSet<ProcessNode>( m.getNodes() );
        refTypeAdded = false;

        Set<Hierarchy> inheritance = determineInheritanceHierarchy();
        Set<Hierarchy> aggregation = determineAggregationHierarchy();

        schema = this.createEmptySchema();

        for ( Hierarchy h : aggregation )
            serializeAggregationHierarchy(h, inheritance);

        for ( Hierarchy h : inheritance )
            serializeInheritanceHierarchy(h);


        addDocumentElementDefinition();
        FileOutputStream fos = new FileOutputStream(f);
        ProcessEditorServerUtils.writeXMLtoStream( fos, schema );
        fos.close();
        
        importedNamespaces.clear();
        typeNames.clear();
        schema = null;
    }

    public Set<Class<? extends ProcessModel>> getSupportedModels() {
         Set<Class<? extends ProcessModel>> models = new HashSet<Class<? extends ProcessModel>>();
         models.add( DomainModel.class );
         return models;
    }

    public String getDisplayName() {
        return "Domain to XSD";
    }

    public String[] getFileTypes() {
        return fileTypes;
    }

    public Set<Hierarchy> determineAggregationHierarchies( ProcessModel m ) {
        if ( ! (m instanceof DomainModel) )
            return null;
        
        this.model = (DomainModel) m;
        return this.determineAggregationHierarchy();
    }

    private Document createEmptySchema() {
        Document doc = XMLHelper.newDocument();
        Element schemaEl = XMLHelper.addDocumentElement(doc, "xs:schema");
        schemaEl.setAttribute("xmlns:xs", SCHEMA_NS);
//        schemaEl.setAttribute("targetNamespace", model.getProperty( DomainModel.PROP_NAMESPACE_URI ));

        return doc;
    }

    private Set<Hierarchy> determineInheritanceHierarchy() {
        Set<Hierarchy> result = new HashSet<Hierarchy>();
        Set<Inheritance> inheritanceEdges = new HashSet<Inheritance>();
        Set<ProcessNode> visited = new HashSet<ProcessNode>();
        
        for ( ProcessEdge e : model.getEdges() ) {
            if ( e instanceof Inheritance )
                inheritanceEdges.add( (Inheritance) e );
        }

        for ( Inheritance i : inheritanceEdges ) {
            ProcessNode source = i.getSource();
            ProcessNode target = i.getTarget();

            visited.add( source ); visited.add( target );

            boolean inserted = false;
            for ( Hierarchy group : result ) {
                if ( group.contains( source ) ) {
                    group.insert(i);
                    inserted = true; break;
                } else if ( group.contains( target ) ) {
                    group.insert(i);
                    inserted = true; break;
                } 
            }

            if ( !inserted ) {
                Hierarchy h = new Hierarchy();
                h.insert(i);
                result.add(h);
            }
        }

        for ( ProcessNode n : model.getNodes() ) {
            if ( ! visited.contains(n) && ( n instanceof DomainClass || n instanceof DomainClassReference ) ) {
                Hierarchy h = new Hierarchy();
                h.root = new HierarchyNode(n);
                result.add(h);
            }
        }

        return result;
    }

    private Set<Hierarchy> determineAggregationHierarchy() {
        Set<Hierarchy> result = new HashSet<Hierarchy>();
        //1 determine all aggregation edges
        Set<Aggregation> aggregations = new HashSet<Aggregation>();

        for ( ProcessEdge e : model.getEdges() ) {
            if ( e instanceof Aggregation )
                aggregations.add( (Aggregation) e );
        }

        Set<ProcessNode> roots = new HashSet<ProcessNode>();
        //2 find root instances && others with no incoming edge
        for ( Aggregation agg : aggregations ) {
            ProcessNode source = agg.getSource();
            boolean isTarget = false;
            for ( Aggregation a : aggregations )
                if ( a.getTarget() == source ) {
                    isTarget = true;
                    break;
                }

            if ( !isTarget )
                roots.add( source );
        }

        for ( ProcessNode root : roots ) {
            Hierarchy h = new Hierarchy();
            aggregations.removeAll(determineAggregationHierarchy(root, aggregations, new HashSet<Aggregation>(), h));
            result.add(h);
        }

        return result;
    }

    private Set<Aggregation> determineAggregationHierarchy( ProcessNode node, Set<Aggregation> aggregations, Set<Aggregation> blocked, Hierarchy h ) {
        Set<Aggregation> removable = new HashSet<Aggregation>();
        for ( Aggregation agg : aggregations)
            if ( !blocked.contains( agg ) && agg.getSource() == node ) {
                h.insert( agg );
                removable.add(agg);

                //if ( agg.getTarget() == node )
                    blocked.add(agg);

                removable.addAll( determineAggregationHierarchy( agg.getTarget(), aggregations, blocked, h) );
            }

        return removable;
    }

    private void serializeInheritanceHierarchy( Hierarchy h ) throws Exception {
        serializeInheritanceNode(h.root);
    }

    private void serializeAggregationHierarchy( Hierarchy h, Set<Hierarchy> hierarchies ) throws Exception {
        serializeAggregationNode(h.root, hierarchies);
    }

    private void serializeInheritanceNode( HierarchyNode n ) throws Exception {
        Element el = null;
        Element seqEl = null;

        if ( unmappedNodes.contains(n.node) ) {
            if ( n.node instanceof DomainClass ) {
                unmappedNodes.remove(n.node);
                el = XMLHelper.addElement(schema, schema.getDocumentElement(), TYPE_COMPLEX_TYPE);
                el.setAttribute(ATTRIBUTE_NAME, getXMLType(n.node.getText()));
                el.setAttribute("abstract", String.valueOf(n.node.getProperty( DomainClass.PROP_ABSTRACT ).equals("1")));

                HierarchyNode superClass = n.parent;

                if ( superClass != null ) {
                    el = XMLHelper.addElement(schema, el, TYPE_COMPLEX_CONTENT);
                    el = XMLHelper.addElement(schema, el, TYPE_EXTENSION);

                    el.setAttribute("base", getXMLType( superClass.node.getText() ));
                    if ( superClass.node instanceof DomainClassReference ) {
                        //resolve reference and add namespace info via "import"
                        importReferencedNamespace( superClass.node.getProperty( DomainClassReference.PROP_REF ), schema);
                    }
                }
                seqEl = XMLHelper.addElement(schema, el, TYPE_SEQUENCE);
                serializeAttributes( (DomainClass) n.node, schema, seqEl );
                serializeAssociations(n.node, schema, seqEl);
            } else if ( n.node instanceof DomainClassReference ) {
                unmappedNodes.remove(n.node);
                //@todo import namespace if necessary
            }
        }

        for ( HierarchyNode hn : n.children ) {
            serializeInheritanceNode(hn);
        }
    }

    private void serializeAggregationNode( HierarchyNode n, Set<Hierarchy> inheritance ) throws Exception {
        if ( !unmappedNodes.contains(n.node) ) {
            return;
        }

        Hierarchy inh = null;
        Element el = null;
        Element seqEl = null;
        if ( n.node instanceof DomainClass ) {
            unmappedNodes.remove(n.node);
            el = XMLHelper.addElement(schema, schema.getDocumentElement(), TYPE_COMPLEX_TYPE);
            el.setAttribute(ATTRIBUTE_NAME, getXMLType(n.node.getText()));
            el.setAttribute("abstract", String.valueOf(n.node.getProperty( DomainClass.PROP_ABSTRACT ).equals("1")));

            inh = getHierarchyContainingNode( n.node, inheritance);
            HierarchyNode superClass = null;

            if ( inh != null )
                superClass = inh.nodes.get( n.node ).parent;
            if ( superClass != null ) {
                el = XMLHelper.addElement(schema, el, TYPE_COMPLEX_CONTENT);
                el = XMLHelper.addElement(schema, el, TYPE_EXTENSION);

                el.setAttribute("base", getXMLType( superClass.node.getText() ));
                if ( superClass.node instanceof DomainClassReference ) {
                    //resolve reference and add namespace info via "import"
                    importReferencedNamespace( superClass.node.getProperty( DomainClassReference.PROP_REF ), schema);
                } else {
                    el.setAttribute("base", getXMLType( superClass.node.getText() ));
                }
            }
            seqEl = XMLHelper.addElement(schema, el, TYPE_SEQUENCE);
            serializeAttributes( (DomainClass) n.node, schema, seqEl);
            serializeAssociations(n.node, schema, seqEl);
        } else if ( n.node instanceof DomainClassReference ) {
            unmappedNodes.remove(n.node);
            //@todo import namespace if necessary
            importReferencedNamespace( n.node.getProperty( DomainClassReference.PROP_REF ), schema);
        }

        if ( el != null ) {
            if ( seqEl == null )
                seqEl = XMLHelper.addElement(schema, el, TYPE_SEQUENCE);

            for ( HierarchyNode hn : n.children ) {
                ProcessEdge conn = null;
                for ( ProcessEdge e : model.getEdges() ) {
                    if ( e.getSource() == n.node && e.getTarget() == hn.node ) {
                        conn = e;
                        break;
                    }
                }

                Point bounds = this.getBounds( conn.getProperty( Aggregation.PROP_TARGET_MULTIPLICITY) );
                //for aggregations we can add the element directly ( default case ) or only refer to it ( if defined somewhere else )

                Element defaultCase = XMLHelper.addElement(schema, seqEl, TYPE_ELEMENT);
                defaultCase.setAttribute(ATTRIBUTE_MIN_OCCURS, String.valueOf( bounds.x ));
                defaultCase.setAttribute(ATTRIBUTE_MAX_OCCURS, bounds.y == Integer.MIN_VALUE ? "unbounded" : String.valueOf( bounds.y ) );
                defaultCase.setAttribute(ATTRIBUTE_NAME, toElementName( hn.node.getText() ) );
                defaultCase.setAttribute(ATTRIBUTE_TYPE, getXMLType( hn.node.getText() ));
                serializeAggregationNode(hn, inheritance);
            }

           if (n.node instanceof DomainClass ) {
               if ( ( inh != null && inh.nodes.get(n.node).parent == null ) || ( inh == null ))
                    addIdAttribute(schema, el);
           }
        }
    }

    private void serializeAttributes( DomainClass dc, Document schema, Element el ) {
        //for each attribute of the class a an xsd:element definition
        List<Attribute> attributes = new LinkedList<Attribute>(dc.getAttributesByIDs().values());
        // Add default attributes TITLE and DESCRIPTION        
        attributes.add(0,new Attribute(CLASS_ATTRIBUTE_DESCRIPTION, "string"));
        attributes.add(0,new Attribute(CLASS_ATTRIBUTE_TITLE, "string"));
        
        for ( Attribute attr : attributes ) {
            Element attEl = XMLHelper.addElement( schema, el, TYPE_ELEMENT);
            //@TODO get space-free representation of attribute name
            attEl.setAttribute(ATTRIBUTE_NAME, attr.getName().toLowerCase().replaceAll("\\s+", "-"));

            attEl.setAttribute(ATTRIBUTE_TYPE, getXMLType( attr.getType() ));
            Point bounds;
            if( attr.getType().equals( ML_TEXT_TYPE_NAME ) )
                bounds = new Point(0, Integer.MIN_VALUE);
            else
                bounds = this.getBounds( attr.getMultiplicity() );
            attEl.setAttribute(ATTRIBUTE_MIN_OCCURS, String.valueOf( bounds.x ));
            attEl.setAttribute(ATTRIBUTE_MAX_OCCURS, bounds.y == Integer.MIN_VALUE ? "unbounded" : String.valueOf( bounds.y ) );
            if( attr.getType().equals( ML_TEXT_TYPE_NAME ) ) {
                Element compTypeEl = XMLHelper.addElement( schema, attEl, TYPE_COMPLEX_TYPE );
                Element attrEl = XMLHelper.addElement(schema, compTypeEl, TYPE_ATTRIBUTE);
                attrEl.setAttribute( ATTRIBUTE_USE, "required");
                attrEl.setAttribute( ATTRIBUTE_NAME, "lang");
                attrEl.setAttribute( ATTRIBUTE_TYPE, "xs:language");
            }
        }
    }

    private List<ProcessEdge> getOutgoingAssociations( ProcessNode n ) {
        List<ProcessEdge> asso = new ArrayList<ProcessEdge>();

        for ( ProcessEdge e : model.getEdges() ) {
            boolean add = false;
            if ( e instanceof Aggregation && e.getTarget() == n && (
                        e.getProperty( Aggregation.PROP_DIRECTION ).equals( Aggregation.DIRECTION_SOURCE )
                        || e.getProperty( Aggregation.PROP_DIRECTION ).equals( Aggregation.DIRECTION_BOTH )
                        )) {
                add = true;
            } else if ( e instanceof Association && (
                        ( e.getProperty( Association.PROP_DIRECTION ).equals( Association.DIRECTION_TARGET )&& e.getSource() == n )
                        || ( e.getProperty( Association.PROP_DIRECTION ).equals( Association.DIRECTION_SOURCE )&& e.getTarget() == n )
                        || e.getProperty( Association.PROP_DIRECTION ).equals( Association.DIRECTION_BOTH )
                        || e.getProperty( Association.PROP_DIRECTION ).equals( Association.DIRECTION_NONE )
                        ))  {
                add = true;
            }

            if ( add ) {
                int i;
                for ( i = 0; i < asso.size(); i++ )
                    if ( e.getId().hashCode() > asso.get(i).hashCode() )
                        break;

                asso.add(i, e);
            }
        }

        return asso;
    }

    private void serializeAssociations( ProcessNode n, Document schema, Element el ) {
        List<ProcessEdge> associations = this.getOutgoingAssociations(n);
        for ( ProcessEdge asso : associations ) {
            boolean nIsSource = ( asso.getSource() == n );
            ProcessNode opposite = nIsSource ? asso.getTarget() : asso.getSource();
            String mult = nIsSource ? asso.getProperty( Association.PROP_TARGET_MULTIPLICITY ) : asso.getProperty( Association.PROP_SOURCE_MULTIPLICITY );
            String assoName = nIsSource ? asso.getProperty( Association.PROP_TARGET_NAME ) : asso.getProperty( Association.PROP_SOURCE_NAME );
            Point bounds = getBounds(mult);
            String name;
            
            if ( assoName != null && !assoName.isEmpty() ) {
                name = toElementName( assoName );
            } else  {
                name = opposite.getText().toLowerCase().replaceAll("\\s+", "-");
                if ( !asso.getLabel().isEmpty() ) {
                    name += "-" + toElementName( asso.getLabel() );
                }
            } 

            Element refEl = addReference(schema, el, name);

            refEl.setAttribute(ATTRIBUTE_MIN_OCCURS, String.valueOf( bounds.x ));
            refEl.setAttribute(ATTRIBUTE_MAX_OCCURS, bounds.y == Integer.MIN_VALUE ? "unbounded" : String.valueOf( bounds.y ) );
        }
    }

    private void addDocumentElementDefinition( ) {
        Comment c = schema.createComment("DEFINITION OF ELEMENTS");
        schema.getDocumentElement().appendChild(c);

        for ( ProcessNode n : model.getNodes() ) {
            if ( n instanceof DomainClass ) {
                Element el  = XMLHelper.addElement(schema, schema.getDocumentElement(), TYPE_ELEMENT);
                el.setAttribute( ATTRIBUTE_NAME, toElementName( n.getName() ));
                el.setAttribute( ATTRIBUTE_TYPE, getXMLType( n.getName() ));
            }
        }
    }

    private Hierarchy getHierarchyContainingNode( ProcessNode n, Set<Hierarchy> hierarchies ) {
        for ( Hierarchy h : hierarchies )
            if ( h.contains(n) )
                return h;

        return null;
    }

    //@TODO refine
    private String getXMLType( String s ) {
        if ( typeNames.containsKey(s) )
            return typeNames.get(s);

        boolean complex = true;
        if ( s.toLowerCase().equals("text") || s.equals( ML_TEXT_TYPE_NAME) )
            s = "string";

        for (String t: xmlTypes) {
            if (t.equalsIgnoreCase(s)) {
                complex = false;
                s = "xs:" + t;
            }
        }

//        if ( xmlTypes.contains(s) ) {
//            complex = false;
//            s = "xs:" + s;
//        }
//
//        else if ( xmlTypes.contains(s.toLowerCase()) ) {
//            complex = false;
//            s = "xs:" + s.toLowerCase();
//        }

        String typeName = toTypeName(s, complex);
        typeNames.put(s, typeName);
        return typeName;
    }

    private Point getBounds( String multiplicity ) {

        if ( multiplicity == null )
            return new Point(0, Integer.MIN_VALUE);

        if ( multiplicity.equals("1") )
            return new Point(1, 1);
        else if ( multiplicity.equals("1..*") )
            return new Point(1, Integer.MIN_VALUE);
        else if ( multiplicity.equals("0..1") )
            return new Point(0, 1);
        else
            return new Point(0, Integer.MIN_VALUE);
    }

    private void addIdAttribute( Document doc, Element el ) {
        Element idAttEl = XMLHelper.addElement(doc, el,TYPE_ATTRIBUTE);
        idAttEl.setAttribute(ATTRIBUTE_NAME,"id");
        idAttEl.setAttribute(ATTRIBUTE_TYPE,"xs:ID");
        idAttEl.setAttribute(ATTRIBUTE_USE,"optional");
    }

    private void addIdRefAttribute( Document doc, Element el ) {
        Element idrefEl = XMLHelper.addElement(doc, el, TYPE_ATTRIBUTE);
        idrefEl.setAttribute(ATTRIBUTE_NAME, "id");
        idrefEl.setAttribute(ATTRIBUTE_TYPE, "xs:IDREF");
        idrefEl.setAttribute(ATTRIBUTE_USE, "required");
    }

    private Element addReference( Document doc, Element el , String elName ) {
        if ( !refTypeAdded ) {
            Element typeEl = XMLHelper.addElement(doc, doc.getDocumentElement(), TYPE_COMPLEX_TYPE);
            typeEl.setAttribute(ATTRIBUTE_NAME, "CT_Reference");
            addIdRefAttribute(doc, typeEl);
            refTypeAdded = true;
        }

        Element refEl = XMLHelper.addElement(doc, el, TYPE_ELEMENT);
        refEl.setAttribute(ATTRIBUTE_NAME, toElementName( elName ) );
        refEl.setAttribute(ATTRIBUTE_TYPE, "CT_Reference");

        return refEl;
    }

    private void importReferencedNamespace( String uri, Document doc ) throws Exception {
        if ( importedNamespaces.containsKey(uri) )
            return;

        try {
            ProcessModel pm = ReferenceHelper.getReferencedModel(uri, model);

            if ( !(pm instanceof DomainModel ) )
                throw new Exception("Referenced models must be of type domain model!");

            String namespace = pm.getProperty( DomainModel.PROP_NAMESPACE_URI );

            if ( !importedNamespaces.containsValue(namespace) ) {
                Element docEl = doc.getDocumentElement();
                docEl.setAttribute("xmlns:" + pm.getProperty( DomainModel.PROP_NAMESPACE_PREFIX ).toLowerCase(), namespace);

                Element impEl = XMLHelper.addElementBefore(doc, (Element) docEl.getFirstChild(), "import");
//
                impEl.setAttribute("namespace", namespace);
                impEl.setAttribute("schemaLocation", namespace);
            }

            importedNamespaces.put(uri, namespace);            
        } catch ( Exception ex ) {
            throw new Exception("An error occured while fetching a referenced model!", ex);
        }

    }

    private String toTypeName( String s, boolean complex ) {
        if ( s.contains(":") )
            return s.split(":")[0].toLowerCase() + (complex ? ":CT_" : ":") + s.split(":")[1].replaceAll("\\s+", "-");
        else
            return (complex ? "CT_" : "") + s.replaceAll("\\s+", "-").replaceAll(":", "-");
    }

    private String toElementName( String s ) {
        if ( s.contains(":") ) {
            s = s.split(":")[1];
        }
        return toTypeName(s, false).toLowerCase();
    }

}

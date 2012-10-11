import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.SquarifiedTreeMapLayout;
import prefuse.controls.ControlAdapter;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tree;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.query.SearchQueryBinding;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.ColorMap;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.UILib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTree;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;


/**
 * This class is responsible for creating a TreeMap visualisation of the given dataset. 
 * The Tree which it visualises is structured as follows:
 * - The first level contains the name of the states
 * - Each state has as its children the political parties in that state, as the second level of the tree
 * - Each political party has as its children the MPs in of that political party from the state, 
 *   as the third level of the tree
 * 
 * The nodes corresponding to MPs are color coded according to the political parties to which they belong, 
 * with the same color for a given party across all states.
 *
 */
public class TreeMap extends Display {

    public static TreeSet<String> labelsList;
    public static Hashtable<String,int[]> labelsColorCoding;
    private static Visualization vis;
    
    private static final Schema LABEL_SCHEMA = PrefuseLib.getVisualItemSchema();
    static {
        LABEL_SCHEMA.setDefault(VisualItem.INTERACTIVE, false);
        LABEL_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(255));
        LABEL_SCHEMA.setDefault(VisualItem.FONT, FontLib.getFont("Tahoma",8));
    }
    
    private static final String tree = "tree";
    private static final String treeNodes = "tree.nodes";
    private static final String treeEdges = "tree.edges";
    private static final String labels = "labels";

    private SearchQueryBinding searchQ;
    private static ColorAction borderColor;
    static Hashtable<String,int[]> colorCoding ;

    /**Signature: void putActions()
     * This method binds the actions to the visualization. 
     *      
     */
    private static void putActions(){
    	// border colors
        borderColor = new BorderColorAction(treeNodes);
        final ColorAction fillColor = new FillColorAction(treeNodes);
        
        // color settings
        ActionList colors = new ActionList();
        colors.add(fillColor);
        colors.add(borderColor);
        vis.putAction("colors", colors);
        
        // animate paint change
        ActionList animatePaint = new ActionList(400);
        animatePaint.add(new ColorAnimator(treeNodes));
        animatePaint.add(new RepaintAction());
        vis.putAction("animatePaint", animatePaint);
        
        // create the single filtering and layout action list
        ActionList layout = new ActionList();
        layout.add(new SquarifiedTreeMapLayout(tree));
        layout.add(new LabelLayout(labels));
        layout.add(colors);
        layout.add(new RepaintAction());
        vis.putAction("layout", layout);

    }
    
    public TreeMap(Tree t, String label, Hashtable<String,int[]> colorCoding) {
        super(new Visualization());
        
        labelsColorCoding=colorCoding;
        vis = m_vis;
        VisualTree vt = m_vis.addTree(tree, t);
        m_vis.setVisible(treeEdges, null, false);
        
        Predicate noLeaf = (Predicate)ExpressionParser.parse("childcount()>0");
        m_vis.setInteractive(treeNodes, noLeaf, false);

        Predicate labelP = (Predicate)ExpressionParser.parse("treedepth()=1");
        m_vis.addDecorators(labels, treeNodes, labelP, LABEL_SCHEMA);
        
        Iterator asd = m_vis.items();
        
        DefaultRendererFactory rf = new DefaultRendererFactory();
        rf.add(new InGroupPredicate(treeNodes), new NodeRenderer());
        rf.add(new InGroupPredicate(labels), new LabelRenderer(label));
        m_vis.setRendererFactory(rf);
                       
        putActions();
        setSize(1300,700);
        setItemSorter(new TreeDepthItemSorter());
        addControlListener(new ControlAdapter() {
            public void itemEntered(VisualItem item, MouseEvent e) {
                item.setStrokeColor(borderColor.getColor(item));
                item.getVisualization().repaint();
            }
            public void itemExited(VisualItem item, MouseEvent e) {
                item.setStrokeColor(item.getEndStrokeColor());
                item.getVisualization().repaint();
            }           
        });
        m_vis.run("layout");
    }
    
    public SearchQueryBinding getSearchQuery() {
        return searchQ;
    }
    

    public static JComponent demo() {
        return demo();
    }

    
    /**
     * Signature: Hashtable<String,int[]> createColorCoding(TreeSet<String> t)
     * Takes a list of String values as a TreeSet, and creates a hashtable which maps each String to an array of three integers 
     * containing RGB values of unique color assigned to it.
     * 
     */
	public static Hashtable<String,int[]> createColorCoding(TreeSet<String> t) {
		Hashtable<String, int[]> coding= new Hashtable<String,int[]>();
		Random rn=new Random();
		Iterator<String> treeIter=t.iterator();
		int i = 5;
		while(treeIter.hasNext()) {
			int[] colors=new int[3];
			colors[0] = i +3;
			colors[1]=colors[0];
			colors[2] = colors[0];
			coding.put(treeIter.next(), colors);
			i = i+3;
		}
		return coding;
	}

    
	/**
     * Signature: Tree makeData()
     * This method parses the table created after reading the input file, and constructs a tree with states at first level, 
     * political parties of a given state at the second level, and MPs of a given party from a given state at the third level. 
     * It also calls the method colorCoding which assigns a unique color to each political party.
     * Returns the constructed tree.
     * 
     */
    private static Tree makeData(){
    	Table nodeList = new Table();
        nodeList.addColumn("nodes", String.class);
        nodeList.addRow();
        Table edgeList = new Table();
        edgeList.addColumn(Tree.DEFAULT_SOURCE_KEY,int.class);
        edgeList.addColumn(Tree.DEFAULT_TARGET_KEY,int.class);

        int numberRow = ReadFile.numberRow;
		Table table = ReadFile.table;
		
		TreeSet <String> states = new TreeSet<String>();
		for (int i= 1; i < numberRow; i++)
		{
			states.add(table.getString(i,4));
		}
		Iterator<String> it = states.iterator(); 
        String empty = it.next();
        TreeSet<String> PartiesList = new TreeSet<String>();
        Hashtable<String, Integer> PartiesFreq = new Hashtable<String,Integer>(); 
        for (int i=1; i< states.size(); i++)
        {
        	int nrow = nodeList.addRow();
        	String stateName = it.next();
        	nodeList.set(nrow,0,stateName);
        	int erow = edgeList.addRow();
        	edgeList.set(erow, 0, 0);
        	edgeList.set(erow, 1, nrow);
        	TreeSet<String> polSet = new TreeSet<String>();
        	for (int j=1; j<numberRow; j++)
        	{
        		if((table.getString(j,4)).equals(stateName))
        		{	
        			polSet.add(table.getString(j, 6));
        		}
        		
        	}
        	Iterator<String> pit = polSet.iterator();
        	while(pit.hasNext())
        	{
        		String Party = pit.next();
        		PartiesList.add(Party);
        		int nr = nodeList.addRow();
        		nodeList.set(nr, 0, Party);
        		
        		int er = edgeList.addRow();
        		edgeList.set(er,0, nrow);
        		edgeList.set(er,1, nr);
        		
        		for (int k =1; k<numberRow; k++)
        		{
        			if((table.getString(k,6)).equals(Party) && (table.getString(k,4)).equals(stateName))
        			{
        				String mpName = table.getString(k, 0);
        				int mpr = nodeList.addRow();
        				nodeList.set(mpr, 0, mpName);
        				int mpe = edgeList.addRow();
        				edgeList.set(mpe, 0, nr);
        				edgeList.set(mpe, 1, mpr);
        			}
        		}
        		
        	}
        }
        Hashtable<String,int[]> colorcoding = createColorCoding(PartiesList);
        colorCoding = colorcoding;
        Tree tm=new Tree(nodeList,edgeList);
    	return tm;
    }
    
    /**
     * This function controls the flow of the execution in this file
     */
    public static void run(){
    	Tree t = makeData();
        TreeMap mpvis = new TreeMap(t,"India",colorCoding);
        UILib.setPlatformLookAndFeel();
        JComponent treemap = mpvis.display(t,colorCoding);
        JFrame frame = new JFrame("Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(treemap);
        frame.pack();
        frame.setVisible(true);
    }
    

    /**
     * Signature: JComponent display(Tree tm, Hashtable<String,int[]> colorCoding)
     * This method takes a tree and a color coding, and returns a JComponent constructed from them.
     * 
     */
    public static JComponent display(Tree tm, Hashtable<String,int[]> colorCoding) {
        Tree t = tm;
        final TreeMap treemap = new TreeMap(t, "nodes", colorCoding);
        
        final JFastLabel title = new JFastLabel("                 ");
        title.setPreferredSize(new Dimension(350, 20));
        title.setVerticalAlignment(SwingConstants.BOTTOM);
        title.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
        title.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));
        
        treemap.addControlListener(new ControlAdapter() {
            public void itemEntered(VisualItem item, MouseEvent e) {
            	System.out.println(item.getString("nodes"));
            }
        });
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(treemap, BorderLayout.CENTER);
        UILib.setColor(panel, Color.BLACK, Color.GRAY);
        return panel;
    }
    
    
    /**
     * Set the stroke color for drawing treemap node outlines. A graded
     * grayscale ramp is used, with higher nodes in the tree drawn in
     * lighter shades of gray.
     */
    public static class BorderColorAction extends ColorAction {
        
        public BorderColorAction(String group) {
            super(group, VisualItem.STROKECOLOR);
        }
        
        public int getColor(VisualItem item) {
            NodeItem nitem = (NodeItem)item;
            if (nitem.isHover() )
                return ColorLib.rgb(255,255,255);
            
            int depth = nitem.getDepth();
            if ( depth == 1 ) {
                return ColorLib.rgb(255,255,255);
            } else if ( depth == 2 ) {
                return ColorLib.rgb(0,0,0);
            } else {
                return ColorLib.rgb(0,0,0);
            }
        }
    }
    
    /**
     * Set fill colors for treemap nodes. 
     * Normal nodes are shaded according to their depth in the tree.
     * 
     */
    public static class FillColorAction extends ColorAction {
        private ColorMap cmap = new ColorMap(
            ColorLib.getCoolPalette(10), 0, 9);

        public FillColorAction(String group) {
            super(group, VisualItem.FILLCOLOR);
        }
        
        public int getColor(VisualItem item) {
            if ( item instanceof NodeItem ) {
                NodeItem nitem = (NodeItem)item;
                if ( nitem.getChildCount() > 0 ) {
                    return 0; 
                } else {
                	String partyName = nitem.getParent().getString("nodes");
                	int[] colors=labelsColorCoding.get(partyName);
                	return ColorLib.rgb(colors[0], colors[1], colors[2]);
                }
            } else {
                return cmap.getColor(0);
            }
        }
        
    }
    
    /**
     * Set label positions. Labels are assumed to be DecoratorItem instances,
     * decorating their respective nodes. The layout simply gets the bounds
     * of the decorated node and assigns the label coordinates to the center
     * of those bounds.
     */
    public static class LabelLayout extends Layout {
        public LabelLayout(String group) {
            super(group);
        }
        public void run(double frac) {
            Iterator iter = m_vis.items(m_group);
            while ( iter.hasNext() ) {
                DecoratorItem item = (DecoratorItem)iter.next();
                VisualItem node = item.getDecoratedItem();
                Rectangle2D bounds = node.getBounds();
                setX(item, null, bounds.getCenterX());
                setY(item, null, bounds.getCenterY());
            }
        }
    }
    
    /**
     * A renderer for treemap nodes. Draws simple rectangles, but defers
     * the bounds management to the layout.
     */
    public static class NodeRenderer extends AbstractShapeRenderer {
        private Rectangle2D m_bounds = new Rectangle2D.Double();
        
        public NodeRenderer() {
            m_manageBounds = false;
        }
        protected Shape getRawShape(VisualItem item) {
            m_bounds.setRect(item.getBounds());
            return m_bounds;
        }
    } 
    
} 
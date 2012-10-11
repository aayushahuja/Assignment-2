import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.distortion.Distortion;
import prefuse.action.distortion.FisheyeDistortion;
import prefuse.action.layout.Layout;
import prefuse.controls.AnchorUpdateControl;
import prefuse.controls.ControlAdapter;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

/**
 * This class creates a Fisheye Menu visualisation of all the states. On clicking a state, a Fisheye menu of the political 
 * parties in the state is displayed. The list of MPs of a given political party from a state can be seen by clicking the 
 * name of the political party in the Fisheye menu.
 * Control enters this class from the 'run' method, which mkes necessary function calls to create the visualisation.
 *
 */
public class StateMenu extends Display {

    /** The data group name of menu items. */
    public static final String ITEMS = "items";
    /** The label data field for menu items. */
    public static final String LABEL = "label";
    /** The action data field for menu items. */
    public static final String ACTION = "action";
    
    /**
     * This schema holds the data representation for internal storage of
     * menu items.
     */
    protected static final Schema ITEM_SCHEMA = new Schema();
    static {
        ITEM_SCHEMA.addColumn(LABEL, String.class);
        ITEM_SCHEMA.addColumn(ACTION, ActionListener.class);
    }
    
    private Table m_items = ITEM_SCHEMA.instantiate(); // table of menu items
    
    private static double m_maxHeight = 550; // maximum menu height in pixels
    private double m_scale = 2;       // scale parameter for fisheye distortion
    private static Visualization vis ;
    private static int scale;
	static double maxHeight;
	static private Distortion feye;
	static private ColorAction colors;
    /**
     * Create a new, empty FisheyeMenu.
     * @see #addMenuItem(String, javax.swing.Action)
     */
    
      public StateMenu() {
    	  super(new Visualization());
          m_vis.addTable(ITEMS, m_items);
          
          // set up the renderer to use
          LabelRenderer renderer = new LabelRenderer(LABEL);
          renderer.setHorizontalPadding(0);
          renderer.setVerticalPadding(1);
          renderer.setHorizontalAlignment(Constants.LEFT);
          m_vis.setRendererFactory(new DefaultRendererFactory(renderer));
          
          // set up this display
          setSize(200,450);
          setHighQuality(true);
          setBorder(BorderFactory.createEmptyBorder(10,10,10,5));
          addControlListener(new ControlAdapter() {
              // dispatch an action event to the menu item
              public void itemClicked(VisualItem item, MouseEvent e) {
                  ActionListener al = (ActionListener)item.get(ACTION);
                  al.actionPerformed(new ActionEvent(item, e.getID(),
                      "click", e.getWhen(), e.getModifiers()));
              }
          });
          
          // text color function
          // items with the mouse over printed in red, otherwise black
          ColorAction colors = new ColorAction(ITEMS, VisualItem.TEXTCOLOR);
          colors.setDefaultColor(ColorLib.gray(0));
          colors.add("hover()", ColorLib.rgb(255,0,0));
          
          // initial layout and coloring
          ActionList init = new ActionList();
          init.add(new VerticalLineLayout(m_maxHeight));
          init.add(colors);
          init.add(new RepaintAction());
          m_vis.putAction("init", init);

          // fisheye distortion based on the current anchor location
          ActionList distort = new ActionList();
          Distortion feye = new FisheyeDistortion(0,m_scale);
          distort.add(feye);
          distort.add(colors);
          distort.add(new RepaintAction());
          m_vis.putAction("distort", distort);
          
          // update the distortion anchor position to be the current
          // location of the mouse pointer
          addControlListener(new AnchorUpdateControl(feye, "distort"));
    }
    
    /**
     * Signature: void addMenuItem(String name, ActionListener listener)
     * Adds a row to the list of items and binds to it the action and the label.
     * 
     */
    public void addMenuItem(String name, ActionListener listener) {
    	int row = m_items.addRow();
        m_items.set(row, LABEL, name);
        m_items.set(row, ACTION, listener);
    }
    
    /**
     * Signature: StateMenu firstScreen(TreeSet<String> tree, final Table tab,final int row)
     * Takes the list of states as a TreeSet, and displays the states as a fisheye menu. 
     * It returns the visualisation as a StateMenu object .
     * 
     */
    public StateMenu firstScreen(TreeSet<String> tree, final Table tab,final int row) {
    	// create a new fisheye menu and populate it
        StateMenu fm = new StateMenu();
        Iterator<String> it = tree.iterator();
        while(it.hasNext()) {
            // add menu items that simply print their label when clicked
            fm.addMenuItem(it.next(), new AbstractAction() {
                public void actionPerformed(ActionEvent e) 
                {
                	Object stateC = (((VisualItem)e.getSource()).get(LABEL));
                	String stateCl = stateC.toString();
                	TreeSet<String> polSet = new TreeSet<String>();
    	        	for (int j=1; j<row; j++)
    	        	{
    	        		//7th column is the political party column number
    	        		if((tab.getString(j,4)).equals(stateCl))
    	        		{	
    	        			polSet.add(tab.getString(j, 6));
    		        		System.out.println(tab.getString(j, 6));
    	        		}
    	        	}
    	        	StateMenu fm = new StateMenu();
    				fm =  fm.secondScreen(polSet,tab,row,stateCl);
    		        // create and display application window
    		        JFrame f = new JFrame("Database Visulaization");
    		        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    		        f.getContentPane().add(fm);
    		        f.pack();
    		        f.setVisible(true);
    	        	
                }
            });
        }
        fm.getVisualization().run("init");
        return fm;
    }
    /**
     * Method 'secondScreen'
     * Signature: StateMenu secondScreen(TreeSet<String> tree, final Table tab,final int row, final String stateCl)
     * Takes the list of political parties as a TreeSet passed to it by the method firstScreen when a State name is 
     * clicked in the first screen, and displays the parties as a fisheye menu. 
     * It returns the visualisation as a StateMenu.
     * 
     */
    public StateMenu secondScreen(TreeSet<String> tree, final Table tab,final int row, final String stateCl) {
        // create a new fisheye menu and populate it
        StateMenu fm = new StateMenu();
        
        Iterator<String> it = tree.iterator();
        while(it.hasNext()) {
            // add menu items that simply print their label when clicked
            fm.addMenuItem(it.next(), new AbstractAction() {
                public void actionPerformed(ActionEvent e) 
                {
                	Object partyC = (((VisualItem)e.getSource()).get(LABEL));
                	String partyCl = partyC.toString();
                	TreeSet<String> polSet = new TreeSet<String>();
    	        	for (int j=1; j<row; j++)
    	        	{
    	        		if((tab.getString(j,6)).equals(partyCl) && tab.getString(j,4).equals(stateCl))
    	        		{	
    	        			polSet.add(tab.getString(j, 0));
    		        		System.out.println(tab.getString(j, 0));
    	        		}
    	        	}
    	        	
    	        	StateMenu fm = new StateMenu();
    				fm =  fm.thirdScreen(polSet,tab,row,partyCl,stateCl);
    		        // create and display application window
    		        JFrame f = new JFrame("Database Visulaization");
    		        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    		        f.getContentPane().add(fm);
    		        f.pack();
    		        f.setVisible(true);
    	        	
                }
            });
        }
        fm.getVisualization().run("init");
        return fm;
    }
    /**
     * Signature: StateMenu thirdScreen(TreeSet<String> tree, final Table tab,final int row, final String partyCl, final String stateCl)
     * Takes the list of members of parliament of a given party from a given state as a TreeSet passed to it by the method 
     * secondScreen when a Political Party name is clicked in the second screen, and displays the MPs as a fisheye menu. 
     * It returns the visualisation as a StateMenu.
     * 
     */
    public StateMenu thirdScreen(TreeSet<String> tree, final Table tab,final int row, final String partyCl, final String stateCl) {
        StateMenu fm = new StateMenu();
        Iterator<String> it = tree.iterator();
        while(it.hasNext()) {
            fm.addMenuItem(it.next(), new AbstractAction() {
                public void actionPerformed(ActionEvent e) 
                {
                	Object cand = (((VisualItem)e.getSource()).get(LABEL));
                	String candi = cand.toString();
                	
                	System.out.println("clicked item: "+ candi);
                    System.out.flush();
                }
            });
        }
        fm.getVisualization().run("init");
        return fm;
    }
    
    /**
     * StateMenu class contains a nested class VerticalLineLayout, which lines up all VisualItems vertically. 
     * It scales the size of the layout such that all items fit within the maximum layout size, and updates the display
     * to the final computed size.
     * Display to the final computed size.
     */
    public class VerticalLineLayout extends Layout {
        private double m_maxHeight = 600;
        
        public VerticalLineLayout(double maxHeight) {
            m_maxHeight = maxHeight;
        }
        
        public void run(double frac) {
        	maxHeight = m_maxHeight;
            double w = 0, h = 0;
            Iterator iter = m_vis.items();
            while ( iter.hasNext() ) {
                VisualItem item = (VisualItem)iter.next();
                item.setSize(1.0);
                h += item.getBounds().getHeight();
            }
            double scale = h > m_maxHeight ? m_maxHeight/h : 1.0;
            
            Display d = m_vis.getDisplay(0);
            Insets ins = d.getInsets();
            
            h = ins.top;
            double ih, y=0, x=ins.left;
            iter = m_vis.items();
            while ( iter.hasNext() ) {
                VisualItem item = (VisualItem)iter.next();
                item.setSize(scale); item.setEndSize(scale);
                Rectangle2D b = item.getBounds();
                
                w = Math.max(w, b.getWidth());
                ih = b.getHeight();
                y = h+(ih/2);
                setX(item, null, x);
                setY(item, null, y);
                h += ih;
            }
            
            setSize(d, (int)Math.round(2*m_scale*w + ins.left + ins.right),
                       (int)Math.round(h + ins.bottom));
        }
        
        private void setSize(final Display d, final int width, final int height)
        {
        	SwingUtilities.invokeLater(new Runnable() {
        		public void run() {
        			d.setSize(width, height);
        		}
        	});
        }
    } 
    
    /**
     * This function controls the flow of the execution in this file
     */
    public static void run(){
    	int numberRow = ReadFile.numberRow;
		Table table = ReadFile.table;
		TreeSet <String> states = new TreeSet<String>();
		for (int i= 1; i < numberRow; i++)
		{
			states.add(table.getString(i,4));
		}
		StateMenu fm = new StateMenu();
		fm =  fm.firstScreen(states,table,numberRow);
        JFrame f = new JFrame("Database Visulaization");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(fm);
        f.pack();
        f.setVisible(true);
    }
    
}

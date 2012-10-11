import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.DataShapeAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.assignment.SizeAction;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.*;
import prefuse.data.Table;
import prefuse.data.io.DelimitedTextTableReader;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.AxisRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.VisiblePredicate;

/**
 * This class creates a mashup visualisation of the given data, using the summary table.
 * The attributes to be plotted on X and Y-axes, and the attribute according to which the points are colorcoded
 * can be chosen at runtime .
 */
public class Mashup {
	static Visualization vis ;
	static Display d;
    private static final String group = "data";
    
    private ShapeRenderer m_shapeR = new ShapeRenderer(2);
    
    public Mashup(Table t, String xfield, String yfield) {
        this(t, xfield, yfield, null);
    }
    
    /**
     *
     * This function creates a visualization , makes all the actions required for visualization
     * It also displays the tooltip when user hovers over a node .
     * Size and background colour of the jframe are also set in this method 
     */
    public Mashup(Table t, String xfield, String yfield, String sfield) {
    	vis = new Visualization();
        vis.addTable(group, t);
        vis.setRendererFactory(new RendererFactory() {
            AbstractShapeRenderer sr = new ShapeRenderer();
            Renderer arY = new AxisRenderer(Constants.RIGHT, Constants.TOP);
            Renderer arX = new AxisRenderer(Constants.CENTER, Constants.FAR_BOTTOM);
            
            public Renderer getRenderer(VisualItem item) {
                return item.isInGroup("ylab") ? arY :
                       item.isInGroup("xlab") ? arX : sr;
            }
        });
        int[] shapes = new int[]{Constants.SHAPE_RECTANGLE};
        int[] hoverPalette = new int[]{ColorLib.rgb(250, 250, 0),ColorLib.rgb(225, 225, 0),ColorLib.rgb(200, 200, 0),ColorLib.rgb(175, 175, 0),ColorLib.rgb(150, 150, 0),ColorLib.rgb(125, 125, 0),ColorLib.rgb(100, 100, 0)};
        
        AxisLayout x_axis = new AxisLayout(group, "StateIndex",Constants.X_AXIS, VisiblePredicate.TRUE);
        AxisLayout y_axis = new AxisLayout(group, "Party",Constants.Y_AXIS, VisiblePredicate.TRUE);
        AxisLabelLayout ylabels = new AxisLabelLayout(group, y_axis);
        AxisLabelLayout xlabels = new AxisLabelLayout(group, x_axis);
        
        DataColorAction color = new DataColorAction(group,sfield,Constants.NOMINAL,VisualItem.FILLCOLOR, hoverPalette);
        DataShapeAction shape = new DataShapeAction(group, sfield , shapes);
        DataSizeAction size = new DataSizeAction("data","Party",10);
        
        vis.putAction("x", x_axis);
        vis.putAction("y", y_axis);
        vis.putAction("color", color);
        vis.putAction("shape", shape);
        vis.putAction("xlabels", xlabels);
        vis.putAction("xlabels", ylabels);
        
        ActionList size1 = new ActionList();
        size1.add(size);
        vis.putAction("size1", size1);
        
        ActionList draw = new ActionList();
        draw.add(x_axis);
        draw.add(y_axis);
        if ( sfield != null )
            draw.add(shape);
        draw.add(color);
        draw.add(size);
        draw.add(new RepaintAction());
        vis.putAction("draw", draw);
		d = new Display(vis); 
		int width=1200, height=600;
		d.setBackground(Color.GRAY); 
		d.addControlListener(new FocusControl(1));
		d.addControlListener(new PanControl());
		d.addControlListener(new ZoomControl()); 
		d.addControlListener(new NeighborHighlightControl());
		d.addControlListener(new HoverActionControl("hover"));
        d.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        d.setSize(width,height);
        d.setHighQuality(true);
        
        ToolTipControl ttc = new ToolTipControl(new String[] {"Party","State",sfield});
        d.addControlListener(ttc);
        vis.run("draw");

    }
    
    /**
     * Signature: void display(Table t)
     * Creates a JFrame to display the scatter plot.
     * It takes as input a sumary table t and displays it in a frame .
     * 
     */
    public static void display(Table t) {
        String xfield = "StateIndex";
        String yfield = "Party";
        String sfield = "Attendance";
        final Mashup sp = demo(t, xfield, yfield, sfield);
        JToolBar toolbar = getEncodingToolbar(sp, xfield, yfield, sfield);
        JFrame frame = new JFrame("MASHUP -- Hover over a node to display a tooltip");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(toolbar, BorderLayout.NORTH);
        frame.getContentPane().add(sp.getDisplay(), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * This function maintains the toolbar used in the mashup.
     * It also handles the action listeners that change the values on the X and Y axis .
     * 
     */
    private static JToolBar getEncodingToolbar(final Mashup sp,
            final String xfield, final String yfield, final String sfield)
    {
        int spacing = 10;
        Table t = (Table)sp.getVisualization().getSourceData(group);
        String[] colnames = new String[t.getColumnCount()];
        for ( int i=0; i<colnames.length; ++i )
            colnames[i] = t.getColumnName(i);
        
        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.add(Box.createHorizontalStrut(spacing));
        
        final JComboBox xcb = new JComboBox(colnames);
        xcb.setSelectedItem(xfield);
        xcb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Visualization vis = sp.getVisualization();
                AxisLayout xaxis = (AxisLayout)vis.getAction("x");
                xaxis.setDataField((String)xcb.getSelectedItem());
                vis.run("draw");
            }
        });
        toolbar.add(new JLabel("X: "));
        toolbar.add(xcb);
        toolbar.add(Box.createHorizontalStrut(2*spacing));
        
        final JComboBox ycb = new JComboBox(colnames);
        ycb.setSelectedItem(yfield);
        ycb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Visualization vis = sp.getVisualization();
                AxisLayout yaxis = (AxisLayout)vis.getAction("y");
                yaxis.setDataField((String)ycb.getSelectedItem());
                vis.run("draw");
            }
        });
        toolbar.add(new JLabel("Y: "));
        toolbar.add(ycb);
        toolbar.add(Box.createHorizontalStrut(2*spacing));
        
        final JComboBox scb = new JComboBox(colnames);
        scb.setSelectedItem(sfield);
        scb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Visualization vis = sp.getVisualization();
                DataColorAction s = (DataColorAction)vis.getAction("color");
                s.setDataField((String)scb.getSelectedItem());
                vis.run("draw");
            }
        });
        toolbar.add(new JLabel("Colour: "));
        toolbar.add(scb);
        toolbar.add(Box.createHorizontalStrut(spacing));
        toolbar.add(Box.createHorizontalGlue());
        
        return toolbar;
    }

    
    public int getPointSize() {
        return m_shapeR.getBaseSize();
    }
    
    public void setPointSize(int size) {
        m_shapeR.setBaseSize(size);
        vis.repaint();
    }
    
    private Component getDisplay() {
		return d;
	}

	public static Mashup demo(Table t, String xfield, String yfield) {
        return demo(t, xfield, yfield, null);
    }
    
    public static Mashup demo(Table t, String xfield,
                                   String yfield, String sfield)
    {
        Mashup scatter = new Mashup(t, xfield, yfield, sfield);
        scatter.setPointSize(10);
        return scatter;
    }
    

	protected Visualization getVisualization() {
		return vis;
	}
    
}

package ca.cgjennings.apps.arkham.plugins;

import ca.cgjennings.apps.arkham.ToolWindow;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Displays memory use as a graph; content for the memory use tool window.
 */
final class MemoryGraph extends JComponent implements MouseListener {

    private static final Color BACKGROUND = Color.DARK_GRAY;
    private static final Color X_GRID_COLOR = Color.GRAY;
    private static final Color Y_GRID_COLOR = Color.GRAY;

    private static final Paint MEMORY_GRAPH = new LinearGradientPaint(
            0f, 0f, 0f, 1f, new float[]{0f, 0.1f, 1f},
            new Color[]{Color.WHITE, Color.YELLOW, Color.ORANGE.darker()}
    );

    private static final BasicStroke QUARTER_STROKE = new BasicStroke(
            1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
            1f, new float[]{2f, 2f}, 0f
    );

    private static final int SAMPLES = 60;
    private static final int LAST_SAMP = SAMPLES - 1;

    private static final int WIDTH = SAMPLES * 3;
    private static final int HEIGHT = 64;

    private double[] heapUse = new double[SAMPLES];
    private double maxMem = 0d;

    private ToolWindow owner;
    private MemoryReadings readings;

    public MemoryGraph(ToolWindow tw, MemoryReadings readings) {
        setOpaque(true);
        Dimension size = new Dimension(WIDTH, HEIGHT);
        setPreferredSize(size);
        setSize(size);
        addMouseListener(this);
        owner = tw;

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        this.readings = readings;
        updateTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;
        AffineTransform oldAT = g.getTransform();

        final int w = getWidth();
        final int h = getHeight();

        g.setPaint(BACKGROUND);
        g.fillRect(0, 0, w, h);

        g.scale((double) w / (SAMPLES - 1), (double) h);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (memShape == null) {
            memShape = makeGraph(heapUse, maxMem, true);
            g.setPaint(MEMORY_GRAPH);
            g.fill(memShape);
        }

        // DRAW X-AXIS GRID
        Composite oldComp = g.getComposite();
        Stroke oldStroke = g.getStroke();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        final float onePixelWide = (SAMPLES - 1) / w;
        g.setStroke(new BasicStroke(onePixelWide));
        g.setPaint(X_GRID_COLOR);

        g.setComposite(gridComp);
        for (int s = SAMPLES; s > 0; s -= 5) {
            g.drawLine(s, 0, s, 1);
        }

        g.setTransform(oldAT);

        // DRAW Y-AXIS GRID
        g.setPaint(Y_GRID_COLOR);
        final float hf = (float) h;
        g.setStroke(QUARTER_STROKE);
        int y = (int) (hf / 4f + 0.5f);
        g.drawLine(0, y, w, y);
        y = (int) (hf * 3f / 4f + 0.5f);
        g.drawLine(0, y, w, y);

        g.setStroke(oldStroke);
        y = (int) (hf / 2f + 0.5f);
        g.drawLine(0, y, w, y);

        g.setComposite(oldComp);
        // DRAW MEMORY USE
    }

    private AlphaComposite gridComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f);

    private void takeSample() {
        roll(heapUse);

        MemoryUsage hUse = memBean.getHeapMemoryUsage();
        MemoryUsage nUse = memBean.getNonHeapMemoryUsage();

        double mem = hUse.getUsed() + nUse.getUsed();
        maxMem = Math.max(mem, maxMem);
        heapUse[LAST_SAMP] = mem;

        // old shapes are now invalid
        memShape = null;

        if (owner.isVisible()) {
            repaint();
            if (readings != null) {
                readings.updateStats();
            }
        }
    }

    private Shape memShape;

    private void roll(double[] samples) {
        for (int i = 1; i < SAMPLES; ++i) {
            samples[i - 1] = samples[i];
        }
    }

    private Shape makeGraph(double[] samples, double max, boolean solid) {
        Path2D.Double path = new Path2D.Double();
        if (max == 0) {
            return path;
        }

        if (solid) {
            path.moveTo(0d, 1d);
        } else {
            path.moveTo(0d, 1d - samples[0] / max);
        }

        for (int s = 0; s < samples.length; ++s) {
            path.lineTo((double) s, 1d - samples[s] / max);
        }
        path.lineTo((double) LAST_SAMP, 1d);
        path.closePath();

        return path;
    }

    private MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();

    private Timer updateTimer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            takeSample();
        }
    });

    public void dispose() {
        updateTimer.stop();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (clickTimer != null) {
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        clickTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                memBean.gc();
                memBean.gc();
                if (count++ == 3) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    clickTimer.stop();
                    clickTimer = null;
                }
            }
            private int count = 0;
        });
        clickTimer.start();
    }
    private Timer clickTimer;

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mouseOver = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseOver = false;
    }

    private boolean mouseOver;
}
/*

package ca.cgjennings.seplugins;

import ca.cgjennings.apps.arkham.AppFrame;
import ca.cgjennings.apps.arkham.StrangeEonsApplication;
import ca.cgjennings.apps.arkham.plugins.Plugin;
import ca.cgjennings.apps.arkham.plugins.PluginContext;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.ToolTipManager;


public class MemoryMonitor extends JLabel implements Plugin {

	public MemoryMonitor() {
		super();
		//setBackground( BACKGROUND );
		setForeground( FOREGROUND );
		setHorizontalAlignment( LEFT );
		setVerticalTextPosition( TOP );
		setHorizontalTextPosition( LEFT );

		float fontSize = TEXT_SIZE;
		if( fontSize > WIDGET_HEIGHT/2 ) fontSize = WIDGET_HEIGHT/2;
		setFont( getFont().deriveFont( fontSize ) );
		setOpaque( false );

		setBorder( BorderFactory.createEmptyBorder() );

		setIcon( new Icon() {
			@Override
			public void paintIcon( Component c, Graphics g, int x, int y ) {
			}

			@Override
			public int getIconWidth() {
				return WIDGET_HEIGHT;
			}

			@Override
			public int getIconHeight() {
				return WIDGET_HEIGHT;
			}
		});

		JPopupMenu menu = new JPopupMenu();
		menu.add( new AbstractAction( "Collect Garbage" ) {
			@Override
			public void actionPerformed( ActionEvent e ) {
				for( int i=0; i<5; ++i ) System.gc();
			}
		});
		menu.add( new AbstractAction( "Clear Image Cache " ) {
			@Override
			public void actionPerformed( ActionEvent e ) {
				resources.ResourceKit.clearImageCache();
			}
		});
		setComponentPopupMenu( menu );

		addMouseListener( new MouseListener() {

			@Override
			public void mouseClicked( MouseEvent e ) {
				if( e.getButton() == MouseEvent.BUTTON1 ) {
					if( (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0 ) {
						clearImageCache();
					} else {
						collectGarbage();
					}
				}
			}

			@Override
			public void mousePressed( MouseEvent e ) {
			}

			@Override
			public void mouseReleased( MouseEvent e ) {
			}

			@Override
			public void mouseEntered( MouseEvent e ) {
				entered = true;
				ToolTipManager.sharedInstance().setInitialDelay( 0 );
				ToolTipManager.sharedInstance().setDismissDelay( 6000 );
				repaint();
			}

			@Override
			public void mouseExited( MouseEvent e ) {
				entered = false;
				ToolTipManager.sharedInstance().setInitialDelay( initialDelay );
				ToolTipManager.sharedInstance().setDismissDelay( dismissDelay );
				repaint();
			}
		});
	}

	private boolean entered = false;
	private int initialDelay = ToolTipManager.sharedInstance().getInitialDelay();
	private int dismissDelay = ToolTipManager.sharedInstance().getDismissDelay();

	private MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();

	public void collectGarbage() {
		for( int i=0; i<2; ++i ) System.gc();
	}

	public void clearImageCache() {
		resources.ResourceKit.clearImageCache();
	}

	@Override
	public String getPluginName() {
		return "Memory Monitor";
	}

	@Override
	public String getPluginDescription() {
		return "Adds a memory monitor widget to the top of the application window";
	}

	@Override
	public int getPluginType() {
		return Plugin.INJECTED;
	}

	@Override
	public int getPluginVersion() {
		return 7;
	}

	@Override
	public boolean initializePlugin( PluginContext context ) {
		return true;
	}

//	private Insets insets = null;
	@Override
	protected void paintComponent( Graphics g ) {
		if( samples != null ) {
//			insets = getInsets( null );
//			Shape clip = g.getClip();
//			try {
//				g.clipRect( insets.left, insets.top, getWidth()-insets.left-insets.right, getHeight()-insets.top-insets.bottom );
				paintGraph( (Graphics2D) g );
//			} finally {
//				g.setClip( clip );
//			}
		}
		super.paintComponent( g );
	}

	private static final int EDGE = 1;

	protected void paintGraph( Graphics2D g ) {
		Polygon p1 = new Polygon();
		Polygon p2 = new Polygon();

		int maxy = getHeight();
		int height = (maxy+1) - GRAPH_INSET*2;
		int width = getWidth() - GRAPH_INSET*2 - EDGE*2;

		double sampleWidth = (double) (getWidth() - GRAPH_INSET*2 - EDGE*2) / (double) NUM_SAMPLES;
		double x = GRAPH_INSET+EDGE;
		p1.addPoint( GRAPH_INSET+EDGE, maxy - (int) (samples[0] * height) );
		for( int i=0; i<NUM_SAMPLES; ++i ) {
			x += sampleWidth;
			p1.addPoint( (int) x, maxy - (int) (samples[i] * height) );
		}
		p1.addPoint( (int) x, maxy );
		p1.addPoint( GRAPH_INSET+EDGE, maxy );

		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		g.setPaint( getBackground() );
		g.fillRect(  0, 0, getWidth(), getHeight() );

		g.setPaint( BACKGROUND );
		g.fillRect( GRAPH_INSET+EDGE, GRAPH_INSET, width, height );

		LinearGradientPaint lgp = new LinearGradientPaint(
				0, GRAPH_INSET, 0, GRAPH_INSET+height, GRADIENT,
				entered ? ACTIVE_COLORS : GRAPH_COLORS,
				LinearGradientPaint.CycleMethod.NO_CYCLE
		);
		g.setPaint( lgp );
		g.fill( p1 );

		g.setPaint( GRID_COLOR );
		g.drawLine( GRAPH_INSET+EDGE, GRAPH_INSET + height/4, width, GRAPH_INSET + height/4 );
		g.drawLine( GRAPH_INSET+EDGE, GRAPH_INSET + height/2, width, GRAPH_INSET + height/2 );
		g.drawLine( GRAPH_INSET+EDGE, GRAPH_INSET + height*3/4, width, GRAPH_INSET + height*3/4 );

		int divWidth = height/2;
		int divx = divWidth/2 + EDGE + GRAPH_INSET;
		for( ; divx < width; divx += divWidth ) {
			g.drawLine( divx, GRAPH_INSET, divx, height );
		}

		g.setPaint( LOAD_COLOR );
		g.draw( p2 );
	}

	private static final Color BACKGROUND = new Color( 0x050522 );
	private static final Color FOREGROUND = Color.WHITE;

	private static final Color GRAPH_COLOR1 = new Color( 0x99bbff );
	private static final Color GRAPH_COLOR2 = new Color( 0x666677 );
	private static final Color ACTIVE_COLOR1 = new Color( 0xddeeff );
	private static final Color ACTIVE_COLOR2 = new Color( 0x99bbff );

	private static final Color[] GRAPH_COLORS = new Color[] { GRAPH_COLOR2, GRAPH_COLOR1 };
	private static final Color[] ACTIVE_COLORS = new Color[] { ACTIVE_COLOR1, ACTIVE_COLOR2 };
	private static final float[] GRADIENT = new float[] { 0f, 1f };

	private static final Color GRID_COLOR = new Color( 0x0a0a44 );
	private static final Color LOAD_COLOR = new Color( 0xffffcc );

	@Override
	public boolean isPluginShowing() {
		return true;
	}

	@Override
	public void showPlugin( PluginContext context, boolean show ) {
		if( show == false ) return;
		if( context.getParentFrame() == null ) return;

		// store a reference to the application so we can remove the component later
		eons = context.getApplication();
		eons.addCustomComponent( this );

		ActionListener timerEvent = new ActionListener() {
			private int run = 0;
			@Override
			public void actionPerformed( ActionEvent e ) {
				// work around for case book player  issues
				if( run++ > 0 && (!isShowing() || !AppFrame.getApp().isActive()) ) return;

				MemoryUsage heap = memBean.getHeapMemoryUsage();

				double used = (double) heap.getUsed() / BYTES_PER_MB;
				double committed = (double) heap.getCommitted() / BYTES_PER_MB;
				double max = (double) heap.getMax() / BYTES_PER_MB;
				if( max < 0 ) {
					max = Double.POSITIVE_INFINITY;
				}

				double percentInUse = used / max;
				if( samples == null ) {
					 samples = new double[ NUM_SAMPLES ];
					 for( int i=1; i<NUM_SAMPLES; ++i ) {
						 samples[i] = percentInUse;
					 }
				}

				for( int i=1; i<NUM_SAMPLES; ++i ) {
					samples[i-1] = samples[i];
				}
				samples[ NUM_SAMPLES-1 ] = percentInUse;

				double vram = getGraphicsConfiguration().getDevice().getAvailableAcceleratedMemory() / BYTES_PER_MB;

				setText(
						String.format( "  %,.1f/%,.1f MiB", used, max )
				);

				StringBuilder ttText = new StringBuilder();

				ttText.append( "<html><table cellspacing=0 cellpadding=0 style='font-size: 10pt' border=0>" );
				ttText.append( String.format( "<tr><td align=right>%,.1f MiB&nbsp;</td><td>in use (%.0f%%)</td></tr>", used, percentInUse * 100d ) );
				ttText.append( String.format( "<tr><td align=right>%,.1f MiB&nbsp;</td><td>allocated</td></tr>", committed ) );
				ttText.append( String.format( "<tr><td align=right>%,.1f MiB&nbsp;</td><td>maximum</td></tr>", max ) );

				if( vram >= 0 ) {
					ttText.append( String.format( "<tr><td align=right>%,.1f MiB&nbsp;</td><td>free Video RAM</td></tr>", vram ) );
				}

				ttText.append( String.format( "<tr><td align=right>%,d&nbsp;</td><td>cached images</td></tr>", resources.ResourceKit.getCachedImageCount() ) );

				ClassLoadingMXBean clb = ManagementFactory.getClassLoadingMXBean();
				ttText.append( String.format( "<tr><td align=right>%,d&nbsp;</td><td>loaded classes</td></tr>", clb.getLoadedClassCount() ) );
				CompilationMXBean compb = ManagementFactory.getCompilationMXBean();
				if( compb.isCompilationTimeMonitoringSupported() ) {
					long compTime = compb.getTotalCompilationTime();
					ttText.append( String.format( "<tr><td align=right>%,d ms&nbsp;</td><td>compilation time</td></tr>", compTime ) );
				}

				ttText.append( "</td></tr></table>" );


//				ttText.append(  String.format( "<html>%,.1f/%,.1f MiB (%.0f%%), %,.1f MiB allocated<br>%d cached images",
//						used, max, percentInUse * 100d, committed, resources.ResourceKit.getCachedImageCount()
//				) );
//				if( vram >= 0 ) {
//					ttText.append( String.format( ", %,.1f MiB free VRAM", vram ) );
//				}
//				ClassLoadingMXBean clb = ManagementFactory.getClassLoadingMXBean();
//				ttText.append( String.format( "<br>%,d/%,d current/total classes", clb.getLoadedClassCount(), clb.getTotalLoadedClassCount() ) );
//
//				CompilationMXBean compb = ManagementFactory.getCompilationMXBean();
//				if( compb.isCompilationTimeMonitoringSupported() ) {
//					long compTime = compb.getTotalCompilationTime();
//					ttText.append( String.format( " (%,d ms compile time)", compTime ) );
//				}


				setToolTipText( ttText.toString() );
				//repaint();
			}
		};

		updateTimer = new Timer( UPDATE_RATE, timerEvent );
		updateTimer.start();
		// run once to initialize the content and size
		timerEvent.actionPerformed( null );
		Dimension d = getPreferredSize();
		d.width += 24;
		setPreferredSize( d );
		setMinimumSize( d );
		setMaximumSize( d );
	}

	@Override
	public void unloadPlugin() {
		if( updateTimer != null ) {
			updateTimer.stop();
			updateTimer = null;
		}
		if( eons != null ) {
			eons.removeCustomComponent( this );
			eons = null;
		}
		samples = null;
	}

	private Timer updateTimer;
	private StrangeEonsApplication eons;
	private double[] samples;

	private static double BYTES_PER_MB = 1024*1024;
	// insets from edge of component to graph
	private static final int GRAPH_INSET = 1;
	// minimum height for the widget (ensures some space is available for the graph)
	private static final int WIDGET_HEIGHT = 30;
	// number of samples to display in the graph
	private static final int NUM_SAMPLES = 30;
	// update the graph and text every UPDATE_RATE milliseconds
	private static final int UPDATE_RATE = 750;
	// maximum point size of memory status text
	private static final int TEXT_SIZE = 12;
}

 */

package com.dauryg;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.media.opengl.*;
import static javax.media.opengl.GL2.*;
import javax.media.opengl.awt.*;
import javax.swing.*;
import java.io.File;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.awt.TextRenderer;

public class JViewer extends JFrame implements KeyListener ,
		MouseWheelListener , ActionListener , GLEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GLJPanel canvas;
	private JMenuBar menuBar;
	private JMenu menu;
	private JMenuItem open;
	private JMenuItem exit;
	private OBJModel model;
	private Animator animator;
	private float x , y , zoomfactor , z;
	private double diameter;
	private Vector center;
	private Bounds b;
	private TextRenderer text;
	private double depth;
	
	public JViewer() {
		super( "JViewer" );
		zoomfactor = 1.0f;
		x = 0; y = 0; z = 0;
		diameter = 1f;
		depth = 1f;
		text = new TextRenderer( new Font( "arial" , Font.PLAIN , 16 ) );
		init();
	}

	@Override
	public void display( GLAutoDrawable drawable )
	{
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

		text.beginRendering( canvas.getWidth() , canvas.getHeight() );
		text.setColor( 0.0f , 0.0f , 0.0f , 1.0f );
		text.draw( "Instructions" , 10 , 590 );
		text.draw( "Space: Toggle Bounding Box" , 10 , 570 );
		text.draw( "Arrow Keys: Rotate Model" , 10 , 550 );
		text.draw( "Mouse Scroll Wheel/ (+/-): Zoom" , 10 , 530 );
		text.endRendering();
		
		if ( model != null )
		{
			gl.glEnable( GL_DEPTH_TEST );
			gl.glMatrixMode( GL_PROJECTION );
			gl.glLoadIdentity();
			gl.glOrtho( -diameter * zoomfactor , diameter * zoomfactor ,
						-diameter * zoomfactor , diameter * zoomfactor , 1 , 1000 );
			gl.glMatrixMode( GL_MODELVIEW );
			gl.glLoadIdentity();
			gl.glTranslated( 0 , 0 , depth );
			gl.glRotatef( x , 1 , 0 , 0 );
			gl.glRotatef( y , 0 , 1 , 0 );
			gl.glTranslated( -center.x , -center.y , -center.z );
			model.draw( gl );
		}

		gl.glFlush();
	}

	@Override
	public void init( GLAutoDrawable drawable )
	{
		GL2 gl = drawable.getGL().getGL2();
		gl.glClearColor( 1.0f , 1.0f , 1.0f , 1.0f );
		gl.glEnable( GL.GL_DEPTH_TEST );
		gl.setSwapInterval( 0 );
	}

	@Override
	public void reshape( GLAutoDrawable drawable , int x , int y , int width , int height ){}

	@Override
	public void actionPerformed( ActionEvent e )
	{
		if ( e.getSource() == open )
		{
			JFileChooser fc = new JFileChooser();

			// In response to a button click:
			int returnVal = fc.showOpenDialog( null );

			File f;

			if ( returnVal == JFileChooser.APPROVE_OPTION )
			{
				f = fc.getSelectedFile();
				loadModel( f );
			}

		}
		else if ( e.getSource() == exit )
		{
			SwingUtilities.invokeLater( new Runnable()
			{
				@Override
				public void run()
				{
					System.exit( 0 );
				}
			} );
		}
	}

	@Override
	public void mouseWheelMoved( MouseWheelEvent e )
	{
		zoomfactor += e.getWheelRotation() * .1f;
		
		if( zoomfactor <  0f )
		{
			zoomfactor = 0f;
		}
	}

	@Override
	public void keyPressed( KeyEvent e )
	{
		if ( e.getKeyCode() == KeyEvent.VK_UP )
		{
			x -= 5;
		}

		if ( e.getKeyCode() == KeyEvent.VK_DOWN )
		{
			x += 5;
		}

		if ( e.getKeyCode() == KeyEvent.VK_LEFT )
		{
			y -= 5;
		}

		if ( e.getKeyCode() == KeyEvent.VK_RIGHT )
		{
			y += 5;
		}

		if ( e.getKeyCode() == KeyEvent.VK_SPACE )
		{
			if( model != null )
				model.toggleBBox();
		}

		if ( e.getKeyCode() == KeyEvent.VK_ADD )
		{
			zoomfactor -= .1;
			System.out.println( "New Zoom factor: " + zoomfactor );
		}

		if ( e.getKeyCode() == KeyEvent.VK_SUBTRACT )
		{
			zoomfactor += .1;
			System.out.println( "New Zoom factor: " + zoomfactor );
		}
	}

	@Override
	public void keyReleased( KeyEvent e )
	{
		if ( e.getKeyCode() == KeyEvent.VK_Q )
		{
			z--;
			System.out.println( z );
		}

		if ( e.getKeyCode() == KeyEvent.VK_A )
		{
			z++;
			System.out.println( z );
		}
	}

	public void init()
	{
		menuBar = new JMenuBar();
		menu = new JMenu( "Menu" );
		open = new JMenuItem( "Open" );
		exit = new JMenuItem( "Exit" );

		open.addActionListener( this );
		exit.addActionListener( this );
		addMouseWheelListener( this );

		menu.add( open );
		menu.add( exit );
		menuBar.add( menu );

		add( menuBar , BorderLayout.NORTH );

		canvas = new GLJPanel();
		canvas.setPreferredSize( new Dimension( 600 , 600 ) );
		canvas.addGLEventListener( this );
		add( canvas , BorderLayout.CENTER );
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		pack(); // Make sure everything gets sized correctly
		setVisible( true );
		setLocationRelativeTo( null ); // Center the app on the monitor
		addKeyListener( this ); // Handle input
		setResizable( false );

		center = new Vector();
		animator = new Animator( canvas );
		animator.start();
	}

	public void loadModel( File f )
	{
		System.out.print( "Loading file: " + f.toString() + "\n" );

		JProgressBar bar = new JProgressBar();
		bar.setIndeterminate( true );

		JDialog d = new JDialog( this );
		d.add( bar );
		d.setLocationRelativeTo( null );
		d.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		d.setVisible( true );
		d.pack();

		ObjReader r = new ObjReader();
		model = r.parseFile( f );
		
		if( model != null)
		{
			Bounds b = model.getBBox();
			center = b.getCenter();
			diameter = Math.round( b.getDiameter() );
			depth = -diameter * 2;

			d.setVisible( false );
			System.out.println( "left: " + ( -diameter * zoomfactor ) + " right:"
					+ ( diameter * zoomfactor ) + " bottom: "
					+ ( -diameter * zoomfactor ) + " top: "
					+ ( diameter * zoomfactor ) + " near: " + 1 + " far: "
					+ ( diameter * 2 ) );
			System.out.println( "Diameter: " + diameter );
		}
	}

	public static void main( String[] args )
	{
		String file = null;
		JViewer j = new JViewer();

		if ( args != null && args.length > 0 )
		{
			file = args[0];
			j.loadModel( new File( file ) );
		}
	}

	@Override
	public void keyTyped( KeyEvent e ){}
	
	@Override
	public void dispose( GLAutoDrawable arg0 ){}
}
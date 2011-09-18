package com.dauryg;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.texture.*;

import javax.imageio.ImageIO;

public class ObjReader {
	
	ArrayList< Vertex >		vertices;
	ArrayList< Vertex > 	normals;
	ArrayList< Tex > 		uv;
	ArrayList< Integer > 	indexes;
	ArrayList< Vertinfo > 	vertInfo;
	ArrayList< IntBuffer > 	faces;
	BufferedReader 			reader;
	File 					file;
	Vector 					min;
	Vector 					max;
	OBJMat 					mat;

	public OBJModel parseFile( File file )
	{
		try
		{
			if( file.exists() == false )
			{
				throw new FileNotFoundException( "!!! " + file.getName() + " Not Found !!!");
			}
			
			this.file = file;
			min = new Vector();
			max = new Vector();
			reader = new BufferedReader( new FileReader( file ) );
			vertices = new ArrayList< Vertex >();
			normals = new ArrayList< Vertex >();
			uv = new ArrayList< Tex >();
			indexes = new ArrayList< Integer >();
			vertInfo = new ArrayList< Vertinfo >();
			faces = new ArrayList< IntBuffer >();
			String line;

			while ( ( line = reader.readLine() ) != null )
			{
				line = line.trim();
				String[] token = line.split( "\\b\\s" );

				// empty line
				if ( line.isEmpty() || token.length == 0 )
					continue;

				String lineType = token[0];

				// Vertice
				if ( lineType.equalsIgnoreCase( "v" ) )
				{
					readVertex( token );
					// Normal
				}
				else if ( lineType.equalsIgnoreCase( "vn" ) )
				{
					readNormal( token );
					// Texture
				}
				else if ( lineType.equalsIgnoreCase( "vt" ) )
				{
					readTexCoor( token );
					// Face
				}
				else if ( lineType.equalsIgnoreCase( "f" ) )
				{
					readFace( token );
					// MTL file
				}
				else if ( lineType.equalsIgnoreCase( "mtllib" ) )
				{
					parseMTL( new File( file.getParent() + '/' + token[1] ) );
				}
			}
			reader.close();
			System.out.println( "Total Vertices: " + vertices.size() );
			System.out.println( "Total Normals: " + normals.size() );
			System.out.println( "Total Texture Coor: " + uv.size() );
			System.out.println( "Total index Count: " + indexes.size() );
			
		}
		catch ( FileNotFoundException e )
		{
			e.printStackTrace();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
		
		if( file.exists() )
		{
			return buildModel();
		}
		else
		{
			return null;
		}
	}

	private void readVertex( String[] tokens )
	{
		Vertex v = new Vertex();
		v.x = Float.parseFloat( tokens[1] );
		v.y = Float.parseFloat( tokens[2] );
		v.z = Float.parseFloat( tokens[3] );
		vertices.add( v );

		// Check the bounding values
		if ( v.x < min.x )
			min.x = v.x;
		if ( v.y < min.y )
			min.y = v.y;
		if ( v.z < min.z )
			min.z = v.z;

		if ( v.x > max.x )
			max.x = v.x;
		if ( v.y > max.y )
			max.y = v.y;
		if ( v.z > max.z )
			max.z = v.z;
	}

	private void readNormal( String[] tokens )
	{
		Vertex n = new Vertex();
		n.x = Float.parseFloat( tokens[1] );
		n.y = Float.parseFloat( tokens[2] );
		n.z = Float.parseFloat( tokens[3] );
		normals.add( n );
	}

	private void readTexCoor( String[] tokens )
	{
		Tex t = new Tex();
		t.u = Float.parseFloat( tokens[1] );
		t.v = Float.parseFloat( tokens[2] );
		uv.add( t );
	}

	private void readFace( String[] tokens )
	{

		int[] face = new int[ tokens.length - 1 ];

		for ( int i = 1; i < tokens.length; i++ )
		{

			String[] info = tokens[i].split( "/" );
			Vertinfo vertinfo = new Vertinfo();

			// Vertex
			int vert = Integer.parseInt( info[0] );

			if ( vert < 0 )
				vertinfo.vert = vertices.size() + vert;
			else
				vertinfo.vert = vert - 1;

			// Texture uv coordinates
			if ( info.length >= 2 && !info[1].isEmpty() )
			{

				int tex = Integer.parseInt( info[1] );

				if ( tex < 0 )
					vertinfo.uv = uv.size() + tex;
				else
					vertinfo.uv = tex - 1;
			}

			// Normal
			if ( info.length >= 3 && !info[2].isEmpty() )
			{

				int norm = Integer.parseInt( info[2] );

				if ( norm < 0 )
					vertinfo.normal = normals.size() + norm;
				else
					vertinfo.normal = norm - 1;
			}

			if ( vertInfo.contains( vertinfo ) )
			{
				indexes.add( vertInfo.indexOf( vertinfo ) );
				face[i - 1] = vertInfo.indexOf( vertinfo );
			}
			else
			{
				vertInfo.add( vertinfo );
				indexes.add( vertInfo.indexOf( vertinfo ) );
				face[i - 1] = vertInfo.indexOf( vertinfo );
			}
		}

		IntBuffer f = GLBuffers.newDirectIntBuffer( face );

		faces.add( f );
	}

	private void parseMTL( File file ) throws IOException
	{

		if ( !file.exists() )
		{
			System.out.println( "File not found: " + file.getPath() );
			return;
		}
		
		System.out.println( "MTL File found: " + file.getPath() );
		BufferedReader reader = new BufferedReader( new FileReader( file ) );
		String line;
		mat = new OBJMat();

		while ( ( line = reader.readLine() ) != null )
		{

			line = line.trim();
			String[] tokens = line.split( "\\b\\s" );

			if ( tokens[0].equalsIgnoreCase( "newmtl" ) )
			{
				mat.name = tokens[0];
			}
			else if ( tokens[0].equalsIgnoreCase( "map_Kd" ) )
			{
				File texfile = new File( file.getParent() + "/" + tokens[1] );

				if ( !file.exists() )
					System.out.println( "File not found: " + texfile.getPath() );
				else
				{
					BufferedImage img = ImageIO.read( texfile );
					ImageUtil.flipImageVertically( img );
					mat.img = img;
					System.out.println( "Diffuse File found: " + texfile );
				}
			}
		}
	}

	private OBJModel buildModel()
	{
		int[] 					i			= null;
		float[] 				v			= null;
		float[] 				n			= null;
		float[] 				tex 		= null;
		ArrayList< Float >		vert		= new ArrayList< Float >();
		ArrayList< Float > 		texcoord	= new ArrayList< Float >();
		ArrayList< Float > 		norm		= new ArrayList< Float >();
		Iterator< Vertinfo >	iterator	= vertInfo.iterator();

		while ( iterator.hasNext() )
		{
			Vertinfo	vertinfo	= iterator.next();
			Vertex		vertex		= vertices.get( vertinfo.vert );
			
			vert.add( vertex.x );
			vert.add( vertex.y );
			vert.add( vertex.z );

			if ( uv.size() > 0 )
			{
				Tex t = uv.get( vertinfo.uv );
				texcoord.add( t.u );
				texcoord.add( t.v );
			}

			if ( normals.size() > 0 )
			{
				Vertex normal = normals.get( vertinfo.normal );
				norm.add( normal.x );
				norm.add( normal.y );
				norm.add( normal.z );
			}
		}

		Float[] f = vert.toArray( new Float[ 0 ] );
		v = new float[ vert.size() ];
		for ( int ii = 0; ii < vert.size(); ii++ )
			v[ii] = f[ii];

		if ( uv.size() > 0 )
		{
			f = texcoord.toArray( new Float[ 0 ] );
			tex = new float[ texcoord.size() ];
			for ( int ii = 0; ii < texcoord.size(); ii++ )
				tex[ii] = f[ii];
		}

		if ( normals.size() > 0 )
		{
			f = norm.toArray( new Float[ 0 ] );
			n = new float[ norm.size() ];
			for ( int ii = 0; ii < norm.size(); ii++ )
				n[ii] = f[ii];
		}

		Integer[] in = indexes.toArray( new Integer[ 0 ] );
		i = new int[ indexes.size() ];
		for ( int ii = 0; ii < indexes.size(); ii++ )
			i[ii] = in[ii];

		IntBuffer[] face = faces.toArray( new IntBuffer[ 0 ] );
		OBJModel m = new OBJModel( v , face , min , max );

		if ( n != null )
			m.addNormals( n );

		if ( mat != null )
			m.addMaterial( mat );

		if ( tex != null )
			m.addUV( tex );

		return m;
	}

	private class Vertex {
		float x , y , z;

		@Override
		public boolean equals( Object o )
		{

			if ( o == null )
				return false;
			if ( o == this )
				return true;
			if ( !( o instanceof Vertex ) )
				return false;

			Vertex v = (Vertex) o;
			return x == v.x && y == v.y && z == v.z;
		}
	}

	private class Tex {
		float u , v;

		@Override
		public boolean equals( Object o )
		{

			if ( o == null )
				return false;
			if ( o == this )
				return true;
			if ( !( o instanceof Tex ) )
				return false;

			Tex t = (Tex) o;
			return u == t.u && v == t.v;
		}
	}

	private class Vertinfo {
		int vert = -1;
		int normal = -1;
		int uv = -1;
		String mat;

		@Override
		public boolean equals( Object o )
		{
			if ( o == null )
				return false;
			if ( o == this )
				return true;
			if ( !( o instanceof Vertinfo ) )
				return false;

			Vertinfo v = (Vertinfo) o;

			return ( vert == v.vert ) && ( normal == v.normal )
					&& ( uv == v.uv ) && ( mat == v.mat );
		}
	}

	class OBJMat {
		String name;
		Texture diffuse_texture;
		BufferedImage img;
	}
}

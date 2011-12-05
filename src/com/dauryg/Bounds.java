/*
Copyright (c) 2011 Daury Guzman <daury@dauryguzman.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial
portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.dauryg;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.*;

import com.jogamp.opengl.util.GLBuffers;

public class Bounds {

	private Vector min;
	private Vector max;
	private Vector center;
	private float diameter;
	private FloatBuffer vertices;
	private IntBuffer indices;
	float width , height , depth;

	public Bounds( Vector min , Vector max ) {

		width = Math.abs( ( max.x - min.x ) );
		height = Math.abs( ( max.y - min.y ) );
		depth = Math.abs( ( max.z - min.z ) );
		this.min = min;
		this.max = max;

		float x = ( min.x + max.x ) / 2;
		float y = ( min.y + max.y ) / 2;
		float z = ( min.z + max.z ) / 2;

		center = new Vector( x , y , z );

		float dx = max.x - min.x;
		float dy = max.y - min.y;
		float dz = max.z - min.z;

		diameter = (float) Math.sqrt( dx * dx + dy * dy + dz * dz );

		// a cube is made up of 8 vertices and
		// each vertex contains 3 element (x,y,z)
		// so the size is set to 24
		vertices = GLBuffers.newDirectFloatBuffer( 24 );
		indices = GLBuffers.newDirectIntBuffer( new int[] { 0 , 1 , 2 , 3 , 4 ,
				7 , 6 , 5 , 3 , 4 , 5 , 0 , 7 , 2 , 1 , 6 } );

		//ascii art source: http://www.myhairyass.com/ASCII/Art/
		/*
		 		  v3_________________________v2
		           / _____________________  /|
       		      / / ___________________/ / |
      		  	 / / /| |               / /  |
     		    / / / | |              / / . |
    		   / / /| | |             / / /| |
   		   	  / / / | | |            / / / | |
  		     / / /  | | |           / / /| | |
 		    / /_/__________________/ / / | | |
		v4 /________________________/ /v7| | |
		   | ______________________ | |  | | |
		   | | |    | | |_________| | |__| | |
		   | | | v0 | |___________| | |____| | v1
		   | | |   / / ___________| | |_  / /
		   | | |  / / /           | | |/ / /
		   | | | / / /            | | | / /
		   | | |/ / /             | | |/ /
		   | | | / /              | | ' /
		   | | |/_/_______________| |  /
		   | |____________________| | /
		 v5|________________________|/v6
		
		*/

		vertices.put( min.x );
		vertices.put( min.y );
		vertices.put( min.z ); // v0
		vertices.put( max.x );
		vertices.put( min.y );
		vertices.put( min.z ); // v1
		vertices.put( max.x );
		vertices.put( max.y );
		vertices.put( min.z ); // v2
		vertices.put( min.x );
		vertices.put( max.y );
		vertices.put( min.z ); // v3

		vertices.put( min.x );
		vertices.put( max.y );
		vertices.put( max.z ); // v4
		vertices.put( min.x );
		vertices.put( min.y );
		vertices.put( max.z ); // v5
		vertices.put( max.x );
		vertices.put( min.y );
		vertices.put( max.z ); // v6
		vertices.put( max.x );
		vertices.put( max.y );
		vertices.put( max.z ); // v7
		vertices.rewind();
	}

	public void draw( GL2 gl )
	{
		gl.glPolygonMode( GL2.GL_FRONT_AND_BACK , GL2.GL_LINE );
		gl.glEnableClientState( GL2.GL_VERTEX_ARRAY );
		gl.glVertexPointer( 3 , GL2.GL_FLOAT , 0 , vertices );
		gl.glDrawElements( GL2.GL_QUADS , indices.capacity() , GL2.GL_UNSIGNED_INT , indices );
		gl.glPolygonMode( GL2.GL_FRONT_AND_BACK , GL2.GL_FILL );
	}

	public float getDiameter()
	{
		return diameter;
	}

	public Vector getCenter()
	{
		return center;
	}

	public Vector getMin()
	{
		return min;
	}

	public Vector getMax()
	{
		return max;
	}
}

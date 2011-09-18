package com.dauryg;

import javax.media.opengl.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL3.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.dauryg.ObjReader.OBJMat;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

public class OBJModel extends Model {

	FloatBuffer vertexBuffer;
	FloatBuffer normalBuffer;
	FloatBuffer textureBuffer;
	Bounds bbox;
	Vector min , max;
	boolean texture , normal;
	OBJMat mat;
	boolean hasMat;
	float[] uv;
	IntBuffer[] faces;
	boolean drawBox;

	public OBJModel( float[] vertex , IntBuffer[] faces , Vector min , Vector max ) {
		
		vertexBuffer = GLBuffers.newDirectFloatBuffer( vertex );
		bbox = new Bounds( min , max );
		texture = false;
		normal = false;
		this.faces = faces;
		drawBox = false;
	}

	public Bounds getBBox()
	{
		return bbox;
	}

	public void addNormals( float[] normals )
	{
		normal = true;
		normalBuffer = GLBuffers.newDirectFloatBuffer( normals );
	}

	public void addMaterial( OBJMat mat )
	{
		hasMat = true;
		this.mat = mat;
	}

	public void addUV( float[] uv )
	{
		this.uv = uv;
	}

	public void toggleBBox()
	{
		drawBox = !drawBox;
	}

	@Override
	public void initGL()
	{
		if ( hasMat )
			if ( mat.diffuse_texture == null && mat.img != null && !texture )
			{
				mat.diffuse_texture = AWTTextureIO.newTexture( GLProfile.getDefault() , mat.img , false );
				mat.diffuse_texture.bind();
				mat.diffuse_texture.enable();
				System.out.println( "Texture must be flipped: "	+ mat.diffuse_texture.getMustFlipVertically() );
				texture = true;
				textureBuffer = GLBuffers.newDirectFloatBuffer( uv );
			}
	}

	@Override
	protected void render( GL g )
	{
		GL2 gl = g.getGL2();

		gl.glColor3f( .5f , .5f , .5f );

		// =======================================\\
		// Retained Mode \\
		// =======================================\\
		gl.glEnableClientState( GL2.GL_VERTEX_ARRAY );
		gl.glVertexPointer( 3 , GL2.GL_FLOAT , 0 , vertexBuffer );

		if ( normal )
		{
			gl.glEnableClientState( GL2.GL_NORMAL_ARRAY );
			gl.glNormalPointer( GL2.GL_FLOAT , 0 , normalBuffer );
		}

		if ( texture )
		{
			gl.glEnableClientState( GL2.GL_TEXTURE_COORD_ARRAY );
			gl.glTexCoordPointer( 2 , GL2.GL_FLOAT , 0 , textureBuffer );
		}
		else
			gl.glPolygonMode( GL2.GL_FRONT_AND_BACK , GL2.GL_LINE );

		for ( IntBuffer face : faces )
		{
			if ( face.capacity() == 3 )
				gl.glDrawElements( GL2.GL_TRIANGLES , face.capacity() ,
						GL2.GL_UNSIGNED_INT , face );
			else if ( face.capacity() == 4 )
				gl.glDrawElements( GL2.GL_QUADS , face.capacity() ,
						GL2.GL_UNSIGNED_INT , face );
			else
				gl.glDrawElements( GL2.GL_POLYGON , face.capacity() ,
						GL2.GL_UNSIGNED_INT , face );
		}

		gl.glDisableClientState( GL2.GL_VERTEX_ARRAY );

		if ( normal )
			gl.glDisableClientState( GL2.GL_NORMAL_ARRAY );

		if ( texture )
			gl.glDisableClientState( GL2.GL_TEXTURE_COORD_ARRAY );

		if ( drawBox )
			bbox.draw( gl );
	}
}
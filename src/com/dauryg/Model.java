package com.dauryg;

import javax.media.opengl.*;

public abstract class Model {

	private boolean glDataInitialized;

	/**
	 * Initialize any opengl data needed i.e. vbo's, shader's, textbinding's
	 */
	public abstract void initGL();

	public final void init()
	{
		if ( Threading.isOpenGLThread() )
		{
			initGL();
			glDataInitialized = true;
		}
		else
		{
			throw new GLException( "Not an OpenGL Thread" );
		}
	}

	protected abstract void render( GL gl );

	public final void draw( GL gl )
	{
		if ( glDataInitialized )
		{
			render( gl );
		}
		else
		{
			init();
		}
	}
}

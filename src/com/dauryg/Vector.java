package com.dauryg;

public class Vector {

	public float x , y , z;

	public Vector() {
		x = 0;
		y = 0;
		z = 0;
	}

	public Vector( Vector v ) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public Vector( float x , float y , float z ) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
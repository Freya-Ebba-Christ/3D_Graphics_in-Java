import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Hashtable;

/**
   Z-Buffer Klasse
**/
class Zbuffer {

	Dimension size;
	int buffer[];
	double far;

	public Zbuffer(Dimension s) {
		size = s; far = 5000.;
		buffer = new int[size.width * size.height]; //Z-Buffer anlegen...Array ist immer noch das Schnellste
	}
/*
* Tiefe überprüfen (siehe Z-Buffer Pseudocode)
*
*/

	public boolean checkDepth(int ix, int iy, double z) {

		if(size.height > iy && size.width > ix &&
		   iy > -1 && ix > -1) {

			int index = iy*size.width+ix;
			double zz=Integer.MAX_VALUE*(z/far);
			int iz = (int)(Integer.MAX_VALUE*(z/far));

			if(iz < buffer[index]) {
				buffer[index] = iz;
				return true;
			}
		}
		return false;
	}
/*
*ZBuffer löschen
*/
public void clearBuffer() {
		for(int i=0; i<size.width*size.height; i++) 
			buffer[i] = Integer.MAX_VALUE;
	}
}

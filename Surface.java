import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

class Surface {
	public Point3 p[][];
	
/*
* Einfacher Konstruktor für die Flächen...
*/
	Surface(){
		p = new Point3[4][4];
		for (int i = 0; i < 4;i++){
			for (int j =0; j< 4;j++){
				p[i][j]=new Point3(0,0,0);
			}
		}
	}
}

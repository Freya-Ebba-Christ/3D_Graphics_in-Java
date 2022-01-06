import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;
class ZbufferedCanvas extends Canvas {
	Dimension appsize;
	Image gs;
	private Image dbImage;
	private Graphics dbGraphics;
	Graphics gr;
	Obj object[];
	int nobjects;
	Point3 ptViewRef = new Point3(0,0,0); // Bezugspunkt
	public double theta, phi, R, cos_theta, cos_phi, sin_theta, sin_phi; // Variablen für die Position eines Punktes
	public double ltheta, lphi, amb = 0.22; //Variablen für die Lichtquelle Ambient ist auf 0.22 voreinegstellt.
	Point prev = new Point(150, 185); //erster prev - Wert...
	Polygon poly;
	int depth[];
	EdgeTable et;
	Point3 light_vect = new Point3();
	Zbuffer zbuf; //z-buffer

	Point origin;
	int linedraw = 0;
	int drag = 0;
	public ZbufferedCanvas() {}
/**
 * Perspepktiventransformation ausführen und nachgucken, ob das Polygon überhaupt sichtbar ist.
 * Mathematische Grundlagen aus Computer Graphics Principles and Practice und Tutorials
 */
void computePlane() {
	
	double pi2rad = Math.PI / 180.;
	cos_theta = Math.cos(theta * pi2rad);
	sin_theta = Math.sin(theta * pi2rad);
	cos_phi = Math.cos(phi * pi2rad);
	sin_phi = Math.sin(phi * pi2rad);
	for (int k = 0; k < nobjects; k++) {
		for (int i = 0; i < object[k].npoints; i++) {
			object[k].tPoint[i] = tranfrm(object[k].point[i]);
		}
		for (int i = 0; i < object[k].nplanes; i++) {
			object[k].plane[i].setFront_or_Back(object[k].tPoint);
		}
	}
}
	double lambert(Point3 nv)
	{
		double bright;
		
		bright = nv.x * light_vect.x + nv.y * light_vect.y + nv.z * light_vect.z; //Lambert Shading wie in Computer Grahics Priciples and Practice 
		if(bright < 0)
			bright = 0;

		return(bright);

	}
/*
*Lichtvektor initialisieren
*/

	void lightdata_init() {
		light_vect.x = -Math.cos(lphi*Math.PI/180) * Math.cos(ltheta*Math.PI/180);
		light_vect.y = -Math.cos(lphi*Math.PI/180) * Math.sin(ltheta*Math.PI/180);
		light_vect.z = -Math.sin(lphi*Math.PI/180);
	 }
	public boolean mouseDown(Event ev, int x, int y) {

					prev = new Point(x,y);
		return true;
	}
// wird ausgelöst, wenn die Maustaste beim Drücken gehalten wir und berechnet dann die neue Position
public boolean mouseDrag(Event ev, int x, int y) {
	if (linedraw == 0 || drag == 1) {
		theta -= (x - prev.x);
		phi += (y - prev.y);
		repaint();
		prev.move(x, y);
	} else {
		theta -= (x - prev.x);
		phi += (y - prev.y);
		drag = 1;
		repaint();
	}
	return true;
}

public boolean mouseUp(Event ev, int x, int y) {

			if(drag == 1) {
				drag = 0;
				repaint();
			}

			return true;
		}
public void paint(Graphics g) {
		computePlane();
		render();
		g.drawImage(gs, 0, 0, this);
	}
/**
 * Da nur noch Flächen vorhanden sind, die zum Betrachter zeigen, haben wir keine Redundanz 
 */
public void render() {
	RGB color = new RGB();
	int red, grn, blu;
	Point3 co = new Point3(); //, norm_vect = new Point3();
	ATT attrib = new ATT();
	if (linedraw == 0 && drag == 0) {
		zbuf.clearBuffer();
	}
	gr.setColor(Color.black);
	gr.fillRect(0, 0, appsize.width, appsize.height);//Bildschirm löschen...
	for (int k = 0; k < nobjects; k++) {
		for (int i = 0; i < object[k].nplanes; i++) {
			poly = new Polygon();
			if (object[k].plane[i].visible) { // Sichtbarkeit prüfen und somit Redundanz verhindern
				depth = new int[object[k].plane[i].npoints + 1];
				co.x = 0;
				co.y = 0;
				co.z = 0;
				for (int j = 0; j < object[k].plane[i].npoints; j++) {
					int index1 = object[k].plane[i].set[j];
					poly.addPoint((int) object[k].tPoint[index1].x + origin.x, origin.y - (int) object[k].tPoint[index1].y);
					depth[j] = (int) (R - object[k].tPoint[index1].z); //Tiefe ermitteln...
					
				}
				if (linedraw == 0 && drag == 0) {
					//Normalisieren:
					co.x /= object[k].plane[i].npoints;
					co.y /= object[k].plane[i].npoints;
					co.z /= object[k].plane[i].npoints;
					//Farbattribute setzen
					attrib.dif.r = object[k].col.r;
					attrib.dif.g = object[k].col.g;
					attrib.dif.b = object[k].col.b;
					attrib.amb.r = (attrib.dif.r * amb);// Umgebungslicht, da wir sonst schwarze flächen bekommen würden...
					attrib.amb.g = (attrib.dif.g * amb);
					attrib.amb.b = (attrib.dif.b * amb);
					color = shading(co, object[k].plane[i].norm_vect, attrib); //lambert shading ausführen
					
// farben für den graphics context berechnen...
					red = (int) (color.r * 255);
					grn = (int) (color.g * 255);
					blu = (int) (color.b * 255);
					gr.setColor(new Color(red, grn, blu));
					poly.addPoint(poly.xpoints[0], poly.ypoints[0]); //Punkte hinzufügen
					depth[object[k].plane[i].npoints] = depth[0];
				
					et = new EdgeTable(poly, depth, zbuf);
//paint aufrufen, damit der Inhalt der KAntentabelle gezeichnet wird				
					et.paint(gr);
				} else {
					gr.setColor(Color.white); //farbe für WireFrame
					for (int j = 0; j < object[k].plane[i].npoints; j++) {
						int jj = j + 1;
						if (j == object[k].plane[i].npoints - 1)
							jj = 0;
						gr.drawLine(poly.xpoints[j], poly.ypoints[j], poly.xpoints[jj], poly.ypoints[jj]);
					}
				}
			}
			poly = null;
			et = null;
		} // loop fürs polygon
	} // loop fürs object
}
public void setImageBuffer(Image b, Graphics g) {
			gs = b;
			gr = g;
		}
// Lichtquelle irgendwo hinsetzten...
public void setLightSource(double th, double ph) {
			ltheta = th; lphi = ph;
	    lightdata_init();
		}
// Wireframe Mode einschalten
public void setLineMode() { linedraw = 1;}
	//Anzahl der Objekte setzen..., die verabeitet werden sollen...
	public void setNumObject(int n) {
			nobjects = n;
			object = new Obj[nobjects];
			for(int i=0; i<nobjects; i++) object[i] = new Obj();
		}
// Shading Mode einschalten
public void setShadeMode() { linedraw = 0;}
		public void setSize(Dimension s) {
			appsize = s;
			origin = new Point(appsize.width/2, appsize.height/2+5);//nis
			zbuf = new Zbuffer(appsize);
			zbuf.clearBuffer();
			resize(appsize);
		}
		public void setViewpoint(double r, double th, double ph) {
			R=r;
			theta = th;
			phi = ph;
		}
/*
*FArbe aufgrund von Lambert shading und den Farbattributen berechnen
*
*/
RGB shading(Point3 co, Point3 nv, ATT att) {
	int i;
	double bright;
	RGB shading_color = new RGB();
	RGB diffuse = new RGB();
	diffuse.r = 0;
	diffuse.g = 0;
	diffuse.b = 0;
	bright = lambert(nv) * 0.7; 
	diffuse.r = att.dif.r * bright;
	diffuse.g = att.dif.g * bright;
	diffuse.b = att.dif.b * bright;
	shading_color.r = diffuse.r + att.amb.r;
	shading_color.g = diffuse.g + att.amb.g;
	shading_color.b = diffuse.b + att.amb.b;
	
	return (shading_color);
}
/**
 * Weltkoordinaten ins Viewpoint-System transformieren...
 * Computer Grahics Principles and Practice und Turorials 
 */
Point3 tranfrm(Point3 point) {
	Point3 point1 = new Point3();
	Point3 point2 = new Point3();
	Point3 point3 = new Point3();
	double temp;
	
	point1.x = point.x - ptViewRef.x;
	point1.y = point.y - ptViewRef.y;
	point1.z = point.z - ptViewRef.z;
	temp = point1.x * cos_theta + point1.y * sin_theta;
	point2.x = -point1.x * sin_theta + point1.y * cos_theta;
	point2.y = -temp * sin_phi + point1.z * cos_phi;
	point3.z = temp * cos_phi + point1.z * sin_phi;
	double dd = R / (R - point3.z);
	point3.x = point2.x * dd;
	point3.y = point2.y * dd;
	return point3;
}
/*
* Double buffering, wie man es in jedem x-beliebiegen Java-Buch erklärt bekommt...
*
*/

public void update(Graphics g)
   {
	  //init double-buffer
	  if (dbImage == null) {
		 dbImage = createImage(
			this.getSize().width,
			this.getSize().height
		 );
		 dbGraphics = dbImage.getGraphics();
	  }
	  //clear background
	  dbGraphics.setColor(getBackground());
	  dbGraphics.fillRect(
		 0,
		 0,
		 this.getSize().width,
		 this.getSize().height
	   );
	  //draw foreground
	  dbGraphics.setColor(getForeground());
	  paint(dbGraphics);
	  //show offsreen
	  g.drawImage(dbImage,0,0,this);
   }                  
}

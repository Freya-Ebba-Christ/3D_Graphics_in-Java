import java.util.Vector;

class Obj {
	int     nplanes;
	int     npoints;
	Point3  point[], tPoint[];
	Plane   plane[];
	RGB   col;

	public Obj() { 
		nplanes = npoints = 0;
		col = new RGB();
	}
// Flächen hinzufügen...

	public void addPlane(Vector v) {
		plane[nplanes] = new Plane(v);
	int top_point0 = plane[nplanes].set[0];
	int top_point1 = plane[nplanes].set[1];
	int top_point2 = plane[nplanes].set[2];
	double nsize,xn,yn,zn;
	// Normale Berechnen..
	xn = (point[top_point1].y-point[top_point0].y)*(point[top_point2].z-point[top_point1].z) -
	              (point[top_point1].z-point[top_point0].z)*(point[top_point2].y-point[top_point1].y);
	yn = (point[top_point1].z-point[top_point0].z)*(point[top_point2].x-point[top_point1].x) -
	              (point[top_point1].x-point[top_point0].x)*(point[top_point2].z-point[top_point1].z);
	zn = (point[top_point1].x-point[top_point0].x)*(point[top_point2].y-point[top_point1].y) -
	              (point[top_point1].y-point[top_point0].y)*(point[top_point2].x-point[top_point1].x);
	// die Länge berechnen...
	nsize = Math.sqrt(xn * xn + yn * yn + zn * zn);
	if(nsize<0.001) {
		 System.out.println("Err nsize:"+nsize);
	}
	else {
	//Normalisieren...
	xn /= nsize;
	yn /= nsize;
	zn /= nsize;}
// und rein damit...
		plane[nplanes].norm_vect.x=xn;
		plane[nplanes].norm_vect.y=yn;
		plane[nplanes].norm_vect.z=zn;

		nplanes++;
	}
	public void addPoint(double x, double y, double z) {
		point[npoints] = new Point3(x,y,z);
		tPoint[npoints] = new Point3(x,y,z);
		npoints++;
	}
	public void setUpObject(int nv, int np, double r, double g, double b) {
		nplanes = npoints = 0;
		col = new RGB(r,g,b);
		point = new Point3[nv];
		tPoint = new Point3[nv];
		plane = new Plane[np];
	}
}

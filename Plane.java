import java.util.Vector;
/**
 * Plane class of a polygon
 */
class Plane {
	public int set[];
	public int npoints;
	public boolean visible;
		public Point3 norm_vect;

	Plane(Vector vec) {
		npoints = vec.size();
		set = new int[npoints];
				norm_vect = new Point3();

		for (int i = 0; i < npoints; i++) {
	    	set[i] = ((Integer)vec.elementAt(i)).intValue();
		}
	}
	/**
	 * Flächensichtbarkeit berechnen
	 * nachgucken, ob das Polygon überhaupt sichtbar ist.
 	 * Computer Graphics Principles and Practice und Tutorials
	 */
	public void setFront_or_Back(Point3 point[]) {
		double det = point[set[2]].x*(point[set[1]].y-point[set[0]].y)+
		point[set[0]].x*(point[set[2]].y-point[set[1]].y)+
		point[set[1]].x*(point[set[0]].y-point[set[2]].y);

		if (det >= 3.5) { 
			visible = true;
		} else {
			visible = false;
		}
	}
}

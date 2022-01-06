import java.applet.*;
import java.awt.*;
import java.util.Vector;


/**
 * Kantentabelle...sortiert in Y-Richtung...
 */
class EdgeTable {
	public int xmin[], ymin[], ymax[],zmin[];
	double gradXY[];
		double gradZ[];
		Zbuffer zbuf;
	public int nsize = 0;

	static final int MIN = 0;
	static final int MID = 1;
	static final int MAX = 2;

//Kanten interpolieren...

EdgeTable(Polygon poly, int depth[], Zbuffer zb) {
	xmin = new int[poly.npoints - 1];
	ymin = new int[poly.npoints - 1];
	ymax = new int[poly.npoints - 1];
	zmin = new int[poly.npoints - 1];
	gradXY = new double[poly.npoints - 1];
	gradZ = new double[poly.npoints - 1];
	zbuf = zb;

	for (int i = 0; i < poly.npoints - 1; i++) {
		if (poly.ypoints[i] < poly.ypoints[i + 1]) {
			xmin[nsize] = poly.xpoints[i];
			ymin[nsize] = poly.ypoints[i];
			ymax[nsize] = poly.ypoints[i + 1];
			zmin[nsize] = depth[i];
		} else {
			xmin[nsize] = poly.xpoints[i + 1];
			ymin[nsize] = poly.ypoints[i + 1];
			ymax[nsize] = poly.ypoints[i];
			zmin[nsize] = depth[i + 1];
		}
		if (poly.ypoints[i] != poly.ypoints[i + 1]) {
			if (Math.abs(poly.ypoints[i] - poly.ypoints[i + 1]) < 0.1) {
				gradXY[nsize] = 0.;
			} else {
				gradXY[nsize] = (double) (poly.xpoints[i] - poly.xpoints[i + 1]) / (poly.ypoints[i] - poly.ypoints[i + 1]); // gradienten nach nicht ganz unbekannter formel berechnen...
			}
			if (Math.abs(poly.ypoints[i] - poly.ypoints[i + 1]) < 0.1) {
				gradZ[nsize] = 0.;
			} else {
				gradZ[nsize] = (double) (depth[i] - depth[i + 1]) / (poly.ypoints[i] - poly.ypoints[i + 1]); //gradienten nach nicht ganz unbekannter formel berechnen...
			}
			nsize++;
		}
	}
	ySort();
}
/**
 * xmin in xpoints[] und attr (MAX,MID,MIN) in vertex[].
 * true zurueck , wenn das Einfügen der Punkte geklappt hat...
 */
boolean insert(double xpoints[], double zpoints[], int vertex[], double xmin, double zmin, int attr, int npoint) {
	int i = 0;
	double EPS = 0.00001;
	while (i < npoint) {
		if (Math.abs(xpoints[i] - xmin) < EPS) {
			if ((vertex[i] == MAX && attr == MIN) || (vertex[i] == MIN && attr == MAX)) {
				return false;
			}
		}
		if (xpoints[i] >= xmin) {
			for (int j = npoint - 1; j >= i; j--) {
				xpoints[j + 1] = xpoints[j];
				zpoints[j + 1] = zpoints[j];
				vertex[j + 1] = vertex[j];
			}
			break;
		}
		i++;
	}
	xpoints[i] = xmin;
	zpoints[i] = zmin;
	vertex[i] = attr;
	return true;
}
	/**
	 * Das Polygon Scanlinienweise zeichnen...
	 */
	public void paint(Graphics g) {
		double xpoints[] = new double[nsize];
		double zpoints[] = new double[nsize];
		int vertex[] = new int[nsize];
		double x[] = new double[nsize];
		double z[] = new double[nsize];
		for (int i = 0; i < nsize; i++) {
			x[i] = xmin[i];
						z[i] = zmin[i];
		}

		for (int scanline = ymin[0]; ; scanline++) {
			int npoint = 0;

			for (int i = 0; i < nsize; i++) {
				if (scanline > ymin[i] && scanline < ymax[i]) {
					if (insert(xpoints, zpoints, vertex, x[i], z[i], MID, npoint)) {
						npoint++;
					}
					x[i] += gradXY[i];
										z[i] += gradZ[i];
				} else if (scanline == ymin[i]) {
					if (insert(xpoints, zpoints, vertex, x[i], z[i], MIN, npoint)) {
						npoint++;
					}
					x[i] += gradXY[i];
										z[i] += gradZ[i];
				} else if (scanline == ymax[i]) {
					if (insert(xpoints, zpoints, vertex, x[i], z[i], MAX, npoint)) {
						npoint++;
					}
					x[i] += gradXY[i];
					z[i] += gradZ[i];
				}
			}

			if (npoint == 0) {
				break;
			}

			for (int i = 0; i < npoint; i += 2) {
							int ix1 = (int)xpoints[i];
							int ix2 = (int)xpoints[i+1];
							double dz = 0.;
							if((ix2-ix1)<1) {
							}
							else { dz = (zpoints[i+1]-zpoints[i])/(ix2-ix1);}
							double zz = zpoints[i];
							
							for(int ix=ix1; ix<ix2; ix++) {
								if(zbuf.checkDepth(ix,scanline,zz)){
									
									g.drawLine(ix,scanline,ix,scanline); //kein setPixel in java 1.0 or 1.1 ohne JAVA2D!!!
								}
								zz+=dz;
							}
			}
		}
		xpoints = null;
				zpoints = null;
	}
	/**
	 * Swaps table contents.
	 */
	void swap(int i, int j) {
		int temp;
		double tempd;
		
		temp = xmin[i];
		xmin[i] = xmin[j];
		xmin[j] = temp;

		temp = ymin[i];
		ymin[i] = ymin[j];
		ymin[j] = temp;

		temp = ymax[i];
		ymax[i] = ymax[j];
		ymax[j] = temp;

		temp = zmin[i];
		zmin[i] = zmin[j];
		zmin[j] = temp;

		tempd = gradXY[i];
		gradXY[i] = gradXY[j];
		gradXY[j] = tempd;

		tempd = gradZ[i];
		gradZ[i] = gradZ[j];
		gradZ[j] = tempd;
	}
	/**
	 * Y-sort
	 */
	void ySort() {
	for (int i = 0; i < nsize; i++) {
			for (int j = 0; j < nsize-i-1; j++) {
				if (ymin[j] > ymin[j+1]) {
					swap(j, j+1);
				}
			}
		}
	}
}

import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;
public class ZBufferDemo extends Applet{
	Dimension appsize;
	Panel p1 = new Panel();
	Panel p2 = new Panel();
	Panel p3 = new Panel();
	Panel p4 = new Panel();
	String datafile;
	Scrollbar sb,sb2,sb3;
	Label l1, l2;
	Button b1, b2;
	Choice c = new Choice();
	ZbufferedCanvas zCanvas;
	double scale = 30;
	CheckboxGroup chkgrp;
	
public boolean action(Event evt, Object obj) {
	
	if (evt.target == b1) { //neu zeichnen
		double scaleTMP = sb.getValue();
		
		if ((int) scale != (int) scaleTMP) {
			scale = scaleTMP;
			polygon_read();// schlecht, aber performance spielt keine Rolle...
		}
		
		zCanvas.setLightSource(sb2.getValue(),sb3.getValue());
		zCanvas.setViewpoint(zCanvas.R, zCanvas.theta, zCanvas.phi);
		zCanvas.repaint();
		return true;
	} else
		if (evt.target instanceof Checkbox) {
			Checkbox chk = (Checkbox) (evt.target);
			if (chk.getLabel().equals("Flat/Lambert Shading")) {
				zCanvas.setShadeMode();
				zCanvas.repaint();
			} else
				if (chk.getLabel().equals("Wireframe")) {
					zCanvas.setLineMode();
					zCanvas.repaint();
				}
				
		} else
			return super.action(evt, obj);
	return false;
}
public void init() {
	setBackground(Color.black);
	setForeground(Color.red);
	setLayout(new BorderLayout(0, 0));
	appsize = size(); 
	p1.setBackground(new Color(0, 0, 0));
	p1.setForeground(Color.white);

	p2.setBackground(new Color(0,0,0));
	p2.add(b1 = new Button("ReDraw"));
	chkgrp = new CheckboxGroup();
	Checkbox chk1 = new Checkbox("Flat/Lambert Shading", chkgrp, true); // Lambert Shading: siehe Computer Graphics Principles and Practice
	Checkbox chk2 = new Checkbox("Wireframe", chkgrp, false);
	p3.setBackground(new Color(0, 0, 0));
	p3.setForeground(Color.white);
	p3.add(l2 = new Label("Mode:"));
	p3.add(chk1);
	p3.add(chk2);
	p3.add(l1 = new Label("Scale:"));
	p3.add(sb = new Scrollbar(Scrollbar.HORIZONTAL,30,20,0,100));
	p3.add(l1 = new Label("MOVE LIGHT:"));
	p3.add(sb2 = new Scrollbar(Scrollbar.VERTICAL,30,20,0,100));
	p3.add(sb3 = new Scrollbar(Scrollbar.HORIZONTAL,30,20,0,100));
	p4.setLayout(new BorderLayout());
	p4.resize(300, 200);
	p4.add("North", p2);
	p4.add("South", p3);
	Dimension s = new Dimension(640, 420);
	zCanvas = new ZbufferedCanvas();
	zCanvas.setSize(s);
	zCanvas.setViewpoint(500, 20, 30);
	
	Image buff = createImage(s.width, s.height);
	Graphics g1 = buff.getGraphics();
	zCanvas.setImageBuffer(buff, g1);
	add("North", p1);
	add("Center", zCanvas);
	add("South", p4);
	resize(appsize);
	datafile = "CubeCube.obj";
	double ltheta = 0;
	double lphi = 0;
	String str;
	if ((str = getParameter("scene")) != null) {
		datafile = getParameter(str);
		datafile = str;
		System.out.println("DataFile:" + datafile);
	}
	if ((str = getParameter("ltheta")) != null) {
		ltheta = Double.valueOf(str).doubleValue();
	}
	if ((str = getParameter("lphi")) != null) {
		lphi = Double.valueOf(str).doubleValue();
	}
	zCanvas.setLightSource(ltheta, lphi);
	polygon_read();
}
	public void paint(Graphics g) {
	}
/*
* Das Datenfile wir eingelesen..., die Punkte werden mit einem Skalierungwert multipliziert und dann
* die Objekte nach und nach eingelesen...
*
*/

public void polygon_read() {
	int nv, nf = 0;
	try {
		InputStream is = new URL(getDocumentBase(), datafile).openStream();
		DataInputStream dis = new DataInputStream(is);
		StringTokenizer st;
		double x, y, z;
		Vector vec;
		String comment;
		st = new StringTokenizer(dis.readLine());
		int nobjects = Integer.parseInt(st.nextToken());
		zCanvas.setNumObject(nobjects);
		comment = st.nextToken();
		for (int k = 0; k < nobjects; k++) {
			st = new StringTokenizer(dis.readLine());
			nv = Integer.parseInt(st.nextToken());
			nf = Integer.parseInt(st.nextToken());
			double r = Double.valueOf(st.nextToken()).doubleValue();
			double g = Double.valueOf(st.nextToken()).doubleValue();
			double b = Double.valueOf(st.nextToken()).doubleValue();
			comment = st.nextToken();
			zCanvas.object[k].setUpObject(nv, nf, r, g, b);
			for (int i = 0; i < nv; i++) {
				st = new StringTokenizer(dis.readLine());
				x = scale * Double.valueOf(st.nextToken()).doubleValue();
				y = scale * Double.valueOf(st.nextToken()).doubleValue();
				z = scale * Double.valueOf(st.nextToken()).doubleValue();
				zCanvas.object[k].addPoint(x, y, z);
			}
			for (int j = 0; j < nf; j++) {
				st = new StringTokenizer(dis.readLine());
				vec = new Vector();
				while (st.hasMoreTokens()) {
					int num = Integer.valueOf(st.nextToken(), 10).intValue() - 1;
					if (num >= 0)
						vec.addElement(new Integer(num));
				}
				zCanvas.object[k].addPlane(vec);
			}
		}
		is.close();
	} catch (IOException e) {
		System.err.println("Datafile not found..." + datafile);
	}
}
}

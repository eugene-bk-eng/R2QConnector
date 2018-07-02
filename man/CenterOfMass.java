package com.local.ideas.experiment.puzzles;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.ocean927.util.Uf;
import org.junit.Assert;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;

public class CenterOfMass {
    public CenterOfMass() {
        //test();
        //testAssert();

        testWithMass();
    }

    void test() {
        // rectangles that do not overlap, adjoint one side.
        Shapes s=new Shapes();
        s.add(new Rect(0,0,10,1)).
                add(new Rect(5,1,10,2));
        //
        for (Rect r: s.shapes) { log(r + ", centroid: " + getCentroid(r)); }
        log("center of mass: " + getCentroid(s.shapes));

        // rectangles that do overlap 100%
        s=new Shapes();
        s.add(new Rect(0,0,10,1)).
                add(new Rect(0,0,10,1));
        //
        for (Rect r: s.shapes) { log(r + ", centroid: " + getCentroid(r)); }
        log("center of mass: " + getCentroid(s.shapes));

        // rectangles that partially overlap
        s=new Shapes();
        s.add(new Rect(0,0,10,1)).
                add(new Rect(5,0,10,1));
        //
        for (Rect r: s.shapes) { log(r + ", centroid: " + getCentroid(r)); }
        log("center of mass: " + getCentroid(s.shapes));
    }

    void testAssert() {
        // rectangles that do not overlap, adjoint one side.
        Assert.assertEquals( getCentroid((new Shapes()).
                        add(new Rect(0,0,10,1)).
                        add(new Rect(5,1,10,2)).shapes),
                new Point(5.8333,0.8333) );

        // rectangles that overlap 100%
        Assert.assertEquals( getCentroid((new Shapes()).
                        add(new Rect(0,0,10,1)).
                        add(new Rect(0,0,10,1)).shapes),
                new Point(5.0,0.5) );

        // rectangles that partially overlap
        Assert.assertEquals( getCentroid((new Shapes()).
                        add(new Rect(0,0,10,1)).
                        add(new Rect(5,0,10,1)).shapes),
                new Point(5.8333,0.5) );
    }

    void testWithMass() {
        Shapes s=new Shapes();
        s.add(new RectWeight( "10 kg",0,0,10,1));
        //
        for (Rect r: s.shapes) { log(r + ", centroid: " + getCentroid(r)); }
        log("center of mass: " + getCentroidWithMass(s.shapes));
    }

    Point getCentroidWithMass(List<? extends Rect> list) {

        // get dimension
        ShapesDimension dimension=new ShapesDimension(list);

        // set up scan
        //double hstep=minshapex/2, vstep=minshapey/2;
        double hstep=0.01,vstep=0.01;
        int hsteps=(int)Math.floor((dimension.maxx-dimension.minx)/hstep);
        int vsteps=(int)Math.floor((dimension.maxy-dimension.miny)/vstep);

        // scan rows and columns
        for (int i = 0; i <hsteps; i++) { // horizontal row x
            for (int j = 0; j < vsteps; j++) { // vertical column y
                double x=dimension.minx + i*hstep;
                double y=dimension.miny + j*vstep;
                //log( "inspect: " + new Point(x,y) );
            }    
        }

        return null;
    }

    class ShapesDimension {
        double minshapex=Double.MAX_VALUE,minshapey=Double.MAX_VALUE;
        double miny=Double.MAX_VALUE,maxy=Double.MIN_VALUE,minx=Double.MAX_VALUE,maxx=Double.MIN_VALUE;
        public ShapesDimension(List<? extends Rect> list) {
            if( !list.isEmpty() ) {
                for (Rect r : list) {
                    minshapex = Math.min(minshapex, Math.abs(r.x2 - r.x1));
                    minshapey = Math.min(minshapey, Math.abs(r.y2 - r.y1));
                    minx = Math.min(minx, Math.min(r.x1, r.x2));
                    maxx = Math.max(maxx, Math.max(r.x1, r.x2));
                    miny = Math.min(miny, Math.min(r.y1, r.y2));
                    maxy = Math.max(maxy, Math.max(r.y1, r.y2));
                }
            }
        }
    }

    Point getCentroid(List<Rect> list) {
        double sumx=0,sumy=0,sumarea=0;
        for (Rect r: list) {
            sumarea+=r.area();
            sumx+=getCentroid(r).x1*r.area();
            sumy+=getCentroid(r).y1*r.area();
        }
        return new Point(sumx/sumarea, sumy/sumarea);
    }

    Point getCentroid(Rect r) {
        return new Point( Math.min(r.x1,r.x2) + Math.abs(r.x2-r.x1)/2,
                Math.min(r.y1,r.y2) + Math.abs(r.y2-r.y1)/2);
    }

    class Shapes {
        List<Rect> shapes= Lists.newArrayList();

        Shapes add(Rect r) {
            shapes.add(r);
            return this;
        }

        boolean isOverlapping(Rect in) {
            for (Rect r: shapes) {
                // check four dots
                if( isPointInsideRect(new Point(in.x1,Math.min(in.y1,in.y2)),r) ) return true;
                if( isPointInsideRect(new Point(in.x1,Math.max(in.y1,in.y2)),r) ) return true;
                if( isPointInsideRect(new Point(Math.min(in.x1,in.x2),in.y1),r) ) return true;
                if( isPointInsideRect(new Point(Math.max(in.x1,in.x2),in.y2),r) ) return true;
            }
            return false;
        }

        boolean isPointInsideRect(Point p, Rect r) {
            if( (p.x1>Math.min(r.x1,r.x2) && p.x1<Math.max(r.x1,r.x2)) &&
                    (p.y1>Math.min(r.y1,r.y2) && p.y1<Math.max(r.y1,r.y2)) ) return true;
            else return false;
        }
    }

    class Point {
        final double x1,y1;
        public Point(double x1, double y1) {
            this.x1 = x1;
            this.y1=y1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Point)) return false;
            Point point = (Point) o;

            return compareAsBigDecimal(point.x1,x1,4) && compareAsBigDecimal(point.y1,y1,4);
        }

        // assume scale is the same
        boolean compareAsBigDecimal(double a, double b, int precision) {
            boolean result=false;
                BigDecimal abg=new BigDecimal(a, new MathContext(precision) );
                BigDecimal bbg=new BigDecimal(b, new MathContext(precision) );
                if( abg.compareTo(bbg)==0 ) {
                    result=true;
                }
            return result;
        }

        @Override
        public int hashCode() {

            return Objects.hash(x1, y1);
        }

        @Override
        public String toString() {
            return "(" + Uf.rDbl(x1,2,true) + "," +  Uf.rDbl(y1,2,true) + ")";
        }
    }

    class RectWeight extends Rect{
        final double kg;
        public RectWeight(double kg, double x1, double y1, double x2, double y2) {
            super(x1, y1, x2, y2);
            this.kg=kg;
        }
        public RectWeight(String kg, double x1, double y1, double x2, double y2) {
            super(x1, y1, x2, y2);
            Preconditions.checkArgument( kg.toLowerCase().contains("kg") );
            this.kg=Double.parseDouble(kg.toLowerCase().replace("kg","").trim());
        }
        @Override
        public String toString() {
            return kg + " kg., " + super.toString();
        }
    }

    class Rect {
        final double x1,y1,x2,y2;

        public Rect(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        double area() { return Math.abs(x1-x2)*Math.abs(y1-y2); }

        @Override
        public String toString() {
            return "(" + x1 + "," + y1 + ") x " + "(" + x2 + "," + y2 + ")";
        }
    }

    public static void main(String[] args) {
        try {
            new CenterOfMass();
        }catch(Exception e){ e.printStackTrace(); }
    }

    public static void log(Object s){ System.out.println(s); }

}

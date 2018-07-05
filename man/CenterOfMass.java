package com.local.ideas.experiment.puzzles;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ocean927.util.Uf;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * https://en.wikipedia.org/wiki/Centroid
 * http://ru.solverbook.com/spravochnik/mexanika/statika/centr-tyazhesti-centr-mass/
 *
 */

public class CenterOfMass {

    public CenterOfMass() {
        long lbeg=System.currentTimeMillis();
            //test();
            //testAssert();
            testWithMass();
        long lend=System.currentTimeMillis();
        log.info("run time: " + DurationFormatUtils.formatDuration(lend-lbeg, "HH:mm:ss:SSS"));
    }

    void test() {
        // rectangles that do not overlap, adjoint one side.
        Canvas s=new Canvas();
        s.add(new Rect("a",0,0,10,1)).
                add(new Rect("b",5,1,10,2));
        //
        for (Rect r: s.shapes) { log.info(r + ", centroid: " + getGeometricCentroid(r)); }
        log.info("center of mass: " + getCentroid(s.shapes));

        // rectangles that do overlap 100%
        s=new Canvas();
        s.add(new Rect("a",0,0,10,1)).
                add(new Rect("b",0,0,10,1));
        //
        for (Rect r: s.shapes) { log.info(r + ", centroid: " + getGeometricCentroid(r)); }
        log.info("center of mass: " + getCentroid(s.shapes));

        // rectangles that partially overlap
        s=new Canvas();
        s.add(new Rect("a",0,0,10,1)).
                add(new Rect("b",5,0,10,1));
        //
        for (Rect r: s.shapes) { log.info(r + ", centroid: " + getGeometricCentroid(r)); }
        log.info("center of mass: " + getCentroid(s.shapes));
    }

    void testAssert() {
        // rectangles that do not overlap, adjoint one side.
        Assert.assertEquals( getCentroid((new Canvas()).
                        add(new Rect("a",0,0,10,1)).
                        add(new Rect("b",5,1,10,2)).shapes),
                new Point("",5.8333,0.8333) );

        // rectangles that overlap 100%
        Assert.assertEquals( getCentroid((new Canvas()).
                        add(new Rect("a",0,0,10,1)).
                        add(new Rect("b",0,0,10,1)).shapes),
                new Point("",5.0,0.5) );

        // rectangles that partially overlap
        Assert.assertEquals( getCentroid((new Canvas()).
                        add(new Rect("a",0,0,10,1)).
                        add(new Rect("b",5,0,10,1)).shapes),
                new Point("",5.8333,0.5) );
    }

    void testWithMass() {
        Canvas s=new Canvas();
        s.add(new RectWeight("large rect", "10 kg",0,0,10,1));
        s.add(new RectWeight("small rect", "2 kg",0,0,10,1));
        //
        for (Rect r: s.shapes) { log.info(r + ", centroid: " + getGeometricCentroid(r)); }
        log.info("center of mass: " + getMassCentroid(s.shapes));
    }

    /** integrate over parts */
    PointWeight getMassCentroid(List<? extends Rect> list) {
        BigDecimal stepsize=new BigDecimal("0.005");
        BigDecimal deltaX=stepsize;
        BigDecimal deltaArea=deltaX.multiply(deltaX);
        Map<RectWeight,Point> mapCenters= Maps.newHashMap();
        for (Rect _r: list) {
            BigDecimal xcenter=BigDecimal.ZERO, ycenter=BigDecimal.ZERO;
            RectWeight r=(RectWeight)_r;
            BigDecimal deltaMass=deltaArea.multiply(BigDecimal.valueOf(r.kg)).divide(r.area, RoundingMode.HALF_DOWN);
            log.info("figure: " + r.name + ", mass: " + r.kg + " kg., area: " + Uf.rDbl(r.area.doubleValue(),4,true ) + " m^2" + ", deltaArea: " + Uf.rDbl(deltaArea.doubleValue(),4,true) + " m^2" + ", deltaMass: " + Uf.rDbl(deltaMass.doubleValue(),5,true) + " kg");
            // run over points
            int xsteps=(int)Math.floor(new BigDecimal(Math.abs(r.x2-r.x1)).divide(stepsize).doubleValue());
            int ysteps=(int)Math.floor(new BigDecimal(Math.abs(r.y2-r.y1)).divide(stepsize).doubleValue());
            MinMaxInfo minmax=new MinMaxInfo(r);
            for (int i = 0; i < xsteps; i++) {
                BigDecimal x=BigDecimal.valueOf(minmax.minx).add(BigDecimal.valueOf(i).multiply(stepsize));
                for (int j = 0; j < ysteps; j++) {
                    BigDecimal y=BigDecimal.valueOf(minmax.miny).add(BigDecimal.valueOf(j).multiply(stepsize));
                    if( x.doubleValue()<=minmax.maxx && y.doubleValue()<=minmax.maxy ) {
                        xcenter=xcenter.add( x.multiply(deltaMass) );
                        ycenter=ycenter.add( y.multiply(deltaMass) );
                    }else{
                        log.info("x: " + x + " over limit " + minmax.maxx);
                        log.info("y: " + y + " over limit " + minmax.maxy);
                    }
                }    
            }
            xcenter=xcenter.divide(BigDecimal.valueOf(r.kg), RoundingMode.HALF_DOWN);
            ycenter=ycenter.divide(BigDecimal.valueOf(r.kg), RoundingMode.HALF_DOWN);
            Point center=new Point(r.name, xcenter.doubleValue(),ycenter.doubleValue());
            mapCenters.put(r,center);

            log.info("raw values x: " + xcenter.toPlainString() + ", y: " + ycenter.toPlainString());
            log.info("Center: " +  center + ", weight: " + r.kg + " kg.");
        }
        BigDecimal xcenter=BigDecimal.ZERO, ycenter=BigDecimal.ZERO, sum=BigDecimal.ZERO;

        for (RectWeight r: mapCenters.keySet()) {
            Point rcenter=mapCenters.get(r);
            sum=sum.add(BigDecimal.valueOf(r.kg));
            xcenter=xcenter.add(BigDecimal.valueOf(rcenter.x).multiply(BigDecimal.valueOf(r.kg)));
            ycenter=ycenter.add(BigDecimal.valueOf(rcenter.y).multiply(BigDecimal.valueOf(r.kg)));
        }
        xcenter=xcenter.divide(sum, RoundingMode.HALF_DOWN);
        ycenter=ycenter.divide(sum, RoundingMode.HALF_DOWN);
        return new PointWeight("global center", sum.doubleValue() + " kg", xcenter.doubleValue(),ycenter.doubleValue());
    }

    Point getCentroid(List<Rect> list) {
        double sumx=0,sumy=0,sumarea=0;
        for (Rect r: list) {
            sumarea+=r.area();
            sumx+=getGeometricCentroid(r).x*r.area();
            sumy+=getGeometricCentroid(r).y*r.area();
        }
        return new Point("",sumx/sumarea, sumy/sumarea);
    }

    /**
     *
     * @param r
     * @return
     */
    Point getGeometricCentroid(Rect r) {
        return new Point( "",Math.min(r.x1,r.x2) + Math.abs(r.x2-r.x1)/2,
                Math.min(r.y1,r.y2) + Math.abs(r.y2-r.y1)/2);
    }

    /** canvas information */
    class MinMaxInfo {
        double minshapex=Double.MAX_VALUE,minshapey=Double.MAX_VALUE;
        double miny=Double.MAX_VALUE,maxy=Double.MIN_VALUE,minx=Double.MAX_VALUE,maxx=Double.MIN_VALUE;
        public MinMaxInfo(Rect r) {
            minshapex = Math.min(minshapex, Math.abs(r.x2 - r.x1));
            minshapey = Math.min(minshapey, Math.abs(r.y2 - r.y1));
            minx = Math.min(minx, Math.min(r.x1, r.x2));
            maxx = Math.max(maxx, Math.max(r.x1, r.x2));
            miny = Math.min(miny, Math.min(r.y1, r.y2));
            maxy = Math.max(maxy, Math.max(r.y1, r.y2));
        }
    }

    /** holds objects */
    class Canvas<T extends Rect> {
        List<? extends Rect> shapes= Lists.newArrayList();

        <T extends Rect> Canvas add(T r) {
            shapes.add(r);
            return this;
        }

        boolean isOverlapping(Rect in) {
            for (Rect r: shapes) {
                // check four dots
                if( isPointInsideRect(new Point("",in.x1,Math.min(in.y1,in.y2)),r) ) return true;
                if( isPointInsideRect(new Point("",in.x1,Math.max(in.y1,in.y2)),r) ) return true;
                if( isPointInsideRect(new Point("",Math.min(in.x1,in.x2),in.y1),r) ) return true;
                if( isPointInsideRect(new Point("",Math.max(in.x1,in.x2),in.y2),r) ) return true;
            }
            return false;
        }

        boolean isPointInsideRect(Point p, Rect r) {
            if( (p.x>Math.min(r.x1,r.x2) && p.x<Math.max(r.x1,r.x2)) &&
                    (p.y>Math.min(r.y1,r.y2) && p.y<Math.max(r.y1,r.y2)) ) return true;
            else return false;
        }
    }

    class PointWeight extends Point {
        final double kg;
        public PointWeight(String name, String kg, double x, double y) {
            super(name, x, y);
            Preconditions.checkArgument( kg.toLowerCase().contains("kg") );
            this.kg=Double.parseDouble(kg.toLowerCase().replace("kg","").trim());
        }
        @Override
        public String toString() {
            return kg + " kg., " + super.toString();
        }
    }

    class Point {
        final String name;
        final double x,y;
        public Point(String name, double x, double y) {
            this.name=name;
            this.x = x;
            this.y=y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Point)) return false;
            Point point = (Point) o;

            return compareAsBigDecimal(point.x,x,4) && compareAsBigDecimal(point.y,y,4);
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

            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "(" + Uf.rDbl(x,2,true) + "," +  Uf.rDbl(y,2,true) + ")";
        }
    }

    class RectWeight extends Rect {
        final double kg;
        public RectWeight(String name, double kg, double x1, double y1, double x2, double y2) {
            super(name, x1, y1, x2, y2);
            this.kg=kg;
        }
        public RectWeight(String name, String kg, double x1, double y1, double x2, double y2) {
            super(name, x1, y1, x2, y2);
            Preconditions.checkArgument( kg.toLowerCase().contains("kg") );
            this.kg=Double.parseDouble(kg.toLowerCase().replace("kg","").trim());
        }
        @Override
        public String toString() {
            return kg + " kg., " + super.toString();
        }
    }

    class Rect {
        final String name;
        final double x1,y1,x2,y2;
        final BigDecimal area;

        public Rect(String name, double x1, double y1, double x2, double y2) {
            this.name=name;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            area=new BigDecimal( Math.abs(x2-x1)*Math.abs(y2-y1) );
        }

        double area() { return Math.abs(x1-x2)*Math.abs(y1-y2); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Rect)) return false;
            Rect rect = (Rect) o;
            return Double.compare(rect.x1, x1) == 0 &&
                    Double.compare(rect.y1, y1) == 0 &&
                    Double.compare(rect.x2, x2) == 0 &&
                    Double.compare(rect.y2, y2) == 0;
        }

        @Override
        public int hashCode() {

            return Objects.hash(x1, y1, x2, y2);
        }

        @Override
        public String toString() {
            return name + "(" + x1 + "," + y1 + ") x " + "(" + x2 + "," + y2 + ")";
        }
    }

    public static void main(String[] args) {
        try {
            new CenterOfMass();
        }catch(Exception e){ e.printStackTrace(); }
    }

    private final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    //public static void log(Object s){ System.out.println(s); }

}

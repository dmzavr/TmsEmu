package com.google.dmzavr;

final class Segment {

    static final class Def {
        double radius = 0;  ///< Radius, abstract units
        double angle = 0;   ///< Angle of segment, degrees
        double chord = 0;   ///< Chord length, abstract units
        double height = 0;  ///< Height of segment, abstract units
        double length = 0;  ///< Length of arc, abstract units
        double area = 0;    ///< Area of segment, abstract units^2
    }

    static void recalcByAreaAndRadius(Def def, double avail_error )
    {
        if( def.area <= 0) {
            def.angle = 0;
            def.chord = 0;
            def.height = 0;
            def.length = 0;
            return;
        }
        double maxarea = Math.PI * Math.pow( def.radius, 2 );
        if( def.area >= maxarea ) {
            def.angle = 360;
            def.chord = 0;
            def.height = def.radius;
            def.length = 2 * Math.PI * def.radius;
        }
        def.angle = 180;
        double min_angle = 0;
        double max_angle = 360;

        double tmp_area = maxarea / 2.;
        int iterations = 100;
        while( Math.abs(tmp_area - def.area) > avail_error && --iterations > 0) {
            if( tmp_area > def.area)
                max_angle = def.angle;
            else
                min_angle = def.angle;
            def.angle = (max_angle + min_angle)/2.;
            tmp_area = Math.pow(def.radius,2) * (Math.PI * def.angle /180. - Math.sin(Math.toRadians(def.angle))) / 2;
        }
        def.chord = 2 * def.radius * Math.sin( Math.toRadians(def.angle) / 2. );
        def.height = def.radius * ( 1 - Math.cos( Math.toRadians(def.angle) / 2. ) );
        def.length = Math.toRadians( def.angle) * def.radius;
    }

    static void recalcByHeightAndRadius(Def def)
    {
        if(def.height >= def.radius*2) {
            def.height = def.radius * 2;
            def.angle = 360;
            def.chord = 0;
            def.length = 2 * Math.PI * def.radius;
            def.area = Math.PI * Math.pow(def.radius,2);
            return;
        }
        def.angle = 2 * Math.toDegrees( Math.acos( 1 - def.height / def.radius) );
        def.chord = 2 * def.radius * Math.sin( Math.toRadians(def.angle) / 2. );
        def.length = Math.toRadians( def.angle) * def.radius;
        def.area = Math.pow(def.radius,2) * (Math.PI * def.angle /180. - Math.sin(Math.toRadians(def.angle))) / 2;
    }
}

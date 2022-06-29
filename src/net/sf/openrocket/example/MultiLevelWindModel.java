package net.sf.openrocket.example;

import org.slf4j.LoggerFactory;
import net.sf.openrocket.util.Coordinate;
import java.util.ArrayList;
import java.util.List;
import net.sf.openrocket.models.wind.PinkNoiseWindModel;
import org.slf4j.Logger;
import net.sf.openrocket.models.wind.WindModel;

public class MultiLevelWindModel implements WindModel
{
    private static final Logger log;
    private PinkNoiseWindModel mainWindModel;
    private List<WindSpecs> windSpecs;

    public MultiLevelWindModel(final List<WindSpecs> winds) {
        this.mainWindModel = new PinkNoiseWindModel(10);
        this.windSpecs = null;
        this.windSpecs = new ArrayList<WindSpecs>();
        for (final WindSpecs windSpec : winds) {
            this.windSpecs.add(new WindSpecs(windSpec.altitude, windSpec.speed, windSpec.direction));
        }
        this.mainWindModel.setAverage(this.windSpecs.get(0).speed);
        this.mainWindModel.setDirection(this.windSpecs.get(0).direction);
    }

    public int getModID() {
        return 0;
    }

    public void setStandardDeviation(final double standardDeviation) {
        this.mainWindModel.setStandardDeviation(standardDeviation);
    }

    public void setTurbulenceIntensity(final double intensity) {
        this.mainWindModel.setTurbulenceIntensity(intensity);
    }

    public Coordinate getWindVelocity(final double time, final double altitude) {
        WindSpecs last_m = null;
        for (final WindSpecs m : this.windSpecs) {
            if (m.altitude >= altitude) {
                return this.interpolateWindVelocity(time, altitude, last_m, m);
            }
            last_m = m;
        }
        final Coordinate ret = this.interpolateWindVelocity(time, altitude, last_m, null);
        return ret;
    }

    private double interpolate(final double v1, final double v2, final double w) {
        return v1 + (v2 - v1) * w;
    }

    private String modelToString(final WindSpecs m) {
        if (m == null) {
            return "(null)";
        }
        return String.format("%dft %ddegrees %dspeed", Math.round(m.altitude), Math.round(m.direction * 180.0 / 3.141592653589793), Math.round(m.speed));
    }

    private Coordinate interpolateWindVelocity(final double time, final double altitude, final WindSpecs m1, final WindSpecs m2) {
        double speed = 0.0;
        double direction = 0.0;
        if (m1 == null) {
            speed = m2.speed;
            direction = m2.direction;
        }
        else if (m2 == null) {
            speed = m1.speed;
            direction = m1.direction;
        }
        else {
            final double a = (altitude - m1.altitude) / (m2.altitude - m1.altitude);
            speed = this.interpolate(m1.speed, m2.speed, a);
            final double sinSum = (1.0 - a) * Math.sin(m1.direction) + a * Math.sin(m2.direction);
            final double cosSum = (1.0 - a) * Math.cos(m1.direction) + a * Math.cos(m2.direction);
            direction = Math.atan2(sinSum, cosSum);
        }
        this.mainWindModel.setAverage(speed);
        this.mainWindModel.setDirection(direction);
        final Coordinate ret = this.mainWindModel.getWindVelocity(time, altitude);
        MultiLevelWindModel.log.debug(String.format("interpolating alt %f between layers %s and %s returning %s", altitude, this.modelToString(m1), this.modelToString(m2), ret.toString()));
        return ret;
    }

    static {
        log = LoggerFactory.getLogger((Class)MultiLevelWindModel.class);
    }

    public static class WindSpecs
    {
        public final double altitude;
        public final double speed;
        public final double direction;

        public WindSpecs(final double a, final double s, final double d) {
            this.altitude = a;
            this.speed = s;
            this.direction = d;
        }
    }

    private class WindModelAndAltitude
    {
        public final PinkNoiseWindModel windModel;
        public final double altitude;

        public WindModelAndAltitude(final PinkNoiseWindModel w, final double a) {
            this.windModel = w;
            this.altitude = a;
        }
    }
}
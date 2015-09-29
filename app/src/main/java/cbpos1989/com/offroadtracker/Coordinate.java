package cbpos1989.com.offroadtracker;

import java.io.Serializable;

/**
 * Created by Colm O'Sullivan on 28/09/2015.
 */
public class Coordinate implements Serializable{
    private double latitude;
    private double longitude;


    public Coordinate(){
        this(0.0D,0.0D);
    }

    public Coordinate(double latitude, double longitude){
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

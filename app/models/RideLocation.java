package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by sivanookala on 22/10/16.
 */
@Entity
public class RideLocation extends Model {

    public static final Finder<Long, RideLocation> find = new Finder<Long, RideLocation>(RideLocation.class);
    @Id
    Long id;
    Long rideId;
    Long postedById;
    Date locationTime;
    Date receivedAt;
    Double latitude;
    Double longitude;
    boolean beforeRide;
    boolean duringRide;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public Date getLocationTime() {
        return locationTime;
    }

    public void setLocationTime(Date locationTime) {
        this.locationTime = locationTime;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isBeforeRide() {
        return beforeRide;
    }

    public void setBeforeRide(boolean beforeRide) {
        this.beforeRide = beforeRide;
    }

    public boolean isDuringRide() {
        return duringRide;
    }

    public void setDuringRide(boolean duringRide) {
        this.duringRide = duringRide;
    }

    public Date getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Date receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Long getPostedById() {
        return postedById;
    }

    public void setPostedById(Long postedById) {
        this.postedById = postedById;
    }
}

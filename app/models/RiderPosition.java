package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by Sivakumar Sudarsi on 1/2/17.
 */

@Entity
public class RiderPosition extends Model {

    @Id
    public Long id;

    Long userId;
    Double lastKnownLatitude;
    Double lastKnownLongitude;
    Date lastLocationTime;
    public static Finder<Long , RiderPosition> find = new Finder<Long, RiderPosition>(RiderPosition.class);

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getLastKnownLatitude() {
        return lastKnownLatitude;
    }

    public void setLastKnownLatitude(Double lastKnownLatitude) {
        this.lastKnownLatitude = lastKnownLatitude;
    }

    public Double getLastKnownLongitude() {
        return lastKnownLongitude;
    }

    public void setLastKnownLongitude(Double lastKnownLongitude) {
        this.lastKnownLongitude = lastKnownLongitude;
    }

    public Date getLastLocationTime() {
        return lastLocationTime;
    }

    public void setLastLocationTime(Date lastLocationTime) {
        this.lastLocationTime = lastLocationTime;
    }
}

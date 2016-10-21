package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by Siva Nookala on 21/10/16.
 */
@Entity
public class Ride extends Model {

    public static final Finder<Long, Ride> find = new Finder<Long, Ride>(Ride.class);

    @Id
    public Long id;
    Long requestorId;
    Long riderId;
    String orderStatus;
    Double orderDistance;
    Double orderAmount;
    Date requestedAt;
    Date acceptedAt;
    Date rideStartedAt;
    Date rideEndedAt;
    Double startLatitude;
    Double startLongitude;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestorId() {
        return requestorId;
    }

    public void setRequestorId(Long requestorId) {
        this.requestorId = requestorId;
    }

    public Long getRiderId() {
        return riderId;
    }

    public void setRiderId(Long riderId) {
        this.riderId = riderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Double getOrderDistance() {
        return orderDistance;
    }

    public void setOrderDistance(Double orderDistance) {
        this.orderDistance = orderDistance;
    }

    public Double getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(Double orderAmount) {
        this.orderAmount = orderAmount;
    }

    public Date getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Date requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Date getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Date acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Date getRideStartedAt() {
        return rideStartedAt;
    }

    public void setRideStartedAt(Date rideStartedAt) {
        this.rideStartedAt = rideStartedAt;
    }

    public Date getRideEndedAt() {
        return rideEndedAt;
    }

    public void setRideEndedAt(Date rideEndedAt) {
        this.rideEndedAt = rideEndedAt;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }
}

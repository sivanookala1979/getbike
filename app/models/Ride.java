package models;

import com.avaje.ebean.Model;
import dataobject.RideStatus;
import utils.DateUtils;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by Siva Nookala on 21/10/16.
 */
@Entity
public class Ride extends Model {

    public static final Finder<Long, Ride> find = new Finder<Long, Ride>(Ride.class);
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String RIDE_ID = "rideId";
    public static final String REQUESTOR_ID = "requestorId";

    @Id
    public Long id;
    Long requestorId;
    Long riderId;
    @Enumerated(EnumType.STRING)
    RideStatus rideStatus;
    Double orderDistance;
    Double orderAmount;
    Date requestedAt;
    Date acceptedAt;
    Date rideStartedAt;
    Date rideEndedAt;
    Double startLatitude;
    Double startLongitude;
    String sourceAddress;
    String destinationAddress;
    Double totalFare;
    Double taxesAndFees;
    Double subTotal;
    Double roundingOff;
    Double totalBill;
    Integer rating;
    char rideGender;
    String actualSourceAddress;
    String actualDestinationAddress;
    boolean rideStarted = false;
    private transient String requestorName;
    private transient String riderName;
    private transient String formatedRequestAt;
    private transient String formatedAcceptedAt;
    private transient String formatedRideStartedAt;
    private transient String formatedRideEndedAt;

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

    public RideStatus getRideStatus() {
        return rideStatus;
    }

    public void setRideStatus(RideStatus rideStatus) {
        this.rideStatus = rideStatus;
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

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public Double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(Double totalFare) {
        this.totalFare = totalFare;
    }

    public Double getTaxesAndFees() {
        return taxesAndFees;
    }

    public void setTaxesAndFees(Double taxesAndFees) {
        this.taxesAndFees = taxesAndFees;
    }

    public Double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(Double subTotal) {
        this.subTotal = subTotal;
    }

    public Double getRoundingOff() {
        return roundingOff;
    }

    public void setRoundingOff(Double roundingOff) {
        this.roundingOff = roundingOff;
    }

    public Double getTotalBill() {
        return totalBill;
    }

    public void setTotalBill(Double totalBill) {
        this.totalBill = totalBill;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public char getRideGender() {
        return rideGender;
    }

    public void setRideGender(char rideGender) {
        this.rideGender = rideGender;
    }

    public String getRequestorName() {
        return requestorName;
    }

    public void setRequestorName(String requestorName) {
        this.requestorName = requestorName;
    }

    public String getRiderName() {
        return riderName;
    }

    public void setRiderName(String riderName) {
        this.riderName = riderName;
    }

    public String getActualSourceAddress() {
        return actualSourceAddress;
    }

    public void setActualSourceAddress(String actualSourceAddress) {
        this.actualSourceAddress = actualSourceAddress;
    }

    public String getActualDestinationAddress() {
        return actualDestinationAddress;
    }

    public void setActualDestinationAddress(String actualDestinationAddress) {
        this.actualDestinationAddress = actualDestinationAddress;
    }

    public boolean isRideStarted() {
        return rideStarted;
    }

    public void setRideStarted(boolean rideStarted) {
        this.rideStarted = rideStarted;
    }

    public String getFormatedRequestAt() {
        return formatedRequestAt;
    }

    public void setFormatedRequestAt(Date milliseconds) {
        this.formatedRequestAt = DateUtils.convertDateMilliSecondToString(milliseconds.getTime());
    }

    public String getFormatedAcceptedAt() {
        return formatedAcceptedAt;
    }

    public void setFormatedAcceptedAt(Date milliseconds) {
        this.formatedAcceptedAt = DateUtils.convertDateMilliSecondToString(milliseconds.getTime());;
    }

    public String getFormatedRideStartedAt() {
        return formatedRideStartedAt;
    }

    public void setFormatedRideStartedAt(Date milliseconds) {
        this.formatedRideStartedAt = DateUtils.convertDateMilliSecondToString(milliseconds.getTime());;
    }

    public String getFormatedRideEndedAt() {
        return formatedRideEndedAt;
    }

    public void setFormatedRideEndedAt(Date milliseconds) {
        this.formatedRideEndedAt = DateUtils.convertDateMilliSecondToString(milliseconds.getTime());
    }
}
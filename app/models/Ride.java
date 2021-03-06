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
    Date parcelRequestRaisedAt;
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
    boolean freeRide = false;
    boolean isPaid;
    private transient boolean userCustomer = false;
    private transient boolean userRider = false;
    Double freeRideDiscount;
    String modeOfPayment;
    String rideType = "Ride";
    String parcelPickupNumber;
    String parcelDropoffNumber;
    String parcelPickupImageName;
    String parcelDropoffImageName;
    String parcelPickupDetails;
    String parcelDropoffDetails;
    String parcelOrderId;
    String parcelReOrderId;
    String rideComments;
    Double codAmount;
    Long groupRideId;
    boolean isGroupRide;
    private transient String requestorName;
    private transient String riderName;
    private transient String formatedRequestAt;
    private transient String formatedAcceptedAt;
    private transient String formatedRideStartedAt;
    private transient String formatedRideEndedAt;
    private transient String customerMobileNumber;
    private transient String riderMobileNumber;
    private transient String ridePaymentStatus;
    private transient boolean processRideSource;
    private transient boolean processRideDestination;
    private transient Double endLatitude;
    private transient Double endLongitude;

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

    public void setFormatedRequestAt(Date date) {
        this.formatedRequestAt = DateUtils.convertDateToString(date, DateUtils.MMMDDYYYYHHMMSSA);
    }

    public String getFormatedAcceptedAt() {
        return formatedAcceptedAt;
    }

    public void setFormatedAcceptedAt(Date date) {
        this.formatedAcceptedAt = DateUtils.convertDateToString(date, DateUtils.MMMDDYYYYHHMMSSA);
    }

    public String getFormatedRideStartedAt() {
        return formatedRideStartedAt;
    }

    public void setFormatedRideStartedAt(Date date) {
        this.formatedRideStartedAt = DateUtils.convertDateToString(date, DateUtils.MMMDDYYYYHHMMSSA);
    }

    public String getFormatedRideEndedAt() {
        return formatedRideEndedAt;
    }

    public void setFormatedRideEndedAt(Date date) {
        this.formatedRideEndedAt = DateUtils.convertDateToString(date, DateUtils.MMMDDYYYYHHMMSSA);
    }

    public boolean isFreeRide() {
        return freeRide;
    }

    public void setFreeRide(boolean freeRide) {
        this.freeRide = freeRide;
    }

    public Double getFreeRideDiscount() {
        return freeRideDiscount;
    }

    public void setFreeRideDiscount(Double freeRideDiscount) {
        this.freeRideDiscount = freeRideDiscount;
    }

    public String getCustomerMobileNumber() {
        return customerMobileNumber;
    }

    public void setCustomerMobileNumber(String customerMobileNumber) {
        this.customerMobileNumber = customerMobileNumber;
    }

    public String getRiderMobileNumber() {
        return riderMobileNumber;
    }

    public void setRiderMobileNumber(String riderMobileNumber) {
        this.riderMobileNumber = riderMobileNumber;
    }

    public String getModeOfPayment() {
        return modeOfPayment;
    }

    public void setModeOfPayment(String modeOfPayment) {
        this.modeOfPayment = modeOfPayment;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public boolean isUserCustomer() {
        return userCustomer;
    }

    public void setUserCustomer(boolean userCustomer) {
        this.userCustomer = userCustomer;
    }

    public boolean isUserRider() {
        return userRider;
    }

    public void setUserRider(boolean userRider) {
        this.userRider = userRider;
    }

    public String getRidePaymentStatus() {
        return ridePaymentStatus;
    }

    public void setRidePaymentStatus(String ridePaymentStatus) {
        this.ridePaymentStatus = ridePaymentStatus;
    }

    public String getRideType() {
        return rideType;
    }

    public void setRideType(String rideType) {
        this.rideType = rideType;
    }

    public String getParcelPickupNumber() {
        return parcelPickupNumber;
    }

    public void setParcelPickupNumber(String parcelPickupNumber) {
        this.parcelPickupNumber = parcelPickupNumber;
    }

    public String getParcelDropoffNumber() {
        return parcelDropoffNumber;
    }

    public void setParcelDropoffNumber(String parcelDropoffNumber) {
        this.parcelDropoffNumber = parcelDropoffNumber;
    }

    public String getParcelPickupImageName() {
        return parcelPickupImageName;
    }

    public void setParcelPickupImageName(String parcelPickupImageName) {
        this.parcelPickupImageName = parcelPickupImageName;
    }

    public String getParcelDropoffImageName() {
        return parcelDropoffImageName;
    }

    public void setParcelDropoffImageName(String parcelDropoffImageName) {
        this.parcelDropoffImageName = parcelDropoffImageName;
    }

    public String getParcelPickupDetails() {
        return parcelPickupDetails;
    }

    public void setParcelPickupDetails(String parcelPickupDetails) {
        this.parcelPickupDetails = parcelPickupDetails;
    }

    public String getParcelDropoffDetails() {
        return parcelDropoffDetails;
    }

    public void setParcelDropoffDetails(String parcelDropoffDetails) {
        this.parcelDropoffDetails = parcelDropoffDetails;
    }

    public String getParcelOrderId() {
        return parcelOrderId;
    }

    public void setParcelOrderId(String parcelOrderId) {
        this.parcelOrderId = parcelOrderId;
    }

    public Double getCodAmount() {
        return codAmount;
    }

    public void setCodAmount(Double codAmount) {
        this.codAmount = codAmount;
    }

    public Date getParcelRequestRaisedAt() {
        return parcelRequestRaisedAt;
    }

    public void setParcelRequestRaisedAt(Date parcelRequestRaisedAt) {
        this.parcelRequestRaisedAt = parcelRequestRaisedAt;
    }

    public Long getGroupRideId() {
        return groupRideId;
    }

    public void setGroupRideId(Long groupRideId) {
        this.groupRideId = groupRideId;
    }

    public Double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(Double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public Double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(Double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public boolean isProcessRideSource() {
        return processRideSource;
    }

    public void setProcessRideSource(boolean processRideSource) {
        System.out.println("Setting source true for " + id);
        this.processRideSource = processRideSource;
    }

    public boolean isProcessRideDestination() {
        return processRideDestination;
    }

    public void setProcessRideDestination(boolean processRideDestination) {
        System.out.println("Setting destination true for " + id);
        this.processRideDestination = processRideDestination;
    }

    public boolean isGroupRide() {
        return isGroupRide;
    }

    public void setGroupRide(boolean groupRide) {
        isGroupRide = groupRide;
    }

    public String getRideComments() {
        return rideComments;
    }

    public void setRideComments(String rideComments) {
        this.rideComments = rideComments;
    }

    public String getParcelReOrderId() {
        return parcelReOrderId;
    }

    public void setParcelReOrderId(String parcelReOrderId) {
        this.parcelReOrderId = parcelReOrderId;
    }
}
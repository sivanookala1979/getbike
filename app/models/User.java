package models;

import com.avaje.ebean.Model;
import utils.StringUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by Siva Sudarsi on 19/10/16.
 */
@Entity
public class User extends Model {

    public static final Finder<Long, User> find = new Finder<Long, User>(User.class);

    @Id
    public Long id;

    public String name;
    public String email;
    public String password;
    public String role;
    public String phoneNumber;
    public char gender;
    public String authToken;
    @javax.persistence.Column(length = 1024)
    public String gcmCode;
    String vehiclePlateImageName;
    String vehicleNumber;
    String drivingLicenseImageName;
    String drivingLicenseNumber;
    boolean validProofsUploaded;
    Long currentRideId;
    boolean isRideInProgress;
    Long currentRequestRideId;
    boolean isRequestInProgress;
    Double lastKnownLatitude;
    Double lastKnownLongitude;
    Date lastLocationTime;
    String occupation;
    String city;
    String yearOfBirth;
    String homeLocation;
    String officeLocation;
    String profileImage;
    boolean mobileVerified;
    String accountHolderName;
    String accountNumber;
    String ifscCode;
    String bankName;
    String branchName;
    String promoCode;
    boolean isSpecialPrice;
    Double spePrice;
    String signupPromoCode;
    Integer freeRidesEarned;
    Integer freeRidesSpent;
    String appVersion;
    boolean appTutorialStatus;
    boolean driverAvailability;
    boolean primeRider;
    boolean vendor;
    String profileType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public char getGender() {
        return gender;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getGcmCode() {
        return gcmCode;
    }

    public void setGcmCode(String gcmCode) {
        this.gcmCode = gcmCode;
    }

    public String getVehiclePlateImageName() {
        return vehiclePlateImageName;
    }

    public void setVehiclePlateImageName(String vehiclePlateImageName) {
        this.vehiclePlateImageName = vehiclePlateImageName;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getDrivingLicenseImageName() {
        return drivingLicenseImageName;
    }

    public void setDrivingLicenseImageName(String drivingLicenseImageName) {
        this.drivingLicenseImageName = drivingLicenseImageName;
    }

    public String getDrivingLicenseNumber() {
        return drivingLicenseNumber;
    }

    public void setDrivingLicenseNumber(String drivingLicenseNumber) {
        this.drivingLicenseNumber = drivingLicenseNumber;
    }

    public boolean isValidProofsUploaded() {
        return validProofsUploaded;
    }

    public void setValidProofsUploaded(boolean validProofsUploaded) {
        this.validProofsUploaded = validProofsUploaded;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getCurrentRideId() {
        return currentRideId;
    }

    public void setCurrentRideId(Long currentRideId) {
        this.currentRideId = currentRideId;
    }

    public boolean isRideInProgress() {
        return isRideInProgress;
    }

    public void setRideInProgress(boolean rideInProgress) {
        isRideInProgress = rideInProgress;
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

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(String yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public String getHomeLocation() {
        return homeLocation;
    }

    public void setHomeLocation(String homeLocation) {
        this.homeLocation = homeLocation;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public boolean isMobileVerified() {
        return mobileVerified;
    }

    public void setMobileVerified(boolean mobileVerified) {
        this.mobileVerified = mobileVerified;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getDisplayName() {
        return StringUtils.isNotNullAndEmpty(name) ? name : phoneNumber;
    }

    public Long getCurrentRequestRideId() {
        return currentRequestRideId;
    }

    public void setCurrentRequestRideId(Long currentRequestRideId) {
        this.currentRequestRideId = currentRequestRideId;
    }

    public boolean isRequestInProgress() {
        return isRequestInProgress;
    }

    public void setRequestInProgress(boolean requestInProgress) {
        isRequestInProgress = requestInProgress;
    }

    public boolean isSpecialPrice() {
        return isSpecialPrice;
    }

    public void setSpecialPrice(boolean specialPrice) {
        isSpecialPrice = specialPrice;
    }

    public Double getSpePrice() {
        return spePrice;
    }

    public void setSpePrice(Double spePrice) {
        this.spePrice = spePrice;
    }

    public String getSignupPromoCode() {
        return signupPromoCode;
    }

    public void setSignupPromoCode(String signupPromoCode) {
        this.signupPromoCode = signupPromoCode;
    }

    public Integer getFreeRidesEarned() {
        return freeRidesEarned;
    }

    public void setFreeRidesEarned(Integer freeRidesEarned) {
        this.freeRidesEarned = freeRidesEarned;
    }

    public Integer getFreeRidesSpent() {
        return freeRidesSpent;
    }

    public void setFreeRidesSpent(Integer freeRidesSpent) {
        this.freeRidesSpent = freeRidesSpent;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public boolean isAppTutorialStatus() {
        return appTutorialStatus;
    }

    public void setAppTutorialStatus(boolean appTutorialStatus) {
        this.appTutorialStatus = appTutorialStatus;
    }

    public boolean isDriverAvailability() {
        return driverAvailability;
    }

    public void setDriverAvailability(boolean driverAvailability) {
        this.driverAvailability = driverAvailability;
    }

    public boolean isPrimeRider() {
        return primeRider;
    }

    public void setPrimeRider(boolean primeRider) {
        this.primeRider = primeRider;
    }

    public boolean isVendor() {
        return vendor;
    }

    public void setVendor(boolean vendor) {
        this.vendor = vendor;
    }

    public String getProfileType() {
        return profileType;
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }
}

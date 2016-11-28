package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

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
}

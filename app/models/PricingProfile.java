package models;

import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by adarsht on 09/12/16.
 */
@Entity
public class PricingProfile extends Model {
    @Id
    public Long id;
    public String name;
    public boolean fixedPrice;
    public Double fixedPriceAmount;
    public boolean hasBasePackage;
    public Double basePackageAmount;
    public Double basePackageKilometers;
    public Double basePackageMinutes;
    public Double additionalPerKilometer;
    public Double additionalPerMinute;

    public static Finder<Long, PricingProfile> find = new Finder<Long, PricingProfile>(PricingProfile.class);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFixedPrice() {
        return fixedPrice;
    }

    public void setFixedPrice(boolean fixedPrice) {
        this.fixedPrice = fixedPrice;
    }

    public Double getFixedPriceAmount() {
        return fixedPriceAmount;
    }

    public void setFixedPriceAmount(Double fixedPriceAmount) {
        this.fixedPriceAmount = fixedPriceAmount;
    }

    public boolean isHasBasePackage() {
        return hasBasePackage;
    }

    public void setHasBasePackage(boolean hasBasePackage) {
        this.hasBasePackage = hasBasePackage;
    }

    public Double getBasePackageAmount() {
        return basePackageAmount;
    }

    public void setBasePackageAmount(Double basePackageAmount) {
        this.basePackageAmount = basePackageAmount;
    }

    public Double getBasePackageKilometers() {
        return basePackageKilometers;
    }

    public void setBasePackageKilometers(Double basePackageKilometers) {
        this.basePackageKilometers = basePackageKilometers;
    }

    public Double getBasePackageMinutes() {
        return basePackageMinutes;
    }

    public void setBasePackageMinutes(Double basePackageMinutes) {
        this.basePackageMinutes = basePackageMinutes;
    }

    public Double getAdditionalPerKilometer() {
        return additionalPerKilometer;
    }

    public void setAdditionalPerKilometer(Double additionalPerKilometer) {
        this.additionalPerKilometer = additionalPerKilometer;
    }

    public Double getAdditionalPerMinute() {
        return additionalPerMinute;
    }

    public void setAdditionalPerMinute(Double additionalPerMinute) {
        this.additionalPerMinute = additionalPerMinute;
    }

    public static Finder<Long, PricingProfile> getFind() {
        return find;
    }

    public static void setFind(Finder<Long, PricingProfile> find) {
        PricingProfile.find = find;
    }


}

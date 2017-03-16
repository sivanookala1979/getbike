package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Ram on 10/3/17.
 */
@Entity
public class PromotionsBanner extends Model {

    @Id
    public Long id;
    String hdpiPromotionalBanner;
    String ldpiPromotionalBanner;
    String mdpiPromotionalBanner;
    String xhdpiPromotionalBanner;
    String xxhdpiPromotionalBanner;
    Boolean showThisBanner = false;
    String promotionsURL;

    public static Finder<Long , PromotionsBanner> find = new Finder<Long, PromotionsBanner>(PromotionsBanner.class);
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHdpiPromotionalBanner() {
        return hdpiPromotionalBanner;
    }

    public void setHdpiPromotionalBanner(String hdpiPromotionalBanner) {
        this.hdpiPromotionalBanner = hdpiPromotionalBanner;
    }

    public String getLdpiPromotionalBanner() {
        return ldpiPromotionalBanner;
    }

    public void setLdpiPromotionalBanner(String ldpiPromotionalBanner) {
        this.ldpiPromotionalBanner = ldpiPromotionalBanner;
    }

    public String getMdpiPromotionalBanner() {
        return mdpiPromotionalBanner;
    }

    public void setMdpiPromotionalBanner(String mdpiPromotionalBanner) {
        this.mdpiPromotionalBanner = mdpiPromotionalBanner;
    }

    public String getXhdpiPromotionalBanner() {
        return xhdpiPromotionalBanner;
    }

    public void setXhdpiPromotionalBanner(String xhdpiPromotionalBanner) {
        this.xhdpiPromotionalBanner = xhdpiPromotionalBanner;
    }

    public String getXxhdpiPromotionalBanner() {
        return xxhdpiPromotionalBanner;
    }

    public void setXxhdpiPromotionalBanner(String xxhdpiPromotionalBanner) {
        this.xxhdpiPromotionalBanner = xxhdpiPromotionalBanner;
    }

    public Boolean getShowThisBanner() {
        return showThisBanner;
    }

    public void setShowThisBanner(Boolean showThisBanner) {
        this.showThisBanner = showThisBanner;
    }

    public String getPromotionsURL() {
        return promotionsURL;
    }

    public void setPromotionsURL(String promotionsURL) {
        this.promotionsURL = promotionsURL;
    }
}

package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by Siva Sudarsi on 19/10/16.
 */
@Entity
public class LoginOtp extends Model {

    public static final Finder<Long, LoginOtp> find = new Finder<Long, LoginOtp>(LoginOtp.class);

    @Id
    public Long id;
    Long userId;
    String generatedOtp;
    Date createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getGeneratedOtp() {
        return generatedOtp;
    }

    public void setGeneratedOtp(String generatedOtp) {
        this.generatedOtp = generatedOtp;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }


}

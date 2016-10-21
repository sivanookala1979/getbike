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
}

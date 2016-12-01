package models;

import com.avaje.ebean.Model;

import javax.persistence.Id;
import javax.persistence.Entity;

/**
 * Created by torato on 1/12/16.
 */
@Entity
public class SystemSettings extends Model {

    @Id
    public Long id;

    private String key;
    private String value;

    public static Finder<Long , SystemSettings> find = new Finder<Long, SystemSettings>(SystemSettings.class);

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString(){
        return "key--->"+this.key+" value->"+this.value;
    }
}

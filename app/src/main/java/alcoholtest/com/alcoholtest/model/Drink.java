package alcoholtest.com.alcoholtest.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Drink {
    User user;
    Mixture mixture;
    long time;

    public Drink(User user, Mixture mixture, long time) {
        this.user = user;
        this.mixture = mixture;
        this.time = time;
    }

    public User getUser() {
        return user;
    }

    public Mixture getMixture() {
        return mixture;
    }

    public long getTime() {
        return time;
    }

}

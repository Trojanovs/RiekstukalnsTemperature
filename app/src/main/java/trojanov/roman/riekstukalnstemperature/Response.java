package trojanov.roman.riekstukalnstemperature;

/**
 * Created by Roman on 16.01.2015.
 */
public class Response {
    private String temperature;
    private String time;

    public Response(String temperature, String time) {
        this.temperature = temperature;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
}

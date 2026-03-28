package neopetsmoneymaker;

public class NeoSetting {

    private String  name,
                    value;

    public NeoSetting( String name, String value ) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}

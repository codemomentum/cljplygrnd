package org.codemomentum.cljplygrnd.serializer;


public class CustomBusinessObject {
    public static final String myField = "field1";

    public Integer number;

    public CustomBusinessObject(Integer number) {
        this.number = number;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}

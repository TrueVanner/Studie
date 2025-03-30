package nl.tue.appdev.studie;

public class Groups {
    private String name, code;
    private boolean isPublic;

    public Groups(){

    }
    public Groups(String name, String code, boolean isPublic){
        this.name=name;
        this.code=code;
        this.isPublic=isPublic;
    }
    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    public String getcode() {
        return code;
    }

    // setter method for all variables.
    public void setcode(String code) {
        this.code = code;
    }

    public boolean getisPublic() {
        return isPublic;
    }

    public void setisPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}

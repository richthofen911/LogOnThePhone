package ap1.com.calibratedbeacondetection;

/**
 * Created by admin on 21/04/15.
 */
public class Beacon {
    private String uuid;
    private String major;
    private String minor;
    private String companyId;
    private String umm;
    private String beaconId;
    private boolean inoutStatus;

    public Beacon(String beaconId, String uuid, String major, String minor, String companyId){
        this.beaconId = beaconId;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.companyId = companyId;
        this.umm = uuid + major + minor;
    }

    public void setInoutStatus(boolean status){
        inoutStatus = status;
    }

    public boolean getInoutStatus(){
        return inoutStatus;
    }

    public String getBeaconId(){
        return beaconId;
    }

    public String getUuid(){
        return uuid;
    }

    public String getMajor(){
        return major;
    }

    public String getMinor(){
        return minor;
    }

    public String getCompanyId(){
        return companyId;
    }

    public String getUmm(){
        return umm;
    }
}

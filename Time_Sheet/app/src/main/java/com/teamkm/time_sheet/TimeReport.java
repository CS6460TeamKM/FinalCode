package com.teamkm.time_sheet;

/**
 * Created by praveena on 12/2/17.
 */

public class TimeReport {

    public String getReg() {
        return reg  ;
    }

    public void setReg(String reg) {

        this.reg = reg;
    }

    public String getHol() {
        return hol;
    }

    public void setHol(String hol) {

        this.hol = hol;
    }

    public String getVac() {
        return vac ;
    }

    public void setVac(String vac) {

        this.vac = vac;
    }

    public String getSic() {
        return sic ;
    }

    public void setSic(String sic) {

        this.sic = sic;
    }

    public String getOh() {
        return oh ;
    }

    public void setOh(String oh) {

        this.oh = oh;  }

    public String getOc() {
        return oc  ;
    }

    public void setOc(String oc) {

        this.oc = oc;
    }

    public String getTot() {
        return tot ;
    }

    public void setTot() {
        //tot = Integer.toString(setTot());
        this.tot = Integer.toString(setTot1());
    }

    public int setTot1 ()
    {
        if(reg.isEmpty()){reg = "0";}
        if(hol.isEmpty()){hol = "0";}
        if(vac.isEmpty()){vac = "0";}
        if(sic.isEmpty()){sic = "0";}
        if(oh.isEmpty()){oh = "0";}
        if(oc.isEmpty()){oc = "0";}



        int tot = Integer.parseInt(reg)- (Integer.parseInt(hol)+Integer.parseInt(vac)+Integer.parseInt(sic))+
                Integer.parseInt(oh)+
                Integer.parseInt(oc);



        return tot < 0 ? 0 : tot;
    }

    public String reg = "0";
    public String hol = "0";
    public String vac = "0";
    public String sic = "0";
    public String oh = "0";
    public String oc = "0";
    public String tot = "0";





}


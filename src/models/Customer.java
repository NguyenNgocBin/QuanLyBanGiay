package models;

public class Customer {
    private String MaKH;
    private String HoTen;
    private String Sdt;
    private String Email;
    private double TongchiTieu;

    public Customer(String MaKH, String HoTen, String Sdt, String Email, double TongchiTieu) {
        this.MaKH = MaKH;
        this.HoTen = HoTen;
        this.Sdt = Sdt;
        this.Email = Email;
        this.TongchiTieu = TongchiTieu;
    }

    public String getMaKH() {
        return MaKH;
    }

    public void setMaKH(String MaKH) {
        this.MaKH = MaKH;
    }

    public String getHoTen() {
        return HoTen;
    }

    public void setHoTen(String HoTen) {
        this.HoTen = HoTen;
    }

    public String getSdt() {
        return Sdt;
    }

    public void setSdt(String Sdt) {
        this.Sdt = Sdt;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public double getTongchiTieu() {
        return TongchiTieu;
    }

    public void setTongchiTieu(double TongchiTieu) {
        this.TongchiTieu = TongchiTieu;
    }

}

package cuoiky;

public class GioHangItem {
    public String ten;
    public double giaGoc;
    public double giaMoi;
    public String hinhAnhPath;
    public String cauHinh;
    public int soLuong;
    public boolean isSelected = false; // Đã khôi phục phục vụ tích chọn Checkbox

    public GioHangItem(String ten, double giaGoc, double giaMoi, String hinhAnhPath, String cauHinh, int soLuong) {
        this.ten = ten;
        this.giaGoc = giaGoc;
        this.giaMoi = giaMoi;
        this.hinhAnhPath = hinhAnhPath;
        this.cauHinh = cauHinh;
        this.soLuong = soLuong;
    }
}
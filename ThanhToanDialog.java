package cuoiky;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ThanhToanDialog extends JFrame {
    private double tongTien = 0;
    private JLabel lblTongTien;
    private TrangChu parent;
    private List<GioHangItem> danhSachMua;

    private Map<String, String[]> mapDiaGioi;
    private JComboBox<String> cbxTinhThanh;
    private JComboBox<String> cbxQuanHuyen;
    private JTextField txtHoTen, txtDienThoai, txtGhiChu, txtDiaChi;

    public ThanhToanDialog(TrangChu parent, List<GioHangItem> danhSachMua) {
        this.parent = parent;
        this.danhSachMua = danhSachMua;

        khoiTaoDuLieuDiaGioi();
        setTitle("Giỏ Hàng & Thanh Toán - Techcare");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 750);
        setLocationRelativeTo(parent);
        setResizable(true);

        Color bgColor = new Color(240, 242, 245);
        getContentPane().setBackground(bgColor);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(bgColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        Dimension blockDimension = new Dimension(750, 200);
        Dimension inputBlockDimension = new Dimension(750, 110);
        Dimension buttonDimension = new Dimension(750, 50);

        // --- 1. GIỎ HÀNG (CÓ CHECKBOX & XÓA LẺ) ---
        JPanel pnlDanhSach = new JPanel();
        pnlDanhSach.setLayout(new BoxLayout(pnlDanhSach, BoxLayout.Y_AXIS));
        pnlDanhSach.setBackground(Color.WHITE);

        for (GioHangItem item : danhSachMua) {
            pnlDanhSach.add(taoPanelSanPham(item));
            pnlDanhSach.add(Box.createVerticalStrut(10));
        }

        JScrollPane scrollGioHang = new JScrollPane(pnlDanhSach);
        scrollGioHang.setPreferredSize(blockDimension);
        scrollGioHang.setMaximumSize(blockDimension);
        scrollGioHang.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollGioHang.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollGioHang.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)), "Giỏ hàng của bạn", TitledBorder.RIGHT, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 14), Color.GRAY));
        mainPanel.add(scrollGioHang); mainPanel.add(Box.createVerticalStrut(10));

        // --- NÚT XÓA HÀNG LOẠT ---
        JPanel pnlXoaHangLoat = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlXoaHangLoat.setOpaque(false);
        pnlXoaHangLoat.setMaximumSize(new Dimension(750, 40));
        JButton btnXoaDaChon = new JButton("Xóa các mục đã chọn");
        btnXoaDaChon.setBackground(new Color(220, 53, 69));
        btnXoaDaChon.setForeground(Color.WHITE);
        btnXoaDaChon.setFont(new Font("Arial", Font.BOLD, 12));
        btnXoaDaChon.addActionListener(e -> {
            int count = 0;
            for (int i = danhSachMua.size() - 1; i >= 0; i--) {
                if (danhSachMua.get(i).isSelected) {
                    parent.xoaKhoiGioHang(danhSachMua.get(i).ten);
                    count++;
                }
            }
            if (count > 0) {
                JOptionPane.showMessageDialog(this, "Đã xóa " + count + " sản phẩm được chọn!");
                this.dispose();
                if (!TrangChu.gioHang.isEmpty()) {
                    new ThanhToanDialog(parent, TrangChu.gioHang).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng tích chọn Checkbox trước sản phẩm cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        });
        pnlXoaHangLoat.add(btnXoaDaChon);
        mainPanel.add(pnlXoaHangLoat); mainPanel.add(Box.createVerticalStrut(15));

        // --- 2. TỔNG TIỀN ---
        lblTongTien = new JLabel();
        tinhTongTien();

        JPanel pnlTongTien = new JPanel(new BorderLayout()); pnlTongTien.setBackground(Color.WHITE);
        pnlTongTien.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)), BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        pnlTongTien.setMaximumSize(new Dimension(750, 80)); pnlTongTien.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlTongTien.add(new JLabel("<html><b style='font-size:16px;'>Tổng tiền (VNĐ): </b></html>"), BorderLayout.WEST); pnlTongTien.add(lblTongTien, BorderLayout.EAST); pnlTongTien.add(new JLabel("<html><center style='color:#ff6600; font-weight:bold; margin-top:5px;'>MUA NGAY NHẬN VOUCHER 200.000 VNĐ</center></html>", SwingConstants.CENTER), BorderLayout.SOUTH);
        mainPanel.add(pnlTongTien); mainPanel.add(Box.createVerticalStrut(20));

        // --- 3. LIÊN HỆ ---
        JPanel pnlLienHe = new JPanel(new GridLayout(2, 2, 15, 10)); pnlLienHe.setBackground(Color.WHITE); pnlLienHe.setMaximumSize(inputBlockDimension); pnlLienHe.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlLienHe.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)), "THÔNG TIN LIÊN HỆ", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 14)));
        txtHoTen = createPlaceholderField("Họ tên (Bắt buộc)"); txtDienThoai = createPlaceholderField("Điện thoại (Bắt buộc)"); txtGhiChu = createPlaceholderField("Ghi chú...");
        pnlLienHe.add(txtHoTen); pnlLienHe.add(txtDienThoai); pnlLienHe.add(txtGhiChu);
        mainPanel.add(pnlLienHe); mainPanel.add(Box.createVerticalStrut(20));

        // --- 4. CÁCH THỨC NHẬN HÀNG ---
        JPanel pnlNhanHang = new JPanel(new BorderLayout()); pnlNhanHang.setBackground(Color.WHITE); pnlNhanHang.setMaximumSize(blockDimension); pnlNhanHang.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlNhanHang.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)), "CHỌN CÁCH THỨC NHẬN HÀNG", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 14)));

        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT)); pnlRadio.setBackground(Color.WHITE);
        JRadioButton rdoGiaoTanNoi = new JRadioButton("Giao tận nơi", true); rdoGiaoTanNoi.setBackground(Color.WHITE); JRadioButton rdoNhanTaiCuaHang = new JRadioButton("Nhận tại cửa hàng"); rdoNhanTaiCuaHang.setBackground(Color.WHITE);
        ButtonGroup bgNhanHang = new ButtonGroup(); bgNhanHang.add(rdoGiaoTanNoi); bgNhanHang.add(rdoNhanTaiCuaHang); pnlRadio.add(rdoGiaoTanNoi); pnlRadio.add(rdoNhanTaiCuaHang); pnlNhanHang.add(pnlRadio, BorderLayout.NORTH);

        CardLayout cardLayout = new CardLayout(); JPanel pnlContainer = new JPanel(cardLayout);
        JPanel pnlGiao = new JPanel(new GridLayout(2, 1, 10, 10)); pnlGiao.setBackground(new Color(250, 250, 250)); pnlGiao.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel pnlCombo = new JPanel(new GridLayout(1, 2, 10, 10)); pnlCombo.setOpaque(false);
        cbxTinhThanh = new JComboBox<>(); cbxQuanHuyen = new JComboBox<>(); cbxTinhThanh.addItem("-- Chọn Tỉnh/Thành --"); cbxQuanHuyen.addItem("-- Chọn Quận/Huyện --"); cbxQuanHuyen.setEnabled(false);

        for (String tinh : mapDiaGioi.keySet()) cbxTinhThanh.addItem(tinh);
        cbxTinhThanh.addActionListener(e -> {
            String t = (String) cbxTinhThanh.getSelectedItem();
            cbxQuanHuyen.removeAllItems(); cbxQuanHuyen.addItem("-- Chọn Quận/Huyện --");
            if (t != null && !t.equals("-- Chọn Tỉnh/Thành --")) {
                String[] h = mapDiaGioi.get(t);
                if (h != null) for (String dist : h) cbxQuanHuyen.addItem(dist);
                cbxQuanHuyen.setEnabled(true);
            } else cbxQuanHuyen.setEnabled(false);
        });
        pnlCombo.add(cbxTinhThanh); pnlCombo.add(cbxQuanHuyen); pnlGiao.add(pnlCombo);
        txtDiaChi = createPlaceholderField("Địa chỉ (Số nhà, tên đường...) (Bắt buộc)"); pnlGiao.add(txtDiaChi);

        JPanel pnlCuaHang = new JPanel(new GridLayout(3, 1, 5, 5)); pnlCuaHang.setBackground(new Color(250, 250, 250)); pnlCuaHang.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlCuaHang.add(new JRadioButton("TechCare : 133 Hàm Nghi", true)); pnlCuaHang.add(new JRadioButton("TechCare : 99 Hàm Nghi")); pnlCuaHang.add(new JRadioButton("TechCare : 101 Nguyễn Văn Thoại"));

        pnlContainer.add(pnlGiao, "GIAO"); pnlContainer.add(pnlCuaHang, "STORE");
        rdoGiaoTanNoi.addActionListener(e -> cardLayout.show(pnlContainer, "GIAO")); rdoNhanTaiCuaHang.addActionListener(e -> cardLayout.show(pnlContainer, "STORE"));
        pnlNhanHang.add(pnlContainer, BorderLayout.CENTER); mainPanel.add(pnlNhanHang); mainPanel.add(Box.createVerticalStrut(25));

        // --- 5. NÚT ĐẶT HÀNG ---
        JButton btnDatHang = new JButton("ĐẶT HÀNG NGAY");
        btnDatHang.setBackground(new Color(255, 87, 34)); btnDatHang.setForeground(Color.WHITE); btnDatHang.setFont(new Font("Arial", Font.BOLD, 18));
        btnDatHang.setPreferredSize(buttonDimension); btnDatHang.setMaximumSize(buttonDimension); btnDatHang.setAlignmentX(Component.CENTER_ALIGNMENT); btnDatHang.setFocusPainted(false); btnDatHang.setBorder(BorderFactory.createEmptyBorder());

        btnDatHang.addActionListener(e -> {
            String hoTen = txtHoTen.getText().trim(); String dienThoai = txtDienThoai.getText().trim(); String diaChi = txtDiaChi.getText().trim();
            String tinh = (String) cbxTinhThanh.getSelectedItem(); String huyen = (String) cbxQuanHuyen.getSelectedItem();

            if (danhSachMua.isEmpty()) { JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống!", "Báo lỗi", JOptionPane.ERROR_MESSAGE); return; }
            if (hoTen.isEmpty() || hoTen.equals("Họ tên (Bắt buộc)")) { JOptionPane.showMessageDialog(this, "Vui lòng nhập Họ tên!", "Báo lỗi", JOptionPane.ERROR_MESSAGE); return; }
            if (!dienThoai.matches("^0\\d{9}$")) { JOptionPane.showMessageDialog(this, "SĐT phải gồm 10 số và bắt đầu bằng số 0!", "Báo lỗi", JOptionPane.ERROR_MESSAGE); return; }

            if (rdoGiaoTanNoi.isSelected()) {
                if(tinh.equals("-- Chọn Tỉnh/Thành --") || huyen.equals("-- Chọn Quận/Huyện --") || diaChi.isEmpty() || diaChi.equals("Địa chỉ (Số nhà, tên đường...) (Bắt buộc)")) {
                    JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ địa chỉ giao hàng!", "Báo lỗi", JOptionPane.ERROR_MESSAGE); return;
                }
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                // 1. KIỂM TRA TỒN KHO TRƯỚC KHI ĐẶT
                for (GioHangItem item : danhSachMua) {
                    PreparedStatement psCheckKho = conn.prepareStatement("SELECT SoLuongTonKho FROM SanPham WHERE TenSP = ?");
                    psCheckKho.setString(1, item.ten);
                    ResultSet rsKho = psCheckKho.executeQuery();
                    if (rsKho.next()) {
                        int tonKho = rsKho.getInt("SoLuongTonKho");
                        if (item.soLuong > tonKho) {
                            JOptionPane.showMessageDialog(this, "Sản phẩm '" + item.ten + "' chỉ còn " + tonKho + " máy trong kho. Vui lòng giảm số lượng!", "Hết hàng", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }

                // ĐÃ SỬA: Bổ sung thêm TaiKhoan vào câu lệnh INSERT
                String sqlInsert = "INSERT INTO DonHang (TenKhachHang, MaSP, TenSP, GiaMua, HinhAnh, CauHinh, SoLuong, TrangThai, TaiKhoan) VALUES (?, ?, ?, ?, ?, ?, ?, N'Chờ xác nhận', ?)";
                String sqlUpdateKho = "UPDATE SanPham SET SoLuongTonKho = SoLuongTonKho - ? WHERE TenSP = ?";

                for (GioHangItem item : danhSachMua) {
                    PreparedStatement ps = conn.prepareStatement(sqlInsert);
                    ps.setString(1, hoTen);
                    String maSP = "UNKNOWN";
                    PreparedStatement psCheck = conn.prepareStatement("SELECT MaSP FROM SanPham WHERE TenSP = ?"); psCheck.setString(1, item.ten);
                    ResultSet rsCheck = psCheck.executeQuery(); if(rsCheck.next()) maSP = rsCheck.getString("MaSP");

                    ps.setString(2, maSP); ps.setString(3, item.ten); ps.setDouble(4, item.giaMoi);
                    ps.setString(5, item.hinhAnhPath); ps.setString(6, item.cauHinh); ps.setInt(7, item.soLuong);
                    ps.setString(8, TrangChu.taiKhoanHienTai); // Truyền tài khoản đang đăng nhập vào đây
                    ps.executeUpdate();

                    PreparedStatement psTruKho = conn.prepareStatement(sqlUpdateKho);
                    psTruKho.setInt(1, item.soLuong); psTruKho.setString(2, item.ten); psTruKho.executeUpdate();
                }
            } catch (Exception ex) { ex.printStackTrace(); }

            JOptionPane.showMessageDialog(this, "Đơn hàng " + String.format("%,.0f đ", tongTien) + " đang ở trạng thái CHỜ XÁC NHẬN.");
            parent.lamSachGioHang(); this.dispose();
        });
        mainPanel.add(btnDatHang);

        JScrollPane mainScroll = new JScrollPane(mainPanel); mainScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); mainScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED); mainScroll.setBorder(null); mainScroll.getViewport().setBackground(bgColor);
        add(mainScroll, BorderLayout.CENTER);
    }

    // ==============================================================
    // CÁC HÀM XỬ LÝ (NẰM Ở CUỐI CLASS) MÀ BẠN COPY BỊ THIẾU
    // ==============================================================

    private void khoiTaoDuLieuDiaGioi() {
        mapDiaGioi = new LinkedHashMap<>();
        mapDiaGioi.put("Hà Nội", new String[]{"Quận Ba Đình", "Quận Hoàn Kiếm", "Quận Tây Hồ", "Quận Long Biên", "Quận Cầu Giấy", "Quận Đống Đa", "Quận Hai Bà Trưng", "Quận Hoàng Mai", "Quận Thanh Xuân", "Quận Hà Đông", "Quận Bắc Từ Liêm", "Quận Nam Từ Liêm", "Thị xã Sơn Tây", "Huyện Ba Vì", "Huyện Chương Mỹ", "Huyện Đan Phượng", "Huyện Đông Anh", "Huyện Gia Lâm", "Huyện Hoài Đức", "Huyện Mê Linh", "Huyện Mỹ Đức", "Huyện Phú Xuyên", "Huyện Phúc Thọ", "Huyện Quốc Oai", "Huyện Sóc Sơn", "Huyện Thạch Thất", "Huyện Thanh Oai", "Huyện Thanh Trì", "Huyện Thường Tín", "Huyện Ứng Hòa"});
        mapDiaGioi.put("TP. Hồ Chí Minh", new String[]{"Quận 1", "Quận 3", "Quận 4", "Quận 5", "Quận 6", "Quận 7", "Quận 8", "Quận 10", "Quận 11", "Quận 12", "Quận Bình Tân", "Quận Bình Thạnh", "Quận Gò Vấp", "Quận Phú Nhuận", "Quận Tân Bình", "Quận Tân Phú", "TP. Thủ Đức", "Huyện Bình Chánh", "Huyện Cần Giờ", "Huyện Củ Chi", "Huyện Hóc Môn", "Huyện Nhà Bè"});
        mapDiaGioi.put("Đà Nẵng", new String[]{"Quận Hải Châu", "Quận Cẩm Lệ", "Quận Thanh Khê", "Quận Liên Chiểu", "Quận Ngũ Hành Sơn", "Quận Sơn Trà", "Huyện Hòa Vang", "Huyện Hoàng Sa"});
        mapDiaGioi.put("Hải Phòng", new String[]{"Quận Đồ Sơn", "Quận Dương Kinh", "Quận Hải An", "Quận Hồng Bàng", "Quận Kiến An", "Quận Lê Chân", "Quận Ngô Quyền", "Huyện An Dương", "Huyện An Lão", "Huyện Bạch Long Vĩ", "Huyện Cát Hải", "Huyện Kiến Thụy", "Huyện Thủy Nguyên", "Huyện Tiên Lãng", "Huyện Vĩnh Bảo"});
        mapDiaGioi.put("Cần Thơ", new String[]{"Quận Bình Thủy", "Quận Cái Răng", "Quận Ninh Kiều", "Quận Ô Môn", "Quận Thốt Nốt", "Huyện Cờ Đỏ", "Huyện Phong Điền", "Huyện Thới Lai", "Huyện Vĩnh Thạnh"});
        mapDiaGioi.put("An Giang", new String[]{"TP. Long Xuyên", "TP. Châu Đốc", "TX. Tân Châu", "Huyện An Phú", "Huyện Châu Phú", "Huyện Châu Thành", "Huyện Chợ Mới", "Huyện Phú Tân", "Huyện Thoại Sơn", "Huyện Tịnh Biên", "Huyện Tri Tôn"});
        mapDiaGioi.put("Bà Rịa - Vũng Tàu", new String[]{"TP. Vũng Tàu", "TP. Bà Rịa", "TX. Phú Mỹ", "Huyện Châu Đức", "Huyện Côn Đảo", "Huyện Đất Đỏ", "Huyện Long Điền", "Huyện Xuyên Mộc"});
        mapDiaGioi.put("Bắc Giang", new String[]{"TP. Bắc Giang", "Huyện Hiệp Hòa", "Huyện Lạng Giang", "Huyện Lục Nam", "Huyện Lục Ngạn", "Huyện Sơn Động", "Huyện Tân Yên", "Huyện Việt Yên", "Huyện Yên Dũng", "Huyện Yên Thế"});
        mapDiaGioi.put("Bắc Kạn", new String[]{"TP. Bắc Kạn", "Huyện Ba Bể", "Huyện Bạch Thông", "Huyện Chợ Đồn", "Huyện Chợ Mới", "Huyện Na Rì", "Huyện Ngân Sơn", "Huyện Pác Nặm"});
        mapDiaGioi.put("Bạc Liêu", new String[]{"TP. Bạc Liêu", "TX. Giá Rai", "Huyện Đông Hải", "Huyện Hòa Bình", "Huyện Hồng Dân", "Huyện Phước Long", "Huyện Vĩnh Lợi"});
        mapDiaGioi.put("Bắc Ninh", new String[]{"TP. Bắc Ninh", "TP. Từ Sơn", "Huyện Gia Bình", "Huyện Lương Tài", "Huyện Quế Võ", "Huyện Thuận Thành", "Huyện Tiên Du", "Huyện Yên Phong"});
        mapDiaGioi.put("Bến Tre", new String[]{"TP. Bến Tre", "Huyện Ba Tri", "Huyện Bình Đại", "Huyện Châu Thành", "Huyện Chợ Lách", "Huyện Giồng Trôm", "Huyện Mỏ Cày Bắc", "Huyện Mỏ Cày Nam", "Huyện Thạnh Phú"});
        mapDiaGioi.put("Bình Định", new String[]{"TP. Quy Nhơn", "TX. An Nhơn", "TX. Hoài Nhơn", "Huyện An Lão", "Huyện Hoài Ân", "Huyện Phù Cát", "Huyện Phù Mỹ", "Huyện Tây Sơn", "Huyện Tuy Phước", "Huyện Vân Canh", "Huyện Vĩnh Thạnh"});
        mapDiaGioi.put("Bình Dương", new String[]{"TP. Thủ Dầu Một", "TP. Dĩ An", "TP. Thuận An", "TX. Bến Cát", "TX. Tân Uyên", "Huyện Bàu Bàng", "Huyện Bắc Tân Uyên", "Huyện Dầu Tiếng", "Huyện Phú Giáo"});
        mapDiaGioi.put("Bình Phước", new String[]{"TP. Đồng Xoài", "TX. Bình Long", "TX. Phước Long", "Huyện Bù Đăng", "Huyện Bù Đốp", "Huyện Bù Gia Mập", "Huyện Chơn Thành", "Huyện Đồng Phú", "Huyện Hớn Quản", "Huyện Lộc Ninh", "Huyện Phú Riềng"});
        mapDiaGioi.put("Bình Thuận", new String[]{"TP. Phan Thiết", "TX. La Gi", "Huyện Bắc Bình", "Huyện Đức Linh", "Huyện Hàm Tân", "Huyện Hàm Thuận Bắc", "Huyện Hàm Thuận Nam", "Huyện Phú Quý", "Huyện Tánh Linh", "Huyện Tuy Phong"});
        mapDiaGioi.put("Cà Mau", new String[]{"TP. Cà Mau", "Huyện Cái Nước", "Huyện Đầm Dơi", "Huyện Năm Căn", "Huyện Ngọc Hiển", "Huyện Phú Tân", "Huyện Thới Bình", "Huyện Trần Văn Thời", "Huyện U Minh"});
        mapDiaGioi.put("Cao Bằng", new String[]{"TP. Cao Bằng", "Huyện Bảo Lâm", "Huyện Bảo Lạc", "Huyện Hạ Lang", "Huyện Hà Quảng", "Huyện Hòa An", "Huyện Nguyên Bình", "Huyện Quảng Hòa", "Huyện Thạch An", "Huyện Trùng Khánh"});
        mapDiaGioi.put("Đắk Lắk", new String[]{"TP. Buôn Ma Thuột", "TX. Buôn Hồ", "Huyện Buôn Đôn", "Huyện Cư Kuin", "Huyện Cư M'gar", "Huyện Ea H'leo", "Huyện Ea Kar", "Huyện Ea Súp", "Huyện Krông Ana", "Huyện Krông Bông", "Huyện Krông Búk", "Huyện Krông Năng", "Huyện Krông Pắc", "Huyện Lắk", "Huyện M'Đrắk"});
        mapDiaGioi.put("Đắk Nông", new String[]{"TP. Gia Nghĩa", "Huyện Cư Jút", "Huyện Đắk Glong", "Huyện Đắk Mil", "Huyện Đắk R'lấp", "Huyện Đắk Song", "Huyện Krông Nô", "Huyện Tuy Đức"});
        mapDiaGioi.put("Điện Biên", new String[]{"TP. Điện Biên Phủ", "TX. Mường Lay", "Huyện Điện Biên", "Huyện Điện Biên Đông", "Huyện Mường Ảng", "Huyện Mường Chà", "Huyện Mường Nhé", "Huyện Nậm Pồ", "Huyện Tủa Chùa", "Huyện Tuần Giáo"});
        mapDiaGioi.put("Đồng Nai", new String[]{"TP. Biên Hòa", "TP. Long Khánh", "Huyện Cẩm Mỹ", "Huyện Định Quán", "Huyện Long Thành", "Huyện Nhơn Trạch", "Huyện Tân Phú", "Huyện Thống Nhất", "Huyện Trảng Bom", "Huyện Vĩnh Cửu", "Huyện Xuân Lộc"});
        mapDiaGioi.put("Đồng Tháp", new String[]{"TP. Cao Lãnh", "TP. Sa Đéc", "TP. Hồng Ngự", "Huyện Cao Lãnh", "Huyện Châu Thành", "Huyện Hồng Ngự", "Huyện Lai Vung", "Huyện Lấp Vò", "Huyện Tam Nông", "Huyện Tân Hồng", "Huyện Thanh Bình", "Huyện Tháp Mười"});
        mapDiaGioi.put("Gia Lai", new String[]{"TP. Pleiku", "TX. An Khê", "TX. Ayun Pa", "Huyện Chư Păh", "Huyện Chư Prông", "Huyện Chư Pưh", "Huyện Chư Sê", "Huyện Đắk Đoa", "Huyện Đắk Pơ", "Huyện Đức Cơ", "Huyện Ia Grai", "Huyện Ia Pa", "Huyện KBang", "Huyện Kông Chro", "Huyện Krông Pa", "Huyện Mang Yang"});
        mapDiaGioi.put("Hà Giang", new String[]{"TP. Hà Giang", "Huyện Bắc Mê", "Huyện Bắc Quang", "Huyện Đồng Văn", "Huyện Hoàng Su Phì", "Huyện Mèo Vạc", "Huyện Quản Bạ", "Huyện Quang Bình", "Huyện Vị Xuyên", "Huyện Xín Mần", "Huyện Yên Minh"});
        mapDiaGioi.put("Hà Nam", new String[]{"TP. Phủ Lý", "TX. Duy Tiên", "Huyện Bình Lục", "Huyện Kim Bảng", "Huyện Lý Nhân", "Huyện Thanh Liêm"});
        mapDiaGioi.put("Hà Tĩnh", new String[]{"TP. Hà Tĩnh", "TX. Hồng Lĩnh", "TX. Kỳ Anh", "Huyện Cẩm Xuyên", "Huyện Can Lộc", "Huyện Đức Thọ", "Huyện Hương Khê", "Huyện Hương Sơn", "Huyện Kỳ Anh", "Huyện Lộc Hà", "Huyện Nghi Xuân", "Huyện Thạch Hà", "Huyện Vũ Quang"});
        mapDiaGioi.put("Hải Dương", new String[]{"TP. Hải Dương", "TP. Chí Linh", "TX. Kinh Môn", "Huyện Bình Giang", "Huyện Cẩm Giàng", "Huyện Gia Lộc", "Huyện Kim Thành", "Huyện Nam Sách", "Huyện Ninh Giang", "Huyện Thanh Hà", "Huyện Thanh Miện", "Huyện Tứ Kỳ"});
        mapDiaGioi.put("Hậu Giang", new String[]{"TP. Vị Thanh", "TP. Ngã Bảy", "TX. Long Mỹ", "Huyện Châu Thành", "Huyện Châu Thành A", "Huyện Long Mỹ", "Huyện Phụng Hiệp", "Huyện Vị Thủy"});
        mapDiaGioi.put("Hòa Bình", new String[]{"TP. Hòa Bình", "Huyện Cao Phong", "Huyện Đà Bắc", "Huyện Kim Bôi", "Huyện Lạc Sơn", "Huyện Lạc Thủy", "Huyện Lương Sơn", "Huyện Mai Châu", "Huyện Tân Lạc", "Huyện Yên Thủy"});
        mapDiaGioi.put("Hưng Yên", new String[]{"TP. Hưng Yên", "TX. Mỹ Hào", "Huyện Ân Thi", "Huyện Khoái Châu", "Huyện Kim Động", "Huyện Phù Cừ", "Huyện Tiên Lữ", "Huyện Văn Giang", "Huyện Văn Lâm", "Huyện Yên Mỹ"});
        mapDiaGioi.put("Khánh Hòa", new String[]{"TP. Nha Trang", "TP. Cam Ranh", "TX. Ninh Hòa", "Huyện Cam Lâm", "Huyện Diên Khánh", "Huyện Khánh Sơn", "Huyện Khánh Vĩnh", "Huyện Trường Sa", "Huyện Vạn Ninh"});
        mapDiaGioi.put("Kiên Giang", new String[]{"TP. Rạch Giá", "TP. Hà Tiên", "TP. Phú Quốc", "Huyện An Biên", "Huyện An Minh", "Huyện Châu Thành", "Huyện Giang Thành", "Huyện Giồng Riềng", "Huyện Gò Quao", "Huyện Hòn Đất", "Huyện Kiên Hải", "Huyện Kiên Lương", "Huyện Tân Hiệp", "Huyện U Minh Thượng", "Huyện Vĩnh Thuận"});
        mapDiaGioi.put("Kon Tum", new String[]{"TP. Kon Tum", "Huyện Đắk Glei", "Huyện Đắk Hà", "Huyện Đắk Tô", "Huyện Ia H'Drai", "Huyện Kon Plông", "Huyện Kon Rẫy", "Huyện Ngọc Hồi", "Huyện Sa Thầy", "Huyện Tu Mơ Rông"});
        mapDiaGioi.put("Lai Châu", new String[]{"TP. Lai Châu", "Huyện Mường Tè", "Huyện Nậm Nhùn", "Huyện Phong Thổ", "Huyện Sìn Hồ", "Huyện Tam Đường", "Huyện Tân Uyên", "Huyện Than Uyên"});
        mapDiaGioi.put("Lâm Đồng", new String[]{"TP. Đà Lạt", "TP. Bảo Lộc", "Huyện Bảo Lâm", "Huyện Cát Tiên", "Huyện Đạ Huoai", "Huyện Đạ Tẻh", "Huyện Đam Rông", "Huyện Di Linh", "Huyện Đơn Dương", "Huyện Đức Trọng", "Huyện Lạc Dương", "Huyện Lâm Hà"});
        mapDiaGioi.put("Lạng Sơn", new String[]{"TP. Lạng Sơn", "Huyện Bắc Sơn", "Huyện Bình Gia", "Huyện Cao Lộc", "Huyện Chi Lăng", "Huyện Đình Lập", "Huyện Hữu Lũng", "Huyện Lộc Bình", "Huyện Tràng Định", "Huyện Văn Lãng", "Huyện Văn Quan"});
        mapDiaGioi.put("Lào Cai", new String[]{"TP. Lào Cai", "TX. Sa Pa", "Huyện Bảo Thắng", "Huyện Bảo Yên", "Huyện Bát Xát", "Huyện Bắc Hà", "Huyện Mường Khương", "Huyện Si Ma Cai", "Huyện Văn Bàn"});
        mapDiaGioi.put("Long An", new String[]{"TP. Tân An", "TX. Kiến Tường", "Huyện Bến Lức", "Huyện Cần Đước", "Huyện Cần Giuộc", "Huyện Châu Thành", "Huyện Đức Hòa", "Huyện Đức Huệ", "Huyện Mộc Hóa", "Huyện Tân Hưng", "Huyện Tân Thạnh", "Huyện Tân Trụ", "Huyện Thạnh Hóa", "Huyện Thủ Thừa", "Huyện Vĩnh Hưng"});
        mapDiaGioi.put("Nam Định", new String[]{"TP. Nam Định", "Huyện Giao Thủy", "Huyện Hải Hậu", "Huyện Mỹ Lộc", "Huyện Nam Trực", "Huyện Nghĩa Hưng", "Huyện Trực Ninh", "Huyện Vụ Bản", "Huyện Xuân Trường", "Huyện Ý Yên"});
        mapDiaGioi.put("Nghệ An", new String[]{"TP. Vinh", "TX. Cửa Lò", "TX. Hoàng Mai", "TX. Thái Hòa", "Huyện Anh Sơn", "Huyện Con Cuông", "Huyện Diễn Châu", "Huyện Đô Lương", "Huyện Hưng Nguyên", "Huyện Kỳ Sơn", "Huyện Nam Đàn", "Huyện Nghi Lộc", "Huyện Nghĩa Đàn", "Huyện Quế Phong", "Huyện Quỳ Châu", "Huyện Quỳ Hợp", "Huyện Quỳnh Lưu", "Huyện Tân Kỳ", "Huyện Thanh Chương", "Huyện Tương Dương", "Huyện Yên Thành"});
        mapDiaGioi.put("Ninh Bình", new String[]{"TP. Ninh Bình", "TP. Tam Điệp", "Huyện Gia Viễn", "Huyện Hoa Lư", "Huyện Kim Sơn", "Huyện Nho Quan", "Huyện Yên Khánh", "Huyện Yên Mô"});
        mapDiaGioi.put("Ninh Thuận", new String[]{"TP. Phan Rang-Tháp Chàm", "Huyện Bác Ái", "Huyện Ninh Hải", "Huyện Ninh Phước", "Huyện Ninh Sơn", "Huyện Thuận Bắc", "Huyện Thuận Nam"});
        mapDiaGioi.put("Phú Thọ", new String[]{"TP. Việt Trì", "TX. Phú Thọ", "Huyện Cẩm Khê", "Huyện Đoan Hùng", "Huyện Hạ Hòa", "Huyện Lâm Thao", "Huyện Phù Ninh", "Huyện Tam Nông", "Huyện Tân Sơn", "Huyện Thanh Ba", "Huyện Thanh Sơn", "Huyện Thanh Thủy", "Huyện Yên Lập"});
        mapDiaGioi.put("Phú Yên", new String[]{"TP. Tuy Hòa", "TX. Đông Hòa", "TX. Sông Cầu", "Huyện Đồng Xuân", "Huyện Phú Hòa", "Huyện Sơn Hòa", "Huyện Sông Hinh", "Huyện Tây Hòa", "Huyện Tuy An"});
        mapDiaGioi.put("Quảng Bình", new String[]{"TP. Đồng Hới", "TX. Ba Đồn", "Huyện Bố Trạch", "Huyện Lệ Thủy", "Huyện Minh Hóa", "Huyện Quảng Ninh", "Huyện Quảng Trạch", "Huyện Tuyên Hóa"});
        mapDiaGioi.put("Quảng Nam", new String[]{"TP. Tam Kỳ", "TP. Hội An", "TX. Điện Bàn", "Huyện Bắc Trà My", "Huyện Đại Lộc", "Huyện Đông Giang", "Huyện Duy Xuyên", "Huyện Hiệp Đức", "Huyện Nam Giang", "Huyện Nam Trà My", "Huyện Nông Sơn", "Huyện Núi Thành", "Huyện Phú Ninh", "Huyện Phước Sơn", "Huyện Quế Sơn", "Huyện Tây Giang", "Huyện Thăng Bình", "Huyện Tiên Phước"});
        mapDiaGioi.put("Quảng Ngãi", new String[]{"TP. Quảng Ngãi", "TX. Đức Phổ", "Huyện Ba Tơ", "Huyện Bình Sơn", "Huyện Lý Sơn", "Huyện Minh Long", "Huyện Mộ Đức", "Huyện Nghĩa Hành", "Huyện Sơn Hà", "Huyện Sơn Tây", "Huyện Sơn Tịnh", "Huyện Trà Bồng", "Huyện Tư Nghĩa"});
        mapDiaGioi.put("Quảng Ninh", new String[]{"TP. Hạ Long", "TP. Cẩm Phả", "TP. Móng Cái", "TP. Uông Bí", "TX. Đông Triều", "TX. Quảng Yên", "Huyện Ba Chẽ", "Huyện Bình Liêu", "Huyện Cô Tô", "Huyện Đầm Hà", "Huyện Hải Hà", "Huyện Tiên Yên", "Huyện Vân Đồn"});
        mapDiaGioi.put("Quảng Trị", new String[]{"TP. Đông Hà", "TX. Quảng Trị", "Huyện Cam Lộ", "Huyện Cồn Cỏ", "Huyện Đa Krông", "Huyện Gio Linh", "Huyện Hải Lăng", "Huyện Hướng Hóa", "Huyện Triệu Phong", "Huyện Vĩnh Linh"});
        mapDiaGioi.put("Sóc Trăng", new String[]{"TP. Sóc Trăng", "TX. Ngã Năm", "TX. Vĩnh Châu", "Huyện Châu Thành", "Huyện Cù Lao Dung", "Huyện Kế Sách", "Huyện Long Phú", "Huyện Mỹ Tú", "Huyện Mỹ Xuyên", "Huyện Thạnh Trị", "Huyện Trần Đề"});
        mapDiaGioi.put("Sơn La", new String[]{"TP. Sơn La", "Huyện Bắc Yên", "Huyện Mai Sơn", "Huyện Mộc Châu", "Huyện Mường La", "Huyện Phù Yên", "Huyện Quỳnh Nhai", "Huyện Sông Mã", "Huyện Sốp Cộp", "Huyện Thuận Châu", "Huyện Vân Hồ", "Huyện Yên Châu"});
        mapDiaGioi.put("Tây Ninh", new String[]{"TP. Tây Ninh", "TX. Hòa Thành", "TX. Trảng Bàng", "Huyện Bến Cầu", "Huyện Châu Thành", "Huyện Dương Minh Châu", "Huyện Gò Dầu", "Huyện Tân Biên", "Huyện Tân Châu"});
        mapDiaGioi.put("Thái Bình", new String[]{"TP. Thái Bình", "Huyện Đông Hưng", "Huyện Hưng Hà", "Huyện Kiến Xương", "Huyện Quỳnh Phụ", "Huyện Thái Thụy", "Huyện Tiền Hải", "Huyện Vũ Thư"});
        mapDiaGioi.put("Thái Nguyên", new String[]{"TP. Thái Nguyên", "TP. Phổ Yên", "TP. Sông Công", "Huyện Đại Từ", "Huyện Định Hóa", "Huyện Đồng Hỷ", "Huyện Phú Bình", "Huyện Phú Lương", "Huyện Võ Nhai"});
        mapDiaGioi.put("Thanh Hóa", new String[]{"TP. Thanh Hóa", "TP. Sầm Sơn", "TX. Bỉm Sơn", "TX. Nghi Sơn", "Huyện Bá Thước", "Huyện Cẩm Thủy", "Huyện Đông Sơn", "Huyện Hà Trung", "Huyện Hậu Lộc", "Huyện Hoằng Hóa", "Huyện Lang Chánh", "Huyện Mường Lát", "Huyện Nga Sơn", "Huyện Ngọc Lặc", "Huyện Như Thanh", "Huyện Như Xuân", "Huyện Nông Cống", "Huyện Quan Hóa", "Huyện Quan Sơn", "Huyện Quảng Xương", "Huyện Thạch Thành", "Huyện Thiệu Hóa", "Huyện Thọ Xuân", "Huyện Thường Xuân", "Huyện Triệu Sơn", "Huyện Vĩnh Lộc", "Huyện Yên Định"});
        mapDiaGioi.put("Thừa Thiên Huế", new String[]{"TP. Huế", "TX. Hương Thủy", "TX. Hương Trà", "Huyện A Lưới", "Huyện Nam Đông", "Huyện Phong Điền", "Huyện Phú Lộc", "Huyện Phú Vang", "Huyện Quảng Điền"});
        mapDiaGioi.put("Tiền Giang", new String[]{"TP. Mỹ Tho", "TX. Cai Lậy", "TX. Gò Công", "Huyện Cái Bè", "Huyện Cai Lậy", "Huyện Châu Thành", "Huyện Chợ Gạo", "Huyện Gò Công Đông", "Huyện Gò Công Tây", "Huyện Tân Phú Đông", "Huyện Tân Phước"});
        mapDiaGioi.put("Trà Vinh", new String[]{"TP. Trà Vinh", "TX. Duyên Hải", "Huyện Càng Long", "Huyện Châu Thành", "Huyện Cầu Kè", "Huyện Cầu Ngang", "Huyện Duyên Hải", "Huyện Tiểu Cần", "Huyện Trà Cú"});
        mapDiaGioi.put("Tuyên Quang", new String[]{"TP. Tuyên Quang", "Huyện Chiêm Hóa", "Huyện Hàm Yên", "Huyện Lâm Bình", "Huyện Na Hang", "Huyện Sơn Dương", "Huyện Yên Sơn"});
        mapDiaGioi.put("Vĩnh Long", new String[]{"TP. Vĩnh Long", "TX. Bình Minh", "Huyện Bình Tân", "Huyện Long Hồ", "Huyện Mang Thít", "Huyện Tam Bình", "Huyện Trà Ôn", "Huyện Vũng Liêm"});
        mapDiaGioi.put("Vĩnh Phúc", new String[]{"TP. Vĩnh Yên", "TP. Phúc Yên", "Huyện Bình Xuyên", "Huyện Lập Thạch", "Huyện Sông Lô", "Huyện Tam Đảo", "Huyện Tam Dương", "Huyện Vĩnh Tường", "Huyện Yên Lạc"});
        mapDiaGioi.put("Yên Bái", new String[]{"TP. Yên Bái", "TX. Nghĩa Lộ", "Huyện Lục Yên", "Huyện Mù Cang Chải", "Huyện Trạm Tấu", "Huyện Trấn Yên", "Huyện Văn Chấn", "Huyện Văn Yên", "Huyện Yên Bình"});
    }

    private void tinhTongTien() {
        tongTien = 0;
        for (GioHangItem item : danhSachMua) {
            tongTien += (item.giaMoi * item.soLuong);
        }
        lblTongTien.setText("<html><b style='color:#cc0000; font-size:26px;'>" + String.format("%,.0f", tongTien) + " đ</b></html>");
    }

    private JTextField createPlaceholderField(String text) {
        JTextField field = new JTextField(text);
        field.setForeground(Color.GRAY);
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(text)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(text);
                }
            }
        });
        return field;
    }

    private JPanel taoPanelSanPham(GioHangItem item) {
        JPanel pnl = new JPanel(new BorderLayout(15, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // CHÈN CHECKBOX VÀO SẢN PHẨM
        JCheckBox chkChon = new JCheckBox();
        chkChon.setBackground(Color.WHITE);
        chkChon.setSelected(item.isSelected);
        chkChon.addActionListener(e -> item.isSelected = chkChon.isSelected());
        pnl.add(chkChon, BorderLayout.WEST);

        JPanel pnlCenterWrapper = new JPanel(new BorderLayout(10, 0));
        pnlCenterWrapper.setOpaque(false);

        JLabel lblHinhAnh = new JLabel(); ImageIcon rawIcon = null;
        if (item.hinhAnhPath != null && !item.hinhAnhPath.trim().isEmpty()) {
            String pathTrim = item.hinhAnhPath.trim();
            if (pathTrim.startsWith("http://") || pathTrim.startsWith("https://")) {
                try {
                    java.net.URL url = new java.net.URL(pathTrim);
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(connection.getInputStream());
                    if (img != null) rawIcon = new ImageIcon(img);
                } catch (Exception e) {}
            } else {
                File file = new File(pathTrim); if (file.exists()) rawIcon = new ImageIcon(pathTrim);
            }
        }
        if (rawIcon != null && rawIcon.getImage() != null) {
            lblHinhAnh.setIcon(new ImageIcon(rawIcon.getImage().getScaledInstance(80, 60, Image.SCALE_SMOOTH)));
        } else { lblHinhAnh.setText("[Lỗi ảnh]"); }
        pnlCenterWrapper.add(lblHinhAnh, BorderLayout.WEST);

        JPanel pnlInfo = new JPanel();
        pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.Y_AXIS));
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.add(new JLabel("<html><div style='width: 320px;'><b style='font-size:14px;'>" + item.ten + "</b><br><span style='color:gray; font-size:11px;'>" + item.cauHinh + "</span></div></html>"));

        JLabel lblXoaLe = new JLabel("<html><u style='color:red; font-size:11px;'>ⓧ Xóa</u></html>");
        lblXoaLe.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblXoaLe.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (JOptionPane.showConfirmDialog(ThanhToanDialog.this, "Bạn muốn xóa sản phẩm này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    parent.xoaKhoiGioHang(item.ten);
                    ThanhToanDialog.this.dispose();
                    if (!TrangChu.gioHang.isEmpty()) {
                        new ThanhToanDialog(parent, TrangChu.gioHang).setVisible(true);
                    } else { JOptionPane.showMessageDialog(parent, "Giỏ hàng đã trống!"); }
                }
            }
        });
        pnlInfo.add(Box.createVerticalStrut(5)); pnlInfo.add(lblXoaLe);
        pnlCenterWrapper.add(pnlInfo, BorderLayout.CENTER);
        pnl.add(pnlCenterWrapper, BorderLayout.CENTER);

        JPanel pnlRight = new JPanel(); pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS)); pnlRight.setBackground(Color.WHITE);
        JLabel lblGia = new JLabel("<html><b style='color:#cc0000; font-size:16px;'>" + String.format("%,.0f đ", item.giaMoi) + "</b></html>");
        lblGia.setAlignmentX(Component.RIGHT_ALIGNMENT); pnlRight.add(lblGia); pnlRight.add(Box.createVerticalStrut(10));

        JPanel pnlSoLuong = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); pnlSoLuong.setBackground(Color.WHITE);
        JButton btnTru = new JButton("-"); btnTru.setMargin(new Insets(1, 5, 1, 5)); btnTru.setBackground(Color.WHITE);
        JTextField txtSoLuong = new JTextField(String.valueOf(item.soLuong), 2); txtSoLuong.setHorizontalAlignment(JTextField.CENTER); txtSoLuong.setEditable(false);
        JButton btnCong = new JButton("+"); btnCong.setMargin(new Insets(1, 5, 1, 5)); btnCong.setBackground(Color.WHITE);

        btnTru.addActionListener(e -> { if (item.soLuong > 1) { item.soLuong--; txtSoLuong.setText(String.valueOf(item.soLuong)); parent.capNhatSoLuongGioHang(item.ten, item.soLuong); tinhTongTien(); } });
        btnCong.addActionListener(e -> { item.soLuong++; txtSoLuong.setText(String.valueOf(item.soLuong)); parent.capNhatSoLuongGioHang(item.ten, item.soLuong); tinhTongTien(); });

        pnlSoLuong.add(btnTru); pnlSoLuong.add(txtSoLuong); pnlSoLuong.add(btnCong); pnlSoLuong.setAlignmentX(Component.RIGHT_ALIGNMENT);
        pnlRight.add(pnlSoLuong); pnl.add(pnlRight, BorderLayout.EAST);

        return pnl;
    }
}
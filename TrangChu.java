package cuoiky;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TrangChu extends JFrame {
    private JPanel pnlDanhSachSP;
    private JButton btnGioHang, btnLogin, btnLichSuMuaHang, btnTestAdmin, btnTestKhach;

    private int trangHienTai = 1;
    private int soSanPhamMotTrang = 10;
    private int tongSoTrang = 1;
    private JLabel lblPhanTrang;
    private String thuongHieuDangLoc = "";

    private int giaIndex = 0;
    private int ramIndex = 0;

    public static String taiKhoanHienTai = "KhachVangLai";
    public static java.util.List<GioHangItem> gioHang = new java.util.ArrayList<>();

    public TrangChu() {
        setTitle("Hệ thống quản lý bán Laptop - Techcare Replica");
        setSize(1350, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel pnlTopWrapper = new JPanel(); pnlTopWrapper.setLayout(new BoxLayout(pnlTopWrapper, BoxLayout.Y_AXIS));
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(new Color(255, 102, 0));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel pnlHeaderLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); pnlHeaderLeft.setOpaque(false);
        JButton btnMenu = new JButton("Mục lục"); btnMenu.setFont(new Font("Arial", Font.BOLD, 16)); btnMenu.setBackground(new Color(255, 102, 0)); btnMenu.setForeground(Color.WHITE); btnMenu.setBorderPainted(false); btnMenu.setFocusPainted(false);
        JPopupMenu popupMenu = createDropdownMenu(); btnMenu.addMouseListener(new MouseAdapter() { public void mouseEntered(MouseEvent e) { popupMenu.show(btnMenu, 0, btnMenu.getHeight()); } });
        JLabel lblLogo = new JLabel("TECHCARE.vn"); lblLogo.setFont(new Font("Arial", Font.BOLD, 26)); lblLogo.setForeground(Color.WHITE);
        pnlHeaderLeft.add(btnMenu); pnlHeaderLeft.add(lblLogo);

        // --- ĐÃ FIX THANH TÌM KIẾM ---
        JPanel pnlHeaderCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        pnlHeaderCenter.setOpaque(false);
        pnlHeaderCenter.setBorder(new EmptyBorder(0, 5, 0, 5));

        JTextField txtSearch = new JTextField(" Bạn muốn tìm gì...");
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 14));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setPreferredSize(new Dimension(280, 35)); // Ép cứng chiều rộng thanh tìm kiếm
        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if (txtSearch.getText().equals(" Bạn muốn tìm gì...")) { txtSearch.setText(""); txtSearch.setForeground(Color.BLACK); } }
            public void focusLost(FocusEvent e) { if (txtSearch.getText().isEmpty()) { txtSearch.setText(" Bạn muốn tìm gì..."); txtSearch.setForeground(Color.GRAY); } }
        });
        JButton btnSearch = new JButton("tìm"); btnSearch.setBackground(Color.BLACK); btnSearch.setForeground(Color.WHITE); btnSearch.setPreferredSize(new Dimension(60, 35));
        btnSearch.addActionListener(e -> { thuongHieuDangLoc = txtSearch.getText().trim(); trangHienTai = 1; loadDuLieuTuSQL(); });

        pnlHeaderCenter.add(txtSearch);
        pnlHeaderCenter.add(btnSearch);

        // --- THU GỌN KHOẢNG CÁCH CÁC NÚT BÊN PHẢI ---
        JPanel pnlHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5)); pnlHeaderRight.setOpaque(false);

        btnTestAdmin = new JButton("Test Admin"); styleHeaderButton(btnTestAdmin); btnTestAdmin.setBackground(new Color(204, 0, 0)); btnTestAdmin.addActionListener(e -> capNhatDangNhapThanhCong("Quản Trị Viên", 1, "admin@techcare.vn"));
        btnTestKhach = new JButton("Test Khách"); styleHeaderButton(btnTestKhach); btnTestKhach.setBackground(new Color(0, 102, 204)); btnTestKhach.addActionListener(e -> capNhatDangNhapThanhCong("Khách Demo", 3, "demo.teacher@vku.edu.vn"));

        JButton btnReload = new JButton("Trang chủ"); styleHeaderButton(btnReload);
        btnReload.addActionListener(e -> { thuongHieuDangLoc = ""; giaIndex = 0; ramIndex = 0; trangHienTai = 1; loadDuLieuTuSQL(); });

        btnLichSuMuaHang = new JButton("Lịch sử Đơn hàng"); styleHeaderButton(btnLichSuMuaHang); btnLichSuMuaHang.setBackground(new Color(0, 153, 51)); btnLichSuMuaHang.setVisible(true);
        btnLichSuMuaHang.addActionListener(e -> { new LichSuGiaoDichFrame(false, taiKhoanHienTai).setVisible(true); });

        btnLogin = new JButton("Đăng Nhập"); styleHeaderButton(btnLogin);
        JPopupMenu popupTaiKhoan = new JPopupMenu(); JMenuItem mnuLogout = new JMenuItem("Đăng xuất"); popupTaiKhoan.add(mnuLogout);
        mnuLogout.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Đăng xuất tài khoản?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                btnLogin.setText("Đăng Nhập"); taiKhoanHienTai = "KhachVangLai";
                btnTestAdmin.setVisible(true);
                btnTestKhach.setVisible(true);
                btnLichSuMuaHang.setVisible(true); loadGioHangTuSQL();
            }
        });
        btnLogin.addActionListener(e -> { if (btnLogin.getText().equals("Đăng Nhập")) new DangNhapDialog(this).setVisible(true); else popupTaiKhoan.show(btnLogin, 0, btnLogin.getHeight()); });

        btnGioHang = new JButton("Giỏ hàng (0)"); btnGioHang.setFont(new Font("Arial", Font.BOLD, 13)); btnGioHang.setBackground(Color.WHITE); btnGioHang.setForeground(new Color(255, 102, 0)); btnGioHang.setPreferredSize(new Dimension(130, 35));
        btnGioHang.addActionListener(e -> { if (gioHang.isEmpty()) JOptionPane.showMessageDialog(this, "Giỏ hàng trống!"); else new ThanhToanDialog(this, gioHang).setVisible(true); });

        pnlHeaderRight.add(btnTestAdmin); pnlHeaderRight.add(btnTestKhach);
        pnlHeaderRight.add(btnReload); pnlHeaderRight.add(btnLichSuMuaHang); pnlHeaderRight.add(btnLogin); pnlHeaderRight.add(btnGioHang);

        headerPanel.add(pnlHeaderLeft, BorderLayout.WEST);
        headerPanel.add(pnlHeaderCenter, BorderLayout.CENTER);
        headerPanel.add(pnlHeaderRight, BorderLayout.EAST);
        pnlTopWrapper.add(headerPanel);

        JPanel pnlBrands = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10)); pnlBrands.setBackground(new Color(240, 242, 245)); pnlBrands.setBorder(new EmptyBorder(5, 0, 5, 0));

        JButton btnFilter = new JButton("LỌC SẢN PHẨM ▼");
        btnFilter.setFont(new Font("Arial", Font.BOLD, 13)); btnFilter.setBackground(Color.WHITE); btnFilter.setForeground(new Color(0, 102, 204)); btnFilter.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 2)); btnFilter.setPreferredSize(new Dimension(150, 35)); btnFilter.setFocusPainted(false);

        JPopupMenu popupFilter = new JPopupMenu();

        JMenu menuGia = new JMenu(" Mức Giá Của Máy "); menuGia.setFont(new Font("Arial", Font.BOLD, 13));
        String[] arrGia = {"Tất cả mức giá", "Dưới 15 triệu", "15 - 25 triệu", "Trên 25 triệu"};
        ButtonGroup bgGia = new ButtonGroup();
        for (int i = 0; i < arrGia.length; i++) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(arrGia[i]);
            if (i == 0) item.setSelected(true);
            final int idx = i;
            item.addActionListener(e -> { giaIndex = idx; trangHienTai = 1; loadDuLieuTuSQL(); });
            bgGia.add(item); menuGia.add(item);
        }

        JMenu menuRam = new JMenu(" Dung Lượng RAM "); menuRam.setFont(new Font("Arial", Font.BOLD, 13));
        String[] arrRam = {"Tất cả RAM", "8GB", "16GB", "32GB"};
        ButtonGroup bgRam = new ButtonGroup();
        for (int i = 0; i < arrRam.length; i++) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(arrRam[i]);
            if (i == 0) item.setSelected(true);
            final int idx = i;
            item.addActionListener(e -> { ramIndex = idx; trangHienTai = 1; loadDuLieuTuSQL(); });
            bgRam.add(item); menuRam.add(item);
        }

        popupFilter.add(menuGia); popupFilter.addSeparator(); popupFilter.add(menuRam);
        btnFilter.addActionListener(e -> popupFilter.show(btnFilter, 0, btnFilter.getHeight()));
        pnlBrands.add(btnFilter);

        String[] brands = {"Dell", "HP", "MSI", "Asus", "Lenovo", "Acer", "Macbook", "Surface"};
        for (String brand : brands) { JButton btnBrand = new JButton(brand.toUpperCase()); btnBrand.setFont(new Font("Arial", Font.BOLD, 13)); btnBrand.setBackground(Color.WHITE); btnBrand.setPreferredSize(new Dimension(100, 35)); btnBrand.addActionListener(e -> { thuongHieuDangLoc = brand; trangHienTai = 1; loadDuLieuTuSQL(); }); pnlBrands.add(btnBrand); }
        pnlTopWrapper.add(pnlBrands); add(pnlTopWrapper, BorderLayout.NORTH);

        JPanel pnlBodyWrapper = new JPanel(new BorderLayout()); pnlBodyWrapper.setBackground(new Color(245, 246, 250));
        pnlDanhSachSP = new JPanel(new GridLayout(0, 5, 15, 15)); pnlDanhSachSP.setBackground(new Color(245, 246, 250)); pnlDanhSachSP.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JScrollPane scrollPane = new JScrollPane(pnlDanhSachSP); scrollPane.setBorder(null); scrollPane.getVerticalScrollBar().setUnitIncrement(30);
        pnlBodyWrapper.add(scrollPane, BorderLayout.CENTER);

        JPanel pnlPagination = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15)); pnlPagination.setBackground(Color.WHITE); pnlPagination.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        JButton btnPrev = new JButton("<< Trang Trước"); styleHeaderButton(btnPrev); lblPhanTrang = new JLabel("Trang 1 / 1"); lblPhanTrang.setFont(new Font("Arial", Font.BOLD, 14)); JButton btnNext = new JButton("Trang Sau >>"); styleHeaderButton(btnNext);
        btnPrev.addActionListener(e -> { if (trangHienTai > 1) { trangHienTai--; loadDuLieuTuSQL(); } });
        btnNext.addActionListener(e -> { if (trangHienTai < tongSoTrang) { trangHienTai++; loadDuLieuTuSQL(); } });
        pnlPagination.add(btnPrev); pnlPagination.add(lblPhanTrang); pnlPagination.add(btnNext);
        pnlBodyWrapper.add(pnlPagination, BorderLayout.SOUTH); add(pnlBodyWrapper, BorderLayout.CENTER);

        JPanel floatPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10)); floatPanel.setOpaque(false);
        JButton btnFacebook = new JButton("Báo lỗi / Hỗ trợ 24/7"); btnFacebook.setBackground(new Color(24, 119, 242)); btnFacebook.setForeground(Color.WHITE); btnFacebook.setFont(new Font("Arial", Font.BOLD, 13)); btnFacebook.setFocusPainted(false);
        btnFacebook.addActionListener(e -> { try { Desktop.getDesktop().browse(new URI("https://www.facebook.com/")); } catch (Exception ex) {} });
        floatPanel.add(btnFacebook); add(floatPanel, BorderLayout.SOUTH);

        loadGioHangTuSQL();
        loadDuLieuTuSQL();
    }

    public void capNhatDangNhapThanhCong(String hoTen, int role, String email) {
        btnLogin.setText("Hi, " + (hoTen.length() > 15 ? hoTen.substring(0, 12) + "..." : hoTen));
        taiKhoanHienTai = email; loadGioHangTuSQL();

        if (role != 1) {
            btnTestAdmin.setVisible(false);
            btnTestKhach.setVisible(false);
            btnLichSuMuaHang.setVisible(true);
        } else {
            btnTestAdmin.setVisible(true);
            btnTestKhach.setVisible(false);
            btnLichSuMuaHang.setVisible(false);
            new AdminFrame(this).setVisible(true);
        }
    }

    private void styleHeaderButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(new Color(255, 120, 30));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(125, 35)); // Thu nhỏ chút xíu để nhường chỗ
    }

    private void loadGioHangTuSQL() { gioHang.clear(); try (Connection conn = DatabaseConnection.getConnection()) { PreparedStatement ps = conn.prepareStatement("SELECT * FROM GioHang WHERE TaiKhoan = ?"); ps.setString(1, taiKhoanHienTai); ResultSet rs = ps.executeQuery(); while (rs.next()) { gioHang.add(new GioHangItem(rs.getString("TenSP"), rs.getDouble("GiaGoc"), rs.getDouble("GiaMoi"), rs.getString("HinhAnhPath"), rs.getString("CauHinh"), rs.getInt("SoLuong"))); } capNhatHienThiGioHang(); } catch (Exception e) {} }
    public void themVaoGioHang(GioHangItem item) { try (Connection conn = DatabaseConnection.getConnection()) { PreparedStatement ps = conn.prepareStatement("INSERT INTO GioHang (TenSP, GiaGoc, GiaMoi, HinhAnhPath, CauHinh, SoLuong, TaiKhoan) VALUES (?,?,?,?,?,?,?)"); ps.setString(1, item.ten); ps.setDouble(2, item.giaGoc); ps.setDouble(3, item.giaMoi); ps.setString(4, item.hinhAnhPath); ps.setString(5, item.cauHinh); ps.setInt(6, item.soLuong); ps.setString(7, taiKhoanHienTai); ps.executeUpdate(); loadGioHangTuSQL(); } catch (Exception e) {} }
    public void xoaKhoiGioHang(String tenSP) { try (Connection conn = DatabaseConnection.getConnection()) { PreparedStatement ps = conn.prepareStatement("DELETE FROM GioHang WHERE TenSP = ? AND TaiKhoan = ?"); ps.setString(1, tenSP); ps.setString(2, taiKhoanHienTai); ps.executeUpdate(); loadGioHangTuSQL(); } catch (Exception e) {} }
    public void capNhatSoLuongGioHang(String tenSP, int slMoi) { try (Connection conn = DatabaseConnection.getConnection()) { PreparedStatement ps = conn.prepareStatement("UPDATE GioHang SET SoLuong = ? WHERE TenSP = ? AND TaiKhoan = ?"); ps.setInt(1, slMoi); ps.setString(2, tenSP); ps.setString(3, taiKhoanHienTai); ps.executeUpdate(); loadGioHangTuSQL(); } catch (Exception e) {} }
    public void lamSachGioHang() { try (Connection conn = DatabaseConnection.getConnection()) { PreparedStatement ps = conn.prepareStatement("DELETE FROM GioHang WHERE TaiKhoan = ?"); ps.setString(1, taiKhoanHienTai); ps.executeUpdate(); loadGioHangTuSQL(); } catch (Exception e) {} }
    private void capNhatHienThiGioHang() { int MathS = 0; for (GioHangItem sp : gioHang) MathS += sp.soLuong; btnGioHang.setText("🛒 Giỏ hàng (" + MathS + ")"); }

    public void loadDuLieuTuSQL() {
        pnlDanhSachSP.removeAll(); pnlDanhSachSP.setLayout(new GridLayout(0, 5, 15, 15));
        try (Connection conn = DatabaseConnection.getConnection()) {
            String whereClause = " WHERE 1=1 ";
            if (!thuongHieuDangLoc.isEmpty() && !thuongHieuDangLoc.equals(" Bạn muốn tìm gì...")) { whereClause += " AND (s.TenSP LIKE N'%" + thuongHieuDangLoc + "%' OR h.TenHang = '" + thuongHieuDangLoc + "') "; }
            if (giaIndex == 1) whereClause += " AND (s.GiaGoc * (100 - s.PhanTramGiam) / 100) < 15000000 ";
            else if (giaIndex == 2) whereClause += " AND (s.GiaGoc * (100 - s.PhanTramGiam) / 100) BETWEEN 15000000 AND 25000000 ";
            else if (giaIndex == 3) whereClause += " AND (s.GiaGoc * (100 - s.PhanTramGiam) / 100) > 25000000 ";
            if (ramIndex == 1) whereClause += " AND s.RAM LIKE '%8GB%' ";
            else if (ramIndex == 2) whereClause += " AND s.RAM LIKE '%16GB%' ";
            else if (ramIndex == 3) whereClause += " AND s.RAM LIKE '%32GB%' ";

            String countSql = "SELECT COUNT(*) AS Total FROM SanPham s INNER JOIN HangLaptop h ON s.MaHang = h.MaHang " + whereClause;
            PreparedStatement psCount = conn.prepareStatement(countSql); ResultSet rsCount = psCount.executeQuery();
            int totalItems = 0; if (rsCount.next()) totalItems = rsCount.getInt("Total");
            tongSoTrang = (int) Math.ceil((double) totalItems / soSanPhamMotTrang); if(tongSoTrang == 0) tongSoTrang = 1;
            lblPhanTrang.setText("Trang " + trangHienTai + " / " + tongSoTrang);

            int offset = (trangHienTai - 1) * soSanPhamMotTrang;
            String sql = "SELECT s.*, h.TenHang FROM SanPham s INNER JOIN HangLaptop h ON s.MaHang = h.MaHang " + whereClause + " ORDER BY s.MaSP OFFSET " + offset + " ROWS FETCH NEXT " + soSanPhamMotTrang + " ROWS ONLY";
            PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery(); boolean coSanPham = false;

            while (rs.next()) {
                coSanPham = true;
                final String ten = rs.getString("TenSP"); final double giaGoc = rs.getDouble("GiaGoc"); final int giamGia = rs.getInt("PhanTramGiam");
                final String hinhAnhPath = rs.getString("HinhAnh"); final String cpu = rs.getString("CPU"); final String ram = rs.getString("RAM");
                final String ssd = rs.getString("SSD"); final String vga = rs.getString("VGA"); final String manHinh = rs.getString("ManHinh");
                final int tonKho = rs.getInt("SoLuongTonKho");
                final double giaMoi = giaGoc - (giaGoc * giamGia / 100);

                JPanel cardSP = new JPanel(); cardSP.setLayout(new BoxLayout(cardSP, BoxLayout.Y_AXIS)); cardSP.setBackground(Color.WHITE); cardSP.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
                JLabel lblHinhAnh = new JLabel(); lblHinhAnh.setAlignmentX(Component.CENTER_ALIGNMENT); lblHinhAnh.setPreferredSize(new Dimension(180, 130)); lblHinhAnh.setMaximumSize(new Dimension(180, 130)); lblHinhAnh.setHorizontalAlignment(SwingConstants.CENTER);
                ImageIcon rawIcon = null;
                if (hinhAnhPath != null && !hinhAnhPath.trim().isEmpty()) { String pathTrim = hinhAnhPath.trim(); if (pathTrim.startsWith("http://") || pathTrim.startsWith("https://")) { try { java.net.URL url = new java.net.URL(pathTrim); java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection(); connection.setRequestProperty("User-Agent", "Mozilla/5.0"); java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(connection.getInputStream()); if (img != null) rawIcon = new ImageIcon(img); } catch (Exception e) {} } else { File file = new File(pathTrim); if (file.exists()) rawIcon = new ImageIcon(pathTrim); } }
                if (rawIcon != null && rawIcon.getImage() != null) { lblHinhAnh.setIcon(new ImageIcon(rawIcon.getImage().getScaledInstance(180, 130, Image.SCALE_SMOOTH))); } else { lblHinhAnh.setText("[Không có ảnh]"); lblHinhAnh.setForeground(Color.GRAY); }

                JLabel lblTen = new JLabel("<html><div style='text-align: left; width: 170px; font-size:12px; height:35px;'><b>" + ten + "</b></div></html>"); lblTen.setAlignmentX(Component.CENTER_ALIGNMENT);
                JLabel lblGia = new JLabel("<html><b style='color:#ff6600;font-size:15px'>" + String.format("%,.0f đ", giaMoi) + "</b> <span style='background-color:#ff9900; color:white; font-size:9px; padding:1px 3px; border-radius:3px;'>-" + giamGia + "%</span><br><strike style='color:gray; font-size:11px'>" + String.format("%,.0f đ", giaGoc) + "</strike></html>"); lblGia.setAlignmentX(Component.CENTER_ALIGNMENT);

                String tonKhoHtml = tonKho > 0 ? "<span style='color:green;'>Còn lại: " + tonKho + " máy</span>" : "<span style='color:red;'><b>HẾT HÀNG</b></span>";
                JLabel lblThongSo = new JLabel("<html><div style='background-color:#F8F9FA; padding:6px; width:170px; font-size:10px; color:#555555;'>⚙ " + cpu + "<br>RAM " + ram + " | " + ssd + "<br>" + tonKhoHtml + "</div></html>");
                lblThongSo.setAlignmentX(Component.CENTER_ALIGNMENT);

                cardSP.add(lblHinhAnh); cardSP.add(Box.createVerticalStrut(10)); cardSP.add(lblTen); cardSP.add(Box.createVerticalStrut(8)); cardSP.add(lblGia); cardSP.add(Box.createVerticalStrut(8)); cardSP.add(lblThongSo);
                cardSP.setCursor(new Cursor(Cursor.HAND_CURSOR));
                cardSP.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { new ChiTietSanPhamDialog(TrangChu.this, ten, giaGoc, giaMoi, hinhAnhPath, cpu, ram, ssd, vga, manHinh, tonKho).setVisible(true); } });
                pnlDanhSachSP.add(cardSP);
            }
            if (!coSanPham) { pnlDanhSachSP.setLayout(new BorderLayout()); JLabel lblRong = new JLabel("Không tìm thấy sản phẩm phù hợp ở trang này!"); lblRong.setFont(new Font("Arial", Font.BOLD, 16)); lblRong.setHorizontalAlignment(SwingConstants.CENTER); pnlDanhSachSP.add(lblRong, BorderLayout.CENTER); }
        } catch (Exception ex) { ex.printStackTrace(); }
        pnlDanhSachSP.revalidate(); pnlDanhSachSP.repaint();
    }

    private JPopupMenu createDropdownMenu() { JPopupMenu popup = new JPopupMenu(); JMenu menuLaptopMoi = new JMenu("Laptop Mới"); JMenuItem mnuDell = new JMenuItem("Laptop Dell"); mnuDell.addActionListener(e -> { thuongHieuDangLoc = "Dell"; trangHienTai = 1; loadDuLieuTuSQL(); }); JMenuItem mnuHP = new JMenuItem("Laptop HP"); mnuHP.addActionListener(e -> { thuongHieuDangLoc = "HP"; trangHienTai = 1; loadDuLieuTuSQL(); }); menuLaptopMoi.add(mnuDell); menuLaptopMoi.add(mnuHP); popup.add(menuLaptopMoi); return popup; }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new TrangChu().setVisible(true)); }
}
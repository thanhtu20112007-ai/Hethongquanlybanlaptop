package cuoiky;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

public class AdminFrame extends JFrame {
    private JTextField txtTenSP, txtGiaGoc, txtGiamGia, txtHinhAnh, txtCPU, txtRAM, txtSSD, txtVGA, txtManHinh, txtTonKho;
    private JComboBox<String> cbxHang;
    private JTable tblSanPham;
    private DefaultTableModel tableModel;

    private String maSPDangChon = "";
    private TrangChu trangChu;

    public AdminFrame(TrangChu trangChu) {
        this.trangChu = trangChu;
        setTitle("Admin - Quản Lý Sản Phẩm");
        setSize(1050, 780);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(11, 2, 10, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết Laptop"));

        txtTenSP = new JTextField(); txtGiaGoc = new JTextField(); txtGiamGia = new JTextField("0");
        txtCPU = new JTextField(); txtRAM = new JTextField(); txtSSD = new JTextField();
        txtVGA = new JTextField(); txtManHinh = new JTextField(); txtTonKho = new JTextField("20");
        cbxHang = new JComboBox<>(new String[]{"Dell", "HP", "Asus", "Lenovo", "Acer", "Macbook", "MSI", "Surface"});
        txtHinhAnh = new JTextField();

        formPanel.add(new JLabel("Tên Laptop: (*)")); formPanel.add(txtTenSP);
        formPanel.add(new JLabel("Hãng:")); formPanel.add(cbxHang);
        formPanel.add(new JLabel("Giá gốc (VNĐ): (*)")); formPanel.add(txtGiaGoc);
        formPanel.add(new JLabel("Giảm giá (%) hoặc Giá mới:")); formPanel.add(txtGiamGia);
        formPanel.add(new JLabel("Số lượng tồn kho:")); formPanel.add(txtTonKho);
        formPanel.add(new JLabel("Link ảnh mạng (URL):")); formPanel.add(txtHinhAnh);
        formPanel.add(new JLabel("CPU:")); formPanel.add(txtCPU);
        formPanel.add(new JLabel("RAM:")); formPanel.add(txtRAM);
        formPanel.add(new JLabel("SSD:")); formPanel.add(txtSSD);
        formPanel.add(new JLabel("VGA:")); formPanel.add(txtVGA);
        formPanel.add(new JLabel("Màn hình:")); formPanel.add(txtManHinh);
        add(formPanel, BorderLayout.NORTH);

        String[] columns = {"STT", "Mã SP", "Tên SP", "Giá Gốc", "Tồn Kho", "RAM"};
        tableModel = new DefaultTableModel(columns, 0);
        tblSanPham = new JTable(tableModel);
        JScrollPane scrollTable = new JScrollPane(tblSanPham);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Danh sách Laptop"));

        tblSanPham.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tblSanPham.getSelectedRow();
                if (row >= 0) {
                    maSPDangChon = tableModel.getValueAt(row, 1).toString();
                    hienThiChiTietLenForm(maSPDangChon);
                }
            }
        });
        add(scrollTable, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnThem = new JButton("THÊM MỚI"); btnThem.setBackground(new Color(0, 153, 51)); btnThem.setForeground(Color.WHITE);
        JButton btnSua = new JButton("CẬP NHẬT"); btnSua.setBackground(new Color(255, 153, 0)); btnSua.setForeground(Color.WHITE);
        JButton btnXoa = new JButton("XÓA SẢN PHẨM"); btnXoa.setBackground(new Color(204, 0, 0)); btnXoa.setForeground(Color.WHITE);

        JButton btnTaoAo = new JButton("TẠO 50 SP ẢO"); btnTaoAo.setBackground(new Color(40, 167, 69)); btnTaoAo.setForeground(Color.WHITE);
        JButton btnLichSuGD = new JButton("LỊCH SỬ"); btnLichSuGD.setBackground(new Color(72, 44, 115)); btnLichSuGD.setForeground(Color.WHITE);
        JButton btnThongKe = new JButton("DOANH THU"); btnThongKe.setBackground(new Color(0, 102, 204)); btnThongKe.setForeground(Color.WHITE);
        JButton btnClear = new JButton("LÀM TRỐNG");

        btnThem.addActionListener(e -> xuLyThem());
        btnSua.addActionListener(e -> xuLySua());
        btnXoa.addActionListener(e -> xuLyXoa());
        btnClear.addActionListener(e -> lamTrongForm());
        btnLichSuGD.addActionListener(e -> new LichSuGiaoDichFrame(true, "Admin").setVisible(true));
        btnThongKe.addActionListener(e -> new ThongKeFrame().setVisible(true));
        btnTaoAo.addActionListener(e -> tao50SanPhamNgauNhien());

        btnPanel.add(btnThem); btnPanel.add(btnSua); btnPanel.add(btnXoa);
        btnPanel.add(btnTaoAo);
        btnPanel.add(btnLichSuGD); btnPanel.add(btnThongKe); btnPanel.add(btnClear);
        add(btnPanel, BorderLayout.SOUTH);

        loadDataVaoBang();
    }

    // --- BỨC TƯỜNG KIỂM DUYỆT DỮ LIỆU (VALIDATION) ---
    private boolean kiemTraHopLe() {
        if (txtTenSP.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên Laptop không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtTenSP.requestFocus();
            return false;
        }
        String strGiaGoc = txtGiaGoc.getText().replaceAll("[^0-9]", "");
        if (strGiaGoc.isEmpty() || Double.parseDouble(strGiaGoc) <= 0) {
            JOptionPane.showMessageDialog(this, "Giá gốc phải là một số lớn hơn 0!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtGiaGoc.requestFocus();
            return false;
        }
        return true;
    }

    private void tao50SanPhamNgauNhien() {
        int confirm = JOptionPane.showConfirmDialog(this, "Hệ thống sẽ tự động tạo 50 sản phẩm ngẫu nhiên\nvới thông số khác nhau để làm dữ liệu mẫu. Tiếp tục?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String[] brands = {"Dell", "HP", "Asus", "Lenovo", "Acer", "Macbook", "MSI", "Surface"};
        String[] cpus = {"Intel Core i5 12400F", "Intel Core i7 13700H", "AMD Ryzen 5 5600H", "AMD Ryzen 7 6800H", "Apple M2"};
        String[] rams = {"8GB DDR4 3200MHz", "16GB DDR4 3200MHz", "16GB DDR5 4800MHz", "32GB DDR5 5600MHz"};
        String[] ssds = {"256GB PCIe NVMe", "512GB PCIe NVMe", "1TB PCIe Gen4"};
        String[] vgas = {"Intel Iris Xe Graphics", "NVIDIA RTX 3050 4GB", "NVIDIA RTX 4060 8GB", "AMD Radeon Graphics"};
        String[] manHinhs = {"14 inch FHD IPS", "15.6 inch FHD 144Hz", "16 inch 2.5K 165Hz"};

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO SanPham (MaSP, TenSP, MaHang, GiaGoc, PhanTramGiam, HinhAnh, CPU, RAM, SSD, VGA, ManHinh, SoLuongTonKho) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            Random rand = new Random();

            for (int i = 0; i < 50; i++) {
                int hangIndex = rand.nextInt(brands.length);
                String tenSP = brands[hangIndex] + " Laptop Mẫu " + rand.nextInt(9999);
                double giaGoc = 10000000 + rand.nextInt(40) * 1000000;
                int giamGia = rand.nextInt(25);
                int tonKho = 10 + rand.nextInt(91);

                ps.setString(1, taoMaSanPhamNgauNhien());
                ps.setString(2, tenSP);
                ps.setInt(3, hangIndex + 1);
                ps.setDouble(4, giaGoc);
                ps.setInt(5, giamGia);
                ps.setString(6, "");
                ps.setString(7, cpus[rand.nextInt(cpus.length)]);
                ps.setString(8, rams[rand.nextInt(rams.length)]);
                ps.setString(9, ssds[rand.nextInt(ssds.length)]);
                ps.setString(10, vgas[rand.nextInt(vgas.length)]);
                ps.setString(11, manHinhs[rand.nextInt(manHinhs.length)]);
                ps.setInt(12, tonKho);

                ps.addBatch();
            }
            ps.executeBatch();

            JOptionPane.showMessageDialog(this, "Tuyệt vời! Đã bơm thành công 50 sản phẩm mẫu vào hệ thống!");
            capNhatGiaoDien();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo dữ liệu ảo: " + ex.getMessage(), "Báo lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String taoMaSanPhamNgauNhien() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 10; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private void loadDataVaoBang() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            ResultSet rs = conn.prepareStatement("SELECT MaSP, TenSP, GiaGoc, SoLuongTonKho, RAM FROM SanPham").executeQuery();
            int stt = 1;
            while (rs.next()) {
                tableModel.addRow(new Object[]{stt++, rs.getString("MaSP"), rs.getString("TenSP"), rs.getDouble("GiaGoc"), rs.getInt("SoLuongTonKho"), rs.getString("RAM")});
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void hienThiChiTietLenForm(String maSP) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM SanPham WHERE MaSP = ?");
            ps.setString(1, maSP);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtTenSP.setText(rs.getString("TenSP")); cbxHang.setSelectedIndex(rs.getInt("MaHang") - 1);
                txtGiaGoc.setText(String.format("%.0f", rs.getDouble("GiaGoc"))); txtGiamGia.setText(String.valueOf(rs.getInt("PhanTramGiam")));
                txtTonKho.setText(String.valueOf(rs.getInt("SoLuongTonKho")));
                txtHinhAnh.setText(rs.getString("HinhAnh")); txtCPU.setText(rs.getString("CPU"));
                txtRAM.setText(rs.getString("RAM")); txtSSD.setText(rs.getString("SSD"));
                txtVGA.setText(rs.getString("VGA")); txtManHinh.setText(rs.getString("ManHinh"));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void xuLyThem() {
        // Gọi hàm kiểm duyệt trước khi chạy SQL
        if (!kiemTraHopLe()) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String maSPMoi = taoMaSanPhamNgauNhien();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO SanPham (MaSP, TenSP, MaHang, GiaGoc, PhanTramGiam, HinhAnh, CPU, RAM, SSD, VGA, ManHinh, SoLuongTonKho) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, maSPMoi);
            gắnDữLiệuVàoQuery(ps, 2);
            if (ps.executeUpdate() > 0) { JOptionPane.showMessageDialog(this, "Đã THÊM thành công!\nMã SP: " + maSPMoi); capNhatGiaoDien(); }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage()); }
    }

    private void xuLySua() {
        if (maSPDangChon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 sản phẩm trong bảng để cập nhật!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Gọi hàm kiểm duyệt trước khi chạy SQL
        if (!kiemTraHopLe()) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE SanPham SET TenSP=?, MaHang=?, GiaGoc=?, PhanTramGiam=?, HinhAnh=?, CPU=?, RAM=?, SSD=?, VGA=?, ManHinh=?, SoLuongTonKho=? WHERE MaSP=?");
            gắnDữLiệuVàoQuery(ps, 1);
            ps.setString(12, maSPDangChon);
            if (ps.executeUpdate() > 0) { JOptionPane.showMessageDialog(this, "Đã CẬP NHẬT thành công!"); capNhatGiaoDien(); }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage()); }
    }

    private void xuLyXoa() {
        if (maSPDangChon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 sản phẩm trong bảng để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Chắc chắn xóa sản phẩm " + maSPDangChon + "?", "Cảnh báo", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM SanPham WHERE MaSP = ?");
                ps.setString(1, maSPDangChon);
                if (ps.executeUpdate() > 0) { JOptionPane.showMessageDialog(this, "Đã XÓA thành công!"); capNhatGiaoDien(); }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi: Không thể xóa vì sản phẩm này đã tồn tại trong Lịch sử Đơn Hàng!\nChi tiết: " + ex.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void gắnDữLiệuVàoQuery(PreparedStatement ps, int startIndex) throws Exception {
        ps.setString(startIndex, txtTenSP.getText().trim());
        ps.setInt(startIndex + 1, cbxHang.getSelectedIndex() + 1);
        String strGiaGoc = txtGiaGoc.getText().replaceAll("[^0-9]", "");
        String strGiamGia = txtGiamGia.getText().replaceAll("[^0-9]", "");
        String strTonKho = txtTonKho.getText().replaceAll("[^0-9]", "");

        double giaGoc = strGiaGoc.isEmpty() ? 0 : Double.parseDouble(strGiaGoc);
        int giamGia = strGiamGia.isEmpty() ? 0 : Integer.parseInt(strGiamGia);
        int tonKho = strTonKho.isEmpty() ? 0 : Integer.parseInt(strTonKho);

        if (giamGia > 100 && giaGoc > 0) giamGia = (int) Math.round(((giaGoc - giamGia) / giaGoc) * 100);

        ps.setDouble(startIndex + 2, giaGoc);
        ps.setInt(startIndex + 3, giamGia);
        ps.setString(startIndex + 4, txtHinhAnh.getText().trim());
        ps.setString(startIndex + 5, txtCPU.getText().trim());
        ps.setString(startIndex + 6, txtRAM.getText().trim());
        ps.setString(startIndex + 7, txtSSD.getText().trim());
        ps.setString(startIndex + 8, txtVGA.getText().trim());
        ps.setString(startIndex + 9, txtManHinh.getText().trim());
        ps.setInt(startIndex + 10, tonKho);
    }

    private void capNhatGiaoDien() {
        loadDataVaoBang();
        lamTrongForm();
        if (trangChu != null) trangChu.loadDuLieuTuSQL();
    }

    private void lamTrongForm() {
        txtTenSP.setText(""); txtGiaGoc.setText(""); txtGiamGia.setText("0"); txtTonKho.setText("20");
        txtHinhAnh.setText(""); txtCPU.setText(""); txtRAM.setText(""); txtSSD.setText("");
        txtVGA.setText(""); txtManHinh.setText(""); maSPDangChon = "";
    }
}